package me.bannock.scraper.plugins.api;

import com.google.gson.annotations.SerializedName;

public class PlugJson {

    @SerializedName("name")
    private String name;

    @SerializedName("version")
    private String version;

    @SerializedName("pluginMain")
    private String pluginMain;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPluginMain() {
        return pluginMain;
    }

}