#!/bin/bash

PORT=8888

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

# 检查端口是否占用
PID=$(lsof -ti:$PORT)
if [ -n "$PID" ]; then
    echo "检测到端口 $PORT 已被占用，正在终止旧进程 (PID=$PID)..."
    kill -9 $PID
    echo "旧进程已终止。"
fi

echo "启动服务器..."
echo "端口: $PORT"
echo "按 Ctrl+C 停止服务器"
echo

# 启动服务器
java -Dfile.encoding=UTF-8 -cp "bin:libs/mysql-connector-j-8.0.33.jar" server.net.VCampusServer

echo
echo "服务器已停止"
