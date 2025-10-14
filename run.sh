#!/bin/bash

# AI Report 启动脚本
# 使用指定的 JDK 17 运行项目

# 设置 JAVA_HOME
export JAVA_HOME=/Users/pidan-l/soft/jdk-17.0.12.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "========================================="
echo "AI Report 启动脚本"
echo "========================================="
echo "使用 JDK: $JAVA_HOME"
echo "Java 版本:"
$JAVA_HOME/bin/java -version
echo "========================================="
echo ""

# 运行 Maven 命令
if [ "$1" = "clean" ]; then
    echo "清理并编译项目..."
    mvn clean compile
elif [ "$1" = "package" ]; then
    echo "打包项目..."
    mvn clean package
elif [ "$1" = "run" ]; then
    echo "启动应用..."
    mvn spring-boot:run
else
    echo "启动应用..."
    mvn spring-boot:run
fi

