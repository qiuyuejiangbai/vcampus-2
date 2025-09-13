package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;

/**
 * 课程教学班卡片组件
 * 用于显示单个教学班的详细信息
 */
public class CourseClassCard extends JPanel {
    private CourseVO course;
    private UserVO currentUser;
    private JLabel teacherLabel;
    private JLabel classTimeLabel;
    private JLabel locationLabel;
    private JLabel capacityLabel;
    private JLabel enrolledCountLabel;
    private JLabel classInfoLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton enrollButton;
    private JButton dropButton;
    private CourseClassCardPanel parentPanel;
    private boolean isEnrolled = false; // 是否已选课
    
    public CourseClassCard(CourseVO course, CourseClassCardPanel parentPanel, UserVO currentUser) {
        this.course = course;
        this.parentPanel = parentPanel;
        this.currentUser = currentUser;
        initComponents();
        setupLayout();
        updateCardContent();
        setupHoverEffects();
        setupButtonEvents();
        // 不在这里设置消息监听器，避免重复设置
    }
    
    private void initComponents() {
        setBorder(UITheme.createCardBorder());
        setBackground(UITheme.WHITE);
        setPreferredSize(new Dimension(380, 320)); // 增加高度以容纳按钮
        setMinimumSize(new Dimension(380, 320));
        setMaximumSize(new Dimension(380, 320));
        
        // 创建标签
        classInfoLabel = new JLabel();
        teacherLabel = new JLabel();
        classTimeLabel = new JLabel();
        locationLabel = new JLabel();
        capacityLabel = new JLabel();
        enrolledCountLabel = new JLabel();
        
        // 根据用户身份创建不同的按钮
        if (currentUser != null && currentUser.isAdmin()) {
            // 管理员：编辑和删除按钮
            editButton = new JButton("编辑");
            deleteButton = new JButton("删除");
            
            UITheme.styleButton(editButton);
            editButton.setPreferredSize(new Dimension(60, 30));
            editButton.setBackground(UITheme.PRIMARY_GREEN);
            editButton.setForeground(UITheme.WHITE);
            
            UITheme.styleButton(deleteButton);
            deleteButton.setPreferredSize(new Dimension(60, 30));
            deleteButton.setBackground(new Color(220, 53, 69)); // 红色
            deleteButton.setForeground(UITheme.WHITE);
        } else if (currentUser != null && currentUser.isStudent()) {
            // 学生：选课和退选按钮
            enrollButton = new JButton("选课");
            dropButton = new JButton("退选");
            
            UITheme.styleButton(enrollButton);
            enrollButton.setPreferredSize(new Dimension(60, 30));
            enrollButton.setBackground(UITheme.PRIMARY_GREEN);
            enrollButton.setForeground(UITheme.WHITE);
            
            UITheme.styleButton(dropButton);
            dropButton.setPreferredSize(new Dimension(60, 30));
            dropButton.setBackground(new Color(220, 53, 69)); // 红色
            dropButton.setForeground(UITheme.WHITE);
            dropButton.setVisible(false); // 初始隐藏退选按钮
        }
        
        // 设置字体和颜色
        classInfoLabel.setFont(UITheme.SUBTITLE_FONT);
        classInfoLabel.setForeground(UITheme.PRIMARY_GREEN);
        
        teacherLabel.setFont(UITheme.CONTENT_FONT);
        teacherLabel.setForeground(UITheme.DARK_GRAY);
        
        classTimeLabel.setFont(UITheme.CONTENT_FONT);
        classTimeLabel.setForeground(UITheme.DARK_GRAY);
        
        locationLabel.setFont(UITheme.CONTENT_FONT);
        locationLabel.setForeground(UITheme.DARK_GRAY);
        
        capacityLabel.setFont(UITheme.CONTENT_FONT);
        capacityLabel.setForeground(UITheme.DARK_GRAY);
        
        enrolledCountLabel.setFont(UITheme.CONTENT_FONT);
        enrolledCountLabel.setForeground(UITheme.MEDIUM_GRAY);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, UITheme.PADDING_MEDIUM));
        
        // 顶部：教学班信息
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(UITheme.createEmptyBorder(0, 0, UITheme.PADDING_SMALL, 0));
        topPanel.add(classInfoLabel);
        
        // 中间：详细信息
        JPanel detailPanel = new JPanel(new GridLayout(2, 2, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL));
        detailPanel.setOpaque(false);
        detailPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_SMALL, 0, UITheme.PADDING_SMALL, 0));
        
        detailPanel.add(teacherLabel);
        detailPanel.add(classTimeLabel);
        detailPanel.add(locationLabel);
        detailPanel.add(capacityLabel);
        
        // 底部：选课信息和按钮
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_SMALL, 0, 0, 0));
        
        // 选课信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(enrolledCountLabel);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.PADDING_SMALL, 0));
        buttonPanel.setOpaque(false);
        
        // 根据用户身份添加不同的按钮
        if (currentUser != null && currentUser.isAdmin()) {
            if (editButton != null) buttonPanel.add(editButton);
            if (deleteButton != null) buttonPanel.add(deleteButton);
        } else if (currentUser != null && currentUser.isStudent()) {
            if (enrollButton != null) buttonPanel.add(enrollButton);
            if (dropButton != null) buttonPanel.add(dropButton);
        }
        
        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        add(detailPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupHoverEffects() {
        // 添加鼠标悬停效果
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBorder(UITheme.createRoundedBorder(UITheme.PRIMARY_GREEN, 2, UITheme.RADIUS_MEDIUM));
                setBackground(UITheme.VERY_LIGHT_GREEN);
                repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setBorder(UITheme.createCardBorder());
                setBackground(UITheme.WHITE);
                repaint();
            }
        });
        
        // 设置鼠标指针
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void updateCardContent() {
        if (course != null) {
            // 教学班标识（教师+时间）
            String classInfo = String.format("教学班 - %s", 
                course.getTeacherName() != null ? course.getTeacherName() : "未设置");
            classInfoLabel.setText(classInfo);
            
            // 详细信息
            teacherLabel.setText("教师: " + (course.getTeacherName() != null ? course.getTeacherName() : "未设置"));
            classTimeLabel.setText("时间: " + (course.getClassTime() != null ? course.getClassTime() : "未设置"));
            locationLabel.setText("地点: " + (course.getLocation() != null ? course.getLocation() : "未设置"));
            capacityLabel.setText("容量: " + (course.getCapacity() != null ? course.getCapacity().toString() : "未设置"));
            
            // 选课信息
            int enrolled = course.getEnrolledCount() != null ? course.getEnrolledCount() : 0;
            int capacity = course.getCapacity() != null ? course.getCapacity() : 0;
            String statusText = String.format("已选: %d/%d", enrolled, capacity);
            enrolledCountLabel.setText(statusText);
        }
    }
    
    /**
     * 获取课程信息
     * @return CourseVO对象
     */
    public CourseVO getCourse() {
        return course;
    }
    
    /**
     * 更新课程信息
     * @param course 新的课程信息
     */
    public void updateCourse(CourseVO course) {
        this.course = course;
        updateCardContent();
    }
    
    /**
     * 设置当前用户
     * @param currentUser 当前用户
     */
    public void setCurrentUser(UserVO currentUser) {
        this.currentUser = currentUser;
        // 重新创建按钮
        recreateButtons();
    }
    
    /**
     * 重新创建按钮
     */
    private void recreateButtons() {
        // 移除现有按钮
        if (editButton != null) {
            editButton.getParent().remove(editButton);
        }
        if (deleteButton != null) {
            deleteButton.getParent().remove(deleteButton);
        }
        if (enrollButton != null) {
            enrollButton.getParent().remove(enrollButton);
        }
        if (dropButton != null) {
            dropButton.getParent().remove(dropButton);
        }
        
        // 重新创建按钮
        if (currentUser != null && currentUser.isAdmin()) {
            // 管理员：编辑和删除按钮
            editButton = new JButton("编辑");
            deleteButton = new JButton("删除");
            
            UITheme.styleButton(editButton);
            editButton.setPreferredSize(new Dimension(60, 30));
            editButton.setBackground(UITheme.PRIMARY_GREEN);
            editButton.setForeground(UITheme.WHITE);
            
            UITheme.styleButton(deleteButton);
            deleteButton.setPreferredSize(new Dimension(60, 30));
            deleteButton.setBackground(new Color(220, 53, 69)); // 红色
            deleteButton.setForeground(UITheme.WHITE);
            
            // 添加事件监听器
            editButton.addActionListener(e -> showEditDialog());
            deleteButton.addActionListener(e -> showDeleteConfirmation());
            
        } else if (currentUser != null && currentUser.isStudent()) {
            // 学生：选课和退选按钮
            enrollButton = new JButton("选课");
            dropButton = new JButton("退选");
            
            UITheme.styleButton(enrollButton);
            enrollButton.setPreferredSize(new Dimension(60, 30));
            enrollButton.setBackground(UITheme.PRIMARY_GREEN);
            enrollButton.setForeground(UITheme.WHITE);
            
            UITheme.styleButton(dropButton);
            dropButton.setPreferredSize(new Dimension(60, 30));
            dropButton.setBackground(new Color(220, 53, 69)); // 红色
            dropButton.setForeground(UITheme.WHITE);
            dropButton.setVisible(false); // 初始隐藏退选按钮
            
            // 添加事件监听器
            enrollButton.addActionListener(e -> enrollCourse());
            dropButton.addActionListener(e -> dropCourse());
        }
        
        // 重新添加到按钮面板
        JPanel buttonPanel = (JPanel) ((JPanel) getComponent(2)).getComponent(1);
        if (currentUser != null && currentUser.isAdmin()) {
            if (editButton != null) buttonPanel.add(editButton);
            if (deleteButton != null) buttonPanel.add(deleteButton);
        } else if (currentUser != null && currentUser.isStudent()) {
            if (enrollButton != null) buttonPanel.add(enrollButton);
            if (dropButton != null) buttonPanel.add(dropButton);
        }
        
        // 刷新显示
        revalidate();
        repaint();
    }
    
    /**
     * 设置按钮事件
     */
    private void setupButtonEvents() {
        if (currentUser != null && currentUser.isAdmin()) {
            // 管理员按钮事件
            if (editButton != null) {
                editButton.addActionListener(e -> {
                    showEditDialog();
                });
            }
            
            if (deleteButton != null) {
                deleteButton.addActionListener(e -> {
                    showDeleteConfirmation();
                });
            }
        } else if (currentUser != null && currentUser.isStudent()) {
            // 学生按钮事件
            if (enrollButton != null) {
                enrollButton.addActionListener(e -> {
                    enrollCourse();
                });
            }
            
            if (dropButton != null) {
                dropButton.addActionListener(e -> {
                    dropCourse();
                });
            }
        }
    }
    
    /**
     * 显示编辑对话框
     */
    private void showEditDialog() {
        CourseEditDialog dialog = new CourseEditDialog(
            SwingUtilities.getWindowAncestor(this), 
            course, 
            this
        );
        dialog.setVisible(true);
    }
    
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmation() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要删除这个教学班吗？\n删除后无法恢复！",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            deleteCourse();
        }
    }
    
    /**
     * 删除课程
     */
    private void deleteCourse() {
        try {
            ServerConnection connection = ServerConnection.getInstance();
            if (!connection.isConnected()) {
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 创建删除请求消息
            Message request = new Message();
            request.setType(MessageType.DELETE_COURSE_REQUEST);
            request.setData(course.getCourseId());
            
            // 发送删除请求
            if (connection.sendMessage(request)) {
                System.out.println("已发送删除课程请求: " + course.getCourseName());
            } else {
                JOptionPane.showMessageDialog(this, "发送删除请求失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("删除课程时发生错误: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "删除课程时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 获取父面板
     * @return CourseClassCardPanel对象
     */
    public CourseClassCardPanel getParentPanel() {
        return parentPanel;
    }
    
    /**
     * 选课方法
     */
    private void enrollCourse() {
        try {
            ServerConnection connection = ServerConnection.getInstance();
            if (!connection.isConnected()) {
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 创建选课请求消息
            Message request = new Message();
            request.setType(MessageType.ENROLL_COURSE_REQUEST);
            request.setData(course.getCourseId());
            
            // 发送选课请求
            if (connection.sendMessage(request)) {
                System.out.println("已发送选课请求: " + course.getCourseName());
            } else {
                JOptionPane.showMessageDialog(this, "发送选课请求失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("选课时发生错误: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "选课时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 退选方法
     */
    private void dropCourse() {
        try {
            ServerConnection connection = ServerConnection.getInstance();
            if (!connection.isConnected()) {
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 创建退选请求消息
            Message request = new Message();
            request.setType(MessageType.DROP_COURSE_REQUEST);
            request.setData(course.getCourseId());
            
            // 发送退选请求
            if (connection.sendMessage(request)) {
                System.out.println("已发送退选请求: " + course.getCourseName());
            } else {
                JOptionPane.showMessageDialog(this, "发送退选请求失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("退选时发生错误: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "退选时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 更新选课状态
     * @param enrolled 是否已选课
     */
    public void updateEnrollmentStatus(boolean enrolled) {
        this.isEnrolled = enrolled;
        if (enrollButton != null && dropButton != null) {
            enrollButton.setVisible(!enrolled);
            dropButton.setVisible(enrolled);
        }
    }
    
    /**
     * 检查是否已选课
     * @return 是否已选课
     */
    public boolean isEnrolled() {
        return isEnrolled;
    }
    
}
