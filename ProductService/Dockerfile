FROM openjdk:21

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} productservice.jar

ENTRYPOINT ["java", "-jar", "/productservice.jar"]

EXPOSE 8083