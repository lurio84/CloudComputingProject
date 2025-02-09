# Etapa 1: Construcción de la aplicación
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar el archivo pom.xml y las dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente de la aplicación
COPY src ./src

# Construir el JAR de la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para la ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el archivo JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Copiar el archivo .env dentro del contenedor (se debe pasar desde el host)
COPY .env .env

# Exponer el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
