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

/** 学生 Dashboard 主容器，包含标题栏、AppBar、SideNav、ContentHost。 */
public class StudentDashboardUI extends JFrame {
    private final common.vo.UserVO currentUser;
    private final client.net.ServerConnection connection;

    private final ContentHost contentHost = new ContentHost();
    private final SideNav sideNav = new SideNav();
    private final AppBar appBar = new AppBar();
    private AppTitleBar titleBar;

    

    public StudentDashboardUI(common.vo.UserVO user, client.net.ServerConnection conn) {
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
                new client.ui.dialog.ChangePasswordDialog(StudentDashboardUI.this, connection, currentUser.getUserId()).setVisible(true);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(255, 255, 255));
        root.add(titleBar, BorderLayout.NORTH);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(true);
        north.setBackground(new Color(255, 255, 255));
        north.add(appBar, BorderLayout.CENTER);

        JPanel west = new JPanel(new BorderLayout());
        west.setOpaque(true);
        west.setBackground(new Color(255, 255, 255));
        west.add(sideNav, BorderLayout.CENTER);
        sideNav.setExpanded(true);

        // 让 SideNav 的右侧阴影在 AppBar 可视高度（减去其底部阴影） 内不绘制，使长度与 AppBar 非阴影区一致
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                int skip = Math.max(0, appBar.getHeight() - appBar.getShadowHeight());
                sideNav.setTopShadowSkipPx(skip);
            }
        });

        // 窗口尺寸变化时同步更新跳过高度，避免白色区域视觉变长
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int skip = Math.max(0, appBar.getHeight() - appBar.getShadowHeight());
                sideNav.setTopShadowSkipPx(skip);
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(new Color(255, 255, 255));
        // 内容与 AppBar/SideNav 保持间距：上 8px、左 8px、右 8px、下 8px
        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
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
        
        // 设置当前用户信息到导航栏
        sideNav.setCurrentUser(currentUser);
    }
    
    /**
     * 刷新用户信息显示
     * 当用户信息更新后（如头像上传），调用此方法刷新显示
     */
    public void refreshUserInfo() {
        sideNav.refreshAvatar();
    }
    
    /**
     * 页面切换时刷新头像显示
     * @param pageKey 页面键值
     */
    private void refreshAvatarsOnPageSwitch(String pageKey) {
        
        // 延迟执行，确保页面切换完成
        SwingUtilities.invokeLater(() -> {
            try {
                // 刷新侧边栏头像
                sideNav.refreshAvatar();
                
                // 根据页面类型刷新相应的头像
                if (ModuleKeys.STUDENT_FORUM.equals(pageKey)) {
                    // 论坛页面：刷新论坛中的头像显示
                    refreshForumAvatars();
                } else if (ModuleKeys.STUDENT_PROFILE.equals(pageKey)) {
                    // 个人信息页面：刷新个人信息中的头像显示
                    refreshProfileAvatars();
                }
                
            } catch (Exception e) {
                System.err.println("[StudentDashboardUI] 刷新头像失败: " + e.getMessage());
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
            client.ui.api.IModuleView forumModule = ModuleRegistry.findByKey(ModuleKeys.STUDENT_FORUM);
            if (forumModule instanceof client.ui.modules.StudentForumModule) {
                ((client.ui.modules.StudentForumModule) forumModule).refreshAllAvatars();
            }
        } catch (Exception e) {
            System.err.println("[StudentDashboardUI] 刷新论坛头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新个人信息页面中的头像
     */
    private void refreshProfileAvatars() {
        try {
            // 获取个人信息模块并刷新头像
            client.ui.api.IModuleView profileModule = ModuleRegistry.findByKey(ModuleKeys.STUDENT_PROFILE);
            if (profileModule instanceof client.ui.modules.StudentProfileModule) {
                ((client.ui.modules.StudentProfileModule) profileModule).refreshAvatarDisplay();
            }
        } catch (Exception e) {
            System.err.println("[StudentDashboardUI] 刷新个人信息头像失败: " + e.getMessage());
        }
    }

    private void initModules() {
        // 注册学籍管理模块 - 优先注册，确保在第一项
        ModuleRegistry.register(new client.ui.modules.StudentProfileModule());

        // 注册学生版论坛
        ModuleRegistry.register(new client.ui.modules.StudentForumModule());

        // 课程管理
        client.ui.modules.StudentCourseModule.registerTo(ModuleRegistry.class);

        // 图书馆
        ModuleRegistry.register(
                new LibraryModule(
                        ModuleKeys.STUDENT_LIBRARY, "图书馆", "resources/icons/LibraryIcon.png"
                )
        );

        // 注册商店模块
        ModuleRegistry.register(
                new client.ui.modules.StoreModule(
                        ModuleKeys.STUDENT_STORE, "校园商店", "resources/icons/店铺.png"
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
        // 默认显示学籍管理模块
        String defaultModuleKey = ModuleKeys.STUDENT_PROFILE;
        contentHost.showPage(defaultModuleKey);
        sideNav.selectKey(defaultModuleKey);
        // 初始化 AppBar 显示当前模块名
        IModuleView home = ModuleRegistry.findByKey(defaultModuleKey);
        if (home != null) appBar.setModuleName(home.getDisplayName());
    }

    private Icon loadIcon(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        final String normalized = path.replace('\\', '/');
        try {
            // 候选路径（类路径变体 + 资源目录前缀 + 绝对/相对文件路径）
            String cp1 = normalized;
            String cp2 = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized; // 处理带/或不带/两种写法
            String cp3 = normalized.startsWith("resources/") ? normalized : ("resources/" + normalized);
            String cp4 = cp3.startsWith("/") ? cp3.substring(1) : "/" + cp3;

            String[] candidates = new String[] { cp1, cp2, cp3, cp4 };

            // 1) 从当前线程的 ClassLoader 与类的 ClassLoader 依次尝试
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

            // 2) 文件系统（相对/绝对路径）
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

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                // 创建模拟用户数据
                common.vo.UserVO mock = new common.vo.UserVO();
                mock.setName("李明远");
                mock.setRole(0); // 学生角色
                
                client.net.ServerConnection conn = client.net.ServerConnection.getInstance();
                new StudentDashboardUI(mock, conn).setVisible(true);
            }
        });
    }
}


