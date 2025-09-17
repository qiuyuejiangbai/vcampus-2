package client.ui.modules.course;

import common.vo.GradeVO;
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
import java.math.BigDecimal;

/**
 * 成绩管理表格面板
 * 用于显示所有成绩信息
 */
public class GradeTablePanel extends JPanel {
    private JTable gradeTable;  // 成绩表格
    private JScrollPane scrollTablePane;  // 表格滚动面板
    private DefaultTableModel tableModel; // 表格模型
    private List<GradeVO> gradeList;   // 成绩数据列表
    private final ServerConnection serverConnection; // 服务器连接

    public GradeTablePanel() {
        this.serverConnection = ServerConnection.getInstance();
        this.gradeList = new ArrayList<>();
        initComponents();      // 初始化组件
        setupLayout();         // 设置布局
        setupEventHandlers();  // 设置事件处理器
        setupMessageListener(); // 设置消息监听器
        loadGradeData();       // 加载成绩数据
    }

    private void setupEventHandlers() {
        // 成绩表格只读，不需要选择事件处理
    }

    /**
     * 设置消息监听器
     */
    private void setupMessageListener() {
        // 设置成绩列表响应监听器
        serverConnection.setMessageListener(MessageType.GET_ALL_GRADES_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<GradeVO> grades = (List<GradeVO>) message.getData();
                    gradeList.clear();
                    gradeList.addAll(grades);
                    updateTableData();
                    System.out.println("成功加载 " + grades.size() + " 条成绩记录");
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
        // 定义表格列名，与数据库字段对应
        String[] columnNames = {
            "学号", "学生姓名", "课程代码", "课程名称", 
            "学分", "教师姓名", "学期", "期中成绩", 
            "期末成绩", "作业成绩", "总成绩", "绩点", 
            "等级", "评分时间", "状态"
        };
        
        // 创建不可编辑的表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };
        
        gradeTable = new JTable(tableModel);
        
        // 设置列宽 - 增加宽度以显示完整表头文字
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(120);  // 学号
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 学生姓名
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // 课程代码
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(180);  // 课程名称
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // 学分
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // 教师姓名
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // 学期
        gradeTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // 期中成绩
        gradeTable.getColumnModel().getColumn(8).setPreferredWidth(100);  // 期末成绩
        gradeTable.getColumnModel().getColumn(9).setPreferredWidth(100);  // 作业成绩
        gradeTable.getColumnModel().getColumn(10).setPreferredWidth(100); // 总成绩
        gradeTable.getColumnModel().getColumn(11).setPreferredWidth(80);  // 绩点
        gradeTable.getColumnModel().getColumn(12).setPreferredWidth(80);  // 等级
        gradeTable.getColumnModel().getColumn(13).setPreferredWidth(180); // 评分时间
        gradeTable.getColumnModel().getColumn(14).setPreferredWidth(100); // 状态
        
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
                    c.setBackground(row % 2 == 0 ? UITheme.WHITE : new Color(248, 250, 252));
                    c.setForeground(UITheme.DARK_GRAY);
                }
                
                // 根据成绩等级设置颜色
                if (column == 12 && value != null) { // 等级列
                    String gradeLevel = value.toString();
                    if (gradeLevel.equals("A+") || gradeLevel.equals("A")) {
                        c.setForeground(UITheme.SUCCESS_GREEN);
                    } else if (gradeLevel.equals("B+") || gradeLevel.equals("B")) {
                        c.setForeground(UITheme.PRIMARY_GREEN);
                    } else if (gradeLevel.equals("C+") || gradeLevel.equals("C")) {
                        c.setForeground(UITheme.WARNING_YELLOW);
                    } else if (gradeLevel.equals("D+") || gradeLevel.equals("D") || gradeLevel.equals("F")) {
                        c.setForeground(UITheme.ERROR_RED);
                    }
                }
                
                // 设置边框
                setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
                
                return c;
            }
        });
        
        // 应用滚动样式 - 启用横向滚动
        scrollTablePane = new JScrollPane(gradeTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollTablePane.setBorder(UITheme.createCardBorder());
        scrollTablePane.setBackground(UITheme.WHITE);
        
        // 设置表格自动调整模式，确保列宽不会自动调整
        gradeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    /**
     * 加载成绩数据
     */
    private void loadGradeData() {
        try {
            // 确保连接到服务器
            if (!serverConnection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!serverConnection.connect()) {
                    System.err.println("无法连接到服务器");
                    gradeList.clear();
                    updateTableData();
                    return;
                }
            }
            
            // 创建获取成绩列表的消息
            Message request = new Message();
            request.setType(MessageType.GET_ALL_GRADES_REQUEST);
            request.setData(null);
            
            // 发送消息到服务器
            if (serverConnection.sendMessage(request)) {
                System.out.println("已发送获取成绩列表请求");
                // 等待服务器响应，这里暂时清空表格
                gradeList.clear();
                updateTableData();
                System.out.println("等待服务器响应成绩数据...");
            } else {
                System.err.println("发送获取成绩数据请求失败");
                gradeList.clear();
                updateTableData();
            }
        } catch (Exception e) {
            System.err.println("加载成绩数据时发生错误: " + e.getMessage());
            gradeList.clear();
            updateTableData();
        }
    }
    
    /**
     * 更新表格数据
     */
    private void updateTableData() {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 添加成绩数据到表格
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (GradeVO grade : gradeList) {
            Object[] rowData = {
                grade.getStudentNo(),
                grade.getStudentName(),
                grade.getCourseCode(),
                grade.getCourseName(),
                grade.getCredits(),
                grade.getTeacherName(),
                grade.getSemester(),
                formatGrade(grade.getMidtermGrade()),
                formatGrade(grade.getFinalGrade()),
                formatGrade(grade.getAssignmentGrade()),
                formatGrade(grade.getTotalGrade()),
                formatGradePoint(grade.getGradePoint()),
                grade.getGradeLevel() != null ? grade.getGradeLevel() : "",
                grade.getGradedTime() != null ? 
                    dateFormat.format(grade.getGradedTime()) : "",
                grade.getGradeStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 格式化成绩显示
     */
    private String formatGrade(BigDecimal grade) {
        if (grade == null) return "";
        return grade.toString();
    }
    
    /**
     * 格式化绩点显示
     */
    private String formatGradePoint(BigDecimal gradePoint) {
        if (gradePoint == null) return "";
        return gradePoint.toString();
    }
    
    /**
     * 刷新成绩数据
     */
    public void refreshData() {
        loadGradeData();
    }
    
    /**
     * 根据学生姓名搜索成绩
     */
    public void searchByStudentName(String studentName) {
        if (studentName == null || studentName.trim().isEmpty()) {
            updateTableData();
            return;
        }
        
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 过滤数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (GradeVO grade : gradeList) {
            if (grade.getStudentName() != null && 
                grade.getStudentName().toLowerCase().contains(studentName.toLowerCase())) {
                Object[] rowData = {
                    grade.getStudentNo(),
                    grade.getStudentName(),
                    grade.getCourseCode(),
                    grade.getCourseName(),
                    grade.getCredits(),
                    grade.getTeacherName(),
                    grade.getSemester(),
                    formatGrade(grade.getMidtermGrade()),
                    formatGrade(grade.getFinalGrade()),
                    formatGrade(grade.getAssignmentGrade()),
                    formatGrade(grade.getTotalGrade()),
                    formatGradePoint(grade.getGradePoint()),
                    grade.getGradeLevel() != null ? grade.getGradeLevel() : "",
                    grade.getGradedTime() != null ? 
                        dateFormat.format(grade.getGradedTime()) : "",
                    grade.getGradeStatus()
                };
                tableModel.addRow(rowData);
            }
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 根据课程名称搜索成绩
     */
    public void searchByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            updateTableData();
            return;
        }
        
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 过滤数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (GradeVO grade : gradeList) {
            if (grade.getCourseName() != null && 
                grade.getCourseName().toLowerCase().contains(courseName.toLowerCase())) {
                Object[] rowData = {
                    grade.getStudentNo(),
                    grade.getStudentName(),
                    grade.getCourseCode(),
                    grade.getCourseName(),
                    grade.getCredits(),
                    grade.getTeacherName(),
                    grade.getSemester(),
                    formatGrade(grade.getMidtermGrade()),
                    formatGrade(grade.getFinalGrade()),
                    formatGrade(grade.getAssignmentGrade()),
                    formatGrade(grade.getTotalGrade()),
                    formatGradePoint(grade.getGradePoint()),
                    grade.getGradeLevel() != null ? grade.getGradeLevel() : "",
                    grade.getGradedTime() != null ? 
                        dateFormat.format(grade.getGradedTime()) : "",
                    grade.getGradeStatus()
                };
                tableModel.addRow(rowData);
            }
        }
        
        // 刷新表格
        tableModel.fireTableDataChanged();
    }
    
    /**
     * 获取成绩表格组件
     * @return JTable对象
     */
    public JTable getGradeTable() {
        return gradeTable;
    }
    
    /**
     * 获取滚动面板组件
     * @return JScrollPane对象
     */
    public JScrollPane getScrollPane() {
        return scrollTablePane;
    }
}
