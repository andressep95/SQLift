package cl.playground.config.parser;

import cl.playground.config.model.SqliftConfig;
import cl.playground.exception.ConfigurationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class YamlParser {
    private static final String INDENT = "  ";

    public SqliftConfig parse(String filePath) throws IOException {
        SqliftConfig config = new SqliftConfig();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isSkippable(line)) {
                    continue;
                }

                if (line.startsWith("version:")) {
                    config.setVersion(extractValue(line));
                } else if (line.startsWith("sql:")) {
                    SqliftConfig.SqlConfig sqlConfig = new SqliftConfig.SqlConfig();
                    config.setSql(sqlConfig);
                    parseSqlConfig(br, sqlConfig);
                }
            }
        }

        validateConfig(config);
        return config;
    }

    private void parseSqlConfig(BufferedReader br, SqliftConfig.SqlConfig sqlConfig) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("engine:")) {
                sqlConfig.setEngine(extractValue(line));
            } else if (line.startsWith("schema:")) {
                sqlConfig.setSchema(extractValue(line));
            } else if (line.startsWith("output:")) {
                SqliftConfig.OutputConfig outputConfig = new SqliftConfig.OutputConfig();
                sqlConfig.setOutput(outputConfig);
                parseOutputConfig(br, outputConfig);
            }
        }
    }

    private void parseOutputConfig(BufferedReader br, SqliftConfig.OutputConfig outputConfig) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT + INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("package:")) {
                outputConfig.setPackageName(extractValue(line));
            } else if (line.startsWith("options:")) {
                SqliftConfig.Options options = new SqliftConfig.Options();
                outputConfig.setOptions(options);
                parseOptions(br, options);
            }
        }
    }

    private void parseOptions(BufferedReader br, SqliftConfig.Options options) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT + INDENT + INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("lombok:")) {
                options.setLombok(Boolean.parseBoolean(extractValue(line)));
            } else if (line.startsWith("jpa:")) {
                options.setJpa(Boolean.parseBoolean(extractValue(line)));
            }
        }
    }

    private String extractValue(String line) {
        String[] parts = line.split(":", 2);
        return parts.length > 1 ? parts[1].trim().replace("\"", "") : "";
    }

    private boolean isSkippable(String line) {
        return line.isEmpty() || line.startsWith("#");
    }

    private void validateConfig(SqliftConfig config) {
        checkNotNullOrEmpty(config.getVersion(), "Version is required");
        SqliftConfig.SqlConfig sql = config.getSql();
        if (sql == null) {
            throw new ConfigurationException("SQL configuration is required");
        }
        checkNotNullOrEmpty(sql.getEngine(), "Database engine is required");
        checkNotNullOrEmpty(sql.getSchema(), "Schema file path is required");

        SqliftConfig.OutputConfig output = sql.getOutput();
        if (output == null || output.getPackageName() == null || output.getPackageName().trim().isEmpty()) {
            throw new ConfigurationException("Output package configuration is required");
        }
    }

    private void checkNotNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ConfigurationException(errorMessage);
        }
    }
}
