package me.bannock.scraper.crawlers;

public interface Crawler {

    /**
     * Scrapes the data at a link and saves its data to the data service
     * @param link
     */
    void crawlLink(String link);

}
