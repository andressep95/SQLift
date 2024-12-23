package cl.playground.core.model;

import java.util.ArrayList;
import java.util.List;

public class ForeignKeyDefinition {
    private final String columnName;
    private final String referenceTableName;
    private final String referenceColumnName;
    private FetchStrategy fetchStrategy = FetchStrategy.LAZY; // Por defecto LAZY
    private List<CascadeStrategy> cascadeStrategies = new ArrayList<>();
    private RelationshipType relationshipType = RelationshipType.MANY_TO_ONE; // Por defecto

    public enum FetchStrategy {
        LAZY,
        EAGER
    }

    public enum CascadeStrategy {
        ALL,
        PERSIST,
        MERGE,
        REMOVE,
        REFRESH,
        DETACH
    }

    public enum RelationshipType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }

    // Constructor base (el actual)
    public ForeignKeyDefinition(String columnName, String referenceTableName, String referenceColumnName) {
        this.columnName = columnName;
        this.referenceTableName = referenceTableName;
        this.referenceColumnName = referenceColumnName;
    }

    // Constructor completo
    public ForeignKeyDefinition(String columnName, String referenceTableName,
                                String referenceColumnName, FetchStrategy fetchStrategy,
                                List<CascadeStrategy> cascadeStrategies,
                                RelationshipType relationshipType) {
        this(columnName, referenceTableName, referenceColumnName);
        this.fetchStrategy = fetchStrategy;
        this.cascadeStrategies = cascadeStrategies;
        this.relationshipType = relationshipType;
    }

    // Getters
    public String getColumnName() {
        return columnName;
    }

    public String getReferenceTableName() {
        return referenceTableName;
    }

    public String getReferenceColumnName() {
        return referenceColumnName;
    }

    public FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public List<CascadeStrategy> getCascadeStrategies() {
        return cascadeStrategies;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    @Override
    public String toString() {
        return "ForeignKeyDefinition{" +
            "columnName='" + columnName + '\'' +
            ", referenceTableName='" + referenceTableName + '\'' +
            ", referenceColumnName='" + referenceColumnName + '\'' +
            ", fetchStrategy=" + fetchStrategy +
            ", cascadeStrategies=" + cascadeStrategies +
            ", relationshipType=" + relationshipType +
            '}';
    }
}