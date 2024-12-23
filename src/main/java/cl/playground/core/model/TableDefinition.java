package cl.playground.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableDefinition {

    private final String tableName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> columns;
    private final List<ForeignKeyDefinition> foreignKeys;
    private final List<ColumnDefinition> primaryKeyColumns = new ArrayList<>();


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


    public List<ColumnDefinition> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void addPrimaryKeyColumn(ColumnDefinition column) {
        primaryKeyColumns.add(column);
    }

    public ColumnDefinition getColumnByName(String columnName) {
        return this.columns.stream()
            .filter(column -> column.getColumnName().equalsIgnoreCase(columnName))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String toString() {
        return "TableDefinition{" +
            "tableName='" + tableName + '\'' +
            ", primaryKey=" + primaryKey +
            ", columns=" + columns +
            ", foreignKeys=" + foreignKeys +
            ", primaryKeyColumns=" + primaryKeyColumns +
            '}';
    }
}
