package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;

import java.util.stream.Collectors;

public class JpaStrategy implements EntityStrategy {
    private final String importPrefix;

    public JpaStrategy(String type) {
        this.importPrefix = (type != null && type.equalsIgnoreCase("jakarta")) ? "jakarta" : "javax";
    }

    @Override
    public String addClassAnnotations(String tableName) {
        return "@Entity\n" +
                "@Table(name = \"" + tableName.toLowerCase() + "\")\n";
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition foreignKey, boolean isPrimaryKey) {
        StringBuilder annotations = new StringBuilder();

        if (isPrimaryKey) {
            annotations.append("    @Id\n")
                    .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                    .append("    @Column(name = \"").append(column.getColumnName()).append("\")\n");
        } else if (foreignKey != null) {
            // Convertir el RelationshipType a la anotación JPA correspondiente
            String relationshipAnnotation = switch (foreignKey.getRelationshipType()) {
                case ONE_TO_ONE -> "@OneToOne";
                case ONE_TO_MANY -> "@OneToMany";
                case MANY_TO_ONE -> "@ManyToOne";
                case MANY_TO_MANY -> "@ManyToMany";
            };

            annotations.append("    ")
                    .append(relationshipAnnotation)
                    .append("(fetch = FetchType.")
                    .append(foreignKey.getFetchStrategy());

            // Agregar cascade si existe
            if (!foreignKey.getCascadeStrategies().isEmpty()) {
                annotations.append(", cascade = {");
                String cascades = foreignKey.getCascadeStrategies().stream()
                        .map(cascade -> "CascadeType." + cascade)
                        .collect(Collectors.joining(", "));
                annotations.append(cascades).append("}");
            }

            annotations.append(")\n");

            annotations.append("    @JoinColumn(name = \"")
                    .append(foreignKey.getColumnName())
                    .append("\", nullable = ")
                    .append(column.isNullable())
                    .append(")\n");
        } else {
            annotations.append("    @Column(name = \"").append(column.getColumnName()).append("\"");

            if (column.getLength() != null) {
                annotations.append(", length = ").append(column.getLength());
            }
            if (!column.isNullable()) {
                annotations.append(", nullable = false");
            }
            if (column.isUnique()) {
                annotations.append(", unique = true");
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
            import %s.persistence.OneToOne;
            import %s.persistence.OneToMany;
            import %s.persistence.ManyToOne;
            import %s.persistence.ManyToMany;
            import %s.persistence.JoinColumn;
            import %s.persistence.FetchType;
            import %s.persistence.CascadeType;
            """,
                importPrefix, importPrefix, importPrefix, importPrefix,
                importPrefix, importPrefix, importPrefix, importPrefix,
                importPrefix, importPrefix, importPrefix, importPrefix,
                importPrefix);
    }
}