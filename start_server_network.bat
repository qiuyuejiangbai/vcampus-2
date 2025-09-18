@echo off
chcp 65001 >nul
echo ========================================
echo vCampus服务器启动脚本 (跨机器网络版)
echo ========================================
echo.

echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境
    pause
    exit /b 1
)

echo 检查编译状态...
if not exist "bin" (
    echo 错误: 项目尚未编译，请先运行 compile.bat
    pause
    exit /b 1
)

echo 复制网络配置文件...
copy resources\config.network.properties bin\config.local.properties >nul 2>&1

echo 启动服务器 (支持跨机器连接)...
echo 端口: 8888
echo 监听: 所有网络接口 (0.0.0.0:8888)
echo 按 Ctrl+C 停止服务器
echo.

java -Dfile.encoding=UTF-8 -cp "bin;libs\mysql-connector-j-8.0.33.jar" server.net.VCampusServer

echo.
echo 服务器已停止
pause
