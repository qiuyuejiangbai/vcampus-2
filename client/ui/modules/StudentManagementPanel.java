package client.ui.modules;

import client.net.ServerConnection;
import client.ui.modules.course.UITheme;
import client.ui.util.HoverableTableCellRenderer;
import common.protocol.Message;
import common.protocol.MessageType;
import common.vo.StudentVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生信息管理面板
 * 提供学生信息的增删查改功能
 */
public class StudentManagementPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton resetPasswordButton;
    
    private UserVO currentUser;
    private ServerConnection connection;
    private List<StudentVO> studentList;
    private JLabel statusLabel; // 状态标签引用
    
    // 表格列名
    private final String[] columnNames = {
        "学生ID", "学号", "姓名", "性别", "专业", "班级", "院系", 
        "联系电话", "邮箱", "入学年份", "年级", "账户余额"
    };

    public StudentManagementPanel(UserVO currentUser, ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        this.studentList = new ArrayList<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        // 延迟加载数据，避免在连接未完全建立时弹窗
        SwingUtilities.invokeLater(() -> {
            if (connection != null && connection.isConnected()) {
                loadStudentData();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));

        // 创建表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };
        
        // 创建表格
        studentTable = new JTable(tableModel);
        studentTable.setFont(UITheme.CONTENT_FONT);
        studentTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setGridColor(UITheme.LIGHT_GRAY);
        studentTable.setShowGrid(false); // 隐藏网格线
        studentTable.setIntercellSpacing(new Dimension(0, 0)); // 移除单元格间距
        
        // 设置表头样式
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        header.setReorderingAllowed(false); // 禁止列重排序
        
        // 创建自定义表格渲染器，支持悬浮效果
        HoverableTableCellRenderer renderer = new HoverableTableCellRenderer();
        studentTable.setDefaultRenderer(Object.class, renderer);
        
        // 添加鼠标悬浮效果
        studentTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            private int lastHoveredRow = -1;
            
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row != lastHoveredRow) {
                    lastHoveredRow = row;
                    renderer.setHoveredRow(row);
                    studentTable.repaint();
                }
            }
        });
        
        // 添加鼠标离开效果
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                renderer.setHoveredRow(-1);
                studentTable.repaint();
            }
        });
        
        // 设置表格排序
        tableSorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(tableSorter);
        
        // 设置列宽
        setupColumnWidths();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.setBackground(UITheme.WHITE);
        scrollPane.getViewport().setBackground(UITheme.WHITE);
        
        // 自定义滚动条样式
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = UITheme.LIGHT_GREEN;
                this.trackColor = UITheme.VERY_LIGHT_GREEN;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 添加到主面板
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 设置表格列宽
     */
    private void setupColumnWidths() {
        // 设置各列的宽度
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // 学生ID - 减少宽度
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 学号
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(80);    // 姓名
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(50);    // 性别
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // 专业
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(80);    // 班级
        studentTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // 院系
        studentTable.getColumnModel().getColumn(7).setPreferredWidth(120);  // 联系电话
        studentTable.getColumnModel().getColumn(8).setPreferredWidth(150);  // 邮箱
        studentTable.getColumnModel().getColumn(9).setPreferredWidth(80);    // 入学年份
        studentTable.getColumnModel().getColumn(10).setPreferredWidth(60);  // 年级
        studentTable.getColumnModel().getColumn(11).setPreferredWidth(80);   // 账户余额
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM));
        buttonPanel.setBackground(UITheme.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, 0, UITheme.PADDING_SMALL, 0)
        ));
        
        // 操作标题
        JLabel actionTitle = new JLabel("学生管理操作");
        actionTitle.setFont(UITheme.SUBTITLE_FONT);
        actionTitle.setForeground(UITheme.DARK_GRAY);
        actionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UITheme.PADDING_LARGE));
        
        // 添加按钮
        addButton = new JButton("添加学生");
        stylePrimaryButton(addButton);
        addButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        editButton = new JButton("编辑学生");
        styleSecondaryButton(editButton);
        editButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        deleteButton = new JButton("删除学生");
        styleDangerButton(deleteButton);
        deleteButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        refreshButton = new JButton("刷新数据");
        styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(100, UITheme.BUTTON_HEIGHT));
        
        resetPasswordButton = new JButton("重置密码");
        styleWarningButton(resetPasswordButton);
        resetPasswordButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        // 初始状态设置
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);
        
        buttonPanel.add(actionTitle);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(resetPasswordButton);
        buttonPanel.add(refreshButton);
        
        return buttonPanel;
    }
    
    /**
     * 样式化主要按钮
     */
    private void stylePrimaryButton(JButton button) {
        button.setFont(UITheme.CONTENT_FONT);
        button.setBackground(UITheme.PRIMARY_GREEN);
        button.setForeground(UITheme.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.HOVER_GREEN);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.PRIMARY_GREEN);
            }
        });
    }
    
    /**
     * 样式化次要按钮
     */
    private void styleSecondaryButton(JButton button) {
        button.setFont(UITheme.CONTENT_FONT);
        button.setBackground(UITheme.WHITE);
        button.setForeground(UITheme.DARK_GRAY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.VERY_LIGHT_GREEN);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.LIGHT_GREEN, 1),
                    BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE)
                ));
            }
        });
    }
    
    /**
     * 样式化危险按钮
     */
    private void styleDangerButton(JButton button) {
        button.setFont(UITheme.CONTENT_FONT);
        button.setBackground(UITheme.ERROR_RED);
        button.setForeground(UITheme.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(200, 50, 60)); // 稍浅的红色
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.ERROR_RED);
            }
        });
    }
    
    /**
     * 样式化警告按钮
     */
    private void styleWarningButton(JButton button) {
        button.setFont(UITheme.CONTENT_FONT);
        button.setBackground(new Color(255, 193, 7)); // 橙色
        button.setForeground(UITheme.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(255, 180, 0)); // 稍深的橙色
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(255, 193, 7));
            }
        });
    }

    private void setupLayout() {
    }

    private void setupEventHandlers() {
        // 表格选择事件
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = studentTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                resetPasswordButton.setEnabled(hasSelection);
            }
        });
        
        // 添加按钮事件
        addButton.addActionListener(e -> showAddStudentDialog());
        
        // 编辑按钮事件
        editButton.addActionListener(e -> showEditStudentDialog());
        
        // 删除按钮事件
        deleteButton.addActionListener(e -> deleteSelectedStudent());
        
        // 密码重置按钮事件
        resetPasswordButton.addActionListener(e -> resetSelectedStudentPassword());
        
        // 刷新按钮事件
        refreshButton.addActionListener(e -> loadStudentData());
    }

    /**
     * 加载学生数据
     */
    public void loadStudentData() {
        // 发送获取所有学生信息的请求
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.GET_ALL_STUDENTS_REQUEST);
            request.setData(null);
            
            connection.setMessageListener(MessageType.GET_ALL_STUDENTS_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    List<StudentVO> students = (List<StudentVO>) message.getData();
                    if (students != null) {
                        updateStudentTable(students);
                    }
                });
            });
            
            connection.setMessageListener(MessageType.GET_ALL_STUDENTS_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "获取学生信息失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新学生表格
     */
    private void updateStudentTable(List<StudentVO> students) {
        studentList.clear();
        studentList.addAll(students);
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        for (StudentVO student : students) {
            Object[] row = {
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGenderName(), // 使用中文性别显示
                student.getMajor(),
                student.getClassName(),
                student.getDepartment(),
                student.getPhone(),
                student.getEmail(),
                student.getEnrollmentYear(),
                student.getGrade(),
                student.getBalance()
            };
            tableModel.addRow(row);
        }
        
        // 更新状态标签
        updateStatusLabel();
    }
    
    /**
     * 更新状态标签
     */
    private void updateStatusLabel() {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> {
                int count = studentList.size();
                statusLabel.setText("学生总数: " + count);
            });
        }
    }

    /**
     * 搜索学生
     */
    public void searchStudents(String searchText) {
        if (tableSorter != null) {
            if (searchText.trim().isEmpty()) {
                tableSorter.setRowFilter(null);
            } else {
                // 在学号、姓名、专业、班级中搜索
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 1, 2, 4, 5));
            }
        }
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        // 清除搜索过滤器
        if (tableSorter != null) {
            tableSorter.setRowFilter(null);
        }
        // 重新加载数据
        loadStudentData();
    }

    /**
     * 获取学生数量
     */
    public int getStudentCount() {
        return studentList.size();
    }

    /**
     * 显示添加学生对话框
     */
    private void showAddStudentDialog() {
        StudentEditDialog dialog = new StudentEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加学生", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            StudentVO newStudent = dialog.getStudent();
            if (newStudent != null) {
                addStudent(newStudent);
            }
        }
    }

    /**
     * 显示编辑学生对话框
     */
    private void showEditStudentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            StudentVO selectedStudent = studentList.get(modelRow);
            
            StudentEditDialog dialog = new StudentEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "编辑学生", selectedStudent);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                StudentVO updatedStudent = dialog.getStudent();
                if (updatedStudent != null) {
                    updateStudent(updatedStudent);
                }
            }
        }
    }

    /**
     * 删除选中的学生
     */
    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            StudentVO selectedStudent = studentList.get(modelRow);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "是否删除学生信息 " + selectedStudent.getName() + " (" + selectedStudent.getStudentNo() + ")？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                deleteStudent(selectedStudent.getId());
            }
        }
    }

    /**
     * 重置选中学生的密码
     */
    private void resetSelectedStudentPassword() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            StudentVO selectedStudent = studentList.get(modelRow);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要重置学生 " + selectedStudent.getName() + " (" + selectedStudent.getStudentNo() + ") 的密码为 123456 吗？",
                "确认重置密码",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                resetStudentPassword(selectedStudent.getUserId());
            }
        }
    }

    /**
     * 添加学生
     */
    private void addStudent(StudentVO student) {
        System.out.println("[DEBUG][StudentManagementPanel] ========== 开始添加学生 ==========");
        System.out.println("[DEBUG][StudentManagementPanel] 学生信息：");
        System.out.println("[DEBUG][StudentManagementPanel] - 学号: " + student.getStudentNo());
        System.out.println("[DEBUG][StudentManagementPanel] - 姓名: " + student.getName());
        System.out.println("[DEBUG][StudentManagementPanel] - 性别: " + student.getGender());
        System.out.println("[DEBUG][StudentManagementPanel] - 出生日期: " + student.getBirthDate());
        System.out.println("[DEBUG][StudentManagementPanel] - 联系电话: " + student.getPhone());
        System.out.println("[DEBUG][StudentManagementPanel] - 邮箱: " + student.getEmail());
        System.out.println("[DEBUG][StudentManagementPanel] - 地址: " + student.getAddress());
        System.out.println("[DEBUG][StudentManagementPanel] - 院系: " + student.getDepartment());
        System.out.println("[DEBUG][StudentManagementPanel] - 班级: " + student.getClassName());
        System.out.println("[DEBUG][StudentManagementPanel] - 专业: " + student.getMajor());
        System.out.println("[DEBUG][StudentManagementPanel] - 年级: " + student.getGrade());
        System.out.println("[DEBUG][StudentManagementPanel] - 入学年份: " + student.getEnrollmentYear());
        System.out.println("[DEBUG][StudentManagementPanel] - 账户余额: " + student.getBalance());
        System.out.println("[DEBUG][StudentManagementPanel] - 用户ID: " + student.getUserId());
        
        if (connection != null && connection.isConnected()) {
            System.out.println("[DEBUG][StudentManagementPanel] 服务器连接正常，准备发送添加学生请求");
            
            Message request = new Message();
            request.setType(MessageType.ADD_STUDENT);
            request.setData(student);
            
            System.out.println("[DEBUG][StudentManagementPanel] 设置成功消息监听器");
            connection.setMessageListener(MessageType.ADD_STUDENT_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][StudentManagementPanel] 收到学生添加成功响应");
                    JOptionPane.showMessageDialog(this, "学生添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentData();
                });
            });
            
            System.out.println("[DEBUG][StudentManagementPanel] 设置失败消息监听器");
            connection.setMessageListener(MessageType.ADD_STUDENT_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][StudentManagementPanel] 收到学生添加失败响应: " + message.getData());
                    JOptionPane.showMessageDialog(this, "学生添加失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            System.out.println("[DEBUG][StudentManagementPanel] 发送添加学生请求");
            connection.sendMessage(request);
            System.out.println("[DEBUG][StudentManagementPanel] ========== 学生添加请求已发送 ==========");
        } else {
            System.err.println("[DEBUG][StudentManagementPanel] 服务器连接异常，无法添加学生");
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新学生
     */
    private void updateStudent(StudentVO student) {
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.UPDATE_STUDENT);
            request.setData(student);
            
            connection.setMessageListener(MessageType.UPDATE_STUDENT_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "学生信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentData();
                });
            });
            
            connection.setMessageListener(MessageType.UPDATE_STUDENT_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "学生信息更新失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除学生
     */
    private void deleteStudent(Integer studentId) {
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.DELETE_STUDENT);
            request.setData(studentId);
            
            connection.setMessageListener(MessageType.DELETE_STUDENT_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "学生删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentData();
                });
            });
            
            connection.setMessageListener(MessageType.DELETE_STUDENT_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "学生删除失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 重置学生密码
     */
    private void resetStudentPassword(Integer userId) {
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.RESET_PASSWORD_REQUEST);
            request.setData(userId);
            
            connection.setMessageListener(MessageType.RESET_PASSWORD_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "密码重置成功，新密码为：123456", "成功", JOptionPane.INFORMATION_MESSAGE);
                });
            });
            
            connection.setMessageListener(MessageType.RESET_PASSWORD_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "密码重置失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters and Setters
    public void setCurrentUser(UserVO currentUser) {
        this.currentUser = currentUser;
    }

    public void setConnection(ServerConnection connection) {
        this.connection = connection;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    public JTable getStudentTable() {
        return studentTable;
    }
}
