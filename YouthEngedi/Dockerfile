FROM maven:3.9.9-eclipse-temurin-22 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:22-jdk

WORKDIR /app

COPY --from=build /app/target/youth-engedi-app.jar youth-engedi-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "youth-engedi-app.jar"]