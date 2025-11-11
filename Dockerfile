# 使用官方Maven镜像作为构建环境
FROM maven:3.9.6-amazoncorretto-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 使用轻量级OpenJDK运行时镜像
FROM amazoncorretto:17-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]