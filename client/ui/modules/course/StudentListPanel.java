package client.ui.modules.course;

import common.vo.EnrollmentVO;
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

/**
 * 学生名单面板
 * 用于显示选择某课程的学生名单
 */
public class StudentListPanel extends JPanel {
    private JTable studentTable;
    private JScrollPane scrollTablePane;
    private DefaultTableModel tableModel;
    private List<EnrollmentVO> enrollmentList;
    private ServerConnection connection;
    private JLabel titleLabel;
    private JLabel emptyLabel;
    private JButton backButton;
    private JButton refreshButton;
    private String currentCourseCode;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private Object parentModule; // 保持对父模块的引用

    public StudentListPanel(UserVO currentUser, ServerConnection connection) {
        this.connection = connection;
        this.enrollmentList = new ArrayList<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建标题标签
        titleLabel = new JLabel("学生名单", JLabel.CENTER);
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_GREEN);
        titleLabel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, 0, UITheme.PADDING_MEDIUM, 0));
        
        // 创建返回按钮
        backButton = new JButton("返回课程列表");
        UITheme.styleButton(backButton);
        backButton.setPreferredSize(new Dimension(120, UITheme.BUTTON_HEIGHT));
        backButton.setBackground(UITheme.MEDIUM_GRAY);
        backButton.setForeground(UITheme.WHITE);
        
        // 创建刷新按钮
        refreshButton = new JButton("刷新");
        UITheme.styleButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
        
        // 创建表格
        String[] columnNames = {"学号", "姓名", "专业", "班级", "选课时间", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        studentTable = new JTable(tableModel);
        
        // 设置列宽
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 学号
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 姓名
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(150); // 专业
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 班级
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(150); // 选课时间
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 状态
        
        // 设置表格样式
        studentTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setGridColor(UITheme.LIGHT_GRAY);
        studentTable.setShowGrid(true);
        studentTable.setBackground(UITheme.WHITE);
        studentTable.setFont(UITheme.CONTENT_FONT);
        studentTable.setSelectionBackground(UITheme.VERY_LIGHT_GREEN);
        studentTable.setSelectionForeground(UITheme.DARK_GRAY);
        
        // 设置表头样式
        JTableHeader header = studentTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        
        // 自定义表格渲染器
        studentTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        scrollTablePane = new JScrollPane(studentTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setBorder(UITheme.createCardBorder());
        scrollTablePane.setBackground(UITheme.WHITE);
        
        // 创建空状态标签
        emptyLabel = new JLabel("暂无学生数据", JLabel.CENTER);
        emptyLabel.setFont(UITheme.DEFAULT_FONT);
        emptyLabel.setForeground(UITheme.LIGHT_GRAY);
        emptyLabel.setVisible(false);
    }

    private void setupLayout() {
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.WHITE);
        topPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM, UITheme.PADDING_LARGE));
        
        // 左侧按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(UITheme.WHITE);
        leftPanel.add(backButton);
        leftPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        leftPanel.add(refreshButton);
        
        // 右侧标题
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(UITheme.WHITE);
        rightPanel.add(titleLabel);
        
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 创建内容面板，使用 CardLayout 来管理表格和空状态标签的显示
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(UITheme.WHITE);
        contentPanel.add(scrollTablePane, "table");
        contentPanel.add(emptyLabel, "empty");
        
        // 初始显示空状态
        contentCardLayout.show(contentPanel, "empty");
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // 返回按钮事件
        backButton.addActionListener(e -> {
            System.out.println("=== 返回课程列表按钮被点击 ===");
            
            // 使用直接引用调用父模块方法
            if (parentModule != null) {
                System.out.println("使用直接引用调用父模块: " + parentModule.getClass().getSimpleName());
                try {
                    java.lang.reflect.Method method = parentModule.getClass().getMethod("showCoursePanel");
                    method.invoke(parentModule);
                    System.out.println("成功通过直接引用调用 showCoursePanel 方法");
                    return;
                } catch (Exception ex) {
                    System.err.println("通过直接引用调用showCoursePanel方法失败: " + ex.getMessage());
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
                        System.out.println("找到 TeacherCourseModule，准备调用 showCoursePanel");
                        try {
                            java.lang.reflect.Method method = parent.getClass().getMethod("showCoursePanel");
                            method.invoke(parent);
                            System.out.println("成功调用 showCoursePanel 方法");
                        } catch (Exception ex) {
                            System.err.println("无法调用showCoursePanel方法: " + ex.getMessage());
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
        });
        
        // 刷新按钮事件
        refreshButton.addActionListener(e -> {
            if (currentCourseCode != null) {
                loadStudentData(currentCourseCode);
            }
        });
    }

    /**
     * 设置消息监听器
     */
    private void setupMessageListener() {
        if (connection != null) {
            // 设置选课记录响应监听器
            connection.setMessageListener(MessageType.GET_ENROLLMENTS_BY_COURSE_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    if (message.getData() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<EnrollmentVO> enrollments = (List<EnrollmentVO>) message.getData();
                        enrollmentList.clear();
                        enrollmentList.addAll(enrollments);
                        
                        updateTableData();
                        System.out.println("成功加载 " + enrollments.size() + " 条选课记录");
                    }
                });
            });
        }
    }

    /**
     * 加载学生数据
     */
    public void loadStudentData(String courseCode) {
        this.currentCourseCode = courseCode;
        
        try {
            // 确保连接到服务器
            if (connection != null && !connection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!connection.connect()) {
                    System.err.println("无法连接到服务器");
                    enrollmentList.clear();
                    updateTableData();
                    return;
                }
            }
            
            // 创建获取选课记录的消息
            Message request = new Message();
            request.setType(MessageType.GET_ENROLLMENTS_BY_COURSE_REQUEST);
            request.setData(courseCode);
            
            // 发送消息到服务器
            if (connection != null && connection.sendMessage(request)) {
                System.out.println("已发送获取选课记录请求，课程代码: " + courseCode);
                // 等待服务器响应，这里暂时清空表格
                enrollmentList.clear();
                updateTableData();
                System.out.println("等待服务器响应选课记录数据...");
            } else {
                System.err.println("发送获取选课记录请求失败");
                enrollmentList.clear();
                updateTableData();
            }
        } catch (Exception e) {
            System.err.println("加载学生数据时发生错误: " + e.getMessage());
            enrollmentList.clear();
            updateTableData();
        }
    }

    /**
     * 设置课程信息
     */
    public void setCourseInfo(String courseCode, String courseName) {
        this.currentCourseCode = courseCode;
        titleLabel.setText(courseName + " - 学生名单");
    }

    /**
     * 更新表格数据
     */
    private void updateTableData() {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        if (enrollmentList.isEmpty()) {
            // 显示空状态标签
            contentCardLayout.show(contentPanel, "empty");
            return;
        }
        
        // 显示表格
        contentCardLayout.show(contentPanel, "table");
        
        // 添加选课记录数据到表格
        for (EnrollmentVO enrollment : enrollmentList) {
            // 跳过空的选课记录
            if (enrollment == null) {
                System.err.println("警告：发现空的选课记录，已跳过");
                continue;
            }
            
            String major = "未知";
            String className = "未知";
            String studentNo = "未知";
            String studentName = "未知";
            String statusName = "未知";
            
            // 安全获取学生学号
            if (enrollment.getStudentNo() != null) {
                studentNo = enrollment.getStudentNo();
            }
            
            // 安全获取学生姓名
            if (enrollment.getStudentName() != null) {
                studentName = enrollment.getStudentName();
            }
            
            // 安全获取状态名称
            if (enrollment.getStatusName() != null) {
                statusName = enrollment.getStatusName();
            }
            
            // 从关联的StudentVO获取专业和班级信息
            if (enrollment.getStudent() != null) {
                major = enrollment.getStudent().getMajor() != null ? enrollment.getStudent().getMajor() : "未知";
                className = enrollment.getStudent().getClassName() != null ? enrollment.getStudent().getClassName() : "未知";
            } else {
                System.out.println("警告：选课记录 " + studentNo + " 缺少关联的学生信息");
            }
            
            Object[] rowData = {
                studentNo,
                studentName,
                major,
                className,
                enrollment.getEnrollmentTime() != null ? enrollment.getEnrollmentTime().toString() : "未知",
                statusName
            };
            tableModel.addRow(rowData);
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        if (currentCourseCode != null) {
            loadStudentData(currentCourseCode);
        }
    }

    /**
     * 获取学生表格组件
     */
    public JTable getStudentTable() {
        return studentTable;
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
        System.out.println("StudentListPanel 设置父模块引用: " + (parentModule != null ? parentModule.getClass().getSimpleName() : "null"));
    }
}
