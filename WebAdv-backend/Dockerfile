FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring /app/app.jar

USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=5 \
  CMD curl --fail --silent http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
