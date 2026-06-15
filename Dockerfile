#Stage 1: Build environment
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

#Copy the pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

#Copy source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

#Rename the output jar for consistency
RUN mv target/*.jar target/knowledge.jar

#Stage 2: Final Runtime Image
FROM eclipse-temurin:17-jre-alpine AS final
WORKDIR /app

#Create a restricted non-root user specifically for the Spring Boot application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

#Copy the compiled executable JAR from the build stage
COPY --from=build /app/target/knowledge.jar app.jar

#Expose the standard port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]