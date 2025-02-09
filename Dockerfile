# Base image with JRE and required tools
FROM eclipse-temurin:17-jdk AS builder

# Set working directory
WORKDIR /app

# Copy only necessary files for Backend build
COPY Backend /app/Backend
WORKDIR /app/Backend

# Build Backend using Maven Wrapper
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Install dependencies and build Frontend (Angular)
FROM node:20 AS frontend-builder
WORKDIR /app/Frontend
COPY Frontend /app/Frontend

RUN npm install && npm run build

# Final image with Nginx and JRE
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy Backend JAR from builder stage
COPY --from=builder /app/Backend/target/*.jar /app/Backend.jar

# Copy .env file to the correct location
COPY .env /app/.env

# Install only required system dependencies (without cache)
RUN apt-get update && apt-get install -y --no-install-recommends nginx && rm -rf /var/lib/apt/lists/*

# Copy built frontend files to Nginx web directory
COPY --from=frontend-builder /app/Frontend/dist/cnotes /usr/share/nginx/html/

# Copy the Nginx configuration
COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose necessary ports
EXPOSE 80 8080

# Start Backend and Nginx
CMD ["sh", "-c", "java -jar /app/Backend.jar & nginx -g 'daemon off;'"]
