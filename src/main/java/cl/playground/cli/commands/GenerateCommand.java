package cl.playground.cli.commands;

import cl.playground.config.model.SqliftConfig;
import cl.playground.config.reader.YamlConfigReader;
import cl.playground.core.reader.SqlReader;
import cl.playground.exception.ConfigurationException;
import cl.playground.util.LogContent;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Command(
        name = "generate",
        description = "Generate Java entity classes from SQL schema"
)
public class GenerateCommand implements Runnable {

    private static final String CONFIG_FILENAME = "sqlift.yaml";

    @Override
    public void run() {
        try {
            // Obtener directorio actual y archivo de configuración
            String currentDir = System.getProperty("user.dir");
            File yamlFile = new File(currentDir, CONFIG_FILENAME);

            if (!yamlFile.exists()) {
                System.out.println("Configuration file not found in directory: " + currentDir);
                return;
            }

            // Leer configuración y almacenar en el contexto
            Map<String, Object> context = extractConfigContext(yamlFile.getPath());

            // Mostrar la configuración leída
            LogContent.logConfiguration(context);

            // Leer y mostrar el contenido SQL
            String sqlContent = SqlReader.readSql((String) context.get("schema"));
            LogContent.logSqlContent(sqlContent);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new ConfigurationException("Failed to read configuration", e);
        }
    }

    private Map<String, Object> extractConfigContext(String configPath) {
        Map<String, Object> context = new HashMap<>();

        // Leer configuración
        SqliftConfig config = YamlConfigReader.readConfig(configPath);

        // Almacenar datos relevantes
        context.put("engine", config.getSql().getEngine());
        context.put("schema", config.getSql().getSchema());
        context.put("outputPackage", config.getSql().getOutput().getPackageName());
        context.put("useLombok", config.getSql().getOutput().getOptions().isLombok());
        context.put("useJpa", config.getSql().getOutput().getOptions().isJpa());

        return context;
    }
}
