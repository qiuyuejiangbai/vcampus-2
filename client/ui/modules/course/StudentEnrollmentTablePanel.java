package client.ui.modules.course;

import common.vo.EnrollmentVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/**
 * 学生选课记录表格面板
 * 用于显示当前学生的选课记录信息
 */
public class StudentEnrollmentTablePanel extends JPanel {
    private JTable enrollmentTable;  // 选课记录表格
    private JScrollPane scrollTablePane;  // 表格滚动面板
    private DefaultTableModel tableModel; // 表格模型
    private List<EnrollmentVO> enrollmentList;   // 选课记录数据列表
    private final ServerConnection serverConnection; // 服务器连接

    public StudentEnrollmentTablePanel() {
        this.serverConnection = ServerConnection.getInstance();
        this.enrollmentList = new ArrayList<>();
        initComponents();      // 初始化组件
        setupLayout();         // 设置布局
        setupEventHandlers();  // 设置事件处理器
        setupMessageListener(); // 设置消息监听器
        loadEnrollmentData();  // 加载选课记录数据
    }

    private void setupEventHandlers() {
        // 选课记录表格只读，不需要选择事件处理
    }

    /**
     * 设置消息监听器
     */
    private void setupMessageListener() {
        // 设置学生选课记录列表响应监听器
        serverConnection.setMessageListener(MessageType.GET_STUDENT_ENROLLMENTS_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<EnrollmentVO> enrollments = (List<EnrollmentVO>) message.getData();
                    enrollmentList.clear();
                    enrollmentList.addAll(enrollments);
                    updateTableData();
                    System.out.println("成功加载 " + enrollments.size() + " 条学生选课记录");
                }
            });
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 添加表格面板
        add(scrollTablePane, BorderLayout.CENTER);
    }

    private void initComponents() {
        // 定义表格列名，学生版本不显示学号和学生姓名
        String[] columnNames = {
            "课程代码", "课程名称", "学分", "教师姓名", 
            "学期", "学年", "选课时间", "状态"
        };
        
        // 创建不可编辑的表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        enrollmentTable = new JTable(tableModel);
        
        // 设置列宽
        enrollmentTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 课程代码
        enrollmentTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 课程名称
        enrollmentTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // 学分
        enrollmentTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 教师姓名
        enrollmentTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 学期
        enrollmentTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 学年
        enrollmentTable.getColumnModel().getColumn(6).setPreferredWidth(150); // 选课时间
        enrollmentTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 状态
        
        // 设置表格样式
        enrollmentTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.setGridColor(UITheme.LIGHT_GRAY);
        enrollmentTable.setShowGrid(true);
        enrollmentTable.setBackground(UITheme.WHITE);
        enrollmentTable.setFont(UITheme.CONTENT_FONT);
        enrollmentTable.setSelectionBackground(UITheme.VERY_LIGHT_GREEN);
        enrollmentTable.setSelectionForeground(UITheme.DARK_GRAY);
        
        // 设置表头样式
        JTableHeader header = enrollmentTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        
        // 自定义表格渲染器
        enrollmentTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        scrollTablePane = new JScrollPane(enrollmentTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setBorder(UITheme.createCardBorder());
        scrollTablePane.setBackground(UITheme.WHITE);
    }
    
    /**
     * 加载选课记录数据
     */
    private void loadEnrollmentData() {
        try {
            // 确保连接到服务器
            if (!serverConnection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!serverConnection.connect()) {
                    System.err.println("无法连接到服务器");
                    enrollmentList.clear();
                    updateTableData();
                    return;
                }
            }
            
            // 创建获取学生选课记录列表的消息
            Message request = new Message();
            request.setType(MessageType.GET_STUDENT_ENROLLMENTS_REQUEST);
            request.setData(null); // 服务器会根据当前登录用户自动获取学生ID
            
            // 发送消息到服务器
            if (serverConnection.sendMessage(request)) {
                System.out.println("已发送获取学生选课记录列表请求");
                // 等待服务器响应，这里暂时清空表格
                enrollmentList.clear();
                updateTableData();
                System.out.println("等待服务器响应学生选课记录数据...");
            } else {
                System.err.println("发送获取学生选课记录数据请求失败");
                enrollmentList.clear();
                updateTableData();
            }
        } catch (Exception e) {
            System.err.println("加载学生选课记录数据时发生错误: " + e.getMessage());
            enrollmentList.clear();
            updateTableData();
        }
    }
    
    /**
     * 更新表格数据
     */
    private void updateTableData() {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 添加选课记录数据到表格
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (EnrollmentVO enrollment : enrollmentList) {
            Object[] rowData = {
                enrollment.getCourseCode(),
                enrollment.getCourseName(),
                enrollment.getCredits(),
                enrollment.getTeacherName(),
                enrollment.getSemester(),
                enrollment.getAcademicYear(),
                enrollment.getEnrollmentTime() != null ? 
                    dateFormat.format(enrollment.getEnrollmentTime()) : "",
                enrollment.getStatusName()
            };
            tableModel.addRow(rowData);
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 刷新选课记录数据
     */
    public void refreshData() {
        loadEnrollmentData();
    }
    
    /**
     * 根据课程名称搜索选课记录
     * @param courseName 课程名称关键词
     */
    public void searchByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            updateTableData();
            return;
        }
        
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 过滤选课记录数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String searchKeyword = courseName.toLowerCase().trim();
        
        for (EnrollmentVO enrollment : enrollmentList) {
            if (enrollment.getCourseName() != null && 
                enrollment.getCourseName().toLowerCase().contains(searchKeyword)) {
                Object[] rowData = {
                    enrollment.getCourseCode(),
                    enrollment.getCourseName(),
                    enrollment.getCredits(),
                    enrollment.getTeacherName(),
                    enrollment.getSemester(),
                    enrollment.getAcademicYear(),
                    enrollment.getEnrollmentTime() != null ? 
                        dateFormat.format(enrollment.getEnrollmentTime()) : "",
                    enrollment.getStatusName()
                };
                tableModel.addRow(rowData);
            }
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 获取选课记录表格组件
     * @return JTable对象
     */
    public JTable getEnrollmentTable() {
        return enrollmentTable;
    }
    
    /**
     * 获取滚动面板组件
     * @return JScrollPane对象
     */
    public JScrollPane getScrollPane() {
        return scrollTablePane;
    }
}
