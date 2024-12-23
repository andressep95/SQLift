package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;

public interface EntityStrategy {
    String addClassAnnotations(String tableName, ClassDefinition classDefinition);
    String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition isForeignKey, boolean isPrimaryKey);
    String addImports();
}
