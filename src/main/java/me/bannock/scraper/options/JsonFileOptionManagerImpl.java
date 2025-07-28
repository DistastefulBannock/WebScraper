package me.bannock.scraper.options;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JsonFileOptionManagerImpl implements OptionManager {

    public JsonFileOptionManagerImpl(){
        if (jsonFile.exists()){
            try{
                String jsonStr = Files.readString(jsonFile.toPath(), fileCharset);
                TypeToken<Map<String, String>> variableFieldToken = new TypeToken<>(){};
                variables.putAll(new Gson().fromJson(jsonStr, variableFieldToken));
            } catch (IOException e) {
                logger.error("Failed to read options file. Will proceed with blank options, optionsFile={}", jsonFile, e);
            } catch (JsonSyntaxException e){
                logger.error("Failed to parse options file due to malformed content. " +
                        "Will proceed with blank options, optionsFile={}", jsonFile, e);
            }
        }
    }

    private final Logger logger = LogManager.getLogger();
    private final File jsonFile = new File("config.json");
    private final Charset fileCharset = StandardCharsets.UTF_8;
    private final Map<String, String> variables = new HashMap<>();

    @Override
    public void setVariable(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        this.variables.put(key, value);
    }

    @Override
    public Optional<String> getVariable(String key) {
        Objects.requireNonNull(key);

        return Optional.ofNullable(this.variables.get(key));
    }

    @Override
    public void saveVariables() {
        try {
            Files.writeString(jsonFile.toPath(), new Gson().toJson(variables), fileCharset);
        } catch (IOException e) {
            logger.error("Failed to save options file, optionsFile={}", jsonFile, e);
        }
    }

}
