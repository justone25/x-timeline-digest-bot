# Multi-stage build for X Timeline Digest Bot
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom files first for better layer caching
COPY pom.xml .
COPY digest-domain-core/pom.xml digest-domain-core/
COPY digest-adapter-discord/pom.xml digest-adapter-discord/
COPY digest-adapter-llm/pom.xml digest-adapter-llm/
COPY digest-adapter-x/pom.xml digest-adapter-x/
COPY digest-infra-persistence/pom.xml digest-infra-persistence/
COPY digest-app-boot/pom.xml digest-app-boot/
COPY digest-shared-kernel/pom.xml digest-shared-kernel/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY digest-domain-core/src digest-domain-core/src
COPY digest-adapter-discord/src digest-adapter-discord/src
COPY digest-adapter-llm/src digest-adapter-llm/src
COPY digest-adapter-x/src digest-adapter-x/src
COPY digest-infra-persistence/src digest-infra-persistence/src
COPY digest-app-boot/src digest-app-boot/src
COPY digest-shared-kernel/src digest-shared-kernel/src

# Build application
ARG BUILD_DATE
RUN mvn clean package -DskipTests

# Stage 2: Runtime with Playwright
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy

WORKDIR /app

# Install OpenJDK 21
RUN apt-get update && \
  apt-get install -y openjdk-21-jre-headless && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

# Copy JAR from builder
COPY --from=builder /build/digest-app-boot/target/digest-app-boot-*.jar /app/app.jar

# Create directory for cookies
RUN mkdir -p /app/config

# Set environment variables with defaults
ENV JAVA_OPTS="" \
  TWITTER_COOKIES_PATH=/app/config/cookies.json \
  TWITTER_HEADLESS=true \
  KIMI_API_KEY="" \
  KIMI_MODEL=kimi-k2-turbo-preview \
  DISCORD_WEBHOOK_URL=""

# Expose port (if needed for monitoring in future)
# EXPOSE 8080

# Health check (optional)
HEALTHCHECK --interval=5m --timeout=10s --start-period=30s \
  CMD pgrep -f "app.jar" || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
