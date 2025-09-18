@echo off
chcp 65001 >nul 2>&1
echo Compiling vCampus Virtual Campus System...
echo.

REM 检查并设置Java版本
if exist "C:\Java\current" (
    set /p current_java=<"C:\Java\current"
    echo 使用Java版本: %current_java%
    set "JAVA_HOME=C:\Java\versions\%current_java%"
    set "PATH=C:\Java\versions\%current_java%\bin;%PATH%"
    echo Java路径: %JAVA_HOME%
    echo.
) else (
    echo 警告: 未检测到Java版本管理器设置
    echo 使用系统默认Java版本
    echo.
)

REM 检查Java版本
echo 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: Java未正确安装或配置
    echo 请检查JAVA_HOME和PATH环境变量
    pause
    exit /b 1
)
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
echo ========================================
echo 开始编译 common 模块...
echo ========================================
javac -cp "libs/*;." -d bin -encoding UTF-8 common/protocol/*.java common/vo/*.java

if %errorlevel% neq 0 (
    echo.
    echo 错误: Common 模块编译失败!
    echo 请检查 common/protocol/ 和 common/vo/ 目录下的Java文件
    echo.
    pause
    exit /b 1
) else (
    echo Common 模块编译成功!
)
echo.

echo ========================================
echo 开始编译 server 模块...
echo ========================================
javac -cp "libs/*;bin;." -d bin -encoding UTF-8 server/util/*.java server/dao/*.java server/dao/impl/*.java server/service/*.java server/net/*.java

if %errorlevel% neq 0 (
    echo.
    echo 错误: Server 模块编译失败!
    echo 请检查 server/ 目录下的Java文件
    echo.
    pause
    exit /b 1
) else (
    echo Server 模块编译成功!
)
echo.

echo ========================================
echo 开始编译 client 模块...
echo ========================================
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
    echo Client 模块编译成功!
    echo.
    echo.
    echo Copying config files to classpath...
    copy resources\config.local.properties bin\ >nul 2>&1
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
    exit /b 0
) else (
    echo.
    echo 错误: Client 模块编译失败!
    echo 请检查 client/ 目录下的Java文件
    echo 查看上方的错误信息以获取详细信息
    echo.
    pause
    exit /b 1
)

