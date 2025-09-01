-- vCampus 虚拟校园系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS vcampus DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vcampus;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    login_id VARCHAR(50) UNIQUE NOT NULL COMMENT '登录ID(学号/教工号)',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    password VARCHAR(255) NOT NULL COMMENT '密码哈希',
    role INT NOT NULL DEFAULT 0 COMMENT '角色: 0-学生, 1-教师, 2-管理员',
    status INT NOT NULL DEFAULT 0 COMMENT '状态: 0-未激活, 1-已激活',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户表';

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    student_no VARCHAR(50) UNIQUE NOT NULL COMMENT '学号',
    major VARCHAR(100) COMMENT '专业',
    class_name VARCHAR(100) COMMENT '班级',
    grade VARCHAR(10) COMMENT '年级',
    enrollment_year INT COMMENT '入学年份',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='学生表';

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(50) UNIQUE NOT NULL COMMENT '课程代码',
    course_name VARCHAR(200) NOT NULL COMMENT '课程名称',
    credits INT NOT NULL DEFAULT 1 COMMENT '学分',
    department VARCHAR(100) COMMENT '开课院系',
    teacher_id INT COMMENT '任课教师ID',
    semester VARCHAR(20) COMMENT '开课学期',
    description TEXT COMMENT '课程描述',
    capacity INT DEFAULT 50 COMMENT '课程容量',
    enrolled_count INT DEFAULT 0 COMMENT '已选人数',
    status INT DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='课程表';

-- 选课记录表
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    grade DECIMAL(5,2) COMMENT '成绩',
    status INT DEFAULT 1 COMMENT '状态: 0-已退课, 1-已选课, 2-已完成',
    UNIQUE KEY uk_student_course (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) COMMENT='选课记录表';

-- 图书表
CREATE TABLE IF NOT EXISTS books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(50) COMMENT 'ISBN号',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    publisher VARCHAR(200) COMMENT '出版社',
    category VARCHAR(100) COMMENT '分类',
    total_stock INT DEFAULT 1 COMMENT '总库存',
    available_stock INT DEFAULT 1 COMMENT '可借库存',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    due_time TIMESTAMP NOT NULL COMMENT '应还时间',
    return_time TIMESTAMP NULL COMMENT '实际归还时间',
    status INT DEFAULT 1 COMMENT '状态: 1-已借出, 2-已归还, 3-逾期',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
) COMMENT='借阅记录表';

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    category VARCHAR(100) COMMENT '分类',
    status INT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    status INT DEFAULT 1 COMMENT '状态: 1-已完成, 0-已取消',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) COMMENT='订单明细表';

-- 论坛主题表
CREATE TABLE IF NOT EXISTS forum_threads (
    thread_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '主题标题',
    content TEXT COMMENT '内容',
    author_id INT NOT NULL,
    reply_count INT DEFAULT 0 COMMENT '回复数',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status INT DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛主题表';

-- 论坛回复表
CREATE TABLE IF NOT EXISTS forum_posts (
    post_id INT PRIMARY KEY AUTO_INCREMENT,
    thread_id INT NOT NULL,
    content TEXT NOT NULL COMMENT '回复内容',
    author_id INT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
    FOREIGN KEY (thread_id) REFERENCES forum_threads(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='论坛回复表';

-- 文件资源表
CREATE TABLE IF NOT EXISTS course_files (
    file_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小(字节)',
    uploader_id INT NOT NULL COMMENT '上传者ID',
    description VARCHAR(500) COMMENT '文件描述',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (uploader_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='课程文件表';

