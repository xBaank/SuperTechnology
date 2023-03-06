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

# Diseño

## Introducción

Este micro servicio se ha diseñado usando [PostgreSQL](https://www.postgresql.org/) como
*base de datos relacional* + [Spring](https://spring.io/) como *framework*.

El proyecto fue creado haciendo uso de la herramienta online [Spring Initializr](https://start.spring.io/)

Luego de una discusión con el equipo de desarrollo, decidimos hacer este microservicio de manera reactiva,
aplicando [R2DBC](https://r2dbc.io/)

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

# Estructura del proyecto

## Documentación

Las clases se encuentran documentadas con KDoc, y hemos implantado **Dokka** para poder presentar una
documentación en formato *HTML*.

Además, los endpoints se encuentran documentados con **Swagger-OpenAPI**, visible desde el respectivo endpoint.

## Seguridad

Hemos aplicado [Spring Security v6](https://docs.spring.io/spring-security/reference/index.html) para garantizar
la seguridad e integridad de la API y de los usuarios; la conexión es segura (SSL) y hemos usado **JWT Auth0** para
la generación de tokens personales.

# Endpoints

# Funcionamiento de la aplicación

# Tests