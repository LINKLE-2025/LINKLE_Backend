# 1단계: Build stage (Gradle)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle wrapper 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

# 권한 부여 (Linux에서 gradlew 실행 가능하도록)
RUN chmod +x gradlew

# 빌드 실행 (테스트는 생략)
RUN ./gradlew clean bootJar -x test

# 2단계: Runtime stage (JRE로 실행)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# prod 프로파일로 실행
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
