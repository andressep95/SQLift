package cl.playground.config.model;

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
        private PersistenceConfig persistence;

        public Options() {
        }

        public boolean isLombok() {
            return lombok;
        }

        public void setLombok(boolean lombok) {
            this.lombok = lombok;
        }

        public PersistenceConfig getPersistence() {
            return persistence;
        }

        public void setPersistence(PersistenceConfig persistence) {
            this.persistence = persistence;
        }

        @Override
        public String toString() {
            return "Options{" +
                    "lombok=" + lombok +
                    ", persistence=" + persistence +
                    '}';
        }
    }

    // Clase interna para la configuración de persistencia
    public static class PersistenceConfig {
        private String mode; // "jakarta", "hibernate", "none"

        public PersistenceConfig() {
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "PersistenceConfig{" +
                    "mode='" + mode + '\'' +
                    '}';
        }
    }
}
