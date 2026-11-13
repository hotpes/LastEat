# 운영 이미지. jar는 빌드 머신 CPU에서 한 번만 만들고(어느 CPU에서나 같음), 실행 이미지만 대상 CPU(arm64 등)로 만든다.
# 그래서 ARM 서버(오라클 Ampere)용 이미지를 만들 때도 Gradle을 에뮬레이션으로 돌리지 않는다. 테스트는 CI가 따로 돌린다.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /src

# 의존성 목록이 그대로면 아래 단계까지 캐시를 재사용한다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon -q > /dev/null

COPY src src
# bootJar만 돌리면 -plain.jar는 만들어지지 않아 jar가 하나다
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon -q \
    && cp build/libs/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /src/app.jar app.jar
USER app

# 컨테이너 메모리 상한(compose의 mem_limit)의 70%까지 힙으로 쓴다. 나머지는 메타스페이스·스레드 등 힙 밖 메모리 몫
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Seoul
EXPOSE 8080
# 이미지에 curl·wget이 없어서 bash로 health를 확인한다
HEALTHCHECK --interval=15s --timeout=3s --start-period=90s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /actuator/health HTTP/1.0\r\n\r\n" >&3 && grep -q "\"UP\"" <&3' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
