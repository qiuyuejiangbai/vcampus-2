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

/** 管理员 Dashboard 主容器，复用结构与特效，仅内容与标题不同。 */
public class AdminDashboardUI extends JFrame {
    private final common.vo.UserVO currentUser;
    private final client.net.ServerConnection connection;

    private final ContentHost contentHost = new ContentHost();
    private final SideNav sideNav = new SideNav();
    private final AppBar appBar = new AppBar();
    private AppTitleBar titleBar;

    private boolean useGrayTheme = false; // false=墨绿主题, true=灰色主题

    public AdminDashboardUI(common.vo.UserVO user, client.net.ServerConnection conn) {
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
        try { titleBar.setTitleText("vCampus-管理员端"); } catch (Throwable ignored) {}

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

        // 管理员：显示姓名 + “管理员”
        sideNav.setCurrentUser(currentUser);
    }

    private void initModules() {
        // 管理员论坛
        client.ui.modules.AdminForumModule.registerTo(ModuleRegistry.class);

        // 课程管理
        client.ui.modules.AdminCourseModule.registerTo(ModuleRegistry.class);

        // 注册管理员图书馆模块
        ModuleRegistry.register(
                new LibraryModule(
                        ModuleKeys.ADMIN_LIBRARY, "图书馆", "resources/icons/LibraryIcon.png"
                )
        );

        for (IModuleView m : ModuleRegistry.getAll()) {
            m.initContext(currentUser, connection);
            contentHost.addPage(m.getKey(), m.getComponent());
            Icon icon = loadIcon(m.getIconPath());
            sideNav.addItem(m.getKey(), m.getDisplayName(), icon);
        }
        // 默认显示论坛模块
        String defaultModuleKey = "admin_forum";
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

    private void toggleThemeImpl() {
        try {
            useGrayTheme = !useGrayTheme;
            if (sideNav != null) {
                if (useGrayTheme) sideNav.applyGrayTheme();
                else sideNav.applyGreenTheme();
            }
            if (titleBar != null) {
                if (useGrayTheme) {
                    titleBar.setBarBackground(new Color(0x33, 0x33, 0x33));
                } else {
                    titleBar.resetBarBackground();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
