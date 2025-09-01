@echo off
chcp 65001 >nul
echo =================================
echo vCampus 开发环境配置向导
echo =================================
echo.

echo 欢迎加入vCampus开发团队！
echo 此脚本将帮助你快速配置开发环境
echo.

echo 请确保你已经：
echo 1. 安装了Java 8或以上版本
echo 2. 安装了MySQL 8.0或以上版本
echo 3. 安装了Git
echo 4. 克隆了项目代码
echo.

set /p continue=按任意键继续，或输入q退出: 
if /i "%continue%"=="q" exit /b

echo.
echo =================================
echo 步骤1: 配置本地数据库
echo =================================

set /p db_name=请输入你的数据库名称（建议：vcampus_dev_你的姓名）: 
set /p mysql_password=请输入MySQL root密码: 
set /p developer_name=请输入你的姓名: 
set /p server_port=请输入服务器端口（默认8888，如有冲突请修改）: 

if "%server_port%"=="" set server_port=8888

echo.
echo 正在创建数据库 %db_name%...
mysql -u root -p%mysql_password% -e "CREATE DATABASE IF NOT EXISTS %db_name% DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %errorlevel% == 0 (
    echo ✓ 数据库创建成功
) else (
    echo ✗ 数据库创建失败，请检查MySQL连接
    pause
    exit /b
)

echo.
echo =================================
echo 步骤2: 创建本地配置文件
echo =================================

echo 正在生成本地配置文件...

(
echo # vCampus 本地开发配置 - %developer_name%
echo # 生成时间: %date% %time%
echo.
echo # 数据库配置
echo db.url=jdbc:mysql://localhost:3306/%db_name%?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai^&allowPublicKeyRetrieval=true
echo db.username=root
echo db.password=%mysql_password%
echo db.driver=com.mysql.cj.jdbc.Driver
echo.
echo # 连接池配置
echo db.pool.initialSize=5
echo db.pool.maxActive=20
echo db.pool.maxIdle=10
echo db.pool.minIdle=5
echo db.pool.maxWait=60000
echo.
echo # 服务器配置
echo server.port=%server_port%
echo server.host=localhost
echo.
echo # 文件上传配置
echo file.upload.path=uploads/
echo file.max.size=10485760
echo.
echo # 日志配置
echo log.level=DEBUG
echo log.file=logs/vcampus-dev-%developer_name%.log
echo.
echo # 系统配置
echo system.name=vCampus虚拟校园系统（%developer_name%开发版）
echo system.version=1.0.0-dev
echo system.author=%developer_name%
echo.
echo # 业务规则配置
echo borrow.max.books=10
echo borrow.days=7
echo student.max.credits=30
echo.
echo # 开发者标识
echo developer.name=%developer_name%
echo developer.id=%db_name%
) > resources\config.local.properties

echo ✓ 本地配置文件创建成功

echo.
echo =================================
echo 步骤3: 初始化数据库表结构
echo =================================

echo 正在创建数据库表...
mysql -u root -p%mysql_password% %db_name% < resources/db/init.sql

if %errorlevel% == 0 (
    echo ✓ 数据库表创建成功
    
    echo.
    echo 正在插入测试数据...
    mysql -u root -p%mysql_password% %db_name% < resources/db/data.sql
    
    if %errorlevel% == 0 (
        echo ✓ 测试数据插入成功
    ) else (
        echo ⚠ 测试数据插入失败，但不影响开发
    )
) else (
    echo ✗ 数据库表创建失败
    pause
    exit /b
)

echo.
echo =================================
echo 步骤4: 创建必要的目录
echo =================================

if not exist "logs" mkdir logs
if not exist "uploads" mkdir uploads
echo ✓ 目录创建完成

echo.
echo =================================
echo 步骤5: Git配置检查
echo =================================

for /f "tokens=*" %%i in ('git config user.name 2^>nul') do set git_name=%%i
for /f "tokens=*" %%i in ('git config user.email 2^>nul') do set git_email=%%i

if "%git_name%"=="" (
    set /p git_name=请输入Git用户名: 
    git config user.name "%git_name%"
)

if "%git_email%"=="" (
    set /p git_email=请输入Git邮箱: 
    git config user.email "%git_email%"
)

echo ✓ Git配置完成
echo   用户名: %git_name%
echo   邮箱: %git_email%

echo.
echo =================================
echo 配置完成！
echo =================================
echo.
echo 你的开发环境配置：
echo 📊 数据库: %db_name%
echo 🚀 服务器端口: %server_port%
echo 👤 开发者: %developer_name%
echo 📁 配置文件: resources\config.local.properties
echo.
echo 下一步操作：
echo 1. 运行 compile.bat 编译项目
echo 2. 运行 start_server.bat 启动服务器
echo 3. 运行 start_client.bat 启动客户端
echo.
echo 测试账户：
echo 管理员: admin / admin123
echo 教师:   T001  / teacher123
echo 学生:   2021001 / student123
echo.
echo Git分支操作：
echo git checkout develop                    # 切换到开发分支
echo git checkout -b feature/your-module    # 创建功能分支
echo git add . ^&^& git commit -m "提交信息"  # 提交代码
echo git push origin feature/your-module    # 推送到远程
echo.
echo 如有问题请联系项目负责人或查看文档：
echo - 数据库配置与Git协作指南.md
echo - vCampus模块开发指导.md
echo.

pause
