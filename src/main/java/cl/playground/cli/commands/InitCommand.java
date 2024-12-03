package cl.playground.cli.commands;

import picocli.CommandLine.Command;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Command(
        name = "init",
        description = "Initialize SQLift configuration files"
)
public class InitCommand implements Runnable {

    @Override
    public void run() {
        generateYamlFile();
        generateReadmeFile();
        System.out.println("✅ sqlift.yaml and README.md files generated successfully");
    }

    private void generateYamlFile() {
        String yamlContent = "version: \"1\"\n" +
                "sql:\n" +
                "  engine: \"postgresql\"     # Database engine\n" +
                "  schema: \"path/to/your/schema.sql\"\n" +
                "  output:\n" +
                "    package: \"com.example.demo.entity\"\n" +
                "    options:\n" +
                "      lombok: false\n" +
                "      jpa:\n" +
                "        enabled: true\n" +
                "        type: \"jakarta\"  # o \"javax\"\n";

        writeToFile("sqlift.yaml", yamlContent);
    }

    private void generateReadmeFile() {
        String readmeContent = "# SQLift Generator\n\n" +
                "A tool to generate Java entity classes from SQL schema files.\n\n" +
                "## Configuration\n\n" +
                "Edit the `sqlift.yaml` file with the following structure:\n\n" +
                "```yaml\n" +
                "version: \"1\"\n" +
                "sql:\n" +
                "  engine: \"postgresql\"\n" +
                "  schema: \"path/to/your/schema.sql\"\n" +
                "  output:\n" +
                "    package: \"com.example.demo.entity\"\n" +
                "    options:\n" +
                "      lombok: true\n" +
                "      jpa: true\n" +
                "```\n\n" +
                "### Configuration Options:\n\n" +
                "- `version`: Configuration version (currently \"1\")\n" +
                "- `sql`:\n" +
                "  - `engine`: Database engine (postgresql, mysql, etc.)\n" +
                "  - `schema`: Path to your SQL schema file\n" +
                "  - `output`:\n" +
                "    - `package`: Base package for generated entities\n" +
                "    - `options`:\n" +
                "      - `lombok`: Generate with Lombok annotations\n" +
                "      - `jpa`: Include JPA annotations\n\n" +
                "## Supported Features\n\n" +
                "- Supported Databases:\n" +
                "  - PostgreSQL\n" +
                "  - MySQL\n" +
                "  - Oracle\n" +
                "  - SQL Server\n\n" +
                "- Code Generation Options:\n" +
                "  - Lombok annotations (@Data, @Getter, @Setter, etc.)\n" +
                "  - JPA annotations (@Entity, @Table, @Column, etc.)\n\n" +
                "## Usage\n\n" +
                "1. Initialize the configuration:\n" +
                "   ```bash\n" +
                "   sqlift init\n" +
                "   ```\n\n" +
                "2. Edit the `sqlift.yaml` file with your configuration\n\n" +
                "3. Generate the entities:\n" +
                "   ```bash\n" +
                "   sqlift generate\n" +
                "   ```\n";

        writeToFile("README.md", readmeContent);
    }

    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName + ": " + e.getMessage());
        }
    }
}