@echo off 
chcp 65001 >nul 
echo ======================================== 
echo        vCampus 客户端启动器 
echo ======================================== 
echo. 
echo 正在启动客户端... 
echo 正在读取服务器配置... 
echo. 
java -cp "bin;config;libs/*" client.ui.LoginFrame 
echo. 
echo 客户端已退出 
pause 
