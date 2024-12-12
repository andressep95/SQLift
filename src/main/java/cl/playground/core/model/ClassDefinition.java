package cl.playground.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDefinition {

    private final String className;
    private final String packageName;
    private final String tableName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> attributes;
    private final List<ForeignKeyDefinition> foreignKeys;
    private final Set<String> imports;
    private TableType tableType = TableType.REGULAR;
    private ClassDefinition firstRelatedClass;
    private ClassDefinition secondRelatedClass;

    public ClassDefinition(String className, String packageName, String tableName) {
        this.className = className;
        this.packageName = packageName;
        this.tableName = tableName;
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

    public String getTableName() {
        return tableName;
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

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public void setRelatedClasses(ClassDefinition first, ClassDefinition second) {
        this.firstRelatedClass = first;
        this.secondRelatedClass = second;
    }

    public ClassDefinition getFirstRelatedClass() {
        return firstRelatedClass;
    }

    public ClassDefinition getSecondRelatedClass() {
        return secondRelatedClass;
    }
}
