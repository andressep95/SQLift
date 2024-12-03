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

| **Comando**                                     | **Descripción**                                                                                 |
|-------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `mvn clean package`                             | Limpia y compila el proyecto, generando un archivo JAR. Si se configura GraalVM, también compila a nativo. |
| `mvn -Pnative package`                          | Compila el proyecto utilizando GraalVM para generar un ejecutable nativo.                       |
| `mv target/<nombre-ejecutable> /usr/local/bin/` | Mueve el ejecutable compilado al PATH para que sea accesible desde cualquier directorio.        |
| `rm /usr/local/bin/<nombre-ejecutable>`         | Elimina el ejecutable del PATH en caso de que necesites volver a compilar o corregir errores.   |
| `nano ~/.zshrc`                                 | Abre el archivo de configuración de Zsh para editar las variables de entorno.                   |
| `source ~/.zshrc`                               | Aplica los cambios realizados en el archivo `.zshrc` sin necesidad de reiniciar la terminal.    |
| `Ctrl + O`                                      | Guarda los cambios realizados en el editor Nano.                                                |
| `Ctrl + X`                                      | Sale del editor Nano.                                                                           |
| `which <nombre-ejecutable>`                     | Busca la ubicación de un ejecutable en el sistema.                                              |
| `sudo rm /usr/local/bin/<nombre-ejecutable>`    | Elimina un ejecutable específico de `/usr/local/bin/`.                                          |
