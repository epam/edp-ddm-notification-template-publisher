FROM eclipse-temurin:11-jre
RUN apt update -y && apt install curl -y
WORKDIR /app
COPY target/notification-template-publisher*.jar app.jar