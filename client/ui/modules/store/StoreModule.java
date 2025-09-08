package client.ui.modules.store;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.net.ServerConnection;
import client.ui.integration.ModuleRegistry;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;

/**
 * 商店模块包装器，实现 IModuleView 接口
 * 可在 Student/Teacher/Admin Dashboard 中复用
 */
public class StoreModule implements IModuleView {
    private final String key;
    private final String displayName;
    private final String iconPath;

    private JPanel root;
    private UserVO currentUser;
    private ServerConnection connection;

    /**
     * 构造函数（可以在不同 Dashboard 使用不同的 key/name/icon）
     */
    public StoreModule(String key, String displayName, String iconPath) {
        this.key = key;
        this.displayName = displayName;
        this.iconPath = iconPath;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void initContext(UserVO user, ServerConnection conn) {
        this.currentUser = user;
        this.connection = conn;

        // 创建商店主框架模块
        StoreMainFrameModule frame = new StoreMainFrameModule(user);
        frame.setVisible(false); // 隐藏单独窗口

        // 将 JFrame 的内容面板嵌入 Dashboard
        root = new JPanel(new BorderLayout());
        root.add(frame.getContentPane(), BorderLayout.CENTER);
    }

    /**
     * 提供快捷注册方法 - 学生端
     */
    public static void registerToStudent() {
        ModuleRegistry.register(
            new StoreModule(ModuleKeys.STUDENT_STORE, "商店", "icons/store.png")
        );
    }

    /**
     * 提供快捷注册方法 - 教师端
     */
    public static void registerToTeacher() {
        ModuleRegistry.register(
            new StoreModule(ModuleKeys.TEACHER_STORE, "商店", "icons/store.png")
        );
    }

    /**
     * 提供快捷注册方法 - 管理员端
     */
    public static void registerToAdmin() {
        ModuleRegistry.register(
            new StoreModule(ModuleKeys.ADMIN_STORE, "商店管理", "icons/store_admin.png")
        );
    }

    /**
     * 自定义注册方法（可选）
     */
    public static void registerCustom(String key, String displayName, String iconPath) {
        ModuleRegistry.register(
            new StoreModule(key, displayName, iconPath)
        );
    }

    // 生命周期管理（可选）
    public void refresh() {
        if (root != null) {
            // 触发商店模块内部刷新逻辑
            StoreMainFrameModule frame = (StoreMainFrameModule)((JPanel)getComponent()).getComponent(0);
            frame.refreshAllPanels();
        }
    }

    public void navigateToCategory(String category) {
        if (root != null) {
            StoreMainFrameModule frame = (StoreMainFrameModule)((JPanel)getComponent()).getComponent(0);
            frame.showProductCategory(category);
        }
    }
}