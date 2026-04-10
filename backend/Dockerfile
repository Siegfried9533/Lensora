# 1. Build mã nguồn bằng Maven và Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy file cấu hình Maven để tận dụng cache (giúp build nhanh hơn ở các lần sau)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ mã nguồn vào và build file JAR
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Chạy ứng dụng với JRE gọn nhẹ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file JAR đã build từ giai đoạn 1 sang giai đoạn 2
# Tên file JAR mặc định thường là artifactId-version.jar
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Mở port 8080
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]