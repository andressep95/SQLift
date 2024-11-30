package cl.playground.core.engine;

import cl.playground.core.model.TableDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.util.TypeMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgresEngine implements DatabaseEngine {
    // Patrón para capturar cada CREATE TABLE completo
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "CREATE TABLE\\s+(?:IF NOT EXISTS\\s+)?([\\w\\.]+)\\s*\\((.*?)\\);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Patrón para cada definición de columna
    private static final Pattern COLUMN_PATTERN = Pattern.compile(
            "\\s*([\\w]+)\\s+([\\w\\s]+)(?:\\(\\s*([\\d,]+)\\s*\\))?\\s*([^,]*)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public List<TableDefinition> parseTables(String sqlContent) {
        List<TableDefinition> tables = new ArrayList<>();
        Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(sqlContent);

        while (tableMatcher.find()) {
            // Extraer nombre de la tabla y su contenido
            String tableName = tableMatcher.group(1);
            String tableContent = tableMatcher.group(2);

            // Crear definición de tabla
            TableDefinition table = new TableDefinition();
            table.setTableName(tableName);
            table.setColumns(parseColumns(tableContent));

            tables.add(table);
            System.out.println("Found table: " + tableName); // Log para debug
        }

        return tables;
    }

    @Override
    public Map<String, ColumnDefinition> parseColumns(String tableContent) {
        Map<String, ColumnDefinition> columns = new HashMap<>();
        String[] lines = tableContent.split(",(?=(?:[^']*'[^']*')*[^']*$)");

        for (String line : lines) {
            line = line.trim();

            // Ignorar líneas de constraint
            if (isConstraintLine(line)) {
                continue;
            }

            Matcher columnMatcher = COLUMN_PATTERN.matcher(line);
            if (columnMatcher.find()) {
                ColumnDefinition column = parseColumnDefinition(columnMatcher);
                if (column != null) {
                    columns.put(column.getColumnName(), column);
                }
            }
        }

        return columns;
    }

    private ColumnDefinition parseColumnDefinition(Matcher matcher) {
        String columnName = matcher.group(1);
        String dataType = matcher.group(2).toLowerCase();
        String size = matcher.group(3); // Puede ser null
        String constraints = matcher.group(4); // Resto de la definición

        ColumnDefinition column = new ColumnDefinition();
        column.setColumnName(columnName);
        column.setColumnType(dataType.trim());
        column.setPrimaryKey(isPrimaryKey(constraints));
        column.setNullable(!isNotNull(constraints));
        column.setUnique(isUnique(constraints));
        column.setForeignKey(isForeignKey(constraints));

        if (size != null) {
            column.setSize(size);
        }

        return column;
    }

    private boolean isConstraintLine(String line) {
        return line.toLowerCase().startsWith("constraint") ||
                line.toLowerCase().startsWith("primary key") ||
                line.toLowerCase().startsWith("foreign key");
    }

    private boolean isPrimaryKey(String constraints) {
        return constraints != null &&
                constraints.toLowerCase().contains("primary key");
    }

    private boolean isNotNull(String constraints) {
        return constraints != null &&
                constraints.toLowerCase().contains("not null");
    }

    private boolean isUnique(String constraints) {
        return constraints != null &&
                constraints.toLowerCase().contains("unique");
    }

    private boolean isForeignKey(String constraints) {
        return constraints != null &&
                constraints.toLowerCase().contains("references");
    }

    @Override
    public String mapDataType(String sqlType) {
        return TypeMapper.mapPostgresType(sqlType.toLowerCase());
    }
}