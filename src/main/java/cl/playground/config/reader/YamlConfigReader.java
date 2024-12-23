package cl.playground.config.reader;

import cl.playground.config.model.SqliftConfig;
import cl.playground.config.parser.YamlParser;
import cl.playground.exception.ConfigurationException;

import java.io.File;
import java.io.IOException;

public class YamlConfigReader {

    public static SqliftConfig readConfig(String configFilePath) throws ConfigurationException {
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            throw new ConfigurationException("Configuration file not found: " + configFilePath);
        }

        if (!configFile.isFile()) {
            throw new ConfigurationException("Path is not a file: " + configFilePath);
        }

        try {
            YamlParser parser = new YamlParser();
            return parser.parse(configFilePath);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file", e);
        }
    }
}
