FROM openjdk:21

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} claimservice.jar

ENTRYPOINT ["java", "-jar", "/claimservice.jar"]

EXPOSE 8084