FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

COPY common common
COPY notification-app notification-app

RUN mkdir prioritas

RUN chmod +x gradlew

RUN ./gradlew :notification-app:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre AS extractor
WORKDIR /build
COPY --from=builder /build/notification-app/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=extractor /build/dependencies/ ./
COPY --from=extractor /build/spring-boot-loader/ ./
COPY --from=extractor /build/snapshot-dependencies/ ./
COPY --from=extractor /build/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]