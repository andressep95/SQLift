package cl.playground.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDefinition {

    private final String className;
    private final String packageName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> primaryKeyColumns = new ArrayList<>();
    private final List<ColumnDefinition> attributes;
    private final List<ForeignKeyDefinition> foreignKeys;
    private final Set<String> imports;

    public ClassDefinition(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
        this.attributes = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.imports = new HashSet<>();
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public ColumnDefinition getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ColumnDefinition primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void addAttribute(ColumnDefinition column) {
        this.attributes.add(column);
    }

    public void addForeignKey(ForeignKeyDefinition foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    public void addImport(String importStatement) {
        this.imports.add(importStatement);
    }

    public List<ColumnDefinition> getAttributes() {
        return attributes;
    }

    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }

    public Set<String> getImports() {
        return imports;
    }

    public List<ColumnDefinition> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void setPrimaryKeyColumns(List<ColumnDefinition> primaryKeyColumns) {
        this.primaryKeyColumns.clear();
        this.primaryKeyColumns.addAll(primaryKeyColumns);
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
            "className='" + className + '\'' +
            ", packageName='" + packageName + '\'' +
            ", primaryKey=" + primaryKey +
            ", primaryKeyColumns=" + primaryKeyColumns +
            ", attributes=" + attributes +
            ", foreignKeys=" + foreignKeys +
            ", imports=" + imports +
            '}';
    }
}
