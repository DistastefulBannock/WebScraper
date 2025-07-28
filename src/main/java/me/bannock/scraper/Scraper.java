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
import me.bannock.scraper.plugins.impl.JarPluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Scraper {

    @Inject
    private Scraper(LinkService linkService, OptionManager optionManager, Set<Crawler> crawlers){
        this.linkService = linkService;

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
    }

    private final Logger logger = LogManager.getLogger();
    private final LinkService linkService;
    private Crawler crawler = null;

    private void start(){
        logger.info("Starting scraper...");
        while (linkService.hasMoreLinks()){
            String link = linkService.getNextLink();
            logger.info("Crawling link, link={}", link);
            crawler.crawlLink(link);
        }
        logger.info("No more links to crawl, stopping scraper...");
    }

    public static void main(String[] args) {
        PluginManager pluginManager = new JarPluginManager();
        pluginManager.loadPlugins();
        Injector injector = Guice.createInjector(Modules.override(new ScraperModule()).with(pluginManager.getModules()));
        injector.getInstance(Scraper.class).start();
    }

}
