package cl.playground.cli.commands;

import cl.playground.config.model.SqliftConfig;
import cl.playground.config.reader.YamlConfigReader;
import cl.playground.core.reader.SqlReader;
import cl.playground.exception.ConfigurationException;
import cl.playground.generator.EntityGenerator;
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

            // Agregar el contenido SQL al contexto
            context.put("sqlContent", sqlContent);

            // Generar las entidades
            System.out.println("\n🔨 Generating entities...");
            EntityGenerator generator = new EntityGenerator();
            generator.generateEntities(context);
            System.out.println("✅ Entities generated successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new ConfigurationException("Failed to read configuration", e);
        }
    }

    private Map<String, Object> extractConfigContext(String configPath) {
        Map<String, Object> context = new HashMap<>();

        // Leer configuración
        SqliftConfig config = YamlConfigReader.readConfig(configPath);

        // Validar configuración requerida
        if (config == null || config.getSql() == null || config.getSql().getOutput() == null ||
                config.getSql().getOutput().getOptions() == null || config.getSql().getOutput().getOptions().getJpa() == null) {
            throw new ConfigurationException("Invalid YAML: Missing required fields.");
        }

        // Almacenar datos relevantes
        context.put("config", config);
        context.put("engine", config.getSql().getEngine());
        context.put("schema", config.getSql().getSchema());
        context.put("outputPackage", config.getSql().getOutput().getPackageName());
        context.put("useLombok", config.getSql().getOutput().getOptions().isLombok());

        System.out.println("✔ YAML configuration loaded successfully.");
        System.out.println(config.toString());


        return context;
    }

}
