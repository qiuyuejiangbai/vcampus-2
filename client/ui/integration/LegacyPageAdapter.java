package client.ui.integration;

import client.ui.api.IModuleView;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 适配已有/他人页面为 IModuleView，不改对方代码。
 */
public class LegacyPageAdapter implements IModuleView {
    private final String key;
    private final String displayName;
    private final String iconPath;
    private final JComponent component;

    public LegacyPageAdapter(String key, String displayName, String iconPath, JComponent component) {
        this.key = key;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.component = component != null ? component : new JPanel();
    }

    @Override
    public String getKey() { return key; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getIconPath() { return iconPath; }

    @Override
    public JComponent getComponent() { return component; }

    @Override
    public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) {
        // 仅存引用时可扩展：本演示适配器无需上下文
    }
}


