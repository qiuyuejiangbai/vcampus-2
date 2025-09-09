package client.ui;

import client.controller.UserController;
import common.vo.UserVO;
import client.ui.util.FontUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import client.ui.util.ScreenUtils;

/**
 * 注册页面
 * 替代登录页面的完整注册界面，使用与登录页面相同的背景
 */
public class RegisterFrame extends JFrame {
    
    // ==================== 设计常量 ====================
    
    // 尺寸常量 - 基础尺寸，实际大小将根据屏幕分辨率动态调整
    private static final int BASE_FRAME_WIDTH = 1200;
    private static final int BASE_FRAME_HEIGHT = 800;
    private static final int CARD_WIDTH = 580;
    private static final int CARD_PADDING = 30;
    private static final int COMPONENT_HEIGHT = 30;
    private static final int FIELD_SPACING = 10;
    private static final int SECTION_SPACING = 16;
    private static final int ARC_SIZE = 24;
    private static final int BUTTON_ARC_SIZE = 14;
    
    // 颜色常量 - 与登录页面保持一致
    private static final Color PRIMARY_COLOR = new Color(55, 161, 101);      // 主色#37A165
    private static final Color PRIMARY_PRESSED = new Color(46, 139, 87);     // 按下态#2E8B57
    private static final Color PRIMARY_DISABLED = new Color(167, 215, 190);  // 禁用态#A7D7BE
    private static final Color SECONDARY_COLOR = new Color(46, 125, 50);     // 次要色
    private static final Color ACCENT_COLOR = new Color(76, 175, 80);        // 强调色
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TEXT = new Color(107, 114, 128);         // 副标题灰度#6B7280
    private static final Color DARK_TEXT = new Color(17, 24, 39);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color ERROR_RED = new Color(220, 38, 38);           // 错误红色#DC2626
    private static final Color WARNING_ORANGE = new Color(245, 158, 11);
    private static final Color FOCUS_COLOR = new Color(55, 161, 101);        // 聚焦边框色
    
    // 背景颜色
    private static final Color INPUT_BACKGROUND = new Color(248, 249, 250);       // #F8F9FA
    
    // 字体常量 - 使用FontUtil动态获取
    private static Font getFontTitle() { return FontUtil.getSourceHanSansFont(Font.BOLD, 24); }
    private static Font getFontSubtitle() { return FontUtil.getSourceHanSansFont(Font.PLAIN, 12); }
    private static Font getFontLabel() { return FontUtil.getSourceHanSansFont(Font.PLAIN, 11); }
    private static Font getFontInput() { return FontUtil.getSourceHanSansFont(Font.PLAIN, 13); }
    private static Font getFontButton() { return FontUtil.getSourceHanSansFont(Font.BOLD, 16); }
    private static Font getFontError() { return FontUtil.getSourceHanSansFont(Font.PLAIN, 12); }
    private static Font getFontSmall() { return FontUtil.getSourceHanSansFont(Font.PLAIN, 11); }
    
    // ==================== UI组件 ====================
    
    // 表单字段
    private JComboBox<UserTypeOption> userTypeComboBox;
    private JTextField nameField;
    private JTextField studentIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField majorField;
    private JCheckBox agreementCheckBox;
    
    // 错误提示标签
    private JLabel nameErrorLabel;
    private JLabel studentIdErrorLabel;
    private JLabel passwordErrorLabel;
    private JLabel confirmPasswordErrorLabel;
    private JLabel phoneErrorLabel;
    private JLabel emailErrorLabel;
    
    // 按钮和其他组件
    private JButton submitButton;
    private JButton backButton;
    private JButton passwordVisibilityButton;
    private JButton confirmPasswordVisibilityButton;
    private JProgressBar passwordStrengthBar;
    private JLabel passwordStrengthLabel;
    
    // 密码可见性控制
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    
    // 背景图片
    private BufferedImage backgroundImage;
    
    // 控制器
    private UserController userController;
    
    // 动态显示的院系专业行
    private JPanel dynamicDepartmentRow;
    
    // 验证正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{8,12}$");
    
    // ==================== 用户类型选项 ====================
    
    private static class UserTypeOption {
        private final String name;
        private final String value;
        
        public UserTypeOption(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // ==================== 构造函数 ====================
    
    public RegisterFrame(UserController userController) {
        this.userController = userController;
        
        // 加载背景图片
        loadBackgroundImage();
        
        initComponents();
        setupLayout();
        setupEventListeners();
        setupValidation();
        
        setTitle("虚拟校园系统 - 用户注册");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 使用屏幕适配工具计算窗口大小
        Dimension windowSize = ScreenUtils.getRegisterWindowSize();
        setSize(windowSize);
        
        // 设置窗口居中显示
        Point centerLocation = ScreenUtils.getCenteredLocation(windowSize);
        setLocation(centerLocation);
        
        // 允许窗口大小调整
        setResizable(true);
        
        // 设置最小窗口大小
        setMinimumSize(new Dimension(900, 700));
    }
    
    // ==================== 初始化方法 ====================
    
    /**
     * 加载背景图片
     */
    private void loadBackgroundImage() {
        try {
            // 尝试多个可能的图片路径和文件名
            String[] possiblePaths = {
                "resources/images/bg.png",
                "resources/images/campus_background.png", 
                "resources/images/background.png",
                "../resources/images/bg.png",
                "../resources/images/campus_background.png",
                "./resources/images/bg.png",
                "./resources/images/campus_background.png"
            };
            
            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                    return;
                }
            }
            
            backgroundImage = null;
            
        } catch (IOException e) {
            backgroundImage = null;
        }
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        // 创建用户类型选项
        UserTypeOption[] userTypes = {
            new UserTypeOption("请选择用户类型", ""),
            new UserTypeOption("学生", "STUDENT"),
            new UserTypeOption("教师", "TEACHER")
        };
        
        // 初始化组件
        userTypeComboBox = createStyledComboBox(userTypes);
        nameField = createStyledTextField("请输入真实姓名");
        studentIdField = createStyledTextField("请输入学号/工号");
        passwordField = createStyledPasswordField("请输入登录密码");
        confirmPasswordField = createStyledPasswordField("请再次输入密码");
        phoneField = createStyledTextField("请输入手机号码");
        emailField = createStyledTextField("请输入邮箱地址");
        departmentField = createStyledTextField("请输入所属院系");
        majorField = createStyledTextField("请输入专业名称");
        
        // 创建错误标签
        nameErrorLabel = createErrorLabel();
        studentIdErrorLabel = createErrorLabel();
        passwordErrorLabel = createErrorLabel();
        confirmPasswordErrorLabel = createErrorLabel();
        phoneErrorLabel = createErrorLabel();
        emailErrorLabel = createErrorLabel();
        
        // 创建密码可见性按钮
        passwordVisibilityButton = createPasswordToggleButton();
        confirmPasswordVisibilityButton = createPasswordToggleButton();
        
        // 创建密码强度指示器
        passwordStrengthBar = createPasswordStrengthBar();
        passwordStrengthLabel = createPasswordStrengthLabel();
        
        // 创建协议复选框
        agreementCheckBox = createAgreementCheckBox();
        
        // 创建按钮
        submitButton = createPrimaryButton("提交注册申请");
        backButton = createSecondaryButton("返回登录");
        
        // 初始时禁用提交按钮
        submitButton.setEnabled(false);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 创建主背景面板
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintBackground(g);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        
        // 创建主卡片
        JPanel cardPanel = createCardPanel();
        
        // 居中放置卡片
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);
        backgroundPanel.add(cardPanel, gbc);
        
        add(backgroundPanel, BorderLayout.CENTER);
    }
    
    /**
     * 绘制背景 - 与登录页面相同
     */
    private void paintBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // 如果有背景图片，使用图片背景；否则使用渐变背景
        if (backgroundImage != null) {
            // 绘制背景图片，缩放以适应窗口大小
            g2d.drawImage(backgroundImage, 0, 0, width, height, null);
            
            // 添加轻磨砂蒙层（白色12-16%透明度），避免与卡片元素"抢对比"
            g2d.setColor(new Color(255, 255, 255, 40)); // 白色16%透明度磨砂蒙层
            g2d.fillRect(0, 0, width, height);
        } else {
            // 使用原来的渐变背景作为后备方案
            Color BACKGROUND_GRADIENT_START = new Color(240, 248, 240); // 背景渐变起始
            Color BACKGROUND_GRADIENT_END = new Color(245, 252, 245);   // 背景渐变结束
            
            GradientPaint gradient = new GradientPaint(
                0, 0, BACKGROUND_GRADIENT_START,
                width, height, BACKGROUND_GRADIENT_END
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            
            // 添加现代化几何装饰元素
            if (width > 300 && height > 300) {
                // 大圆形装饰
                g2d.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
                g2d.fillOval(width - 250, -150, 400, 400);
                g2d.fillOval(-150, height - 250, 400, 400);
                
                // 小圆形装饰
                g2d.setColor(new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue(), 20));
                g2d.fillOval(width - 400, 100, 150, 150);
                g2d.fillOval(100, height - 400, 150, 150);
                
                // 绿色点缀
                g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 15));
                g2d.fillOval(width / 2 - 50, 50, 100, 100);
                g2d.fillOval(50, height / 2 - 50, 100, 100);
            }
        }
        
        g2d.dispose();
    }
    
    /**
     * 创建主卡片面板
     */
    private JPanel createCardPanel() {
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int width = getWidth();
                int height = getHeight();
                
                if (width <= 0 || height <= 0) {
                    return;
                }
                
                // 绘制完全不透明的白色卡片背景
                g2d.setColor(WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, width - 2, height - 2, ARC_SIZE, ARC_SIZE));
                
                // 添加微妙的渐变边框
                GradientPaint borderGradient = new GradientPaint(
                    0, 0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 80),
                    width, height, new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue(), 80)
                );
                g2d.setPaint(borderGradient);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, width - 4, height - 4, ARC_SIZE, ARC_SIZE);
                
                // 添加高光效果
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fill(new RoundRectangle2D.Float(2, 2, width - 6, height / 2 - 10, 22, 22));
            }
        };
        
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(CARD_WIDTH, 640));
        cardPanel.setBorder(new EmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        
        // 添加标题区域
        cardPanel.add(createTitleSection());
        cardPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        
        // 添加表单区域
        cardPanel.add(createFormSection());
        cardPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        
        // 添加按钮区域
        cardPanel.add(createButtonSection());
        
        return cardPanel;
    }
    
    /**
     * 创建标题区域
     */
    private JPanel createTitleSection() {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        // 第一行：标题和返回按钮
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        
        // 左侧：标题和副标题
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(getFontTitle());
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("请填写以下信息完成注册申请");
        subtitleLabel.setFont(getFontSubtitle());
        subtitleLabel.setForeground(GRAY_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(subtitleLabel);
        
        // 右侧：返回按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        backButton.setPreferredSize(new Dimension(70, 28));
        rightPanel.add(backButton);
        
        headerRow.add(leftPanel, BorderLayout.WEST);
        headerRow.add(rightPanel, BorderLayout.EAST);
        
        titlePanel.add(headerRow);
        
        return titlePanel;
    }
    
    /**
     * 创建表单区域
     */
    private JPanel createFormSection() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        
        // 第一行：用户类型和真实姓名
        JPanel row1 = createFormRow(
            createFormField("用户类型", userTypeComboBox, null, true),
            createFormField("真实姓名", nameField, nameErrorLabel, true)
        );
        formPanel.add(row1);
        
        // 第二行：学号/工号（单独一行，居中显示）
        formPanel.add(createFormField("学号/工号", studentIdField, studentIdErrorLabel, true));
        
        // 第三行：登录密码
        JPanel passwordContainer = new JPanel(new BorderLayout());
        passwordContainer.setOpaque(false);
        passwordContainer.add(passwordField, BorderLayout.CENTER);
        passwordContainer.add(passwordVisibilityButton, BorderLayout.EAST);
        formPanel.add(createFormField("登录密码", passwordContainer, passwordErrorLabel, true));
        
        // 第四行：确认密码
        JPanel confirmPasswordContainer = new JPanel(new BorderLayout());
        confirmPasswordContainer.setOpaque(false);
        confirmPasswordContainer.add(confirmPasswordField, BorderLayout.CENTER);
        confirmPasswordContainer.add(confirmPasswordVisibilityButton, BorderLayout.EAST);
        formPanel.add(createFormField("确认密码", confirmPasswordContainer, confirmPasswordErrorLabel, true));
        
        // 密码强度指示器（紧凑版）
        JPanel strengthPanel = new JPanel(new BorderLayout(4, 0));
        strengthPanel.setOpaque(false);
        strengthPanel.add(passwordStrengthBar, BorderLayout.CENTER);
        strengthPanel.add(passwordStrengthLabel, BorderLayout.EAST);
        formPanel.add(createCompactFormField("密码强度", strengthPanel));
        
        // 第五行：手机号码和邮箱地址
        JPanel row5 = createFormRow(
            createFormField("手机号码", phoneField, phoneErrorLabel, true),
            createFormField("邮箱地址", emailField, emailErrorLabel, true)
        );
        formPanel.add(row5);
        
        // 第六行：所属院系和专业名称（动态显示）
        JPanel row6 = createDynamicDepartmentRow();
        formPanel.add(row6);
        
        formPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        // 协议复选框
        JPanel agreementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        agreementPanel.setOpaque(false);
        agreementPanel.add(agreementCheckBox);
        formPanel.add(agreementPanel);
        
        return formPanel;
    }
    
    /**
     * 创建表单行（两列布局）
     */
    private JPanel createFormRow(JPanel leftField, JPanel rightField) {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.add(leftField);
        row.add(rightField);
        return row;
    }
    
    /**
     * 创建动态院系专业行
     */
    private JPanel createDynamicDepartmentRow() {
        dynamicDepartmentRow = new JPanel(new GridLayout(1, 2, 20, 0));
        dynamicDepartmentRow.setOpaque(false);
        
        // 默认显示学生模式（院系+专业）
        updateDepartmentRow(true);
        
        return dynamicDepartmentRow;
    }
    
    /**
     * 更新院系专业行显示
     * @param isStudent true显示专业字段，false隐藏专业字段
     */
    private void updateDepartmentRow(boolean isStudent) {
        dynamicDepartmentRow.removeAll();
        
        JPanel departmentPanel = createFormField("所属院系", departmentField, null, false);
        dynamicDepartmentRow.add(departmentPanel);
        
        if (isStudent) {
            // 学生：显示专业字段
            JPanel majorPanel = createFormField("专业名称", majorField, null, false);
            dynamicDepartmentRow.add(majorPanel);
        } else {
            // 教师：显示空白面板
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            dynamicDepartmentRow.add(emptyPanel);
        }
        
        // 刷新布局
        dynamicDepartmentRow.revalidate();
        dynamicDepartmentRow.repaint();
    }
    
    /**
     * 根据用户类型更新院系专业行
     */
    private void updateDepartmentRowBasedOnUserType() {
        UserTypeOption selectedType = (UserTypeOption) userTypeComboBox.getSelectedItem();
        if (selectedType != null) {
            boolean isStudent = "STUDENT".equals(selectedType.getValue());
            updateDepartmentRow(isStudent);
            
            // 如果切换到教师，清空专业字段
            if (!isStudent) {
                majorField.setText("请输入专业名称");
                majorField.setForeground(GRAY_TEXT);
            }
        }
    }
    
    /**
     * 创建按钮区域
     */
    private JPanel createButtonSection() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        
        submitButton.setPreferredSize(new Dimension(180, COMPONENT_HEIGHT));
        buttonPanel.add(submitButton);
        
        return buttonPanel;
    }
    
    // ==================== 组件创建方法 ====================
    

    
    /**
     * 创建表单字段
     */
    private JPanel createFormField(String labelText, JComponent component, JLabel errorLabel, boolean required) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        
        // 标签行
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(getFontLabel());
        label.setForeground(DARK_TEXT);
        labelPanel.add(label);
        
        if (required) {
            JLabel requiredLabel = new JLabel(" *");
            requiredLabel.setFont(getFontLabel());
            requiredLabel.setForeground(ERROR_RED);
            labelPanel.add(requiredLabel);
        }
        
        fieldPanel.add(labelPanel);
        fieldPanel.add(Box.createVerticalStrut(4));
        
        // 组件行
        component.setPreferredSize(new Dimension(0, COMPONENT_HEIGHT));
        fieldPanel.add(component);
        
        // 错误提示行
        if (errorLabel != null) {
            fieldPanel.add(Box.createVerticalStrut(2));
            fieldPanel.add(errorLabel);
        }
        
        fieldPanel.add(Box.createVerticalStrut(FIELD_SPACING));
        
        return fieldPanel;
    }
    
    /**
     * 创建紧凑版表单字段（用于密码强度等辅助信息）
     */
    private JPanel createCompactFormField(String labelText, JComponent component) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        
        // 标签行
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 10)); // 进一步缩小字体
        label.setForeground(GRAY_TEXT);
        labelPanel.add(label);
        
        fieldPanel.add(labelPanel);
        fieldPanel.add(Box.createVerticalStrut(2)); // 进一步缩小间距
        
        // 组件行
        component.setPreferredSize(new Dimension(0, 16)); // 进一步缩小高度
        fieldPanel.add(component);
        
        fieldPanel.add(Box.createVerticalStrut(4)); // 进一步缩小底部间距
        
        return fieldPanel;
    }
    
    /**
     * 创建样式化文本框
     */
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                super.paintComponent(g);
            }
        };
        
        field.setFont(getFontInput());
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setBackground(INPUT_BACKGROUND);
        field.setOpaque(false);
        
        // 添加占位符效果
        field.setForeground(GRAY_TEXT);
        field.setText(placeholder);
        
        // 添加焦点效果
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(DARK_TEXT);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(FOCUS_COLOR, 1, 10),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(GRAY_TEXT);
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER_COLOR, 1, 10),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }
        });
        
        return field;
    }
    
    /**
     * 创建样式化密码框
     */
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                super.paintComponent(g);
            }
        };
        
        field.setFont(getFontInput());
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(5, 8, 5, 32) // 右侧留空间给按钮
        ));
        field.setBackground(INPUT_BACKGROUND);
        field.setOpaque(false);
        field.setEchoChar('●');
        
        // 添加焦点效果
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(FOCUS_COLOR, 1, 10),
                    BorderFactory.createEmptyBorder(5, 8, 5, 32)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER_COLOR, 1, 10),
                    BorderFactory.createEmptyBorder(5, 8, 5, 32)
                ));
            }
        });
        
        return field;
    }
    
    /**
     * 创建样式化下拉框
     */
    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(getFontInput());
        comboBox.setBackground(INPUT_BACKGROUND);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(2, 6, 2, 6) // 与输入框保持一致的圆角和内边距
        ));
        comboBox.setOpaque(false);
        return comboBox;
    }
    
    /**
     * 创建错误标签
     */
    private JLabel createErrorLabel() {
        JLabel label = new JLabel();
        label.setFont(getFontError());
        label.setForeground(ERROR_RED);
        label.setVisible(false);
        return label;
    }
    
    /**
     * 创建密码可见性切换按钮
     */
    private JButton createPasswordToggleButton() {
        JButton button = new JButton("显示");
        button.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        button.setBorder(null);
        button.setBackground(null);
        button.setForeground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(44, COMPONENT_HEIGHT));
        
        return button;
    }
    
    /**
     * 创建密码强度进度条
     */
    private JProgressBar createPasswordStrengthBar() {
        JProgressBar bar = new JProgressBar(0, 4);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(0, 4)); // 进一步缩小高度
        return bar;
    }
    
    /**
     * 创建密码强度标签
     */
    private JLabel createPasswordStrengthLabel() {
        JLabel label = new JLabel("弱");
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 9)); // 进一步缩小字体
        label.setForeground(GRAY_TEXT);
        return label;
    }
    
    /**
     * 创建协议复选框
     */
    private JCheckBox createAgreementCheckBox() {
        JCheckBox checkBox = new JCheckBox(
            "<html>我已阅读并同意 <font color='#37A165'><u>用户协议</u></font> 和 " +
            "<font color='#37A165'><u>隐私政策</u></font></html>"
        );
        checkBox.setFont(getFontSmall());
        checkBox.setForeground(GRAY_TEXT);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        
        return checkBox;
    }
    
    /**
     * 创建主按钮
     */
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                ButtonModel model = getModel();
                
                // 创建渐变背景
                Color startColor = model.isPressed() ? PRIMARY_COLOR.darker().darker() : 
                                 model.isRollover() ? PRIMARY_COLOR.darker() : PRIMARY_COLOR;
                Color endColor = model.isPressed() ? SECONDARY_COLOR.darker() :
                               model.isRollover() ? SECONDARY_COLOR : SECONDARY_COLOR.brighter();
                
                GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, BUTTON_ARC_SIZE, BUTTON_ARC_SIZE);
                
                // 添加高光效果
                if (!model.isPressed()) {
                    g2d.setColor(new Color(255, 255, 255, model.isRollover() ? 60 : 40));
                    g2d.fillRoundRect(2, 2, width - 4, height / 2, 12, 12);
                }
                
                // 绘制文本
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (width - fm.stringWidth(getText())) / 2;
                int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
            }
        };
        
        button.setFont(getFontButton());
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * 创建次要按钮
     */
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                boolean hovered = getModel().isRollover();
                
                // 绘制边框
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(
                    1, 1, getWidth() - 2, getHeight() - 2, 16, 16
                ));
                
                // 绘制背景（悬停时）
                if (hovered || pressed) {
                    g2d.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), 
                                          PRIMARY_COLOR.getBlue(), pressed ? 30 : 15));
                    g2d.fill(new RoundRectangle2D.Float(
                        1, 1, getWidth() - 2, getHeight() - 2, 16, 16
                    ));
                }
                
                // 绘制文字
                g2d.setColor(PRIMARY_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(getFontSubtitle());
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    // ==================== 事件设置 ====================
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 返回按钮 - 返回登录页面
        backButton.addActionListener(e -> {
            // 隐藏当前注册页面
            setVisible(false);
            
            // 显示登录页面
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            
            // 关闭注册页面
            dispose();
        });
        
        // 密码可见性切换
        passwordVisibilityButton.addActionListener(e -> togglePasswordVisibility(passwordField, passwordVisibilityButton, "passwordVisible"));
        confirmPasswordVisibilityButton.addActionListener(e -> togglePasswordVisibility(confirmPasswordField, confirmPasswordVisibilityButton, "confirmPasswordVisible"));
        
        // 用户类型变化
        userTypeComboBox.addActionListener(e -> {
            updateDepartmentRowBasedOnUserType();
            updateSubmitButtonState();
        });
        
        // 协议复选框
        agreementCheckBox.addActionListener(e -> updateSubmitButtonState());
        
        // 提交按钮
        submitButton.addActionListener(e -> submitRegistration());
        
        // 键盘绑定
        setupKeyBindings();
    }
    
    /**
     * 切换密码可见性
     */
    private void togglePasswordVisibility(JPasswordField field, JButton button, String type) {
        if ("passwordVisible".equals(type)) {
            passwordVisible = !passwordVisible;
            field.setEchoChar(passwordVisible ? (char) 0 : '●');
            button.setText(passwordVisible ? "隐藏" : "显示");
        } else {
            confirmPasswordVisible = !confirmPasswordVisible;
            field.setEchoChar(confirmPasswordVisible ? (char) 0 : '●');
            button.setText(confirmPasswordVisible ? "隐藏" : "显示");
        }
    }
    
    /**
     * 设置键盘绑定
     */
    private void setupKeyBindings() {
        // ESC键返回登录页面
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backButton.doClick(); // 触发返回按钮事件
            }
        });
        
        // Enter键提交（如果按钮可用）
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKey, "SUBMIT");
        getRootPane().getActionMap().put("SUBMIT", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (submitButton.isEnabled()) {
                    submitRegistration();
                }
            }
        });
        
        // 设置默认按钮
        getRootPane().setDefaultButton(submitButton);
    }
    
    /**
     * 设置验证
     */
    private void setupValidation() {
        // 为所有输入字段添加文档监听器
        addDocumentListener(nameField, this::validateName);
        addDocumentListener(studentIdField, this::validateStudentId);
        addDocumentListener(passwordField, this::validatePassword);
        addDocumentListener(confirmPasswordField, this::validateConfirmPassword);
        addDocumentListener(phoneField, this::validatePhone);
        addDocumentListener(emailField, this::validateEmail);
    }
    
    /**
     * 添加文档监听器
     */
    private void addDocumentListener(JTextComponent field, Runnable validator) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    validator.run();
                    updateSubmitButtonState();
                });
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    validator.run();
                    updateSubmitButtonState();
                });
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    validator.run();
                    updateSubmitButtonState();
                });
            }
        });
    }
    
    // ==================== 验证方法 ====================
    
    /**
     * 验证姓名
     */
    private void validateName() {
        String name = getFieldText(nameField, "请输入真实姓名");
        if (name.isEmpty()) {
            showFieldError(nameErrorLabel, "姓名不能为空");
        } else if (name.length() < 2 || name.length() > 20) {
            showFieldError(nameErrorLabel, "姓名长度应在2-20个字符之间");
        } else if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z]+$")) {
            showFieldError(nameErrorLabel, "姓名只能包含中文和英文字母");
        } else {
            hideFieldError(nameErrorLabel);
        }
    }
    
    /**
     * 验证学号/工号
     */
    private void validateStudentId() {
        String studentId = getFieldText(studentIdField, "请输入学号/工号");
        if (studentId.isEmpty()) {
            showFieldError(studentIdErrorLabel, "学号/工号不能为空");
        } else if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
            showFieldError(studentIdErrorLabel, "学号/工号应为8-12位数字");
        } else {
            hideFieldError(studentIdErrorLabel);
        }
    }
    
    /**
     * 验证密码并更新强度指示器
     */
    private void validatePassword() {
        String password = new String(passwordField.getPassword());
        
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "密码不能为空");
            updatePasswordStrength(0);
        } else if (password.length() < 6) {
            showFieldError(passwordErrorLabel, "密码长度至少6位");
            updatePasswordStrength(1);
        } else {
            hideFieldError(passwordErrorLabel);
            
            // 计算密码强度
            int strength = calculatePasswordStrength(password);
            updatePasswordStrength(strength);
        }
        
        // 重新验证确认密码
        validateConfirmPassword();
    }
    
    /**
     * 验证确认密码
     */
    private void validateConfirmPassword() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordErrorLabel, "请确认密码");
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordErrorLabel, "两次输入的密码不一致");
        } else {
            hideFieldError(confirmPasswordErrorLabel);
        }
    }
    
    /**
     * 计算密码强度
     */
    private int calculatePasswordStrength(String password) {
        int strength = 1; // 基础强度
        
        // 包含数字
        if (password.matches(".*\\d.*")) strength++;
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) strength++;
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) strength++;
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            strength++;
        }
        
        // 长度加分
        if (password.length() >= 12) strength++;
        
        return Math.min(strength, 4);
    }
    
    /**
     * 更新密码强度指示器
     */
    private void updatePasswordStrength(int strength) {
        passwordStrengthBar.setValue(strength);
        
        String[] labels = {"很弱", "弱", "中等", "强", "很强"};
        Color[] colors = {
            new Color(244, 67, 54),   // 红色
            new Color(255, 152, 0),   // 橙色
            new Color(255, 193, 7),   // 黄色
            new Color(76, 175, 80),   // 绿色
            new Color(46, 125, 50)    // 深绿色
        };
        
        if (strength > 0 && strength <= labels.length) {
            passwordStrengthLabel.setText(labels[strength - 1]);
            passwordStrengthBar.setForeground(colors[strength - 1]);
        } else {
            passwordStrengthLabel.setText("");
        }
    }
    
    /**
     * 验证手机号
     */
    private void validatePhone() {
        String phone = getFieldText(phoneField, "请输入手机号码");
        if (phone.isEmpty()) {
            showFieldError(phoneErrorLabel, "手机号不能为空");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            showFieldError(phoneErrorLabel, "请输入正确的手机号格式");
        } else {
            hideFieldError(phoneErrorLabel);
        }
    }
    
    /**
     * 验证邮箱
     */
    private void validateEmail() {
        String email = getFieldText(emailField, "请输入邮箱地址");
        if (email.isEmpty()) {
            showFieldError(emailErrorLabel, "邮箱不能为空");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(emailErrorLabel, "请输入正确的邮箱格式");
        } else {
            hideFieldError(emailErrorLabel);
        }
    }
    
    /**
     * 获取字段文本（排除占位符）
     */
    private String getFieldText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }
    
    /**
     * 显示字段错误
     */
    private void showFieldError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * 隐藏字段错误
     */
    private void hideFieldError(JLabel errorLabel) {
        errorLabel.setVisible(false);
    }
    
    /**
     * 检查表单是否有效
     */
    private boolean isFormValid() {
        return !nameErrorLabel.isVisible() &&
               !studentIdErrorLabel.isVisible() &&
               !passwordErrorLabel.isVisible() &&
               !confirmPasswordErrorLabel.isVisible() &&
               !phoneErrorLabel.isVisible() &&
               !emailErrorLabel.isVisible() &&
               !getFieldText(nameField, "请输入真实姓名").isEmpty() &&
               !getFieldText(studentIdField, "请输入学号/工号").isEmpty() &&
               passwordField.getPassword().length >= 6 &&
               !getFieldText(phoneField, "请输入手机号码").isEmpty() &&
               !getFieldText(emailField, "请输入邮箱地址").isEmpty() &&
               userTypeComboBox.getSelectedIndex() > 0 &&
               agreementCheckBox.isSelected();
    }
    
    /**
     * 更新提交按钮状态
     */
    private void updateSubmitButtonState() {
        submitButton.setEnabled(isFormValid());
    }
    
    /**
     * 聚焦到第一个错误字段
     */
    private void focusFirstErrorField() {
        if (nameErrorLabel.isVisible()) {
            nameField.requestFocus();
            nameField.selectAll();
        } else if (studentIdErrorLabel.isVisible()) {
            studentIdField.requestFocus();
            studentIdField.selectAll();
        } else if (passwordErrorLabel.isVisible()) {
            passwordField.requestFocus();
            passwordField.selectAll();
        } else if (confirmPasswordErrorLabel.isVisible()) {
            confirmPasswordField.requestFocus();
            confirmPasswordField.selectAll();
        } else if (phoneErrorLabel.isVisible()) {
            phoneField.requestFocus();
            phoneField.selectAll();
        } else if (emailErrorLabel.isVisible()) {
            emailField.requestFocus();
            emailField.selectAll();
        }
    }
    
    /**
     * 提交注册
     */
    private void submitRegistration() {
        if (!isFormValid()) {
            JOptionPane.showMessageDialog(this, 
                "请填写完整的信息并确保格式正确", 
                "表单验证失败", 
                JOptionPane.WARNING_MESSAGE);
            focusFirstErrorField();
            return;
        }
        
        // 禁用按钮并显示加载状态
        submitButton.setEnabled(false);
        submitButton.setText("正在提交...");
        
        // 创建用户对象
        UserVO user = new UserVO();
        UserTypeOption userType = (UserTypeOption) userTypeComboBox.getSelectedItem();
        
        // 角色转换：字符串转为整数
        Integer roleCode = null;
        if ("STUDENT".equals(userType.getValue())) {
            roleCode = 0;
        } else if ("TEACHER".equals(userType.getValue())) {
            roleCode = 1;
        }
        
        user.setRole(roleCode);
        user.setId(getFieldText(studentIdField, "请输入学号/工号"));
        user.setPassword(new String(passwordField.getPassword()));
        
        // 收集详细信息
        String name = getFieldText(nameField, "请输入真实姓名");
        String phone = getFieldText(phoneField, "请输入手机号码");
        String email = getFieldText(emailField, "请输入邮箱地址");
        String department = getFieldText(departmentField, "请输入所属院系");
        String major = null;
        String title = null;
        
        // 根据角色设置专业或职称
        if ("STUDENT".equals(userType.getValue())) {
            major = getFieldText(majorField, "请输入专业名称");
        } else if ("TEACHER".equals(userType.getValue())) {
            // 教师可以设置职称，如果有相关字段的话
            title = "讲师"; // 默认职称
        }
        
        // 提交注册请求
        userController.registerWithDetails(user, name, phone, email, department, major, title, new UserController.RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                SwingUtilities.invokeLater(() -> {
                    // 显示成功对话框
                    String successMessage = "<html><div style='text-align: center; padding: 20px;'>" +
                        "<div style='font-size: 18px; color: #43A047; margin-bottom: 10px;'>✓ 注册申请提交成功</div>" +
                        "<div style='font-size: 14px; color: #6C757D;'>" + message + "</div>" +
                        "<div style='font-size: 12px; color: #6C757D; margin-top: 10px;'>账户已自动激活，您可以直接登录使用系统</div>" +
                        "</div></html>";
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        successMessage,
                        "注册成功",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // 返回登录页面
                    backButton.doClick();
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    // 显示错误对话框
                    String failureMessage = "<html><div style='text-align: center; padding: 20px;'>" +
                        "<div style='font-size: 18px; color: #E53935; margin-bottom: 10px;'>✗ 注册申请失败</div>" +
                        "<div style='font-size: 14px; color: #6C757D;'>" + errorMessage + "</div>" +
                        "</div></html>";
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        failureMessage,
                        "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // 恢复按钮状态
                    submitButton.setEnabled(true);
                    submitButton.setText("提交注册申请");
                    
                    // 聚焦到第一个错误字段（如果有验证错误）
                    if (errorMessage.contains("用户名") || errorMessage.contains("学号")) {
                        studentIdField.requestFocus();
                        studentIdField.selectAll();
                    } else if (errorMessage.contains("邮箱")) {
                        emailField.requestFocus();
                        emailField.selectAll();
                    } else if (errorMessage.contains("手机")) {
                        phoneField.requestFocus();
                        phoneField.selectAll();
                    }
                });
            }
        });
    }
    
    /**
     * 自定义圆角边框
     */
    private static class RoundedBorder implements Border {
        private Color color;
        private int thickness;
        private int radius;
        
        public RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x + thickness / 2, y + thickness / 2, 
                             width - thickness, height - thickness, radius, radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
