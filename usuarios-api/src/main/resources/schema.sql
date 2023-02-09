-- Tablas --
CREATE TABLE IF NOT EXISTS users(
    id uuid NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    avatar VARCHAR(255) NOT NULL,
    role VARCHAR(15) NOT NULL,
    created_at DATE NOT NULL,
    active boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS addresses(
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    address VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES USERS(id)
);

-- Datos de muestra --
INSERT INTO users(id, username, password, email, phone, avatar, role, created_at, active)
VALUES ('e273e764-a7da-11ed-afa1-0242ac120002', 'TEST_USER', 'TEST_PASS', 'test@test.com', '111111111', 'AVATAR_TEST', 'USER', '1994-10-27', 'true')
ON CONFLICT DO NOTHING;

INSERT INTO addresses(id, user_id, address)
VALUES ('60682304-a7e2-11ed-afa1-0242ac120002', 'e273e764-a7da-11ed-afa1-0242ac120002', 'TEST_ADDRESS')
ON CONFLICT DO NOTHING;