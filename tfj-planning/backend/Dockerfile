# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier d'abord les fichiers de configuration pour profiter du cache Docker
COPY pom.xml .
# Télécharger les dépendances (y compris Lombok)
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler avec les annotation processors Lombok
RUN mvn clean package -DskipTests -B

# Étape 2 : Exécution avec JDK
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
