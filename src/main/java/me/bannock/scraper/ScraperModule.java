package me.bannock.scraper;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.bannock.scraper.crawlers.Crawler;
import me.bannock.scraper.crawlers.MockCrawlerImpl;
import me.bannock.scraper.data.DataService;
import me.bannock.scraper.data.SimpleDataServiceImpl;
import me.bannock.scraper.data.savers.DataSaver;
import me.bannock.scraper.links.LinkService;
import me.bannock.scraper.links.SimpleLinkServiceImpl;
import me.bannock.scraper.links.providers.LinkProvider;
import me.bannock.scraper.options.JsonFileOptionManagerImpl;
import me.bannock.scraper.options.OptionManager;
import me.bannock.scraper.plugins.PluginManager;

public class ScraperModule extends AbstractModule {

    public ScraperModule(PluginManager pluginManager){
        this.pluginManager = pluginManager;
    }

    private final PluginManager pluginManager;

    @Override
    protected void configure() {
        bind(PluginManager.class).toInstance(pluginManager);
        bind(OptionManager.class).to(JsonFileOptionManagerImpl.class).asEagerSingleton();

        Multibinder<LinkProvider> linkProviderMultibinder = Multibinder.newSetBinder(binder(), LinkProvider.class);

        Multibinder<Crawler> crawlerMultibinder = Multibinder.newSetBinder(binder(), Crawler.class);
        crawlerMultibinder.addBinding().to(MockCrawlerImpl.class);

        Multibinder<DataSaver> dataSaverMultibinder = Multibinder.newSetBinder(binder(), DataSaver.class);

        bind(LinkService.class).to(SimpleLinkServiceImpl.class).asEagerSingleton();
        bind(DataService.class).to(SimpleDataServiceImpl.class).asEagerSingleton();
    }

}
