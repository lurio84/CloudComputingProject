# Etapa 1: Construcción de la aplicación Angular
FROM node:18-alpine AS build

WORKDIR /app

# Copiar archivos esenciales primero
COPY package.json package-lock.json ./
RUN npm install

# Copiar archivos de configuración
COPY angular.json tsconfig.json tsconfig.app.json ./

# Copiar el resto del código
COPY . .

# Construir la aplicación Angular
RUN npm run build

# Etapa 2: Servir la aplicación con Nginx
FROM nginx:alpine

# Copiar la configuración de Nginx personalizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar los archivos compilados desde la ruta correcta
COPY --from=build /app/dist/cnotes /usr/share/nginx/html

# Exponer el puerto para Nginx
EXPOSE 80

# Iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]
