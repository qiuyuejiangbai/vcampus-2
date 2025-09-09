@echo off
chcp 65001 >nul
echo 正在编译vCampus虚拟校园系统...

REM 检查Java版本
java -version
echo.

REM 创建输出目录
if not exist "bin" mkdir bin

REM 检查MySQL驱动是否存在
if not exist "libs\mysql-connector-j-8.0.33.jar" (
    echo 警告: 未找到MySQL驱动文件
    echo 请将 mysql-connector-j-8.0.33.jar 放入 libs\ 目录
    echo 继续编译...
    echo.
)

REM 检查 FlatLaf 是否存在
if not exist "libs\flatlaf-3.4.1.jar" (
    echo 警告: 未找到 flatlaf-3.4.1.jar
    echo 请从 https://search.maven.org/artifact/com.formdev/flatlaf 下载对应版本放入 libs\
)

REM 编译Java源文件（分步编译以便定位错误）
echo 编译通用模块...
javac -cp "libs/*;." -d bin -encoding UTF-8 common/protocol/*.java common/vo/*.java

if %errorlevel% neq 0 (
    echo 通用模块编译失败！
    pause
    exit /b 1
)

echo 编译服务器端...
javac -cp "libs/*;bin;." -d bin -encoding UTF-8 server/util/*.java server/dao/*.java server/dao/impl/*.java server/service/*.java server/net/*.java

if %errorlevel% neq 0 (
    echo 服务器端编译失败！
    pause
    exit /b 1
)

echo 编译客户端...
javac -cp "libs/*;bin;." -d bin -encoding UTF-8 ^
  client/net/*.java ^
  client/controller/*.java ^
  client/ui/*.java ^
  client/ui/util/*.java ^
  client/ui/dashboard/*.java ^
  client/ui/modules/*.java ^
  client/ui/integration/*.java ^
  client/ui/api/*.java

if %errorlevel% == 0 (
    echo.
    echo 复制配置文件到classpath...
    copy resources\config.properties bin\ >nul 2>&1
    echo.
    echo ================================
    echo 编译成功！
    echo ================================
    echo 编译输出目录: bin/
    echo.
    echo 下一步：
    echo 1. 确保MySQL数据库已配置
    echo 2. 运行服务器: start_server.bat
    echo 3. 运行客户端: start_client.bat
    echo.
    pause
) else (
    echo 客户端编译失败！请检查错误信息。
    pause
)

