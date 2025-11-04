#!/bin/bash

# 确保在mini12306-api目录下
cd /root/mini12306/mini12306-api

# 编译项目（跳过测试）
echo "正在编译项目..."
mvn clean package -DskipTests

# 如果编译成功，启动应用
if [ $? -eq 0 ]; then
    echo "编译成功，正在启动应用..."
    java -jar target/mini12306-api-0.0.1-SNAPSHOT.jar
else
    echo "编译失败，请检查错误信息"
    exit 1
fi
