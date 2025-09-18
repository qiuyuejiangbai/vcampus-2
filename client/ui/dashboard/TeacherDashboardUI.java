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
    private final common.vo.UserVO currentUser;
    private final client.net.ServerConnection connection;

    private final ContentHost contentHost = new ContentHost();
    private final SideNav sideNav = new SideNav();
    private final AppBar appBar = new AppBar();
    private AppTitleBar titleBar;


    public TeacherDashboardUI(common.vo.UserVO user, client.net.ServerConnection conn) {
        this.currentUser = user;
        this.connection = conn;

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
                contentHost.showPage(key);
                client.ui.api.IModuleView m = ModuleRegistry.findByKey(key);
                if (m != null) appBar.setModuleName(m.getDisplayName());
            }
        });

        // 设置当前用户信息到导航栏（教师：显示姓名 + 职称/院系占位）
        sideNav.setCurrentUser(currentUser);
    }
    
    /**
     * 刷新用户信息显示
     * 当用户信息更新后（如头像上传），调用此方法刷新显示
     */
    public void refreshUserInfo() {
        sideNav.refreshAvatar();
    }

    private void initModules() {
        // 注册教师个人信息模块（学籍管理）- 优先注册，确保在第一项
        ModuleRegistry.register(new client.ui.modules.TeacherProfileModule());

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
                System.out.println("[TeacherDashboardUI] 已注册头像更新监听器: " + m.getDisplayName());
            }
        }
        // 默认显示个人信息模块（学籍管理）
        String defaultModuleKey = ModuleKeys.TEACHER_PROFILE;
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
                // 清理模块注册表，确保下次登录时不会显示之前的模块
                client.ui.integration.ModuleRegistry.clearAll();
                
                // 关闭当前窗口
                dispose();
                
                // 显示登录窗口
                SwingUtilities.invokeLater(() -> {
                    client.ui.LoginFrame loginFrame = new client.ui.LoginFrame();
                    loginFrame.setVisible(true);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "登出时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}


