FROM maven:4.0.0-rc-5-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -ntp -B -DskipTests package \
    && mv target/logistica-seguimiento-*.jar /app/application.jar

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/application.jar application.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "application.jar"]
