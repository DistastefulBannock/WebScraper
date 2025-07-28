package me.bannock.scraper.data.savers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerDataSaverImpl implements DataSaver {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void saveLine(String line) {
        logger.info(line);
    }

}
