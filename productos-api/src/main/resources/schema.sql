-- Tabla Producto
CREATE TABLE IF NOT EXISTS PRODUCTOS
(
    id          UUID PRIMARY KEY,
    nombre      TEXT NOT NULL,
    categoria   TEXT NOT NULL,
    stock       INTEGER,
    descripcion TEXT NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    precio      DOUBLE,
    activo      BOOLEAN
)