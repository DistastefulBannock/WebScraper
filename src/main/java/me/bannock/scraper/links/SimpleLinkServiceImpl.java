package me.bannock.scraper.links;

import com.google.inject.Inject;
import me.bannock.scraper.links.providers.LinkProvider;

import java.util.Iterator;
import java.util.Set;

public class SimpleLinkServiceImpl implements LinkService {

    @Inject
    public SimpleLinkServiceImpl(Set<LinkProvider> providers){
        this.providerIterator = providers.iterator();
        this.linkProvider = providerIterator.next();
    }

    private final Iterator<LinkProvider> providerIterator;
    private LinkProvider linkProvider;

    @Override
    public String getNextLink() {
        return linkProvider.getNextLink();
    }

    @Override
    public boolean hasMoreLinks() {
        if (this.linkProvider.hasMoreLinks())
            return true;

        while (!linkProvider.hasMoreLinks() && providerIterator.hasNext()){
            linkProvider = providerIterator.next();
        }
        return linkProvider.hasMoreLinks();
    }

}
