package client.ui;

import client.controller.UserController;
import client.net.ServerConnection;
import client.ui.util.FontUtil;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主界面
 * 根据用户角色显示不同的功能模块
 */
public class MainFrame extends JFrame {
    private static final int FRAME_WIDTH = 1440;
    private static final int FRAME_HEIGHT = 900;
    
    private UserVO currentUser;
    private UserController userController;
    private ServerConnection serverConnection;
    
    // 界面组件
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JPanel modulePanel;
    
    public MainFrame(UserVO user) {
        this.currentUser = user;
        this.userController = new UserController();
        this.serverConnection = ServerConnection.getInstance();
        
        // 最小改动：直接切换到新的 Dashboard 窗口（保持登录流程与业务不变）
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                dispose();
                if (currentUser != null && currentUser.isTeacher()) {
                    new client.ui.dashboard.TeacherDashboardUI(currentUser, serverConnection).setVisible(true);
                } else {
                    new client.ui.dashboard.StudentDashboardUI(currentUser, serverConnection).setVisible(true);
                }
            }
        });
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setTitle("vCampus 虚拟校园系统 - " + currentUser.getRoleName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // 去除标题栏与边框
        setUndecorated(true);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        
        // 创建组件
        welcomeLabel = new JLabel();
        statusLabel = new JLabel("在线", SwingConstants.RIGHT);
        modulePanel = new JPanel();
        
        // 设置样式
        FontUtil.setLabelFont(welcomeLabel, Font.BOLD, 20);
        welcomeLabel.setForeground(new Color(34, 139, 34));  // 墨绿色
        FontUtil.setLabelFont(statusLabel, Font.PLAIN, 14);
        statusLabel.setForeground(new Color(46, 125, 50));   // 深绿色
        
        // 设置欢迎信息
        updateWelcomeLabel();
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 248, 240));  // 淡绿色背景
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(new Color(240, 248, 240));  // 淡绿色背景
        welcomePanel.add(welcomeLabel);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBackground(new Color(240, 248, 240));  // 淡绿色背景
        statusPanel.add(statusLabel);
        
        // 添加登出按钮（顶部右侧）
        JButton logoutButton = new JButton("退出登录");
        FontUtil.setButtonFont(logoutButton, Font.BOLD, 14);
        logoutButton.setBackground(new Color(220, 20, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> performLogout());
        statusPanel.add(logoutButton);
        
        topPanel.add(welcomePanel, BorderLayout.WEST);
        topPanel.add(statusPanel, BorderLayout.EAST);
        
        // 模块面板
        modulePanel.setBackground(Color.WHITE);
        modulePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 底部状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        JLabel infoLabel = new JLabel("vCampus 虚拟校园系统 v1.0.0");
        FontUtil.setLabelFont(infoLabel, Font.PLAIN, 12);
        infoLabel.setForeground(Color.GRAY);
        bottomPanel.add(infoLabel, BorderLayout.WEST);
        
        JLabel serverLabel = new JLabel("服务器: " + serverConnection.getServerHost() + ":" + serverConnection.getServerPort());
        serverLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        serverLabel.setForeground(Color.GRAY);
        bottomPanel.add(serverLabel, BorderLayout.EAST);
        
        // 添加到主窗口
        add(topPanel, BorderLayout.NORTH);
        add(modulePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                performLogout();
            }
        });
    }
    
    /**
     * 加载功能模块
     */
    private void loadModules() {
        modulePanel.setLayout(new GridLayout(0, 2, 24, 24));
        
        if (currentUser.isStudent()) {
            loadStudentModules();
        } else if (currentUser.isTeacher()) {
            loadTeacherModules();
        } else if (currentUser.isAdmin()) {
            loadAdminModules();
        }
        
        modulePanel.revalidate();
        modulePanel.repaint();
    }
    
    /**
     * 加载学生功能模块
     */
    private void loadStudentModules() {
        addModuleButton("个人信息", "查看和修改个人信息", new Color(34, 139, 34), e -> openPersonalInfo());
        addModuleButton("选课系统", "查看课程和选课退课", new Color(46, 125, 50), e -> openCourseSelection());
        addModuleButton("成绩查询", "查看个人成绩单", new Color(76, 175, 80), e -> openGradeQuery());
        addModuleButton("图书馆", "图书检索和借阅管理", new Color(56, 142, 60), e -> openLibrary());
        addModuleButton("校园商店", "商品浏览和购买", new Color(67, 160, 71), e -> openStore());
        addModuleButton("校园论坛", "交流讨论和资源分享", new Color(85, 139, 47), e -> openForum());
    }
    
    /**
     * 加载教师功能模块
     */
    private void loadTeacherModules() {
        addModuleButton("个人信息", "查看和修改个人信息", new Color(34, 139, 34), e -> openPersonalInfo());
        addModuleButton("课程管理", "管理教授课程和学生", new Color(46, 125, 50), e -> openCourseManagement());
        addModuleButton("成绩管理", "录入和修改学生成绩", new Color(76, 175, 80), e -> openGradeManagement());
        addModuleButton("学生查询", "查看学生信息", new Color(56, 142, 60), e -> openStudentQuery());
        addModuleButton("图书馆", "图书检索和借阅管理", new Color(67, 160, 71), e -> openLibrary());
        addModuleButton("校园论坛", "交流讨论和资源分享", new Color(85, 139, 47), e -> openForum());
    }
    
    /**
     * 加载管理员功能模块
     */
    private void loadAdminModules() {
        addModuleButton("用户管理", "管理用户账户和权限", new Color(34, 139, 34), e -> openUserManagement());
        addModuleButton("学籍管理", "管理学生学籍信息", new Color(46, 125, 50), e -> openStudentManagement());
        addModuleButton("课程管理", "管理课程和教学安排", new Color(76, 175, 80), e -> openCourseManagement());
        addModuleButton("图书管理", "管理图书馆藏书", new Color(56, 142, 60), e -> openBookManagement());
        addModuleButton("商店管理", "管理校园商店商品", new Color(67, 160, 71), e -> openStoreManagement());
        addModuleButton("论坛管理", "管理论坛内容", new Color(85, 139, 47), e -> openForumManagement());
        addModuleButton("系统统计", "查看系统使用统计", new Color(102, 187, 106), e -> openSystemStats());
        addModuleButton("系统设置", "系统配置和参数设置", new Color(72, 156, 76), e -> openSystemSettings());
    }
    
    /**
     * 添加模块按钮
     */
    private void addModuleButton(String title, String description, Color color, ActionListener action) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        button.addActionListener(action);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        descLabel.setForeground(Color.WHITE);
        
        button.add(titleLabel, BorderLayout.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);
        
        buttonPanel.add(button, BorderLayout.CENTER);
        modulePanel.add(buttonPanel);
    }
    
    /**
     * 更新欢迎标签
     */
    private void updateWelcomeLabel() {
        String welcome = String.format("欢迎您，%s (%s)", currentUser.getId(), currentUser.getRoleName());
        welcomeLabel.setText(welcome);
    }
    
    /**
     * 执行登出
     */
    private void performLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要退出登录吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            userController.logout(() -> {
                SwingUtilities.invokeLater(() -> {
                    // 关闭当前窗口
                    dispose();
                    
                    // 重新打开登录界面
                    new LoginFrame().setVisible(true);
                });
            });
        }
    }
    
    // 以下是各个模块的打开方法（暂时显示提示信息）
    private void openPersonalInfo() {
        JOptionPane.showMessageDialog(this, "个人信息模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openCourseSelection() {
        JOptionPane.showMessageDialog(this, "选课系统模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openGradeQuery() {
        JOptionPane.showMessageDialog(this, "成绩查询模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openLibrary() {
        JOptionPane.showMessageDialog(this, "图书馆模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openStore() {
        JOptionPane.showMessageDialog(this, "校园商店模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openForum() {
        JOptionPane.showMessageDialog(this, "校园论坛模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openCourseManagement() {
        JOptionPane.showMessageDialog(this, "课程管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openGradeManagement() {
        JOptionPane.showMessageDialog(this, "成绩管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openStudentQuery() {
        JOptionPane.showMessageDialog(this, "学生查询模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openUserManagement() {
        JOptionPane.showMessageDialog(this, "用户管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openStudentManagement() {
        JOptionPane.showMessageDialog(this, "学籍管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openBookManagement() {
        JOptionPane.showMessageDialog(this, "图书管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openStoreManagement() {
        JOptionPane.showMessageDialog(this, "商店管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openForumManagement() {
        JOptionPane.showMessageDialog(this, "论坛管理模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openSystemStats() {
        JOptionPane.showMessageDialog(this, "系统统计模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openSystemSettings() {
        JOptionPane.showMessageDialog(this, "系统设置模块开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}
