# Generamos la imagen que ejecuta la aplicacion
FROM openjdk:17-jdk-slim-buster
EXPOSE 6963:6963
# Directorio donde se almacena la aplicacion
# RUN mkdir /app
# Copiamos el jar
COPY  build/libs/pedidos-api-all.jar /app/pedidos-api.jar
# Ejecutamos la aplicacion
ENTRYPOINT ["java", "-jar", "/app/pedidos-api.jar"]