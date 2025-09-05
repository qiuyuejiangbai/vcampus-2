#!/bin/bash
# 启动 vCampus 客户端

echo "启动 vCampus 客户端..."
echo

# 检查是否已编译
if [ ! -d "bin" ]; then
    echo "错误: 项目尚未编译"
    echo "请先运行: compile_without_mysql.sh 或 compile.sh"
    exit 1
fi

echo "正在启动客户端..."
echo "默认服务器: localhost:8888"
echo

# 启动客户端（将 resources 也加入 classpath 以加载图标等资源）
java -cp "bin:resources:libs/*" client.ui.LoginFrame
