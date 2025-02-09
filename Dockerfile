# Etapa 1: Construcción del Backend (Spring Boot con Maven)
FROM maven:3.8.7-eclipse-temurin-17 AS backend-build
WORKDIR /backend

# Copiar archivos necesarios y descargar dependencias
COPY Backend/pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar la aplicación
COPY Backend/src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Construcción del Frontend (Angular)
FROM node:18-alpine AS frontend-build
WORKDIR /frontend

# Copiar archivos esenciales y descargar dependencias
COPY Frontend/package.json Frontend/package-lock.json ./
RUN npm install

# Copiar archivos de configuración
COPY Frontend/angular.json Frontend/tsconfig.json Frontend/tsconfig.app.json ./

# Copiar el resto del código fuente
COPY Frontend/ ./

# Construir la aplicación Angular
RUN npm run build

# Etapa 3: Imagen final con JRE y Nginx
FROM eclipse-temurin:17-jre AS final
WORKDIR /app

# Copiar el JAR compilado del backend
COPY --from=backend-build /backend/target/*.jar backend.jar

# Copiar los archivos compilados del frontend
COPY --from=frontend-build /frontend/dist/cnotes /usr/share/nginx/html

# Instalar Nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Copiar configuración personalizada de Nginx desde la carpeta Frontend
COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Copiar el archivo .env desde la raíz del proyecto
COPY .env /app/.env

# Exponer los puertos
EXPOSE 80 8080

# Comando para ejecutar el backend y nginx en segundo plano
CMD ["sh", "-c", "java -jar /app/backend.jar & nginx -g 'daemon off;'"]
