# Start with an OpenJDK base image
FROM openjdk:17

# Copy the Spring Boot JAR file into the container
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8081

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app.jar"]
