# Stage 1: Build the application using maven caching
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and resolve dependencies to cache them
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the package
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create a secure, lightweight runtime image
FROM eclipse-temurin:21-jre

# Create a system group and user to run the app securely as non-root
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/route-optimizer-0.0.1-SNAPSHOT.jar app.jar

# Run the container under the non-privileged user account
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]