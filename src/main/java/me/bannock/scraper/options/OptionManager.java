package me.bannock.scraper.options;

import java.util.Optional;

/**
 * This manager persists common cheat configuration data.
 * Configuration data from other subsystems should generally not be stored here.
 */
public interface OptionManager {

    /**
     * @param key The unique key for this configuration variable
     * @param value The value you want the variable to be
     */
    void setVariable(String key, String value);

    /**
     * @param key The unique key for the configuration variable
     * @return The value of the variable, if it could be found
     */
    Optional<String> getVariable(String key);

    /**
     * Saves all the currently stored variables
     */
    void saveVariables();

}
