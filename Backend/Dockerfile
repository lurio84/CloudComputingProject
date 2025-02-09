# Etapa 1: Construcción de la aplicación
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos necesarios y descargar dependencias
COPY Backend/pom.xml ./
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar la aplicación
COPY Backend/src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para la ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Copiar el archivo .env dentro del contenedor
COPY .env /app/.env

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
