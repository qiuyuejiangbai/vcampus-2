-- vCampus 虚拟校园系统初始数据
USE vcampus;

-- 插入管理员账户 (密码: admin123)
INSERT INTO users (login_id, name, password, role, status, phone, email, balance) VALUES 
('admin', '系统管理员', '0192023a7bbd73250516f069df18b500', 2, 1, '13800000000', 'admin@vcampus.edu.cn', 10000.00);

-- 插入教师账户 (密码: teacher123)
INSERT INTO users (login_id, name, password, role, status, phone, email, balance) VALUES 
('T001', '张三', 'a426dcf72ba25d046591f81a5495eab7', 1, 1, '13800000001', 'zhangsan@vcampus.edu.cn', 5000.00),
('T002', '李四', 'a426dcf72ba25d046591f81a5495eab7', 1, 1, '13800000002', 'lisi@vcampus.edu.cn', 5000.00),
('T003', '王五', 'a426dcf72ba25d046591f81a5495eab7', 1, 1, '13800000003', 'wangwu@vcampus.edu.cn', 5000.00);

-- 插入学生账户 (密码: student123)
INSERT INTO users (login_id, name, password, role, status, phone, email, balance) VALUES 
('2021001', '赵六', 'ad6a280417a0f533d8b670c61667e1a0', 0, 1, '13800001001', 'zhaoliu@student.vcampus.edu.cn', 1000.00),
('2021002', '钱七', 'ad6a280417a0f533d8b670c61667e1a0', 0, 1, '13800001002', 'qianqi@student.vcampus.edu.cn', 1000.00),
('2021003', '孙八', 'ad6a280417a0f533d8b670c61667e1a0', 0, 1, '13800001003', 'sunba@student.vcampus.edu.cn', 1000.00),
('2021004', '周九', 'ad6a280417a0f533d8b670c61667e1a0', 0, 1, '13800001004', 'zhoujiu@student.vcampus.edu.cn', 1000.00),
('2021005', '吴十', 'ad6a280417a0f533d8b670c61667e1a0', 0, 1, '13800001005', 'wushi@student.vcampus.edu.cn', 1000.00);

-- 插入学生信息
INSERT INTO students (user_id, student_no, major, class_name, grade, enrollment_year) VALUES 
(5, '2021001', '计算机科学与技术', '计科2101班', '2021级', 2021),
(6, '2021002', '软件工程', '软工2101班', '2021级', 2021),
(7, '2021003', '信息安全', '信安2101班', '2021级', 2021),
(8, '2021004', '计算机科学与技术', '计科2101班', '2021级', 2021),
(9, '2021005', '软件工程', '软工2101班', '2021级', 2021);

-- 插入课程信息
INSERT INTO courses (course_code, course_name, credits, department, teacher_id, semester, description, capacity, enrolled_count) VALUES 
('CS101', 'Java程序设计', 3, '计算机学院', 2, '2024春', 'Java编程语言基础课程，包括面向对象编程、集合框架、异常处理等内容。', 50, 3),
('CS102', '数据结构与算法', 4, '计算机学院', 3, '2024春', '数据结构基础知识和常用算法的学习，包括线性表、树、图等数据结构。', 45, 2),
('CS103', '数据库系统原理', 3, '计算机学院', 4, '2024春', '关系型数据库的设计原理、SQL语言、事务处理和数据库优化。', 40, 1),
('CS104', '计算机网络', 3, '计算机学院', 2, '2024春', '计算机网络基础知识，包括TCP/IP协议、网络编程等。', 35, 0),
('CS105', '软件工程', 3, '计算机学院', 3, '2024春', '软件开发生命周期、需求分析、系统设计和项目管理。', 30, 0);

-- 插入选课记录
INSERT INTO enrollments (student_id, course_id, enrollment_time, grade, status) VALUES 
(1, 1, '2024-02-15 10:00:00', 85.5, 2),
(2, 1, '2024-02-15 10:30:00', 92.0, 2),
(3, 1, '2024-02-15 11:00:00', 78.5, 2),
(1, 2, '2024-02-16 09:00:00', 88.0, 2),
(2, 2, '2024-02-16 09:30:00', 95.5, 2),
(3, 3, '2024-02-17 14:00:00', 82.0, 1);

-- 插入图书信息
INSERT INTO books (isbn, title, author, publisher, category, total_stock, available_stock) VALUES 
('978-7-111-54742-6', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', '计算机技术', 5, 4),
('978-7-111-54743-3', 'Java核心技术 卷II', 'Cay S. Horstmann', '机械工业出版社', '计算机技术', 3, 3),
('978-7-115-28969-4', '算法导论', 'Thomas H. Cormen', '人民邮电出版社', '计算机技术', 4, 3),
('978-7-111-55842-2', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '计算机技术', 3, 2),
('978-7-115-35208-1', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '数据库', 6, 5),
('978-7-111-26969-4', '设计模式', 'Erich Gamma', '机械工业出版社', '软件工程', 2, 2),
('978-7-115-48904-4', 'Spring实战', 'Craig Walls', '人民邮电出版社', '计算机技术', 4, 4),
('978-7-121-31065-1', '计算机网络：自顶向下方法', 'James F. Kurose', '电子工业出版社', '计算机网络', 3, 3);

-- 插入借阅记录
INSERT INTO borrow_records (user_id, book_id, borrow_time, due_time, return_time, status) VALUES 
(5, 1, '2024-03-01 10:00:00', '2024-04-01 10:00:00', NULL, 1),
(6, 4, '2024-03-05 14:30:00', '2024-04-05 14:30:00', NULL, 1),
(7, 5, '2024-03-10 09:15:00', '2024-04-10 09:15:00', '2024-03-25 16:20:00', 2);

-- 插入商品信息
INSERT INTO products (product_name, description, price, stock, category) VALUES 
('笔记本', '学生专用笔记本，A4大小，100页', 8.50, 200, '文具用品'),
('圆珠笔', '蓝色圆珠笔，书写流畅', 2.00, 500, '文具用品'),
('橡皮擦', '4B橡皮擦，擦除干净不留痕', 1.50, 300, '文具用品'),
('计算器', '科学计算器，适合理工科学生', 45.00, 50, '电子产品'),
('U盘', '32GB USB3.0 U盘', 35.00, 100, '电子产品'),
('矿泉水', '550ml纯净水', 2.50, 1000, '饮品食品'),
('面包', '全麦吐司面包', 6.00, 80, '饮品食品'),
('咖啡', '速溶咖啡，提神醒脑', 15.00, 150, '饮品食品'),
('苹果', '新鲜红富士苹果，1个装', 3.00, 200, '饮品食品'),
('牛奶', '纯牛奶250ml', 4.50, 120, '饮品食品');

-- 插入订单记录
INSERT INTO orders (user_id, total_amount, status, created_time) VALUES 
(5, 25.50, 1, '2024-03-15 10:30:00'),
(6, 47.00, 1, '2024-03-16 14:20:00');

-- 插入订单明细
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES 
(1, 1, 2, 8.50, 17.00),
(1, 2, 3, 2.00, 6.00),
(1, 3, 1, 1.50, 1.50),
(1, 6, 1, 2.50, 2.50),
(2, 4, 1, 45.00, 45.00),
(2, 2, 1, 2.00, 2.00);

-- 插入论坛主题
INSERT INTO forum_threads (title, content, author_id, reply_count) VALUES 
('欢迎来到vCampus虚拟校园！', '大家好，欢迎使用vCampus虚拟校园系统！这里是我们的交流平台，可以分享学习心得、讨论课程内容、交流生活感悟。让我们一起营造一个积极向上的学习氛围！', 1, 2),
('Java程序设计课程讨论', '有同学在学习Java面向对象编程时遇到困难吗？欢迎在这里讨论交流，互相帮助！', 2, 3),
('图书馆新书推荐', '最近图书馆新进了一批计算机类书籍，推荐大家借阅学习。特别推荐《算法导论》和《深入理解计算机系统》。', 3, 1),
('校园商店优惠活动', '本月校园商店文具用品八折优惠，需要购买学习用品的同学不要错过哦！', 1, 0);

-- 插入论坛回复
INSERT INTO forum_posts (thread_id, content, author_id) VALUES 
(1, '系统界面很友好，功能很全面，点赞！', 5),
(1, '期待更多功能的上线，加油！', 6),
(2, '我在学习继承和多态时有些困惑，有同学能帮忙解答一下吗？', 7),
(2, '建议多做练习题，理论结合实践才能更好理解。', 8),
(2, '可以参考一下《Java核心技术》这本书，讲得很详细。', 5),
(3, '《算法导论》确实是经典教材，值得深入学习！', 9);

-- 更新课程的已选人数
UPDATE courses SET enrolled_count = (
    SELECT COUNT(*) FROM enrollments WHERE course_id = courses.course_id AND status IN (1, 2)
);

-- 更新图书的可借库存
UPDATE books SET available_stock = total_stock - (
    SELECT COUNT(*) FROM borrow_records WHERE book_id = books.book_id AND status = 1
);

-- 更新论坛主题的回复数
UPDATE forum_threads SET reply_count = (
    SELECT COUNT(*) FROM forum_posts WHERE thread_id = forum_threads.thread_id AND status = 1
);
