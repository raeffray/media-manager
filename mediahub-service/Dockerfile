FROM openjdk:17-jdk-alpine

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} mediahub-service.jar

ENTRYPOINT ["java","-jar","/mediahub-service.jar"]