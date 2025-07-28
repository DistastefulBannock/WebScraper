package me.bannock.scraper.plugins;

import com.google.inject.AbstractModule;
import me.bannock.scraper.plugins.api.Plugin;

import java.util.Collection;

public interface PluginManager {

    /**
     * Loads all plugins to this plugin manager. This method WILL
     * load classes into this vm and may or may not run the code
     * contained within said classes. Please ensure any plugins
     * are safe before loading them.
     */
    void loadPlugins();

    /**
     * @return All guice modules from loaded plugins that bind
     *         new mutators
     */
    Collection<AbstractModule> getModules();
    /**
     * @return All instances of every loaded plugin's main class
     */
    Collection<Plugin> getPlugins();

}