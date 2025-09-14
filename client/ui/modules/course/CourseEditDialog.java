package client.ui.modules.course;

import common.vo.CourseVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 课程编辑对话框
 * 用于编辑课程的教学班信息
 */
public class CourseEditDialog extends JDialog {
    private CourseVO course;
    
    // 输入组件
    private JTextField teacherField;
    private JTextField classTimeField;
    private JTextField locationField;
    private JSpinner capacitySpinner;
    private JLabel enrolledCountLabel;
    
    // 按钮
    private JButton saveButton;
    private JButton cancelButton;
    
    public CourseEditDialog(Window parent, CourseVO course, CourseClassCard parentCard) {
        super(parent, "编辑教学班信息", ModalityType.APPLICATION_MODAL);
        this.course = course;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        populateFields();
        
        // 设置对话框属性
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        // 创建输入组件
        teacherField = new JTextField(20);
        classTimeField = new JTextField(20);
        locationField = new JTextField(20);
        capacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        enrolledCountLabel = new JLabel();
        
        // 设置组件样式
        UITheme.styleTextField(teacherField);
        UITheme.styleTextField(classTimeField);
        UITheme.styleTextField(locationField);
        
        // 设置字体
        teacherField.setFont(UITheme.CONTENT_FONT);
        classTimeField.setFont(UITheme.CONTENT_FONT);
        locationField.setFont(UITheme.CONTENT_FONT);
        capacitySpinner.setFont(UITheme.CONTENT_FONT);
        enrolledCountLabel.setFont(UITheme.CONTENT_FONT);
        enrolledCountLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        // 创建按钮
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        
        // 设置按钮样式
        UITheme.styleButton(saveButton);
        UITheme.styleButton(cancelButton);
        saveButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        saveButton.setBackground(UITheme.PRIMARY_GREEN);
        saveButton.setForeground(UITheme.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WHITE);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.WHITE);
        mainPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        // 标题
        JLabel titleLabel = new JLabel("编辑教学班信息");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(UITheme.createEmptyBorder(0, 0, UITheme.PADDING_LARGE, 0));
        
        // 表单面板
        JPanel formPanel = createFormPanel();
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(UITheme.PADDING_LARGE));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.WHITE);
        formPanel.setBorder(UITheme.createCardBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 教师
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("教师:"), gbc);
        gbc.gridx = 1;
        formPanel.add(teacherField, gbc);
        
        // 时间
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("时间:"), gbc);
        gbc.gridx = 1;
        formPanel.add(classTimeField, gbc);
        
        // 地点
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("地点:"), gbc);
        gbc.gridx = 1;
        formPanel.add(locationField, gbc);
        
        // 容量
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("容量:"), gbc);
        gbc.gridx = 1;
        formPanel.add(capacitySpinner, gbc);
        
        // 已选人数（只读）
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("已选:"), gbc);
        gbc.gridx = 1;
        formPanel.add(enrolledCountLabel, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.PADDING_MEDIUM, 0));
        buttonPanel.setBackground(UITheme.WHITE);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        return buttonPanel;
    }
    
    private void setupEventHandlers() {
        // 保存按钮事件
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCourse();
            }
        });
        
        // 取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 回车键保存
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void populateFields() {
        if (course != null) {
            teacherField.setText(course.getTeacherName() != null ? course.getTeacherName() : "");
            classTimeField.setText(course.getClassTime() != null ? course.getClassTime() : "");
            locationField.setText(course.getLocation() != null ? course.getLocation() : "");
            capacitySpinner.setValue(course.getCapacity() != null ? course.getCapacity() : 1);
            
            int enrolled = course.getEnrolledCount() != null ? course.getEnrolledCount() : 0;
            enrolledCountLabel.setText(String.valueOf(enrolled) + " 人");
        }
    }
    
    private void saveCourse() {
        // 验证输入
        if (teacherField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入教师姓名", "输入错误", JOptionPane.WARNING_MESSAGE);
            teacherField.requestFocus();
            return;
        }
        
        if (classTimeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入上课时间", "输入错误", JOptionPane.WARNING_MESSAGE);
            classTimeField.requestFocus();
            return;
        }
        
        if (locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入上课地点", "输入错误", JOptionPane.WARNING_MESSAGE);
            locationField.requestFocus();
            return;
        }
        
        int capacity = (Integer) capacitySpinner.getValue();
        int enrolled = course.getEnrolledCount() != null ? course.getEnrolledCount() : 0;
        
        if (capacity < enrolled) {
            JOptionPane.showMessageDialog(this, 
                String.format("容量不能小于已选人数 (%d 人)", enrolled), 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
            capacitySpinner.requestFocus();
            return;
        }
        
        // 更新课程信息
        course.setTeacherName(teacherField.getText().trim());
        course.setClassTime(classTimeField.getText().trim());
        course.setLocation(locationField.getText().trim());
        course.setCapacity(capacity);
        
        // 发送更新请求
        try {
            ServerConnection connection = ServerConnection.getInstance();
            if (!connection.isConnected()) {
                JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 创建更新请求消息
            Message request = new Message();
            request.setType(MessageType.UPDATE_COURSE_REQUEST);
            request.setData(course);
            
            // 发送更新请求
            if (connection.sendMessage(request)) {
                System.out.println("已发送更新课程请求: " + course.getCourseName());
                JOptionPane.showMessageDialog(this, "课程信息已更新", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "发送更新请求失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("更新课程时发生错误: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "更新课程时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
