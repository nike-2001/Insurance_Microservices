FROM openjdk:21

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} policyservice.jar

ENTRYPOINT ["java", "-jar", "/policyservice.jar"]

EXPOSE 8082