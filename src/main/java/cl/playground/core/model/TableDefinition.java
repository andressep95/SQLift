package cl.playground.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableDefinition {

    private final String tableName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> columns;
    private final List<ForeignKeyDefinition> foreignKeys;
    private TableType tableType;
    private TableDefinition firstRelatedTable;
    private TableDefinition secondRelatedTable;
    private String rawDefinition;

    public TableDefinition(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.tableType = TableType.REGULAR;
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

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public void setRelatedTables(TableDefinition first, TableDefinition second) {
        this.firstRelatedTable = first;
        this.secondRelatedTable = second;
    }

    public TableDefinition getFirstRelatedTable() {
        return firstRelatedTable;
    }

    public TableDefinition getSecondRelatedTable() {
        return secondRelatedTable;
    }

    public void setRawDefinition(String rawDefinition) {
        this.rawDefinition = rawDefinition;
    }

    public String getRawDefinition() {
        return rawDefinition;
    }
}
