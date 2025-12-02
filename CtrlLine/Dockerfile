# ==========================================
# Stage 1 — Builder
# ==========================================
FROM gradle:8.7.0-jdk21 as builder

WORKDIR /app

# 캐시 최적화를 위해 의존성 먼저 가져오기
COPY build.gradle settings.gradle gradlew ./ 
COPY gradle ./gradle

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# 프로젝트 전체 복사
COPY . .

# 테스트 생략하고 빌드
RUN ./gradlew clean bootJar -x test --no-daemon


# ==========================================
# Stage 2 — Runtime
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

ARG BUILD_PROFILE=prod
ARG PORT=8080

# Copy jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Timezone 설정
ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=${BUILD_PROFILE}

EXPOSE ${PORT}

# CMD 사용해 signal 처리 안전하게
ENTRYPOINT ["java", "-jar", "app.jar"]
