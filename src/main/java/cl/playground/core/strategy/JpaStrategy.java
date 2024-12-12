package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;
import cl.playground.core.model.TableType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JpaStrategy implements EntityStrategy {
    private final String importPrefix;
    private Set<String> requiredImports;

    public JpaStrategy(String type) {
        this.importPrefix = (type != null && type.equalsIgnoreCase("jakarta")) ? "jakarta" : "javax";
        this.requiredImports = new HashSet<>();
    }

    @Override
    public String addClassAnnotations(String tableName, TableType tableType) {
        StringBuilder annotations = new StringBuilder();
        requiredImports.add("Entity");
        requiredImports.add("Table");

        annotations.append("@Entity\n");
        annotations.append("@Table(name = \"").append(tableName.toLowerCase()).append("\")\n");

        return annotations.toString();
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition foreignKey, boolean isPrimaryKey, TableType tableType) {
        StringBuilder annotations = new StringBuilder();

        if (isPrimaryKey) {
            requiredImports.add("Id");
            requiredImports.add("GeneratedValue");
            requiredImports.add("GenerationType");
            requiredImports.add("Column");

            annotations.append("    @Id\n")
                    .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                    .append("    @Column(name = \"").append(column.getColumnName()).append("\")\n");
        } else if (foreignKey != null) {
            if (tableType == TableType.INTERMEDIATE) {
                requiredImports.add("JoinColumn");
                annotations.append("    @JoinColumn(name = \"")
                        .append(foreignKey.getColumnName())
                        .append("\", nullable = ")
                        .append(column.isNullable())
                        .append(")\n");
            } else {
                String relationshipAnnotation = switch (foreignKey.getRelationshipType()) {
                    case ONE_TO_ONE -> {
                        requiredImports.add("OneToOne");
                        yield "@OneToOne";
                    }
                    case ONE_TO_MANY -> {
                        requiredImports.add("OneToMany");
                        yield "@OneToMany";
                    }
                    case MANY_TO_ONE -> {
                        requiredImports.add("ManyToOne");
                        yield "@ManyToOne";
                    }
                    case MANY_TO_MANY -> {
                        requiredImports.add("ManyToMany");
                        yield "@ManyToMany";
                    }
                };

                requiredImports.add("FetchType");
                annotations.append("    ")
                        .append(relationshipAnnotation)
                        .append("(fetch = FetchType.")
                        .append(foreignKey.getFetchStrategy());

                if (!foreignKey.getCascadeStrategies().isEmpty()) {
                    requiredImports.add("CascadeType");
                    annotations.append(", cascade = {");
                    String cascades = foreignKey.getCascadeStrategies().stream()
                            .map(cascade -> "CascadeType." + cascade)
                            .collect(Collectors.joining(", "));
                    annotations.append(cascades).append("}");
                }

                annotations.append(")\n");
                requiredImports.add("JoinColumn");
                annotations.append("    @JoinColumn(name = \"")
                        .append(foreignKey.getColumnName())
                        .append("\", nullable = ")
                        .append(column.isNullable())
                        .append(")\n");
            }
        } else {
            requiredImports.add("Column");
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
    public String addRelationalAnnotations(ClassDefinition classDefinition, TableType tableType) {
        if (tableType == TableType.INTERMEDIATE) {
            return "";
        }

        StringBuilder annotations = new StringBuilder();

        if (classDefinition.getFirstRelatedClass() != null && classDefinition.getSecondRelatedClass() != null) {
            requiredImports.add("ManyToMany");
            requiredImports.add("JoinTable");
            requiredImports.add("JoinColumn");
            requiredImports.add("Set");

            annotations.append("    @ManyToMany\n")
                    .append("    @JoinTable(\n")
                    .append("        name = \"").append(classDefinition.getTableName().toLowerCase()).append("\",\n")
                    .append("        joinColumns = @JoinColumn(name = \"").append(classDefinition.getPrimaryKey().getColumnName()).append("\"),\n")
                    .append("        inverseJoinColumns = @JoinColumn(name = \"")
                    .append(classDefinition.getSecondRelatedClass().getPrimaryKey().getColumnName())
                    .append("\")\n")
                    .append("    )\n")
                    .append("    private Set<").append(classDefinition.getSecondRelatedClass().getClassName()).append("> ")
                    .append(toCamelCase(classDefinition.getSecondRelatedClass().getClassName())).append("s;\n\n");
        }

        for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
            ClassDefinition referencedClass = null;
            if (classDefinition.getFirstRelatedClass() != null &&
                    fk.getReferenceTableName().equals(classDefinition.getFirstRelatedClass().getTableName())) {
                referencedClass = classDefinition.getFirstRelatedClass();
            } else if (classDefinition.getSecondRelatedClass() != null &&
                    fk.getReferenceTableName().equals(classDefinition.getSecondRelatedClass().getTableName())) {
                referencedClass = classDefinition.getSecondRelatedClass();
            }

            if (referencedClass != null) {
                switch (fk.getRelationshipType()) {
                    case ONE_TO_MANY:
                        requiredImports.add("OneToMany");
                        requiredImports.add("Set");
                        annotations.append("    @OneToMany(mappedBy = \"")
                                .append(toCamelCase(classDefinition.getClassName()))
                                .append("\")\n")
                                .append("    private Set<").append(referencedClass.getClassName()).append("> ")
                                .append(toCamelCase(referencedClass.getClassName())).append("s;\n\n");
                        break;
                    case ONE_TO_ONE:
                        if (!fk.isOwner()) {
                            requiredImports.add("OneToOne");
                            annotations.append("    @OneToOne(mappedBy = \"")
                                    .append(toCamelCase(classDefinition.getClassName()))
                                    .append("\")\n")
                                    .append("    private ").append(referencedClass.getClassName()).append(" ")
                                    .append(toCamelCase(referencedClass.getClassName())).append(";\n\n");
                        }
                        break;
                }
            }
        }

        return annotations.toString();
    }

    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    @Override
    public String addImports() {
        StringBuilder imports = new StringBuilder();

        // Generar imports dinámicamente
        Map<String, String> importMap = new HashMap<>();
        importMap.put("Entity", "persistence.Entity");
        importMap.put("Table", "persistence.Table");
        importMap.put("Column", "persistence.Column");
        importMap.put("Id", "persistence.Id");
        importMap.put("GeneratedValue", "persistence.GeneratedValue");
        importMap.put("GenerationType", "persistence.GenerationType");
        importMap.put("OneToOne", "persistence.OneToOne");
        importMap.put("OneToMany", "persistence.OneToMany");
        importMap.put("ManyToOne", "persistence.ManyToOne");
        importMap.put("ManyToMany", "persistence.ManyToMany");
        importMap.put("JoinColumn", "persistence.JoinColumn");
        importMap.put("JoinTable", "persistence.JoinTable");
        importMap.put("FetchType", "persistence.FetchType");
        importMap.put("CascadeType", "persistence.CascadeType");
        importMap.put("Set", "java.util.Set");

        for (String annotation : requiredImports) {
            String importPath = importMap.get(annotation);
            if (importPath != null) {
                if (importPath.startsWith("persistence.")) {
                    imports.append(String.format("import %s.%s;\n", importPrefix, importPath));
                } else {
                    imports.append(String.format("import %s;\n", importPath));
                }
            }
        }

        return imports.toString();
    }
}