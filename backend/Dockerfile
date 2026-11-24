# Stage 1: Build application JAR
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user
RUN addgroup --system resolvex && adduser --system --ingroup resolvex resolvex
USER resolvex

COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=8088 \
    SPRING_PROFILES_ACTIVE=prod \
    FILE_UPLOAD_DIR=/app/uploads

EXPOSE 8088

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
