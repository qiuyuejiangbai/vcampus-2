 package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.course.CourseTablePanel;
import client.ui.modules.course.StudentEnrollmentTablePanel;
import client.ui.modules.course.UITheme;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;

/** 学生课程管理模块 */
public class StudentCourseModule implements IModuleView {
    private JPanel root;
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;


    public StudentCourseModule() { 
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
        
        // 创建学生选课记录表格面板
        StudentEnrollmentTablePanel enrollmentTablePanel = new StudentEnrollmentTablePanel();
        
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
    private JPanel createEnrollmentSearchPanel(StudentEnrollmentTablePanel enrollmentTablePanel) {
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
                enrollmentTablePanel.searchByCourseName(searchText);
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
     * 注册模块
     */
    @Override 
    public String getKey() { 
        return ModuleKeys.STUDENT_COURSE; 
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
        
        // 设置选课成功和失败的消息监听器，用于刷新选课记录表格
        setupEnrollmentMessageListeners();
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
    
    /**
     * 设置选课相关的消息监听器
     */
    private void setupEnrollmentMessageListeners() {
        if (connection != null) {
            // 选课成功后刷新选课记录表格和更新按钮状态
            connection.setMessageListener(common.protocol.MessageType.ENROLL_COURSE_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("收到选课成功消息: " + message.getData());
                    // 刷新选课记录表格
                    refreshEnrollmentTable();
                    // 通知所有课程卡片更新按钮状态
                    notifyCourseCardsEnrollmentSuccess((Integer) message.getData());
                });
            });
            
            // 选课失败后显示错误信息
            connection.setMessageListener(common.protocol.MessageType.ENROLL_COURSE_FAIL, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("收到选课失败消息: " + message.getData());
                    String errorMessage = message.getData() != null ? message.getData().toString() : "选课失败";
                    JOptionPane.showMessageDialog(root, "选课失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
            
            // 退选成功后刷新选课记录表格和更新按钮状态
            connection.setMessageListener(common.protocol.MessageType.DROP_COURSE_SUCCESS, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("收到退选成功消息: " + message.getData());
                    // 刷新选课记录表格
                    refreshEnrollmentTable();
                    // 通知所有课程卡片更新按钮状态
                    notifyCourseCardsDropSuccess((Integer) message.getData());
                });
            });
            
            // 退选失败后显示错误信息
            connection.setMessageListener(common.protocol.MessageType.DROP_COURSE_FAIL, message -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("收到退选失败消息: " + message.getData());
                    String errorMessage = message.getData() != null ? message.getData().toString() : "退选失败";
                    JOptionPane.showMessageDialog(root, "退选失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                });
            });
        }
    }
    
    /**
     * 刷新选课记录表格
     */
    private void refreshEnrollmentTable() {
        // 这里需要获取选课记录表格面板并刷新
        // 由于选课记录表格在另一个选项卡中，我们需要通过某种方式访问它
        // 暂时通过重新加载整个模块来实现
        SwingUtilities.invokeLater(() -> {
            // 重新初始化选课记录选项卡
            JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
            if (tabbedPane.getTabCount() > 1) {
                JPanel enrollmentPanel = (JPanel) tabbedPane.getComponentAt(1);
                // 查找选课记录表格面板并刷新
                refreshEnrollmentTableInPanel(enrollmentPanel);
            }
        });
    }
    
    /**
     * 在面板中查找并刷新选课记录表格
     */
    private void refreshEnrollmentTableInPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof StudentEnrollmentTablePanel) {
                ((StudentEnrollmentTablePanel) component).refreshData();
                return;
            } else if (component instanceof Container) {
                refreshEnrollmentTableInPanel((Container) component);
            }
        }
    }
    
    /**
     * 通知所有课程卡片选课成功
     */
    private void notifyCourseCardsEnrollmentSuccess(Integer courseId) {
        if (courseId == null) return;
        
        // 查找课程管理选项卡中的课程表格面板
        JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
        if (tabbedPane.getTabCount() > 0) {
            JPanel coursePanel = (JPanel) tabbedPane.getComponentAt(0);
            notifyCourseCardsInPanel(coursePanel, courseId, true);
        }
    }
    
    /**
     * 通知所有课程卡片退选成功
     */
    private void notifyCourseCardsDropSuccess(Integer courseId) {
        if (courseId == null) return;
        
        // 查找课程管理选项卡中的课程表格面板
        JTabbedPane tabbedPane = (JTabbedPane) root.getComponent(0);
        if (tabbedPane.getTabCount() > 0) {
            JPanel coursePanel = (JPanel) tabbedPane.getComponentAt(0);
            notifyCourseCardsInPanel(coursePanel, courseId, false);
        }
    }
    
    /**
     * 在面板中查找并通知课程卡片
     */
    private void notifyCourseCardsInPanel(Container container, Integer courseId, boolean enrolled) {
        for (Component component : container.getComponents()) {
            if (component instanceof client.ui.modules.course.CourseClassCard) {
                client.ui.modules.course.CourseClassCard card = (client.ui.modules.course.CourseClassCard) component;
                if (card.getCourse().getCourseId().equals(courseId)) {
                    card.updateEnrollmentStatus(enrolled);
                    if (enrolled) {
                        JOptionPane.showMessageDialog(card, "选课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(card, "退选成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else if (component instanceof Container) {
                notifyCourseCardsInPanel((Container) component, courseId, enrolled);
            }
        }
    }

    public static void registerTo(Class<?> ignored) { 
        ModuleRegistry.register(new StudentCourseModule()); 
    }

    public UserVO getCurrentUser() {
        return currentUser;
    }

    public ServerConnection getConnection() {
        return connection;
    }
}