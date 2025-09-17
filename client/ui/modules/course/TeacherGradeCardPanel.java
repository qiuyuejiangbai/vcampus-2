package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 教师成绩管理卡片面板
 * 用于显示教师教授的所有课程（教学班）信息，以卡片的形式布局
 * 专门用于成绩管理选项卡
 */
public class TeacherGradeCardPanel extends JPanel {
    private JPanel cardContainer;
    private JScrollPane scrollPane;
    private List<CourseVO> courseList;
    private List<TeacherGradeCard> courseCards;
    private Map<String, List<CourseVO>> coursesByCode;
    private UserVO currentUser;
    private ServerConnection connection;
    private JLabel emptyLabel;
    private Object parentModule; // 保持对父模块的引用

    public TeacherGradeCardPanel(UserVO currentUser, ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        this.courseList = new ArrayList<>();
        this.courseCards = new ArrayList<>();
        this.coursesByCode = new HashMap<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
        
        // 成绩管理面板不独立加载数据，等待父模块提供数据
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建卡片容器
        cardContainer = new JPanel();
        cardContainer.setLayout(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        cardContainer.setBackground(UITheme.WHITE);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(cardContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(UITheme.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(UITheme.WHITE);
        
        // 创建空状态标签
        emptyLabel = new JLabel("暂无课程数据", JLabel.CENTER);
        emptyLabel.setFont(UITheme.DEFAULT_FONT);
        emptyLabel.setForeground(UITheme.LIGHT_GRAY);
        emptyLabel.setVisible(false);
    }

    private void setupLayout() {
        add(scrollPane, BorderLayout.CENTER);
        // emptyLabel 将在 refreshCards 方法中动态显示/隐藏
    }

    private void setupEventHandlers() {
        // 可以添加其他事件处理
    }

    /**
     * 创建测试卡片，确保界面能够显示
     */
    private TeacherGradeCard createTestCard() {
        System.out.println("=== 创建成绩管理测试卡片 ===");
        
        // 创建一个测试课程
        CourseVO testCourse = new CourseVO();
        testCourse.setCourseId(999);
        testCourse.setCourseCode("GRADE001");
        testCourse.setCourseName("成绩管理测试课程");
        testCourse.setCredits(3);
        testCourse.setDepartment("计算机学院");
        testCourse.setTeacherId(1);
        testCourse.setTeacherName("测试教师");
        testCourse.setSemester("2024春");
        testCourse.setAcademicYear("2023-2024");
        testCourse.setClassTime("周一 1-2节");
        testCourse.setLocation("测试教室");
        testCourse.setCapacity(50);
        testCourse.setEnrolledCount(0);
        testCourse.setStatus("active");
        testCourse.setDescription("这是一个成绩管理测试课程");
        
        // 创建测试卡片
        TeacherGradeCard testCard = new TeacherGradeCard(testCourse, this, currentUser, connection);
        
        System.out.println("成绩管理测试卡片创建完成");
        return testCard;
    }
    
    /**
     * 设置消息监听器
     */
    private void setupMessageListener() {
        // 成绩管理面板不设置自己的消息监听器，避免与课程管理面板冲突
        // 数据将通过父模块统一管理
        System.out.println("成绩管理面板跳过消息监听器设置，避免冲突");
    }

    /**
     * 按课程代码分组课程数据
     */
    public void groupCoursesByCode(List<CourseVO> courses) {
        coursesByCode.clear();
        System.out.println("开始分组课程数据（成绩管理），当前用户ID: " + (currentUser != null ? currentUser.getUserId() : "null"));
        System.out.println("课程总数: " + courses.size());
        
        for (CourseVO course : courses) {
            System.out.println("处理课程: " + course.getCourseName() + 
                             ", teacher_id: " + course.getTeacherId() + 
                             ", teacher_name: " + course.getTeacherName());
        }
        
        for (CourseVO course : courses) {
            // 只显示当前教师教授的课程
            if (currentUser != null && course.getTeacherId() != null) {
                // 根据用户ID获取对应的teacher_id
                Integer teacherId = getTeacherIdByUserId(currentUser.getUserId());
                System.out.println("课程: " + course.getCourseName() + ", 课程teacher_id: " + course.getTeacherId() + 
                                 ", 当前用户teacher_id: " + teacherId);
                
                if (teacherId != null && course.getTeacherId().equals(teacherId)) {
                    String courseCode = course.getCourseCode();
                    coursesByCode.computeIfAbsent(courseCode, k -> new ArrayList<>()).add(course);
                    System.out.println("添加课程到分组（成绩管理）: " + course.getCourseName());
                }
            }
        }
        
        System.out.println("分组完成，共 " + coursesByCode.size() + " 个课程组");
    }
    
    /**
     * 根据user_id获取对应的teacher_id
     * 这是一个临时的映射方法，实际项目中应该从数据库查询
     */
    private Integer getTeacherIdByUserId(Integer userId) {
        if (userId == null) return null;
        
        // 根据数据库数据映射：张三(user_id=2->teacher_id=1), 李四(user_id=3->teacher_id=2), 王五(user_id=4->teacher_id=3)
        switch (userId) {
            case 2: return 1; // 张三
            case 3: return 2; // 李四
            case 4: return 3; // 王五
            default: return null;
        }
    }

    /**
     * 加载课程数据
     */
    private void loadCourseData() {
        System.out.println("=== 开始加载课程数据（成绩管理） ===");
        System.out.println("连接状态: " + (connection != null ? "已连接" : "未连接"));
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getLoginId() : "null"));
        System.out.println("用户ID: " + (currentUser != null ? currentUser.getUserId() : "null"));
        
        try {
            if (connection == null) {
                System.err.println("连接为空，无法加载课程数据");
                courseList.clear();
                refreshCards();
                return;
            }
            
            // 确保连接到服务器
            if (!connection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!connection.connect()) {
                    System.err.println("无法连接到服务器");
                    courseList.clear();
                    refreshCards();
                    return;
                }
            }
            
            // 创建获取课程列表的消息
            Message request = new Message();
            request.setType(MessageType.GET_ALL_COURSES_REQUEST);
            request.setData(null);
            
            // 发送消息到服务器
            if (connection.sendMessage(request)) {
                System.out.println("已发送获取课程列表请求（成绩管理）");
                // 等待服务器响应，这里暂时清空卡片
                courseList.clear();
                refreshCards();
                System.out.println("等待服务器响应课程数据...");
            } else {
                System.err.println("发送获取课程数据请求失败");
                courseList.clear();
                refreshCards();
            }
        } catch (Exception e) {
            System.err.println("加载课程数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            courseList.clear();
            refreshCards();
        }
        System.out.println("=== 课程数据加载完成（成绩管理） ===");
    }

    /**
     * 刷新卡片显示
     */
    public void refreshCards() {
        System.out.println("=== 刷新卡片显示（成绩管理） ===");
        System.out.println("课程组数量: " + coursesByCode.size());
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getLoginId() : "null"));
        System.out.println("连接状态: " + (connection != null ? "已连接" : "未连接"));
        
        // 清空现有卡片
        cardContainer.removeAll();
        courseCards.clear();
        
        if (coursesByCode.isEmpty()) {
            System.out.println("没有课程数据，保持成绩管理测试卡片显示");
            // 确保滚动面板可见
            remove(emptyLabel);
            add(scrollPane, BorderLayout.CENTER);
            emptyLabel.setVisible(false);
            scrollPane.setVisible(true);
            
            // 重新添加测试卡片
            TeacherGradeCard testCard = createTestCard();
            cardContainer.add(testCard);
            courseCards.add(testCard);
            revalidate();
            repaint();
            return;
        }
        
        System.out.println("开始创建成绩管理课程卡片");
        // 移除空状态标签，添加滚动面板
        remove(emptyLabel);
        add(scrollPane, BorderLayout.CENTER);
        emptyLabel.setVisible(false);
        scrollPane.setVisible(true);
        
        // 为每个课程代码创建卡片
        for (Map.Entry<String, List<CourseVO>> entry : coursesByCode.entrySet()) {
            List<CourseVO> courses = entry.getValue();
            
            // 使用第一个课程作为代表（因为同一课程代码的课程信息基本相同）
            CourseVO representativeCourse = courses.get(0);
            System.out.println("创建成绩管理课程卡片: " + representativeCourse.getCourseName() + 
                             " (teacher_id: " + representativeCourse.getTeacherId() + ")");
            
            // 创建教师成绩管理课程卡片
            TeacherGradeCard card = new TeacherGradeCard(representativeCourse, this, currentUser, connection);
            courseCards.add(card);
            cardContainer.add(card);
        }
        
        System.out.println("创建了 " + courseCards.size() + " 个成绩管理课程卡片");
        
        // 刷新布局
        cardContainer.revalidate();
        cardContainer.repaint();
        
        System.out.println("=== 成绩管理卡片刷新完成 ===");
    }

    /**
     * 根据课程名称搜索课程
     */
    public void searchByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            refreshCards();
            return;
        }
        
        // 清空现有卡片
        cardContainer.removeAll();
        courseCards.clear();
        
        if (coursesByCode.isEmpty()) {
            emptyLabel.setVisible(true);
            scrollPane.setVisible(false);
            return;
        }
        
        emptyLabel.setVisible(false);
        scrollPane.setVisible(true);
        
        // 搜索匹配的课程
        for (Map.Entry<String, List<CourseVO>> entry : coursesByCode.entrySet()) {
            List<CourseVO> courses = entry.getValue();
            
            // 检查课程名称是否包含搜索关键词
            boolean matches = false;
            for (CourseVO course : courses) {
                if (course.getCourseName().toLowerCase().contains(courseName.toLowerCase())) {
                    matches = true;
                    break;
                }
            }
            
            if (matches) {
                CourseVO representativeCourse = courses.get(0);
                TeacherGradeCard card = new TeacherGradeCard(representativeCourse, this, currentUser, connection);
                courseCards.add(card);
                cardContainer.add(card);
            }
        }
        
        // 刷新布局
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        loadCourseData();
    }

    /**
     * 获取课程数量
     */
    public int getCourseCount() {
        return coursesByCode.size();
    }

    /**
     * 设置当前用户
     */
    public void setCurrentUser(UserVO currentUser) {
        this.currentUser = currentUser;
        // 重新分组课程数据
        groupCoursesByCode(courseList);
        refreshCards();
    }

    /**
     * 设置服务器连接
     */
    public void setConnection(ServerConnection connection) {
        this.connection = connection;
        setupMessageListener();
    }
    
    /**
     * 设置父模块引用
     */
    public void setParentModule(Object parentModule) {
        this.parentModule = parentModule;
        System.out.println("设置父模块引用（成绩管理）: " + (parentModule != null ? parentModule.getClass().getSimpleName() : "null"));
    }

    /**
     * 获取课程卡片列表
     */
    public List<TeacherGradeCard> getCourseCards() {
        return courseCards;
    }

    /**
     * 设置课程数据（从父模块接收）
     */
    public void setCourseData(List<CourseVO> courses) {
        System.out.println("=== 成绩管理面板接收课程数据 ===");
        if (courses != null) {
            courseList.clear();
            courseList.addAll(courses);
            groupCoursesByCode(courses);
            refreshCards();
            System.out.println("成绩管理面板成功接收并处理了 " + courses.size() + " 门课程");
        }
    }

    /**
     * 处理课程卡片点击事件
     */
    public void onCourseCardClicked(CourseVO course) {
        System.out.println("=== TeacherGradeCardPanel.onCourseCardClicked 被调用 ===");
        System.out.println("点击了成绩管理课程卡片: " + (course != null ? course.getCourseName() : "null"));
        
        if (parentModule != null) {
            try {
                // 调用父模块的显示成绩管理方法
                java.lang.reflect.Method method = parentModule.getClass().getMethod("showGradeManagement", CourseVO.class);
                method.invoke(parentModule, course);
                System.out.println("成功调用父模块的成绩管理方法");
            } catch (Exception e) {
                System.err.println("调用父模块成绩管理方法失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("父模块为空，无法显示成绩管理界面");
        }
    }
}
