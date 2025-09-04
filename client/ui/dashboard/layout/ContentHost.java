package client.ui.dashboard.layout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 中心卡槽容器：封装 CardLayout，提供按 key 切换页面。
 */
public class ContentHost extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final Map<String, JComponent> keyToComponent = new HashMap<String, JComponent>();

    public ContentHost() {
        setLayout(cardLayout);
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
    }
    
    public void addPage(String key, JComponent component) {
        if (key == null || component == null) return;
        keyToComponent.put(key, component);
        add(component, key);
    }

    public void showPage(String key) {
        if (!keyToComponent.containsKey(key)) return;
        cardLayout.show(this, key);
        revalidate();
        repaint();
    }
}


