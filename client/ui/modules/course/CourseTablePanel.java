package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class CourseTablePanel extends JPanel {
    private JTable courseTable;  // 课程表格
    private JScrollPane scrollTablePane;  // 表格滚动面板
    private CourseVO selectedCourse;     // 当前选中的课程
    private DefaultTableModel tableModel; // 表格模型
    private List<CourseVO> courseList;   // 课程数据列表
    private final ServerConnection serverConnection; // 服务器连接
    private UserVO currentUser; // 当前用户

    // 课程教学班卡片组件
    private CourseClassCardPanel courseClassCardPanel;  // 教学班卡片面板

    public CourseTablePanel() {
        this.serverConnection = ServerConnection.getInstance();
        this.courseList = new ArrayList<>();
        initComponents();      // 初始化组件
        setupLayout();         // 设置布局
        setupEventHandlers();  // 设置事件处理器
        setupMessageListener(); // 设置消息监听器
        loadCourseData();      // 加载课程数据
    }
    
    public CourseTablePanel(UserVO currentUser) {
        this.serverConnection = ServerConnection.getInstance();
        this.courseList = new ArrayList<>();
        this.currentUser = currentUser;
        initComponents();      // 初始化组件
        setupLayout();         // 设置布局
        setupEventHandlers();  // 设置事件处理器
        setupMessageListener(); // 设置消息监听器
        loadCourseData();      // 加载课程数据
    }
    
    /**
     * 设置当前用户
     * @param currentUser 当前用户
     */
    public void setCurrentUser(UserVO currentUser) {
        this.currentUser = currentUser;
        if (courseClassCardPanel != null) {
            courseClassCardPanel.setCurrentUser(currentUser);
            // 如果当前有选中的课程，重新显示教学班以更新按钮
            if (selectedCourse != null) {
                showCourseClasses(selectedCourse.getCourseCode());
            }
        }
    }

    private void setupEventHandlers() {
        // 添加表格选择事件监听器
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < courseList.size()) {
                    selectedCourse = courseList.get(selectedRow);
                    System.out.println("选中课程: " + selectedCourse.getCourseName());
                    showCourseClasses(selectedCourse.getCourseCode());
                } else {
                    hideCourseClasses();
                }
            }
        });
    }


    /*
     * 当从服务器接收到"获取所有课程成功"的消息时，将接收到的课程列表数据更新到客户端的courseList中
     * 并刷新界面表格显示，最后打印加载的课程数量
     */
    private void setupMessageListener() {
        // 设置课程列表响应监听器
        serverConnection.setMessageListener(MessageType.GET_ALL_COURSES_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<CourseVO> courses = (List<CourseVO>) message.getData();
                    courseList.clear();
                    courseList.addAll(courses);
                    
                    // 按课程代码分组教学班数据
                    courseClassCardPanel.groupCoursesByCode(courses);
                    
                    updateTableData();
                    System.out.println("成功加载 " + courses.size() + " 门课程");
                }
            });
        });
        
        // 设置课程更新成功响应监听器
        serverConnection.setMessageListener(MessageType.UPDATE_COURSE_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof CourseVO) {
                    CourseVO updatedCourse = (CourseVO) message.getData();
                    System.out.println("课程更新成功: " + updatedCourse.getCourseName());
                    
                    // 更新本地课程列表
                    for (int i = 0; i < courseList.size(); i++) {
                        if (courseList.get(i).getCourseId().equals(updatedCourse.getCourseId())) {
                            courseList.set(i, updatedCourse);
                            break;
                        }
                    }
                    
                    // 更新教学班卡片面板的数据
                    courseClassCardPanel.groupCoursesByCode(courseList);
                    
                    // 刷新表格显示
                    updateTableData();
                    
                    // 刷新卡片显示
                    courseClassCardPanel.refreshCards();
                    
                    // 如果当前选中的课程被更新，刷新教学班显示
                    if (selectedCourse != null && selectedCourse.getCourseId().equals(updatedCourse.getCourseId())) {
                        selectedCourse = updatedCourse;
                        showCourseClasses(selectedCourse.getCourseCode());
                    }
                }
            });
        });
        
        // 设置课程删除成功响应监听器
        serverConnection.setMessageListener(MessageType.DELETE_COURSE_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof Integer) {
                    Integer deletedCourseId = (Integer) message.getData();
                    System.out.println("课程删除成功，ID: " + deletedCourseId);
                    
                    // 从本地课程列表中移除删除的课程
                    courseList.removeIf(course -> course.getCourseId().equals(deletedCourseId));
                    
                    // 从教学班卡片面板中移除对应的卡片
                    courseClassCardPanel.removeCourseCard(deletedCourseId);
                    
                    // 更新教学班卡片面板的数据
                    courseClassCardPanel.groupCoursesByCode(courseList);
                    
                    // 刷新表格显示
                    updateTableData();
                    
                    // 如果当前选中的课程被删除，隐藏教学班显示
                    if (selectedCourse != null && selectedCourse.getCourseId().equals(deletedCourseId)) {
                        selectedCourse = null;
                        hideCourseClasses();
                    }
                }
            });
        });
        
        // 设置选课成功响应监听器
        serverConnection.setMessageListener(MessageType.ENROLL_COURSE_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof Integer) {
                    Integer courseId = (Integer) message.getData();
                    System.out.println("选课成功，课程ID: " + courseId);
                    
                    // 更新对应卡片的选课状态
                    for (CourseClassCard card : courseClassCardPanel.getClassCards()) {
                        if (card.getCourse().getCourseId().equals(courseId)) {
                            card.updateEnrollmentStatus(true);
                            break;
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this, "选课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
        
        // 设置选课失败响应监听器
        serverConnection.setMessageListener(MessageType.ENROLL_COURSE_FAIL, message -> {
            SwingUtilities.invokeLater(() -> {
                String errorMessage = message.getData() != null ? message.getData().toString() : "选课失败";
                System.out.println("选课失败: " + errorMessage);
                JOptionPane.showMessageDialog(this, "选课失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
            });
        });
        
        // 设置退选成功响应监听器
        serverConnection.setMessageListener(MessageType.DROP_COURSE_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof Integer) {
                    Integer courseId = (Integer) message.getData();
                    System.out.println("退选成功，课程ID: " + courseId);
                    
                    // 更新对应卡片的选课状态
                    for (CourseClassCard card : courseClassCardPanel.getClassCards()) {
                        if (card.getCourse().getCourseId().equals(courseId)) {
                            card.updateEnrollmentStatus(false);
                            break;
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this, "退选成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
        
        // 设置退选失败响应监听器
        serverConnection.setMessageListener(MessageType.DROP_COURSE_FAIL, message -> {
            SwingUtilities.invokeLater(() -> {
                String errorMessage = message.getData() != null ? message.getData().toString() : "退选失败";
                System.out.println("退选失败: " + errorMessage);
                JOptionPane.showMessageDialog(this, "退选失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
            });
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建主面板，使用垂直BoxLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.WHITE);
        mainPanel.setBorder(UITheme.createEmptyBorder(0, 0, 0, 0));
        
        // 添加表格面板
        mainPanel.add(scrollTablePane);
        
        // 添加教学班卡片面板，设置固定高度
        courseClassCardPanel.setPreferredSize(new Dimension(0, 360)); // 增加高度以适应新的卡片高度
        courseClassCardPanel.setMinimumSize(new Dimension(0, 360));
        courseClassCardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        mainPanel.add(courseClassCardPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private void initComponents() {
        // 更新列定义以匹配数据库字段
        String[] columnNames = {"课程代码", "课程名称", "学分", "开课院系", "学期", "状态"};
        
        // 创建不可编辑的表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        courseTable = new JTable(tableModel);
        
        // 设置列宽
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 课程代码
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(250); // 课程名称
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 学分
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(150); // 开课院系
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 学期
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 状态
        
        // 设置表格样式
        courseTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setGridColor(UITheme.LIGHT_GRAY);
        courseTable.setShowGrid(true);
        courseTable.setBackground(UITheme.WHITE);
        courseTable.setFont(UITheme.CONTENT_FONT);
        courseTable.setSelectionBackground(UITheme.VERY_LIGHT_GREEN);
        courseTable.setSelectionForeground(UITheme.DARK_GRAY);
        
        // 设置表头样式
        JTableHeader header = courseTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        
        // 自定义表格渲染器
        courseTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // 设置字体和颜色
                c.setFont(UITheme.CONTENT_FONT);
                
                if (isSelected) {
                    c.setBackground(UITheme.VERY_LIGHT_GREEN);
                    c.setForeground(UITheme.DARK_GRAY);
                } else {
                    c.setBackground(row % 2 == 0 ? UITheme.WHITE : new Color(248, 250, 252));
                    c.setForeground(UITheme.DARK_GRAY);
                }
                
                // 设置边框
                setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
                
                return c;
            }
        });
        
        // 应用滚动样式
        scrollTablePane = new JScrollPane(courseTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setBorder(UITheme.createCardBorder());
        scrollTablePane.setBackground(UITheme.WHITE);
        
        // 创建教学班卡片面板
        courseClassCardPanel = new CourseClassCardPanel(currentUser);
    }
    
    /**
     * 加载课程数据
     */
    private void loadCourseData() {
        try {
            // 确保连接到服务器
            if (!serverConnection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!serverConnection.connect()) {
                    System.err.println("无法连接到服务器");
                    courseList.clear();
                    updateTableData();
                    return;
                }
            }
            
            // 创建获取课程列表的消息
            Message request = new Message();
            request.setType(MessageType.GET_ALL_COURSES_REQUEST);
            request.setData(null);
            
            // 发送消息到服务器
            if (serverConnection.sendMessage(request)) {
                System.out.println("已发送获取课程列表请求");
                // 等待服务器响应，这里暂时清空表格
                courseList.clear();
                updateTableData();
                System.out.println("等待服务器响应课程数据...");
            } else {
                System.err.println("发送获取课程数据请求失败");
                courseList.clear();
                updateTableData();
            }
        } catch (Exception e) {
            System.err.println("加载课程数据时发生错误: " + e.getMessage());
            courseList.clear();
            updateTableData();
        }
    }
    
    
    /**
     * 更新表格数据
     */
    private void updateTableData() {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 添加课程数据到表格
        for (CourseVO course : courseList) {
            Object[] rowData = {
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredits(),
                course.getDepartment(),
                course.getSemester(),
                course.getStatusName()
            };
            tableModel.addRow(rowData);
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 刷新课程数据
     */
    public void refreshData() {
        loadCourseData();
    }
    
    /**
     * 获取当前选中的课程
     * @return 选中的课程，如果没有选中则返回null
     */
    public CourseVO getSelectedCourse() {
        return selectedCourse;
    }
    
    /**
     * 根据课程代码搜索课程
     * @param courseCode 课程代码
     */
    public void searchByCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            updateTableData();
            return;
        }
        
        List<CourseVO> filteredCourses = new ArrayList<>();
        for (CourseVO course : courseList) {
            if (course.getCourseCode().toLowerCase().contains(courseCode.toLowerCase())) {
                filteredCourses.add(course);
            }
        }
        
        // 临时替换课程列表
        List<CourseVO> originalList = new ArrayList<>(courseList);
        courseList = filteredCourses;
        updateTableData();
        courseList = originalList;
    }
    
    /**
     * 根据课程名称搜索课程
     * @param courseName 课程名称
     */
    public void searchByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            updateTableData();
            return;
        }
        
        List<CourseVO> filteredCourses = new ArrayList<>();
        for (CourseVO course : courseList) {
            if (course.getCourseName().toLowerCase().contains(courseName.toLowerCase())) {
                filteredCourses.add(course);
            }
        }
        
        // 临时替换课程列表
        List<CourseVO> originalList = new ArrayList<>(courseList);
        courseList = filteredCourses;
        updateTableData();
        courseList = originalList;
    }
    
    /**
     * 获取课程表格组件
     * @return JTable对象
     */
    public JTable getCourseTable() {
        return courseTable;
    }
    
    /**
     * 获取滚动面板组件
     * @return JScrollPane对象
     */
    public JScrollPane getScrollPane() {
        return scrollTablePane;
    }
    
    /**
     * 显示指定课程的教学班信息
     * @param courseCode 课程代码
     */
    private void showCourseClasses(String courseCode) {
        if (courseCode != null && courseClassCardPanel.hasCourseClasses(courseCode)) {
            List<CourseVO> classes = courseClassCardPanel.getCourseClasses(courseCode);
            courseClassCardPanel.showCourseClasses(courseCode, classes);
        } else {
            hideCourseClasses();
        }
    }
    
    /**
     * 隐藏教学班信息
     */
    private void hideCourseClasses() {
        courseClassCardPanel.hideCourseClasses();
    }
}