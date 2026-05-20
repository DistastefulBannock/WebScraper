package me.bannock.scraper;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import me.bannock.scraper.crawlers.Crawler;
import me.bannock.scraper.links.LinkService;
import me.bannock.scraper.options.OptionKeys;
import me.bannock.scraper.options.OptionManager;
import me.bannock.scraper.plugins.PluginManager;
import me.bannock.scraper.plugins.api.Plugin;
import me.bannock.scraper.plugins.impl.JarPluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Scraper {

    @Inject
    private Scraper(LinkService linkService, PluginManager pluginManager, Injector injector,
                    OptionManager optionManager, Set<Crawler> crawlers){
        this.linkService = linkService;
        this.pluginManager = pluginManager;
        this.injector = injector;

        if (optionManager.getVariable(OptionKeys.crawlerClass).isEmpty()) {
            optionManager.setVariable(OptionKeys.crawlerClass, "me.bannock.scraper.crawlers.MockCrawlerImpl");
            optionManager.saveVariables();
        }
        String crawlerClass = optionManager.getVariable(OptionKeys.crawlerClass).get();
        for (Crawler crawler : crawlers){
            String crawlerInstanceClass = crawler.getClass().getCanonicalName();
            logger.info("Found crawler, crawler={}", crawlerInstanceClass);
            if (crawlerInstanceClass.equals(crawlerClass)){
                this.crawler = crawler;
            }
        }
        if (this.crawler == null){
            logger.error("Crawler class instance could not be found. Plugin may be missing or scraper is misconfigured, crawlerClass={}", crawlerClass);
            throw new IllegalStateException("Could not find crawler class instance. Plugin may be missing or scraper is misconfigured");
        }
        logger.info("Using configured crawler, crawler={}", crawlerClass);

        if (optionManager.getVariable(OptionKeys.waitInsteadOfExitForNoMoreLinks).isEmpty()) {
            optionManager.setVariable(OptionKeys.waitInsteadOfExitForNoMoreLinks, "false");
            optionManager.saveVariables();
        }
        this.dedicatedMode = Boolean.parseBoolean(optionManager.getVariable(OptionKeys.waitInsteadOfExitForNoMoreLinks).get());
        optionManager.setVariable(OptionKeys.waitInsteadOfExitForNoMoreLinks, dedicatedMode + "");
        optionManager.saveVariables();
    }

    private final Logger logger = LogManager.getLogger();
    private final LinkService linkService;
    private final PluginManager pluginManager;
    private final Injector injector;
    private Crawler crawler = null;
    private final boolean dedicatedMode;
    private volatile boolean running = true;

    private void start(){
        logger.info("Starting scraper...");

        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!running)
                return;
            logger.info("Shutdown signal received, stopping scraper...");
            this.running = false;
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            LogManager.shutdown();
        }, "scraper-shutdown-hook"));

        try {
            runCrawlLoop();
        } catch (Exception e){
            logger.warn("Something went wrong while crawling pages", e);
        }
        logger.info("Telling plugins to exit...");
        for (Plugin plugin : pluginManager.getPlugins())
            plugin.onExit(injector);
        logger.info("Exiting...");
        this.running = false; // Tells shutdown hook the graceful exist was already handled. Prevents hang in some environments
    }

    private void runCrawlLoop(){
        do{
            while (linkService.hasMoreLinks() && running){
                String link = linkService.getNextLink();
                logger.info("Crawling link, link={}", link);
                crawler.crawlLink(link);
            }
            if (!running)
                break;

            if (dedicatedMode){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for more links", e);
                }
            }else{
                logger.info("No more links to crawl, stopping scraper...");
            }
        }while (dedicatedMode && running);
    }

    public static void main(String[] args) {
        PluginManager pluginManager = new JarPluginManager();
        pluginManager.loadPlugins();
        Injector injector = Guice.createInjector(Modules.override(new ScraperModule(pluginManager)).with(pluginManager.getModules()));
        injector.getInstance(Scraper.class).start();
    }

}
