# 빌드 단계
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Gradle 래퍼 및 설정 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 의존성만 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN adduser -D -u 1000 appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
