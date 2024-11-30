package cl.playground.core.model;

import java.util.*;

public class ClassDefinition {
    private String className;
    private String packageName;
    private String tableName;  // Agregar esto
    private Set<String> imports;
    private List<ColumnDefinition> columns;

    public ClassDefinition() {
        this.imports = new HashSet<>();
        this.columns = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void addImport(String importStatement) {
        this.imports.add(importStatement);
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void addColumn(ColumnDefinition column) {
        this.columns.add(column);
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    // Método para obtener columnas en orden específico
    public List<ColumnDefinition> getOrderedColumns() {
        // Ordenar columnas: primero ID, luego foreign keys, luego el resto
        List<ColumnDefinition> orderedColumns = new ArrayList<>();

        // Primero agregar ID
        columns.stream()
                .filter(ColumnDefinition::isPrimaryKey)
                .findFirst()
                .ifPresent(orderedColumns::add);

        // Luego agregar foreign keys
        columns.stream()
                .filter(c -> c.isForeignKey() && !c.isPrimaryKey())
                .forEach(orderedColumns::add);

        // Finalmente agregar el resto
        columns.stream()
                .filter(c -> !c.isPrimaryKey() && !c.isForeignKey())
                .forEach(orderedColumns::add);

        return orderedColumns;
    }
}