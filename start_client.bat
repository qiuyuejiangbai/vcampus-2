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
echo 默认服务器: localhost:8888
echo.

java -cp "bin;libs/*" client.ui.LoginFrame
REM 启动客户端（将 resources 也加入 classpath 以加载图标等资源）
java -cp "bin;resources;libs/*" client.ui.LoginFrame

echo.
echo 客户端已退出
pause
