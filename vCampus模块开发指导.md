# vCampus 虚拟校园系统 - 模块开发指导

## 项目概述

vCampus是一个基于Java Swing和MySQL的C/S架构虚拟校园系统，包含用户管理、学籍管理、教务管理、图书馆、商店、论坛和文件资源等七大核心模块。

## 整体架构

```
vCampus/
├── client/           # 客户端(UI层)
├── server/           # 服务器端(业务逻辑层)  
├── common/           # 共享模块(数据传输对象和协议)
└── resources/        # 配置文件和数据库脚本
```

### 技术栈
- **前端**: Java Swing
- **后端**: Java Socket编程
- **数据库**: MySQL 8.0
- **通信协议**: 自定义消息协议
- **架构模式**: MVC + 三层架构

---

## 模块一：用户管理模块 👤

### 模块概述
负责用户注册、登录、权限管理等基础功能。

### 开发人员要求
- 熟悉Java基础和Swing界面开发
- 了解MD5加密和用户权限设计
- **建议分配给**: 有经验的开发者(作为基础模块)

### 核心功能
1. **用户注册登录**
2. **用户信息管理**
3. **权限控制**
4. **密码加密**

### 已完成部分
- ✅ UserVO数据模型
- ✅ UserService业务逻辑
- ✅ UserDAO数据访问层
- ✅ LoginFrame登录界面
- ✅ MD5加密工具

### 待开发功能

#### 客户端开发任务
```java
// 需要开发的UI组件
client/ui/
├── UserManagementPanel.java      // 用户管理主面板
├── UserProfileDialog.java        // 用户资料编辑对话框
├── ChangePasswordDialog.java     // 修改密码对话框
└── UserListPanel.java           // 用户列表管理(管理员用)
```

#### 服务器端开发任务
- 完善用户权限验证逻辑
- 实现用户状态管理(激活/停用)
- 添加用户搜索和筛选功能

### 消息协议
```java
// 用户管理相关的消息类型
LOGIN_REQUEST/SUCCESS/FAIL
REGISTER_REQUEST/SUCCESS/FAIL
UPDATE_USER_REQUEST/SUCCESS/FAIL
GET_USER_INFO_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 完成用户资料管理界面
2. **第2周**: 实现密码修改功能
3. **第3周**: 开发管理员用户管理功能
4. **第4周**: 测试和优化

---

## 模块二：学籍管理模块 🎓

### 模块概述
管理学生的学籍信息、成绩记录等。

### 开发人员要求
- 熟悉数据库操作和复杂查询
- 了解学籍管理业务逻辑
- **建议分配给**: 中级开发者

### 核心功能
1. **学生信息管理**
2. **成绩管理**
3. **学籍状态跟踪**
4. **成绩单生成**

### 数据模型
```sql
-- 主要涉及的数据表
students          -- 学生基本信息
enrollments       -- 选课记录(包含成绩)
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/student/
├── StudentManagementPanel.java   // 学籍管理主面板
├── StudentInfoDialog.java        // 学生信息编辑
├── GradeManagementPanel.java     // 成绩管理面板
├── TranscriptDialog.java         // 成绩单查看
└── StudentSearchPanel.java       // 学生搜索功能
```

#### 服务器端组件
```java
server/service/
├── StudentService.java           // 学生业务服务
└── GradeService.java            // 成绩业务服务

server/dao/
├── StudentDAO.java               // 已存在，需完善
└── GradeDAO.java                // 成绩数据访问
```

### 开发任务清单
- [ ] 创建StudentService业务逻辑层
- [ ] 完善StudentDAO数据访问层
- [ ] 开发学生信息管理界面
- [ ] 实现成绩录入和查询功能
- [ ] 开发成绩单导出功能

### 消息协议
```java
GET_STUDENT_INFO_REQUEST/SUCCESS
UPDATE_STUDENT_REQUEST/SUCCESS
GET_TRANSCRIPT_REQUEST/SUCCESS
GET_ALL_STUDENTS_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 完善数据访问层和业务逻辑层
2. **第2周**: 开发学生信息管理界面
3. **第3周**: 实现成绩管理功能
4. **第4周**: 开发成绩单和报表功能

---

## 模块三：教务管理模块 📚

### 模块概述
课程管理、选课系统、教师课程管理等。

### 开发人员要求
- 熟悉复杂业务逻辑处理
- 了解教务管理流程
- **建议分配给**: 高级开发者

### 核心功能
1. **课程信息管理**
2. **选课退课系统**
3. **教师课程管理**
4. **课程容量控制**

### 数据模型
```sql
-- 主要涉及的数据表
courses           -- 课程信息
enrollments       -- 选课记录
course_files      -- 课程资源文件
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/course/
├── CourseManagementPanel.java    // 课程管理主面板
├── CourseSelectionPanel.java     // 选课面板(学生用)
├── MyCoursePanel.java           // 我的课程面板
├── CourseDetailDialog.java      // 课程详情对话框
├── TeacherCoursePanel.java      // 教师课程管理面板
└── StudentListDialog.java       // 学生名单对话框
```

#### 服务器端组件
```java
server/service/
├── CourseService.java           // 课程业务服务
└── EnrollmentService.java       // 选课业务服务

server/dao/
├── CourseDAO.java               // 已存在，需完善
└── EnrollmentDAO.java           // 已存在，需完善
```

### 开发任务清单
- [ ] 完善CourseService和EnrollmentService
- [ ] 开发课程管理界面(管理员用)
- [ ] 实现选课系统(学生用)
- [ ] 开发教师课程管理功能
- [ ] 实现课程容量和冲突检测
- [ ] 添加课程搜索和筛选功能

### 消息协议
```java
GET_ALL_COURSES_REQUEST/SUCCESS
ENROLL_COURSE_REQUEST/SUCCESS/FAIL
DROP_COURSE_REQUEST/SUCCESS/FAIL
GET_MY_COURSES_REQUEST/SUCCESS
UPDATE_GRADE_REQUEST/SUCCESS
```

### 开发步骤
1. **第1-2周**: 完善业务逻辑层和数据访问层
2. **第3周**: 开发课程管理界面
3. **第4周**: 实现选课退课功能
4. **第5周**: 开发教师功能模块
5. **第6周**: 测试和优化

---

## 模块四：图书馆管理模块 📖

### 模块概述
图书信息管理、借阅归还系统、借阅记录管理。

### 开发人员要求
- 熟悉库存管理逻辑
- 了解图书管理业务流程
- **建议分配给**: 中级开发者

### 核心功能
1. **图书信息管理**
2. **借阅归还系统**
3. **借阅记录查询**
4. **逾期管理**

### 数据模型
```sql
-- 主要涉及的数据表
books             -- 图书信息
borrow_records    -- 借阅记录
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/library/
├── LibraryManagementPanel.java   // 图书馆管理主面板
├── BookSearchPanel.java          // 图书搜索面板
├── BookManagementPanel.java      // 图书管理面板(管理员用)
├── BorrowPanel.java             // 借阅面板
├── ReturnPanel.java             // 归还面板
├── BorrowHistoryPanel.java      // 借阅历史面板
└── BookDetailDialog.java        // 图书详情对话框
```

#### 服务器端组件
```java
server/service/
├── BookService.java             // 图书业务服务
└── BorrowService.java           // 借阅业务服务

server/dao/
├── BookDAO.java                 // 图书数据访问
└── BorrowRecordDAO.java         // 借阅记录数据访问
```

### 开发任务清单
- [ ] 创建完整的数据访问层
- [ ] 实现图书业务逻辑层
- [ ] 开发图书搜索和浏览界面
- [ ] 实现借阅归还功能
- [ ] 开发借阅记录管理
- [ ] 实现逾期提醒功能
- [ ] 添加图书统计报表

### 消息协议
```java
SEARCH_BOOK_REQUEST/SUCCESS
BORROW_BOOK_REQUEST/SUCCESS/FAIL
RETURN_BOOK_REQUEST/SUCCESS/FAIL
GET_BORROW_RECORDS_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 开发数据访问层和业务逻辑层
2. **第2周**: 实现图书搜索和浏览功能
3. **第3周**: 开发借阅归还系统
4. **第4周**: 实现借阅记录和逾期管理
5. **第5周**: 测试和优化

---

## 模块五：商店模块 🛒

### 模块概述
商品管理、购买系统、订单管理、用户余额管理。

### 开发人员要求
- 熟悉电商系统开发
- 了解订单和支付流程
- **建议分配给**: 中级开发者

### 核心功能
1. **商品管理**
2. **购物车系统**
3. **订单管理**
4. **余额支付**

### 数据模型
```sql
-- 主要涉及的数据表
products          -- 商品信息
orders            -- 订单信息
order_items       -- 订单明细
users.balance     -- 用户余额
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/store/
├── StorePanel.java              // 商店主面板
├── ProductListPanel.java        // 商品列表面板
├── ProductDetailDialog.java     // 商品详情对话框
├── ShoppingCartPanel.java       // 购物车面板
├── OrderPanel.java              // 订单面板
├── ProductManagementPanel.java  // 商品管理面板(管理员用)
└── BalanceManagementPanel.java  // 余额管理面板
```

#### 服务器端组件
```java
server/service/
├── ProductService.java          // 商品业务服务
├── OrderService.java            // 订单业务服务
└── PaymentService.java          // 支付业务服务

server/dao/
├── ProductDAO.java              // 商品数据访问
├── OrderDAO.java                // 订单数据访问
└── OrderItemDAO.java            // 订单明细数据访问
```

### 开发任务清单
- [ ] 创建完整的数据访问层
- [ ] 实现商品和订单业务逻辑
- [ ] 开发商品浏览和搜索界面
- [ ] 实现购物车功能
- [ ] 开发订单和支付系统
- [ ] 实现库存管理
- [ ] 添加销售统计功能

### 消息协议
```java
GET_STORE_ITEMS_REQUEST/SUCCESS
PURCHASE_REQUEST/SUCCESS/FAIL
GET_ORDER_HISTORY_REQUEST/SUCCESS
ADD_PRODUCT_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 开发数据访问层和业务逻辑层
2. **第2周**: 实现商品浏览和管理功能
3. **第3周**: 开发购物车和订单系统
4. **第4周**: 实现支付和余额管理
5. **第5周**: 测试和优化

---

## 模块六：论坛模块 💬

### 模块概述
校园论坛系统，支持主题发布、回复讨论等。

### 开发人员要求
- 熟悉论坛系统开发
- 了解内容管理和审核机制
- **建议分配给**: 中级开发者

### 核心功能
1. **主题管理**
2. **回复系统**
3. **内容审核**
4. **用户互动**

### 数据模型
```sql
-- 主要涉及的数据表
forum_threads     -- 论坛主题
forum_posts       -- 论坛回复
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/forum/
├── ForumPanel.java              // 论坛主面板
├── ThreadListPanel.java         // 主题列表面板
├── ThreadDetailPanel.java       // 主题详情面板
├── CreateThreadDialog.java      // 创建主题对话框
├── ReplyDialog.java             // 回复对话框
└── ForumManagementPanel.java    // 论坛管理面板(管理员用)
```

#### 服务器端组件
```java
server/service/
├── ForumService.java            // 论坛业务服务
├── ThreadService.java           // 主题业务服务
└── PostService.java             // 回复业务服务

server/dao/
├── ThreadDAO.java               // 主题数据访问
└── PostDAO.java                 // 回复数据访问
```

### 开发任务清单
- [ ] 创建完整的数据访问层
- [ ] 实现论坛业务逻辑层
- [ ] 开发主题浏览和搜索功能
- [ ] 实现主题发布和回复功能
- [ ] 开发内容管理和审核功能
- [ ] 实现用户互动功能(点赞等)
- [ ] 添加热门话题统计

### 消息协议
```java
GET_ALL_THREADS_REQUEST/SUCCESS
CREATE_THREAD_REQUEST/SUCCESS
GET_POSTS_REQUEST/SUCCESS
CREATE_POST_REQUEST/SUCCESS
DELETE_THREAD_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 开发数据访问层和业务逻辑层
2. **第2周**: 实现主题浏览和搜索功能
3. **第3周**: 开发主题发布和回复功能
4. **第4周**: 实现内容管理功能
5. **第5周**: 测试和优化

---

## 模块七：文件资源模块 📁

### 模块概述
课程文件上传下载、资源管理等。

### 开发人员要求
- 熟悉文件I/O操作
- 了解网络文件传输
- **建议分配给**: 高级开发者

### 核心功能
1. **文件上传下载**
2. **文件管理**
3. **权限控制**
4. **存储优化**

### 数据模型
```sql
-- 主要涉及的数据表
course_files      -- 课程文件信息
```

### 需要开发的组件

#### 客户端UI组件
```java
client/ui/file/
├── FileManagementPanel.java     // 文件管理主面板
├── FileUploadDialog.java        // 文件上传对话框
├── FileDownloadDialog.java      // 文件下载对话框
├── CourseFilePanel.java         // 课程文件面板
└── FileProgressDialog.java      // 文件传输进度对话框
```

#### 服务器端组件
```java
server/service/
├── FileService.java             // 文件业务服务
└── FileTransferService.java     // 文件传输服务

server/dao/
└── FileDAO.java                 // 文件数据访问

server/util/
└── FileUtil.java                // 文件工具类
```

### 开发任务清单
- [ ] 创建文件数据访问层
- [ ] 实现文件业务逻辑层
- [ ] 开发文件上传下载功能
- [ ] 实现文件权限控制
- [ ] 开发文件管理界面
- [ ] 实现断点续传功能
- [ ] 添加文件预览功能

### 消息协议
```java
FILE_UPLOAD_REQUEST/READY/SUCCESS/FAIL
FILE_DOWNLOAD_REQUEST/READY/SUCCESS/FAIL
GET_COURSE_FILES_REQUEST/SUCCESS
DELETE_FILE_REQUEST/SUCCESS
```

### 开发步骤
1. **第1周**: 开发数据访问层和基础文件操作
2. **第2周**: 实现文件上传下载核心功能
3. **第3周**: 开发文件管理界面
4. **第4周**: 实现高级功能(断点续传等)
5. **第5周**: 测试和优化

---

## 开发协作指南

### 代码规范
1. **包命名**: 按模块划分包结构
2. **类命名**: 使用驼峰命名法，类名首字母大写
3. **方法命名**: 动词开头，驼峰命名法
4. **注释规范**: 必须添加类注释和方法注释

### Git协作流程
```bash
# 主分支
main                    # 稳定发布版本

# 开发分支  
develop                 # 开发主分支

# 功能分支
feature/user-management     # 用户管理模块
feature/student-management  # 学籍管理模块
feature/course-management   # 教务管理模块
feature/library-management  # 图书馆模块
feature/store-management    # 商店模块
feature/forum-management    # 论坛模块
feature/file-management     # 文件资源模块
```

### 接口约定
1. **消息协议**: 严格按照MessageType定义
2. **数据传输**: 统一使用VO对象
3. **错误处理**: 统一的异常处理机制
4. **日志规范**: 统一的日志格式

### 测试要求
1. **单元测试**: 每个Service类必须有对应测试
2. **集成测试**: 模块间接口测试
3. **UI测试**: 界面功能测试
4. **性能测试**: 关键功能性能测试

### 部署和构建
```bash
# 编译项目
compile.bat

# 启动服务器
start_server.bat

# 启动客户端
start_client.bat
```

---

## 开发时间规划

### 总体时间安排 (8周)
- **第1-2周**: 基础模块(用户管理)
- **第3-4周**: 核心模块(学籍管理、教务管理)
- **第5-6周**: 业务模块(图书馆、商店)
- **第7周**: 扩展模块(论坛、文件资源)
- **第8周**: 集成测试和优化

### 里程碑检查点
- **第2周末**: 用户管理模块完成
- **第4周末**: 教务相关模块完成
- **第6周末**: 所有业务模块完成
- **第8周末**: 系统集成测试完成

---

## 注意事项

### 开发优先级
1. **高优先级**: 用户管理、学籍管理、教务管理
2. **中优先级**: 图书馆管理、商店管理
3. **低优先级**: 论坛、文件资源

### 技术难点
1. **网络通信**: Socket编程和消息协议处理
2. **文件传输**: 大文件上传下载和断点续传
3. **并发处理**: 多用户同时操作的并发控制
4. **数据一致性**: 选课、购买等操作的事务处理

### 风险控制
1. **技术风险**: 提前进行技术验证
2. **进度风险**: 定期进度检查和调整
3. **质量风险**: 代码审查和测试覆盖
4. **集成风险**: 早期集成和持续集成

---

## 联系方式

如有任何技术问题或协作需求，请及时沟通协调。

**祝开发顺利！** 🚀
