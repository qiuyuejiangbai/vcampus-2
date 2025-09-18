package client.ui.modules.course;

import common.vo.GradeVO;
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
import java.math.BigDecimal;

/**
 * 教师可编辑成绩管理表格面板
 * 用于显示和编辑指定课程的所有学生成绩
 */
public class TeacherEditableGradeTablePanel extends JPanel {
    private JTable gradeTable;
    private JScrollPane scrollTablePane;
    private DefaultTableModel tableModel;
    private List<GradeVO> gradeList;
    private CourseVO currentCourse;
    private ServerConnection connection;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel courseInfoLabel;
    private JLabel statusLabel;
    private Object parentModule;

    public TeacherEditableGradeTablePanel(CourseVO course, UserVO currentUser, ServerConnection connection, Object parentModule) {
        this.currentCourse = course;
        this.connection = connection;
        this.parentModule = parentModule;
        this.gradeList = new ArrayList<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
        loadGradeData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建课程信息标签
        courseInfoLabel = new JLabel();
        courseInfoLabel.setFont(UITheme.SUBTITLE_FONT);
        courseInfoLabel.setForeground(UITheme.PRIMARY_GREEN);
        courseInfoLabel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        
        // 定义表格列名
        String[] columnNames = {
            "学号", "学生姓名", "期中成绩", "期末成绩", 
            "作业成绩", "总成绩", "绩点", "等级"
        };
        
        // 创建可编辑的表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有成绩相关的列可编辑：期中成绩(2), 期末成绩(3), 作业成绩(4)
                return column >= 2 && column <= 4;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // 成绩列返回BigDecimal类型
                if (columnIndex >= 2 && columnIndex <= 4) {
                    return BigDecimal.class;
                }
                return String.class;
            }
        };
        
        gradeTable = new JTable(tableModel);
        
        // 设置列宽
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(120);  // 学号
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 学生姓名
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // 期中成绩
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // 期末成绩
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // 作业成绩
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // 总成绩
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // 绩点
        gradeTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // 等级
        
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
                
                c.setFont(UITheme.CONTENT_FONT);
                
                if (isSelected) {
                    c.setBackground(UITheme.VERY_LIGHT_GREEN);
                    c.setForeground(UITheme.DARK_GRAY);
                } else {
                    c.setBackground(row % 2 == 0 ? UITheme.WHITE : new Color(248, 250, 252));
                    c.setForeground(UITheme.DARK_GRAY);
                }
                
                // 根据成绩等级设置颜色
                if (column == 7 && value != null) { // 等级列
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
                
                // 可编辑列的背景色
                if (column >= 2 && column <= 4) {
                    c.setBackground(new Color(255, 255, 240)); // 浅黄色背景表示可编辑
                }
                
                setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
                
                return c;
            }
        });
        
        // 创建滚动面板 - 表格横向占满，不需要横向滚动
        scrollTablePane = new JScrollPane(gradeTable);
        scrollTablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTablePane.setBorder(UITheme.createCardBorder());
        scrollTablePane.setBackground(UITheme.WHITE);
        
        // 设置表格自动调整模式 - 横向占满
        gradeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // 创建按钮
        saveButton = new JButton("保存成绩");
        UITheme.styleButton(saveButton);
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.setBackground(UITheme.SUCCESS_GREEN);
        saveButton.setForeground(UITheme.WHITE);
        
        cancelButton = new JButton("返回");
        UITheme.styleButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setBackground(UITheme.MEDIUM_GRAY);
        cancelButton.setForeground(UITheme.WHITE);
        
        // 创建状态标签
        statusLabel = new JLabel("准备就绪");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.MEDIUM_GRAY);
    }

    private void setupLayout() {
        // 顶部面板：课程信息和状态
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.WHITE);
        topPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        
        topPanel.add(courseInfoLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);
        
        // 中间面板：表格
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UITheme.WHITE);
        tablePanel.setBorder(UITheme.createEmptyBorder(0, UITheme.PADDING_MEDIUM, 0, UITheme.PADDING_MEDIUM));
        tablePanel.add(scrollTablePane, BorderLayout.CENTER);
        
        // 底部面板：按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        buttonPanel.setBackground(UITheme.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // 添加所有面板
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 更新课程信息
        updateCourseInfo();
    }

    private void setupEventHandlers() {
        // 保存按钮事件
        saveButton.addActionListener(e -> saveGrades());
        
        // 取消按钮事件
        cancelButton.addActionListener(e -> {
            if (parentModule != null) {
                // 调用父模块的返回方法
                try {
                    java.lang.reflect.Method method = parentModule.getClass().getMethod("showCoursePanel");
                    method.invoke(parentModule);
                } catch (Exception ex) {
                    System.err.println("调用父模块返回方法失败: " + ex.getMessage());
                }
            }
        });
        
        // 表格编辑事件 - 当成绩被编辑时自动计算总成绩
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() >= 2 && e.getColumn() <= 4) { // 成绩列被编辑
                int row = e.getFirstRow();
                calculateTotalGrade(row);
            }
        });
    }

    private void setupMessageListener() {
        if (connection != null) {
            // 设置获取课程成绩响应监听器
            connection.setMessageListener(MessageType.GET_GRADES_BY_COURSE_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    if (message.getData() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<GradeVO> grades = (List<GradeVO>) message.getData();
                        gradeList.clear();
                        gradeList.addAll(grades);
                        updateTableData();
                        statusLabel.setText("已加载 " + grades.size() + " 条成绩记录");
                        System.out.println("成功加载课程成绩: " + grades.size() + " 条记录");
                    }
                });
            });
            
            // 设置更新成绩响应监听器
            connection.setMessageListener(MessageType.UPDATE_GRADE_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("成绩保存成功");
                    System.out.println("成绩更新成功");
                });
            });
            
            connection.setMessageListener(MessageType.UPDATE_GRADE_FAIL, message -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("成绩保存失败");
                    System.err.println("成绩更新失败");
                });
            });
        }
    }

    private void loadGradeData() {
        if (currentCourse == null || connection == null) {
            statusLabel.setText("无法加载成绩数据");
            return;
        }
        
        try {
            if (!connection.isConnected()) {
                if (!connection.connect()) {
                    statusLabel.setText("无法连接到服务器");
                    return;
                }
            }
            
            // 创建获取课程成绩的消息
            Message request = new Message();
            request.setType(MessageType.GET_GRADES_BY_COURSE_REQUEST);
            request.setData(currentCourse.getCourseId());
            
            if (connection.sendMessage(request)) {
                statusLabel.setText("正在加载成绩数据...");
                System.out.println("已发送获取课程成绩请求: " + currentCourse.getCourseName());
            } else {
                statusLabel.setText("发送请求失败");
            }
        } catch (Exception e) {
            statusLabel.setText("加载成绩数据时发生错误");
            System.err.println("加载成绩数据时发生错误: " + e.getMessage());
        }
    }

    private void updateTableData() {
        tableModel.setRowCount(0);
        
        for (GradeVO grade : gradeList) {
            Object[] rowData = {
                grade.getStudentNo(),
                grade.getStudentName(),
                grade.getMidtermGrade(),
                grade.getFinalGrade(),
                grade.getAssignmentGrade(),
                grade.getTotalGrade(),
                formatGradePoint(grade.getGradePoint()),
                grade.getGradeLevel() != null ? grade.getGradeLevel() : ""
            };
            tableModel.addRow(rowData);
        }
        
        tableModel.fireTableDataChanged();
    }

    private void calculateTotalGrade(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        
        try {
            // 获取各项成绩
            BigDecimal midterm = getBigDecimalValue(tableModel.getValueAt(row, 2));
            BigDecimal finalGrade = getBigDecimalValue(tableModel.getValueAt(row, 3));
            BigDecimal assignment = getBigDecimalValue(tableModel.getValueAt(row, 4));
            
            // 计算总成绩（均分）
            BigDecimal total = BigDecimal.ZERO;
            int count = 0;
            
            if (midterm != null) {
                total = total.add(midterm);
                count++;
            }
            if (finalGrade != null) {
                total = total.add(finalGrade);
                count++;
            }
            if (assignment != null) {
                total = total.add(assignment);
                count++;
            }
            
            // 计算平均值
            if (count > 0) {
                total = total.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            // 更新总成绩
            tableModel.setValueAt(total, row, 5);
            
            // 计算绩点和等级
            BigDecimal gradePoint = calculateGradePoint(total);
            String gradeLevel = calculateGradeLevel(total);
            
            tableModel.setValueAt(gradePoint, row, 6);
            tableModel.setValueAt(gradeLevel, row, 7);
            
        } catch (Exception e) {
            System.err.println("计算总成绩时发生错误: " + e.getMessage());
        }
    }

    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal calculateGradePoint(BigDecimal totalGrade) {
        if (totalGrade == null) return null;
        
        double score = totalGrade.doubleValue();
        if (score >= 95) return new BigDecimal("4.0");
        else if (score >= 90) return new BigDecimal("3.7");
        else if (score >= 85) return new BigDecimal("3.3");
        else if (score >= 80) return new BigDecimal("3.0");
        else if (score >= 75) return new BigDecimal("2.7");
        else if (score >= 70) return new BigDecimal("2.3");
        else if (score >= 65) return new BigDecimal("2.0");
        else if (score >= 60) return new BigDecimal("1.0");
        else return new BigDecimal("0.0");
    }

    private String calculateGradeLevel(BigDecimal totalGrade) {
        if (totalGrade == null) return "";
        
        double score = totalGrade.doubleValue();
        if (score >= 95) return "A+";
        else if (score >= 90) return "A";
        else if (score >= 85) return "B+";
        else if (score >= 80) return "B";
        else if (score >= 75) return "C+";
        else if (score >= 70) return "C";
        else if (score >= 65) return "D+";
        else if (score >= 60) return "D";
        else return "F";
    }

    private void saveGrades() {
        if (connection == null) {
            statusLabel.setText("无法保存：连接不可用");
            return;
        }
        
        try {
            statusLabel.setText("正在保存成绩...");
            
            // 遍历表格，更新成绩数据
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                GradeVO grade = gradeList.get(row);
                
                // 更新成绩信息
                grade.setMidtermGrade(getBigDecimalValue(tableModel.getValueAt(row, 2)));
                grade.setFinalGrade(getBigDecimalValue(tableModel.getValueAt(row, 3)));
                grade.setAssignmentGrade(getBigDecimalValue(tableModel.getValueAt(row, 4)));
                grade.setTotalGrade(getBigDecimalValue(tableModel.getValueAt(row, 5)));
                grade.setGradePoint(getBigDecimalValue(tableModel.getValueAt(row, 6)));
                grade.setGradeLevel((String) tableModel.getValueAt(row, 7));
                grade.setGradedTime(new java.sql.Timestamp(System.currentTimeMillis()));
                
                // 发送更新请求
                Message request = new Message();
                request.setType(MessageType.UPDATE_GRADE_REQUEST);
                request.setData(grade);
                
                if (!connection.sendMessage(request)) {
                    statusLabel.setText("保存失败：发送请求失败");
                    return;
                }
            }
            
            statusLabel.setText("成绩保存请求已发送");
            System.out.println("已发送成绩保存请求");
            
        } catch (Exception e) {
            statusLabel.setText("保存成绩时发生错误");
            System.err.println("保存成绩时发生错误: " + e.getMessage());
        }
    }

    private void updateCourseInfo() {
        if (currentCourse != null) {
            courseInfoLabel.setText("课程: " + currentCourse.getCourseName() + 
                                  " (" + currentCourse.getCourseCode() + ") - " + 
                                  "学期: " + currentCourse.getSemester());
        }
    }

    private String formatGradePoint(BigDecimal gradePoint) {
        if (gradePoint == null) return "";
        return gradePoint.toString();
    }

    /**
     * 获取成绩表格组件
     */
    public JTable getGradeTable() {
        return gradeTable;
    }

    /**
     * 刷新成绩数据
     */
    public void refreshData() {
        loadGradeData();
    }
}
