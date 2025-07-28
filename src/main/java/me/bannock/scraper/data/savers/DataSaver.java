package me.bannock.scraper.data.savers;

/**
 * A service that saves data
 */
public interface DataSaver {

    /**
     * @param line The line to save
     */
    void saveLine(String line);

}
