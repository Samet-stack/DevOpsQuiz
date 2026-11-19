FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY src/main/java ./src/main/java
RUN mkdir -p build/classes && \
    javac -encoding UTF-8 -d build/classes $(find src/main/java -name "*.java")

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/classes ./build/classes

RUN mkdir -p /app/data /app/logs
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-cp", "build/classes", "DevOpsQuiz"]
