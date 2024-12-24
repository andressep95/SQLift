package cl.playground.core.engine;

import cl.playground.core.model.*;
import cl.playground.core.util.TypeMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgresEngine implements DatabaseEngine {

    @Override
    public Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage) {
        Map<String, ClassDefinition> classMap = new HashMap<>();
        Matcher tableMatcher = Pattern.compile("CREATE TABLE\\s+(\\w+)\\s*\\(([^;]+)\\);", Pattern.CASE_INSENSITIVE).matcher(sqlContent);

        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).toLowerCase();
            String columnsDefinition = tableMatcher.group(2);

            System.out.println("Procesando tabla: " + tableName);
            TableDefinition tableDefinition = parseTableDefinition(tableName, columnsDefinition);

            String className = TypeMapper.toPascalCase(tableName);
            ClassDefinition classDefinition = new ClassDefinition(className, basePackage);

            // Establecer clave primaria
            if (!tableDefinition.getPrimaryKeyColumns().isEmpty()) {
                System.out.println("Claves primarias detectadas para la tabla " + tableName + ": " + tableDefinition.getPrimaryKeyColumns());
                classDefinition.setPrimaryKeyColumns(tableDefinition.getPrimaryKeyColumns());
            }

            // Agregar columnas regulares
            for (ColumnDefinition column : tableDefinition.getColumns()) {
                if (!tableDefinition.getPrimaryKeyColumns().contains(column)) {
                    System.out.println("Agregando atributo: " + column);
                    classDefinition.addAttribute(column);
                }
            }

            // Agregar claves foráneas
            for (ForeignKeyDefinition fk : tableDefinition.getForeignKeys()) {
                System.out.println("Agregando clave foránea: " + fk);
                classDefinition.addForeignKey(fk);
            }

            classMap.put(tableName, classDefinition);
        }

        processRelationships(classMap);
        System.out.println("Clases generadas: " + classMap);
        return classMap;
    }

    @Override
    public String mapDataType(String sqlType, String columnName, boolean isForeignKey) {
        String mappedType = TypeMapper.mapPostgresType(sqlType, columnName, isForeignKey);
        System.out.println("Mapeando tipo SQL: " + sqlType + " a tipo Java: " + mappedType);
        return mappedType;
    }

    private TableDefinition parseTableDefinition(String tableName, String columnsDefinition) {
        TableDefinition tableDefinition = new TableDefinition(tableName);
        Matcher columnMatcher = Pattern.compile("(\\w+)\\s+(\\w+)(?:\\(([^)]+)\\))?(\\s+[^,]+)?", Pattern.CASE_INSENSITIVE).matcher(columnsDefinition);

        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String columnType = columnMatcher.group(2);
            String constraints = columnMatcher.group(4);

            // Ignorar palabras clave PRIMARY y FOREIGN
            if (columnName.equalsIgnoreCase("PRIMARY") || columnName.equalsIgnoreCase("FOREIGN")) {
                continue;
            }

            System.out.println("Procesando columna: " + columnName);
            ColumnDefinition column = identifyColumnConstraints(columnName, columnType, constraints);
            tableDefinition.addColumn(column);

            if (constraints != null) {
                identifyForeignKeys(columnsDefinition, tableDefinition);
            }
        }

        // Detectar clave primaria
        identifyPrimaryKey(columnsDefinition, tableDefinition);

        return tableDefinition;
    }

    private void identifyPrimaryKey(String columnsDefinition, TableDefinition tableDefinition) {
        Matcher primaryKeyMatcher = Pattern.compile("PRIMARY KEY\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE).matcher(columnsDefinition);
        if (primaryKeyMatcher.find()) {
            String[] primaryKeyColumns = primaryKeyMatcher.group(1).split(",");
            for (String pkColumn : primaryKeyColumns) {
                pkColumn = pkColumn.trim();
                ColumnDefinition column = tableDefinition.getColumnByName(pkColumn);
                if (column != null) {
                    tableDefinition.addPrimaryKeyColumn(column);
                    System.out.println("Clave primaria compuesta detectada: " + pkColumn);
                }
            }
        }
    }

    private void identifyForeignKeys(String columnsDefinition, TableDefinition tableDefinition) {
        // Patrón para capturar foreign keys definidas al final de la tabla
        Pattern fkPattern = Pattern.compile(
            "FOREIGN KEY\\s*\\(([^)]+)\\)\\s+REFERENCES\\s+(\\w+)\\s*\\(([^)]+)\\)(\\s+ON\\s+DELETE\\s+\\w+)?",
            Pattern.CASE_INSENSITIVE
                                           );

        Matcher fkMatcher = fkPattern.matcher(columnsDefinition);
        // Limpiar las foreign keys existentes para evitar duplicados
        tableDefinition.getForeignKeys().clear();

        while (fkMatcher.find()) {
            String columnName = fkMatcher.group(1).trim();
            String referencedTable = fkMatcher.group(2).toLowerCase();
            String referencedColumn = fkMatcher.group(3).trim();

            ForeignKeyDefinition fk = new ForeignKeyDefinition(
                columnName,
                referencedTable,
                referencedColumn
            );
            tableDefinition.addForeignKey(fk);
        }
    }

    private ColumnDefinition identifyColumnConstraints(String columnName, String columnType, String constraints) {
        boolean isNullable = constraints == null || !constraints.toLowerCase().contains("not null");
        boolean isUnique = constraints != null && constraints.toLowerCase().contains("unique");
        String defaultValue = extractDefaultValue(constraints);
        String length = columnType.toLowerCase().contains("varchar") ? extractLength(columnType) : null;

        ColumnDefinition column = new ColumnDefinition(
            columnName,
            columnType,
            isNullable,
            defaultValue,
            isUnique,
            length
        );

        System.out.println("Restricciones de columna detectadas: " + column);
        return column;
    }

    private String extractDefaultValue(String constraints) {
        if (constraints == null) return null;
        Matcher defaultMatcher = Pattern.compile("DEFAULT\\s+([^\\s,]+)", Pattern.CASE_INSENSITIVE).matcher(constraints);
        String defaultValue = defaultMatcher.find() ? defaultMatcher.group(1) : null;
        System.out.println("Valor por defecto extraído: " + defaultValue);
        return defaultValue;
    }

    private String extractLength(String columnType) {
        Matcher lengthMatcher = Pattern.compile("\\((\\d+)\\)").matcher(columnType);
        return lengthMatcher.find() ? lengthMatcher.group(1) : null;
    }

    private void processRelationships(Map<String, ClassDefinition> classMap) {
        for (ClassDefinition classDefinition : classMap.values()) {
            Map<String, ColumnDefinition> columnUpdates = new HashMap<>();

            for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
                String referencedTableName = fk.getReferenceTableName();
                ClassDefinition referencedClass = classMap.get(referencedTableName);

                if (referencedClass != null) {
                    String originalColumnName = fk.getColumnName();
                    String newFieldName = originalColumnName.replace("_id", "");

                    System.out.println("Procesando relación: " + classDefinition.getClassName() + " -> " + referencedClass.getClassName());

                    ColumnDefinition relationColumn = new ColumnDefinition(
                        originalColumnName,
                        referencedClass.getClassName(),
                        true,
                        null,
                        false,
                        null
                    );

                    columnUpdates.put(originalColumnName, relationColumn);
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
}
