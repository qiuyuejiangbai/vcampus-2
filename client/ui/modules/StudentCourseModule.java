package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.course.CourseTablePanel;
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
        root.setOpaque(true);  //设置主面板为不透明
        root.setBackground(UIManager.getColor("Panel.background"));

        root.add(TabbedPane(), BorderLayout.CENTER);//添加选项卡

        root.setVisible(true);
    }


    //创建选项卡
    private JTabbedPane TabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 课程管理选项卡
        JPanel courseManagementPanel = createCourseManagementPanel();
        tabbedPane.addTab("课程管理", courseManagementPanel);

        // 选课记录选项卡
        JPanel enrollmentManagementPanel = createEnrollmentManagementPanel();
        tabbedPane.addTab("选课记录", enrollmentManagementPanel);

        // 成绩查询选项卡
        JPanel scoreQueryPanel = createScoreQueryPanel();
        tabbedPane.addTab("成绩查询", scoreQueryPanel);

        return tabbedPane;
    }


    //创建成绩查询选项卡
    private JPanel createScoreQueryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("成绩查询功能正在开发中...", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }


    //创建选课记录选项卡
    private JPanel createEnrollmentManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("选课记录功能正在开发中...", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }


    //创建课程管理选项卡
    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建课程表格面板
        CourseTablePanel courseTablePanel = new CourseTablePanel();
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton refreshButton = new JButton("刷新");
        
        // 添加搜索和刷新按钮的事件监听器
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                courseTablePanel.searchByCourseName(searchText);
            } else {
                courseTablePanel.refreshData();
            }
        });
        
        refreshButton.addActionListener(e -> courseTablePanel.refreshData());
        
        // 添加组件到搜索面板
        searchPanel.add(new JLabel("搜索课程:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        // 添加状态标签
        JLabel statusLabel = new JLabel("课程总数: 0");
        searchPanel.add(statusLabel);
        
        // 更新状态标签
        SwingUtilities.invokeLater(() -> {
            int courseCount = courseTablePanel.getCourseTable().getRowCount();
            statusLabel.setText("课程总数: " + courseCount);
        });
        
        // 设置布局
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(courseTablePanel, BorderLayout.CENTER);
        
        return panel;
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