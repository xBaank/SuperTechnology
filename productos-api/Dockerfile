# Generacion de una imagen docker
FROM gradle:7-jdk17 as build

# Se copia el codigo fuente de la aplicacion; el directorio actual
COPY --chown=gradle:gradle . /home/productos-api/gradle/src
WORKDIR /home/productos-api/gradle/src
RUN gradle bootJar --no-daemon

# Generamos la imagen que ejecuta la aplicacion
FROM openjdk:17-jdk-slim-buster
EXPOSE 6963:6963
# Directorio donde se almacena la aplicacion
RUN mkdir /app
# Copiamos el jar
COPY --from=build /home/productos-api/gradle/src/build/libs/productos-api-0.0.1.jar /app/productos-api-springboot.jar
# Ejecutamos la aplicacion
ENTRYPOINT ["java", "-jar", "/app/productos-api-springboot.jar"]