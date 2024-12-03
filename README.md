# SQLift Generator

A tool to generate Java entity classes from SQL schema files.

## Quick Start

1. Install SQLift:
   ```bash
   mvn clean package
   mv target/sqlift /usr/local/bin/
   ```

2. Initialize a new project:
   ```bash
   sqlift init
   ```

3. Configure your `sqlift.yaml`:
   ```yaml
   version: "1"
   sql:
     engine: "postgresql"
     schema: "path/to/your/schema.sql"
     output:
       package: "com.example.demo.entity"
       options:
         lombok: false
         jpa:
           enabled: true
           type: "jakarta"  # or "javax"
   ```

4. Generate your entities:
   ```bash
   sqlift generate
   ```

## Features

- Database Support:
    - PostgreSQL
    - MySQL (Coming soon)
    - Oracle (Coming soon)
    - SQL Server (Coming soon)

- Code Generation:
    - JPA Annotations (Jakarta EE / Java EE)
    - Lombok Support
    - Clean and maintainable code generation

## Documentation

- [Configuration Guide](docs/CONFIGURATION.md)
- [Development Guide](docs/DEVELOPMENT.md)
```
