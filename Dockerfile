# Multi-stage Dockerfile for building and running the Spring Boot application

# Build stage: use Maven with JDK 17 to compile and package the app
FROM maven:3.8.8-jdk-17-slim AS builder
WORKDIR /workspace
# copy maven files first to leverage layer caching
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster iteration)
RUN mvn -B -DskipTests package

# Runtime stage: use a lightweight JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy the packaged jar from the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose the port the app uses (configured as 8081 in application.yml)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

