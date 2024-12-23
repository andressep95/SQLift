package cl.playground.core.strategy;

import cl.playground.core.model.ClassDefinition;
import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.ForeignKeyDefinition;

public class LombokStrategy implements EntityStrategy {
    @Override
    public String addImports() {
        return "import lombok.Getter;\n" +
                "import lombok.Setter;\n" +
                "import lombok.NoArgsConstructor;\n" +
                "import lombok.AllArgsConstructor;\n";
    }

    @Override
    public String addClassAnnotations(String tableName, ClassDefinition classDefinition) {
        return "@Getter\n" +
            "@Setter\n" +
            "@NoArgsConstructor\n" +
            "@AllArgsConstructor\n";
    }


    @Override
    public String addFieldAnnotations(ColumnDefinition column, ForeignKeyDefinition isForeignKey, boolean isPrimaryKey) {
        return ""; // Lombok no necesita anotaciones en campos
    }
}
