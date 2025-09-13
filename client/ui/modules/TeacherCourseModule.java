package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.course.UITheme;
import client.ui.modules.course.TeacherCourseCardPanel;
import client.ui.modules.course.StudentListPanel;
import common.vo.UserVO;
import common.vo.CourseVO;

import javax.swing.*;
import java.awt.*;

/** 教师课程管理模块 */
public class TeacherCourseModule implements IModuleView {
    private JPanel root;
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;
    private TeacherCourseCardPanel courseCardPanel;
    private StudentListPanel studentListPanel;
    private CardLayout cardLayout;

    public TeacherCourseModule() { 
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UITheme.BACKGROUND_GRAY);
        root.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));

        // 创建卡片布局容器
        JPanel cardContainer = new JPanel();
        cardLayout = new CardLayout();
        cardContainer.setLayout(cardLayout);
        cardContainer.setBackground(UITheme.BACKGROUND_GRAY);

        // 创建选项卡面板
        JPanel tabbedPanel = createTabbedPane();
        cardContainer.add(tabbedPanel, "tabbedPanel");

        // 创建学生名单面板
        studentListPanel = new StudentListPanel(currentUser, connection);
        cardContainer.add(studentListPanel, "studentListPanel");

        root.add(cardContainer, BorderLayout.CENTER);
        root.setVisible(true);
    }

    //创建选项卡
    private JPanel createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 设置选项卡样式
        tabbedPane.setFont(UITheme.SUBTITLE_FONT);
        tabbedPane.setBackground(UITheme.WHITE);
        tabbedPane.setForeground(UITheme.DARK_GRAY);
        tabbedPane.setBorder(UITheme.createEmptyBorder(0, 0, 0, 0));
        
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
                    // 选中状态：墨绿背景
                    g2d.setColor(UITheme.PRIMARY_GREEN);
                    g2d.fillRoundRect(x, y, w, h, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                } else {
                    // 未选中状态：白色背景
                    g2d.setColor(UITheme.WHITE);
                    g2d.fillRoundRect(x, y, w, h, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                }
                
                g2d.dispose();
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(UITheme.PRIMARY_GREEN);
                } else {
                    g2d.setColor(UITheme.BORDER_COLOR);
                }
                g2d.drawRoundRect(x, y, w - 1, h - 1, UITheme.RADIUS_MEDIUM, UITheme.RADIUS_MEDIUM);
                
                g2d.dispose();
            }
        });

        // 课程管理选项卡
        JPanel courseManagementPanel = createCourseManagementPanel();
        tabbedPane.addTab("课程管理", courseManagementPanel);

        // 成绩管理选项卡
        JPanel gradeManagementPanel = createGradeManagementPanel();
        tabbedPane.addTab("成绩管理", gradeManagementPanel);

        // 将选项卡面板包装在JPanel中
        JPanel tabbedPanel = new JPanel(new BorderLayout());
        tabbedPanel.add(tabbedPane, BorderLayout.CENTER);
        tabbedPanel.setBackground(UITheme.BACKGROUND_GRAY);
        
        return tabbedPanel;
    }

    //创建课程管理选项卡
    private JPanel createCourseManagementPanel() {
        System.out.println("=== 创建课程管理选项卡 ===");
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();
        
        // 创建课程卡片面板（如果已初始化）
        JPanel courseContentPanel;
        if (courseCardPanel != null) {
            System.out.println("使用已创建的课程卡片面板");
            courseContentPanel = courseCardPanel;
        } else {
            System.out.println("创建占位面板，等待课程卡片面板初始化");
            // 创建占位面板
            courseContentPanel = new JPanel(new BorderLayout());
            courseContentPanel.setBackground(UITheme.WHITE);
            JLabel placeholderLabel = new JLabel("正在加载课程数据...", JLabel.CENTER);
            placeholderLabel.setFont(UITheme.DEFAULT_FONT);
            placeholderLabel.setForeground(UITheme.LIGHT_GRAY);
            courseContentPanel.add(placeholderLabel, BorderLayout.CENTER);
        }
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(courseContentPanel, BorderLayout.CENTER);
        
        System.out.println("课程管理选项卡创建完成");
        return panel;
    }

    //创建成绩管理选项卡
    private JPanel createGradeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        // 占位内容
        JLabel placeholderLabel = new JLabel("成绩管理功能开发中...", JLabel.CENTER);
        placeholderLabel.setFont(UITheme.DEFAULT_FONT);
        placeholderLabel.setForeground(UITheme.LIGHT_GRAY);
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建搜索面板
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.WHITE);
        searchPanel.setBorder(UITheme.createEmptyBorder(0, 0, UITheme.PADDING_LARGE, 0));
        
        // 左侧搜索区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_MEDIUM, 0));
        leftPanel.setBackground(UITheme.WHITE);
        
        // 搜索标签
        JLabel searchLabel = new JLabel("搜索课程:");
        searchLabel.setFont(UITheme.CONTENT_FONT);
        searchLabel.setForeground(UITheme.DARK_GRAY);
        
        // 搜索输入框
        JTextField searchField = new JTextField(25);
        UITheme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(300, UITheme.INPUT_HEIGHT));
        
        // 搜索按钮
        JButton searchButton = new JButton("搜索");
        UITheme.styleButton(searchButton);
        searchButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
        
        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        UITheme.styleButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
        
        // 添加事件监听器
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                courseCardPanel.searchByCourseName(searchText);
            } else {
                courseCardPanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> {
            courseCardPanel.refreshData();
            searchField.setText("");
        });
        
        // 添加到左侧面板
        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(searchButton);
        leftPanel.add(refreshButton);
        
        // 右侧状态区域
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.PADDING_MEDIUM, 0));
        rightPanel.setBackground(UITheme.WHITE);
        
        // 状态标签
        JLabel statusLabel = new JLabel("课程总数: 0");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        // 更新状态标签
        SwingUtilities.invokeLater(() -> {
            if (courseCardPanel != null) {
                int courseCount = courseCardPanel.getCourseCount();
                statusLabel.setText("课程总数: " + courseCount);
            } else {
                statusLabel.setText("课程总数: 0");
            }
        });
        
        rightPanel.add(statusLabel);
        
        // 添加到搜索面板
        searchPanel.add(leftPanel, BorderLayout.WEST);
        searchPanel.add(rightPanel, BorderLayout.EAST);
        
        return searchPanel;
    }

    private void setupLayout() {
    }

    private void setupEventHandlers() {
    }

    @Override 
    public String getKey() { 
        return ModuleKeys.TEACHER_COURSE; 
    }
    
    @Override 
    public String getDisplayName() { 
        return "课程管理"; 
    }
    
    @Override 
    public String getIconPath() { 
        return null;
    }
    
    @Override 
    public JComponent getComponent() { 
        return root; 
    }
    
    @Override 
    public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
        
        System.out.println("=== 初始化教师课程模块 ===");
        System.out.println("用户: " + (currentUser != null ? currentUser.getLoginId() : "null"));
        System.out.println("用户ID: " + (currentUser != null ? currentUser.getUserId() : "null"));
        System.out.println("连接状态: " + (connection != null ? "已连接" : "未连接"));
        
        // 创建课程卡片面板（现在用户信息已经可用）
        System.out.println("创建课程卡片面板...");
        courseCardPanel = new TeacherCourseCardPanel(currentUser, connection);
        System.out.println("课程卡片面板创建完成");
        
        // 重新创建整个界面
        System.out.println("重新创建UI...");
        recreateUI();
        
        // 延迟更新课程管理面板，确保UI已经完全创建
        SwingUtilities.invokeLater(() -> {
            updateCourseManagementPanelDirectly();
        });
        
        System.out.println("=== 教师课程模块初始化完成 ===");
    }
    
    /**
     * 重新创建整个UI
     */
    private void recreateUI() {
        System.out.println("重新创建教师课程模块UI");
        if (root != null) {
            root.removeAll();
            
            // 创建卡片布局容器
            JPanel cardContainer = new JPanel();
            cardLayout = new CardLayout();
            cardContainer.setLayout(cardLayout);
            cardContainer.setBackground(UITheme.BACKGROUND_GRAY);

            // 创建选项卡面板
            JPanel tabbedPanel = createTabbedPane();
            cardContainer.add(tabbedPanel, "tabbedPanel");

            // 创建学生名单面板
            studentListPanel = new StudentListPanel(currentUser, connection);
            cardContainer.add(studentListPanel, "studentListPanel");

            root.add(cardContainer, BorderLayout.CENTER);
            root.revalidate();
            root.repaint();
            
            System.out.println("UI重新创建完成");
        }
    }
    
    /**
     * 直接更新课程管理面板，确保课程卡片面板正确显示
     */
    private void updateCourseManagementPanelDirectly() {
        System.out.println("=== 直接更新课程管理面板 ===");
        if (courseCardPanel == null) {
            System.err.println("课程卡片面板为空，无法更新");
            return;
        }
        
        try {
            // 查找课程管理选项卡
            Container cardContainer = (Container) root.getComponent(0);
            JPanel tabbedPanel = (JPanel) cardContainer.getComponent(0);
            Component[] components = tabbedPanel.getComponents();
            
            for (Component comp : components) {
                if (comp instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) comp;
                    JPanel courseManagementPanel = (JPanel) tabbedPane.getComponentAt(0);
                    
                    System.out.println("找到课程管理面板，正在更新...");
                    
                    // 移除所有组件
                    courseManagementPanel.removeAll();
                    
                    // 重新设置布局
                    courseManagementPanel.setLayout(new BorderLayout());
                    courseManagementPanel.setBackground(UITheme.WHITE);
                    courseManagementPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
                    
                    // 创建搜索面板
                    JPanel searchPanel = createSearchPanel();
                    
                    // 添加组件
                    courseManagementPanel.add(searchPanel, BorderLayout.NORTH);
                    courseManagementPanel.add(courseCardPanel, BorderLayout.CENTER);
                    
                    // 刷新界面
                    courseManagementPanel.revalidate();
                    courseManagementPanel.repaint();
                    
                    System.out.println("课程管理面板更新完成");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("更新课程管理面板时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== 课程管理面板更新完成 ===");
    }

    /**
     * 显示学生名单界面
     */
    public void showStudentList(CourseVO course) {
        if (studentListPanel != null && course != null) {
            studentListPanel.setCourseInfo(course.getCourseCode(), course.getCourseName());
            studentListPanel.loadStudentData(course.getCourseCode());
            cardLayout.show((Container) root.getComponent(0), "studentListPanel");
        }
    }

    /**
     * 显示课程管理界面
     */
    public void showCoursePanel() {
        cardLayout.show((Container) root.getComponent(0), "tabbedPanel");
    }

    public static void registerTo(Class<?> ignored) { 
        ModuleRegistry.register(new TeacherCourseModule()); 
    }

    public UserVO getCurrentUser() {
        return currentUser;
    }

    public ServerConnection getConnection() {
        return connection;
    }
}
