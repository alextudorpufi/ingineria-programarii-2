# Multi-stage Dockerfile for Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POMs for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY marketplace-domain/pom.xml marketplace-domain/
COPY marketplace-application/pom.xml marketplace-application/
COPY marketplace-infrastructure/pom.xml marketplace-infrastructure/
COPY marketplace-app/pom.xml marketplace-app/
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY marketplace-domain/src marketplace-domain/src
COPY marketplace-application/src marketplace-application/src
COPY marketplace-infrastructure/src marketplace-infrastructure/src
COPY marketplace-app/src marketplace-app/src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create unprivileged application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy application fat jar from marketplace-app
COPY --from=builder /app/marketplace-app/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
