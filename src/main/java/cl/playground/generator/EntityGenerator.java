package cl.playground.generator;

import cl.playground.core.engine.DatabaseEngine;
import cl.playground.core.engine.DatabaseEngineFactory;
import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.TableDefinition;
import cl.playground.core.builder.EntityBuilder;
import cl.playground.core.strategy.EntityStrategy;
import cl.playground.core.strategy.StrategyFactory;
import cl.playground.exception.GenerationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class EntityGenerator {

    public void generateEntities(Map<String, Object> context) {
        try {
            DatabaseEngine engine = DatabaseEngineFactory.createEngine((String) context.get("engine"));
            List<EntityStrategy> strategies = StrategyFactory.createStrategies(
                    (boolean) context.get("useLombok"),
                    (boolean) context.get("useJpa")
            );

            String sqlContent = (String) context.get("sqlContent");
            List<TableDefinition> tables = engine.parseTables(sqlContent);

            // Convertir TableDefinition a ClassDefinition
            for (TableDefinition table : tables) {
                ClassDefinition classDefinition = convertToClassDefinition(
                        table,
                        (String) context.get("outputPackage")
                );

                EntityBuilder builder = new EntityBuilder(engine, strategies);
                String classContent = builder
                        .withClassDefinition(classDefinition)
                        .build();

                writeEntityFile(classDefinition.getPackageName(),
                        classDefinition.getClassName(),
                        classContent);

                System.out.println("Generated entity: " + classDefinition.getClassName());
            }

        } catch (Exception e) {
            throw new GenerationException("Failed to generate entities", e);
        }
    }

    private ClassDefinition convertToClassDefinition(TableDefinition table, String basePackage) {
        ClassDefinition classDefinition = new ClassDefinition();

        // Configurar nombre de clase y paquete
        classDefinition.setClassName(toClassName(table.getTableName()));
        classDefinition.setPackageName(basePackage);
        classDefinition.setTableName(table.getTableName());

        // Agregar columnas
        for (ColumnDefinition column : table.getColumns().values()) {
            classDefinition.addColumn(column);

            // Agregar imports necesarios según el tipo de dato
            if (column.getColumnType().equalsIgnoreCase("timestamp")) {
                classDefinition.addImport("java.time.LocalDateTime");
            } else if (column.getColumnType().equalsIgnoreCase("numeric")) {
                classDefinition.addImport("java.math.BigDecimal");
            }

            // Si es foreign key, agregar import de la clase referenciada
            if (column.isForeignKey()) {
                classDefinition.addImport(basePackage + "." + toClassName(column.getColumnName().replace("_id", "")));
            }
        }

        return classDefinition;
    }

    private String toClassName(String tableName) {
        // usuarios -> Usuario
        String singular = tableName.endsWith("s") ?
                tableName.substring(0, tableName.length() - 1) :
                tableName;
        return toPascalCase(singular);
    }

    private String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    private void writeEntityFile(String packageName, String className, String content)
            throws IOException {
        String packagePath = packageName.replace('.', '/');
        Path directory = Paths.get("src/main/java", packagePath);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(className + ".java"), content);
    }
}