# Development Guide.

## Build Commands

| Command | Description |
|---------|-------------|
| `mvn clean package` | Clean and compile the project, generating a JAR file. If GraalVM is configured, it also compiles to native. |
| `mvn -Pnative package` | Compile the project using GraalVM to generate a native executable. |

## Installation

| Command                            | Description |
|------------------------------------|-------------|
| `mv target/sqlift /usr/local/bin/` | Move the compiled executable to PATH for system-wide access. |
| `rm /usr/local/bin/sqlift`         | Remove the executable from PATH if you need to recompile or fix errors. |

## Environment Setup

| Command | Description |
|---------|-------------|
| `nano ~/.zshrc` | Open Zsh configuration file to edit environment variables. |
| `source ~/.zshrc` | Apply changes made to `.zshrc` without restarting the terminal. |
| `Ctrl + O` | Save changes in Nano editor. |
| `Ctrl + X` | Exit Nano editor. |

## Utility Commands

| Command                         | Description |
|---------------------------------|-------------|
| `which sqlift`                  | Find the location of an executable in the system. |
| `sudo rm /usr/local/bin/sqlift` | Remove a specific executable from `/usr/local/bin/`. |


## Git Commands
Los comandos en orden serían:

1. Primero eliminar el tag tanto local como remoto:
```bash
# Eliminar tag local
git tag -d v1.0

# Eliminar tag remoto
git push origin :refs/tags/v1.0
```

2. Añadir, hacer commit y push de los cambios:
```bash
# Agregar todos los cambios
git add .

# Commit con mensaje descriptivo
git commit -m "feat: add JPA implementation type selection and improve documentation

- Add support for selecting JPA implementation (jakarta/javax)
- Update configuration model for JPA options
- Improve documentation structure
- Update README with clearer instructions"

# Push al remoto
git push origin develop
```

3. Volver a crear el tag en la versión actualizada:
```bash
# Crear tag local
git tag v1.0

# Push del tag
git push origin v1.0
```
