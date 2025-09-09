package client.ui.dashboard.layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 中心卡槽容器：封装 CardLayout，提供按 key 切换页面。
 */
public class ContentHost extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final Map<String, JComponent> keyToComponent = new HashMap<String, JComponent>();
    private String currentKey = null;

    public enum TransitionType { FADE, SLIDE }

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
        currentKey = key;
        revalidate();
        repaint();
    }

    /**
     * 使用动画切换到目标页面。
     * 若当前无页面或目标等于当前，直接显示。
     */
    public void showPageAnimated(final String targetKey, final TransitionType type) {
        // 关闭所有动画：直接切换
        showPage(targetKey);
    }

    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    private BufferedImage renderToImage(JComponent comp, Dimension size) {
        if (comp == null || size == null) return null;
        try {
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 在 comp 的坐标系原样绘制，不修改其 size/layout，避免引发布局波动
            comp.printAll(g2);
            g2.dispose();
            return img;
        } catch (Throwable t) {
            return null;
        }
    }

    private BufferedImage renderSelfToImage(Dimension size) {
        if (size == null) return null;
        try {
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.printAll(g2);
            g2.dispose();
            return img;
        } catch (Throwable t) {
            return null;
        }
    }
}


