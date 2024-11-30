package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;

public interface EntityStrategy {
    String addImports();
    String addClassAnnotations(String tableName);
    String addFieldAnnotations(ColumnDefinition column);
}
