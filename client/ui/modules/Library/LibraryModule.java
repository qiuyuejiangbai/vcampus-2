package client.ui.modules.Library;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.net.ServerConnection;
import client.ui.integration.ModuleRegistry;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;

/**
 * 图书馆模块包装器，实现 IModuleView 接口
 * 可在 Student/Teacher/Admin Dashboard 中复用
 */
public class LibraryModule implements IModuleView {
    private final String key;
    private final String displayName;
    private final String iconPath;

    private JPanel root;
    private UserVO currentUser;
    private ServerConnection connection;
    private LibraryMainFrameModule frame;

    /**
     * 构造函数（可以在不同 Dashboard 使用不同的 key/name/icon）
     */
    public LibraryModule(String key, String displayName, String iconPath) {
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

        // ⚡ 用现有的 LibraryMainFrameModule 构建实际 UI
        frame = new LibraryMainFrameModule(user);
        frame.setVisible(false); // 不要作为单独窗口显示

        // ⚡ 把 JFrame 的内容面板嵌入 Dashboard
        root = new JPanel(new BorderLayout());
        root.add(frame.getContentPane(), BorderLayout.CENTER);
    }

    /**
     * 生命周期结束时释放资源
     */
    public void dispose() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        if (frame != null) {
            frame.dispose(); // ✅ 自动释放 LibraryController
            frame = null;
        }
    }

    /**
     * 提供一个快捷注册方法
     */
    public static void registerToStudent() {
        ModuleRegistry.register(
                new LibraryModule(ModuleKeys.STUDENT_LIBRARY, "图书馆", "icons/library.png")
        );
    }

    public static void registerToTeacher() {
        ModuleRegistry.register(
                new LibraryModule(ModuleKeys.TEACHER_LIBRARY, "图书馆", "icons/library.png")
        );
    }

    public static void registerToAdmin() {
        ModuleRegistry.register(
                new LibraryModule(ModuleKeys.ADMIN_LIBRARY, "图书馆", "icons/library.png")
        );
    }
}
