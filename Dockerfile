FROM nexus-docker-registry.apps.cicd2.mdtu-ddm.projects.epam.com/openjdk:11.0.7-jre-slim
RUN apt update -y && apt install curl -y
WORKDIR /app
COPY target/notification-template-publisher*.jar app.jar