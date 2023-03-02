# Generacion de una imagen docker
FROM gradle:7-jdk17 as build

# Se copia el codigo fuente de la aplicacion; el directorio actual
COPY --chown=gradle:gradle . /home/usuarios-api/gradle/src
WORKDIR /home/usuarios-api/gradle/src
RUN gradle bootJar --no-daemon

# Generamos la imagen que ejecuta la aplicacion
FROM openjdk:17-jdk-slim-buster
EXPOSE 6969:6969
# Directorio donde se almacena la aplicacion
RUN mkdir /app
# Copiamos el jar
COPY --from=build /home/usuarios-api/gradle/src/build/libs/usuarios-api-0.0.1.jar /app/usuarios-api-springboot.jar
# Ejecutamos la aplicacion
ENTRYPOINT ["java", "-jar", "/app/usuarios-api-springboot.jar"]