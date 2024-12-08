package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;

public class JpaStrategy implements EntityStrategy {

    private final String importPrefix;

    public JpaStrategy(String type) {
        this.importPrefix = (type != null && type.equalsIgnoreCase("jakarta")) ? "jakarta" : "javax";
    }

    @Override
    public String addClassAnnotations(String tableName) {
        return "@Entity\n" +
                "@Table(name = \"" + tableName + "\")\n";
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, boolean isForeignKey, boolean isPrimaryKey) {
        StringBuilder annotations = new StringBuilder();

        if (isPrimaryKey) {
            annotations.append("    @Id\n")
                    .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                    .append("    @Column(name = \"").append(column.getColumnName()).append("\")\n");
        } else if (isForeignKey) {
            annotations.append("\", nullable = ").append(column.isNullable() ? "true" : "false")
                    .append(")\n");
        } else {
            annotations.append("    @Column(name = \"").append(column.getColumnName()).append("\"");
            if (!column.isNullable()) {
                annotations.append(", nullable = false");
            }
            annotations.append(")\n");
        }

        return annotations.toString();
    }


    @Override
    public String addImports() {
        return String.format("""
            import %s.persistence.Entity;
            import %s.persistence.Table;
            import %s.persistence.Column;
            import %s.persistence.Id;
            import %s.persistence.GeneratedValue;
            import %s.persistence.GenerationType;
            """,
                importPrefix, importPrefix, importPrefix, importPrefix,
                importPrefix, importPrefix, importPrefix, importPrefix);
    }
}
