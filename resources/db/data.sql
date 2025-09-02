-- vCampus 虚拟校园系统初始数据
-- 根据重写的表结构更新初始化数据
USE vcampus;

-- ========================================
-- 用户认证数据（users表）
-- ========================================

-- 插入管理员用户认证信息 (密码: admin123)
INSERT INTO users (login_id, password, role) VALUES 
('admin', '0192023a7bbd73250516f069df18b500', 2);

-- 插入教师用户认证信息 (密码: teacher123)
INSERT INTO users (login_id, password, role) VALUES 
('T001', 'a426dcf72ba25d046591f81a5495eab7', 1),
('T002', 'a426dcf72ba25d046591f81a5495eab7', 1),
('T003', 'a426dcf72ba25d046591f81a5495eab7', 1);

-- 插入学生用户认证信息 (密码: student123)
INSERT INTO users (login_id, password, role) VALUES 
('2021001', 'ad6a280417a0f533d8b670c61667e1a0', 0),
('2021002', 'ad6a280417a0f533d8b670c61667e1a0', 0),
('2021003', 'ad6a280417a0f533d8b670c61667e1a0', 0),
('2021004', 'ad6a280417a0f533d8b670c61667e1a0', 0),
('2021005', 'ad6a280417a0f533d8b670c61667e1a0', 0);

-- ========================================
-- 管理员详细信息（admins表）
-- ========================================

-- 插入管理员详细信息
INSERT INTO admins (user_id, username, permissions) VALUES 
(1, 'admin', '["user_management", "course_management", "system_config"]');

-- ========================================
-- 教师详细信息（teachers表）
-- ========================================

-- 插入教师详细信息
INSERT INTO teachers (user_id, name, teacher_no, phone, email, department, research_area) VALUES 
(2, '张三', 'T001', '13800000001', 'zhangsan@vcampus.edu.cn', '计算机学院', 'Java程序设计,软件工程'),
(3, '李四', 'T002', '13800000002', 'lisi@vcampus.edu.cn', '计算机学院', '数据结构与算法,算法设计'),
(4, '王五', 'T003', '13800000003', 'wangwu@vcampus.edu.cn', '计算机学院', '数据库系统,大数据技术');

-- ========================================
-- 学生详细信息（students表）
-- ========================================

-- 插入学生详细信息
INSERT INTO students (user_id, name, student_no, gender, birth_date, phone, email, address, department, class_name, major, grade_table_key, balance, enrollment_year) VALUES 
(5, '赵六', '2021001', 'male', '2003-05-15', '13800001001', 'zhaoliu@student.vcampus.edu.cn', '北京市海淀区', '计算机学院', '计科2101班', '计算机科学与技术', 'CS2021001', 1000.00, 2021),
(6, '钱七', '2021002', 'female', '2003-03-22', '13800001002', 'qianqi@student.vcampus.edu.cn', '上海市浦东新区', '计算机学院', '软工2101班', '软件工程', 'SE2021002', 1000.00, 2021),
(7, '孙八', '2021003', 'male', '2003-07-08', '13800001003', 'sunba@student.vcampus.edu.cn', '广州市天河区', '计算机学院', '信安2101班', '信息安全', 'IS2021003', 1000.00, 2021),
(8, '周九', '2021004', 'female', '2003-01-12', '13800001004', 'zhoujiu@student.vcampus.edu.cn', '深圳市南山区', '计算机学院', '计科2101班', '计算机科学与技术', 'CS2021004', 1000.00, 2021),
(9, '吴十', '2021005', 'male', '2003-09-30', '13800001005', 'wushi@student.vcampus.edu.cn', '杭州市西湖区', '计算机学院', '软工2101班', '软件工程', 'SE2021005', 1000.00, 2021);

-- ========================================
-- 课程信息（courses表）
-- ========================================

-- 插入课程信息（修正teacher_id引用teachers表）
INSERT INTO courses (course_code, course_name, credits, department, teacher_id, teacher_name, semester, academic_year, class_time, location, capacity, enrolled_count, status, description) VALUES 
('CS101', 'Java程序设计', 3, '计算机学院', 1, '张三', '2024春', '2023-2024', '周一 1-2节, 周三 3-4节', '教学楼A101', 50, 3, 'active', 'Java面向对象程序设计基础课程'),
('CS102', '数据结构与算法', 4, '计算机学院', 2, '李四', '2024春', '2023-2024', '周二 3-4节, 周四 1-2节', '教学楼A102', 45, 2, 'active', '数据结构基础与算法分析'),
('CS103', '数据库系统原理', 3, '计算机学院', 3, '王五', '2024春', '2023-2024', '周一 3-4节, 周五 1-2节', '教学楼A103', 40, 1, 'active', '关系数据库系统原理与应用'),
('CS104', '计算机网络', 3, '计算机学院', 1, '张三', '2024春', '2023-2024', '周三 1-2节, 周五 3-4节', '教学楼A104', 35, 0, 'active', '计算机网络基础与协议分析'),
('CS105', '软件工程', 3, '计算机学院', 2, '李四', '2024春', '2023-2024', '周二 1-2节, 周四 3-4节', '教学楼A105', 30, 0, 'active', '软件工程方法与项目管理');

-- ========================================
-- 课程时间表（course_schedules表）
-- ========================================

-- 插入课程时间表
INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, classroom, building, weeks) VALUES 
(1, 1, '08:00:00', '09:40:00', 'A101', '教学楼A', '1-16周'),
(1, 3, '10:00:00', '11:40:00', 'A101', '教学楼A', '1-16周'),
(2, 2, '10:00:00', '11:40:00', 'A102', '教学楼A', '1-16周'),
(2, 4, '08:00:00', '09:40:00', 'A102', '教学楼A', '1-16周'),
(3, 1, '10:00:00', '11:40:00', 'A103', '教学楼A', '1-16周'),
(3, 5, '08:00:00', '09:40:00', 'A103', '教学楼A', '1-16周');

-- ========================================
-- 选课记录（enrollments表）
-- ========================================

-- 插入选课记录（包含冗余信息字段）
INSERT INTO enrollments (student_id, course_id, semester, academic_year, enrollment_time, status, 
                        student_name, student_no, course_name, course_code, credits, teacher_name) VALUES 
(1, 1, '2024春', '2023-2024', '2024-02-15 10:00:00', 'enrolled', '赵六', '2021001', 'Java程序设计', 'CS101', 3, '张三'),
(2, 1, '2024春', '2023-2024', '2024-02-15 10:30:00', 'enrolled', '钱七', '2021002', 'Java程序设计', 'CS101', 3, '张三'),
(3, 1, '2024春', '2023-2024', '2024-02-15 11:00:00', 'enrolled', '孙八', '2021003', 'Java程序设计', 'CS101', 3, '张三'),
(1, 2, '2024春', '2023-2024', '2024-02-16 09:00:00', 'enrolled', '赵六', '2021001', '数据结构与算法', 'CS102', 4, '李四'),
(2, 2, '2024春', '2023-2024', '2024-02-16 09:30:00', 'enrolled', '钱七', '2021002', '数据结构与算法', 'CS102', 4, '李四'),
(3, 3, '2024春', '2023-2024', '2024-02-17 14:00:00', 'enrolled', '孙八', '2021003', '数据库系统原理', 'CS103', 3, '王五');

-- ========================================
-- 成绩记录（grades表）
-- ========================================

-- 插入成绩记录（包含冗余信息字段）
INSERT INTO grades (enrollment_id, student_id, course_id, teacher_id, semester, midterm_grade, final_grade, assignment_grade, total_grade, grade_point, grade_level,
                   student_name, student_no, course_name, course_code, credits, teacher_name, graded_time) VALUES 
(1, 1, 1, 1, '2024春', 85.0, 87.0, 90.0, 87.3, 3.7, 'B+', '赵六', '2021001', 'Java程序设计', 'CS101', 3, '张三', '2024-03-15 16:30:00'),
(2, 2, 1, 1, '2024春', 92.0, 90.0, 95.0, 91.7, 4.0, 'A', '钱七', '2021002', 'Java程序设计', 'CS101', 3, '张三', '2024-03-15 16:35:00'),
(3, 3, 1, 1, '2024春', 78.0, 80.0, 85.0, 80.6, 3.0, 'B', '孙八', '2021003', 'Java程序设计', 'CS101', 3, '张三', '2024-03-15 16:40:00'),
(4, 1, 2, 2, '2024春', 88.0, 85.0, 92.0, 87.8, 3.8, 'B+', '赵六', '2021001', '数据结构与算法', 'CS102', 4, '李四', '2024-03-20 10:20:00'),
(5, 2, 2, 2, '2024春', 95.0, 96.0, 98.0, 96.2, 4.0, 'A', '钱七', '2021002', '数据结构与算法', 'CS102', 4, '李四', '2024-03-20 10:25:00');

-- ========================================
-- 课程资源（course_resources表）
-- ========================================

-- 插入课程资源示例
INSERT INTO course_resources (course_id, resource_name, resource_type, file_path, file_size, uploader_id, description, is_public) VALUES 
(1, 'Java基础教程.pdf', 'document', '/resources/courses/java_tutorial.pdf', 2048000, 2, 'Java编程基础教程文档', 1),
(1, 'Java实验指导.doc', 'document', '/resources/courses/java_lab.doc', 512000, 2, '实验操作指导文档', 1),
(2, '数据结构课件.ppt', 'document', '/resources/courses/data_structure.ppt', 1024000, 3, '数据结构与算法课程课件', 1),
(2, '算法演示视频.mp4', 'video', '/resources/courses/algorithm_demo.mp4', 52428800, 3, '常用算法演示视频', 1),
(3, '数据库设计规范.pdf', 'document', '/resources/courses/db_design.pdf', 1536000, 4, '数据库设计规范与实践', 1);

-- ========================================
-- 图书信息（books表）
-- ========================================

-- 插入图书信息
INSERT INTO books (isbn, title, author, publisher, category, total_stock, available_stock, location) VALUES 
('978-7-111-54742-6', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', '计算机技术', 5, 4, 'A区-计算机类'),
('978-7-111-54743-3', 'Java核心技术 卷II', 'Cay S. Horstmann', '机械工业出版社', '计算机技术', 3, 3, 'A区-计算机类'),
('978-7-115-28969-4', '算法导论', 'Thomas H. Cormen', '人民邮电出版社', '计算机技术', 4, 3, 'A区-计算机类'),
('978-7-111-55842-2', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '计算机技术', 3, 2, 'A区-计算机类'),
('978-7-115-35208-1', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '数据库', 6, 5, 'A区-数据库类'),
('978-7-111-26969-4', '设计模式', 'Erich Gamma', '机械工业出版社', '软件工程', 2, 2, 'B区-软件工程类'),
('978-7-115-48904-4', 'Spring实战', 'Craig Walls', '人民邮电出版社', '计算机技术', 4, 4, 'A区-计算机类'),
('978-7-121-31065-1', '计算机网络：自顶向下方法', 'James F. Kurose', '电子工业出版社', '计算机网络', 3, 3, 'A区-网络类');

-- ========================================
-- 借阅记录（borrow_records表）
-- ========================================

-- 插入借阅记录
INSERT INTO borrow_records (user_id, book_id, borrow_time, due_time, return_time, status) VALUES 
(5, 1, '2024-03-01 10:00:00', '2024-04-01 10:00:00', NULL, 1),
(6, 4, '2024-03-05 14:30:00', '2024-04-05 14:30:00', NULL, 1),
(7, 5, '2024-03-10 09:15:00', '2024-04-10 09:15:00', '2024-03-25 16:20:00', 2);

-- ========================================
-- 商品信息（products表）
-- ========================================

-- 插入商品信息
INSERT INTO products (product_name, description, price, stock, category, status) VALUES 
('笔记本', '学生专用笔记本，A4大小，100页', 8.50, 200, '文具用品', 'available'),
('圆珠笔', '蓝色圆珠笔，书写流畅', 2.00, 500, '文具用品', 'available'),
('橡皮擦', '4B橡皮擦，擦除干净不留痕', 1.50, 300, '文具用品', 'available'),
('计算器', '科学计算器，适合理工科学生', 45.00, 50, '电子产品', 'available'),
('U盘', '32GB USB3.0 U盘', 35.00, 100, '电子产品', 'available'),
('矿泉水', '550ml纯净水', 2.50, 1000, '饮品食品', 'available'),
('面包', '全麦吐司面包', 6.00, 80, '饮品食品', 'available'),
('咖啡', '速溶咖啡，提神醒脑', 15.00, 150, '饮品食品', 'available'),
('苹果', '新鲜红富士苹果，1个装', 3.00, 200, '饮品食品', 'available'),
('牛奶', '纯牛奶250ml', 4.50, 120, '饮品食品', 'available');

-- ========================================
-- 订单记录（orders表）
-- ========================================

-- 插入订单记录
INSERT INTO orders (order_no, user_id, total_amount, status, created_time) VALUES 
('ORD202403150001', 5, 25.50, 'paid', '2024-03-15 10:30:00'),
('ORD202403160001', 6, 47.00, 'paid', '2024-03-16 14:20:00');

-- ========================================
-- 订单明细（order_items表）
-- ========================================

-- 插入订单明细
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES 
(1, 1, 2, 8.50, 17.00),
(1, 2, 3, 2.00, 6.00),
(1, 3, 1, 1.50, 1.50),
(1, 6, 1, 2.50, 2.50),
(2, 4, 1, 45.00, 45.00),
(2, 2, 1, 2.00, 2.00);

-- ========================================
-- 论坛主题（forum_threads表）
-- ========================================

-- 插入论坛主题
INSERT INTO forum_threads (title, content, author_id, category, reply_count, view_count) VALUES 
('欢迎来到vCampus虚拟校园！', '大家好，欢迎使用vCampus虚拟校园系统！这里是我们的交流平台，可以分享学习心得、讨论课程内容、交流生活感悟。让我们一起营造一个积极向上的学习氛围！', 1, '公告', 2, 150),
('Java程序设计课程讨论', '有同学在学习Java面向对象编程时遇到困难吗？欢迎在这里讨论交流，互相帮助！', 2, '学习交流', 3, 89),
('图书馆新书推荐', '最近图书馆新进了一批计算机类书籍，推荐大家借阅学习。特别推荐《算法导论》和《深入理解计算机系统》。', 3, '图书推荐', 1, 67),
('校园商店优惠活动', '本月校园商店文具用品八折优惠，需要购买学习用品的同学不要错过哦！', 1, '活动通知', 0, 45);

-- ========================================
-- 论坛回复（forum_posts表）
-- ========================================

-- 插入论坛回复
INSERT INTO forum_posts (thread_id, content, author_id) VALUES 
(1, '系统界面很友好，功能很全面，点赞！', 5),
(1, '期待更多功能的上线，加油！', 6),
(2, '我在学习继承和多态时有些困惑，有同学能帮忙解答一下吗？', 7),
(2, '建议多做练习题，理论结合实践才能更好理解。', 8),
(2, '可以参考一下《Java核心技术》这本书，讲得很详细。', 5),
(3, '《算法导论》确实是经典教材，值得深入学习！', 9);

-- ========================================
-- 系统配置（system_configs表）
-- ========================================

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, description, config_type, is_public) VALUES 
('system.name', 'vCampus虚拟校园系统', '系统名称', 'string', true),
('system.version', '1.0.0', '系统版本', 'string', true),
('borrow.max.books', '10', '最大借书数量', 'number', false),
('borrow.days', '7', '借书天数', 'number', false),
('student.max.credits', '30', '学生最大选课学分', 'number', false);

-- ========================================
-- 更新统计数据
-- ========================================

-- 更新课程的已选人数
UPDATE courses SET enrolled_count = (
    SELECT COUNT(*) FROM enrollments WHERE course_id = courses.course_id AND status = 'enrolled'
);

-- 更新图书的可借库存
UPDATE books SET available_stock = total_stock - (
    SELECT COUNT(*) FROM borrow_records WHERE book_id = books.book_id AND status = 1
);

-- 更新论坛主题的回复数
UPDATE forum_threads SET reply_count = (
    SELECT COUNT(*) FROM forum_posts WHERE thread_id = forum_threads.thread_id AND status = 1
);