FROM eclipse-temurin:17-jdk-alpine

# Crear el punto de montaje que usará EFS
RUN mkdir -p /mnt/efs/tickets

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
