# ---- compilacion ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo lo que define las dependencias: la capa se reutiliza mientras no cambie build.gradle
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && sh gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN sh gradlew --no-daemon bootJar -x test

# ---- ejecucion ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 tienda
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar
USER tienda
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
