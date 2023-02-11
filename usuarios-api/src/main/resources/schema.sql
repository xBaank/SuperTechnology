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

-- Datos de muestra --
INSERT INTO users(id, username, password, email, phone, avatar, role, created_at, active)
VALUES ('e273e764-a7da-11ed-afa1-0242ac120002', 'TEST_USER', 'TEST_PASS', 'test@test.com', '111111111', 'AVATAR_TEST', 'USER', '1994-10-27', 'true')
ON CONFLICT DO NOTHING;

INSERT INTO addresses(user_id, address)
VALUES ('e273e764-a7da-11ed-afa1-0242ac120002', 'TEST_ADDRESS')
ON CONFLICT DO NOTHING;