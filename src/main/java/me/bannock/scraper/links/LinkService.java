package me.bannock.scraper.links;

public interface LinkService {

    /**
     * Gets the next link to crawl. hasMoreLinks must return true before this method is called
     * @return The next link to crawl
     */
    String getNextLink();

    /**
     * @return Whether if the provider has any more links to crawl
     */
    boolean hasMoreLinks();

}
