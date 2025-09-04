package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;

import javax.swing.*;
import java.awt.*;

/** 学生版论坛模块。 */
public class StudentForumModule implements IModuleView {
    private JPanel root;

    public StudentForumModule() { buildUI(); }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel placeholder = new JLabel("论坛-学生端（开发中）", SwingConstants.CENTER);
        placeholder.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 20));
        root.add(placeholder, BorderLayout.CENTER);
    }

    @Override public String getKey() { return ModuleKeys.STUDENT_FORUM; }
    @Override public String getDisplayName() { return "论坛"; }
    @Override public String getIconPath() { return "icons/论坛.png"; }
    @Override public JComponent getComponent() { return root; }
    @Override public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) { }

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new StudentForumModule()); }
}


