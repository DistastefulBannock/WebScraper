package me.bannock.scraper.plugins.impl;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import me.bannock.scraper.plugins.PluginManager;
import me.bannock.scraper.plugins.api.PlugJson;
import me.bannock.scraper.plugins.api.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarPluginManager implements PluginManager {
    private final Logger logger = LogManager.getLogger();
    private final String plugJsonPath = "plug.json";

    private final List<Plugin> plugins;
    private final List<AbstractModule> pluginModules;
    private final File pluginDir;

    @Inject
    public JarPluginManager(){
        this.plugins = new ArrayList<>();
        this.pluginModules = new ArrayList<>();
        this.pluginDir = new File("plugins");

        if (!pluginDir.exists()){
            logger.info("Plugins directory does not exist, creating it...");
            pluginDir.mkdirs();
        }
    }

    @Override
    public void loadPlugins() {
        logger.info("Now loading plugins");
        for (File file : Objects.requireNonNull(pluginDir.listFiles())){
            String fileName = file.getName();
            logger.info(String.format("Found file \"%s\" in plugins directory...", fileName));
            if (!fileName.toLowerCase().endsWith(".jar") &&
                    !fileName.toLowerCase().endsWith(".zip")){
                logger.info(String.format("\"%s\" is not a jar/zip, skipping...", fileName));
                continue;
            }
            logger.info(String.format("Loading plugin \"%s\"...", fileName));
            try(ZipFile zip = new ZipFile(file)){
                URLClassLoader externalClassLoader =
                        new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());

                PlugJson plugJson = fetchPlugJson(zip);
                if (plugJson == null){
                    logger.warn("No plug.json found. This file is NOT a plugin");
                    continue;
                }
                // We need to make sure that these expected fields are populated before continuing.
                // It will likely still work without a few of them, but it may cause bugs in other
                // classes
                Objects.requireNonNull(plugJson.getName());
                Objects.requireNonNull(plugJson.getVersion());
                Objects.requireNonNull(plugJson.getName());

                Plugin plugin = createPluginInstance(externalClassLoader, plugJson);
                if (plugin == null){
                    logger.warn("Was unable to load plugin");
                    continue;
                }
                plugin.setName(plugJson.getName());
                plugin.setVersion(plugJson.getVersion());
                plugin.finalizePlugin();

                // Now that we have an instance of the plugin's main class, we can load it
                if (!plugin.load()) {
                    externalClassLoader.close();
                    throw new RuntimeException(
                            String.format("Plugin \"%s v%s\"decided it didn't want to load itself",
                                    plugin.getName(), plugin.getVersion())
                    );
                }

                this.plugins.add(plugin);
                this.pluginModules.addAll(plugin.getPluginModules());
                logger.info(String.format("Loaded plugin \"%s %s\"",
                        plugJson.getName(), plugJson.getVersion()));
            } catch (MalformedURLException e){
                logger.warn("Failed to create URL out of file. Can't load plugin", e);
            } catch (IOException e) {
                logger.warn("Failed to load plugin", e);
            } catch (RuntimeException e){
                logger.warn("Something went wrong while loading plugin", e);
            }
        }

    }

    @Override
    public Collection<AbstractModule> getModules() {
        return this.pluginModules;
    }

    /**
     * Creates an instance of a plugin's main class
     * @param pluginClassLoader The class loader to use
     *                          when loading the class
     * @param plugJson The plug.json object for the
     *                 specified plugin
     * @return An instance of the plugin's main class,
     *         or null if something went wrong while
     *         loading it
     */
    private Plugin createPluginInstance(URLClassLoader pluginClassLoader,
                                                  PlugJson plugJson){
        Objects.requireNonNull(pluginClassLoader);
        Objects.requireNonNull(plugJson);

        // We need to load the plugin's main class before creating an instance of it
        Class<?> pluginMain;
        try{
            pluginMain = Class.forName(plugJson.getPluginMain(), false, pluginClassLoader);
            if (!Plugin.class.isAssignableFrom(pluginMain)){
                logger.warn("Main plugin class does not extend DonutGuardPlugin. " +
                        "Unable to load plugin");
                return null;
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Was unable to load the plugin's main class (typo in plug.json?)", e);
            return null;
        }

        try {
            return (Plugin) pluginMain.getConstructor().newInstance();
        } catch (InstantiationException e) {
            logger.warn("Why is pluginMain abstract? Unable to load.", e);
        } catch (IllegalAccessException e) {
            logger.warn("Unable to access the constructor for a plugin's main class. " +
                    "Please make sure it is public", e);
        } catch (InvocationTargetException e) {
            logger.warn("A plugin's main class's constructor threw an exception", e);
        } catch (NoSuchMethodException e) {
            logger.warn("Unable to find zero argument constructor", e);
        } catch (ClassCastException e){
            logger.warn("Something went wrong while creating plugin main", e);
        }
        return null;
    }

    /**
     * Creates a PlugJson object from a plugin's plug.json file
     * @param zipFile The zip file to fetch plug.json from
     * @return A PlugJson object representing the plug.json file,
     *         or null if the file doesn't exist
     * @throws IOException If something goes wrong while reading
     */
    private PlugJson fetchPlugJson(ZipFile zipFile) throws IOException {
        Objects.requireNonNull(zipFile);

        ZipEntry entry = zipFile.getEntry(plugJsonPath);
        if (entry == null){
            logger.warn(String.format("%s not found in zip", plugJsonPath));
            return null;
        }
        logger.info(String.format("Reading %s from entry...", plugJsonPath));
        try(InputStream plugJsonIn = zipFile.getInputStream(entry);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream()){
            String json = extractJson(plugJsonIn, bytes);
            logger.info(String.format("Successfully read %s from entry", plugJsonPath));
            return new Gson().fromJson(json, PlugJson.class);
        }catch (Exception e){
            logger.error(String.format("Unable to read %s", plugJsonPath), e);
            throw e;
        }
    }

    private String extractJson(InputStream languageIn, ByteArrayOutputStream bytes) throws IOException {
        byte[] buffer = new byte[2048];
        int readBytes = 0;
        while ((readBytes = languageIn.read(buffer, 0, buffer.length)) != -1){
            bytes.write(buffer, 0, readBytes);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Override
    public Collection<Plugin> getPlugins() {
        return Collections.unmodifiableList(this.plugins);
    }

}