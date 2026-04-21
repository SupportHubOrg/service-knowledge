#Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

#Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

#Copy source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

#Rename the output jar
RUN mv target/*.jar target/knowledge.jar

#Stage 2: Final Runtime Image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/knowledge.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]