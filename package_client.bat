@echo off
chcp 65001 >nul
echo ========================================
echo vCampus 客户端打包脚本
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
)

echo 创建客户端发布目录...
if exist "client_release" rmdir /s /q "client_release"
mkdir "client_release"
mkdir "client_release\libs"
mkdir "client_release\resources"
mkdir "client_release\config"

echo 复制客户端文件...
REM 复制整个bin目录以保持正确的包结构
xcopy /s /e /y /i "bin" "client_release\bin"
xcopy /s /e /y /i "libs\*.jar" "client_release\libs\"

echo 复制资源文件...
REM 复制所有资源文件夹
xcopy /s /e /y /i "resources\icons" "client_release\resources\icons"
xcopy /s /e /y /i "resources\images" "client_release\resources\images"
xcopy /s /e /y /i "resources\fonts" "client_release\resources\fonts"
xcopy /s /e /y /i "resources\avatars" "client_release\resources\avatars"
xcopy /s /e /y /i "resources\docs" "client_release\resources\docs"
xcopy /s /e /y /i "resources\themes" "client_release\resources\themes"

echo 创建客户端配置文件...
echo # vCampus 客户端配置文件 > "client_release\config\config.local.properties"
echo # 请根据实际服务器地址修改以下配置 >> "client_release\config\config.local.properties"
echo. >> "client_release\config\config.local.properties"
echo # 服务器配置 >> "client_release\config\config.local.properties"
echo # 服务器地址 - 请修改为实际的服务器IP地址 >> "client_release\config\config.local.properties"
echo server.host=localhost >> "client_release\config\config.local.properties"
echo # 服务器端口 - 请确保与服务器端配置一致 >> "client_release\config\config.local.properties"
echo server.port=8888 >> "client_release\config\config.local.properties"
echo. >> "client_release\config\config.local.properties"
echo # 配置示例： >> "client_release\config\config.local.properties"
echo # server.host=192.168.1.100    # 局域网服务器 >> "client_release\config\config.local.properties"
echo # server.host=server.company.com  # 远程服务器 >> "client_release\config\config.local.properties"
echo # server.port=9999             # 自定义端口 >> "client_release\config\config.local.properties"

echo 创建客户端启动脚本...
echo @echo off > "client_release\start_client.bat"
echo chcp 65001 ^>nul >> "client_release\start_client.bat"
echo echo ======================================== >> "client_release\start_client.bat"
echo echo        vCampus 客户端启动器 >> "client_release\start_client.bat"
echo echo ======================================== >> "client_release\start_client.bat"
echo echo. >> "client_release\start_client.bat"
echo echo 正在启动客户端... >> "client_release\start_client.bat"
echo echo 正在读取服务器配置... >> "client_release\start_client.bat"
echo echo. >> "client_release\start_client.bat"
echo java -cp "bin;config;libs/*" client.ui.LoginFrame >> "client_release\start_client.bat"
echo echo. >> "client_release\start_client.bat"
echo echo 客户端已退出 >> "client_release\start_client.bat"
echo pause >> "client_release\start_client.bat"

echo 创建README文件...
echo vCampus 客户端部署说明 > "client_release\README.txt"
echo. >> "client_release\README.txt"
echo 1. 系统要求： >> "client_release\README.txt"
echo    - Java 8 或更高版本 >> "client_release\README.txt"
echo    - Windows 操作系统 >> "client_release\README.txt"
echo. >> "client_release\README.txt"
echo 2. 配置服务器连接： >> "client_release\README.txt"
echo    - 编辑 config\config.local.properties 文件 >> "client_release\README.txt"
echo    - 修改 server.host 为服务器IP地址 >> "client_release\README.txt"
echo    - 修改 server.port 为服务器端口（默认8888） >> "client_release\README.txt"
echo. >> "client_release\README.txt"
echo 3. 启动客户端： >> "client_release\README.txt"
echo    - 双击 start_client.bat 启动客户端 >> "client_release\README.txt"
echo. >> "client_release\README.txt"
echo 4. 常见问题： >> "client_release\README.txt"
echo    - 如果无法连接服务器，请检查网络连接和服务器地址配置 >> "client_release\README.txt"
echo    - 如果启动失败，请确保已安装Java运行环境 >> "client_release\README.txt"
echo. >> "client_release\README.txt"
echo 5. 联系支持： >> "client_release\README.txt"
echo    - 如有问题请联系系统管理员 >> "client_release\README.txt"

echo.
echo ========================================
echo 客户端打包完成！
echo ========================================
echo 发布目录: client_release\
echo.
echo 部署说明:
echo 1. 将 client_release 文件夹复制到目标机器
echo 2. 编辑 config\config.local.properties 配置服务器地址
echo 3. 运行 start_client.bat 启动客户端
echo.
echo 配置文件位置: client_release\config\config.local.properties
echo 启动脚本位置: client_release\start_client.bat
echo.
echo 打包完成，按任意键退出...
pause >nul
