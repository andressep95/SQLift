package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;

public class JpaStrategy implements EntityStrategy {

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
            annotations.append("    @ManyToOne\n")
                    .append("    @JoinColumn(name = \"").append(column.getColumnName())
                    .append("\", nullable = ").append(column.isNullable() ? "true" : "false")
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
        return "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "import javax.persistence.Column;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.GeneratedValue;\n" +
                "import javax.persistence.GenerationType;\n" +
                "import javax.persistence.ManyToOne;\n" +
                "import javax.persistence.JoinColumn;\n";
    }
}
