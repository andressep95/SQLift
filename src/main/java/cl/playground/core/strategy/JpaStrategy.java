package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;

import java.util.List;
import java.util.stream.Collectors;

public class JpaStrategy implements EntityStrategy {
    private final String importPrefix = "jakarta";

    @Override
    public String addClassAnnotations(String tableName, ClassDefinition classDefinition) {
        StringBuilder annotations = new StringBuilder();
        annotations.append("@Entity\n")
            .append("@Table(\n")
            .append("    name = \"").append(tableName.toLowerCase()).append("s").append("\"");

        // Agregar restricciones únicas si existen
        List<ColumnDefinition> uniqueColumns = getUniqueColumns(classDefinition);
        if (!uniqueColumns.isEmpty()) {
            annotations.append(",\n    uniqueConstraints = {\n");
            annotations.append(uniqueColumns.stream()
                .map(column -> "        @UniqueConstraint(name = \"uk_" + tableName.toLowerCase() + "s" + "_" + column.getColumnName() +
                    "\", columnNames = {\"" + column.getColumnName() + "\"})")
                .collect(Collectors.joining(",\n")));
            annotations.append("\n    }");
        }
        annotations.append(")\n");

        return annotations.toString();
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition foreignKey, boolean isPrimaryKey) {
        StringBuilder annotations = new StringBuilder();

        if (isPrimaryKey) {
            annotations.append("    @Id\n")
                .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                .append("    @Column(name = \"").append(column.getColumnName()).append("\")\n");
        } else if (foreignKey != null) {
            String relationshipAnnotation = switch (foreignKey.getRelationshipType()) {
                case ONE_TO_ONE -> "@OneToOne";
                case ONE_TO_MANY -> "@OneToMany";
                case MANY_TO_ONE -> "@ManyToOne";
                case MANY_TO_MANY -> "@ManyToMany";
            };

            annotations.append("    ").append(relationshipAnnotation)
                .append("(fetch = FetchType.").append(foreignKey.getFetchStrategy());

            if (!foreignKey.getCascadeStrategies().isEmpty()) {
                annotations.append(", cascade = {")
                    .append(foreignKey.getCascadeStrategies().stream()
                        .map(cascade -> "CascadeType." + cascade)
                        .collect(Collectors.joining(", ")))
                    .append("}");
            }

            annotations.append(")\n")
                .append("    @JoinColumn(name = \"").append(foreignKey.getColumnName())
                .append("\", nullable = ").append(column.isNullable()).append(")\n");
        } else {
            annotations.append("    @Column(name = \"").append(column.getColumnName()).append("\"");

            if (column.getLength() != null) {
                annotations.append(", length = ").append(column.getLength());
            }
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
                import %s.persistence.*;
                """,
            importPrefix);
    }

    private List<ColumnDefinition> getUniqueColumns(ClassDefinition classDefinition) {
        // Usar los atributos únicos directamente desde el classDefinition proporcionado
        return classDefinition.getAttributes().stream()
            .filter(ColumnDefinition::isUnique)
            .collect(Collectors.toList());
    }
}
