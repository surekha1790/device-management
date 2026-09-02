# syntax=docker/dockerfile:1

#-----------------------------------------------
# Build Stage - full JDK and Maven
#-----------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

# Cache the maven repository .m2 to avoid re-downloading dependencies
# Test are skipped as they are typically verified in CI build
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

#-----------------------------------------------
# Runtime Stage - JRE only
#-----------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN groupadd --system --gid 1001 app \
   && useradd --system --uid 1001 --gid app \
      --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=app:app \
    /build/target/device-management-service-*.jar \
    /app/app.jar

USER app
EXPOSE 8080

# Allocate up to 75% of container's memory limit to heap
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# use exec so JVM becomes PID 1 and receives container signals directly
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]