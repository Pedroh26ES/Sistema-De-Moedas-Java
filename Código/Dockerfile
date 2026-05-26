FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml package.json package-lock.json vite.config.js ./
COPY frontend ./frontend
COPY src ./src

RUN npm ci
RUN npm run build:frontend
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/quarkus-app/ ./

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]
