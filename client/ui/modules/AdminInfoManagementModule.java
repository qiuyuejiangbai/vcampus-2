package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.course.UITheme;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * 管理员信息管理模块
 * 包含学生信息和教师信息的管理功能
 */
public class AdminInfoManagementModule implements IModuleView {
    private JPanel root;
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;

    public AdminInfoManagementModule() { 
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UITheme.WHITE);
        root.setBorder(null);

        root.add(createTabbedPane(), BorderLayout.CENTER);
        root.setVisible(true);
    }

    /**
     * 创建选项卡面板
     */
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 设置选项卡样式
        tabbedPane.setFont(UITheme.SUBTITLE_FONT); // 使用稍大的字体
        tabbedPane.setBackground(UITheme.WHITE);
        tabbedPane.setForeground(UITheme.DARK_GRAY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, 0, UITheme.PADDING_MEDIUM));
        
        // 设置选项卡位置和布局
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        // 自定义选项卡渲染器
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    // 选中状态：渐变绿色背景
                    GradientPaint gradient = new GradientPaint(
                        x, y, UITheme.PRIMARY_GREEN,
                        x, y + h, UITheme.HOVER_GREEN
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, UITheme.RADIUS_LARGE, UITheme.RADIUS_LARGE);
                } else {
                    // 未选中状态：浅色背景
                    g2d.setColor(UITheme.VERY_LIGHT_GREEN);
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, UITheme.RADIUS_LARGE, UITheme.RADIUS_LARGE);
                }
                
                g2d.dispose();
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(UITheme.PRIMARY_GREEN);
                    g2d.setStroke(new BasicStroke(2.0f));
                } else {
                    g2d.setColor(UITheme.LIGHT_GREEN);
                    g2d.setStroke(new BasicStroke(1.0f));
                }
                g2d.drawRoundRect(x + 2, y + 2, w - 4, h - 4, UITheme.RADIUS_LARGE, UITheme.RADIUS_LARGE);
                
                g2d.dispose();
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(UITheme.WHITE); // 选中时白色字体
                    g2d.setFont(font.deriveFont(Font.BOLD)); // 加粗
                } else {
                    g2d.setColor(UITheme.MEDIUM_GRAY); // 未选中时中灰色字体
                    g2d.setFont(font);
                }
                
                FontMetrics fm = g2d.getFontMetrics();
                int x = textRect.x + (textRect.width - fm.stringWidth(title)) / 2;
                int y = textRect.y + fm.getAscent() + (textRect.height - fm.getHeight()) / 2;
                g2d.drawString(title, x, y);
                
                g2d.dispose();
            }
            
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return Math.max(fontHeight + UITheme.PADDING_LARGE, 48); // 增加选项卡高度
            }
            
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(super.calculateTabWidth(tabPlacement, tabIndex, metrics) + UITheme.PADDING_XLARGE, 120); // 增加选项卡宽度
            }
        });

        // 学生信息管理选项卡
        JPanel studentManagementPanel = createStudentManagementPanel();
        tabbedPane.addTab("学生信息管理", studentManagementPanel);

        // 教师信息管理选项卡
        JPanel teacherManagementPanel = createTeacherManagementPanel();
        tabbedPane.addTab("教师信息管理", teacherManagementPanel);

        return tabbedPane;
    }

    /**
     * 创建学生信息管理面板
     */
    private JPanel createStudentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(null);
        
        // 创建学生信息表格面板
        StudentManagementPanel studentTablePanel = new StudentManagementPanel(currentUser, connection);
        
        // 创建搜索面板
        JPanel searchPanel = createStudentSearchPanel(studentTablePanel);
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(studentTablePanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * 创建教师信息管理面板
     */
    private JPanel createTeacherManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(null);
        
        // 创建教师信息表格面板
        TeacherManagementPanel teacherTablePanel = new TeacherManagementPanel(currentUser, connection);
        
        // 创建搜索面板
        JPanel searchPanel = createTeacherSearchPanel(teacherTablePanel);
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(teacherTablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建学生信息搜索面板
     */
    private JPanel createStudentSearchPanel(StudentManagementPanel studentTablePanel) {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM, UITheme.PADDING_LARGE),
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.LIGHT_GRAY)
        ));
        
        // 左侧搜索区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM));
        leftPanel.setBackground(UITheme.WHITE);
        
        // 搜索标题
        JLabel searchTitle = new JLabel("学生信息搜索");
        searchTitle.setFont(UITheme.SUBTITLE_FONT);
        searchTitle.setForeground(UITheme.DARK_GRAY);
        searchTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UITheme.PADDING_LARGE));
        
        // 搜索输入框
        JPanel searchFieldPanel = createStyledSearchField(25, "输入学号、姓名、专业或班级进行搜索");
        searchFieldPanel.setPreferredSize(new Dimension(350, UITheme.INPUT_HEIGHT));
        
        // 获取内部的JTextField
        JTextField searchField = (JTextField) searchFieldPanel.getClientProperty("textField");
        
        // 搜索按钮
        JButton searchButton = new JButton("搜索");
        styleActionButton(searchButton);
        searchButton.setPreferredSize(new Dimension(90, UITheme.BUTTON_HEIGHT));
        
        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(90, UITheme.BUTTON_HEIGHT));
        
        // 添加事件监听器
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                studentTablePanel.searchStudents(searchText);
            } else {
                studentTablePanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> {
            studentTablePanel.refreshData();
            searchField.setText("");
        });
        
        // 添加到左侧面板
        leftPanel.add(searchTitle);
        leftPanel.add(searchFieldPanel);
        leftPanel.add(searchButton);
        leftPanel.add(refreshButton);
        
        // 右侧状态区域
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        rightPanel.setBackground(UITheme.WHITE);
        
        // 状态标签容器
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusContainer.setBackground(UITheme.VERY_LIGHT_GREEN);
        statusContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.LIGHT_GREEN, 1),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
        ));
        
        // 状态标签
        JLabel statusLabel = new JLabel("学生总数: 0");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.DARK_GRAY);
        
        // 将状态标签存储到学生管理面板，以便数据加载完成后更新
        studentTablePanel.setStatusLabel(statusLabel);
        
        statusContainer.add(statusLabel);
        rightPanel.add(statusContainer);
        
        // 添加到搜索面板
        searchPanel.add(leftPanel, BorderLayout.WEST);
        searchPanel.add(rightPanel, BorderLayout.EAST);
        
        return searchPanel;
    }
    
    /**
     * 创建教师信息搜索面板
     */
    private JPanel createTeacherSearchPanel(TeacherManagementPanel teacherTablePanel) {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM, UITheme.PADDING_LARGE),
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.LIGHT_GRAY)
        ));
        
        // 左侧搜索区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_LARGE, UITheme.PADDING_MEDIUM));
        leftPanel.setBackground(UITheme.WHITE);
        
        // 搜索标题
        JLabel searchTitle = new JLabel("教师信息搜索");
        searchTitle.setFont(UITheme.SUBTITLE_FONT);
        searchTitle.setForeground(UITheme.DARK_GRAY);
        searchTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UITheme.PADDING_LARGE));
        
        // 搜索输入框
        JPanel searchFieldPanel = createStyledSearchField(25, "输入工号、姓名、院系或职称进行搜索");
        searchFieldPanel.setPreferredSize(new Dimension(350, UITheme.INPUT_HEIGHT));
        
        // 获取内部的JTextField
        JTextField searchField = (JTextField) searchFieldPanel.getClientProperty("textField");
        
        // 搜索按钮
        JButton searchButton = new JButton("搜索");
        styleActionButton(searchButton);
        searchButton.setPreferredSize(new Dimension(90, UITheme.BUTTON_HEIGHT));
        
        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(90, UITheme.BUTTON_HEIGHT));
        
        // 添加事件监听器
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                teacherTablePanel.searchTeachers(searchText);
            } else {
                teacherTablePanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> {
            teacherTablePanel.refreshData();
            searchField.setText("");
        });
        
        // 添加到左侧面板
        leftPanel.add(searchTitle);
        leftPanel.add(searchFieldPanel);
        leftPanel.add(searchButton);
        leftPanel.add(refreshButton);
        
        // 右侧状态区域
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        rightPanel.setBackground(UITheme.WHITE);
        
        // 状态标签容器
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusContainer.setBackground(UITheme.VERY_LIGHT_GREEN);
        statusContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.LIGHT_GREEN, 1),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
        ));
        
        // 状态标签
        JLabel statusLabel = new JLabel("教师总数: 0");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.DARK_GRAY);
        
        // 将状态标签存储到教师管理面板，以便数据加载完成后更新
        teacherTablePanel.setStatusLabel(statusLabel);
        
        statusContainer.add(statusLabel);
        rightPanel.add(statusContainer);
        
        // 添加到搜索面板
        searchPanel.add(leftPanel, BorderLayout.WEST);
        searchPanel.add(rightPanel, BorderLayout.EAST);
        
        return searchPanel;
    }

    private void setupLayout() {
    }

    private void setupEventHandlers() {
    }

    /**
     * 注册到模块注册表
     */
    @Override 
    public String getKey() { 
        return ModuleKeys.ADMIN_INFO_MANAGEMENT; 
    }
    
    @Override 
    public String getDisplayName() { 
        return "信息管理"; 
    }
    
    @Override 
    public String getIconPath() { 
        return "resources/icons/学籍.png";
    }
    
    @Override 
    public JComponent getComponent() { 
        return root; 
    }
    
    @Override 
    public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        
        // 更新子面板的用户信息
        updateSubPanelsUser();
        
        // 延迟加载数据，确保连接已建立
        SwingUtilities.invokeLater(() -> {
            loadDataAfterConnectionEstablished();
        });
    }
    
    /**
     * 在连接建立后加载数据
     */
    private void loadDataAfterConnectionEstablished() {
        // 获取选项卡面板
        JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
        
        // 加载学生数据
        if (tabbedPane.getTabCount() > 0) {
            JPanel studentPanel = (JPanel) tabbedPane.getComponentAt(0);
            StudentManagementPanel studentTablePanel = findStudentManagementPanel(studentPanel);
            if (studentTablePanel != null && connection != null && connection.isConnected()) {
                studentTablePanel.loadStudentData();
            }
        }
        
        // 加载教师数据
        if (tabbedPane.getTabCount() > 1) {
            JPanel teacherPanel = (JPanel) tabbedPane.getComponentAt(1);
            TeacherManagementPanel teacherTablePanel = findTeacherManagementPanel(teacherPanel);
            if (teacherTablePanel != null && connection != null && connection.isConnected()) {
                teacherTablePanel.loadTeacherData();
            }
        }
    }
    
    /**
     * 更新子面板的用户信息
     */
    private void updateSubPanelsUser() {
        // 获取选项卡面板
        JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
        
        // 更新学生管理面板
        if (tabbedPane.getTabCount() > 0) {
            JPanel studentPanel = (JPanel) tabbedPane.getComponentAt(0);
            StudentManagementPanel studentTablePanel = findStudentManagementPanel(studentPanel);
            if (studentTablePanel != null) {
                studentTablePanel.setCurrentUser(currentUser);
                studentTablePanel.setConnection(connection);
            }
        }
        
        // 更新教师管理面板
        if (tabbedPane.getTabCount() > 1) {
            JPanel teacherPanel = (JPanel) tabbedPane.getComponentAt(1);
            TeacherManagementPanel teacherTablePanel = findTeacherManagementPanel(teacherPanel);
            if (teacherTablePanel != null) {
                teacherTablePanel.setCurrentUser(currentUser);
                teacherTablePanel.setConnection(connection);
            }
        }
    }
    
    /**
     * 在面板中查找学生管理面板
     */
    private StudentManagementPanel findStudentManagementPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof StudentManagementPanel) {
                return (StudentManagementPanel) component;
            } else if (component instanceof Container) {
                StudentManagementPanel result = findStudentManagementPanel((Container) component);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * 在面板中查找教师管理面板
     */
    private TeacherManagementPanel findTeacherManagementPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof TeacherManagementPanel) {
                return (TeacherManagementPanel) component;
            } else if (component instanceof Container) {
                TeacherManagementPanel result = findTeacherManagementPanel((Container) component);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static void registerTo(Class<?> ignored) { 
        ModuleRegistry.register(new AdminInfoManagementModule()); 
    }

    public UserVO getCurrentUser() {
        return currentUser;
    }

    public ServerConnection getConnection() {
        return connection;
    }
    
    /**
     * 创建带样式的搜索框
     */
    private JPanel createStyledSearchField(int columns, String placeholder) {
        // 创建主面板
        JPanel searchPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(UITheme.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                
                g2d.dispose();
            }
        };
        searchPanel.setBackground(UITheme.WHITE);
        searchPanel.setOpaque(false);
        
        // 创建搜索输入框
        JTextField searchField = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(UITheme.MEDIUM_GRAY);
                    g2d.setFont(getFont());
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(placeholder, x, y);
                    g2d.dispose();
                }
            }
        };
        searchField.setFont(UITheme.CONTENT_FONT);
        searchField.setBackground(UITheme.WHITE);
        searchField.setForeground(UITheme.DARK_GRAY);
        searchField.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
        searchField.setOpaque(false);
        
        // 创建圆角边框
        Border defaultBorder = new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.BORDER_COLOR);
                g2d.drawRoundRect(x, y, width - 1, height - 1, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                g2d.dispose();
            }
            
            @Override
            public java.awt.Insets getBorderInsets(Component c) {
                return new java.awt.Insets(1, 1, 1, 1);
            }
        };
        
        Border hoverBorder = new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.LIGHT_GREEN);
                g2d.drawRoundRect(x, y, width - 1, height - 1, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                g2d.dispose();
            }
            
            @Override
            public java.awt.Insets getBorderInsets(Component c) {
                return new java.awt.Insets(2, 2, 2, 2);
            }
        };
        
        Border focusBorder = new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.PRIMARY_GREEN);
                g2d.drawRoundRect(x, y, width - 1, height - 1, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                g2d.dispose();
            }
            
            @Override
            public java.awt.Insets getBorderInsets(Component c) {
                return new java.awt.Insets(2, 2, 2, 2);
            }
        };
        
        // 设置默认边框
        searchPanel.setBorder(defaultBorder);
        
        // 添加搜索图标
        try {
            ImageIcon searchIcon = new ImageIcon("resources/icons/搜索.png");
            Image scaledIcon = searchIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, UITheme.PADDING_SMALL, 0, 0));
            searchPanel.add(iconLabel, BorderLayout.WEST);
        } catch (Exception e) {
            // 如果图标加载失败，继续使用默认样式
        }
        
        // 添加输入框到面板
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // 将JTextField存储为客户端属性，方便外部访问
        searchPanel.putClientProperty("textField", searchField);
        
        // 创建统一的鼠标监听器
        java.awt.event.MouseAdapter hoverAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                searchPanel.setBorder(hoverBorder);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!searchField.hasFocus()) {
                    searchPanel.setBorder(defaultBorder);
                }
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                searchField.requestFocus();
            }
        };
        
        // 添加鼠标悬浮效果到搜索面板
        searchPanel.addMouseListener(hoverAdapter);
        
        // 添加鼠标悬浮效果到搜索图标
        Component[] components = searchPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                component.addMouseListener(hoverAdapter);
            }
        }
        
        // 添加鼠标悬浮效果到输入框
        searchField.addMouseListener(hoverAdapter);
        
        // 添加焦点效果
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                searchPanel.setBorder(focusBorder);
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                searchPanel.setBorder(defaultBorder);
            }
        });
        
        return searchPanel;
    }
    
    /**
     * 样式化主要操作按钮
     */
    private void styleActionButton(JButton button) {
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
     * 样式化次要操作按钮
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
}
