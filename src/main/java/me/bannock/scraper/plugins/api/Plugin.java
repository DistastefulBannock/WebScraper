package me.bannock.scraper.plugins.api;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import java.util.List;

public abstract class Plugin {

    private String name, version;
    private boolean finalized = false;

    /**
     * Initializes the plugin
     * @return True if the plugin should be loaded, otherwise false
     */
    public boolean load(){return true;}

    /**
     * Called when the scraper is trying to exit.
     * @param injector The injector instance for this scraper.
     */
    public void onExit(Injector injector){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (finalized)
            throw new IllegalStateException("Plugin is finalized; cannot change name");
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (finalized)
            throw new IllegalStateException("Plugin is finalized; cannot change version");
        this.version = version;
    }

    /**
     * @return All the guice modules that the scraper should load for the plugin
     */
    public abstract List<AbstractModule> getPluginModules();

    /**
     * A finalized instance will no longer have working setName or setVersion methods
     */
    public void finalizePlugin(){
        this.finalized = true;
    }

}