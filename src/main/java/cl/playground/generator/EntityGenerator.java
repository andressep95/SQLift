package cl.playground.generator;

import cl.playground.config.model.SqliftConfig;
import cl.playground.core.engine.*;
import cl.playground.core.model.*;
import cl.playground.core.builder.EntityBuilder;
import cl.playground.core.strategy.*;
import cl.playground.exception.GenerationException;
import java.nio.file.*;
import java.util.*;

public class EntityGenerator {

    public void generateEntities(Map<String, Object> context) {
        try {
            // Crear engine y estrategias
            DatabaseEngine engine = DatabaseEngineFactory.createEngine((String) context.get("engine"));
            List<EntityStrategy> strategies = StrategyFactory.createStrategies(
                    (boolean) context.get("useLombok"),
                    ((SqliftConfig) context.get("config")).getSql().getOutput().getOptions().getJpa()
            );

            // Procesar SQL y obtener definiciones de clase
            String sqlContent = (String) context.get("sqlContent");
            Map<String, ClassDefinition> classDefinitions =
                    engine.mapSqlToClassDefinitions(sqlContent, (String) context.get("outputPackage"));

            // Generar cada clase
            for (ClassDefinition classDefinition : classDefinitions.values()) {
                EntityBuilder builder = new EntityBuilder(engine, strategies);
                String classContent = builder
                        .withClassDefinition(classDefinition)
                        .build();

                writeEntityFile(
                        classDefinition.getPackageName(),
                        classDefinition.getClassName(),
                        classContent
                );

                System.out.println("Generated entity: " + classDefinition.getClassName());
            }

        } catch (Exception e) {
            throw new GenerationException("Failed to generate entities", e);
        }
    }

    private void writeEntityFile(String packageName, String className, String content)
            throws java.io.IOException {
        String packagePath = packageName.replace('.', '/');
        Path directory = Paths.get("src/main/java", packagePath);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(className + ".java"), content);
    }
}