package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;

import javax.swing.*;
import java.awt.*;

/** 管理员论坛模块：为管理员提供论坛管理与浏览入口。 */
public class AdminForumModule implements IModuleView {
    private JPanel root;

    public AdminForumModule() { buildUI(); }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UIManager.getColor("Panel.background"));
        // 预留顶部说明区域，可后续替换为管理员专属内容面板
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("论坛（管理员）");
        title.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        title.setFont(new java.awt.Font("Microsoft YaHei UI", Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
    }

    @Override public String getKey() { return ModuleKeys.ADMIN_FORUM; }
    @Override public String getDisplayName() { return "论坛"; }
    @Override public String getIconPath() { return "resources/icons/论坛.png"; }
    @Override public JComponent getComponent() { return root; }
    @Override public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) { }

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new AdminForumModule()); }
}
