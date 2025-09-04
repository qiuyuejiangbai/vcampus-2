package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;

import javax.swing.*;
import java.awt.*;

/** 管理员资源中心模块：为管理员提供资源管理与发布入口。 */
public class AdminResourceCenterModule implements IModuleView {
    private JPanel root;

    public AdminResourceCenterModule() { buildUI(); }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UIManager.getColor("Panel.background"));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("资源中心（管理员）");
        title.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        title.setFont(new java.awt.Font("Microsoft YaHei UI", Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
    }

    @Override public String getKey() { return ModuleKeys.ADMIN_RESOURCE_CENTER; }
    @Override public String getDisplayName() { return "资源中心"; }
    @Override public String getIconPath() { return "resources/icons/资源中心.png"; }
    @Override public JComponent getComponent() { return root; }
    @Override public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) { }

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new AdminResourceCenterModule()); }
}
