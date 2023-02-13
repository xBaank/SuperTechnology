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
#INSERT INTO PRODUCTOS VALUES('1234123h12g34j1234', 'perro', 'PIEZA', 3, 'Animal de cuatro patas', null, null, null, 12.3, TRUE)