#!/bin/bash

echo "================================="
echo " vCampus 数据库配置向导"
echo "================================="
echo

echo "请确保您已经："
echo "1. 安装了 MySQL 8.0 或以上版本"
echo "2. MySQL 服务正在运行"
echo "3. 知道 MySQL 的 root 密码"
echo

# 读取 MySQL 密码
read -s -p "请输入 MySQL root 密码: " mysql_password
echo
echo "正在创建数据库和表..."
echo

echo "正在删除现有数据库（如果存在）..."
mysql -u root -p"$mysql_password" -e "DROP DATABASE IF EXISTS vcampus;" 2>/dev/null

echo "正在创建新数据库..."
mysql -u root -p"$mysql_password" --default-character-set=utf8mb4 < resources/db/init.sql
if [ $? -eq 0 ]; then
    echo "✓ 数据库表创建成功"
    echo
    echo "正在插入初始数据..."

    mysql -u root -p"$mysql_password" --default-character-set=utf8mb4 < resources/db/data.sql
    if [ $? -eq 0 ]; then
        echo "✓ 初始数据插入成功"
        echo
        echo "================================="
        echo " 数据库配置完成！"
        echo "================================="
        echo
        echo "默认测试账户："
        echo "管理员: admin / admin123"
        echo "教师:   T001  / teacher123"
        echo "学生:   2021001 / student123"
        echo
        echo "下一步: 运行 ./start_server.sh 启动服务器"
    else
        echo "✗ 初始数据插入失败"
        echo "请检查 MySQL 连接和权限"
    fi
else
    echo "✗ 数据库创建失败"
    echo "请检查："
    echo "1. MySQL 服务是否正在运行"
    echo "2. 密码是否正确"
    echo "3. root 用户是否有创建数据库的权限"
fi

echo
