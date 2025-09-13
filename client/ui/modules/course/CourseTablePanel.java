package client.ui.modules.course;

import common.vo.CourseVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    public CourseTablePanel() {
        this.serverConnection = ServerConnection.getInstance();
        this.courseList = new ArrayList<>();
        initComponents();      // 初始化组件
        setupLayout();         // 设置布局
        setupEventHandlers();  // 设置事件处理器
        setupMessageListener(); // 设置消息监听器
        loadCourseData();      // 加载课程数据
    }

    private void setupEventHandlers() {
        // 添加表格选择事件监听器
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < courseList.size()) {
                    selectedCourse = courseList.get(selectedRow);
                    System.out.println("选中课程: " + selectedCourse.getCourseName());
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
                    updateTableData();
                    System.out.println("成功加载 " + courses.size() + " 门课程");
                }
            });
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(scrollTablePane, BorderLayout.CENTER);
    }

    private void initComponents() {
        // 更新列定义以匹配数据库字段
        String[] columnNames = {"课程代码", "课程名称", "学分", "开课院系", "任课教师", "学期", "上课时间", "上课地点", "容量", "已选人数", "状态"};
        
        // 创建不可编辑的表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        courseTable = new JTable(tableModel);
        
        // 设置列宽
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 课程代码
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 课程名称
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // 学分
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 开课院系
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 任课教师
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 学期
        courseTable.getColumnModel().getColumn(6).setPreferredWidth(150); // 上课时间
        courseTable.getColumnModel().getColumn(7).setPreferredWidth(120); // 上课地点
        courseTable.getColumnModel().getColumn(8).setPreferredWidth(60);  // 容量
        courseTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // 已选人数
        courseTable.getColumnModel().getColumn(10).setPreferredWidth(80); // 状态
        
        // 设置表格样式
        courseTable.setRowHeight(25);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setGridColor(Color.LIGHT_GRAY);
        courseTable.setShowGrid(true);
        
        // 设置表头样式
        courseTable.getTableHeader().setReorderingAllowed(false);
        courseTable.getTableHeader().setResizingAllowed(true);
        
        // 应用滚动样式
        scrollTablePane = new JScrollPane(courseTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
                course.getTeacherName(),
                course.getSemester(),
                course.getClassTime(),
                course.getLocation(),
                course.getCapacity(),
                course.getEnrolledCount(),
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
}