# Gestión de usuarios

Micro servicio de gestión de usuarios de una tienda de tecnología para las
asignaturas de Acceso a Datos y Programación de Procesos y Servicios del
IES Luis Vives (Leganés) curso 22/23.

# Índice

- [Diseño](#diseño)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Endpoints](#endpoints)
- [Funcionamiento de la aplicación](#funcionamiento-de-la-aplicación)
- [Tests](#tests)
- [Autores](#autores)

# Diseño

## Introducción

Este micro servicio se ha diseñado usando **PostgreSQL** como
*base de datos relacional* + [Spring](https://spring.io/) como *framework*.

El proyecto fue creado haciendo uso de la herramienta online [Spring Initializr](https://start.spring.io/).

Luego de una discusión con el equipo de desarrollo, decidimos hacer este micro servicio de manera reactiva,
aplicando **R2DBC**.

## Configuración del proyecto

La configuración del proyecto se realizó utilizando [Gradle](https://gradle.org/); nos apoyamos
en las siguientes tecnologías:

- [Spring Data](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Corrutinas Kotlin](https://kotlinlang.org/docs/coroutines-overview.html)
- [PostgreSQL](https://www.postgresql.org/)
- [R2DBC](https://r2dbc.io/)
- [Mockk](https://mockk.io/)
- [Dokka](https://github.com/Kotlin/dokka)
- [Auth0 JWT](https://auth0.com/docs/secure/tokens/json-web-tokens)
- [Sertialization Kotlin JSON](https://github.com/Kotlin/kotlinx.serialization)
- [Swagger-SpringDoc-OpenAPI](https://springdoc.org/v2/)
- [JUnit5](https://junit.org/junit5/)

## Configuración de la base de datos

La base de datos es ejecutada desde un [docker](https://www.docker.com/), y nos conectamos a ella; las tablas son
cargadas desde el archivo [schema.sql](./src/main/resources/schema.sql); los datos iniciales son
cargados desde la clase Main (en este caso, *UsuariosApiApplication.kt*) haciendo uso de una lista de
usuarios base y un método del controlador.

## Dominio

Gestionar usuarios y direcciones:

### Usuario

| Campo     | Tipo      | Descripción                                |
|-----------|-----------|--------------------------------------------|
| id        | UUID      | Identificador único.                       |
| username  | String    | Nombre del usuario.                        |
| email     | String    | Email del usuario.                         |
| password  | String    | Contraseña del usuario.                    |
| phone     | String    | Teléfono del usuario                       |
| avatar    | String    | Avatar del usuario.                        |
| role      | UserRole  | Rol del usuario(USER, ADMIN O SUPER_ADMIN) |
| createdAt | LocalDate | Fecha de creación del usuario.             |
| active    | Boolean   | Estado activo del usuario.                 |

### Dirección

| Campo   | Tipo   | Descripción                               |
|---------|--------|-------------------------------------------|
| id      | UUID   | Identificador único.                      |
| userId  | UUID   | Identificador único del usuario asociado. |
| address | String | Nombre de la dirección.                   |

# Estructura del proyecto

## Documentación

Las clases se encuentran documentadas con KDoc, y hemos implantado **Dokka** para poder presentar una
documentación en formato *HTML*.

Además, los endpoints se encuentran documentados con **Swagger-OpenAPI**, visible desde el respectivo endpoint.

## Seguridad

Hemos aplicado [Spring Security v6](https://docs.spring.io/spring-security/reference/index.html) para garantizar
la seguridad e integridad de la API y de los usuarios; la conexión es segura (SSL) y hemos usado **JWT Auth0** para
la generación de tokens personales.

Las contraseñas de los usuarios se encuentran encriptadas con [Bcrypt](https://en.wikipedia.org/wiki/Bcrypt) con una
implementación propia de **Spring Security**

# Endpoints

La ruta del micro servicio será: https://localhost:6969/usuarios

## Usuarios

| Método | Endpoint(/usuarios)                 | Auth | Descripción                                        | Status Code | Return Content |
|--------|-------------------------------------|------|----------------------------------------------------|-------------|----------------|
| GET    |                                     | NO   | Mensaje de bienvenida.                             | 200         | String         |
| GET    | /                                   | NO   | Mensaje de bienvenida Alt.                         | 200         | String         |
| POST   | /register                           | NO   | Registro de usuario.                               | 200         | JSON           |
| POST   | /create                             | JWT  | Creación de usuario por un Administrador.          | 200         | JSON           |
| GET    | /login                              | NO   | Iniciar sesión.                                    | 200         | JSON           |
| GET    | /list                               | JWT  | Mostrar un listado completo de usuarios.           | 200         | JSON           |
| GET    | /list/paging?page=X&size=Y&sortBy=Z | JWT  | Mostrar un listado paginado de usuarios.           | 200         | JSON           |
| GET    | /list/{active}                      | JWT  | Mostrar un listado según la actividad de usuarios. | 200         | JSON           |
| GET    | /username/{username}                | JWT  | Búsqueda de un usuario por nombre de usuario.      | 200         | JSON           |
| GET    | /id/{id}                            | JWT  | Búsqueda de un usuario por ID.                     | 200         | JSON           |
| GET    | /email/{userEmail}                  | JWT  | Búsqueda de un usuario por email.                  | 200         | JSON           |
| GET    | /phone/{userPhone}                  | JWT  | Búsqueda de un usuario por teléfono.               | 200         | JSON           |
| PUT    | /me                                 | JWT  | Actualización de un usuario a si mismo.            | 200         | JSON           |
| PUT    | /me/avatar                          | JWT  | Actualización del avatar de un usuario a si mismo. | 200         | JSON           |
| PUT    | /activity/{email}                   | JWT  | Cambio de actividad de un usuario.                 | 200         | JSON           |
| PUT    | /role                               | JWT  | Actualización de rol de un usuario.                | 200         | JSON           |
| DELETE | /{email}                            | JWT  | Eliminación de un usuario.                         | 200         | JSON           |
| GET    | /me                                 | JWT  | Obtener información de un usuario a si mismo.      | 200         | JSON           |

## Direcciones

| Método | Endpoint(/usuarios)                 | Auth | Descripción                                                                 | Status Code | Return Content |
|--------|-------------------------------------|------|-----------------------------------------------------------------------------|-------------|----------------|
| GET    | /list/address                       | JWT  | Mostrar listado completo de direcciones.                                    | 200         | JSON           |
| GET    | /list/paging?page=X&size=Y&sortBy=Z | JWT  | Mostrar listado paginado de direcciones.                                    | 200         | JSON           |
| GET    | /list/address/{userId}              | JWT  | Mostrar direcciones de un usuario.                                          | 200         | JSON           |
| GET    | /address/{id}                       | JWT  | Búsqueda de una dirección por ID.                                           | 200         | String         |
| GET    | /address?name=X                     | JWT  | Búsqueda de una dirección por nombre.                                       | 200         | String         |
| DELETE | /me/address?name=X                  | JWT  | Eliminación de una dirección propia por nombre.                             | 200         | String         |
| DELETE | /address?name=X&email=Y             | JWT  | Eliminación de una dirección de un usuario por nombre de dirección y email. | 200         | String         |

### Almacenamiento

| Método | Endpoint(/usuarios/storage) | Auth | Descripción                              | Status Code | Return Content |
|--------|-----------------------------|------|------------------------------------------|-------------|----------------|
| GET    | /filename:.+                | NO   | Obtener archivo especificado almacenado. | 200         | Resource       |
| POST   | " "                         | JWT  | Subir archivo multiparte.                | 201         | JSON           |
| DELETE | /filename:.+                | JWT  | Eliminar archivo especifo almacenado.    | 200         |                |

# Funcionamiento de la aplicación

Haciendo uso de un cliente que permita recibir y enviar *request-response*, por ejemplo,
[Postman](https://www.postman.com/) o el plugin [Thunder Client](https://www.thunderclient.com/)
en [VSC](https://code.visualstudio.com/) se realizan las operaciones mostradas en sus respectivos *end points*.

# Tests

Se han testeado los repositorios, los servicios y los controladores, usando **JUnit 5** y aplicando **Mockk**.

Se han testeado los end points usando **Postman** "E2E"; se adjunta el *json* exportado.

# Autores

[Mario Resa](https://github.com/Mario999X), [Daniel Rodríguez](https://github.com/Idliketobealoli),
[Sebastian Mendoza](https://github.com/SebsMendoza), [Alfredo Rafael Maldonado](https://github.com/reyalfre),
[Azahara Blanco](https://github.com/Azaharabl), [Roberto Blázquez](https://github.com/xBaank),
[Iván Azagra](https://github.com/IvanAzagraTroya).