# ===============================
# Stage 1: Build
# ===============================
FROM gradle:9.1.0-jdk21-alpine AS builder
WORKDIR /app

# 프로젝트 전체 복사
COPY . .
#
RUN chmod +x gradlew

# Gradle 빌드 (테스트 제외)
RUN ./gradlew clean build -x test

# ===============================
# Stage 2: Run
# ===============================
FROM eclipse-temurin:21-jre-alpine-3.22

WORKDIR /app

ARG BUILD_PROFILE=prod
ARG BUILD_PORT=8080

# Build 단계에서 생성된 Jar 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata

# Spring 프로파일 설정
ENV SPRING_PROFILES_ACTIVE=${BUILD_PROFILE}

# 앱 포트
EXPOSE ${BUILD_PORT}

# Spring Boot 실행
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
