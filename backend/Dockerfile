# syntax=docker/dockerfile:1

FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /workspace
COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src
RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
