package cl.playground.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import cl.playground.core.model.ColumnMetadata;
import cl.playground.core.model.RelationMetadata;
import cl.playground.core.model.TableMetadata;

public class SchemaProcessor {
    private final PostgresEngine engine;
    private Map<String, List<RelationMetadata>> inverseRelationsMap;

    public SchemaProcessor(PostgresEngine engine) {
        this.engine = engine;
        this.inverseRelationsMap = new HashMap<>();
    }

    public List<TableMetadata> processSchema(String schema) {
        List<TableMetadata> tables = new ArrayList<>();
        List<String> statements = engine.extractCreateTableStatements(schema);

        // Primera pasada: procesar estructura básica y relaciones directas
        statements.forEach(statement -> {
            TableMetadata table = new TableMetadata();
            String tableName = engine.extractTableName(statement).toLowerCase(); // Importante: normalizar a minúsculas
            table.setTableName(tableName);

            // Procesar columnas
            List<ColumnMetadata> columns = new ArrayList<>();
            engine.extractColumnDefinitions(statement).forEach(columnDef -> {
                ColumnMetadata column = new ColumnMetadata();
                column.setColumnName(engine.extractColumnName(columnDef));
                column.setColumnType(engine.extractColumnType(columnDef));
                column.setNotNull(engine.isNotNullColumn(columnDef));
                column.setUnique(engine.isUniqueColumn(columnDef));
                column.setDefaultValue(engine.extractDefaultValue(columnDef));
                columns.add(column);
            });
            table.setColumns(columns);

            // Procesar claves primarias
            table.setPrimaryKeys(engine.extractPrimaryKeyColumns(statement));

            // Procesar relaciones directas
            List<RelationMetadata> relations = new ArrayList<>();
            engine.extractTableRelations(statement).forEach(relationString -> {
                String[] parts = relationString.split(" -> ");
                String[] targetParts = parts[1].split("\\.");
                String sourceColumn = parts[0];
                String targetTable = targetParts[0].toLowerCase(); // Normalizar a minúsculas
                String targetColumn = targetParts[1];

                // Agregar relación directa
                relations.add(new RelationMetadata(
                        sourceColumn,
                        targetTable,
                        targetColumn,
                        true));

                // Registrar relación inversa
                inverseRelationsMap
                        .computeIfAbsent(targetTable, k -> new ArrayList<>())
                        .add(new RelationMetadata(
                                targetColumn,
                                tableName,
                                sourceColumn,
                                false));
            });
            table.setRelations(relations);
            tables.add(table);
        });

        // Segunda pasada: agregar relaciones inversas
        tables.forEach(table -> {
            List<RelationMetadata> inverseRelations = inverseRelationsMap.get(table.getTableName());
            if (inverseRelations != null && !inverseRelations.isEmpty()) {
                // Agregar las relaciones inversas a las relaciones existentes
                table.getRelations().addAll(inverseRelations);
            }
        });

        return tables;
    }

}