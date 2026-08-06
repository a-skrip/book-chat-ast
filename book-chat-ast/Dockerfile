FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/book-chat-ast-1.0-SNAPSHOT.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]