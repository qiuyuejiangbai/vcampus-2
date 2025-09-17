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
 * 成绩管理面板
 * 用于显示选择某课程的学生成绩管理
 */
public class GradeManagementPanel extends JPanel {
    private JTable gradeTable;
    private JScrollPane scrollTablePane;
    private DefaultTableModel tableModel;
    private List<EnrollmentVO> enrollmentList;
    private ServerConnection connection;
    private JLabel titleLabel;
    private JLabel emptyLabel;
    private JButton backButton;
    private JButton refreshButton;
    private JButton saveGradesButton;
    private String currentCourseCode;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private Object parentModule; // 保持对父模块的引用

    public GradeManagementPanel(UserVO currentUser, ServerConnection connection) {
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
        titleLabel = new JLabel("成绩管理", JLabel.CENTER);
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
        
        // 创建保存成绩按钮
        saveGradesButton = new JButton("保存成绩");
        UITheme.styleButton(saveGradesButton);
        saveGradesButton.setPreferredSize(new Dimension(100, UITheme.BUTTON_HEIGHT));
        saveGradesButton.setBackground(UITheme.SUCCESS_GREEN);
        saveGradesButton.setForeground(UITheme.WHITE);
        
        // 创建表格
        String[] columnNames = {"学号", "姓名", "专业", "班级", "平时成绩", "期中成绩", "期末成绩", "总成绩", "等级"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有成绩相关列可以编辑
                return column >= 4 && column <= 7; // 平时成绩、期中成绩、期末成绩、总成绩
            }
        };
        
        gradeTable = new JTable(tableModel);
        
        // 设置列宽
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 学号
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 姓名
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(120); // 专业
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 班级
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 平时成绩
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 期中成绩
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 期末成绩
        gradeTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 总成绩
        gradeTable.getColumnModel().getColumn(8).setPreferredWidth(60);  // 等级
        
        // 设置表格样式
        gradeTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        gradeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradeTable.setGridColor(UITheme.LIGHT_GRAY);
        gradeTable.setShowGrid(true);
        gradeTable.setBackground(UITheme.WHITE);
        gradeTable.setFont(UITheme.CONTENT_FONT);
        gradeTable.setSelectionBackground(UITheme.VERY_LIGHT_GREEN);
        gradeTable.setSelectionForeground(UITheme.DARK_GRAY);
        
        // 设置表头样式
        JTableHeader header = gradeTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        
        // 自定义表格渲染器
        gradeTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // 设置字体和颜色
                c.setFont(UITheme.CONTENT_FONT);
                
                if (isSelected) {
                    c.setBackground(UITheme.VERY_LIGHT_GREEN);
                    c.setForeground(UITheme.DARK_GRAY);
                } else {
                    // 成绩列使用不同的背景色
                    if (column >= 4 && column <= 7) {
                        c.setBackground(row % 2 == 0 ? new Color(250, 255, 250) : new Color(245, 255, 245));
                    } else {
                        c.setBackground(row % 2 == 0 ? UITheme.WHITE : new Color(248, 250, 252));
                    }
                    c.setForeground(UITheme.DARK_GRAY);
                }
                
                // 设置边框
                setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
                
                return c;
            }
        });
        
        // 应用滚动样式
        scrollTablePane = new JScrollPane(gradeTable);
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
        leftPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        leftPanel.add(saveGradesButton);
        
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
                loadGradeData(currentCourseCode);
            }
        });
        
        // 保存成绩按钮事件
        saveGradesButton.addActionListener(e -> {
            saveGrades();
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
                        System.out.println("成功加载 " + enrollments.size() + " 条成绩记录");
                    }
                });
            });
        }
    }

    /**
     * 加载成绩数据
     */
    public void loadGradeData(String courseCode) {
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
                System.out.println("已发送获取成绩记录请求，课程代码: " + courseCode);
                // 等待服务器响应，这里暂时清空表格
                enrollmentList.clear();
                updateTableData();
                System.out.println("等待服务器响应成绩记录数据...");
            } else {
                System.err.println("发送获取成绩记录请求失败");
                enrollmentList.clear();
                updateTableData();
            }
        } catch (Exception e) {
            System.err.println("加载成绩数据时发生错误: " + e.getMessage());
            enrollmentList.clear();
            updateTableData();
        }
    }

    /**
     * 设置课程信息
     */
    public void setCourseInfo(String courseCode, String courseName) {
        this.currentCourseCode = courseCode;
        titleLabel.setText(courseName + " - 成绩管理");
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
            
            // 安全获取学生学号
            if (enrollment.getStudentNo() != null) {
                studentNo = enrollment.getStudentNo();
            }
            
            // 安全获取学生姓名
            if (enrollment.getStudentName() != null) {
                studentName = enrollment.getStudentName();
            }
            
            // 从关联的StudentVO获取专业和班级信息
            if (enrollment.getStudent() != null) {
                major = enrollment.getStudent().getMajor() != null ? enrollment.getStudent().getMajor() : "未知";
                className = enrollment.getStudent().getClassName() != null ? enrollment.getStudent().getClassName() : "未知";
            } else {
                System.out.println("警告：选课记录 " + studentNo + " 缺少关联的学生信息");
            }
            
            // 获取成绩信息（这里使用默认值，实际应该从数据库获取）
            String regularGrade = ""; // 平时成绩
            String midtermGrade = ""; // 期中成绩
            String finalGrade = ""; // 期末成绩
            String totalGrade = ""; // 总成绩
            String gradeLevel = ""; // 等级
            
            Object[] rowData = {
                studentNo,
                studentName,
                major,
                className,
                regularGrade,
                midtermGrade,
                finalGrade,
                totalGrade,
                gradeLevel
            };
            tableModel.addRow(rowData);
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }

    /**
     * 保存成绩
     */
    private void saveGrades() {
        System.out.println("=== 保存成绩 ===");
        
        // 这里应该实现保存成绩的逻辑
        // 遍历表格数据，获取修改的成绩
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String studentNo = (String) tableModel.getValueAt(i, 0);
            String regularGrade = (String) tableModel.getValueAt(i, 4);
            String midtermGrade = (String) tableModel.getValueAt(i, 5);
            String finalGrade = (String) tableModel.getValueAt(i, 6);
            String totalGrade = (String) tableModel.getValueAt(i, 7);
            
            System.out.println("学生: " + studentNo + ", 平时: " + regularGrade + 
                             ", 期中: " + midtermGrade + ", 期末: " + finalGrade + ", 总分: " + totalGrade);
        }
        
        JOptionPane.showMessageDialog(this, "成绩保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        if (currentCourseCode != null) {
            loadGradeData(currentCourseCode);
        }
    }

    /**
     * 获取成绩表格组件
     */
    public JTable getGradeTable() {
        return gradeTable;
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
        System.out.println("GradeManagementPanel 设置父模块引用: " + (parentModule != null ? parentModule.getClass().getSimpleName() : "null"));
    }
}
