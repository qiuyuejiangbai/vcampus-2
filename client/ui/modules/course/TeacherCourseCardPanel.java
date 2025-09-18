package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;
import client.ui.modules.TeacherCourseModule;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 教师课程卡片面板
 * 用于显示教师教授的所有课程（教学班）信息，以卡片的形式布局
 */
public class TeacherCourseCardPanel extends JPanel {
    private JPanel cardContainer;
    private JScrollPane scrollPane;
    private List<CourseVO> courseList;
    private List<TeacherCourseCard> courseCards;
    private Map<String, List<CourseVO>> coursesByCode;
    private UserVO currentUser;
    private ServerConnection connection;
    private JLabel emptyLabel;
    private Object parentModule; // 保持对父模块的引用

    public TeacherCourseCardPanel(UserVO currentUser, ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        this.courseList = new ArrayList<>();
        this.courseCards = new ArrayList<>();
        this.coursesByCode = new HashMap<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
        
        // 延迟加载课程数据，确保消息监听器已设置
        SwingUtilities.invokeLater(() -> {
            loadCourseData();
        });
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
     * 设置消息监听器
     */
    private void setupMessageListener() {
        if (connection != null) {
            // 设置课程列表响应监听器
            connection.setMessageListener(MessageType.GET_ALL_COURSES_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("收到课程列表响应消息");
                    if (message.getData() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseData = (Map<String, Object>) message.getData();
                        
                        // 获取课程列表
                        if (responseData.get("courses") instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<CourseVO> courses = (List<CourseVO>) responseData.get("courses");
                            courseList.clear();
                            courseList.addAll(courses);
                            
                            // 按课程代码分组教学班数据
                            groupCoursesByCode(courses);
                            
                            refreshCards();
                            System.out.println("教师成功加载 " + courses.size() + " 门课程");
                            
                            // 同时更新成绩管理面板的数据
                            updateGradeCardPanelData(courses);
                            
                            // 通知父模块更新状态标签
                            notifyParentModuleUpdateStatus();
                        }
                    } else if (message.getData() instanceof List) {
                        // 兼容旧的响应格式
                        @SuppressWarnings("unchecked")
                        List<CourseVO> courses = (List<CourseVO>) message.getData();
                        courseList.clear();
                        courseList.addAll(courses);
                        
                        // 按课程代码分组教学班数据
                        groupCoursesByCode(courses);
                        
                        refreshCards();
                        System.out.println("教师成功加载 " + courses.size() + " 门课程");
                        
                        // 同时更新成绩管理面板的数据
                        updateGradeCardPanelData(courses);
                        
                        // 通知父模块更新状态标签
                        notifyParentModuleUpdateStatus();
                    } else {
                        System.out.println("课程数据格式错误: " + message.getData());
                    }
                });
            });
        } else {
            System.out.println("连接为空，无法设置消息监听器");
        }
    }

    /**
     * 按课程代码分组课程数据
     */
    public void groupCoursesByCode(List<CourseVO> courses) {
        coursesByCode.clear();
        System.out.println("开始分组课程数据，当前用户ID: " + (currentUser != null ? currentUser.getUserId() : "null"));
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
                    System.out.println("添加课程到分组: " + course.getCourseName());
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
        System.out.println("=== 开始加载课程数据 ===");
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
                System.out.println("已发送获取课程列表请求");
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
        System.out.println("=== 课程数据加载完成 ===");
    }

    /**
     * 刷新卡片显示
     */
    public void  refreshCards() {
        System.out.println("=== 刷新卡片显示 ===");
        System.out.println("课程组数量: " + coursesByCode.size());
        System.out.println("当前用户: " + (currentUser != null ? currentUser.getLoginId() : "null"));
        System.out.println("连接状态: " + (connection != null ? "已连接" : "未连接"));
        
        // 清空现有卡片
        cardContainer.removeAll();
        courseCards.clear();
        
        if (coursesByCode.isEmpty()) {
            System.out.println("没有课程数据，保持测试卡片显示");
            // 确保滚动面板可见
            remove(emptyLabel);
            add(scrollPane, BorderLayout.CENTER);
            emptyLabel.setVisible(false);
            scrollPane.setVisible(true);
            
            // 显示空状态
            JLabel emptyLabel = new JLabel("暂无课程数据");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardContainer.add(emptyLabel);
            revalidate();
            repaint();
            return;
        }
        
        System.out.println("开始创建课程卡片");
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
            System.out.println("创建课程卡片: " + representativeCourse.getCourseName() + 
                             " (teacher_id: " + representativeCourse.getTeacherId() + ")");
            
            // 创建教师课程卡片
            TeacherCourseCard card = new TeacherCourseCard(representativeCourse, this, currentUser, connection);
            courseCards.add(card);
            cardContainer.add(card);
        }
        
        System.out.println("创建了 " + courseCards.size() + " 个课程卡片");
        
        // 刷新布局
        cardContainer.revalidate();
        cardContainer.repaint();
        
        System.out.println("=== 卡片刷新完成 ===");
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
                TeacherCourseCard card = new TeacherCourseCard(representativeCourse, this, currentUser, connection);
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
        System.out.println("设置父模块引用: " + (parentModule != null ? parentModule.getClass().getSimpleName() : "null"));
    }

    /**
     * 获取课程卡片列表
     */
    public List<TeacherCourseCard> getCourseCards() {
        return courseCards;
    }
    
    /**
     * 通知父模块更新状态标签
     */
    private void notifyParentModuleUpdateStatus() {
        if (parentModule != null && parentModule instanceof TeacherCourseModule) {
            TeacherCourseModule module = (TeacherCourseModule) parentModule;
            module.updateStatusLabels();
        }
    }

    /**
     * 更新成绩管理面板的数据
     */
    private void updateGradeCardPanelData(List<CourseVO> courses) {
        if (parentModule != null) {
            try {
                // 通过反射获取成绩管理面板并更新数据
                java.lang.reflect.Field gradeCardPanelField = parentModule.getClass().getDeclaredField("gradeCardPanel");
                gradeCardPanelField.setAccessible(true);
                Object gradeCardPanel = gradeCardPanelField.get(parentModule);
                
                if (gradeCardPanel != null) {
                    // 调用成绩管理面板的数据更新方法
                    java.lang.reflect.Method setCourseDataMethod = gradeCardPanel.getClass().getMethod("setCourseData", List.class);
                    setCourseDataMethod.invoke(gradeCardPanel, courses);
                    
                    System.out.println("成绩管理面板数据已同步更新");
                }
            } catch (Exception e) {
                System.err.println("更新成绩管理面板数据失败: " + e.getMessage());
            }
        }
    }

    /**
     * 处理课程卡片点击事件
     */
    public void onCourseCardClicked(CourseVO course) {
        System.out.println("=== TeacherCourseCardPanel.onCourseCardClicked 被调用 ===");
        System.out.println("点击了课程卡片: " + (course != null ? course.getCourseName() : "null"));
        
        // 使用直接引用调用父模块方法
        if (parentModule != null) {
            System.out.println("使用直接引用调用父模块: " + parentModule.getClass().getSimpleName());
            try {
                java.lang.reflect.Method method = parentModule.getClass().getMethod("showStudentList", CourseVO.class);
                method.invoke(parentModule, course);
                System.out.println("成功通过直接引用调用 showStudentList 方法");
                return;
            } catch (Exception ex) {
                System.err.println("通过直接引用调用showStudentList方法失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        // 备用方案：通过父容器查找
        System.out.println("尝试备用方案：通过父容器查找");
        if (getParent() != null) {
            Container parent = getParent();
            int level = 0;
            while (parent != null) {
                System.out.println("查找父容器 [" + level + "]: " + parent.getClass().getName());
                
                if (parent.getClass().getName().equals("client.ui.modules.TeacherCourseModule")) {
                    System.out.println("找到 TeacherCourseModule，准备调用 showStudentList");
                    try {
                        java.lang.reflect.Method method = parent.getClass().getMethod("showStudentList", CourseVO.class);
                        method.invoke(parent, course);
                        System.out.println("成功调用 showStudentList 方法");
                    } catch (Exception ex) {
                        System.err.println("无法调用showStudentList方法: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    break;
                }
                parent = parent.getParent();
                level++;
                
                // 防止无限循环
                if (level > 10) {
                    System.err.println("查找父容器层级过深，停止查找");
                    break;
                }
            }
            
            if (parent == null) {
                System.err.println("未找到 TeacherCourseModule 父容器");
            }
        } else {
            System.err.println("getParent() 返回 null，且无直接引用");
        }
    }
}
