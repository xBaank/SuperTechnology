DROP TABLE IF EXISTS productos;
CREATE TABLE IF NOT EXISTS productos
(
    uuid        TEXT NOT NULL,
    nombre      TEXT NOT NULL,
    categoria   TEXT NOT NULL,
    stock       INTEGER,
    description TEXT NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    precio      DOUBLE,
    activo      BOOLEAN
);
INSERT INTO productos(uuid, nombre, categoria, stock, description, precio, activo)
VALUES (uuid(), 'perro', 'PIEZA', 3, 'Animal', 12.3, true);
INSERT INTO productos(uuid, nombre, categoria, stock, description, precio, activo)
VALUES (uuid(), 'gato', 'PIEZA', 3, 'Animal', 10.3, false);

