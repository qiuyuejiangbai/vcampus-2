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

    private boolean useGrayTheme = false; // false=墨绿主题, true=灰色主题
    

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
            @Override public void toggleTheme() { toggleThemeImpl(); }
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
                contentHost.showPage(key);
                client.ui.api.IModuleView m = ModuleRegistry.findByKey(key);
                if (m != null) appBar.setModuleName(m.getDisplayName());
            }
        });
        
        // 设置当前用户信息到导航栏
        sideNav.setCurrentUser(currentUser);
    }

    private void initModules() {
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
                        ModuleKeys.STUDENT_STORE, "校园商店", null//"icons/module/store.png"
                 )
         );

        for (IModuleView m : ModuleRegistry.getAll()) {
            m.initContext(currentUser, connection);
            contentHost.addPage(m.getKey(), m.getComponent());
            Icon icon = loadIcon(m.getIconPath());
            sideNav.addItem(m.getKey(), m.getDisplayName(), icon);
        }
        // 默认显示论坛模块
        String defaultModuleKey = "student_forum";
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

    private void toggleThemeImpl() {
        try {
            useGrayTheme = !useGrayTheme;
            if (sideNav != null) {
                if (useGrayTheme) sideNav.applyGrayTheme();
                else sideNav.applyGreenTheme();
            }
            if (titleBar != null) {
                if (useGrayTheme) {
                    titleBar.setBarBackground(new Color(0x33, 0x33, 0x33)); // 深灰
                } else {
                    titleBar.resetBarBackground(); // 恢复墨绿
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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


