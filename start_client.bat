@echo off
chcp 65001 >nul
echo 启动vCampus客户端...
echo.

REM 检查是否已编译
if not exist "bin" (
    echo 错误: 项目尚未编译
    echo 请先运行: compile_without_mysql.bat 或 compile.bat
    pause
    exit /b
)

echo 正在启动客户端...
echo 默认服务器: localhost:8888
echo.

REM 启动客户端（将 resources 也加入 classpath 以加载图标等资源）
java -cp "bin;resources;libs/*" client.ui.LoginFrame

pause
