package me.bannock.scraper.data;

import com.google.inject.Inject;
import me.bannock.scraper.data.savers.DataSaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class SimpleDataServiceImpl implements DataService {

    @Inject
    public SimpleDataServiceImpl(Set<DataSaver> dataSavers){
        this.dataSavers = dataSavers;
        for (DataSaver saver : dataSavers){
            logger.info("Found {} data saver", saver.getClass().getSimpleName());
        }
    }

    private final Logger logger = LogManager.getLogger();
    private final Set<DataSaver> dataSavers;

    @Override
    public void saveLine(String line) {
        logger.info("Saving line, line=\"{}\"", line);
        dataSavers.forEach(saver -> saver.saveLine(line));
    }

}
