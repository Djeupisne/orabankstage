# Étape 1 : Build avec Node.js
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG API_URL=http://localhost:8080
ENV API_URL=${API_URL}
RUN npm run build -- --configuration production

# Étape 2 : Servir avec Nginx
FROM nginx:alpine
COPY --from=build /app/dist/tfj-planning-browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
