@echo off
chcp 65001 >nul

echo ===================================================
echo              vCampus 一键编译运行脚本
 echo ===================================================
echo.

REM 1. 首先执行编译脚本
echo [1/3] 开始编译项目...
echo.
call compile.bat
if %errorlevel% neq 0 (
    echo 编译失败！请检查错误信息。
    pause
    exit /b 1
)
echo.

REM 2. 编译成功后，启动服务器
echo [2/3] 启动服务器...
echo 注意：服务器将在新窗口中启动，请勿关闭该窗口！
echo.
start "vCampus Server" start_server.bat

REM 3. 等待服务器启动完成
echo 等待3秒，确保服务器完全启动...
ping -n 4 127.0.0.1 >nul
echo.

REM 4. 启动客户端
echo [3/3] 启动客户端...
echo.
start "vCampus Client" start_client.bat

echo.
echo ===================================================
echo 已成功启动编译和运行流程！
echo 1. 服务器已在新窗口启动
 echo 2. 客户端已在新窗口启动
 echo ===================================================
echo 按任意键退出此窗口...
pause >nul
