#!/bin/bash

echo "========================================"
echo " vCampus 服务器启动脚本"
echo "========================================"
echo

echo "检查 Java 环境..."
java -version >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "错误: 未找到 Java 环境"
    exit 1
fi

echo "检查编译状态..."
if [ ! -d "bin" ]; then
    echo "错误: 项目尚未编译，请先运行 compile.sh"
    exit 1
fi

echo "复制配置文件..."
cp resources/config.properties bin/ >/dev/null 2>&1

echo "启动服务器..."
echo "端口: 8888"
echo "按 Ctrl+C 停止服务器"
echo

java -Dfile.encoding=UTF-8 -cp "bin:libs/mysql-connector-j-8.0.33.jar" server.net.VCampusServer

echo
echo "服务器已停止"
