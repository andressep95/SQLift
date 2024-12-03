package cl.playground.core.builder;

import cl.playground.core.engine.DatabaseEngine;
import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.strategy.EntityStrategy;
import cl.playground.core.model.ForeignKeyDefinition;

import java.util.List;
import java.util.Set;

public class EntityBuilder {
    private final DatabaseEngine engine;
    private final List<EntityStrategy> strategies;
    private ClassDefinition classDefinition;

    public EntityBuilder(DatabaseEngine engine, List<EntityStrategy> strategies) {
        this.engine = engine;
        this.strategies = strategies;
    }

    public EntityBuilder withClassDefinition(ClassDefinition classDefinition) {
        this.classDefinition = classDefinition;
        return this;
    }

    public String build() {
        if (classDefinition.getPrimaryKey() == null) {
            throw new IllegalStateException("No primary key defined for class " + classDefinition.getClassName());
        }


        StringBuilder classContent = new StringBuilder();

        // Package declaration
        classContent.append("package ").append(classDefinition.getPackageName()).append(";\n\n");

        // Imports
        addImports(classContent);

        // Class annotations
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addClassAnnotations(classDefinition.getClassName()));
        }

        // Class declaration
        classContent.append("public class ").append(classDefinition.getClassName()).append(" {\n\n");

        // 1. Primary Key
        if (classDefinition.getPrimaryKey() != null) {
            addField(classContent, classDefinition.getPrimaryKey(), false, true);
        }

        // 2. Foreign Keys
        List<ForeignKeyDefinition> foreignKeys = classDefinition.getForeignKeys();
        List<ColumnDefinition> attributes = classDefinition.getAttributes();

        for (ForeignKeyDefinition fk : foreignKeys) {
            for (ColumnDefinition column : attributes) {
                if (column.getColumnName().equals(fk.getColumnName())) {
                    addField(classContent, column, true, false);
                    break;
                }
            }
        }

        // 3. Regular fields
        for (ColumnDefinition column : attributes) {
            if (isRegularField(column, foreignKeys)) {
                addField(classContent, column, false, false);
            }
        }

        // Default Constructor
        addDefaultConstructor(classContent);

        // All args Constructor
        addAllArgsConstructor(classContent);

        // Getters and Setters
        addGettersAndSetters(classContent);

        // Close class
        classContent.append("}");

        return classContent.toString();
    }

    private boolean isRegularField(ColumnDefinition column, List<ForeignKeyDefinition> foreignKeys) {
        return foreignKeys.stream()
                .noneMatch(fk -> fk.getColumnName().equals(column.getColumnName())) &&
                !column.getColumnName().equals(classDefinition.getPrimaryKey().getColumnName());
    }


    private void addImports(StringBuilder classContent) {
        // Base imports
        Set<String> imports = classDefinition.getImports();
        for (String importStatement : imports) {
            classContent.append("import ").append(importStatement).append(";\n");
        }

        // Strategy imports
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addImports());
        }
        classContent.append("\n");
    }

    private void addField(StringBuilder classContent, ColumnDefinition column, boolean isForeignKey, boolean isPrimaryKey) {
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addFieldAnnotations(column, isForeignKey, isPrimaryKey));
        }

        String javaType = engine.mapDataType(
                column.getColumnType(),
                column.getColumnName(),
                isForeignKey
        );

        String fieldName = toCamelCase(column.getColumnName().replace("_id", ""));
        classContent.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n");
    }


    private void addDefaultConstructor(StringBuilder classContent) {
        classContent.append("    public ").append(classDefinition.getClassName())
                .append("() {\n    }\n\n");
    }

    private void addAllArgsConstructor(StringBuilder classContent) {
        classContent.append("    public ").append(classDefinition.getClassName()).append("(");

        // Parameters
        boolean first = true;

        // Primary Key parameter
        if (classDefinition.getPrimaryKey() != null) {
            String type = engine.mapDataType(
                    classDefinition.getPrimaryKey().getColumnType(),
                    classDefinition.getPrimaryKey().getColumnName(),
                    false
            );
            String name = toCamelCase(classDefinition.getPrimaryKey().getColumnName());
            classContent.append(type).append(" ").append(name);
            first = false;
        }

        // Other parameters
        List<ForeignKeyDefinition> foreignKeys = classDefinition.getForeignKeys();

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            if (!first) classContent.append(", ");

            boolean isForeignKey = foreignKeys.stream()
                    .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));

            String type = engine.mapDataType(column.getColumnType(), column.getColumnName(), isForeignKey);
            String name = isForeignKey ?
                    toCamelCase(column.getColumnName().replace("_id", "")) :
                    toCamelCase(column.getColumnName());

            classContent.append(type).append(" ").append(name);
            first = false;
        }

        classContent.append(") {\n");

        // Constructor body
        if (classDefinition.getPrimaryKey() != null) {
            String name = toCamelCase(classDefinition.getPrimaryKey().getColumnName());
            classContent.append("        this.").append(name).append(" = ").append(name).append(";\n");
        }

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isForeignKey = foreignKeys.stream()
                    .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));

            String name = isForeignKey ?
                    toCamelCase(column.getColumnName().replace("_id", "")) :
                    toCamelCase(column.getColumnName());

            classContent.append("        this.").append(name).append(" = ").append(name).append(";\n");
        }

        classContent.append("    }\n\n");
    }

    private void addGettersAndSetters(StringBuilder classContent) {
        // Primary key
        if (classDefinition.getPrimaryKey() != null) {
            addAccessors(classContent, classDefinition.getPrimaryKey(), false);
        }

        // Other fields
        List<ForeignKeyDefinition> foreignKeys = classDefinition.getForeignKeys();
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isForeignKey = foreignKeys.stream()
                    .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            addAccessors(classContent, column, isForeignKey);
        }
    }

    private void addAccessors(StringBuilder classContent, ColumnDefinition column, boolean isForeignKey) {
        String javaType = engine.mapDataType(column.getColumnType(), column.getColumnName(), isForeignKey);
        String fieldName = isForeignKey ?
                toCamelCase(column.getColumnName().replace("_id", "")) :
                toCamelCase(column.getColumnName());
        String methodName = capitalize(fieldName);

        // Getter
        classContent.append("    public ").append(javaType).append(" get")
                .append(methodName).append("() {\n");
        classContent.append("        return ").append(fieldName).append(";\n");
        classContent.append("    }\n\n");

        // Setter
        classContent.append("    public void set").append(methodName)
                .append("(").append(javaType).append(" ").append(fieldName).append(") {\n");
        classContent.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        classContent.append("    }\n\n");
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (char c : input.toLowerCase().toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }


}