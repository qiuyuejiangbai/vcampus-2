# vCampus 虚拟校园系统

基于Java Swing和MySQL的C/S架构虚拟校园系统。

## 快速启动

### 环境要求
- Java 8 或以上
- MySQL 8.0 或以上
- Windows 操作系统

### 启动步骤

**1. 安装MySQL**
- 下载并安装 MySQL 8.0: https://dev.mysql.com/downloads/mysql/
- 记住设置的root密码

**2. 配置数据库**
```bash
setup_database.bat
```
按提示输入MySQL root密码，脚本将自动：
- 删除现有的vcampus数据库（如果存在）
- 创建新的数据库和表结构
- 插入测试数据

**3. 编译并启动**
```bash
# 编译项目
compile.bat

# 启动服务器（必须先启动）
start_server.bat

# 启动客户端
start_client.bat
```

## 测试账户

| 角色 | 登录ID | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | T001 | teacher123 |
| 学生 | 2021001 | student123 |

## 故障排除

**常见问题:**
1. **服务器闪退**: 检查MySQL服务是否启动
2. **编译失败**: 确保Java环境正确安装  
3. **连接失败**: 确认数据库配置和端口8888可用
4. **驱动缺失**: 确保`libs/mysql-connector-j-8.0.33.jar`存在
5. **数据库错误**: 重新运行`setup_database.bat`会自动重置数据库

## 项目结构

```
vCampus/
├── client/           # 客户端源码
├── server/           # 服务器端源码  
├── common/           # 共享模块
├── resources/        # 配置文件和数据库脚本
├── libs/             # MySQL驱动
├── bin/              # 编译输出
├── compile.bat       # 编译脚本
├── setup_database.bat # 数据库配置脚本
├── start_server.bat  # 服务器启动脚本
├── start_client.bat  # 客户端启动脚本
└── README.md         # 本文档
```
