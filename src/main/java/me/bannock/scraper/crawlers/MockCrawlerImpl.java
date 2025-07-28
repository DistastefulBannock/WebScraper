package me.bannock.scraper.crawlers;

import com.google.inject.Inject;
import me.bannock.scraper.data.DataService;

public class MockCrawlerImpl implements Crawler {

    @Inject
    public MockCrawlerImpl(DataService dataService){
        this.dataService = dataService;
    }

    private final DataService dataService;

    @Override
    public void crawlLink(String link) {
        dataService.saveLine(link);
    }

}
