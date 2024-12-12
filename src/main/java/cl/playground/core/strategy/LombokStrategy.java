package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;
import cl.playground.core.model.TableType;

import java.util.HashSet;
import java.util.Set;

public class LombokStrategy implements EntityStrategy {
    private Set<String> requiredImports;

    public LombokStrategy() {
        this.requiredImports = new HashSet<>();
    }

    @Override
    public String addClassAnnotations(String tableName, TableType tableType) {
        requiredImports.add("Getter");
        requiredImports.add("Setter");
        requiredImports.add("NoArgsConstructor");
        requiredImports.add("AllArgsConstructor");

        return "@Getter\n" +
                "@Setter\n" +
                "@NoArgsConstructor\n" +
                "@AllArgsConstructor\n";
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition foreignKey,
                                      boolean isPrimaryKey, TableType tableType) {
        return ""; // Lombok no necesita anotaciones en campos
    }

    @Override
    public String addRelationalAnnotations(ClassDefinition classDefinition, TableType tableType) {
        return ""; // Lombok no necesita anotaciones relacionales
    }

    @Override
    public String addImports() {
        StringBuilder imports = new StringBuilder();

        for (String annotation : requiredImports) {
            imports.append(String.format("import lombok.%s;\n", annotation));
        }

        return imports.toString();
    }
}