package cl.playground.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SchemaProcessorTest {

    private SchemaProcessor schemaProcessor;
    private PostgresEngine postgresEngine;

    @BeforeEach
    void setUp() {
        postgresEngine = new PostgresEngine();
        schemaProcessor = new SchemaProcessor(postgresEngine);
    }

    @Test
   void testSchemaProcessor() {
        String sqlContent = """
            CREATE TABLE SUCURSALES (
                id SERIAL,
                nombre VARCHAR(100) NOT NULL,
                direccion VARCHAR(200) NOT NULL,
                telefono VARCHAR(20),
                email VARCHAR(100),
                activo BOOLEAN DEFAULT TRUE,
                PRIMARY KEY (id)
            );
            CREATE TABLE CATEGORIAS (
                id SERIAL,
                nombre VARCHAR(50) NOT NULL,
                descripcion TEXT,
                stock_minimo INTEGER DEFAULT 10,
                PRIMARY KEY (id)
            );
            CREATE TABLE PROVEEDORES (
                id SERIAL,
                nombre VARCHAR(100) NOT NULL,
                rut VARCHAR(20) NOT NULL UNIQUE,
                direccion VARCHAR(200),
                telefono VARCHAR(20),
                email VARCHAR(100),
                activo BOOLEAN DEFAULT TRUE,
                PRIMARY KEY (id)
            );
            CREATE TABLE PRODUCTOS (
                id SERIAL,
                codigo VARCHAR(50) NOT NULL UNIQUE,
                nombre VARCHAR(100) NOT NULL,
                descripcion TEXT,
                precio_compra DECIMAL(10,2) NOT NULL,
                precio_venta DECIMAL(10,2) NOT NULL,
                categoria_id INTEGER NOT NULL,
                proveedor_id INTEGER NOT NULL,
                activo BOOLEAN DEFAULT TRUE,
                PRIMARY KEY (id),
                FOREIGN KEY (categoria_id) REFERENCES CATEGORIAS(id) ON DELETE RESTRICT,
                FOREIGN KEY (proveedor_id) REFERENCES PROVEEDORES(id) ON DELETE RESTRICT
            );
            CREATE TABLE STOCK_SUCURSAL (
                sucursal_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL DEFAULT 0,
                ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (sucursal_id, producto_id),
                FOREIGN KEY (sucursal_id) REFERENCES SUCURSALES(id) ON DELETE CASCADE,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            CREATE TABLE MOVIMIENTOS (
                id SERIAL,
                tipo_movimiento VARCHAR(20) NOT NULL,
                sucursal_origen_id INTEGER NOT NULL,
                sucursal_destino_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                observacion TEXT,
                PRIMARY KEY (id),
                FOREIGN KEY (sucursal_origen_id) REFERENCES SUCURSALES(id) ON DELETE RESTRICT,
                FOREIGN KEY (sucursal_destino_id) REFERENCES SUCURSALES(id) ON DELETE RESTRICT,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            CREATE TABLE COMPRAS_PROVEEDOR (
                id SERIAL,
                proveedor_id INTEGER NOT NULL,
                fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                numero_factura VARCHAR(50),
                total DECIMAL(10,2) NOT NULL,
                PRIMARY KEY (id),
                FOREIGN KEY (proveedor_id) REFERENCES PROVEEDORES(id) ON DELETE RESTRICT
            );
            CREATE TABLE DETALLE_COMPRA (
                compra_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                precio_unitario DECIMAL(10,2) NOT NULL,
                subtotal DECIMAL(10,2) NOT NULL,
                PRIMARY KEY (compra_id, producto_id),
                FOREIGN KEY (compra_id) REFERENCES COMPRAS_PROVEEDOR(id) ON DELETE CASCADE,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            """;

        schemaProcessor.processSchema(sqlContent).forEach(table -> {
            System.out.println("Tabla: " + table.getTableName());
            System.out.println("Columnas:");
            table.getColumns().forEach(column -> {
                System.out.println("  - " + column.getColumnName() + " (" + column.getColumnType() + ")");
            });
            System.out.println("Claves primarias: " + table.getPrimaryKeys());
            System.out.println("Relaciones:");
            table.getRelations().forEach(relation -> {
                System.out.println("  - " + relation.getSourceColumn() + " -> " + relation.getTargetTable() + "." + relation.getTargetColumn());
            });
        });
   }
}