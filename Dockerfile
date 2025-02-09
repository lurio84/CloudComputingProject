# Stage 1: Backend Build (Spring Boot with Maven)
FROM maven:3.8.7-eclipse-temurin-17 AS backend-build
WORKDIR /backend

# Copy necessary files and download dependencies
COPY Backend/pom.xml .
RUN mvn dependency:go-offline

# Copy source code and compile the application
COPY Backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Frontend Build (Angular)
FROM node:18-alpine AS frontend-build
WORKDIR /frontend

# Copy essential files and install dependencies
COPY Frontend/package.json Frontend/package-lock.json ./
RUN npm install

# Copy configuration files
COPY Frontend/angular.json Frontend/tsconfig.json Frontend/tsconfig.app.json ./

# Copy the rest of the source code
COPY Frontend/ ./

# Build the Angular application
RUN npm run build

# Stage 3: Final Image with JRE and Nginx
FROM eclipse-temurin:17-jre AS final
WORKDIR /app

# Copy the compiled backend JAR
COPY --from=backend-build /backend/target/*.jar backend.jar

# Copy the compiled frontend files
COPY --from=frontend-build /frontend/dist/cnotes /usr/share/nginx/html

# Install Nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Copy custom Nginx configuration from the Frontend folder
COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Copy the .env file from the root of the project
COPY .env /app/.env

# Expose ports
EXPOSE 80 8080

# Command to run the backend and Nginx in the background
CMD ["sh", "-c", "java -jar /app/backend.jar & nginx -g 'daemon off;'"]
