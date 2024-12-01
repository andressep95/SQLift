package cl.playground.core.strategy;

import cl.playground.core.model.ColumnDefinition;

public class LombokStrategy implements EntityStrategy {
    @Override
    public String addImports() {
        return "import lombok.Getter;\n" +
                "import lombok.Setter;\n" +
                "import lombok.NoArgsConstructor;\n" +
                "import lombok.AllArgsConstructor;\n";
    }

    @Override
    public String addClassAnnotations(String tableName) {
        return "@Getter\n" +
                "@Setter\n" +
                "@NoArgsConstructor\n" +
                "@AllArgsConstructor\n";
    }

    @Override
    public String addFieldAnnotations(ColumnDefinition column, boolean isForeignKey, boolean isPrimaryKey) {
        return ""; // Lombok no necesita anotaciones en campos
    }
}
