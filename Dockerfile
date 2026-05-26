FROM openjdk:8-jre-slim
WORKDIR /app
COPY target/im-system-1.0.0.jar app.jar
COPY data/ /app/data/
COPY uploads/ /app/uploads/
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--server.port=${PORT:8080}"]
