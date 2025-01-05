package cl.playground.core.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class PostgresEngine {

    public List<String> extractCreateTableStatements(String sql) {
        List<String> statements = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+.*?;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            statements.add(matcher.group());
        }

        return statements;
    }

    public List<String> extractColumnDefinitions(String sql) {
        List<String> columnDefinitions = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+\\w+\\s*\\((.*?)\\);",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String columnsDefinition = matcher.group(1);
            String[] lines = columnsDefinition.split(",(?![^(]*\\))");

            for (String line : lines) {
                line = line.trim();
                // Ignorar constraints
                if (!line.isEmpty() &&
                        !line.toUpperCase().startsWith("PRIMARY") &&
                        !line.toUpperCase().startsWith("FOREIGN") &&
                        !line.toUpperCase().startsWith("CONSTRAINT")) {
                    columnDefinitions.add(line);
                }
            }
        }

        return columnDefinitions;
    }

    public String extractTableName(String sql) {
        // Patrón que busca después de CREATE TABLE, ignorando espacios y
        // mayúsculas/minúsculas
        Pattern pattern = Pattern.compile("(?i)\\bCREATE\\s+TABLE\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            // Retorna el nombre de la tabla encontrado (grupo 1 del matcher)
            return matcher.group(1);
        }

        return null; // Retorna null si no encuentra ninguna tabla
    }

    public String extractColumnName(String columnDefinition) {
        // Obtener la primera palabra antes de cualquier tipo de dato o constraint
        String[] parts = columnDefinition.trim().split("\\s+");

        if (parts.length > 0) {
            return parts[0];
        }

        return null;
    }

    public String extractColumnType(String sql) {
        // Eliminar comentarios SQL si existen
        sql = sql.replaceAll("--.*$", "");

        // Patrón para capturar el tipo de dato, incluyendo cualquier precisión/escala
        Pattern pattern = Pattern.compile("\\s+([A-Za-z]+(?:\\s*\\([^)]*\\))?)\\s*(?:ARRAY|\\[\\])?");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String dataType = matcher.group(1).trim();

            // Manejar tipos de arrays
            if (sql.contains("ARRAY") || sql.contains("[]")) {
                dataType += "[]";
            }

            return dataType.toUpperCase();
        }

        return null;
    }

    public List<String> extractTableRelations(String sql) {
        List<String> relations = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s*REFERENCES\\s+([^\\s]+)\\s*\\(([^)]+)\\)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            String sourceColumn = matcher.group(1).trim();
            String targetTable = matcher.group(2).trim();
            String targetColumn = matcher.group(3).trim();
            relations.add(String.format("%s -> %s.%s", sourceColumn, targetTable, targetColumn));
        }

        return relations;
    }

    public boolean isNotNullColumn(String columnDefinition) {
        // Verifica NOT NULL explícito
        Pattern notNullPattern = Pattern.compile(".*\\bNOT\\s+NULL\\b.*",
                Pattern.CASE_INSENSITIVE);
        return notNullPattern.matcher(columnDefinition).matches();
    }

    public boolean isUniqueColumn(String columnDefinition) {
        Pattern pattern = Pattern.compile(".*\\bUNIQUE\\b.*",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(columnDefinition);
        return matcher.matches();
    }

    public String extractDefaultValue(String columnDefinition) {
        Pattern pattern = Pattern.compile(
                "DEFAULT\\s+(" +
                        "true|false|" + // Booleanos
                        "CURRENT_DATE|" + // Funciones de fecha
                        "CURRENT_TIMESTAMP|" +
                        "CURRENT_TIME|" +
                        "NOW\\(\\)" +
                        ")",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(columnDefinition);

        if (matcher.find()) {
            return matcher.group(1); // Retornamos el valor exactamente como lo encontramos
        }
        return null;
    }

    public List<String> extractPrimaryKeyColumns(String sql) {
        List<String> primaryKeyColumns = new ArrayList<>();

        // Patrón para clave primaria simple (columna PRIMARY KEY)
        Pattern simplePattern = Pattern.compile(
                "\\s*(\\w+)\\s+\\w+(?:\\([^)]*\\))?\\s+.*?PRIMARY\\s+KEY",
                Pattern.CASE_INSENSITIVE);
        Matcher simpleMatcher = simplePattern.matcher(sql);

        // Patrón para clave primaria compuesta (constraint PRIMARY KEY)
        Pattern compositePattern = Pattern.compile(
                "CONSTRAINT\\s+\\w+\\s+PRIMARY\\s+KEY\\s*\\(([^)]+)\\)|PRIMARY\\s+KEY\\s*\\(([^)]+)\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher compositeMatcher = compositePattern.matcher(sql);

        // Buscamos todas las claves primarias simples
        while (simpleMatcher.find()) {
            primaryKeyColumns.add(simpleMatcher.group(1).trim());
        }

        // Buscamos todas las claves primarias compuestas
        while (compositeMatcher.find()) {
            String columns = compositeMatcher.group(1) != null ? compositeMatcher.group(1) : compositeMatcher.group(2);
            for (String column : columns.split(",")) {
                primaryKeyColumns.add(column.trim());
            }
        }

        return primaryKeyColumns;
    }
}