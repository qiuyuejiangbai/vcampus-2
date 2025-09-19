package client.ui.dashboard;

import client.ui.api.IModuleView;
import client.ui.dashboard.layout.AppBar;
import client.ui.dashboard.layout.ContentHost;
import client.ui.dashboard.layout.SideNav;
import client.ui.dashboard.titlebar.AppTitleBar;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import client.ui.modules.Library.LibraryModule;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/** 教师 Dashboard 主容器，复用学生端结构与特效，仅内容与标题不同。 */
public class TeacherDashboardUI extends JFrame {
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;
    private final client.controller.TeacherController teacherController;

    private ContentHost contentHost = new ContentHost();
    private SideNav sideNav;
    private AppBar appBar = new AppBar();
    private AppTitleBar titleBar;


    public TeacherDashboardUI(common.vo.UserVO user, client.net.ServerConnection conn) {
        this.currentUser = user;
        this.connection = conn;
        this.teacherController = new client.controller.TeacherController();
        this.sideNav = new SideNav(this.teacherController);

        initLaf();
        initFrameChrome();
        initStructure();
        initModules();
    }

    private void initLaf() {
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatLightLaf.setup();
    }

    private void initFrameChrome() {
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1440, 900);
        setLocationRelativeTo(null);

        titleBar = new AppTitleBar(this, new AppTitleBar.WindowControl() {
            @Override public void minimize() { setState(Frame.ICONIFIED); }
            @Override public void toggleMaximize() {
                setExtendedState((getExtendedState() & Frame.MAXIMIZED_BOTH) == 0 ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);
            }
            @Override public void close() { dispose(); }
            @Override public void logout() { logoutImpl(); }
            @Override public void changePassword() { 
                new client.ui.dialog.ChangePasswordDialog(TeacherDashboardUI.this, connection, currentUser.getUserId()).setVisible(true);
            }
        });
        // 教师端标题
        try { titleBar.setTitleText("vCampus-教师端"); } catch (Throwable ignored) {}

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xF5, 0xF6, 0xF8));
        root.add(titleBar, BorderLayout.NORTH);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(true);
        north.setBackground(new Color(0xF5, 0xF6, 0xF8));
        north.add(appBar, BorderLayout.CENTER);

        JPanel west = new JPanel(new BorderLayout());
        west.setOpaque(true);
        west.setBackground(new Color(0xF5, 0xF6, 0xF8));
        west.add(sideNav, BorderLayout.CENTER);
        sideNav.setExpanded(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                int skip = Math.max(0, appBar.getHeight() - appBar.getShadowHeight());
                sideNav.setTopShadowSkipPx(skip);
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int skip = Math.max(0, appBar.getHeight() - appBar.getShadowHeight());
                sideNav.setTopShadowSkipPx(skip);
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(new Color(0xF5, 0xF6, 0xF8));
        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contentWrap.add(contentHost, BorderLayout.CENTER);

        center.add(north, BorderLayout.NORTH);
        center.add(contentWrap, BorderLayout.CENTER);

        root.add(west, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void initStructure() {
        sideNav.setNavListener(new SideNav.NavListener() {
            @Override public void onNavSelected(String key) {
                // 使用淡入淡出动画切换页面
                contentHost.showPageAnimated(key, client.ui.dashboard.layout.ContentHost.TransitionType.FADE);
                client.ui.api.IModuleView m = ModuleRegistry.findByKey(key);
                if (m != null) appBar.setModuleName(m.getDisplayName());
                
                // 页面切换时刷新头像显示，确保从服务器获取最新头像
                refreshAvatarsOnPageSwitch(key);
            }
        });

        // 设置当前用户信息到导航栏（教师：显示姓名 + 职称/院系占位）
        System.out.println("[DEBUG][TeacherDashboardUI] ========== 准备设置用户信息到侧边栏 ==========");
        System.out.println("[DEBUG][TeacherDashboardUI] currentUser=" + (currentUser != null ? "非null" : "null"));
        if (currentUser != null) {
            System.out.println("[DEBUG][TeacherDashboardUI] 用户详情：userId=" + currentUser.getUserId() + 
                ", loginId=" + currentUser.getLoginId() + ", role=" + currentUser.getRole() + 
                ", name=" + currentUser.getName() + ", isTeacher=" + currentUser.isTeacher());
        }
        System.out.println("[DEBUG][TeacherDashboardUI] 调用sideNav.setCurrentUser");
        sideNav.setCurrentUser(currentUser);
        System.out.println("[DEBUG][TeacherDashboardUI] setCurrentUser调用完成");
    }
    
    /**
     * 刷新用户信息显示
     * 当用户信息更新后（如头像上传），调用此方法刷新显示
     */
    public void refreshUserInfo() {
        sideNav.refreshAvatar();
    }
    
    /**
     * 页面切换时刷新头像显示 - 强制从服务器获取最新头像
     * @param pageKey 页面键值
     */
    private void refreshAvatarsOnPageSwitch(String pageKey) {
        
        // 延迟执行，确保页面切换完成
        SwingUtilities.invokeLater(() -> {
            try {
                // 强制刷新侧边栏头像
                sideNav.refreshAvatar();
                
                // 根据页面类型刷新相应的头像
                if (ModuleKeys.TEACHER_FORUM.equals(pageKey)) {
                    // 论坛页面：强制刷新论坛中的头像显示
                    refreshForumAvatars();
                }
                
            } catch (Exception e) {
                System.err.println("[TeacherDashboardUI] 刷新头像失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 刷新论坛页面中的头像
     */
    private void refreshForumAvatars() {
        try {
            // 获取论坛模块并刷新头像
            client.ui.api.IModuleView forumModule = ModuleRegistry.findByKey(ModuleKeys.TEACHER_FORUM);
            if (forumModule instanceof client.ui.modules.TeacherForumModule) {
                ((client.ui.modules.TeacherForumModule) forumModule).refreshAllAvatars();
            }
        } catch (Exception e) {
            System.err.println("[TeacherDashboardUI] 刷新论坛头像失败: " + e.getMessage());
        }
    }
    

    private void initModules() {
        ModuleRegistry.register(new client.ui.modules.TeacherForumModule());

        // 课程管理
        client.ui.modules.TeacherCourseModule.registerTo(ModuleRegistry.class);

        // 注册图书馆模块
        ModuleRegistry.register(
                new LibraryModule(
                        ModuleKeys.TEACHER_LIBRARY, "图书馆", "resources/icons/LibraryIcon.png"
                )
        );

        // 注册商店模块
        ModuleRegistry.register(
                new client.ui.modules.StoreModule(
                        ModuleKeys.TEACHER_STORE, "校园商店", "resources/icons/店铺.png"
                )
        );
        
        for (IModuleView m : ModuleRegistry.getAll()) {
            m.initContext(currentUser, connection);
            contentHost.addPage(m.getKey(), m.getComponent());
            Icon icon = loadIcon(m.getIconPath());
            sideNav.addItem(m.getKey(), m.getDisplayName(), icon);
            
            // 如果模块实现了AvatarUpdateListener接口，注册为头像更新监听器
            if (m instanceof client.ui.dashboard.layout.SideNav.AvatarUpdateListener) {
                sideNav.addAvatarUpdateListener((client.ui.dashboard.layout.SideNav.AvatarUpdateListener) m);
            }
        }
        // 默认显示论坛模块
        String defaultModuleKey = ModuleKeys.TEACHER_FORUM;
        contentHost.showPage(defaultModuleKey);
        sideNav.selectKey(defaultModuleKey);
        IModuleView home = ModuleRegistry.findByKey(defaultModuleKey);
        if (home != null) appBar.setModuleName(home.getDisplayName());
    }

    private Icon loadIcon(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        final String normalized = path.replace('\\', '/');
        try {
            String cp1 = normalized;
            String cp2 = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized;
            String cp3 = normalized.startsWith("resources/") ? normalized : ("resources/" + normalized);
            String cp4 = cp3.startsWith("/") ? cp3.substring(1) : "/" + cp3;

            String[] candidates = new String[] { cp1, cp2, cp3, cp4 };

            ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
            ClassLoader cl2 = getClass().getClassLoader();
            for (String p : candidates) {
                if (p == null || p.trim().isEmpty()) continue;
                try {
                    java.net.URL url = (cl1 != null ? cl1.getResource(p) : null);
                    if (url == null && cl2 != null) url = cl2.getResource(p);
                    if (url != null) return new ImageIcon(url);
                } catch (Exception ignored) { }
            }

            for (String p : new String[] { normalized, cp3 }) {
                try {
                    java.io.File file = new java.io.File(p);
                    if (file.exists()) return new ImageIcon(file.getAbsolutePath());
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.err.println("加载图标失败: " + normalized + " - " + e.getMessage());
        }
        System.err.println("未找到图标资源: " + normalized + " （请确认位于 classpath 或 resources 目录）");
        return null;
    }

    private void logoutImpl() {
        try {
            // 使用主题化确认对话框
            int result = client.ui.dialog.ThemedConfirmDialog.showConfirmDialog(
                this,
                "确认登出",
                "确定要登出吗？"
            );
            
            if (result == JOptionPane.YES_OPTION) {
                System.out.println("[TeacherDashboardUI] 开始执行登出清理流程...");
                
                // 1. 清理ContentHost中的所有页面组件
                if (contentHost != null) {
                    contentHost.clearAllPages();
                    System.out.println("[TeacherDashboardUI] ContentHost页面组件已清理");
                }
                
                // 2. 清理模块注册表，确保下次登录时不会显示之前的模块
                client.ui.integration.ModuleRegistry.clearAll();
                System.out.println("[TeacherDashboardUI] 模块注册表已清理");
                
                // 3. 清理用户信息
                this.currentUser = null;
                System.out.println("[TeacherDashboardUI] 用户信息已清理");
                
                // 4. 清理连接
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                    System.out.println("[TeacherDashboardUI] 服务器连接已断开");
                }
                
                // 5. 清理UI组件引用
                this.sideNav = null;
                this.appBar = null;
                this.contentHost = null;
                System.out.println("[TeacherDashboardUI] UI组件引用已清理");
                
                // 6. 关闭当前窗口
                dispose();
                System.out.println("[TeacherDashboardUI] 主窗口已关闭");
                
                // 7. 显示登录窗口
                SwingUtilities.invokeLater(() -> {
                    client.ui.LoginFrame loginFrame = new client.ui.LoginFrame();
                    loginFrame.setVisible(true);
                    System.out.println("[TeacherDashboardUI] 登录窗口已显示");
                });
                
                System.out.println("[TeacherDashboardUI] 登出清理流程完成");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "登出时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}


