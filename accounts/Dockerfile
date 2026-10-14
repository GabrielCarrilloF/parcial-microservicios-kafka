# Etapa 1: compilar el JAR con Maven (igual que el Dockerfile del profe)
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:17-jre

RUN groupadd --system spring && useradd --system --gid spring spring
WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/target/accounts-*.jar app.jar
USER spring:spring

# Render free = 512 MB de RAM: limitar el heap de la JVM
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
