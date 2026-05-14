# Build aşaması: Maven ve Java 21 ile projeyi derle
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Önce sadece pom.xml kopyala → bağımlılıklar cache'den gelir, build hızlanır
COPY pom.xml .
COPY src ./src

# Projeyi derle, testleri atla
RUN mvn clean package -DskipTests

# Run aşaması: Maven artık gerekmiyor, sadece JRE yeterli → image boyutu küçülür
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Build aşamasında oluşan .jar dosyasını kopyala
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]