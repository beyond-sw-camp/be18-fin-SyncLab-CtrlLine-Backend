# ===============================
# Stage 1: Build
# ===============================
FROM gradle:9.1.0-jdk21-alpine AS builder
WORKDIR /app

COPY . .
RUN chmod +x gradlew

# Gradle build (skip tests)
RUN ./gradlew clean build -x test

# ===============================
# Stage 2: Run
# ===============================
FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG BUILD_PROFILE=staging-prod
ARG BUILD_PORT=8088

# ---- tzdata 설치 (Debian) ----
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Seoul

RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -snf "/usr/share/zoneinfo/${TZ}" /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

# Build 단계에서 생성된 Jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Profile 설정
ENV SPRING_PROFILES_ACTIVE=${BUILD_PROFILE}

# Expose port
EXPOSE ${BUILD_PORT}

# Entry point
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
