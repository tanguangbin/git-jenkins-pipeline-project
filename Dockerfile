FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

#SPRING_PROFILES_ACTIVE 项目中的 dev prod test配置文件
#SERVER_PORT 项目中的 dev prod test配置文件 不同的端口
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${SERVER_PORT}", "-jar", "/app.jar"]



## Start with an OpenJDK base image
#FROM openjdk:17
#
## Copy the Spring Boot JAR file into the container
#COPY target/*.jar app.jar
#
## Expose the port your Spring Boot app runs on
#EXPOSE 8081
#
## Command to run the Spring Boot application
#ENTRYPOINT ["java", "-jar", "/app.jar"]
