package cl.playground.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableDefinition {

    private final String tableName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> columns;
    private final List<ForeignKeyDefinition> foreignKeys;


    public TableDefinition(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnDefinition getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ColumnDefinition primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }
    public void addColumn(ColumnDefinition column) {
        this.columns.add(column);
    }

    public void addForeignKey(ForeignKeyDefinition foreignKey) {
        this.foreignKeys.add(foreignKey);
    }
}
