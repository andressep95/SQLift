package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;

public interface EntityStrategy {
    String addClassAnnotations(String tableName);
    String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition isForeignKey, boolean isPrimaryKey);
    String addImports();
}
