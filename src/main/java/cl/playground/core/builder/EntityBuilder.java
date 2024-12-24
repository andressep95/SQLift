package cl.playground.core.builder;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;
import cl.playground.core.strategy.EntityStrategy;

import java.util.List;
import java.util.Map;

public class EntityBuilder {
    private final List<EntityStrategy> strategies;
    private final Map<String, ClassDefinition> allClassDefinitions;
    private ClassDefinition classDefinition;

    public EntityBuilder(List<EntityStrategy> strategies, Map<String, ClassDefinition> allClassDefinitions) {
        this.strategies = strategies;
        this.allClassDefinitions = allClassDefinitions;
    }

    public EntityBuilder withClassDefinition(ClassDefinition classDefinition) {
        this.classDefinition = classDefinition;
        return this;
    }

    public String build() {
        if (classDefinition == null) {
            throw new IllegalStateException("ClassDefinition must be set before building.");
        }

        StringBuilder entity = new StringBuilder();

        // Package
        entity.append("package ").append(classDefinition.getPackageName()).append(";\n\n");

        // Imports from strategies
        for (EntityStrategy strategy : strategies) {
            entity.append(strategy.addImports()).append("\n");
        }

        // Class annotations
        for (EntityStrategy strategy : strategies) {
            entity.append(strategy.addClassAnnotations(classDefinition.getClassName(), classDefinition));
        }

        // Class declaration
        entity.append("public class ").append(classDefinition.getClassName()).append(" {\n\n");

        if (isIntermediateEntity()) {
            buildIntermediateEntity(entity);
        } else {
            buildRegularEntity(entity);
        }

        entity.append("}\n");
        return entity.toString();
    }

    private void addField(StringBuilder classContent, ColumnDefinition column, boolean isForeignKey, boolean isPrimaryKey) {
        for (EntityStrategy strategy : strategies) {
            ForeignKeyDefinition foreignKey = null;
            if (isForeignKey) {
                foreignKey = classDefinition.getForeignKeys().stream()
                    .filter(fk -> fk.getColumnName().equals(column.getColumnName()))
                    .findFirst().orElse(null);
            }
            classContent.append(strategy.addFieldAnnotations(column, foreignKey, isPrimaryKey));
        }

        String javaType = mapDataType(column.getColumnType());
        String fieldName = toCamelCase(column.getColumnName());
        classContent.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
    }

    private String toSingular(String word) {
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private void addManyToOneRelationships(StringBuilder classContent) {
        for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
            // Convertir el nombre de la tabla a singular antes de usarlo como tipo
            String targetEntity = capitalize(toSingular(removeIdSuffix(toCamelCase(fk.getReferenceTableName()))));
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));

            classContent.append("    @ManyToOne\n")
                .append("    @MapsId(\"").append(fieldName).append("Id\")\n")
                .append("    @JoinColumn(\n")
                .append("        name = \"").append(fk.getColumnName()).append("\",\n")
                .append("        foreignKey = @ForeignKey(name = \"fk_")
                .append(toSnakeCase(classDefinition.getClassName())).append("_")
                .append(toSnakeCase(fk.getReferenceTableName())).append("\")\n")
                .append("    )\n")
                .append("    private ").append(targetEntity).append(" ").append(fieldName).append(";\n\n");
        }
    }

    private String generateCompositePrimaryKeyClass() {
        StringBuilder compositeKeyClass = new StringBuilder();
        compositeKeyClass.append("    @Embeddable\n")
            .append("    static class ").append(classDefinition.getClassName()).append("Id {\n");

        // Campos
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String type = mapDataType(pkColumn.getColumnType());
            String fieldName = toCamelCase(pkColumn.getColumnName());
            compositeKeyClass.append("        private ").append(type).append(" ").append(fieldName).append(";\n");
        }
        compositeKeyClass.append("\n");

        // Constructor sin argumentos
        compositeKeyClass.append("        public ").append(classDefinition.getClassName()).append("Id() {}\n\n");

        // Constructor con argumentos
        compositeKeyClass.append("        public ").append(classDefinition.getClassName()).append("Id(");
        boolean first = true;
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            if (!first) compositeKeyClass.append(", ");
            String type = mapDataType(pkColumn.getColumnType());
            String fieldName = toCamelCase(pkColumn.getColumnName());
            compositeKeyClass.append(type).append(" ").append(fieldName);
            first = false;
        }
        compositeKeyClass.append(") {\n");

        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String fieldName = toCamelCase(pkColumn.getColumnName());
            compositeKeyClass.append("            this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }
        compositeKeyClass.append("        }\n\n");

        // Getters y Setters
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String type = mapDataType(pkColumn.getColumnType());
            String fieldName = toCamelCase(pkColumn.getColumnName());
            String capitalizedField = capitalize(fieldName);

            compositeKeyClass.append("        public ").append(type).append(" get").append(capitalizedField).append("() {\n")
                .append("            return ").append(fieldName).append(";\n        }\n\n")
                .append("        public void set").append(capitalizedField).append("(").append(type)
                .append(" ").append(fieldName).append(") {\n")
                .append("            this.").append(fieldName).append(" = ").append(fieldName).append(";\n        }\n\n");
        }

        // Agregar equals y hashCode
        compositeKeyClass.append("        @Override\n")
            .append("        public boolean equals(Object o) {\n")
            .append("            if (this == o) return true;\n")
            .append("            if (o == null || getClass() != o.getClass()) return false;\n")
            .append("            ").append(classDefinition.getClassName()).append("Id that = (")
            .append(classDefinition.getClassName()).append("Id) o;\n");

        // Construir la comparación de equals
        first = true;
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            String fieldName = toCamelCase(pkColumn.getColumnName());
            if (first) {
                compositeKeyClass.append("            return ").append(fieldName).append(".equals(that.")
                    .append(fieldName).append(")");
                first = false;
            } else {
                compositeKeyClass.append("\n                && ").append(fieldName).append(".equals(that.")
                    .append(fieldName).append(")");
            }
        }
        compositeKeyClass.append(";\n        }\n\n");

        // Agregar hashCode
        compositeKeyClass.append("        @Override\n")
            .append("        public int hashCode() {\n")
            .append("            return java.util.Objects.hash(");

        first = true;
        for (ColumnDefinition pkColumn : classDefinition.getPrimaryKeyColumns()) {
            if (!first) compositeKeyClass.append(", ");
            compositeKeyClass.append(toCamelCase(pkColumn.getColumnName()));
            first = false;
        }
        compositeKeyClass.append(");\n        }\n");

        compositeKeyClass.append("    }\n");
        return compositeKeyClass.toString();
    }

    private String mapDataType(String sqlType) {
        return switch (sqlType.toUpperCase()) {
            case "SERIAL", "BIGINT" -> "Long";
            case "VARCHAR" -> "String";
            case "INTEGER", "INT" -> "Integer";
            case "NUMERIC" -> "java.math.BigDecimal";
            case "DATE" -> "java.time.LocalDate";
            default -> "Object";
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    private String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String removeIdSuffix(String name) {
        return name.replaceAll("(?i)_?id$", "");
    }

    private void addDefaultConstructor(StringBuilder classContent) {
        classContent.append("    public ").append(classDefinition.getClassName()).append("() {\n    }\n\n");
    }

    private void addAllArgsConstructor(StringBuilder classContent) {
        List<ForeignKeyDefinition> foreignKeys = classDefinition.getForeignKeys();

        classContent.append("    public ").append(classDefinition.getClassName()).append("(");

        // Parámetros para entidades referenciadas y campos regulares
        boolean first = true;

        for (ForeignKeyDefinition fk : foreignKeys) {
            if (!first) classContent.append(", ");
            String entityType = capitalize(toSingular(removeIdSuffix(toCamelCase(fk.getReferenceTableName()))));
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            classContent.append(entityType).append(" ").append(fieldName);
            first = false;
        }

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = foreignKeys.stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                if (!first) classContent.append(", ");
                classContent.append(mapDataType(column.getColumnType())).append(" ")
                    .append(toCamelCase(column.getColumnName()));
                first = false;
            }
        }

        classContent.append(") {\n");

        // Inicialización del ID compuesto
        if (!foreignKeys.isEmpty()) {
            classContent.append("        this.id = new ").append(classDefinition.getClassName())
                .append("Id(");

            first = true;
            for (ForeignKeyDefinition fk : foreignKeys) {
                if (!first) classContent.append(", ");
                String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
                classContent.append(fieldName).append(".getId()");
                first = false;
            }
            classContent.append(");\n");
        }

        // Asignación de campos
        for (ForeignKeyDefinition fk : foreignKeys) {
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            classContent.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = foreignKeys.stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                String fieldName = toCamelCase(column.getColumnName());
                classContent.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            }
        }

        classContent.append("    }\n\n");
    }

    private void addGettersAndSetters(StringBuilder classContent) {
        // Getter y Setter para el ID
        classContent.append("    public ").append(classDefinition.getClassName()).append("Id getId() {\n")
            .append("        return id;\n    }\n\n")
            .append("    public void setId(").append(classDefinition.getClassName()).append("Id id) {\n")
            .append("        this.id = id;\n    }\n\n");

        // Getters y Setters para relaciones
        for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
            String targetEntity = capitalize(toSingular(removeIdSuffix(toCamelCase(fk.getReferenceTableName()))));
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            String capitalizedField = capitalize(fieldName);

            // Getter
            classContent.append("    public ").append(targetEntity).append(" get").append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n    }\n\n");

            // Setter con lógica de ID
            classContent.append("    public void set").append(capitalizedField).append("(")
                .append(targetEntity).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                .append("        if (this.id == null) {\n")
                .append("            this.id = new ").append(classDefinition.getClassName()).append("Id();\n")
                .append("        }\n")
                .append("        this.id.set").append(capitalizedField).append("Id(")
                .append(fieldName).append(".getId());\n")
                .append("    }\n\n");
        }

        // Getters y Setters para campos regulares
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = classDefinition.getForeignKeys().stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                String type = mapDataType(column.getColumnType());
                String fieldName = toCamelCase(column.getColumnName());
                String capitalizedField = capitalize(fieldName);

                classContent.append("    public ").append(type).append(" get").append(capitalizedField).append("() {\n")
                    .append("        return ").append(fieldName).append(";\n    }\n\n")
                    .append("    public void set").append(capitalizedField).append("(").append(type)
                    .append(" ").append(fieldName).append(") {\n")
                    .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
            }
        }
    }

    private boolean isIntermediateEntity() {
        return classDefinition.getPrimaryKeyColumns().size() > 1 && !classDefinition.getForeignKeys().isEmpty();
    }

    private void buildIntermediateEntity(StringBuilder entity) {
        // ID embebido
        entity.append("    @EmbeddedId\n")
            .append("    private ").append(classDefinition.getClassName()).append("Id id;\n\n");

        // Relaciones ManyToOne
        addManyToOneRelationships(entity);

        // Campos regulares
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = classDefinition.getForeignKeys().stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                addIntermediateEntityField(entity, column);
            }
        }

        // Constructores
        addIntermediateEntityConstructors(entity);

        // Getters y setters
        addIntermediateEntityGettersAndSetters(entity);

        // Clase ID embebida
        entity.append(generateCompositePrimaryKeyClass());
    }

    private void buildRegularEntity(StringBuilder entity) {
        // Generar clave primaria
        if (!classDefinition.getPrimaryKeyColumns().isEmpty()) {
            ColumnDefinition primaryKey = classDefinition.getPrimaryKeyColumns().get(0);
            addRegularEntityPrimaryKey(entity, primaryKey);
        }

        // Generar campos normales
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            addRegularEntityField(entity, column);
        }

        // Agregar relaciones inversas
        addInverseRelationships(entity);

        // Constructores y métodos
        addRegularEntityConstructors(entity);
        addRegularEntityGettersAndSetters(entity);
    }


    private void addInverseRelationships(StringBuilder entity) {
        String currentClassName = classDefinition.getClassName().toLowerCase();

        // Identificar clases que referencian a esta clase
        for (ClassDefinition otherClass : allClassDefinitions.values()) {
            if (otherClass.getClassName().equals(classDefinition.getClassName())) {
                continue; // No agregar relación a sí misma
            }

            boolean hasRelationToThis = otherClass.getForeignKeys().stream()
                .anyMatch(fk -> fk.getReferenceTableName().equals(toSnakeCase(classDefinition.getClassName())));

            if (hasRelationToThis) {
                String otherClassName = otherClass.getClassName();
                String fieldName = toCamelCase(toPlural(otherClassName.toLowerCase()));

                // Generar campo con @OneToMany
                entity.append("    @OneToMany(\n")
                    .append("        mappedBy = \"").append(toCamelCase(currentClassName)).append("\",\n")
                    .append("        cascade = CascadeType.ALL,\n")
                    .append("        orphanRemoval = true\n")
                    .append("    )\n")
                    .append("    private Set<").append(otherClassName).append("> ")
                    .append(fieldName).append(";\n\n");
            }
        }
    }

    private String toPlural(String singular) {
        if (singular.endsWith("s")) {
            return singular + "es";
        } else if (singular.endsWith("y")) {
            return singular.substring(0, singular.length() - 1) + "ies";
        } else {
            return singular + "s";
        }
    }

    private void addRegularEntityField(StringBuilder entity, ColumnDefinition column) {
        entity.append("    @Column(\n")
            .append("        name = \"").append(column.getColumnName()).append("\"");

        if (!column.isNullable()) {
            entity.append(",\n        nullable = false");
        }

        entity.append("\n    )\n");
        entity.append("    private ")
            .append(mapDataType(column.getColumnType())).append(" ")
            .append(toCamelCase(column.getColumnName())).append(";\n\n");
    }

    private void addRegularEntityPrimaryKey(StringBuilder entity, ColumnDefinition pk) {
        entity.append("    @Id\n")
            .append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n")
            .append("    @Column(name = \"").append(pk.getColumnName()).append("\")\n")
            .append("    private ").append(mapDataType(pk.getColumnType())).append(" id;\n\n");
    }

    private void addIntermediateEntityField(StringBuilder entity, ColumnDefinition column) {
        entity.append("    @Column(\n")
            .append("        name = \"").append(column.getColumnName()).append("\"");

        if (!column.isNullable()) {
            entity.append(",\n        nullable = false");
        }
        if (column.getDefaultValue() != null) {
            entity.append(",\n        columnDefinition = \"")
                .append(column.getColumnType())
                .append(" DEFAULT ")
                .append(column.getDefaultValue())
                .append("\"");
        }

        entity.append("\n    )\n");
        entity.append("    private ")
            .append(mapDataType(column.getColumnType())).append(" ")
            .append(toCamelCase(column.getColumnName())).append(";\n\n");
    }

    private void addIntermediateEntityConstructors(StringBuilder entity) {
        // Constructor sin argumentos
        entity.append("    public ").append(classDefinition.getClassName()).append("() {}\n\n");

        // Constructor con argumentos
        List<ForeignKeyDefinition> foreignKeys = classDefinition.getForeignKeys();
        entity.append("    public ").append(classDefinition.getClassName()).append("(");

        boolean first = true;
        // Primero las entidades
        for (ForeignKeyDefinition fk : foreignKeys) {
            if (!first) entity.append(", ");
            String entityType = capitalize(toSingular(removeIdSuffix(toCamelCase(fk.getReferenceTableName()))));
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            entity.append(entityType).append(" ").append(fieldName);
            first = false;
        }

        // Luego los campos regulares
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = foreignKeys.stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                if (!first) entity.append(", ");
                entity.append(mapDataType(column.getColumnType())).append(" ")
                    .append(toCamelCase(column.getColumnName()));
                first = false;
            }
        }

        entity.append(") {\n");

        // Inicializar ID compuesto
        entity.append("        this.id = new ").append(classDefinition.getClassName())
            .append("Id(");
        first = true;
        for (ForeignKeyDefinition fk : foreignKeys) {
            if (!first) entity.append(", ");
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            entity.append(fieldName).append(".getId()");
            first = false;
        }
        entity.append(");\n");

        // Asignar campos
        for (ForeignKeyDefinition fk : foreignKeys) {
            String fieldName = toCamelCase(removeIdSuffix(fk.getColumnName()));
            entity.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            boolean isFK = foreignKeys.stream()
                .anyMatch(fk -> fk.getColumnName().equals(column.getColumnName()));
            if (!isFK) {
                String fieldName = toCamelCase(column.getColumnName());
                entity.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            }
        }

        entity.append("    }\n\n");
    }

    private void addRegularEntityConstructors(StringBuilder entity) {
        // Constructor sin argumentos
        entity.append("    public ").append(classDefinition.getClassName()).append("() {}\n\n");

        // Constructor con argumentos (sin ID)
        entity.append("    public ").append(classDefinition.getClassName()).append("(");

        boolean first = true;
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            if (!first) entity.append(", ");
            entity.append(mapDataType(column.getColumnType()))
                .append(" ").append(toCamelCase(column.getColumnName()));
            first = false;
        }

        entity.append(") {\n");

        for (ColumnDefinition column : classDefinition.getAttributes()) {
            String fieldName = toCamelCase(column.getColumnName());
            entity.append("        this.").append(fieldName)
                .append(" = ").append(fieldName).append(";\n");
        }

        entity.append("    }\n\n");
    }

    private void addIntermediateEntityGettersAndSetters(StringBuilder entity) {
        addGettersAndSetters(entity);
    }

    private void addRegularEntityGettersAndSetters(StringBuilder entity) {
        // Getter y Setter para ID
        entity.append("    public Long getId() {\n")
            .append("        return id;\n")
            .append("    }\n\n")
            .append("    public void setId(Long id) {\n")
            .append("        this.id = id;\n")
            .append("    }\n\n");

        // Para los demás campos
        for (ColumnDefinition column : classDefinition.getAttributes()) {
            String type = mapDataType(column.getColumnType());
            String fieldName = toCamelCase(column.getColumnName());
            String capitalizedField = capitalize(fieldName);

            entity.append("    public ").append(type).append(" get").append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n")
                .append("    }\n\n")
                .append("    public void set").append(capitalizedField).append("(")
                .append(type).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                .append("    }\n\n");
        }

        // En el método addRegularEntityGettersAndSetters
        allClassDefinitions.values().stream()
            .filter(otherClass -> !otherClass.getClassName().equals(classDefinition.getClassName()))
            .filter(otherClass -> otherClass.getForeignKeys().stream()
                .anyMatch(fk -> fk.getReferenceTableName().equals(toSnakeCase(classDefinition.getClassName()))))
            .forEach(otherClass -> {
                String otherClassName = otherClass.getClassName();
                String fieldName = toCamelCase(toPlural(otherClassName.toLowerCase()));
                String capitalizedField = capitalize(fieldName);

                entity.append("    public Set<").append(otherClassName).append("> get")
                    .append(capitalizedField).append("() {\n")
                    .append("        return ").append(fieldName).append(";\n")
                    .append("    }\n\n")
                    .append("    public void set").append(capitalizedField).append("(Set<")
                    .append(otherClassName).append("> ").append(fieldName).append(") {\n")
                    .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                    .append("    }\n\n");
            });
    }
}
