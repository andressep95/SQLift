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
                (boolean) context.get("useJpa")
                                                                              );

            System.out.println("Estrategias utilizadas: " + strategies);

            // Procesar SQL y obtener definiciones de clase
            String sqlContent = (String) context.get("sqlContent");
            Map<String, ClassDefinition> classDefinitions =
                engine.mapSqlToClassDefinitions(sqlContent, (String) context.get("outputPackage"));

            System.out.println("Clases definidas a partir del SQL: " + classDefinitions.keySet());

            // Generar cada clase - AQUÍ ESTÁ EL CAMBIO
            for (ClassDefinition classDefinition : classDefinitions.values()) {
                EntityBuilder builder = new EntityBuilder(strategies, classDefinitions); // Modificado
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
            System.err.println("Error al generar entidades: " + e.getMessage());
            throw new GenerationException("Failed to generate entities", e);
        }
    }

    private void writeEntityFile(String packageName, String className, String content)
        throws java.io.IOException {
        String packagePath = packageName.replace('.', '/');
        Path directory = Paths.get("src/main/java", packagePath);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(className + ".java"), content);
        System.out.println("Archivo generado: " + directory.resolve(className + ".java"));
    }
}
