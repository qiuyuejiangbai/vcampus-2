#!/bin/bash

./compile.sh

# 后台启动服务器，并把输出写入日志
./start_server.sh > server.log 2>&1 &

# 等待一下，确保服务器启动完成
sleep 1

# 启动客户端
./start_client.sh
