# 课程表功能调试指南

## 问题分析

课程表没有显示课程的可能原因：

1. **消息监听器冲突**：StudentSchedulePanel和StudentEnrollmentTablePanel都在监听同一个消息类型，后面的会覆盖前面的
2. **数据获取问题**：选课记录数据没有正确获取
3. **节次匹配问题**：课程时间与节次匹配逻辑有问题

## 解决方案

### 1. 修改数据获取方式
- 不再使用消息监听器获取选课记录数据
- 直接从StudentEnrollmentTablePanel获取已加载的选课记录数据
- 避免消息监听器冲突

### 2. 添加详细调试信息
- 在关键步骤添加System.out.println调试信息
- 打印选课记录、课程时间表、节次匹配等详细信息
- 便于排查问题

### 3. 节次匹配逻辑
- 根据数据库中的start_time字段匹配到对应节次
- 支持6个标准节次：1-2节、3-4节、5-6节、7-8节、9-10节、其他时间
- 添加详细的匹配过程调试信息

## 测试步骤

### 1. 启动服务器
```bash
cd C:\vcampus-2
java -cp "libs/*;bin" -Dfile.encoding=UTF-8 server.net.VCampusServer
```

### 2. 启动客户端
```bash
cd C:\vcampus-2
java -cp "libs/*;bin" -Dfile.encoding=UTF-8 client.ui.LoginFrame
```

### 3. 登录测试
- 使用学生账户登录（如：student1/123456）
- 进入"课程管理"模块
- 切换到"选课记录"选项卡
- 确保有选课记录显示

### 4. 测试课程表
- 点击"课程表"按钮
- 查看控制台输出的调试信息
- 检查课程表是否显示课程

## 调试信息说明

### 客户端调试信息
```
设置课程表消息监听器
从选课记录表格面板获取数据
从选课记录表格面板获取到 X 条选课记录
选课记录: 课程名称 状态:enrolled 课程ID:X
已发送获取课程时间表请求，课程数量: X
收到课程时间表成功消息
成功加载 X 条课程时间表
课程时间表: 课程名称 星期X 时间:XX:XX:XX-XX:XX:XX 教室:XXX
=== 更新课程表显示 ===
选课记录数量: X
课程时间表数量: X
课程映射数量: X
查找星期X X节 的课程，总课程数: X
检查课程: 课程名称 星期X 时间:XX:XX:XX
检查课程 课程名称 时间:XX:XX:XX 小时:X 节次:X (X节)
节次 X 时间范围: X-X
节次匹配结果: true/false
找到课程: 课程名称 星期X X节 教室:XXX
```

### 服务器端调试信息
```
[CourseSchedule][Server] 开始查询课程时间表
[CourseSchedule][Server] 查询课程ID列表: [1, 2, 3]
[CourseScheduleDAO] 执行SQL: SELECT cs.*, c.course_name, c.course_code, t.teacher_name FROM course_schedules cs LEFT JOIN courses c ON cs.course_id = c.course_id LEFT JOIN teachers t ON c.teacher_id = t.teacher_id WHERE cs.course_id IN (?,?,?) ORDER BY cs.day_of_week, cs.start_time
[CourseScheduleDAO] 课程ID参数: [1, 2, 3]
[CourseScheduleDAO] 找到课程时间表: 课程名称 星期X 时间:XX:XX:XX-XX:XX:XX
[CourseScheduleDAO] 总共找到 X 条课程时间表
[CourseSchedule][Server] 查询结果数量: X
[CourseSchedule][Server] 查询完成，返回条数=X
[CourseSchedule][Server] 已发送响应: GET_COURSE_SCHEDULES_SUCCESS
```

## 常见问题排查

### 1. 没有选课记录
- 检查数据库中的enrollments表是否有数据
- 检查学生是否已选课
- 检查选课记录状态是否为"enrolled"

### 2. 没有课程时间表
- 检查数据库中的course_schedules表是否有数据
- 检查课程ID是否正确
- 检查服务器端SQL查询是否正常

### 3. 节次匹配失败
- 检查课程开始时间格式
- 检查节次匹配逻辑
- 查看调试信息中的时间匹配过程

### 4. 课程表显示空白
- 检查课程表网格创建逻辑
- 检查课程信息显示逻辑
- 查看调试信息中的课程查找过程

## 数据库数据示例

### 选课记录表 (enrollments)
```sql
INSERT INTO enrollments (student_id, course_id, semester, academic_year, enrollment_time, status, 
                        student_name, student_no, course_name, course_code, credits, teacher_name) VALUES 
(1, 1, '2024春', '2023-2024', '2024-02-15 10:00:00', 'enrolled', '赵六', '2021001', 'Java程序设计', 'CS101', 3, '张三');
```

### 课程时间表 (course_schedules)
```sql
INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, classroom, building, weeks) VALUES 
(1, 1, '08:00:00', '09:40:00', 'A101', '教学楼A', '1-16周'),
(1, 3, '10:00:00', '11:40:00', 'A101', '教学楼A', '1-16周');
```

## 预期结果

课程表应该显示：
- 左侧显示节次（1-2节、3-4节等）
- 顶部显示星期（周一到周日）
- 课程信息包含名称、地点、教师
- 冲突课程用红色背景标识
