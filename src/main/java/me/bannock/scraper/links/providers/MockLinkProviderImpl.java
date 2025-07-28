package me.bannock.scraper.links.providers;

public class MockLinkProviderImpl implements LinkProvider {

    private int mockLinks = 10;

    @Override
    public String getNextLink() {
        return "https://google.com";
    }

    @Override
    public boolean hasMoreLinks() {
        return (mockLinks--) > 0;
    }

}
