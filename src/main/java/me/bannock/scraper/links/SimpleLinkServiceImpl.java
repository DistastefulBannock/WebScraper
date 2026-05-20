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
        // todo: In dedicated mode, only the last link provider will be queried for new links.
        //  Another impl should be added that loops over them all forever
        if (this.linkProvider.hasMoreLinks())
            return true;
        LinkProvider originalLinkProvider = linkProvider;

        while (providerIterator.hasNext()){
            this.linkProvider = providerIterator.next();
            if (linkProvider.hasMoreLinks())
                return true;
        }
        if (linkProvider == originalLinkProvider)
            return false;
        return linkProvider.hasMoreLinks();
    }

}
