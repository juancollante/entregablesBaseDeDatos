# Build sin Maven Wrapper (evita depender de .mvn en Git — Render/GitHub).
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -ntp -B -DskipTests package \
    && mv target/logistica-auth-*.jar /app/application.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/application.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
