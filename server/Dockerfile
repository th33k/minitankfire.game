# Stage 1: Build
FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose the default port
EXPOSE 8080

# Run the server
CMD ["java", "-jar", "app.jar"]
