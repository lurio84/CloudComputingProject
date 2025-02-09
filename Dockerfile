# Base image with JRE and required tools
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy entire project structure as-is
COPY . .

# Build Backend using Maven Wrapper
WORKDIR /app/Backend
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
WORKDIR /app

# Install dependencies and build Frontend (Angular)
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g npm@latest

RUN cd /app/Frontend && npm install && npm run build

# Install Nginx
RUN apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Move built frontend to Nginx web directory
RUN cp -r /app/Frontend/dist/cnotes/* /usr/share/nginx/html/

# Copy the Nginx configuration
COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose necessary ports
EXPOSE 80 8080

# Start Backend and Nginx in the background
CMD ["sh", "-c", "java -jar /app/Backend/target/*.jar & nginx -g 'daemon off;'" ]