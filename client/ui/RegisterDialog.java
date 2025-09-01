package client.ui;

import client.controller.UserController;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 注册对话框
 * 提供用户注册功能
 */
public class RegisterDialog extends JDialog {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 350;
    
    private JTextField loginIdField;
    private JTextField nameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    
    private UserController userController;
    
    public RegisterDialog(JFrame parent, UserController userController) {
        super(parent, "用户注册", true);
        this.userController = userController;
        
        initComponents();
        setupLayout();
        setupEventListeners();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 创建组件
        loginIdField = new JTextField(20);
        nameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        roleComboBox = new JComboBox<>(new String[]{"学生", "教师"});
        registerButton = new JButton("注册");
        cancelButton = new JButton("取消");
        
        // 设置样式
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 12);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 12);
        Font buttonFont = new Font("微软雅黑", Font.BOLD, 12);
        
        loginIdField.setFont(fieldFont);
        nameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
        confirmPasswordField.setFont(fieldFont);
        phoneField.setFont(fieldFont);
        emailField.setFont(fieldFont);
        roleComboBox.setFont(fieldFont);
        registerButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        
        // 设置按钮颜色
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(128, 128, 128));
        cancelButton.setForeground(Color.WHITE);
        
        // 设置提示文本
        loginIdField.setToolTipText("请输入学号或教工号");
        nameField.setToolTipText("请输入真实姓名");
        phoneField.setToolTipText("请输入联系电话");
        emailField.setToolTipText("请输入邮箱地址");
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 248, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(25, 25, 112));
        titlePanel.add(titleLabel);
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 登录ID
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("登录ID:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(loginIdField, gbc);
        
        // 姓名
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("姓名:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // 密码
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordField, gbc);
        
        // 确认密码
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("确认密码:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(confirmPasswordField, gbc);
        
        // 联系电话
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("联系电话:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(phoneField, gbc);
        
        // 邮箱
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("邮箱:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(emailField, gbc);
        
        // 角色
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("角色:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(roleComboBox, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(buttonPanel, gbc);
        
        // 添加到对话框
        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 注册按钮
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });
        
        // 取消按钮
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * 执行注册
     */
    private void performRegister() {
        // 获取输入数据
        String loginId = loginIdField.getText().trim();
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        int roleIndex = roleComboBox.getSelectedIndex();
        
        // 验证输入
        if (loginId.isEmpty()) {
            showError("请输入登录ID");
            loginIdField.requestFocus();
            return;
        }
        
        if (name.isEmpty()) {
            showError("请输入姓名");
            nameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            passwordField.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            showError("密码长度不能少于6位");
            passwordField.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            confirmPasswordField.requestFocus();
            return;
        }
        
        // 验证邮箱格式（简单验证）
        if (!email.isEmpty() && !isValidEmail(email)) {
            showError("邮箱格式不正确");
            emailField.requestFocus();
            return;
        }
        
        // 创建用户对象
        UserVO user = new UserVO();
        user.setLoginId(loginId);
        user.setName(name);
        user.setPassword(password);
        user.setPhone(phone.isEmpty() ? null : phone);
        user.setEmail(email.isEmpty() ? null : email);
        user.setRole(roleIndex); // 0-学生, 1-教师
        
        // 禁用按钮防止重复点击
        setButtonsEnabled(false);
        
        // 执行注册
        userController.register(user, new UserController.RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(RegisterDialog.this, message, "注册成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    showError(errorMessage);
                    setButtonsEnabled(true);
                });
            }
        });
    }
    
    /**
     * 显示错误消息
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入错误", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 设置按钮启用状态
     */
    private void setButtonsEnabled(boolean enabled) {
        registerButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
