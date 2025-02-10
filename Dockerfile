# 1. Backend (Java) Build Stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy only the necessary files for the Backend
COPY Backend /app/Backend
WORKDIR /app/Backend

# Build Backend using Maven Wrapper
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# 2. Frontend (Angular) Build Stage
FROM node:20 AS frontend-builder
WORKDIR /app/Frontend
COPY Frontend /app/Frontend

RUN npm install && npm run build

# 3. Final Image with Nginx and JRE
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy Backend JAR from the builder stage
COPY --from=builder /app/Backend/target/*.jar /app/Backend.jar

# Copy the .env file to the correct location
COPY .env /app/.env

# Install required dependencies
RUN apt-get update && apt-get install -y --no-install-recommends nginx && rm -rf /var/lib/apt/lists/*

# Ensure Nginx does not use the default configuration
RUN rm -rf /etc/nginx/sites-enabled/default /etc/nginx/sites-available/default /etc/nginx/conf.d/default.conf

# Copy built frontend files to the Nginx web directory
COPY --from=frontend-builder /app/Frontend/dist/cnotes /usr/share/nginx/html/

# Copy the custom Nginx configuration
COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Ensure correct permissions
RUN chmod -R 755 /usr/share/nginx/html && chown -R www-data:www-data /usr/share/nginx/html

# Validate Nginx configuration before starting
RUN nginx -t

# Expose necessary ports
EXPOSE 80 8080

# Start Backend and Nginx
CMD ["sh", "-c", "java -jar /app/Backend.jar & nginx -g 'daemon off;'"]
