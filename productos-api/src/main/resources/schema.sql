DROP TABLE IF EXISTS productos;
CREATE TABLE IF NOT EXISTS productos
(
    id          UUID PRIMARY KEY,
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
INSERT INTO productos(id, nombre, categoria, stock, description, precio, activo)
VALUES (UUID(), 'perro', 'PIEZA', 3, 'Animal', 12.3, TRUE)
