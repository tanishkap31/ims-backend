# Use OpenJDK image
FROM openjdk:17-jdk

# Set working directory
WORKDIR /app

# Copy JAR file from target folder
COPY target/*.jar app.jar

# Expose the Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
