# =====================================================================
# Imagen única para la nube: compila el frontend, lo empaqueta dentro del
# backend y ejecuta todo en un solo servicio (una sola URL, sin CORS).
# Base de datos: H2 dentro del contenedor (los datos demo se recrean al reiniciar).
# =====================================================================
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline || true   # solo precarga la caché; no debe frenar la construcción
COPY backend/src ./src
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /app/backend/target/dronemission-backend-1.0.0.jar app.jar
# UTF-8 para que nombres con tildes y ñ (por ejemplo, del administrador) se lean bien
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
# Memoria ajustada al plan gratuito (512 MB)
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Xmx300m -Xss512k -XX:MaxMetaspaceSize=150m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
ENV SPRING_PROFILES_ACTIVE=h2
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
