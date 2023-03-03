### Pedidos API

| Environment Variable    | Default Value                          | Description                                             |
|-------------------------|----------------------------------------|---------------------------------------------------------|
| PORT                    | 8080                                   | The port that the server will listen on                 |
| SSL_PORT                | 8443                                   | The SSL port that the server will listen on             |
| KEYSTORE                | .cert/server_key.p12                   | The path to the keystore containing the SSL certificate |
| KEY_ALIAS               | SuperTechnology                        | The alias of the key in the keystore                    |
| KEYSTORE_PASSWORD       | 1A2B3C4O                               | The password to the keystore                            |
| PRIVATE_KEY_PASSWORD    | 1A2B3C4O                               | The password to the private key in the keystore         |
| JWT_ISSUER              | pedidos                                | The issuer of the JWT token                             |
| JWT_AUDIENCE            | pedidos                                | The audience of the JWT token                           |
| JWT_SECRET              | pedidos                                | The secret used to sign the JWT token                   |
| MONGO_CONNECTION_STRING | mongodb://root:example@localhost:27017 | The connection string for the MongoDB instance          |
| MONGO_DATABASE          | pedidos                                | The name of the database to use in the MongoDB instance |
| USUARIOS_URL            | http://localhost:8081                  | The URL for the usuarios microservice                   |
| PRODUCTOS_URL           | http://localhost:8082                  | The URL for the productos microservice                  |

### Docker compose

```
version: "3.9"

services:
  pedidos-api:
    image: pedidos-api
    ports:
      - "8080:8080"
      - "8443:8443"
    environment:
      - PORT=8080
      - SSL_PORT=8443
      - KEYSTORE=/app/.cert/server_key.p12
      - KEY_ALIAS=SuperTechnology
      - KEYSTORE_PASSWORD=1A2B3C4O
      - PRIVATE_KEY_PASSWORD=1A2B3C4O
      - JWT_ISSUER=pedidos
      - JWT_AUDIENCE=pedidos
      - JWT_SECRET=pedidos
      - MONGO_CONNECTION_STRING=mongodb://root:example@mongo:27017
      - MONGO_DATABASE=pedidos
      - USUARIOS_URL=http://usuarios:8081
      - PRODUCTOS_URL=http://productos:8082
    depends_on:
      - mongo
      - usuarios
      - productos

  mongo:
    image: mongo:latest
    restart: always
    environment:
      - MONGO_INITDB_ROOT_USERNAME=root
      - MONGO_INITDB_ROOT_PASSWORD=example

  usuarios:
    image: usuarios-api
    environment:
    

  productos:
    image: productos-api
    environment:

```