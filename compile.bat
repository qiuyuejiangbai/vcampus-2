@echo off
chcp 65001 >nul
echo Compiling vCampus Virtual Campus System...

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
echo Compiling common modules...
javac -cp "libs/*;." -d bin -encoding UTF-8 common/protocol/*.java common/vo/*.java

if %errorlevel% neq 0 (
    echo Common modules compilation failed!
    pause
    exit /b 1
)

echo Compiling server...
javac -cp "libs/*;bin;." -d bin -encoding UTF-8 server/util/*.java server/dao/*.java server/dao/impl/*.java server/service/*.java server/net/*.java

if %errorlevel% neq 0 (
    echo Server compilation failed!
    pause
    exit /b 1
)

echo Compiling client...
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
    echo Copying config files to classpath...
    copy resources\config.properties bin\ >nul 2>&1
    echo.
    echo ================================
    echo Compilation successful!
    echo ================================
    echo Output directory: bin/
    echo.
    echo Next steps:
    echo 1. Ensure MySQL database is configured
    echo 2. Run server: start_server.bat
    echo 3. Run client: start_client.bat
    echo.
    pause
) else (
    echo Client compilation failed! Please check error messages.
    pause
)

