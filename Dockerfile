FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY resources ./resources
COPY web-tests ./web-tests
RUN mvn -B package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 rpg && chown rpg /app
COPY --from=build /app/target/rpg-game-4.1.0.jar app.jar
USER rpg
ENV BIND_ADDRESS=0.0.0.0
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=65","-jar","app.jar"]
