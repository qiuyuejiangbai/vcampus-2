-- vCampus 虚拟校园系统数据库初始化脚本
-- 根据VO类设计重写，确保数据库表结构与类设计一致
-- 创建数据库
CREATE DATABASE IF NOT EXISTS vcampus DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vcampus;

-- ========================================
-- 核心用户和角色管理表
-- ========================================

-- 用户基础表（只包含认证相关的核心字段，与UserVO类一致）
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    login_id VARCHAR(50) UNIQUE NOT NULL COMMENT '登录ID(学号/工号/管理员ID)',
    password VARCHAR(255) NOT NULL COMMENT '密码哈希值(MD5)',
    role INT NOT NULL DEFAULT 0 COMMENT '角色: 0-学生, 1-教师, 2-管理员',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_login_id (login_id),
    INDEX idx_role (role)
) COMMENT='用户统一认证表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 学生信息表（根据StudentVO类设计）
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    user_id INT UNIQUE NOT NULL COMMENT '关联用户表ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    student_no VARCHAR(50) UNIQUE NOT NULL COMMENT '学号',
    gender VARCHAR(10) DEFAULT 'male' COMMENT '性别: male-男, female-女, other-其他',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '联系方式',
    email VARCHAR(100) COMMENT '邮箱',
    address TEXT COMMENT '家庭地址',
    department VARCHAR(100) COMMENT '所属院系',
    class_name VARCHAR(100) COMMENT '所属班级',
    major VARCHAR(100) COMMENT '专业',
    grade_table_key VARCHAR(100) COMMENT '成绩数据表对应的键',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    enrollment_year INT COMMENT '入学年份',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_student_no (student_no),
    INDEX idx_user_id (user_id),
    INDEX idx_major (major),
    INDEX idx_class_name (class_name),
    INDEX idx_enrollment_year (enrollment_year)
) COMMENT='学生信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 教师信息表（根据TeacherVO类设计）
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    user_id INT UNIQUE NOT NULL COMMENT '关联用户表ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    teacher_no VARCHAR(50) UNIQUE NOT NULL COMMENT '工号',
    phone VARCHAR(20) COMMENT '联系方式',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(100) COMMENT '所属院系',
    research_area TEXT COMMENT '研究方向',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_user_id (user_id)
) COMMENT='教师信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 管理员信息表（根据AdminVO类设计）
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    user_id INT UNIQUE NOT NULL COMMENT '关联用户表ID',
    username VARCHAR(100) NOT NULL COMMENT '管理员用户名',
    permissions JSON COMMENT '权限配置JSON',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_username (username),
    INDEX idx_user_id (user_id)
) COMMENT='管理员信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 课程和教学管理表
-- ========================================

-- 课程表（根据CourseVO类设计）
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    course_code VARCHAR(50) UNIQUE NOT NULL COMMENT '课程代码',
    course_name VARCHAR(200) NOT NULL COMMENT '课程名称',
    credits INT NOT NULL DEFAULT 1 COMMENT '学分',
    department VARCHAR(100) COMMENT '开课院系',
    teacher_id INT COMMENT '任课教师ID（teachers表）',
    teacher_name VARCHAR(100) COMMENT '任课教师姓名（冗余字段，用于显示）',
    semester VARCHAR(50) COMMENT '开课学期',
    academic_year VARCHAR(20) COMMENT '学年',
    class_time VARCHAR(200) COMMENT '上课时间描述',
    location VARCHAR(200) COMMENT '上课地点',
    capacity INT DEFAULT 50 COMMENT '课程容量',
    enrolled_count INT DEFAULT 0 COMMENT '已选人数',
    status VARCHAR(20) DEFAULT 'planning' COMMENT '课程状态: planning-计划中, active-激活, inactive-停用, completed-已完成',
    description TEXT COMMENT '课程描述',
    prerequisites TEXT COMMENT '先修课程要求',
    syllabus TEXT COMMENT '教学大纲',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束（修正：引用teachers表而不是users表）
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL,
    
    -- 索引
    INDEX idx_course_code (course_code),
    INDEX idx_course_name (course_name),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_semester (semester),
    INDEX idx_department (department),
    INDEX idx_status (status)
) COMMENT='课程表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 课程时间表（根据CourseScheduleVO类设计）
CREATE TABLE IF NOT EXISTS course_schedules (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '时间表ID',
    course_id INT NOT NULL COMMENT '课程ID',
    day_of_week INT NOT NULL COMMENT '星期几(1-7, 1=周一)',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    classroom VARCHAR(100) COMMENT '教室',
    building VARCHAR(100) COMMENT '教学楼',
    weeks VARCHAR(100) COMMENT '授课周次（如：1-16周）',
    
    -- 外键约束
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_course_id (course_id),
    INDEX idx_day_time (day_of_week, start_time),
    INDEX idx_classroom (classroom)
) COMMENT='课程时间表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 选课记录表（根据EnrollmentVO类设计）
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '选课记录ID',
    student_id INT NOT NULL COMMENT '学生ID',
    course_id INT NOT NULL COMMENT '课程ID',
    semester VARCHAR(50) NOT NULL COMMENT '学期',
    academic_year VARCHAR(20) COMMENT '学年',
    enrollment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    drop_time TIMESTAMP NULL COMMENT '退课时间',
    drop_reason VARCHAR(200) COMMENT '退课原因',
    status VARCHAR(20) DEFAULT 'enrolled' COMMENT '状态: enrolled-已选课, dropped-已退课, completed-已完成',
    
    -- 关联信息字段（冗余，用于显示和业务逻辑）
    student_name VARCHAR(100) COMMENT '学生姓名',
    student_no VARCHAR(50) COMMENT '学号',
    course_name VARCHAR(200) COMMENT '课程名称',
    course_code VARCHAR(50) COMMENT '课程代码',
    credits INT COMMENT '学分',
    teacher_name VARCHAR(100) COMMENT '教师姓名',
    
    -- 唯一约束
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester),
    
    -- 外键约束
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_semester (semester),
    INDEX idx_status (status),
    INDEX idx_enrollment_time (enrollment_time)
) COMMENT='选课记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 成绩表（根据GradeVO类设计）
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    enrollment_id INT NOT NULL COMMENT '选课记录ID',
    student_id INT NOT NULL COMMENT '学生ID',
    course_id INT NOT NULL COMMENT '课程ID',
    teacher_id INT NOT NULL COMMENT '任课教师ID',
    semester VARCHAR(50) NOT NULL COMMENT '学期',
    midterm_grade DECIMAL(5,2) COMMENT '期中成绩',
    final_grade DECIMAL(5,2) COMMENT '期末成绩',
    assignment_grade DECIMAL(5,2) COMMENT '作业成绩',
    attendance_grade DECIMAL(5,2) COMMENT '考勤成绩',
    total_grade DECIMAL(5,2) COMMENT '总成绩',
    grade_point DECIMAL(3,2) COMMENT '绩点',
    grade_level VARCHAR(10) COMMENT '等级(A+,A,B+,B,C+,C,D+,D,F)',
    is_retake BOOLEAN DEFAULT FALSE COMMENT '是否重修',
    comments TEXT COMMENT '教师评语',
    graded_time TIMESTAMP NULL COMMENT '评分时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 关联信息字段（冗余，用于显示）
    student_name VARCHAR(100) COMMENT '学生姓名',
    student_no VARCHAR(50) COMMENT '学号',
    course_name VARCHAR(200) COMMENT '课程名称',
    course_code VARCHAR(50) COMMENT '课程代码',
    credits INT COMMENT '学分',
    teacher_name VARCHAR(100) COMMENT '教师姓名',
    
    -- 唯一约束
    UNIQUE KEY uk_enrollment (enrollment_id),
    
    -- 外键约束
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_semester (semester),
    INDEX idx_grade_level (grade_level),
    INDEX idx_total_grade (total_grade)
) COMMENT='成绩表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 课程资源表（根据CourseResourceVO类设计）
CREATE TABLE IF NOT EXISTS course_resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
    course_id INT NOT NULL COMMENT '课程ID',
    resource_name VARCHAR(200) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型: document-文档, video-视频, audio-音频, image-图片, link-链接, other-其他',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_url VARCHAR(500) COMMENT '外部链接',
    file_size BIGINT COMMENT '文件大小(字节)',
    uploader_id INT NOT NULL COMMENT '上传者ID',
    description TEXT COMMENT '资源描述',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (uploader_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_course_id (course_id),
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_resource_type (resource_type),
    INDEX idx_is_public (is_public)
) COMMENT='课程资源表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 图书管理系统表
-- ========================================

-- 图书表（根据BookVO类设计）
CREATE TABLE IF NOT EXISTS books (
    book_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '图书ID',
    isbn VARCHAR(50) COMMENT 'ISBN号',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    publisher VARCHAR(200) COMMENT '出版社',
    category VARCHAR(100) COMMENT '分类',
    publication_date DATE COMMENT '出版日期',
    total_stock INT DEFAULT 1 COMMENT '总库存',
    available_stock INT DEFAULT 1 COMMENT '可借库存',
    location VARCHAR(100) COMMENT '馆藏位置',
    status VARCHAR(20) DEFAULT 'available' COMMENT '状态: available-可借, unavailable-不可借, maintenance-维护中',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_category (category),
    INDEX idx_status (status)
) COMMENT='图书表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 借阅记录表（根据BorrowRecordVO类设计）
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '借阅记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    book_id INT NOT NULL COMMENT '图书ID',
    borrow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    due_time TIMESTAMP NOT NULL COMMENT '应还时间',
    return_time TIMESTAMP NULL COMMENT '实际归还时间',
    extend_count INT DEFAULT 0 COMMENT '续借次数',
    status INT DEFAULT 1 COMMENT '状态: 1-已借出, 2-已归还, 3-逾期, 4-丢失',
    fine_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚金',
    notes TEXT COMMENT '备注',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_borrow_time (borrow_time),
    INDEX idx_due_time (due_time),
    INDEX idx_status (status)
) COMMENT='借阅记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 文献/资料库（library_documents）
-- ========================================
CREATE TABLE IF NOT EXISTS library_documents (
  doc_id        INT AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(255)      NOT NULL,
  authors       VARCHAR(255)      NULL,         -- 多作者用分号/逗号分隔
  year          INT               NULL,
  category      VARCHAR(100)      NULL,         -- 期刊/会议/学位论文/报告/标准/教材/其他
  subject       VARCHAR(100)      NULL,         -- 学科分类：计算机/文学/管理/...
  keywords      VARCHAR(500)      NULL,         -- 搜索关键词，分号/逗号分隔
  abstract_txt  TEXT              NULL,         -- 摘要（可选）
  file_type     VARCHAR(20)       NOT NULL,     -- pdf/docx/pptx/zip...
  file_size     BIGINT            NULL,         -- 字节数
  storage_path  VARCHAR(500)      NOT NULL,     -- 服务器磁盘绝对路径 或 OSS URL
  uploader_id   INT               NULL,         -- 可选：上传者 user_id
  upload_time   TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
  is_public     TINYINT(1)        DEFAULT 1     -- 1=可被检索/下载；0=隐藏
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 常用索引（模糊搜索会用 LIKE；后续需要可改 FULLTEXT）
CREATE INDEX idx_library_documents_title     ON library_documents (title);
CREATE INDEX idx_library_documents_authors   ON library_documents (authors);
CREATE INDEX idx_library_documents_category  ON library_documents (category);
CREATE INDEX idx_library_documents_year      ON library_documents (year);
CREATE INDEX idx_library_documents_file_type ON library_documents (file_type);

-- ========================================
-- 商城系统表
-- ========================================

-- 商品表（根据ProductVO类设计）
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    category VARCHAR(100) COMMENT '分类',
    brand VARCHAR(100) COMMENT '品牌',
    image_url JSON COMMENT '商品图片',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='商品表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表（根据OrderVO类设计）
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    delivery_address TEXT COMMENT '配送地址',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending-待支付, paid-已支付, shipped-已发货, delivered-已送达, cancelled-已取消',
    notes TEXT COMMENT '备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='订单表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单明细表（根据OrderItemVO类设计）
CREATE TABLE IF NOT EXISTS order_items (
    item_id INT NOT NULL,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) COMMENT='订单明细表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 购物车表（根据ShoppingCartVO类设计）
CREATE TABLE IF NOT EXISTS shopping_cart (
     user_id INT NOT NULL,
  id INT PRIMARY KEY AUTO_INCREMENT ,
  product_id INT NOT NULL ,
  quantity INT NOT NULL DEFAULT 1 COMMENT '商品数量',
  price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
  `add_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  -- 确保一个用户不会重复添加同一商品
  UNIQUE INDEX `user_product_unique` (`user_id`, `product_id`),
  -- 外键关联（如果存在用户表和商品表）
  FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE
) COMMENT='用户购物车表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- ========================================
-- 论坛系统表
-- ========================================

CREATE TABLE IF NOT EXISTS forum_threads (
    thread_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '主题标题',
    content TEXT COMMENT '内容',
    author_id INT NOT NULL,
    section_id INT NULL COMMENT '板块ID',
    category VARCHAR(100) COMMENT '分类(兼容旧数据)',
    is_essence BOOLEAN DEFAULT FALSE COMMENT '是否精华',
    reply_count INT DEFAULT 0 COMMENT '回复数',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    last_post_time TIMESTAMP NULL DEFAULT NULL COMMENT '最后回帖时间',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_locked BOOLEAN DEFAULT FALSE COMMENT '是否锁定',
    status INT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常, 2-隐藏',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
    -- ,FOREIGN KEY (section_id) REFERENCES forum_sections(section_id) ON DELETE SET NULL
) COMMENT='论坛主题表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forum_posts (
    post_id INT PRIMARY KEY AUTO_INCREMENT,
    thread_id INT NOT NULL,
    content TEXT NOT NULL COMMENT '回复内容',
    author_id INT NOT NULL,
    parent_post_id INT COMMENT '父回复ID（支持嵌套回复）',
    quote_post_id INT COMMENT '引用回复ID',
    reply_level INT DEFAULT 0 COMMENT '回复层级：0-顶级回复，1-二级回复，2-三级回复等',
    reply_path VARCHAR(500) COMMENT '回复路径，用于快速查询子回复（如：1/2/3）',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    edit_count INT DEFAULT 0 COMMENT '编辑次数',
    edited_time TIMESTAMP NULL DEFAULT NULL COMMENT '最后编辑时间',
    status INT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常, 2-隐藏',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (thread_id) REFERENCES forum_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_post_id) REFERENCES forum_posts(post_id) ON DELETE SET NULL,
    FOREIGN KEY (quote_post_id) REFERENCES forum_posts(post_id) ON DELETE SET NULL,
    
    -- 索引
    INDEX idx_thread_id (thread_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_post_id (parent_post_id),
    INDEX idx_reply_level (reply_level),
    INDEX idx_reply_path (reply_path),
    INDEX idx_created_time (created_time),
    INDEX idx_status (status)
) COMMENT='论坛回复表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 板块表
CREATE TABLE IF NOT EXISTS forum_sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '板块名称',
    description VARCHAR(255) COMMENT '简介',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态: 0-禁用,1-启用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT='论坛板块表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE IF NOT EXISTS forum_tags (
    tag_id INT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(50) NOT NULL UNIQUE,
    status INT DEFAULT 1 COMMENT '状态'
) COMMENT='论坛标签表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 主题-标签关联表
CREATE TABLE IF NOT EXISTS forum_thread_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    thread_id INT NOT NULL,
    tag_id INT NOT NULL,
    UNIQUE KEY uniq_thread_tag (thread_id, tag_id),
    FOREIGN KEY (thread_id) REFERENCES forum_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES forum_tags(tag_id) ON DELETE CASCADE
) COMMENT='主题标签关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 点赞表（主题/回复通用）
CREATE TABLE IF NOT EXISTS forum_likes (
    like_id INT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('thread','post') NOT NULL,
    entity_id INT NOT NULL,
    user_id INT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_like (entity_type, entity_id, user_id),
    INDEX idx_entity (entity_type, entity_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛点赞表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 收藏表（主题）
CREATE TABLE IF NOT EXISTS forum_favorites (
    favorite_id INT PRIMARY KEY AUTO_INCREMENT,
    thread_id INT NOT NULL,
    user_id INT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_fav (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES forum_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛收藏表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 附件/图片表（主题/回复通用）
CREATE TABLE IF NOT EXISTS forum_attachments (
    attachment_id INT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('thread','post') NOT NULL,
    entity_id INT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_type VARCHAR(50) COMMENT 'MIME或类型',
    file_size INT DEFAULT 0,
    storage_path VARCHAR(300) NOT NULL,
    uploader_id INT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_attach_entity (entity_type, entity_id),
    FOREIGN KEY (uploader_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛附件表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 公告表
CREATE TABLE IF NOT EXISTS forum_announcements (
    announcement_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    status INT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='论坛公告表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 浏览日志（用于热门/趋势统计）
CREATE TABLE IF NOT EXISTS forum_views (
    view_id INT PRIMARY KEY AUTO_INCREMENT,
    thread_id INT NOT NULL,
    user_id INT NULL,
    ip_hash CHAR(64) NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_thread_time (thread_id, viewed_at),
    INDEX idx_user_time (user_id, viewed_at),
    FOREIGN KEY (thread_id) REFERENCES forum_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='论坛浏览日志' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 举报表
CREATE TABLE IF NOT EXISTS forum_reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('thread','post') NOT NULL,
    entity_id INT NOT NULL,
    reporter_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status INT DEFAULT 0 COMMENT '0-未处理,1-通过,2-驳回',
    handled_by INT NULL,
    handled_time TIMESTAMP NULL DEFAULT NULL,
    remark VARCHAR(255) NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (handled_by) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='论坛举报表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审核/操作日志
CREATE TABLE IF NOT EXISTS forum_moderation_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('thread','post') NOT NULL,
    entity_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    operator_id INT NOT NULL,
    detail VARCHAR(255) NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛审核操作日志' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 通知表
CREATE TABLE IF NOT EXISTS forum_notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type ENUM('reply','mention','like','system') NOT NULL,
    payload JSON NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛通知表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 系统配置表
-- ========================================

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    config_id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置描述',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型: string-字符串, number-数字, boolean-布尔, json-JSON',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='系统配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;