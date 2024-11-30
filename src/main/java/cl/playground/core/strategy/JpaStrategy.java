package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;

public class JpaStrategy implements EntityStrategy {
    @Override
    public String addImports() {
        return "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "import javax.persistence.Column;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.GeneratedValue;\n" +
                "import javax.persistence.GenerationType;\n";
    }

    @Override
    public String addClassAnnotations(String tableName) {
        return "@Entity\n" +
                "@Table(name = \"" + tableName + "\")\n";
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column) {
        StringBuilder annotations = new StringBuilder();

        if (column.isPrimaryKey()) {
            annotations.append("    @Id\n")
                    .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        }

        annotations.append("    @Column(name = \"").append(column.getColumnName()).append("\"");
        if (!column.isNullable()) {
            annotations.append(", nullable = false");
        }
        annotations.append(")\n");

        return annotations.toString();
    }
}
