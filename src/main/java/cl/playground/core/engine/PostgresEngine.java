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
    private static final Pattern INTERMEDIATE_TABLE_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(\\w+)\\s*\\(\\s*" +
                    ".*?REFERENCES\\s+(\\w+)\\s*\\([^)]+\\)\\s*(ON\\s+(?:DELETE|UPDATE)\\s+(?:CASCADE|RESTRICT|SET\\s+NULL))?" +
                    ".*?REFERENCES\\s+(\\w+)\\s*\\([^)]+\\)\\s*(ON\\s+(?:DELETE|UPDATE)\\s+(?:CASCADE|RESTRICT|SET\\s+NULL))?" +
                    ".*?\\)\\s*;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage) {
        Map<String, ClassDefinition> classMap = new HashMap<>();
        Map<String, TableDefinition> tableMap = new HashMap<>();

        // Paso 1: Parsear todas las tablas
        parseAllTables(sqlContent, tableMap);

        // Paso 2: Crear las ClassDefinition básicas
        createBasicClassDefinitions(tableMap, classMap, basePackage);

        // Paso 3: Procesar relaciones básicas
        processBasicRelationships(classMap);

        // Paso 4: Analizar y procesar tablas intermedias
        processIntermediateTables(tableMap, classMap);

        return classMap;
    }

    private void parseAllTables(String sqlContent, Map<String, TableDefinition> tableMap) {
        Matcher tableMatcher = TABLE_PATTERN.matcher(sqlContent);
        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).toLowerCase();
            String columnsDefinition = tableMatcher.group(2);

            TableDefinition tableDefinition = parseTableDefinition(tableName, columnsDefinition);
            tableDefinition.setRawDefinition(tableMatcher.group(0));
            tableMap.put(tableName, tableDefinition);
        }
    }

    private void createBasicClassDefinitions(Map<String, TableDefinition> tableMap,
                                             Map<String, ClassDefinition> classMap,
                                             String basePackage) {
        for (TableDefinition tableDefinition : tableMap.values()) {
            String className = TypeMapper.toPascalCase(tableDefinition.getTableName());
            ClassDefinition classDefinition = new ClassDefinition(
                    className,
                    basePackage,
                    tableDefinition.getTableName());

            if (tableDefinition.getPrimaryKey() != null) {
                classDefinition.setPrimaryKey(tableDefinition.getPrimaryKey());
            }

            for (ColumnDefinition column : tableDefinition.getColumns()) {
                if (!column.equals(tableDefinition.getPrimaryKey())) {
                    classDefinition.addAttribute(column);
                }
            }

            for (ForeignKeyDefinition fk : tableDefinition.getForeignKeys()) {
                classDefinition.addForeignKey(fk);
            }

            classMap.put(tableDefinition.getTableName(), classDefinition);
        }
    }

    private void processBasicRelationships(Map<String, ClassDefinition> classMap) {
        for (ClassDefinition classDefinition : classMap.values()) {
            Map<String, ColumnDefinition> columnUpdates = new HashMap<>();

            for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
                String referencedTableName = fk.getReferenceTableName();
                ClassDefinition referencedClass = classMap.get(referencedTableName);

                if (referencedClass != null) {
                    ColumnDefinition relationColumn = new ColumnDefinition(
                            fk.getColumnName(),
                            referencedClass.getClassName(),
                            true,
                            null,
                            false,
                            null,
                            true
                    );
                    columnUpdates.put(fk.getColumnName(), relationColumn);
                }
            }

            for (Map.Entry<String, ColumnDefinition> update : columnUpdates.entrySet()) {
                classDefinition.getAttributes().removeIf(attr ->
                        attr.getColumnName().equals(update.getKey())
                );
                classDefinition.addAttribute(update.getValue());
            }
        }
    }

    private void processIntermediateTables(Map<String, TableDefinition> tableMap,
                                           Map<String, ClassDefinition> classMap) {
        for (TableDefinition table : tableMap.values()) {
            if (isIntermediateTable(table, tableMap)) {
                List<ForeignKeyDefinition> fks = table.getForeignKeys();
                TableDefinition firstTable = tableMap.get(fks.get(0).getReferenceTableName());
                TableDefinition secondTable = tableMap.get(fks.get(1).getReferenceTableName());

                if (firstTable != null && secondTable != null) {
                    table.setTableType(TableType.INTERMEDIATE);
                    table.setRelatedTables(firstTable, secondTable);

                    ClassDefinition classDefinition = classMap.get(table.getTableName());
                    if (classDefinition != null) {
                        classDefinition.setTableType(TableType.INTERMEDIATE);
                        classDefinition.setRelatedClasses(
                                classMap.get(firstTable.getTableName()),
                                classMap.get(secondTable.getTableName())
                        );
                    }
                }
            }
        }
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

    private boolean isIntermediateTable(TableDefinition table, Map<String, TableDefinition> allTables) {
        if (table.getForeignKeys().size() != 2) return false;

        Matcher matcher = INTERMEDIATE_TABLE_PATTERN.matcher(table.getRawDefinition());
        if (!matcher.matches()) return false;

        String firstTable = matcher.group(2);
        String secondTable = matcher.group(4);
        String tableName = table.getTableName();

        return followsNamingConvention(tableName, firstTable, secondTable) &&
                hasProperCascading(table.getRawDefinition());
    }

    private boolean followsNamingConvention(String tableName, String firstTable, String secondTable) {
        String normalizedName = tableName.toLowerCase();
        String combined = firstTable + "_" + secondTable;
        String reversed = secondTable + "_" + firstTable;

        return normalizedName.equals(combined) ||
                normalizedName.equals(reversed) ||
                normalizedName.endsWith("_rel") ||
                normalizedName.endsWith("_map") ||
                normalizedName.endsWith("_link");
    }

    private boolean hasProperCascading(String tableDefinition) {
        return tableDefinition.toLowerCase().contains("on delete") ||
                tableDefinition.toLowerCase().contains("on update");
    }
}