# 1단계: 빌드 환경 설정
FROM gradle:8.5.0-jdk17-jammy AS build

# 빌드할 서비스 이름을 인자로 받음
ARG SERVICE_NAME

WORKDIR /home/gradle/project

# --- Docker 캐시 최적화 부분 시작 ---

# 1. 의존성 변경이 없을 경우 캐시를 활용하기 위해 빌드 파일들만 먼저 복사
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

# 각 모듈의 build.gradle 파일도 미리 복사
COPY ai-adapter-service/build.gradle ./ai-adapter-service/
COPY circuit-breaker-service/build.gradle ./circuit-breaker-service/
COPY core-service/build.gradle ./core-service/
COPY discovery-service/build.gradle ./discovery-service/

# 2. 소스코드를 복사하기 전에 의존성만 먼저 다운로드 (이 레이어는 자주 캐시됨)
# --no-daemon 옵션은 빌드 후 Gradle 데몬이 살아있지 않도록 보장
RUN ./gradlew dependencies --no-daemon

# 3. 이제 전체 소스코드를 복사
COPY . .

# --- Docker 캐시 최적화 부분 끝 ---

# 4. 애플리케이션을 클린 빌드하고 JAR 파일 생성
RUN ./gradlew :${SERVICE_NAME}:clean :${SERVICE_NAME}:bootJar --no-daemon

# 2단계: 최종 실행 환경
FROM eclipse-temurin:17-jre-jammy

ARG SERVICE_NAME

WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=build /home/gradle/project/${SERVICE_NAME}/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
