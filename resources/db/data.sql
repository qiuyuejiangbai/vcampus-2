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
-- 初始化若干示例文献（请先在服务器准备好这些文件）
-- ========================================

INSERT INTO library_documents
(title, authors, year, category, subject, keywords, abstract_txt, file_type, file_size, storage_path, uploader_id, is_public)
VALUES
('基于机器学习方法的操作系统优化研究综述', '黄卓懿；彭龙；徐浩；李琢；刘晓东；刘敏；余杰', 2025, '期刊', '计算机', '操作系统;机器学习;进程调度;内存管理;',
 '随着计算机硬件与应用的快速发展，现代操作系统在硬件资源管理方面面临巨大挑战。机器学习为操作系统优化提供了创新解决方案。综述基于机器学习的操作系统资源管理方法，总结机器学习模型在操作系统中的具体应用案例，分析其优势与局限性，并探讨当前研究面临的挑战。最后对未来研究方向及应用前景进行展望.', 'pdf', 824532,
 '/docs/计算机/2025/基于机器学习方法的操作系统优化研究综述_黄卓懿.pdf', 2, 1),
 ('基于U-Net神经网络的采煤塌陷类型识别', '杨书平', 2025, '期刊', '计算机', 'U-Net;深度学习;塌陷区识别;采煤塌陷区;',
  '针对地下煤炭资源被开采利用，出现大规模采空区，产生大面积沉陷，威胁到矿区的安全生产和人们的人身安全问题。本研究首先对鹤岗矿区遥感影像进行预处理、数据集的制作，然后用U-Net卷积神经网络训练模型，修改训练参数，进行塌陷坑、地裂缝、塌陷盆地的识别，整体精度达到97.58%，表明此次模型训练结果较准确。从而得到鹤岗矿区地裂缝、塌陷坑、塌陷盆地的具体分布图，发现盆地的查准率最高，地裂缝识别效果其次，塌陷坑由于本身形状及特征的因素影响，识别的效果差于其他两类地物，分析上述三类地物的精度指标，证明了U-Net网络模型适合于本次研究的研究，为复垦、治理采空塌陷区提供基础资料具有重要的意义。', 'pdf', 824532,
  '/docs/计算机/2025/基于U-Net神经网络的采煤塌陷类型识别_杨书平.pdf', 2, 1),
 ('论叙事静默的双重组合', '涂年根', 2025, '期刊', '社会科学', '叙事静默;双重组合;虚与实;简洁与繁复;',
  '叙事静默的双重组合主要是指在静默叙事中，静默作为一种隐形的存在，总是以双重组合的方式出现，即“虚与实”的相互映衬、“简洁与繁复”相互依存，这种双重的组合方式之间相互印证，缺一不可。分析文本的过程中，只有同时考察两者之间的关系，才能发现静默存在的价值与意义。叙事静默双重组合的存在可以增强叙事交流的层次感、深化主题的表达，同时也会激发读者的想象力，实现对文本中“无言意义”活动的阐释。', 'pdf', 824532,
  '/docs/社会科学/2025/论叙事静默的双重组合_涂年根.pdf', 2, 1),
 ('论基特勒对《德古拉》的媒介化解读', '张梦杨', 2022, '期刊', '哲学', '《德古拉》;基特勒;媒介技术;话语网络;新女性;',
  '斯托克的《德古拉》是吸血鬼文学作品中的经典,围绕它的研究以考察作品主题和分析吸血鬼形象的嬗变为代表。媒介理论家基特勒注意到《德古拉》中出现的新型媒介设备,他对《德古拉》展开的媒介化解读,将德古拉的死因归为打字机和留声机的使用,掌握打字技术的米娜·哈克在实现媒介技术融合的同时,成为拥有书写能力的“新女性”。基特勒提出的话语网络概念是理解《德古拉》媒介化解读的关键。话语网络1900的“离散化”“媒介化”特征在《德古拉》中得到诠释,基特勒对打字机与女性关系的思考,说明了女性在话语网络1800和1900的身份转换,以米娜·哈克为典型的女性借由打字机参与了话语的流通与建构。', 'pdf', 824532,
  '/docs/哲学/2022/论基特勒对《德古拉》的媒介化解读_张梦杨.pdf', 2, 1);

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

-- 先插入论坛板块
INSERT INTO forum_sections (name, description, sort_order, status) VALUES
('学术交流', '课程/学术问题讨论', 1, 1),
('校园生活', '生活/活动/社团', 2, 1),
('二手交易', '闲置物品交易', 3, 1),
('失物招领', '失物与招领信息', 4, 1),
('求助咨询', '问题求助与咨询', 5, 1);

-- 插入论坛主题（补充 section_id / is_essence / like_count / favorite_count / last_post_time）
INSERT INTO forum_threads (title, content, author_id, section_id, category, is_essence, reply_count, view_count, like_count, favorite_count, is_pinned, is_locked, status, created_time, last_post_time) VALUES 
('欢迎来到vCampus虚拟校园！', '大家好，欢迎使用vCampus虚拟校园系统！这里是我们的交流平台，可以分享学习心得、讨论课程内容、交流生活感悟。让我们一起营造一个积极向上的学习氛围！', 1, 1, '公告', TRUE, 2, 150, 2, 5, TRUE, FALSE, 1, '2024-03-05 09:00:00', '2024-03-07 14:20:00'),
('Java程序设计课程讨论', '有同学在学习Java面向对象编程时遇到困难吗？欢迎在这里讨论交流，互相帮助！', 2, 1, '学习交流', FALSE, 3, 89, 1, 2, FALSE, FALSE, 1, '2024-03-08 10:15:00', '2024-03-10 09:15:00'),
('图书馆新书推荐', '最近图书馆新进了一批计算机类书籍，推荐大家借阅学习。特别推荐《算法导论》和《深入理解计算机系统》。', 3, 2, '图书推荐', FALSE, 1, 67, 0, 1, FALSE, FALSE, 1, '2024-03-09 15:30:00', '2024-03-10 11:30:00'),
('校园商店优惠活动', '本月校园商店文具用品八折优惠，需要购买学习用品的同学不要错过哦！', 1, 2, '活动通知', FALSE, 0, 45, 0, 0, FALSE, FALSE, 1, '2024-03-12 08:45:00', NULL);

-- ========================================
-- 插入论坛回复（补充 like_count / 层级 / 路径）
INSERT INTO forum_posts (thread_id, content, author_id, parent_post_id, reply_level, reply_path, like_count, created_time) VALUES 
(1, '系统界面很友好，功能很全面，点赞！', 5, NULL, 0, '1', 1, '2024-03-06 10:30:00'),
(1, '期待更多功能的上线，加油！', 6, NULL, 0, '2', 0, '2024-03-07 14:20:00'),
(2, '我在学习继承和多态时有些困惑，有同学能帮忙解答一下吗？', 7, NULL, 0, '3', 0, '2024-03-09 16:45:00'),
(2, '建议多做练习题，理论结合实践才能更好理解。', 8, 3, 1, '3/4', 0, '2024-03-09 17:10:00'),
(2, '可以参考一下《Java核心技术》这本书，讲得很详细。', 5, NULL, 0, '5', 1, '2024-03-10 09:15:00'),
(3, '《算法导论》确实是经典教材，值得深入学习！', 9, NULL, 0, '6', 0, '2024-03-10 11:30:00');

-- 标签与主题-标签
INSERT INTO forum_tags (tag_name) VALUES ('Java'), ('学习'), ('公告'), ('活动'), ('图书');
INSERT INTO forum_thread_tags (thread_id, tag_id) VALUES 
(1, 3), (1, 2),
(2, 1), (2, 2),
(3, 5),
(4, 4);

-- 点赞（主题/回复）示例
INSERT INTO forum_likes (entity_type, entity_id, user_id, created_time) VALUES 
('thread', 1, 5, '2024-03-06 10:35:00'),
('thread', 1, 6, '2024-03-07 14:25:00'),
('thread', 2, 7, '2024-03-09 16:50:00'),
('post', 1, 6, '2024-03-07 14:25:00'),
('post', 3, 8, '2024-03-09 17:15:00');

-- 收藏示例
INSERT INTO forum_favorites (thread_id, user_id, created_time) VALUES 
(1, 5, '2024-03-06 10:40:00'),
(2, 6, '2024-03-08 10:20:00');

-- 附件示例（为主题与回复各插入一个）
INSERT INTO forum_attachments (entity_type, entity_id, file_name, file_type, file_size, storage_path, uploader_id) VALUES 
('thread', 2, 'java-oop-tips.pdf', 'application/pdf', 204800, '/uploads/forum/2/java-oop-tips.pdf', 2),
('post', 3, 'question.png', 'image/png', 102400, '/uploads/forum/posts/3/question.png', 7);

-- 公告示例
INSERT INTO forum_announcements (title, content, is_pinned, status, created_time) VALUES 
('欢迎使用校园论坛系统！', '请遵守论坛规则，文明发言。', TRUE, 1, '2024-03-05 08:30:00'),
('期末考试安排已发布', '请进入教务系统查看详细安排。', TRUE, 1, '2024-03-08 09:00:00');

-- 浏览日志（简化）
INSERT INTO forum_views (thread_id, user_id, ip_hash, viewed_at) VALUES 
(1, 5, 'iphash-a', '2024-03-06 10:30:00'),
(1, 6, 'iphash-b', '2024-03-07 14:20:00'),
(2, 7, 'iphash-c', '2024-03-09 16:45:00');

-- 举报与日志示例
INSERT INTO forum_reports (entity_type, entity_id, reporter_id, reason, status, created_time) VALUES 
('post', 4, 5, '不友善言论', 0, '2024-03-09 17:20:00');
INSERT INTO forum_moderation_logs (entity_type, entity_id, action, operator_id, detail, created_time) VALUES 
('thread', 1, 'pin', 1, '置顶公告', '2024-03-05 09:05:00');

-- 通知示例
INSERT INTO forum_notifications (user_id, type, payload, is_read, created_time) VALUES 
(2, 'reply', JSON_OBJECT('threadId', 2, 'postId', 3, 'fromUser', 7), FALSE, '2024-03-09 16:50:00'),
(3, 'like', JSON_OBJECT('entity','thread','entityId', 3, 'fromUser', 9), TRUE, '2024-03-10 11:35:00');

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