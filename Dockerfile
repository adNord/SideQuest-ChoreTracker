FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21
WORKDIR /

COPY --from=build /app/target/ChoreTracker-0.0.1-SNAPSHOT.jar /choretracker.jar
EXPOSE 8080
CMD ["java", "-jar", "/choretracker.jar"]