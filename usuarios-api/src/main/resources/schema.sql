-- RESET ---
DROP TABLE IF EXISTS addresses, users cascade;

-- Tablas --
CREATE TABLE IF NOT EXISTS users(
    id uuid NOT NULL DEFAULT GEN_RANDOM_UUID() UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL UNIQUE,
    avatar VARCHAR(255) NOT NULL,
    role VARCHAR(15) NOT NULL,
    created_at DATE NOT NULL,
    active boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS addresses(
    id uuid NOT NULL DEFAULT GEN_RANDOM_UUID() UNIQUE,
    user_id uuid NOT NULL,
    address VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES USERS(id)
);