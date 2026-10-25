# Multi-stage Dockerfile for Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY src/ src/
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create unprivileged application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy application jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
