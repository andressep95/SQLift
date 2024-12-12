package cl.playground.core.engine;

import cl.playground.core.model.*;
import cl.playground.core.util.TypeMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgresEngine implements DatabaseEngine {
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "CREATE TABLE\\s+(\\w+)\\s*\\(([^;]+)\\);",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COLUMN_PATTERN = Pattern.compile(
            "(\\w+)\\s+(\\w+)(?:\\(([^)]+)\\))?(\\s+[^,]+)?",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FOREIGN_KEY_PATTERN = Pattern.compile(
            "REFERENCES\\s+(\\w+)\\s*\\((\\w+)\\)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage) {
        Map<String, ClassDefinition> classMap = new HashMap<>();
        Map<String, TableDefinition> tableMap = new HashMap<>();
        Matcher tableMatcher = TABLE_PATTERN.matcher(sqlContent);

        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).toLowerCase();
            String columnsDefinition = tableMatcher.group(2);

            TableDefinition tableDefinition = parseTableDefinition(tableName, columnsDefinition);
            tableMap.put(tableName, tableDefinition);

            String className = TypeMapper.toPascalCase(tableName);
            ClassDefinition classDefinition = new ClassDefinition(className, basePackage);

            // Establecer primary key
            if (tableDefinition.getPrimaryKey() != null) {
                classDefinition.setPrimaryKey(tableDefinition.getPrimaryKey());
            }

            // Agregar columnas regulares
            for (ColumnDefinition column : tableDefinition.getColumns()) {
                if (!column.equals(tableDefinition.getPrimaryKey())) {
                    classDefinition.addAttribute(column);
                }
            }

            // Agregar foreign keys
            for (ForeignKeyDefinition fk : tableDefinition.getForeignKeys()) {
                classDefinition.addForeignKey(fk);
            }

            classMap.put(tableName, classDefinition);
        }

        processRelationships(classMap);
        return classMap;
    }

    public String mapDataType(String sqlType, String columnName, boolean isForeignKey) {
        return TypeMapper.mapPostgresType(sqlType, columnName, isForeignKey);
    }


    private TableDefinition parseTableDefinition(String tableName, String columnsDefinition) {
        TableDefinition tableDefinition = new TableDefinition(tableName);
        Matcher columnMatcher = COLUMN_PATTERN.matcher(columnsDefinition);

        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String columnType = columnMatcher.group(2);
            String size = columnMatcher.group(3);
            String constraints = columnMatcher.group(4);

            boolean isNullable = constraints == null || !constraints.toLowerCase().contains("not null");
            boolean isUnique = constraints != null && constraints.toLowerCase().contains("unique");
            String defaultValue = extractDefaultValue(constraints);
            String length = columnType.toLowerCase().contains("varchar") ? size : null;

            // Verificar si es foreign key primero
            if (constraints != null) {
                Matcher fkMatcher = FOREIGN_KEY_PATTERN.matcher(constraints);
                if (fkMatcher.find()) {
                    String referencedTableName = fkMatcher.group(1).toLowerCase();
                    String referencedColumnName = fkMatcher.group(2);
                    // Para foreign keys, usamos el tipo de la entidad referenciada
                    ColumnDefinition column = new ColumnDefinition(
                            columnName,
                            referencedTableName,
                            isNullable,
                            defaultValue,
                            isUnique,
                            length,
                            true
                    );
                    tableDefinition.addColumn(column);

                    ForeignKeyDefinition fk = new ForeignKeyDefinition(
                            columnName,
                            referencedTableName,
                            referencedColumnName
                    );
                    tableDefinition.addForeignKey(fk);
                    continue;
                }
            }

            // Para columnas normales
            ColumnDefinition column = new ColumnDefinition(
                    columnName,
                    columnType,
                    isNullable,
                    defaultValue,
                    isUnique,
                    length
            );

            if (isPrimaryKey(constraints)) {
                tableDefinition.setPrimaryKey(column);
            } else {
                tableDefinition.addColumn(column);
            }
        }

        return tableDefinition;
    }

    private void processRelationships(Map<String, ClassDefinition> classMap) {
        for (ClassDefinition classDefinition : classMap.values()) {
            Map<String, ColumnDefinition> columnUpdates = new HashMap<>();

            for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
                String referencedTableName = fk.getReferenceTableName();
                ClassDefinition referencedClass = classMap.get(referencedTableName);

                if (referencedClass != null) {
                    // Encontrar la columna original
                    String originalColumnName = fk.getColumnName();
                    String newFieldName = originalColumnName.replace("_id", "");

                    // Crear la nueva columna manteniendo el mismo nombre que la FK
                    ColumnDefinition relationColumn = new ColumnDefinition(
                            originalColumnName,           // Mantener el nombre original
                            referencedClass.getClassName(), // columnType
                            true,                         // isNullable
                            null,                         // defaultValue
                            false,                        // isUnique
                            null,                         // length
                            true                          // isForeignKey
                    );

                    // Guardar para actualizar después
                    columnUpdates.put(originalColumnName, relationColumn);
                }
            }

            // Actualizar todas las columnas de una vez
            for (Map.Entry<String, ColumnDefinition> update : columnUpdates.entrySet()) {
                // Reemplazar la columna vieja por la nueva
                classDefinition.getAttributes().removeIf(attr ->
                        attr.getColumnName().equals(update.getKey())
                );
                classDefinition.addAttribute(update.getValue());
            }
        }
    }

    private boolean isPrimaryKey(String constraints) {
        return constraints != null &&
                (constraints.toLowerCase().contains("primary key") ||
                        constraints.toLowerCase().contains("serial primary"));
    }

    private String extractDefaultValue(String constraints) {
        if (constraints == null) return null;
        Pattern defaultPattern = Pattern.compile("DEFAULT\\s+([^\\s,]+)", Pattern.CASE_INSENSITIVE);
        Matcher defaultMatcher = defaultPattern.matcher(constraints);
        return defaultMatcher.find() ? defaultMatcher.group(1) : null;
    }
}