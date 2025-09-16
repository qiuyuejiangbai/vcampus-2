@echo off
chcp 65001 >nul
echo ========================================
echo        vCampus 客户端启动器
echo ========================================
echo.

REM 检查是否已编译
if not exist "bin" (
    echo 项目尚未编译，正在编译...
    call compile.bat
    if errorlevel 1 (
        echo 编译失败，请检查错误信息
        pause
        exit /b 1
    )
    echo 编译完成！
    echo.
) else (
    echo 项目已编译，跳过编译步骤...
    echo.
)

echo 正在启动客户端...
echo 正在读取服务器配置...
echo.

REM 启动客户端（正常显示）
java -cp "bin;resources;libs/*" client.ui.LoginFrame

echo.
echo 客户端已退出
pause
