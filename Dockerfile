FROM maven:3.9.16-eclipse-temurin-8 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /build/target/im-system-1.0.0.jar /app/app.jar
RUN mkdir -p /app/data /app/uploads
EXPOSE 8080
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080}"]
