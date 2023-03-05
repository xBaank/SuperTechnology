DROP TABLE IF EXISTS productos;
CREATE TABLE IF NOT EXISTS productos
(
    id          INTEGER PRIMARY KEY AUTO_INCREMENT,
    uuid        UUID not NULL default UUID(),
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
-- PIEZAS
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Gigabyte B550 AORUS ELITE V2', 'PIEZA', 3, 'Placa base de Gigabyte', 129.91, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('MSI MPG B550 GAMING PLUS', 'PIEZA', 4, 'Placa base de MSI', 149.89, false);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Asus TUF GAMING B550-PLUS', 'PIEZA', 5, 'Placa base de Asus', 134.90, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('MSI Spatium M390 SSD 500GB NVMe M.2', 'PIEZA', 2, 'Disco duro de MSI', 67.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Kioxia EXCERIA 480GB SSD SATA', 'PIEZA', 3, 'Disco duro de Kioxia', 33.99, false);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Samsung 980 SSD 1TB PCIe 3.0 NVMe M.2', 'PIEZA', 6, 'Disco duro de Samsung', 99.98, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('MSI GeForce RTX 3060 Ti VENTUS 3X 8GD6X OC 8GB GDDR6X', 'PIEZA', 6, 'Tarjeta gráfica de MSI', 509.89, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Asus Dual GeForce RTX 3060 OC Edition V2 12GB GDDR6', 'PIEZA', 6, 'Tarjeta gráfica de Asus', 369.90, false);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Gigabyte GeForce RTX 3060 GAMING OC 12GB GDDR6 Rev 2.0', 'PIEZA', 6, 'Tarjeta gráfica de Gigabyte', 399.91, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Intel Core i7-13700KF 3.4 GHz Box', 'PIEZA', 6, 'Procesador de Intel', 399.91, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('AMD Ryzen 5 5500 3.6GHz Box', 'PIEZA', 6, 'Procesador de AMD', 399.91, false);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Intel Core i9-13900KF 3 GHz Box', 'PIEZA', 6, 'Procesador de Intel', 599.91, true);
-- REPARACIONES
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Movil iPhone 13 128GB', 'REPARACION', 1, 'Cambiar pantalla', 10.3, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Movil Samsung Galaxy A13 A137 3/32GB', 'REPARACION', 1, 'Reparar botón encendido', 7.3, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Portatil HP Victus 16-d1033ns Intel Core i7', 'REPARACION', 1, 'Reparación de pad tactil', 20.70, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Portatil Lenovo Legion 5 15IAH7H Intel Core i7', 'REPARACION', 1, 'Reparación de teclado', 25.90, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Videoconsola PS4', 'REPARACION', 1, 'Reparar lector de discos', 35.7, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('HP DeskJet 2720e Impresora Multifuncion Color Wifi', 'REPARACION', 1, 'Reparación de scanner', 15.25, true);
-- MONTAJE
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('HP Victus 15L TG02-0020ns AMD Ryzen 7 5700G-16GB-1TB 512GB SSD-GTX 1660 SUPER', 'MONTAJE', 1, 'Montaje de PC', 879.98, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('HP OMEN 40L GT21-0024ns AMD Ryzen 5 5600X-16GB-512GB SSD-RTX 3060Ti', 'MONTAJE', 1, 'Montaje de PC', 1299.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('ASUS ExpertCenter D5 Tower D500TC-511400030X Intel Core i5-11400-8GB-256GB SSD', 'MONTAJE', 1, 'Montaje de PC', 499.0, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('MSI MAG META S 5SI-044XES AMD Ryzen 5 5600X-16GB-512GB SSD-GTX 1660 SUPER', 'MONTAJE', 1, 'Montaje de PC', 849.99, true);
-- PERSONALIZADO
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Luces leds', 'PERSONALIZADO', 1, 'Instalación de luces led en pc sobremesa', 50.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Refrigeracion liquida', 'PERSONALIZADO', 1, 'Instalación de refrigeración líquida en pc de sobremesa', 100.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Disco SSD', 'PERSONALIZADO', 1, 'Instalación de nueva SSD en portatil', 60.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Ventilador led', 'PERSONALIZADO', 1, 'Instalación de ventilador con luz led', 45.99, true);
-- MÓVIL
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Apple iPhone 14 Pro Max 256GB Negro', 'MOVIL', 10, 'Móvil de Apple', 1438.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Samsung Galaxy A53 5G 8-256GB Negro', 'MOVIL', 5, 'Móvil de Samsung', 389.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Xiaomi Redmi Note 10 Pro 8-128GB Gris', 'MOVIL', 7, 'Móvil de Xiaomi', 248.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Realme GT Neo 2 5G 12-256GB Negro', 'MOVIL', 10, 'Móvil de Realme', 410.99, true);
-- PORTÁTIL
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Lenovo Legion 5 15IAH7H Intel Core i7', 'PORTATIL', 4, 'Portátil de Lenovo', 1569.01, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('ASUS ROG Strix G15 G513RC-HF094 AMD Ryzen 7', 'PORTATIL', 7, 'Portátil de Asus', 889.0, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('MSI Katana GF66 12UC-820XES Intel Core i7', 'PORTATIL', 2, 'Portátil de MSI', 849.0, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('HP Victus 16-d1021ns Intel Core i7', 'PORTATIL', 3, 'Portátil de HP', 849.0, true);
-- SOBREMESA
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('HP OMEN 40L GT21-0050ns Intel Core i7', 'SOBREMESA', 3, 'PC de sobremesa de HP', 1899.99, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Lenovo Legion T5 26IAB7 Intel Core i7', 'SOBREMESA', 4, 'PC de sobremesa de Lenovo', 1949.01, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Acer Nitro N50 N50-640 Intel Core i7', 'SOBREMESA', 5, 'PC de sobremesa de Acer', 1149.98, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Dell OptiPlex 5000 MFF Intel Core i7', 'SOBREMESA', 3, 'PC de sobremesa de Dell', 1140.63, true);
-- TABLETS
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Apple iPad Pro 2022 11 WiFi 128GB Gris', 'TABLET', 3, 'Tablet de Apple', 1029.0, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Samsung Galaxy Tab S8 Plus 5G 128GB Plata', 'TABLET', 4, 'Tablet de Samsung', 1249.0, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Huawei MediaPad T5 10.1 2-32GB 4G Negra', 'TABLET', 3, 'Tablet de Huawei', 281.83, true);
INSERT INTO productos(nombre, categoria, stock, description, precio, activo)
VALUES ('Lenovo Yoga Tab 11 4-128GB 11 Gris', 'TABLET', 3, 'Tablet de Lenovo', 440.08, true);
