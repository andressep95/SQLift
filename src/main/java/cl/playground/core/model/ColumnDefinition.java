package cl.playground.core.model;

public class ColumnDefinition {
    private final String columnName;
    private final String columnType;
    private final boolean isNullable;
    private final String defaultValue;
    private final boolean isUnique;
    private final String length;
    private boolean isForeignKey;

    public ColumnDefinition(String columnName, String columnType, boolean isNullable,
                            String defaultValue, boolean isUnique, String length) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.isUnique = isUnique;
        this.length = length;
        this.isForeignKey = false; // Por defecto no es clave foránea
    }

    // Constructor sobrecargado que incluye isForeignKey
    public ColumnDefinition(String columnName, String columnType, boolean isNullable,
                            String defaultValue, boolean isUnique, String length,
                            boolean isForeignKey) {
        this(columnName, columnType, isNullable, defaultValue, isUnique, length);
        this.isForeignKey = isForeignKey;
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

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.isForeignKey = foreignKey;
    }
}