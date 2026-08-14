# Multi-stage Dockerfile for Fleet Telematics Stream Service
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy pom.xml and source files
COPY pom.xml .
COPY src ./src

# Install maven & build jar
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built artifact from builder
COPY --from=builder /app/target/telematics-stream-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=prod

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
