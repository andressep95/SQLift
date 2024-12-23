# Contexto para SQLift

SQLift es una herramienta de CLI escrita en Java la cual solo contiene la dependencia de PicoCLI para generar su entorno
Se enfoca en leer archivos SQL que sean de postgres que tengan sentencias DDL de CREATE TABLE y demas para que en base a ellos
poder crear las entidades en Java necesarias para poder emular esas tablas cuando se active el DDL en proyectos de 
Spring Data JPA, cuenta con la opcion de añadir Lombok a la ecuacion para tener clases mas cortas y efectivas.

Los archivos de la primera etapa de este proyecto de lectura de datos y demas son los siguientes:

```java
@Command(
        name = "generate",
        description = "Generate Java entity classes from SQL schema"
)
public class GenerateCommand implements Runnable {

    private static final String CONFIG_FILENAME = "sqlift.yaml";

    @Override
    public void run() {
        try {
            // Obtener directorio actual y archivo de configuración
            String currentDir = System.getProperty("user.dir");
            File yamlFile = new File(currentDir, CONFIG_FILENAME);

            if (!yamlFile.exists()) {
                System.out.println("Configuration file not found in directory: " + currentDir);
                return;
            }

            // Leer configuración y almacenar en el contexto
            Map<String, Object> context = extractConfigContext(yamlFile.getPath());

            // Mostrar la configuración leída
            LogContent.logConfiguration(context);

            // Leer y mostrar el contenido SQL
            String sqlContent = SqlReader.readSql((String) context.get("schema"));
            LogContent.logSqlContent(sqlContent);

            // Agregar el contenido SQL al contexto
            context.put("sqlContent", sqlContent);

            // Generar las entidades
            System.out.println("\n🔨 Generating entities...");
            EntityGenerator generator = new EntityGenerator();
            generator.generateEntities(context);
            System.out.println("✅ Entities generated successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new ConfigurationException("Failed to read configuration", e);
        }
    }

    private Map<String, Object> extractConfigContext(String configPath) {
        Map<String, Object> context = new HashMap<>();

        // Leer configuración
        SqliftConfig config = YamlConfigReader.readConfig(configPath);

        // Validar configuración requerida
        if (config == null || config.getSql() == null || config.getSql().getOutput() == null) {
            throw new ConfigurationException("Invalid YAML: Missing required fields.");
        }

        SqliftConfig.OutputConfig outputConfig = config.getSql().getOutput();
        SqliftConfig.Options options = outputConfig.getOptions();

        // Almacenar datos relevantes
        context.put("config", config);
        context.put("engine", config.getSql().getEngine());
        context.put("schema", config.getSql().getSchema());
        context.put("outputPackage", outputConfig.getPackageName());
        context.put("useLombok", options != null && options.isLombok());
        context.put("useJpa", options != null && options.isJpa());

        System.out.println("✔ YAML configuration loaded successfully.");
        System.out.println(config.toString());

        return context;
    }
}
```

```java
public class SqliftConfig {
    private String version;
    private SqlConfig sql;

    // Constructor por defecto necesario para el parser YAML
    public SqliftConfig() {
    }

    // Getters y Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SqlConfig getSql() {
        return sql;
    }

    public void setSql(SqlConfig sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return "SqliftConfig{" +
                "version='" + version + '\'' +
                ", sql=" + sql +
                '}';
    }

    // Clase interna para la configuración SQL
    public static class SqlConfig {
        private String engine;
        private String schema;
        private OutputConfig output;

        public SqlConfig() {
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public OutputConfig getOutput() {
            return output;
        }

        public void setOutput(OutputConfig output) {
            this.output = output;
        }

        @Override
        public String toString() {
            return "SqlConfig{" +
                    "engine='" + engine + '\'' +
                    ", schema='" + schema + '\'' +
                    ", output=" + output +
                    '}';
        }
    }

    // Clase interna para la configuración de salida
    public static class OutputConfig {
        private String packageName;
        private Options options;

        public OutputConfig() {
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public Options getOptions() {
            return options;
        }

        public void setOptions(Options options) {
            this.options = options;
        }

        @Override
        public String toString() {
            return "OutputConfig{" +
                    "packageName='" + packageName + '\'' +
                    ", options=" + options +
                    '}';
        }
    }

    // Clase interna para las opciones
    public static class Options {
        private boolean lombok;
        private boolean jpa;

        public Options() {
        }

        // getters y setters
        public boolean isLombok() {
            return lombok;
        }

        public void setLombok(boolean lombok) {
            this.lombok = lombok;
        }

        public boolean isJpa() {
            return jpa;
        }

        public void setJpa(boolean jpa) {
            this.jpa = jpa;
        }

        @Override
        public String toString() {
            return "Options{" +
                    "lombok=" + lombok +
                    ", jpa=" + jpa +
                    '}';
        }
    }
}
```

```java
public class YamlParser {
    private static final String INDENT = "  ";

    public SqliftConfig parse(String filePath) throws IOException {
        SqliftConfig config = new SqliftConfig();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isSkippable(line)) {
                    continue;
                }

                if (line.startsWith("version:")) {
                    config.setVersion(extractValue(line));
                } else if (line.startsWith("sql:")) {
                    SqliftConfig.SqlConfig sqlConfig = new SqliftConfig.SqlConfig();
                    config.setSql(sqlConfig);
                    parseSqlConfig(br, sqlConfig);
                }
            }
        }

        validateConfig(config);
        return config;
    }

    private void parseSqlConfig(BufferedReader br, SqliftConfig.SqlConfig sqlConfig) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("engine:")) {
                sqlConfig.setEngine(extractValue(line));
            } else if (line.startsWith("schema:")) {
                sqlConfig.setSchema(extractValue(line));
            } else if (line.startsWith("output:")) {
                SqliftConfig.OutputConfig outputConfig = new SqliftConfig.OutputConfig();
                sqlConfig.setOutput(outputConfig);
                parseOutputConfig(br, outputConfig);
            }
        }
    }

    private void parseOutputConfig(BufferedReader br, SqliftConfig.OutputConfig outputConfig) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT + INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("package:")) {
                outputConfig.setPackageName(extractValue(line));
            } else if (line.startsWith("options:")) {
                SqliftConfig.Options options = new SqliftConfig.Options();
                outputConfig.setOptions(options);
                parseOptions(br, options);
            }
        }
    }

    private void parseOptions(BufferedReader br, SqliftConfig.Options options) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(INDENT + INDENT + INDENT)) {
                break;
            }
            line = line.trim();
            if (isSkippable(line)) {
                continue;
            }

            if (line.startsWith("lombok:")) {
                options.setLombok(Boolean.parseBoolean(extractValue(line)));
            } else if (line.startsWith("jpa:")) {
                options.setJpa(Boolean.parseBoolean(extractValue(line)));
            }
        }
    }

    private String extractValue(String line) {
        String[] parts = line.split(":", 2);
        return parts.length > 1 ? parts[1].trim().replace("\"", "") : "";
    }

    private boolean isSkippable(String line) {
        return line.isEmpty() || line.startsWith("#");
    }

    private void validateConfig(SqliftConfig config) {
        checkNotNullOrEmpty(config.getVersion(), "Version is required");
        SqliftConfig.SqlConfig sql = config.getSql();
        if (sql == null) {
            throw new ConfigurationException("SQL configuration is required");
        }
        checkNotNullOrEmpty(sql.getEngine(), "Database engine is required");
        checkNotNullOrEmpty(sql.getSchema(), "Schema file path is required");

        SqliftConfig.OutputConfig output = sql.getOutput();
        if (output == null || output.getPackageName() == null || output.getPackageName().trim().isEmpty()) {
            throw new ConfigurationException("Output package configuration is required");
        }
    }

    private void checkNotNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ConfigurationException(errorMessage);
        }
    }
}
```

```java
public class YamlConfigReader {

    public static SqliftConfig readConfig(String configFilePath) throws ConfigurationException {
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            throw new ConfigurationException("Configuration file not found: " + configFilePath);
        }

        if (!configFile.isFile()) {
            throw new ConfigurationException("Path is not a file: " + configFilePath);
        }

        try {
            YamlParser parser = new YamlParser();
            return parser.parse(configFilePath);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file", e);
        }
    }
}
```

```java
public class ClassDefinition {

    private final String className;
    private final String packageName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> attributes;
    private final List<ForeignKeyDefinition> foreignKeys;
    private final Set<String> imports;

    public ClassDefinition(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
        this.attributes = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.imports = new HashSet<>();
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public ColumnDefinition getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ColumnDefinition primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void addAttribute(ColumnDefinition column) {
        this.attributes.add(column);
    }

    public void addForeignKey(ForeignKeyDefinition foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    public void addImport(String importStatement) {
        this.imports.add(importStatement);
    }

    public List<ColumnDefinition> getAttributes() {
        return attributes;
    }

    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }

    public Set<String> getImports() {
        return imports;
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
            "className='" + className + '\'' +
            ", packageName='" + packageName + '\'' +
            ", primaryKey=" + primaryKey +
            ", attributes=" + attributes +
            ", foreignKeys=" + foreignKeys +
            ", imports=" + imports +
            '}';
    }
}
```

```java
public class ColumnDefinition {
    private final String columnName;
    private final String columnType;
    private final boolean isNullable;
    private final String defaultValue;
    private final boolean isUnique;
    private final String length;
    private boolean isForeignKey;

    public ColumnDefinition(String columnName, String columnType, boolean isNullable,
                            String defaultValue, boolean isUnique, String length) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.isUnique = isUnique;
        this.length = length;
        this.isForeignKey = false; // Por defecto no es clave foránea
    }

    // Constructor sobrecargado que incluye isForeignKey
    public ColumnDefinition(String columnName, String columnType, boolean isNullable,
                            String defaultValue, boolean isUnique, String length,
                            boolean isForeignKey) {
        this(columnName, columnType, isNullable, defaultValue, isUnique, length);
        this.isForeignKey = isForeignKey;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public String getLength() {
        return length;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.isForeignKey = foreignKey;
    }

    @Override
    public String toString() {
        return "ColumnDefinition{" +
            "columnName='" + columnName + '\'' +
            ", columnType='" + columnType + '\'' +
            ", isNullable=" + isNullable +
            ", defaultValue='" + defaultValue + '\'' +
            ", isUnique=" + isUnique +
            ", length='" + length + '\'' +
            ", isForeignKey=" + isForeignKey +
            '}';
    }
}
```

```java
public class ForeignKeyDefinition {
    private final String columnName;
    private final String referenceTableName;
    private final String referenceColumnName;
    private FetchStrategy fetchStrategy = FetchStrategy.LAZY; // Por defecto LAZY
    private List<CascadeStrategy> cascadeStrategies = new ArrayList<>();
    private RelationshipType relationshipType = RelationshipType.MANY_TO_ONE; // Por defecto

    public enum FetchStrategy {
        LAZY,
        EAGER
    }

    public enum CascadeStrategy {
        ALL,
        PERSIST,
        MERGE,
        REMOVE,
        REFRESH,
        DETACH
    }

    public enum RelationshipType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }

    // Constructor base (el actual)
    public ForeignKeyDefinition(String columnName, String referenceTableName, String referenceColumnName) {
        this.columnName = columnName;
        this.referenceTableName = referenceTableName;
        this.referenceColumnName = referenceColumnName;
    }

    // Constructor completo
    public ForeignKeyDefinition(String columnName, String referenceTableName,
                                String referenceColumnName, FetchStrategy fetchStrategy,
                                List<CascadeStrategy> cascadeStrategies,
                                RelationshipType relationshipType) {
        this(columnName, referenceTableName, referenceColumnName);
        this.fetchStrategy = fetchStrategy;
        this.cascadeStrategies = cascadeStrategies;
        this.relationshipType = relationshipType;
    }

    // Getters
    public String getColumnName() {
        return columnName;
    }

    public String getReferenceTableName() {
        return referenceTableName;
    }

    public String getReferenceColumnName() {
        return referenceColumnName;
    }

    public FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public List<CascadeStrategy> getCascadeStrategies() {
        return cascadeStrategies;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    @Override
    public String toString() {
        return "ForeignKeyDefinition{" +
            "columnName='" + columnName + '\'' +
            ", referenceTableName='" + referenceTableName + '\'' +
            ", referenceColumnName='" + referenceColumnName + '\'' +
            ", fetchStrategy=" + fetchStrategy +
            ", cascadeStrategies=" + cascadeStrategies +
            ", relationshipType=" + relationshipType +
            '}';
    }
}
```

```java
public class TableDefinition {

    private final String tableName;
    private ColumnDefinition primaryKey;
    private final List<ColumnDefinition> columns;
    private final List<ForeignKeyDefinition> foreignKeys;


    public TableDefinition(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnDefinition getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ColumnDefinition primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }

    public void addColumn(ColumnDefinition column) {
        this.columns.add(column);
    }

    public void addForeignKey(ForeignKeyDefinition foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    @Override
    public String toString() {
        return "TableDefinition{" +
            "tableName='" + tableName + '\'' +
            ", primaryKey=" + primaryKey +
            ", columns=" + columns +
            ", foreignKeys=" + foreignKeys +
            '}';
    }
}
```

```java
public interface DatabaseEngine {
    Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage);
    String mapDataType(String sqlType, String columnName, boolean isForeignKey);
}
```

```java
public class PostgresEngine implements DatabaseEngine {
    private static final Pattern TABLE_PATTERN = Pattern.compile(
        "CREATE TABLE\\s+(\\w+)\\s*\\(([^;]+)\\);",
        Pattern.CASE_INSENSITIVE
                                                                );
    private static final Pattern COLUMN_PATTERN = Pattern.compile(
        "(\\w+)\\s+(\\w+)(?:\\(([^)]+)\\))?(\\s+[^,]+)?",
        Pattern.CASE_INSENSITIVE
                                                                 );
    private static final Pattern FOREIGN_KEY_PATTERN = Pattern.compile(
        "REFERENCES\\s+(\\w+)\\s*\\((\\w+)\\)",
        Pattern.CASE_INSENSITIVE
                                                                      );

    @Override
    public Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage) {
        Map<String, ClassDefinition> classMap = new HashMap<>();
        Map<String, TableDefinition> tableMap = new HashMap<>();
        Matcher tableMatcher = TABLE_PATTERN.matcher(sqlContent);

        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).toLowerCase();
            String columnsDefinition = tableMatcher.group(2);

            System.out.println("Procesando tabla: " + tableName);
            TableDefinition tableDefinition = parseTableDefinition(tableName, columnsDefinition);
            tableMap.put(tableName, tableDefinition);

            String className = TypeMapper.toPascalCase(tableName);
            ClassDefinition classDefinition = new ClassDefinition(className, basePackage);

            // Establecer primary key
            if (tableDefinition.getPrimaryKey() != null) {
                System.out.println("Clave primaria detectada para la tabla " + tableName + ": " + tableDefinition.getPrimaryKey());
                classDefinition.setPrimaryKey(tableDefinition.getPrimaryKey());
            }

            // Agregar columnas regulares
            for (ColumnDefinition column : tableDefinition.getColumns()) {
                if (!column.equals(tableDefinition.getPrimaryKey())) {
                    System.out.println("Agregando atributo: " + column);
                    classDefinition.addAttribute(column);
                }
            }

            // Agregar foreign keys
            for (ForeignKeyDefinition fk : tableDefinition.getForeignKeys()) {
                System.out.println("Agregando clave foránea: " + fk);
                classDefinition.addForeignKey(fk);
            }

            classMap.put(tableName, classDefinition);
        }

        processRelationships(classMap);
        System.out.println("Clases generadas: " + classMap);
        return classMap;
    }

    public String mapDataType(String sqlType, String columnName, boolean isForeignKey) {
        String mappedType = TypeMapper.mapPostgresType(sqlType, columnName, isForeignKey);
        System.out.println("Mapeando tipo SQL: " + sqlType + " a tipo Java: " + mappedType);
        return mappedType;
    }

    private TableDefinition parseTableDefinition(String tableName, String columnsDefinition) {
        TableDefinition tableDefinition = new TableDefinition(tableName);
        Matcher columnMatcher = COLUMN_PATTERN.matcher(columnsDefinition);

        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String columnType = columnMatcher.group(2);
            String size = columnMatcher.group(3);
            String constraints = columnMatcher.group(4);

            System.out.println("Procesando columna: " + columnName);
            boolean isNullable = constraints == null || !constraints.toLowerCase().contains("not null");
            boolean isUnique = constraints != null && constraints.toLowerCase().contains("unique");
            String defaultValue = extractDefaultValue(constraints);
            String length = columnType.toLowerCase().contains("varchar") ? size : null;

            // Verificar si es foreign key primero
            if (constraints != null) {
                Matcher fkMatcher = FOREIGN_KEY_PATTERN.matcher(constraints);
                if (fkMatcher.find()) {
                    String referencedTableName = fkMatcher.group(1).toLowerCase();
                    String referencedColumnName = fkMatcher.group(2);
                    System.out.println("Clave foránea detectada en columna: " + columnName + ", referencia a: " + referencedTableName + "(" + referencedColumnName + ")");

                    ColumnDefinition column = new ColumnDefinition(
                        columnName,
                        referencedTableName,
                        isNullable,
                        defaultValue,
                        isUnique,
                        length,
                        true
                    );
                    tableDefinition.addColumn(column);

                    ForeignKeyDefinition fk = new ForeignKeyDefinition(
                        columnName,
                        referencedTableName,
                        referencedColumnName
                    );
                    tableDefinition.addForeignKey(fk);
                    continue;
                }
            }

            // Para columnas normales
            ColumnDefinition column = new ColumnDefinition(
                columnName,
                columnType,
                isNullable,
                defaultValue,
                isUnique,
                length
            );

            if (isPrimaryKey(constraints)) {
                System.out.println("Columna detectada como clave primaria: " + column);
                tableDefinition.setPrimaryKey(column);
            } else {
                tableDefinition.addColumn(column);
            }
        }

        return tableDefinition;
    }

    private void processRelationships(Map<String, ClassDefinition> classMap) {
        for (ClassDefinition classDefinition : classMap.values()) {
            Map<String, ColumnDefinition> columnUpdates = new HashMap<>();

            for (ForeignKeyDefinition fk : classDefinition.getForeignKeys()) {
                String referencedTableName = fk.getReferenceTableName();
                ClassDefinition referencedClass = classMap.get(referencedTableName);

                if (referencedClass != null) {
                    String originalColumnName = fk.getColumnName();
                    String newFieldName = originalColumnName.replace("_id", "");

                    System.out.println("Procesando relación: " + classDefinition.getClassName() + " -> " + referencedClass.getClassName());

                    ColumnDefinition relationColumn = new ColumnDefinition(
                        originalColumnName,
                        referencedClass.getClassName(),
                        true,
                        null,
                        false,
                        null,
                        true
                    );

                    columnUpdates.put(originalColumnName, relationColumn);
                }
            }

            for (Map.Entry<String, ColumnDefinition> update : columnUpdates.entrySet()) {
                classDefinition.getAttributes().removeIf(attr ->
                        attr.getColumnName().equals(update.getKey())
                                                        );
                classDefinition.addAttribute(update.getValue());
            }
        }
    }

    private boolean isPrimaryKey(String constraints) {
        return constraints != null &&
            (constraints.toLowerCase().contains("primary key") ||
                constraints.toLowerCase().contains("serial primary"));
    }

    private String extractDefaultValue(String constraints) {
        if (constraints == null) return null;
        Pattern defaultPattern = Pattern.compile("DEFAULT\\s+([^\\s,]+)", Pattern.CASE_INSENSITIVE);
        Matcher defaultMatcher = defaultPattern.matcher(constraints);
        String defaultValue = defaultMatcher.find() ? defaultMatcher.group(1) : null;
        System.out.println("Valor por defecto extraído: " + defaultValue);
        return defaultValue;
    }
}
```

```java
public class TypeMapper {
    private static final List<String> IGNORED_SQL_TYPES = Arrays.asList(
            "key", "primary", "foreign", "null", "default", "constraint",
            "references", "check", "unique", "current_timestamp"
    );

    private static final Map<String, String> POSTGRES_TYPE_MAP = new HashMap<>();

    static {
        // Tipos numéricos
        POSTGRES_TYPE_MAP.put("serial", "Integer");
        POSTGRES_TYPE_MAP.put("bigserial", "Long");
        POSTGRES_TYPE_MAP.put("int", "Integer");
        POSTGRES_TYPE_MAP.put("integer", "Integer");
        POSTGRES_TYPE_MAP.put("bigint", "Long");
        POSTGRES_TYPE_MAP.put("numeric", "java.math.BigDecimal");
        POSTGRES_TYPE_MAP.put("decimal", "java.math.BigDecimal");
        POSTGRES_TYPE_MAP.put("real", "Float");
        POSTGRES_TYPE_MAP.put("double precision", "Double");

        // Tipos de texto
        POSTGRES_TYPE_MAP.put("varchar", "String");
        POSTGRES_TYPE_MAP.put("text", "String");
        POSTGRES_TYPE_MAP.put("char", "String");

        // Tipos de fecha/hora
        POSTGRES_TYPE_MAP.put("timestamp", "java.time.LocalDateTime");
        POSTGRES_TYPE_MAP.put("date", "java.time.LocalDate");
        POSTGRES_TYPE_MAP.put("time", "java.time.LocalTime");

        // Tipos booleanos
        POSTGRES_TYPE_MAP.put("boolean", "Boolean");
        POSTGRES_TYPE_MAP.put("bool", "Boolean");
    }

    public static String mapPostgresType(String sqlType, String columnName, boolean isForeignKey) {
        if (isIgnoredSqlType(sqlType)) {
            return null;
        }

        // Si es una foreign key, usar el nombre de la entidad
        if (isForeignKey) {
            String entityName = columnName.replace("_id", "");
            return toPascalCase(entityName);
        }

        // Para tipos normales
        String baseType = sqlType.toLowerCase().split("\\s+")[0];
        String javaType = POSTGRES_TYPE_MAP.get(baseType);

        if (javaType == null) {
            // Verificar si es una referencia a clase (empieza con mayúscula)
            if (Character.isUpperCase(sqlType.charAt(0))) {
                return sqlType;
            }
            System.out.println("Tipo SQL no reconocido: " + sqlType + ". Usando Object por defecto.");
            return "Object";
        }

        return javaType;
    }

    private static boolean isIgnoredSqlType(String sqlType) {
        return IGNORED_SQL_TYPES.contains(sqlType.toLowerCase());
    }

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Manejar plurales
        if (input.endsWith("es")) {
            input = input.substring(0, input.length() - 2);
        } else if (input.endsWith("s")) {
            input = input.substring(0, input.length() - 1);
        }

        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    // Método para agregar mapeos personalizados si es necesario
    public static void addCustomMapping(String sqlType, String javaType) {
        POSTGRES_TYPE_MAP.put(sqlType.toLowerCase(), javaType);
    }
}
```