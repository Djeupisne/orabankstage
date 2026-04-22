# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier d'abord pom.xml pour le cache
COPY pom.xml .

# Copier le code source
COPY src ./src

# Compiler et packager - Maven utilisera automatiquement Lombok comme annotation processor
# grâce à la configuration dans pom.xml (maven-compiler-plugin avec annotationProcessorPaths)
RUN mvn clean package -DskipTests -B

# Étape 2 : Exécution avec JDK
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
