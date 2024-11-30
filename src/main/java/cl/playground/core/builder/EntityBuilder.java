package cl.playground.core.builder;

import cl.playground.core.engine.DatabaseEngine;
import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.strategy.EntityStrategy;

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
        StringBuilder classContent = new StringBuilder();

        // Package declaration
        classContent.append("package ").append(classDefinition.getPackageName()).append(";\n\n");

        // Imports
        Set<String> imports = classDefinition.getImports();
        for (String importStatement : imports) {
            classContent.append("import ").append(importStatement).append(";\n");
        }

        // Strategy imports
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addImports());
        }
        classContent.append("\n");


        // Class annotations
        for (EntityStrategy strategy : strategies) {
            classContent.append(strategy.addClassAnnotations(classDefinition.getTableName()));
        }

        // Class declaration
        classContent.append("public class ").append(classDefinition.getClassName()).append(" {\n\n");

        // Fields in specific order (usando getOrderedColumns())
        List<ColumnDefinition> orderedColumns = classDefinition.getOrderedColumns();
        for (ColumnDefinition column : orderedColumns) {
            // Strategy annotations
            for (EntityStrategy strategy : strategies) {
                classContent.append(strategy.addFieldAnnotations(column));
            }

            String javaType = engine.mapDataType(column.getColumnType());
            String fieldName = toCamelCase(column.getColumnName());
            classContent.append("    private ").append(javaType)
                    .append(" ").append(fieldName).append(";\n\n");
        }

        // Default Constructor
        classContent.append("    public ").append(classDefinition.getClassName()).append("() {\n    }\n\n");

        // Getters and Setters
        for (ColumnDefinition column : orderedColumns) {
            String javaType = engine.mapDataType(column.getColumnType());
            String fieldName = toCamelCase(column.getColumnName());

            // Ajustar nombre si es foreign key
            if (column.isForeignKey()) {
                fieldName = fieldName.replace("Id", "");
            }

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

        // Close class
        classContent.append("}");

        return classContent.toString();
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