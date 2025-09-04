# vCampus 虚拟校园系统

基于Java Swing和MySQL的C/S架构虚拟校园系统，采用FlatLaf现代化UI设计。

## ✨ 系统特性

- **现代化UI**: 使用FlatLaf库提供现代化的界面设计
- **模块化架构**: 七大核心模块独立开发，便于团队协作
- **C/S架构**: 客户端-服务器分离，支持多用户并发访问
- **MySQL数据库**: 稳定可靠的数据存储解决方案

## 🚀 快速启动

### 环境要求
- Java 8 或以上版本
- MySQL 8.0 或以上版本
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

## 👤 测试账户

| 角色 | 登录ID | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | T001 | teacher123 |
| 学生 | 2021001 | student123 |

## 📁 项目结构

```
vCampus/
├── client/           # 客户端源码
│   ├── controller/   # 控制器层
│   ├── net/         # 网络通信层
│   └── ui/          # 用户界面层
├── server/           # 服务器端源码
│   ├── dao/         # 数据访问层
│   ├── net/         # 网络服务层
│   ├── service/     # 业务逻辑层
│   └── util/        # 工具类
├── common/           # 共享模块
│   ├── protocol/    # 通信协议
│   └── vo/          # 数据传输对象
├── resources/        # 配置文件和数据库脚本
│   ├── config.properties      # 系统配置
│   └── db/                   # 数据库脚本
├── libs/             # 第三方依赖库
│   ├── flatlaf-3.4.1.jar    # FlatLaf UI库
│   └── mysql-connector-j-8.0.33.jar  # MySQL驱动
├── bin/              # 编译输出目录
├── compile.bat       # 编译脚本
├── setup_database.bat # 数据库配置脚本
├── start_server.bat  # 服务器启动脚本
├── start_client.bat  # 客户端启动脚本
└── README.md         # 项目说明文档
```

## 🎨 FlatLaf UI 主题

系统使用FlatLaf库提供现代化的界面外观，支持多种主题：

- **FlatLightLaf** - 亮色主题（默认）
- **FlatDarkLaf** - 暗色主题
- **FlatIntelliJLaf** - IntelliJ主题
- **FlatDarculaLaf** - Darcula主题

可在 `LoginFrame.java` 中修改主题设置。

## 🗄️ 数据库配置指南

### 本地开发环境配置

**1. 复制配置文件模板**
```bash
cp resources/config.properties resources/config.local.properties
```

**2. 修改本地配置**
编辑 `resources/config.local.properties`：
```properties
# 数据库配置
db.url=jdbc:mysql://localhost:3306/vcampus_dev?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=你的MySQL密码
db.driver=com.mysql.cj.jdbc.Driver

# 服务器配置
server.port=8888
server.host=localhost

# 文件上传路径
file.upload.path=uploads/
```

**3. 数据库命名规范**
```sql
-- 开发环境
vcampus_dev          -- 主开发数据库
vcampus_dev_张三     -- 个人开发数据库
vcampus_test         -- 测试数据库

-- 生产环境
vcampus              -- 生产数据库
```

## 🌳 Git协作流程

### 分支策略

采用 **Git Flow** 工作流：

```
main                 # 生产环境分支（受保护）
├── develop         # 开发主分支（受保护）
├── release/*       # 发布分支
├── hotfix/*        # 紧急修复分支
└── feature/*       # 功能开发分支
    ├── feature/user-management      # 用户管理模块
    ├── feature/student-management   # 学籍管理模块
    ├── feature/course-management    # 教务管理模块
    ├── feature/library-management   # 图书馆模块
    ├── feature/store-management     # 商店模块
    ├── feature/forum-management     # 论坛模块
    └── feature/file-management      # 文件资源模块
```

### 开发工作流程

**1. 创建功能分支**
```bash
# 从develop创建新的功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-management

# 推送分支到远程
git push -u origin feature/user-management
```

**2. 开发过程**
```bash
# 添加文件到暂存区
git add .

# 提交更改（遵循提交信息规范）
git commit -m "feat: 添加用户管理主面板UI组件"

# 推送到远程分支
git push origin feature/user-management
```

**3. 提交信息规范**
```bash
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具链相关
```

**4. 合并流程**
```bash
# 功能完成后合并到develop
git checkout develop
git pull origin develop
git merge feature/user-management
git push origin develop

# 删除功能分支
git branch -d feature/user-management
git push origin --delete feature/user-management
```

## 🏗️ 系统架构

### 技术栈
- **前端**: Java Swing + FlatLaf UI
- **后端**: Java Socket编程
- **数据库**: MySQL 8.0
- **通信协议**: 自定义消息协议
- **架构模式**: MVC + 三层架构

### 七大核心模块

| 模块 | 功能描述 | 开发优先级 |
|------|----------|------------|
| 👤 用户管理 | 用户注册、登录、权限管理 | 高 |
| 🎓 学籍管理 | 学生信息、成绩管理 | 高 |
| 📚 教务管理 | 课程管理、选课系统 | 高 |
| 📖 图书馆 | 图书借阅、归还管理 | 中 |
| 🛒 商店 | 商品管理、订单系统 | 中 |
| 💬 论坛 | 校园论坛、讨论交流 | 低 |
| 📁 文件资源 | 文件上传、下载管理 | 低 |

## ⚠️ 开发注意事项

### 冲突预防策略

**1. MessageType编号分配**
```java
public enum MessageType {
    // 用户管理模块 (1-30)
    LOGIN_REQUEST(1),
    REGISTER_REQUEST(2),
    
    // 学籍管理模块 (31-60)
    GET_STUDENT_INFO_REQUEST(31),
    UPDATE_STUDENT_REQUEST(32),
    
    // 教务管理模块 (61-90)
    GET_ALL_COURSES_REQUEST(61),
    ENROLL_COURSE_REQUEST(62),
    
    // 图书馆模块 (91-120)
    SEARCH_BOOK_REQUEST(91),
    BORROW_BOOK_REQUEST(92),
    
    // 商店模块 (121-150)
    GET_STORE_ITEMS_REQUEST(121),
    PURCHASE_REQUEST(122),
    
    // 论坛模块 (151-180)
    GET_ALL_THREADS_REQUEST(151),
    CREATE_THREAD_REQUEST(152),
    
    // 文件资源模块 (181-210)
    FILE_UPLOAD_REQUEST(181),
    FILE_DOWNLOAD_REQUEST(182);
}
```

**2. 数据库表修改规范**
- ✅ 只能添加字段，不能删除或修改现有字段
- ✅ 使用版本化迁移脚本管理数据库变更
- ❌ 避免直接修改已存在的表结构

**3. 接口设计原则**
- 模块间接口保持独立
- 避免交叉依赖
- 统一错误处理机制

### 开发最佳实践

**开发前**
1. 检查依赖关系，确认模块间依赖
2. 同步最新代码，基于最新develop分支开发
3. 预留编号范围，提前分配消息类型等编号

**开发中**
1. 频繁提交，小步快跑，便于回滚
2. 定期同步，每日同步develop分支
3. 及时沟通，发现潜在冲突立即通知团队

**开发后**
1. 代码审查，重点检查共享文件修改
2. 集成测试，验证模块间兼容性
3. 文档更新，更新接口文档和变更日志

## 🛠️ 故障排除

**常见问题:**
1. **服务器闪退**: 检查MySQL服务是否启动
2. **编译失败**: 确保Java环境正确安装和FlatLaf库存在
3. **连接失败**: 确认数据库配置和端口8888可用
4. **驱动缺失**: 确保`libs/mysql-connector-j-8.0.33.jar`存在
5. **数据库错误**: 重新运行`setup_database.bat`会自动重置数据库
6. **UI显示异常**: 检查FlatLaf库是否正确加载

## 📞 技术支持

如有任何技术问题或协作需求，请及时沟通协调。

- 项目仓库: [Git仓库地址]
- 问题反馈: [Issues链接]
- 技术文档: 参见 `vCampus模块开发指导.md`，以及《[动态加载页面注册指南](docs/dynamic-page-guide.md)》

---

**祝开发顺利！** 🚀