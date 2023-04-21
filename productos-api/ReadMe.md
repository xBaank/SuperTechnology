# Gestión de productos

Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y Programación
de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23

# Índice

- [Diseño](#diseño)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Endpoints](#endpoints)
- [Funcionamiento de la aplicación](#funcionamiento-de-la-aplicación)
- [Tests](#tests)
- [Autores](#autores)

# Diseño

## Introducción

Este micro servicio se ha diseñado usando *MariaDB* como *base de datos relacional* + [Spring](https://spring.io/) como
*framework*.

El proyecto se creó utilizando la herramienta [Spring Initializr](https://www.jetbrains.com/help/idea/spring-boot.html)
integrada en IntelliJ.

En este caso, y siguiendo la línea por la que se ha seguido el curso, se decidió que este micro servicio fuera reactivo
aplicando **R2DBC**.

## Configuración del proyecto

La configuración del proyecto se realizó utilizando [Gradle](https://gradle.org/). Utilizamos las siguientes
dependencias:

- [Spring Data](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Corrutinas Kotlin](https://kotlinlang.org/docs/coroutines-overview.html)
- [MariaDB](https://mariadb.org/)
- [R2DBC](https://r2dbc.io/)
- [Mockk](https://mockk.io/)
- [Dokka](https://github.com/Kotlin/dokka)
- [Auth0 JWT](https://auth0.com/docs/secure/tokens/json-web-tokens)
- [Swagger-SpringDoc-OpenAPI](https://springdoc.org/v2/)
- [JUnit5](https://junit.org/junit5/)

## Configuración de la base de datos

La base de datos se ejecuta desde [Docker](https://www.docker.com/), a la cual nos conectamos. Las tablas y la
información de los productos se cargan desde un archivo [schema.sql](./src/main/resources/schema.sql) para poder tratar
los datos desde el principio de la ejecución.

## Dominios

Gestión de productos:

### Producto

| Campo       | Tipo          | Descripción                          |
|-------------|---------------|--------------------------------------|
| uuid        | UUID          | Identificador único.                 |
| nombre      | String        | Nombre del producto.                 |
| categoria   | Categoria     | Categoría del producto.              |
| Stock       | Int           | Cantidad de producto la BBDD.        |
| description | String        | Descripción del producto.            |
| createdAt   | LocalDateTime | Fecha de creación del producto.      |
| updateAt    | LocalDateTime | Fecha de actualización del producto. |
| deleteAt    | LocalDateTime | Fecha de eliminación de producto.    |
| activo      | Boolean       | Estado activo del producto.          |

# Estructura del proyecto

## Documentación

Las clases se encuentran documentadas con KDoc, y se ha implementado **Dokka** para poder presentar una documentación en
formato *HTML*.

Además, los endpoints se encuentran documentados con **Swagger-OpenApu**, visibles en la carpeta Controller.

## Seguridad

Se ha aplicado a este micro servicio [Spring Security v6](https://docs.spring.io/spring-security/reference/index.html)
para garantizar la seguridad e integridad de la API. Además, se ha implementado **SSL** y **JWT** para que el proyecto
fuera más seguro todavía.

# Endpoints

La ruta del micro servicio es: https://localhost:6963/products

## Productos

| Método | Endpoint(/products)        | Auth | Descripción                                                   | Status Code | Return Content |
|--------|----------------------------|------|---------------------------------------------------------------|-------------|----------------|
| GET    |                            | NO   | Obtención de todos los productos.                             | 200         | JSON           |
| GET    | /{id}                      | NO   | Obtención de producto mediante uuid                           | 200         | JSON           |
| GET    | /category/{category}       | NO   | Obtención de producto mediante una categoría.                 | 200         | JSON           |
| POST   | /name/{name}               | NO   | Obtención de producto mediante un nombre específico.          | 200         | JSON           |
| GET    | /paging                    | NO   | Obtención de productos paginado.                              | 200         | JSON           |
| GET    | /admin                     | JWT  | Muestra todos los productos detallados.                       | 200         | JSON           |
| GET    | /admin/{id}                | JWT  | Muestra un producto por uuid detallado.                       | 200         | JSON           |
| GET    | /admin/category/{category} | JWT  | Muestra una lista de productos detallados según una categoría | 200         | JSON           |
| GET    | /admin/name/{name}         | JWT  | Muestra un producto detallado con un nombre específico.       | 200         | JSON           |
| GET    | /admin/paging              | JWT  | Lista de productos paginada y detallada.                      | 200         | JSON           |
| POST   | /                          | JWT  | Creación de un producto nuevo.                                | 200         | JSON           |
| PUT    | /{id}                      | JWT  | Actualización de un producto.                                 | 200         | JSON           |
| DELETE | /                          | JWT  | Eliminación permanente de un producto.                        | 200         | JSON           |

# Funcionamiento de la aplicación

Haciendo uso de [Postman](https://www.postman.com/), se ha podido realizar pruebas de todos los endpoints realizados.

# Test

El repositorio y el controlador de este micro servicio se han testeado con **JUnit5** y **Mock**. Para comprobar los
endpoints, se ha generado un archivo *json* importado desde Postman.

# Autores

[Mario Resa](https://github.com/Mario999X), [Daniel Rodríguez](https://github.com/Idliketobealoli),
[Sebastian Mendoza](https://github.com/SebsMendoza), [Alfredo Rafael Maldonado](https://github.com/reyalfre),
[Azahara Blanco](https://github.com/Azaharabl), [Roberto Blázquez](https://github.com/xBaank),
[Iván Azagra](https://github.com/IvanAzagraTroya).