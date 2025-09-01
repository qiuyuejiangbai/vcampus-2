package client.ui;

import client.controller.UserController;
import client.net.ServerConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 登录界面
 * 系统的入口界面，提供用户登录和注册功能
 */
public class LoginFrame extends JFrame {
    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 700;
    
    // UI组件
    private JTextField loginIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;
    private JButton connectButton;
    private JLabel statusLabel;
    private JPanel loginCardPanel;
    private JLabel eyeIconLabel;
    private boolean passwordVisible = false;
    
    // 控制器
    private UserController userController;
    private ServerConnection serverConnection;
    
    // 颜色常量
    private static final Color PRIMARY_GREEN = new Color(52, 124, 84);
    private static final Color LIGHT_GREEN = new Color(76, 175, 80);
    private static final Color BACKGROUND_GREEN = new Color(230, 245, 235);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TEXT = new Color(128, 128, 128);
    
    public LoginFrame() {
        initComponents();
        setupLayout();
        setupEventListeners();
        
        // 初始化控制器和连接
        serverConnection = ServerConnection.getInstance();
        userController = new UserController();
        
        // 尝试连接服务器
        connectToServer();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setTitle("虚拟校园系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 创建输入框
        loginIdField = createStyledTextField("请输入学号或教工号");
        passwordField = createStyledPasswordField("请输入密码");
        
        // 创建登录按钮
        loginButton = createStyledButton("登录", PRIMARY_GREEN);
        
        // 创建注册链接
        registerLabel = new JLabel("<html><u>注册新账户</u></html>");
        registerLabel.setForeground(GRAY_TEXT);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 创建密码可见性切换图标
        eyeIconLabel = new JLabel("👁");
        eyeIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        // 创建连接状态相关组件
        connectButton = new JButton("连接服务器");
        connectButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        connectButton.setBackground(new Color(255, 140, 0));
        connectButton.setForeground(Color.WHITE);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        
        statusLabel = new JLabel("未连接服务器", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        
        // 初始状态
        loginButton.setEnabled(false);
        registerLabel.setEnabled(false);
    }
    
    /**
     * 创建样式化的文本框
     */
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        field.setPreferredSize(new Dimension(320, 45));
        
        // 添加占位符效果
        field.setForeground(GRAY_TEXT);
        field.setText(placeholder);
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(GRAY_TEXT);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    /**
     * 创建样式化的密码框
     */
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 45)
        ));
        field.setPreferredSize(new Dimension(320, 45));
        field.setEchoChar('●');
        
        return field;
    }
    
    /**
     * 创建样式化的按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(320, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 创建背景面板（带渐变效果）
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // 创建渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(144, 238, 144),  // 浅绿色
                    getWidth(), getHeight(), new Color(60, 179, 113)  // 深绿色
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        
        // 创建登录卡片面板
        loginCardPanel = createLoginCard();
        
        // 将卡片添加到背景面板中央
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        backgroundPanel.add(loginCardPanel, gbc);
        
        // 创建状态栏
        JPanel statusPanel = createStatusPanel();
        
        // 添加到主窗口
        add(backgroundPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建登录卡片
     */
    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角矩形背景
                g2d.setColor(WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                
                // 绘制阴影效果
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth(), getHeight(), 20, 20));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 500));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // 创建毕业帽图标
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制绿色圆形背景
                g2d.setColor(PRIMARY_GREEN);
                g2d.fillOval(0, 0, 60, 60);
                
                // 绘制毕业帽图标 (简化版)
                g2d.setColor(WHITE);
                g2d.setStroke(new BasicStroke(2));
                // 帽子底部
                g2d.drawLine(15, 35, 45, 35);
                // 帽子顶部
                g2d.drawLine(20, 25, 40, 25);
                // 连接线
                g2d.drawLine(30, 25, 30, 35);
                // 流苏
                g2d.drawLine(40, 25, 45, 20);
            }
        };
        iconLabel.setPreferredSize(new Dimension(60, 60));
        iconPanel.add(iconLabel);
        
        // 标题
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("虚拟校园系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        titlePanel.add(titleLabel);
        
        // 副标题
        JPanel subtitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subtitlePanel.setOpaque(false);
        JLabel subtitleLabel = new JLabel("请输入您的学号/教工号和密码登录");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(GRAY_TEXT);
        subtitlePanel.add(subtitleLabel);
        
        // 用户名输入区域
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernamePanel.setOpaque(false);
        
        JPanel usernameContainer = new JPanel(new BorderLayout());
        usernameContainer.setOpaque(false);
        usernameContainer.setPreferredSize(new Dimension(320, 60));
        
        JLabel usernameLabel = new JLabel("学号/教工号");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameLabel.setForeground(new Color(51, 51, 51));
        usernameLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        usernameContainer.add(usernameLabel, BorderLayout.NORTH);
        usernameContainer.add(loginIdField, BorderLayout.CENTER);
        usernamePanel.add(usernameContainer);
        
        // 密码输入区域
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.setOpaque(false);
        
        JPanel passwordContainer = new JPanel(new BorderLayout());
        passwordContainer.setOpaque(false);
        passwordContainer.setPreferredSize(new Dimension(320, 60));
        
        JLabel passwordLabel = new JLabel("密码");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(51, 51, 51));
        passwordLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        // 密码输入框容器（包含眼睛图标）
        JPanel passwordFieldContainer = new JPanel();
        passwordFieldContainer.setLayout(new OverlayLayout(passwordFieldContainer));
        passwordFieldContainer.setOpaque(false);
        
        passwordFieldContainer.add(passwordField);
        
        JPanel eyePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        eyePanel.setOpaque(false);
        eyePanel.setBorder(new EmptyBorder(0, 0, 0, 15));
        eyePanel.add(eyeIconLabel);
        passwordFieldContainer.add(eyePanel);
        
        passwordContainer.add(passwordLabel, BorderLayout.NORTH);
        passwordContainer.add(passwordFieldContainer, BorderLayout.CENTER);
        passwordPanel.add(passwordContainer);
        
        // 登录按钮
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setOpaque(false);
        loginPanel.add(loginButton);
        
        // 注册链接
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setOpaque(false);
        registerPanel.add(registerLabel);
        
        // 服务条款
        JPanel termsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        termsPanel.setOpaque(false);
        JLabel termsLabel = new JLabel("登录即表示您同意我们的服务条款和隐私政策");
        termsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        termsLabel.setForeground(new Color(153, 153, 153));
        termsPanel.add(termsLabel);
        
        // 添加组件到卡片
        card.add(Box.createVerticalStrut(10));
        card.add(iconPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(titlePanel);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitlePanel);
        card.add(Box.createVerticalStrut(30));
        card.add(usernamePanel);
        card.add(Box.createVerticalStrut(20));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(loginPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(registerPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(termsPanel);
        
        return card;
    }
    
    /**
     * 创建状态栏
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusPanel.setPreferredSize(new Dimension(FRAME_WIDTH, 40));
        
        JPanel statusLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLeftPanel.setOpaque(false);
        statusLeftPanel.add(statusLabel);
        
        JPanel statusRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusRightPanel.setOpaque(false);
        statusRightPanel.add(connectButton);
        
        statusPanel.add(statusLeftPanel, BorderLayout.WEST);
        statusPanel.add(statusRightPanel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 登录按钮
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // 注册链接
        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (registerLabel.isEnabled()) {
                    openRegisterDialog();
                }
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (registerLabel.isEnabled()) {
                    registerLabel.setForeground(PRIMARY_GREEN);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (registerLabel.isEnabled()) {
                    registerLabel.setForeground(GRAY_TEXT);
                }
            }
        });
        
        // 密码可见性切换
        eyeIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                togglePasswordVisibility();
            }
        });
        
        // 连接服务器按钮
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        // 回车键登录
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled()) {
                    performLogin();
                }
            }
        };
        
        loginIdField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }
    
    /**
     * 切换密码可见性
     */
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setEchoChar('●');
            eyeIconLabel.setText("👁");
            passwordVisible = false;
        } else {
            passwordField.setEchoChar((char) 0);
            eyeIconLabel.setText("🙈");
            passwordVisible = true;
        }
    }
    
    /**
     * 连接服务器
     */
    private void connectToServer() {
        connectButton.setEnabled(false);
        statusLabel.setText("正在连接服务器...");
        statusLabel.setForeground(Color.ORANGE);
        
        // 在后台线程中连接
        SwingUtilities.invokeLater(() -> {
            boolean connected = serverConnection.connect();
            
            if (connected) {
                statusLabel.setText("服务器连接成功");
                statusLabel.setForeground(new Color(0, 128, 0));
                loginButton.setEnabled(true);
                registerLabel.setEnabled(true);
                connectButton.setText("重新连接");
            } else {
                statusLabel.setText("服务器连接失败");
                statusLabel.setForeground(Color.RED);
                loginButton.setEnabled(false);
                registerLabel.setEnabled(false);
            }
            
            connectButton.setEnabled(true);
        });
    }
    
    /**
     * 执行登录
     */
    private void performLogin() {
        String loginId = loginIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 检查是否是占位符文本
        if (loginId.isEmpty() || loginId.equals("请输入学号或教工号")) {
            JOptionPane.showMessageDialog(this, "请输入学号或教工号", "提示", JOptionPane.WARNING_MESSAGE);
            loginIdField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        // 禁用按钮防止重复点击
        setButtonsEnabled(false);
        statusLabel.setText("正在登录...");
        statusLabel.setForeground(Color.ORANGE);
        
        // 执行登录
        userController.login(loginId, password, new UserController.LoginCallback() {
            @Override
            public void onSuccess(common.vo.UserVO user) {
                SwingUtilities.invokeLater(() -> {
                    // 登录成功，打开主界面
                    openMainFrame(user);
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(LoginFrame.this, errorMessage, "登录失败", JOptionPane.ERROR_MESSAGE);
                    setButtonsEnabled(true);
                    statusLabel.setText("服务器连接成功");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    passwordField.setText("");
                    passwordField.requestFocus();
                });
            }
        });
    }
    
    /**
     * 打开注册对话框
     */
    private void openRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this, userController);
        registerDialog.setVisible(true);
    }
    
    /**
     * 打开主界面
     */
    private void openMainFrame(common.vo.UserVO user) {
        // 隐藏登录界面
        setVisible(false);
        
        // 打开主界面
        MainFrame mainFrame = new MainFrame(user);
        mainFrame.setVisible(true);
        
        // 关闭登录界面
        dispose();
    }
    
    /**
     * 设置按钮启用状态
     */
    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerLabel.setEnabled(enabled);
        connectButton.setEnabled(enabled);
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        // 设置FlatLaf现代化外观
        try {
            // 使用FlatLaf亮色主题
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            
            // 可选：启用一些现代化特性
            System.setProperty("flatlaf.useRoundedBorders", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "false");
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf, using system default");
            e.printStackTrace();
            // 如果FlatLaf加载失败，回退到系统默认
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
