package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.course.CourseTablePanel;
import client.ui.modules.course.EnrollmentTablePanel;
import client.ui.modules.course.UITheme;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;

/** 管理员课程管理模块 */
public class AdminCourseModule implements IModuleView {
    private JPanel root;
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;

    public AdminCourseModule() { 
        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UITheme.BACKGROUND_GRAY);
        root.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));

        root.add(createTabbedPane(), BorderLayout.CENTER);

        root.setVisible(true);
    }

    //创建选项卡
    private JTabbedPane createTabbedPane() {
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

        // 选课记录选项卡
        JPanel enrollmentManagementPanel = createEnrollmentManagementPanel();
        tabbedPane.addTab("选课记录", enrollmentManagementPanel);

        return tabbedPane;
    }

    //创建选课记录选项卡
    private JPanel createEnrollmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        // 创建选课记录表格面板
        EnrollmentTablePanel enrollmentTablePanel = new EnrollmentTablePanel();
        
        // 创建搜索面板
        JPanel searchPanel = createEnrollmentSearchPanel(enrollmentTablePanel);
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(enrollmentTablePanel, BorderLayout.CENTER);
        
        return panel;
    }

    //创建课程管理选项卡
    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        // 创建课程表格面板
        CourseTablePanel courseTablePanel = new CourseTablePanel(currentUser);
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel(courseTablePanel);
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(courseTablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建搜索面板
    private JPanel createSearchPanel(CourseTablePanel courseTablePanel) {
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
                courseTablePanel.searchByCourseName(searchText);
            } else {
                courseTablePanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> {
            courseTablePanel.refreshData();
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
            int courseCount = courseTablePanel.getCourseTable().getRowCount();
            statusLabel.setText("课程总数: " + courseCount);
        });
        
        rightPanel.add(statusLabel);
        
        // 添加到搜索面板
        searchPanel.add(leftPanel, BorderLayout.WEST);
        searchPanel.add(rightPanel, BorderLayout.EAST);
        
        return searchPanel;
    }
    
    // 创建选课记录搜索面板
    private JPanel createEnrollmentSearchPanel(EnrollmentTablePanel enrollmentTablePanel) {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.WHITE);
        searchPanel.setBorder(UITheme.createEmptyBorder(0, 0, UITheme.PADDING_LARGE, 0));
        
        // 左侧搜索区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_MEDIUM, 0));
        leftPanel.setBackground(UITheme.WHITE);
        
        // 搜索标签
        JLabel searchLabel = new JLabel("搜索选课记录:");
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
                // 这里可以实现搜索功能，暂时只是刷新数据
                enrollmentTablePanel.refreshData();
            } else {
                enrollmentTablePanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> {
            enrollmentTablePanel.refreshData();
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
        JLabel statusLabel = new JLabel("选课记录总数: 0");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        // 更新状态标签
        SwingUtilities.invokeLater(() -> {
            int enrollmentCount = enrollmentTablePanel.getEnrollmentTable().getRowCount();
            statusLabel.setText("选课记录总数: " + enrollmentCount);
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


    /**
     * 注册到模块注册表
     */
    @Override 
    public String getKey() { 
        return ModuleKeys.ADMIN_COURSE; 
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
        
        // 更新课程表格面板的用户信息
        updateCourseTablePanelUser();
    }
    
    /**
     * 更新课程表格面板的用户信息
     */
    private void updateCourseTablePanelUser() {
        // 获取课程管理选项卡中的课程表格面板
        JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
        if (tabbedPane.getTabCount() > 0) {
            JPanel coursePanel = (JPanel) tabbedPane.getComponentAt(0);
            CourseTablePanel courseTablePanel = findCourseTablePanel(coursePanel);
            if (courseTablePanel != null) {
                courseTablePanel.setCurrentUser(currentUser);
            }
        }
    }
    
    /**
     * 在面板中查找课程表格面板
     */
    private CourseTablePanel findCourseTablePanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof CourseTablePanel) {
                return (CourseTablePanel) component;
            } else if (component instanceof Container) {
                CourseTablePanel result = findCourseTablePanel((Container) component);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static void registerTo(Class<?> ignored) { 
        ModuleRegistry.register(new AdminCourseModule()); 
    }

    public UserVO getCurrentUser() {
        return currentUser;
    }

    public ServerConnection getConnection() {
        return connection;
    }
}
