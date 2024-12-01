package cl.playground.core.model;

public class ColumnDefinition {

    private final String columnName;
    private final String columnType;
    private final boolean isNullable;
    private final String defaultValue;

    public ColumnDefinition(String columnName, String columnType, boolean isNullable, String defaultValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
