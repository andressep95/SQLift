package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;
import cl.playground.core.model.TableType;

public interface EntityStrategy {
    String addClassAnnotations(String tableName, TableType tableType);
    String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition foreignKey, boolean isPrimaryKey, TableType tableType);
    String addRelationalAnnotations(ClassDefinition classDefinition, TableType tableType);
    String addImports();
}