package cl.playground.core.builder;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;
import cl.playground.core.strategy.EntityStrategy;

import java.util.List;
import java.util.stream.Collectors;

public class EntityBuilder {

    private final List<EntityStrategy> strategies;
    private ClassDefinition classDefinition;

    public EntityBuilder(List<EntityStrategy> strategies) {
        this.strategies = strategies;
    }

    public EntityBuilder withClassDefinition(ClassDefinition classDefinition) {
        this.classDefinition = classDefinition;
        return this;
    }

    public String build() {
        if (classDefinition == null) {
            throw new IllegalStateException("ClassDefinition must be set before building.");
        }

        StringBuilder entity = new StringBuilder();

        // Add package
        entity.append("package ").append(classDefinition.getPackageName()).append(";\n\n");

        // Add imports
        for (EntityStrategy strategy : strategies) {
            entity.append(strategy.addImports()).append("\n");
        }

        // Add class annotations dynamically
        for (EntityStrategy strategy : strategies) {
            entity.append(strategy.addClassAnnotations(classDefinition.getClassName(), classDefinition));
        }

        // Add class declaration
        entity.append("public class ").append(classDefinition.getClassName()).append(" {\n\n");

        // Handle primary key
        if (!classDefinition.getPrimaryKeyColumns().isEmpty()) {
            if (classDefinition.getPrimaryKeyColumns().size() > 1) {
                entity.append(generateCompositePrimaryKeyClass());
            } else {
                ColumnDefinition primaryKey = classDefinition.getPrimaryKeyColumns().get(0);
                addField(entity, primaryKey, false, true);
            }
        }

        // Add fields
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            for (EntityStrategy strategy : strategies) {
                ForeignKeyDefinition foreignKey = classDefinition.getForeignKeys().stream()
                    .filter(fk -> fk.getColumnName().equals(column.getColumnName()))
                    .findFirst().orElse(null);

                boolean isPrimaryKey = classDefinition.getPrimaryKeyColumns().contains(column);
                entity.append(strategy.addFieldAnnotations(column, foreignKey, isPrimaryKey));
            }

            entity.append("    private ")
                .append(mapDataType(column.getColumnType())).append(" ")
                .append(toCamelCase(column.getColumnName())).append(";\n");
        }

        entity.append("\n");
        // Add constructor and methods if no Lombok
        boolean usesLombok = strategies.stream().anyMatch(s -> s.getClass().getSimpleName().equals("LombokStrategy"));

        if (!usesLombok) {
            addDefaultConstructor(entity);
            addAllArgsConstructor(entity);
            addGettersAndSetters(entity);
        }

        // Close class
        entity.append("}");

        return entity.toString();
    }

    private void addField(StringBuilder classContent, ColumnDefinition column, boolean isForeignKey, boolean isPrimaryKey) {
        // Obtener la definición de clave foránea si aplica
        ForeignKeyDefinition foreignKey = null;
        if (isForeignKey) {
            foreignKey = classDefinition.getForeignKeys().stream()
                .filter(fk -> fk.getColumnName().equals(column.getColumnName()))
                .findFirst().orElse(null);
        }

        // Aplicar las anotaciones de cada estrategia
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addFieldAnnotations(column, foreignKey, isPrimaryKey));
        }

        // Generar la definición del campo
        String javaType = mapDataType(column.getColumnType());
        String fieldName = toCamelCase(column.getColumnName());
        classContent.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n");
    }

    private String mapDataType(String sqlType) {
        return switch (sqlType.toUpperCase()) {
            case "SERIAL" -> "Long";
            case "VARCHAR" -> "String";
            case "INTEGER" -> "Integer";
            case "NUMERIC" -> "java.math.BigDecimal";
            case "DATE" -> "java.time.LocalDate";
            default -> "Object"; // Fallback for unmapped types
        };
    }

    private String generateCompositePrimaryKeyClass() {
        StringBuilder compositeKeyClass = new StringBuilder();
        compositeKeyClass.append("@Embeddable\n")
            .append("public static class ")
            .append(classDefinition.getClassName()).append("Id {\n");

        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            compositeKeyClass.append("    private ")
                .append(mapDataType(pkColumn.getColumnType())).append(" ")
                .append(toCamelCase(pkColumn.getColumnName())).append(";\n");
        }

        compositeKeyClass.append("\n    // Getters and Setters\n");
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String fieldName = toCamelCase(pkColumn.getColumnName());
            String capitalizedField = capitalize(fieldName);

            compositeKeyClass.append("    public ")
                .append(mapDataType(pkColumn.getColumnType())).append(" get").append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n    }\n\n");

            compositeKeyClass.append("    public void set").append(capitalizedField).append("(")
                .append(mapDataType(pkColumn.getColumnType())).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
        }

        compositeKeyClass.append("}\n\n");

        return compositeKeyClass.toString();
    }

    private void addDefaultConstructor(StringBuilder classContent) {
        classContent.append("    public ").append(classDefinition.getClassName()).append("() {\n    }\n\n");
    }

    private void addAllArgsConstructor(StringBuilder classContent) {
        classContent.append("    public ").append(classDefinition.getClassName()).append("(");

        // Parameters
        boolean first = true;

        // Add primary key columns first
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            if (!first) classContent.append(", ");
            first = false;

            String type = mapDataType(pkColumn.getColumnType());
            String name = toCamelCase(pkColumn.getColumnName());
            classContent.append(type).append(" ").append(name);
        }

        // Add other attributes
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            if (!first) classContent.append(", ");
            first = false;

            String type = mapDataType(column.getColumnType());
            String name = toCamelCase(column.getColumnName());
            classContent.append(type).append(" ").append(name);
        }
        classContent.append(") {\n");

        // Constructor body for primary key columns
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String name = toCamelCase(pkColumn.getColumnName());
            classContent.append("        this.").append(name).append(" = ").append(name).append(";\n");
        }

        // Constructor body for other attributes
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            String name = toCamelCase(column.getColumnName());
            classContent.append("        this.").append(name).append(" = ").append(name).append(";\n");
        }
        classContent.append("    }\n\n");
    }

    private void addGettersAndSetters(StringBuilder classContent) {
        for (ColumnDefinition column : classDefinition.getPrimaryKeyColumns()) {
            String type = mapDataType(column.getColumnType());
            String fieldName = toCamelCase(column.getColumnName());
            String capitalizedField = capitalize(fieldName);

            // Getter
            classContent.append("    public ").append(type).append(" get").append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n    }\n\n");

            // Setter
            classContent.append("    public void set").append(capitalizedField).append("(").append(type)
                .append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
        }

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            String type = mapDataType(column.getColumnType());
            String fieldName = toCamelCase(column.getColumnName());
            String capitalizedField = capitalize(fieldName);

            // Getter
            classContent.append("    public ").append(type).append(" get").append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n    }\n\n");

            // Setter
            classContent.append("    public void set").append(capitalizedField).append("(").append(type)
                .append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
