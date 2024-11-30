package cl.playground.core.model;

import java.util.Map;

public class TableDefinition {
    private String tableName;
    private Map<String, ColumnDefinition> columns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, ColumnDefinition> columns) {
        this.columns = columns;
    }
}
