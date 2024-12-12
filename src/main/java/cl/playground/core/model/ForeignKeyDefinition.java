package cl.playground.core.model;

import cl.playground.core.strategy.Definition;

import java.util.ArrayList;
import java.util.List;

public class ForeignKeyDefinition implements Definition {
    private final String columnName;
    private final String referenceTableName;
    private final String referenceColumnName;
    private FetchStrategy fetchStrategy = FetchStrategy.LAZY; // Por defecto LAZY
    private List<CascadeStrategy> cascadeStrategies = new ArrayList<>();
    private RelationshipType relationshipType = RelationshipType.MANY_TO_ONE; // Por defecto
    private boolean isOwner = true;

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
    @Override
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

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}