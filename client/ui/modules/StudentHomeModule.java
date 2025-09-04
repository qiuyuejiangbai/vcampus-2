package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;

import javax.swing.*;
import java.awt.*;

/** 学生动态主页模块：与教师区分，后续可加载学生专属内容。 */
public class StudentHomeModule implements IModuleView {
    private JPanel root;

    public StudentHomeModule() { buildUI(); }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UIManager.getColor("Panel.background"));
    }

    @Override public String getKey() { return ModuleKeys.STUDENT_HOME; }
    @Override public String getDisplayName() { return "主页"; }
    @Override public String getIconPath() { return null; }
    @Override public JComponent getComponent() { return root; }
    @Override public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) { }

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new StudentHomeModule()); }
}


