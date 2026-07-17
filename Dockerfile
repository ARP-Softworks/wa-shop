# Multi-stage: Angular SPA + Spring Boot JAR → single runtime image.
# Build: docker build -t wa-shop:latest .

# ---------------------------------------------------------------------------
# Stage 1 — Angular production build (dependency layer cached)
# ---------------------------------------------------------------------------
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build -- --configuration=production

# ---------------------------------------------------------------------------
# Stage 2 — Spring Boot package (Maven deps cached separately from sources)
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app/backend

COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline -DskipTests

COPY backend/src ./src
# Run unit/integration tests before packaging (H2/test profile — no external DB required).
RUN mvn -B test

# Profile with-frontend copies from ../frontend/dist/frontend/browser
COPY --from=frontend-build /app/frontend/dist/frontend/browser /app/frontend/dist/frontend/browser

RUN mvn -B -Pwith-frontend package -DskipTests \
  && mv target/wa-shop-*.jar /app/application.jar

# ---------------------------------------------------------------------------
# Stage 3 — Lightweight JRE runtime (non-root)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget \
  && addgroup -S washop \
  && adduser -S washop -G washop \
  && mkdir -p /app/data/media \
  && chown -R washop:washop /app

COPY --from=backend-build --chown=washop:washop /app/application.jar /app/app.jar

USER washop

ENV SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080 \
    JAVA_OPTS=""

EXPOSE 8080

# Uses SERVER_PORT so the healthcheck tracks the configured listen port.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/api/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
