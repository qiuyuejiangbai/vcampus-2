package client.ui.modules;

import client.net.ServerConnection;
import client.ui.modules.course.UITheme;
import client.ui.util.HoverableTableCellRenderer;
import common.protocol.Message;
import common.protocol.MessageType;
import common.vo.TeacherVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师信息管理面板
 * 提供教师信息的增删查改功能
 */
public class TeacherManagementPanel extends JPanel {
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton resetPasswordButton;
    
    private UserVO currentUser;
    private ServerConnection connection;
    private List<TeacherVO> teacherList;
    private JLabel statusLabel; // 状态标签引用
    
    // 表格列名
    private final String[] columnNames = {
        "教师ID", "工号", "姓名", "联系电话", "邮箱", "院系", "职称", "办公室", "研究方向", "账户余额"
    };

    public TeacherManagementPanel(UserVO currentUser, ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        this.teacherList = new ArrayList<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        // 延迟加载数据，避免在连接未完全建立时弹窗
        SwingUtilities.invokeLater(() -> {
            if (connection != null && connection.isConnected()) {
                loadTeacherData();
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
        teacherTable = new JTable(tableModel);
        teacherTable.setFont(UITheme.CONTENT_FONT);
        teacherTable.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        teacherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teacherTable.setGridColor(UITheme.LIGHT_GRAY);
        teacherTable.setShowGrid(false); // 隐藏网格线
        teacherTable.setIntercellSpacing(new Dimension(0, 0)); // 移除单元格间距
        
        // 设置表头样式
        JTableHeader header = teacherTable.getTableHeader();
        header.setFont(UITheme.SUBTITLE_FONT);
        header.setBackground(UITheme.PRIMARY_GREEN);
        header.setForeground(UITheme.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        header.setReorderingAllowed(false); // 禁止列重排序
        
        // 创建自定义表格渲染器，支持悬浮效果
        HoverableTableCellRenderer renderer = new HoverableTableCellRenderer();
        teacherTable.setDefaultRenderer(Object.class, renderer);
        
        // 添加鼠标悬浮效果
        teacherTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            private int lastHoveredRow = -1;
            
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = teacherTable.rowAtPoint(e.getPoint());
                if (row != lastHoveredRow) {
                    lastHoveredRow = row;
                    renderer.setHoveredRow(row);
                    teacherTable.repaint();
                }
            }
        });
        
        // 添加鼠标离开效果
        teacherTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                renderer.setHoveredRow(-1);
                teacherTable.repaint();
            }
        });
        
        // 设置表格排序
        tableSorter = new TableRowSorter<>(tableModel);
        teacherTable.setRowSorter(tableSorter);
        
        // 设置列宽
        setupColumnWidths();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(teacherTable);
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
        teacherTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // 教师ID
        teacherTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 工号
        teacherTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // 姓名
        teacherTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // 联系电话
        teacherTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // 邮箱
        teacherTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // 院系
        teacherTable.getColumnModel().getColumn(6).setPreferredWidth(80);    // 职称
        teacherTable.getColumnModel().getColumn(7).setPreferredWidth(100);   // 办公室
        teacherTable.getColumnModel().getColumn(8).setPreferredWidth(120);   // 研究方向
        teacherTable.getColumnModel().getColumn(9).setPreferredWidth(80);    // 账户余额
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM));
        buttonPanel.setBackground(UITheme.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, 0, UITheme.PADDING_MEDIUM, 0)
        ));
        
        // 操作标题
        JLabel actionTitle = new JLabel("教师管理操作");
        actionTitle.setFont(UITheme.SUBTITLE_FONT);
        actionTitle.setForeground(UITheme.DARK_GRAY);
        actionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UITheme.PADDING_LARGE));
        
        // 添加按钮
        addButton = new JButton("添加教师");
        stylePrimaryButton(addButton);
        addButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        editButton = new JButton("编辑教师");
        styleSecondaryButton(editButton);
        editButton.setPreferredSize(new Dimension(110, UITheme.BUTTON_HEIGHT));
        
        deleteButton = new JButton("删除教师");
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
        teacherTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = teacherTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                resetPasswordButton.setEnabled(hasSelection);
            }
        });
        
        // 添加按钮事件
        addButton.addActionListener(e -> showAddTeacherDialog());
        
        // 编辑按钮事件
        editButton.addActionListener(e -> showEditTeacherDialog());
        
        // 删除按钮事件
        deleteButton.addActionListener(e -> deleteSelectedTeacher());
        
        // 密码重置按钮事件
        resetPasswordButton.addActionListener(e -> resetSelectedTeacherPassword());
        
        // 刷新按钮事件
        refreshButton.addActionListener(e -> loadTeacherData());
    }

    /**
     * 加载教师数据
     */
    public void loadTeacherData() {
        // 发送获取所有教师信息的请求
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.GET_ALL_TEACHERS_REQUEST);
            request.setData(null);
            
            connection.setMessageListener(MessageType.GET_ALL_TEACHERS_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    @SuppressWarnings("unchecked")
                    List<TeacherVO> teachers = (List<TeacherVO>) message.getData();
                    if (teachers != null) {
                        updateTeacherTable(teachers);
                    }
                });
            });
            
            connection.setMessageListener(MessageType.GET_ALL_TEACHERS_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "获取教师信息失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新教师表格
     */
    private void updateTeacherTable(List<TeacherVO> teachers) {
        teacherList.clear();
        teacherList.addAll(teachers);
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        for (TeacherVO teacher : teachers) {
            Object[] row = {
                teacher.getId(),
                teacher.getTeacherNo(),
                teacher.getName(),
                teacher.getPhone(),
                teacher.getEmail(),
                teacher.getDepartment(),
                teacher.getTitle(),
                teacher.getOffice(),
                teacher.getResearchArea(),
                teacher.getBalance()
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
                int count = teacherList.size();
                statusLabel.setText("教师总数: " + count);
            });
        }
    }

    /**
     * 搜索教师
     */
    public void searchTeachers(String searchText) {
        if (tableSorter != null) {
            if (searchText.trim().isEmpty()) {
                tableSorter.setRowFilter(null);
            } else {
                // 在工号、姓名、院系、职称、账户余额中搜索
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 1, 2, 5, 6, 9));
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
        loadTeacherData();
    }

    /**
     * 获取教师数量
     */
    public int getTeacherCount() {
        return teacherList.size();
    }

    /**
     * 显示添加教师对话框
     */
    private void showAddTeacherDialog() {
        TeacherEditDialog dialog = new TeacherEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加教师", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            TeacherVO newTeacher = dialog.getTeacher();
            if (newTeacher != null) {
                addTeacher(newTeacher);
            }
        }
    }

    /**
     * 显示编辑教师对话框
     */
    private void showEditTeacherDialog() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = teacherTable.convertRowIndexToModel(selectedRow);
            TeacherVO selectedTeacher = teacherList.get(modelRow);
            
            TeacherEditDialog dialog = new TeacherEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "编辑教师", selectedTeacher);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                TeacherVO updatedTeacher = dialog.getTeacher();
                if (updatedTeacher != null) {
                    updateTeacher(updatedTeacher);
                }
            }
        }
    }

    /**
     * 删除选中的教师
     */
    private void deleteSelectedTeacher() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = teacherTable.convertRowIndexToModel(selectedRow);
            TeacherVO selectedTeacher = teacherList.get(modelRow);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除教师 " + selectedTeacher.getName() + " (" + selectedTeacher.getTeacherNo() + ") 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                deleteTeacher(selectedTeacher.getId());
            }
        }
    }

    /**
     * 重置选中教师的密码
     */
    private void resetSelectedTeacherPassword() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = teacherTable.convertRowIndexToModel(selectedRow);
            TeacherVO selectedTeacher = teacherList.get(modelRow);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要重置教师 " + selectedTeacher.getName() + " (" + selectedTeacher.getTeacherNo() + ") 的密码为 123456 吗？",
                "确认重置密码",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                resetTeacherPassword(selectedTeacher.getUserId());
            }
        }
    }

    /**
     * 添加教师
     */
    private void addTeacher(TeacherVO teacher) {
        System.out.println("[DEBUG][TeacherManagementPanel] ========== 开始添加教师 ==========");
        System.out.println("[DEBUG][TeacherManagementPanel] 教师信息：");
        System.out.println("[DEBUG][TeacherManagementPanel] - 工号: " + teacher.getTeacherNo());
        System.out.println("[DEBUG][TeacherManagementPanel] - 姓名: " + teacher.getName());
        System.out.println("[DEBUG][TeacherManagementPanel] - 联系电话: " + teacher.getPhone());
        System.out.println("[DEBUG][TeacherManagementPanel] - 邮箱: " + teacher.getEmail());
        System.out.println("[DEBUG][TeacherManagementPanel] - 院系: " + teacher.getDepartment());
        System.out.println("[DEBUG][TeacherManagementPanel] - 职称: " + teacher.getTitle());
        System.out.println("[DEBUG][TeacherManagementPanel] - 办公室: " + teacher.getOffice());
        System.out.println("[DEBUG][TeacherManagementPanel] - 研究方向: " + teacher.getResearchArea());
        System.out.println("[DEBUG][TeacherManagementPanel] - 用户ID: " + teacher.getUserId());
        
        if (connection != null && connection.isConnected()) {
            System.out.println("[DEBUG][TeacherManagementPanel] 服务器连接正常，准备发送添加教师请求");
            
            Message request = new Message();
            request.setType(MessageType.ADD_TEACHER);
            request.setData(teacher);
            
            System.out.println("[DEBUG][TeacherManagementPanel] 设置成功消息监听器");
            connection.setMessageListener(MessageType.ADD_TEACHER_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][TeacherManagementPanel] 收到教师添加成功响应");
                    JOptionPane.showMessageDialog(this, "教师添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadTeacherData();
                });
            });
            
            System.out.println("[DEBUG][TeacherManagementPanel] 设置失败消息监听器");
            connection.setMessageListener(MessageType.ADD_TEACHER_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][TeacherManagementPanel] 收到教师添加失败响应: " + message.getData());
                    JOptionPane.showMessageDialog(this, "教师添加失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            System.out.println("[DEBUG][TeacherManagementPanel] 发送添加教师请求");
            connection.sendMessage(request);
            System.out.println("[DEBUG][TeacherManagementPanel] ========== 教师添加请求已发送 ==========");
        } else {
            System.err.println("[DEBUG][TeacherManagementPanel] 服务器连接异常，无法添加教师");
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新教师
     */
    private void updateTeacher(TeacherVO teacher) {
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.UPDATE_TEACHER);
            request.setData(teacher);
            
            connection.setMessageListener(MessageType.UPDATE_TEACHER_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "教师信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadTeacherData();
                });
            });
            
            connection.setMessageListener(MessageType.UPDATE_TEACHER_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "教师信息更新失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除教师
     */
    private void deleteTeacher(Integer teacherId) {
        if (connection != null && connection.isConnected()) {
            Message request = new Message();
            request.setType(MessageType.DELETE_TEACHER);
            request.setData(teacherId);
            
            connection.setMessageListener(MessageType.DELETE_TEACHER_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "教师删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadTeacherData();
                });
            });
            
            connection.setMessageListener(MessageType.DELETE_TEACHER_FAILURE, message -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "教师删除失败: " + message.getData(), "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            connection.sendMessage(request);
        } else {
            JOptionPane.showMessageDialog(this, "服务器连接异常", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 重置教师密码
     */
    private void resetTeacherPassword(Integer userId) {
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

    public JTable getTeacherTable() {
        return teacherTable;
    }
}
