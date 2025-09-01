# vCampus 数据库配置与Git协作指南

## 📊 数据库配置方案

### 方案一：本地独立数据库（推荐）

每个开发者在本地配置独立的MySQL数据库，避免数据冲突。

#### 1. 环境准备

**安装MySQL 8.0：**
```bash
# Windows用户
1. 下载MySQL 8.0安装包
2. 安装时记住root密码
3. 确保MySQL服务启动

# 验证安装
mysql --version
```

#### 2. 本地数据库配置步骤

**步骤1：克隆项目**
```bash
git clone <项目地址>
cd vcampus
```

**步骤2：配置数据库连接**
```bash
# 复制配置文件模板
cp resources/config.properties resources/config.local.properties
```

**修改本地配置文件：**
```properties
# resources/config.local.properties
# 数据库配置 (修改为你的本地配置)
db.url=jdbc:mysql://localhost:3306/vcampus_dev?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=你的MySQL密码
db.driver=com.mysql.cj.jdbc.Driver

# 服务器配置 (可选择不同端口避免冲突)
server.port=8888
server.host=localhost

# 文件上传路径 (使用本地路径)
file.upload.path=uploads/
```

**步骤3：初始化数据库**
```bash
# Windows
setup_database.bat

# 或者手动执行
mysql -u root -p < resources/db/init.sql
mysql -u root -p < resources/db/data.sql
```

#### 3. 数据库命名规范

为避免冲突，建议使用以下命名：
```sql
-- 开发环境
vcampus_dev          -- 主开发数据库
vcampus_dev_张三     -- 个人开发数据库
vcampus_test         -- 测试数据库

-- 生产环境
vcampus              -- 生产数据库
```

#### 4. 配置文件管理

**创建多环境配置：**
```
resources/
├── config.properties          # 默认配置模板
├── config.local.properties    # 本地开发配置（不提交到Git）
├── config.test.properties     # 测试环境配置
└── config.prod.properties     # 生产环境配置
```

**修改代码加载配置：**
```java
// DatabaseUtil.java 修改建议
public class DatabaseUtil {
    private static final String CONFIG_FILE = getConfigFile();
    
    private static String getConfigFile() {
        // 优先加载本地配置
        File localConfig = new File("resources/config.local.properties");
        if (localConfig.exists()) {
            return "resources/config.local.properties";
        }
        return "resources/config.properties";
    }
}
```

### 方案二：共享开发数据库

如果团队选择共享数据库，需要额外的协调机制。

#### 数据库分区策略
```sql
-- 为每个开发者分配用户ID范围
-- 开发者A: user_id 1000-1999
-- 开发者B: user_id 2000-2999  
-- 开发者C: user_id 3000-3999

-- 测试数据插入示例
INSERT INTO users (user_id, login_id, name, password, role) 
VALUES (1001, 'dev_a_001', '开发者A测试用户', 'password_hash', 0);
```

---

## 🌳 Git分支管理教程

### Git分支策略

采用**Git Flow**工作流，确保代码质量和协作效率。

#### 分支结构
```
main                 # 生产环境分支（受保护）
├── develop         # 开发主分支（受保护）
├── release/*       # 发布分支
├── hotfix/*        # 紧急修复分支
└── feature/*       # 功能开发分支
    ├── feature/user-management
    ├── feature/student-management  
    ├── feature/course-management
    ├── feature/library-management
    ├── feature/store-management
    ├── feature/forum-management
    └── feature/file-management
```

### 详细Git操作教程

#### 1. 初始设置

**克隆项目：**
```bash
git clone <项目地址>
cd vcampus

# 查看所有分支
git branch -a

# 切换到develop分支
git checkout develop
git pull origin develop
```

**配置Git用户信息：**
```bash
git config user.name "你的姓名"
git config user.email "你的邮箱"

# 查看配置
git config --list
```

#### 2. 功能开发流程

**创建功能分支：**
```bash
# 从develop创建新的功能分支
git checkout develop
git pull origin develop

# 创建并切换到功能分支
git checkout -b feature/user-management

# 推送分支到远程
git push -u origin feature/user-management
```

**开发过程中：**
```bash
# 查看文件状态
git status

# 添加文件到暂存区
git add .
# 或者添加特定文件
git add src/main/java/client/ui/UserManagementPanel.java

# 提交更改
git commit -m "feat: 添加用户管理主面板UI组件"

# 推送到远程分支
git push origin feature/user-management
```

**提交信息规范：**
```bash
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具链相关

# 示例
git commit -m "feat: 实现用户登录功能"
git commit -m "fix: 修复用户密码验证bug" 
git commit -m "docs: 更新用户管理模块文档"
```

#### 3. 分支合并流程

**功能完成后合并：**
```bash
# 切换到develop分支
git checkout develop
git pull origin develop

# 合并功能分支
git merge feature/user-management

# 推送合并结果
git push origin develop

# 删除本地功能分支
git branch -d feature/user-management

# 删除远程功能分支
git push origin --delete feature/user-management
```

**使用Pull Request（推荐）：**
```bash
# 推送功能分支
git push origin feature/user-management

# 在GitHub/GitLab上创建Pull Request
# 1. 选择 feature/user-management -> develop
# 2. 填写PR描述
# 3. 指定代码审查者
# 4. 等待审查通过后合并
```

#### 4. 冲突解决

**合并冲突处理：**
```bash
# 尝试合并时出现冲突
git merge feature/student-management
# Auto-merging common/protocol/MessageType.java
# CONFLICT (content): Merge conflict in common/protocol/MessageType.java

# 查看冲突文件
git status

# 手动编辑冲突文件
# 文件中会显示冲突标记：
# <<<<<<< HEAD
# 当前分支的内容
# =======
# 要合并分支的内容
# >>>>>>> feature/student-management

# 解决冲突后添加文件
git add common/protocol/MessageType.java

# 完成合并
git commit -m "resolve: 解决MessageType消息类型冲突"
```

#### 5. 同步最新代码

**定期同步develop分支：**
```bash
# 在功能分支中同步最新代码
git checkout feature/user-management
git fetch origin
git merge origin/develop

# 如果有冲突，解决后推送
git push origin feature/user-management
```

**使用rebase保持历史整洁：**
```bash
# 将功能分支变基到最新的develop
git checkout feature/user-management
git rebase develop

# 如果有冲突，解决后继续
git add .
git rebase --continue

# 强制推送（注意：只在功能分支使用）
git push --force-with-lease origin feature/user-management
```

---

## ⚠️ 模块冲突详细讲解

### 冲突类型分析

#### 1. 代码文件冲突

**高频冲突文件：**
```
common/protocol/MessageType.java     # 所有模块都要添加消息类型
common/vo/UserVO.java               # 多个模块修改用户对象
resources/config.properties         # 配置文件修改
resources/db/init.sql               # 数据库结构修改
```

**冲突示例：**
```java
// MessageType.java 冲突示例
public enum MessageType {
<<<<<<< HEAD
    // 用户管理模块
    LOGIN_REQUEST,
    REGISTER_REQUEST,
    // 学籍管理模块  
    GET_STUDENT_INFO_REQUEST,
=======
    // 用户管理模块
    LOGIN_REQUEST,
    REGISTER_REQUEST,
    // 教务管理模块
    GET_ALL_COURSES_REQUEST,
>>>>>>> feature/course-management
    UPDATE_STUDENT_REQUEST,
}
```

#### 2. 数据库结构冲突

**外键依赖冲突：**
```sql
-- 学籍管理模块添加字段
ALTER TABLE students ADD COLUMN gpa DECIMAL(3,2);

-- 教务管理模块也修改学生表
ALTER TABLE students ADD COLUMN advisor_id INT;

-- 冲突：两个模块同时修改同一张表
```

**表创建顺序冲突：**
```sql
-- 模块A创建courses表
CREATE TABLE courses (...);

-- 模块B创建enrollments表，但依赖courses表
CREATE TABLE enrollments (
    course_id INT,
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);
-- 如果courses表还未创建，会报错
```

#### 3. 业务逻辑冲突

**权限验证冲突：**
```java
// 用户管理模块的权限验证
public boolean hasPermission(int userId, String action) {
    // 实现A
}

// 教务管理模块也实现了权限验证
public boolean checkCoursePermission(int userId, int courseId) {
    // 实现B，可能与A冲突
}
```

### 冲突预防策略

#### 1. 文件级别预防

**MessageType分区管理：**
```java
public enum MessageType {
    // 用户管理模块 (1-30)
    LOGIN_REQUEST(1),
    REGISTER_REQUEST(2),
    LOGOUT_REQUEST(3),
    
    // 学籍管理模块 (31-60)
    GET_STUDENT_INFO_REQUEST(31),
    UPDATE_STUDENT_REQUEST(32),
    GET_TRANSCRIPT_REQUEST(33),
    
    // 教务管理模块 (61-90)
    GET_ALL_COURSES_REQUEST(61),
    ADD_COURSE_REQUEST(62),
    ENROLL_COURSE_REQUEST(63),
    
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
    
    private final int code;
    MessageType(int code) { this.code = code; }
    public int getCode() { return code; }
}
```

**配置文件分离：**
```properties
# config.properties - 公共配置
system.name=vCampus虚拟校园系统
system.version=1.0.0

# config.user.properties - 用户模块配置
user.session.timeout=3600
user.max.login.attempts=5

# config.course.properties - 教务模块配置
course.max.enrollment=50
course.drop.deadline=7
```

#### 2. 数据库级别预防

**版本化迁移脚本：**
```sql
-- migrations/v1.0.0_init_database.sql
-- 基础表结构

-- migrations/v1.1.0_user_module.sql  
-- 用户管理模块相关表

-- migrations/v1.2.0_student_module.sql
-- 学籍管理模块相关表

-- migrations/v1.3.0_course_module.sql
-- 教务管理模块相关表
```

**表修改规范：**
```sql
-- 规范：只能添加字段，不能删除或修改现有字段
-- ✅ 正确
ALTER TABLE users ADD COLUMN last_login TIMESTAMP;

-- ❌ 错误 - 可能影响其他模块
ALTER TABLE users DROP COLUMN phone;
ALTER TABLE users MODIFY COLUMN name VARCHAR(50); -- 长度变小
```

#### 3. 接口级别预防

**接口隔离原则：**
```java
// 用户管理模块接口
public interface UserService {
    UserVO login(String loginId, String password);
    boolean register(UserVO user);
}

// 学籍管理模块接口  
public interface StudentService {
    StudentVO getStudentInfo(int studentId);
    boolean updateStudent(StudentVO student);
}

// 避免交叉依赖
```

### 冲突解决流程

#### 1. 发现冲突

**自动检测：**
```bash
# Git合并时自动检测
git merge feature/course-management
# Auto-merging common/protocol/MessageType.java
# CONFLICT (content): Merge conflict in common/protocol/MessageType.java
```

**人工检查：**
```bash
# 定期检查可能的冲突文件
git diff develop..feature/user-management -- common/protocol/MessageType.java
```

#### 2. 冲突协调

**立即通知相关开发者：**
```bash
# 发现冲突后立即通知
@张三 @李四 MessageType.java出现合并冲突，请协调解决

# 冲突内容：
# 用户管理模块添加了LOGIN_REQUEST
# 教务管理模块添加了GET_COURSES_REQUEST  
# 需要重新分配消息类型编号
```

**协调会议：**
- 召集相关开发者
- 分析冲突原因
- 制定解决方案
- 更新开发规范

#### 3. 冲突解决

**代码冲突解决：**
```java
// 解决MessageType冲突
public enum MessageType {
    // 用户管理模块 (1-30) - 张三负责
    LOGIN_REQUEST(1),
    REGISTER_REQUEST(2),
    
    // 教务管理模块 (61-90) - 李四负责  
    GET_ALL_COURSES_REQUEST(61),
    ADD_COURSE_REQUEST(62),
    
    // 避免编号冲突
}
```

**数据库冲突解决：**
```sql
-- 创建新的迁移脚本解决冲突
-- migrations/v1.4.0_resolve_student_table_conflict.sql

-- 合并学籍管理和教务管理的字段修改
ALTER TABLE students 
ADD COLUMN gpa DECIMAL(3,2) COMMENT '学生GPA',
ADD COLUMN advisor_id INT COMMENT '导师ID',
ADD FOREIGN KEY (advisor_id) REFERENCES users(user_id);
```

### 最佳实践总结

#### 开发前
1. **检查依赖关系** - 确认模块间依赖
2. **同步最新代码** - 基于最新develop分支开发
3. **预留编号范围** - 提前分配消息类型等编号

#### 开发中  
1. **频繁提交** - 小步快跑，便于回滚
2. **定期同步** - 每日同步develop分支
3. **及时沟通** - 发现潜在冲突立即通知

#### 开发后
1. **代码审查** - 重点检查共享文件修改
2. **集成测试** - 验证模块间兼容性
3. **文档更新** - 更新接口文档和变更日志

---

## 🔧 工具推荐

### Git可视化工具
- **SourceTree** - 图形化Git客户端
- **GitKraken** - 专业Git工具
- **VS Code Git插件** - 编辑器内置Git支持

### 数据库工具
- **MySQL Workbench** - 官方数据库管理工具
- **Navicat** - 商业数据库工具
- **DBeaver** - 免费开源数据库工具

### 协作工具
- **钉钉/企业微信** - 即时通讯
- **Confluence** - 文档协作
- **Jira** - 任务管理

---

## 📞 紧急联系流程

### 严重冲突处理
1. **立即停止相关分支开发**
2. **通知项目负责人**
3. **召开紧急协调会议**
4. **制定解决方案**
5. **更新开发规范**

### 联系方式
- 项目负责人：[联系方式]
- 技术负责人：[联系方式]  
- 紧急联系群：[群号/链接]

---

**记住：预防胜于治疗，良好的协作规范比事后解决冲突更重要！** 🚀
