# Java 8 — matches local JDK 8; Spring Boot 2.7.x supports 8 through 19.
FROM eclipse-temurin:8-jdk AS builder
WORKDIR /workspace

COPY gradlew ./
COPY gradle gradle
COPY gradle.properties build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew --version \
    && ./gradlew clean bootJar -x test -x check --no-daemon -Pproduction

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/application.jar /app/application.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/application.jar"]
