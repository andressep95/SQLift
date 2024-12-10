package cl.playground.core.model;

public class ColumnDefinition {

    private final String columnName;
    private final String columnType;
    private final boolean isNullable;
    private final String defaultValue;
    private final boolean isUnique;
    private final String length;

    public ColumnDefinition(String columnName, String columnType, boolean isNullable, String defaultValue, boolean isUnique, String length) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.isUnique = isUnique;
        this.length = length;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public String getLength() {
        return length;
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
