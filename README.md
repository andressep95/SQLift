````
version: "1"
sql:
  engine: "postgresql"     # Motor de base de datos
  schema: "path/to/your/schema.sql"  # Archivo SQL (cambié 'file' por 'schema' para ser más específico)
  output:
    package: "com.example.demo.entity"  # Paquete base donde se generarán las entidades
    options:
      lombok: true  # (Opcional) Si deseas generar con anotaciones de Lombok
      jpa: true     # (Opcional) Si deseas incluir anotaciones JPA
````

