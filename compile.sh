#!/bin/bash

echo "正在编译 vCampus 虚拟校园系统..."
echo

# 检查 Java 版本
java -version
echo

# 创建输出目录
if [ ! -d "bin" ]; then
    mkdir bin
fi

# 检查 MySQL 驱动是否存在
if [ ! -f "libs/mysql-connector-j-8.0.33.jar" ]; then
    echo "警告: 未找到 MySQL 驱动文件"
    echo "请将 mysql-connector-j-8.0.33.jar 放入 libs/ 目录"
    echo "继续编译..."
    echo
fi

# 编译通用模块
echo "编译通用模块..."
javac -cp "libs/*:." -d bin -encoding UTF-8 common/protocol/*.java common/vo/*.java
if [ $? -ne 0 ]; then
    echo "通用模块编译失败！"
    exit 1
fi

# 编译服务器端
echo "编译服务器端..."
javac -cp "libs/*:bin:." -d bin -encoding UTF-8 server/util/*.java server/dao/*.java server/dao/impl/*.java server/service/*.java server/net/*.java
if [ $? -ne 0 ]; then
    echo "服务器端编译失败！"
    exit 1
fi

# 编译客户端
echo "编译客户端..."
# 注意：使用 find 递归所有 client/ui 下的 .java 文件
javac -cp "libs/*:bin:." -d bin -encoding UTF-8 \
    client/net/*.java \
    client/controller/*.java \
    $(find client/ui -name "*.java")
if [ $? -ne 0 ]; then
    echo "客户端编译失败！请检查错误信息。"
    exit 1
fi

# 复制配置文件
echo
echo "复制配置文件到 classpath..."
cp resources/config.properties bin/ >/dev/null 2>&1

echo
echo "================================"
echo " 编译成功！"
echo "================================"
echo "编译输出目录: bin/"
echo
echo "下一步："
echo "1. 确保 MySQL 数据库已配置"
echo "2. 运行服务器: ./start_server.sh"
echo "3. 运行客户端: ./start_client.sh"
echo
