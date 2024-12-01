package cl.playground.core.model;

public class ForeignKeyDefinition {
    private final String columnName;
    private final String referenceTableName;
    private final String referenceColumnName;

    public ForeignKeyDefinition(String columnName, String referenceTableName, String referenceColumnName) {
        this.columnName = columnName;
        this.referenceTableName = referenceTableName;
        this.referenceColumnName = referenceColumnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getReferenceTableName() {
        return referenceTableName;
    }

    public String getReferenceColumnName() {
        return referenceColumnName;
    }
}