package client.ui;

import client.controller.UserController;
import client.net.ServerConnection;
import client.ui.util.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.border.AbstractBorder;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import client.ui.util.ScreenUtils;

/**
 * 登录界面
 * 系统的入口界面，提供用户登录和注册功能
 */
public class LoginFrame extends JFrame {
    // 窗口大小 - 恢复为合适的固定大小，确保内容完整展示
    private static final int BASE_FRAME_WIDTH = 1200;
    private static final int BASE_FRAME_HEIGHT = 800;
    
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
    
    // 错误提示标签
    private JLabel loginIdErrorLabel;
    private JLabel passwordErrorLabel;
    
    // 加载状态
    private boolean isLoading = false;
    private String originalButtonText;
    
    // 连接状态提示
    private JLabel connectionToast;
    
    // 控制器
    private UserController userController;
    private ServerConnection serverConnection;
    
    // 背景图片
    private BufferedImage backgroundImage;
    
    // 现代化颜色常量 - 提升设计感的主色调
    private static final Color PRIMARY_COLOR = new Color(55, 161, 101);      // 主色#37A165
    private static final Color PRIMARY_PRESSED = new Color(46, 139, 87);     // 按下态#2E8B57
    private static final Color PRIMARY_DISABLED = new Color(167, 215, 190);  // 禁用态#A7D7BE
    private static final Color SECONDARY_COLOR = new Color(46, 125, 50);     // 次要色
    private static final Color ACCENT_COLOR = new Color(76, 175, 80);        // 强调色
    private static final Color BACKGROUND_GRADIENT_START = new Color(240, 248, 240); // 背景渐变起始
    private static final Color BACKGROUND_GRADIENT_END = new Color(245, 252, 245);   // 背景渐变结束
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TEXT = new Color(107, 114, 128);         // 副标题灰度#6B7280
    private static final Color DARK_TEXT = new Color(17, 24, 39);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color ERROR_RED = new Color(220, 38, 38);           // 错误红色#DC2626
    private static final Color WARNING_ORANGE = new Color(245, 158, 11);
    private static final Color FOCUS_COLOR = new Color(55, 161, 101);        // 聚焦边框色
    
    public LoginFrame() {
        // 加载背景图片
        loadBackgroundImage();
        
        initComponents();
        setupLayout();
        setupEventListeners();
        
        // 初始化控制器和连接
        serverConnection = ServerConnection.getInstance();
        userController = new UserController();
        
        // 启动淡入动画
        startFadeInAnimation();
        
        // 尝试连接服务器
        connectToServer();
    }
    
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
        setTitle("虚拟校园系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 使用固定的窗口大小，确保内容完整展示
        setSize(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT);
        
        // 设置窗口居中显示
        Point centerLocation = ScreenUtils.getCenteredLocation(new Dimension(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT));
        setLocation(centerLocation);
        
        // 允许窗口大小调整
        setResizable(true);
        
        // 设置最小窗口大小，确保内容不被压缩
        setMinimumSize(new Dimension(1000, 700));
        
        // 创建输入框 - 简化占位文本
        loginIdField = createStyledTextField("学号/教工号");
        passwordField = createStyledPasswordField("密码");
        
        // 创建登录按钮
        loginButton = createStyledButton("登录", PRIMARY_COLOR);
        
        // 创建注册链接 - 统一字体，确保可点击区域≥44px
        registerLabel = new JLabel("<html><u>注册新账户</u></html>");
        registerLabel.setForeground(GRAY_TEXT);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontUtil.setLabelFont(registerLabel, Font.PLAIN, 14);
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerLabel.setPreferredSize(new Dimension(120, 44)); // 确保可点击区域≥44px
        registerLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // 增加点击区域
        
        // 创建密码可见性切换图标 - 统一字体，确保可点击区域≥44px
        eyeIconLabel = new JLabel("显示");
        eyeIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontUtil.setLabelFont(eyeIconLabel, Font.PLAIN, 12);
        eyeIconLabel.setForeground(PRIMARY_COLOR);
        eyeIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        eyeIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        eyeIconLabel.setPreferredSize(new Dimension(44, 44)); // 确保可点击区域≥44px
        eyeIconLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // 增加点击区域
        
        // 创建连接状态相关组件，确保高度≥44px
        connectButton = new JButton("连接服务器");
        connectButton.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        connectButton.setBackground(WARNING_ORANGE);
        connectButton.setForeground(Color.WHITE);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.setPreferredSize(new Dimension(120, 44)); // 确保高度≥44px
        
        statusLabel = new JLabel("未连接服务器", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_RED);
        
        // 创建错误提示标签 - 12px红色小字
        loginIdErrorLabel = new JLabel(" ");
        loginIdErrorLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12)); // 12px小字
        loginIdErrorLabel.setForeground(ERROR_RED); // 红色#DC2626
        loginIdErrorLabel.setVisible(false);
        
        passwordErrorLabel = new JLabel(" ");
        passwordErrorLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12)); // 12px小字
        passwordErrorLabel.setForeground(ERROR_RED); // 红色#DC2626
        passwordErrorLabel.setVisible(false);
        
        // 初始状态
        loginButton.setEnabled(false);
        registerLabel.setEnabled(false);
    }
    
    /**
     * 创建样式化的文本框 - 统一尺寸44px高度
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
        
        // 统一字体设置 - 调整为14px，避免数字显示过大
        field.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 12),
            BorderFactory.createEmptyBorder(12, 16, 12, 16) // 统一内边距
        ));
        field.setPreferredSize(new Dimension(400, 44)); // 增加输入框宽度到400px，统一高度44px
        field.setBackground(new Color(248, 250, 252));
        field.setOpaque(false);
        
        // 添加占位符效果
        field.setForeground(GRAY_TEXT);
        field.setText(placeholder);
        
        // 添加焦点效果和动画
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                animateFieldFocus(field, true, placeholder);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                animateFieldFocus(field, false, placeholder);
            }
        });
        
        return field;
    }
    
    /**
     * 创建样式化的密码框 - 统一尺寸44px高度
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
        
        // 统一字体设置 - 调整为14px，避免密码字符显示过大
        field.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 12),
            BorderFactory.createEmptyBorder(12, 16, 12, 48) // 右侧留空间给眼睛图标
        ));
        field.setPreferredSize(new Dimension(400, 44)); // 增加密码框宽度到400px，统一高度44px
        field.setBackground(new Color(248, 250, 252));
        field.setOpaque(false);
        field.setEchoChar('•'); // 使用更小的密码字符
        
        // 添加焦点效果
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                animatePasswordFieldFocus(field, true);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                animatePasswordFieldFocus(field, false);
            }
        });
        
        return field;
    }
    
    /**
     * 创建样式化的按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
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
                Color startColor = model.isPressed() ? bgColor.darker().darker() : 
                                 model.isRollover() ? bgColor.darker() : bgColor;
                Color endColor = model.isPressed() ? SECONDARY_COLOR.darker() :
                               model.isRollover() ? SECONDARY_COLOR : SECONDARY_COLOR.brighter();
                
                GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 14, 14);
                
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
        
        // 统一字体和尺寸设置
        button.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 16));
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(400, 44)); // 增加按钮宽度到400px，统一高度44px
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 创建现代化背景面板
        JPanel backgroundPanel = new JPanel() {
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
                
                // 如果有背景图片，使用图片背景；否则使用渐变背景
                if (backgroundImage != null) {
                    // 绘制背景图片，缩放以适应窗口大小
                    g2d.drawImage(backgroundImage, 0, 0, width, height, null);
                    
                    // 添加轻磨砂蒙层（白色12-16%透明度），避免与卡片元素"抢对比"
                    g2d.setColor(new Color(255, 255, 255, 40)); // 白色16%透明度磨砂蒙层
                    g2d.fillRect(0, 0, width, height);
                } else {
                    // 使用原来的渐变背景作为后备方案
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
        
        // 创建连接状态提示（右上角）
        connectionToast = new JLabel("服务器连接成功", SwingConstants.CENTER);
        connectionToast.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        connectionToast.setForeground(WHITE);
        connectionToast.setBackground(SUCCESS_GREEN);
        connectionToast.setOpaque(true);
        connectionToast.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_GREEN, 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        connectionToast.setVisible(false);
        
        // 使用LayeredPane来处理Toast叠加
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT));
        
        // 设置主要内容位置
        backgroundPanel.setBounds(0, 0, BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT - 40);
        statusPanel.setBounds(0, BASE_FRAME_HEIGHT - 40, BASE_FRAME_WIDTH, 40);
        
        // 设置Toast位置（右上角）
        connectionToast.setBounds(BASE_FRAME_WIDTH - 200, 16, 180, 40);
        
        // 添加到层级面板
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(statusPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(connectionToast, JLayeredPane.POPUP_LAYER);
        
        // 添加到主窗口
        add(layeredPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建登录卡片 - 使用GridBagLayout重新设计
     */
    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
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
                g2d.fill(new RoundRectangle2D.Float(0, 0, width - 2, height - 2, 24, 24)); // 圆角24px
                
                // 添加微妙的渐变边框
                GradientPaint borderGradient = new GradientPaint(
                    0, 0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 80),
                    width, height, new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue(), 80)
                );
                g2d.setPaint(borderGradient);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, width - 4, height - 4, 24, 24); // 圆角24px
                
                // 添加高光效果
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fill(new RoundRectangle2D.Float(2, 2, width - 6, height / 2 - 10, 22, 22)); // 圆角22px
            }
        };
        
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(480, 640)); // 增加卡片大小，确保内容完整展示
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 32, 32, 32)); // 卡片内边距32px
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0); // 采用8pt网格间距
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        
        // 创建现代化图标
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // 创建渐变圆形背景
                GradientPaint gradient = new GradientPaint(
                    x, y, PRIMARY_COLOR,
                    x + size, y + size, SECONDARY_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fillOval(x, y, size, size);
                
                // 添加高光效果
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillOval(x + 8, y + 8, size / 3, size / 3);
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(80, 80)); // 调整为80px圆形，更大更醒目
        iconContainer.setLayout(new BorderLayout());
        
        // 创建图标样式的学校建筑物图标
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight());
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int iconSize = (int)(size * 0.6); // 图标大小为容器的60%，更好的比例
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2f));
                
                // 绘制精美的学位帽图标（参考提供的代码）
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                // 使用100x100规范坐标系，然后缩放到实际大小
                double pad = size * 0.10; // 减少边距，让图标更大
                double scale = (size - pad * 2) / 100.0; // 将0..100映射到圆内
                AffineTransform oldTransform = g2d.getTransform();
                
                g2d.translate(centerX, centerY);
                g2d.scale(scale, scale);
                g2d.translate(-50, -50); // 让(0,0)落在左上角
                
                // 学位帽上方面片（菱形）- 调整位置确保居中
                GeneralPath hatTop = new GeneralPath();
                hatTop.moveTo(50, 25);
                hatTop.lineTo(80, 40);
                hatTop.lineTo(50, 55);
                hatTop.lineTo(20, 40);
                hatTop.closePath();
                
                // 帽沿（矩形略带透视）
                GeneralPath brim = new GeneralPath();
                brim.moveTo(28, 50);
                brim.lineTo(72, 50);
                brim.lineTo(68, 60);
                brim.lineTo(32, 60);
                brim.closePath();
                
                // 流苏绳
                GeneralPath tassel = new GeneralPath();
                tassel.moveTo(68, 55);
                tassel.quadTo(75, 63, 72, 73); // 弯曲下垂
                
                // 流苏小球
                Ellipse2D.Float tasselBall = new Ellipse2D.Float(69, 71, 6, 6);
                
                // 设置描边样式 - 白色粗描边
                g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Color.WHITE);
                
                // 仅描边绘制（线条风格）
                g2d.draw(hatTop);
                g2d.draw(brim);
                g2d.draw(tassel);
                g2d.fill(tasselBall);
                
                // 恢复变换
                g2d.setTransform(oldTransform);
            }
        };
        iconPanel.setOpaque(false);
        iconContainer.add(iconPanel, BorderLayout.CENTER);
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 0); // Logo与标题间距12px
        card.add(iconContainer, gbc);
        
        // 标题 - 28px字号，加粗
        JLabel titleLabel = new JLabel("虚拟校园系统", SwingConstants.CENTER);
        titleLabel.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 28)); // 标题28px
        titleLabel.setForeground(DARK_TEXT);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0); // 标题与副标题间距8px
        card.add(titleLabel, gbc);
        
        // 副标题 - 14px字号，灰度#6B7280
        JLabel subtitleLabel = new JLabel("请输入您的学号/教工号和密码登录", SwingConstants.CENTER);
        subtitleLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 14)); // 副标题14px
        subtitleLabel.setForeground(GRAY_TEXT); // 灰度#6B7280
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 32, 0);
        card.add(subtitleLabel, gbc);
        
        // 用户名标签 - 12px正文字号
        JLabel usernameLabel = new JLabel("学号/教工号", SwingConstants.LEFT);
        usernameLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12)); // 正文12px
        usernameLabel.setForeground(DARK_TEXT);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(usernameLabel, gbc);
        
        // 用户名输入框
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 4, 0); // 减少间距给错误提示留空间
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(loginIdField, gbc);
        
        // 用户名错误提示
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 16, 0);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(loginIdErrorLabel, gbc);
        
        // 密码标签 - 12px正文字号
        JLabel passwordLabel = new JLabel("密码", SwingConstants.LEFT);
        passwordLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12)); // 正文12px
        passwordLabel.setForeground(DARK_TEXT);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(passwordLabel, gbc);
        
        // 密码输入框容器（包含眼睛图标）
        JPanel passwordFieldContainer = new JPanel();
        passwordFieldContainer.setLayout(new OverlayLayout(passwordFieldContainer));
        passwordFieldContainer.setOpaque(false);
        passwordFieldContainer.setPreferredSize(new Dimension(400, 44));
        
        passwordFieldContainer.add(passwordField);
        
        JPanel eyePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        eyePanel.setOpaque(false);
        eyePanel.setBorder(new EmptyBorder(10, 0, 10, 15));
        eyePanel.add(eyeIconLabel);
        passwordFieldContainer.add(eyePanel);
        
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 4, 0); // 减少间距给错误提示留空间
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(passwordFieldContainer, gbc);
        
        // 密码错误提示
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 32, 0); // 增加底部间距
        gbc.anchor = GridBagConstraints.WEST;
        card.add(passwordErrorLabel, gbc);
        
        // 登录按钮 - 略微下移，不要太贴近密码输入框
        gbc.gridy = 9;
        gbc.insets = new Insets(8, 0, 20, 0); // 增加顶部间距8px
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(loginButton, gbc);
        
        // 注册链接
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(registerLabel, gbc);
        
        // 服务条款
        JLabel termsLabel = new JLabel("登录即表示您同意我们的服务条款和隐私政策", SwingConstants.CENTER);
        termsLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        termsLabel.setForeground(new Color(158, 158, 158));
        
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(termsLabel, gbc);
        
        return card;
    }
    
    /**
     * 创建状态栏
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusPanel.setPreferredSize(new Dimension(BASE_FRAME_WIDTH, 40));
        
        JPanel statusLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLeftPanel.setOpaque(false);
        statusLeftPanel.add(statusLabel);
        
        JPanel statusRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusRightPanel.setOpaque(false);
        // 默认隐藏连接按钮，只在失败时显示
        connectButton.setVisible(false);
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
                    registerLabel.setForeground(PRIMARY_COLOR);
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
        
        // 键盘支持：回车键登录，Esc键关闭
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled()) {
                    performLogin();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Esc键关闭窗口
                    System.exit(0);
                }
            }
        };
        
        loginIdField.addKeyListener(keyListener);
        passwordField.addKeyListener(keyListener);
        
        // 为整个窗口添加键盘监听
        addKeyListener(keyListener);
        setFocusable(true);
    }
    
    /**
     * 切换密码可见性
     */
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setEchoChar('●');
            eyeIconLabel.setText("显示");
            passwordVisible = false;
        } else {
            passwordField.setEchoChar((char) 0);
            eyeIconLabel.setText("隐藏");
            passwordVisible = true;
        }
    }
    
    /**
     * 连接服务器
     */
    private void connectToServer() {
        connectButton.setEnabled(false);
        statusLabel.setText("正在连接服务器...");
        statusLabel.setForeground(WARNING_ORANGE);

        // 在后台线程中进行阻塞连接，避免阻塞EDT
        new Thread(() -> {
            boolean connected = serverConnection.connect();

            SwingUtilities.invokeLater(() -> {
                if (connected) {
                    // 成功时，显示右上角轻提示2-3秒后消失
                    showConnectionToast(true);
                    statusLabel.setText("就绪");
                    statusLabel.setForeground(DARK_TEXT);
                    loginButton.setEnabled(true);
                    registerLabel.setEnabled(true);
                    connectButton.setVisible(false); // 隐藏连接按钮
                } else {
                    // 失败时，显示橙色重新连接按钮
                    statusLabel.setText("服务器连接失败");
                    statusLabel.setForeground(ERROR_RED);
                    loginButton.setEnabled(false);
                    registerLabel.setEnabled(false);
                    connectButton.setVisible(true); // 显示重新连接按钮
                    connectButton.setText("重新连接");
                }

                connectButton.setEnabled(true);
            });
        }, "ServerConnectThread").start();
    }
    
    /**
     * 执行登录
     */
    private void performLogin() {
        String loginId = loginIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 清除之前的错误提示
        hideErrorMessages();
        
        // 检查是否是占位符文本
        if (loginId.isEmpty() || loginId.equals("学号/教工号")) {
            showFieldError(loginIdErrorLabel, "请输入学号或教工号");
            loginIdField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "请输入密码");
            passwordField.requestFocus();
            return;
        }
        
        // 禁用按钮防止重复点击，切换为加载态
        setLoadingState(true);
        statusLabel.setText("正在登录...");
        statusLabel.setForeground(WARNING_ORANGE);
        
        // 执行登录
        userController.login(loginId, password, new UserController.LoginCallback() {
            @Override
            public void onSuccess(common.vo.UserVO user) {
                SwingUtilities.invokeLater(() -> {
                    // 登录成功，打开对应的 Dashboard
                    openDashboard(user);
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    // 恢复按钮状态
                    setLoadingState(false);
                    
                    // 启动摇摆动画
                    startShakeAnimation();
                    
                    // 显示错误提示在密码框下方
                    showFieldError(passwordErrorLabel, errorMessage);
                    
                    statusLabel.setText("服务器连接成功");
                    statusLabel.setForeground(SUCCESS_GREEN);
                    passwordField.setText("");
                    passwordField.requestFocus();
                });
            }
        });
    }
    
    /**
     * 打开注册页面
     */
    private void openRegisterDialog() {
        // 隐藏登录页面
        setVisible(false);
        
        // 打开注册页面
        RegisterFrame registerFrame = new RegisterFrame(userController);
        registerFrame.setVisible(true);
        
        // 关闭登录页面
        dispose();
    }
    
    /**
     * 打开对应的 Dashboard
     */
    private void openDashboard(common.vo.UserVO user) {
        // 隐藏登录界面
        setVisible(false);

        // 根据角色打开对应 Dashboard 主界面
        if (user != null && user.isTeacher()) {
            new client.ui.dashboard.TeacherDashboardUI(user, serverConnection).setVisible(true);
        } else if (user != null && user.isAdmin()) {
            new client.ui.dashboard.AdminDashboardUI(user, serverConnection).setVisible(true);
        } else if (user != null && user.isStudent()) {
            new client.ui.dashboard.StudentDashboardUI(user, serverConnection).setVisible(true);
        } else {
            // 未知用户角色，显示错误信息
            JOptionPane.showMessageDialog(
                this,
                "未知的用户角色，请联系管理员",
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
            // 重新显示登录界面
            setVisible(true);
            return;
        }

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
     * 设置加载状态
     */
    private void setLoadingState(boolean loading) {
        isLoading = loading;
        if (loading) {
            originalButtonText = loginButton.getText();
            loginButton.setText("正在登录...");
            loginButton.setEnabled(false);
            registerLabel.setEnabled(false);
            connectButton.setEnabled(false);
        } else {
            loginButton.setText(originalButtonText);
            loginButton.setEnabled(true);
            registerLabel.setEnabled(true);
            connectButton.setEnabled(true);
        }
    }
    
    /**
     * 显示字段错误提示
     */
    private void showFieldError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        // 添加淡入动画效果
        Timer fadeInTimer = new Timer(20, new ActionListener() {
            private float alpha = 0.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.1f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                // 设置透明度效果
                Color color = ERROR_RED;
                errorLabel.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha)));
            }
        });
        fadeInTimer.start();
    }
    
    /**
     * 隐藏错误提示
     */
    private void hideErrorMessages() {
        loginIdErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
    }
    
    /**
     * 显示连接状态Toast提示
     */
    private void showConnectionToast(boolean success) {
        if (success) {
            connectionToast.setText("服务器连接成功");
            connectionToast.setBackground(SUCCESS_GREEN);
            connectionToast.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_GREEN, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
        } else {
            connectionToast.setText("连接失败");
            connectionToast.setBackground(ERROR_RED);
            connectionToast.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR_RED, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
        }
        
        connectionToast.setVisible(true);
        
        // 2-3秒后自动消失
        Timer hideTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionToast.setVisible(false);
                ((Timer) e.getSource()).stop();
            }
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
    
    /**
     * 启动淡入动画
     */
    private void startFadeInAnimation() {
        // 简化处理：移除透明度动画，避免兼容性问题
        // 直接显示窗口，保持良好的用户体验
    }
    
    /**
     * 卡片摇摆动画（登录失败时）
     */
    private void startShakeAnimation() {
        final int originalX = loginCardPanel.getX();
        final int shakeDistance = 10;
        final int shakeDuration = 500;
        final int shakeCount = 6;
        
        Timer shakeTimer = new Timer(shakeDuration / (shakeCount * 2), new ActionListener() {
            private int count = 0;
            private boolean right = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count >= shakeCount * 2) {
                    loginCardPanel.setLocation(originalX, loginCardPanel.getY());
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                int offset = right ? shakeDistance : -shakeDistance;
                loginCardPanel.setLocation(originalX + offset, loginCardPanel.getY());
                right = !right;
                count++;
            }
        });
        shakeTimer.start();
    }
    
    /**
     * 文本框焦点动画
     */
    private void animateFieldFocus(JTextField field, boolean focused, String placeholder) {
        Timer animTimer = new Timer(10, new ActionListener() {
            private int step = 0;
            private final int totalSteps = 15;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                float progress = (float) step / totalSteps;
                
                if (focused) {
                    // 获得焦点时的动画 - 主色边框#37A165，1px边框 + 微弱外发光
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new FocusedBorder(FOCUS_COLOR, 1, 12), // 聚焦态专用边框
                        BorderFactory.createEmptyBorder(12, 16, 12, 16) // 统一内边距
                    ));
                    
                    // 背景颜色过渡
                    Color startBg = new Color(248, 250, 252);
                    Color endBg = WHITE;
                    int r = (int) (startBg.getRed() + (endBg.getRed() - startBg.getRed()) * progress);
                    int g = (int) (startBg.getGreen() + (endBg.getGreen() - startBg.getGreen()) * progress);
                    int b = (int) (startBg.getBlue() + (endBg.getBlue() - startBg.getBlue()) * progress);
                    field.setBackground(new Color(r, g, b));
                    
                    if (step == 1 && field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(DARK_TEXT);
                    }
                } else {
                    // 失去焦点时的动画
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_COLOR, 1, 12),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16) // 统一内边距
                    ));
                    
                    // 背景颜色过渡
                    Color startBg = WHITE;
                    Color endBg = new Color(248, 250, 252);
                    int r = (int) (startBg.getRed() + (endBg.getRed() - startBg.getRed()) * progress);
                    int g = (int) (startBg.getGreen() + (endBg.getGreen() - startBg.getGreen()) * progress);
                    int b = (int) (startBg.getBlue() + (endBg.getBlue() - startBg.getBlue()) * progress);
                    field.setBackground(new Color(r, g, b));
                    
                    if (step == totalSteps && field.getText().isEmpty()) {
                        field.setForeground(GRAY_TEXT);
                        field.setText(placeholder);
                    }
                }
                
                if (step >= totalSteps) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        animTimer.start();
    }
    
    /**
     * 密码框焦点动画
     */
    private void animatePasswordFieldFocus(JPasswordField field, boolean focused) {
        Timer animTimer = new Timer(10, new ActionListener() {
            private int step = 0;
            private final int totalSteps = 15;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                float progress = (float) step / totalSteps;
                
                if (focused) {
                    // 获得焦点时的动画 - 主色边框#37A165，1px边框 + 微弱外发光
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new FocusedBorder(FOCUS_COLOR, 1, 12), // 聚焦态专用边框
                        BorderFactory.createEmptyBorder(12, 16, 12, 48) // 统一内边距，右侧留空间给眼睛图标
                    ));
                    
                    // 背景颜色过渡
                    Color startBg = new Color(248, 250, 252);
                    Color endBg = WHITE;
                    int r = (int) (startBg.getRed() + (endBg.getRed() - startBg.getRed()) * progress);
                    int g = (int) (startBg.getGreen() + (endBg.getGreen() - startBg.getGreen()) * progress);
                    int b = (int) (startBg.getBlue() + (endBg.getBlue() - startBg.getBlue()) * progress);
                    field.setBackground(new Color(r, g, b));
                } else {
                    // 失去焦点时的动画
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_COLOR, 1, 12),
                        BorderFactory.createEmptyBorder(12, 16, 12, 48) // 统一内边距，右侧留空间给眼睛图标
                    ));
                    
                    // 背景颜色过渡
                    Color startBg = WHITE;
                    Color endBg = new Color(248, 250, 252);
                    int r = (int) (startBg.getRed() + (endBg.getRed() - startBg.getRed()) * progress);
                    int g = (int) (startBg.getGreen() + (endBg.getGreen() - startBg.getGreen()) * progress);
                    int b = (int) (startBg.getBlue() + (endBg.getBlue() - startBg.getBlue()) * progress);
                    field.setBackground(new Color(r, g, b));
                }
                
                if (step >= totalSteps) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        animTimer.start();
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
        
        // 设置全局默认字体 - 统一字体设置
        FontUtil.setGlobalFont();
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
    
    /**
     * 聚焦态边框 - 带外发光效果
     */
    private static class FocusedBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;
        
        public FocusedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制外发光效果
            for (int i = 3; i >= 0; i--) {
                int alpha = 20 - i * 5; // 渐变透明度
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                g2d.setStroke(new BasicStroke(thickness + i));
                g2d.drawRoundRect(x + thickness / 2 - i, y + thickness / 2 - i, 
                                 width - thickness + i * 2, height - thickness + i * 2, 
                                 radius + i, radius + i);
            }
            
            // 绘制主边框
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x + thickness / 2, y + thickness / 2, 
                             width - thickness, height - thickness, radius, radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 3, thickness + 3, thickness + 3, thickness + 3);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness + 3;
            return insets;
        }
    }
    
    /**
     * 自定义圆角边框
     */
    private static class RoundedBorder extends AbstractBorder {
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
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness;
            return insets;
        }
    }
}
