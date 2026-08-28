# Этап 1: Сборка JAR
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники и собираем
COPY src src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/book-chat-ast-*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]