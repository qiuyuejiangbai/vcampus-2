package client.ui.dashboard.layout;

import javax.swing.*;
import java.awt.*;

/**
 * 一条用于制造“卡片阴影感”的边缘渐变条。
 * 常用在侧边栏右侧或顶部栏底部，营造层次区分。
 */
public class ShadowEdge extends JComponent {
    public enum Orientation { RIGHT, BOTTOM }

    private final Orientation orientation;
    private final int shadowSize;
    private final Color shadowColor;

    public ShadowEdge(Orientation orientation) {
        this(orientation, 14, new Color(0, 0, 0, 50));
    }

    public ShadowEdge(Orientation orientation, int shadowSize, Color shadowColor) {
        this.orientation = orientation;
        this.shadowSize = shadowSize;
        this.shadowColor = shadowColor;
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        if (orientation == Orientation.RIGHT) {
            return new Dimension(shadowSize, 10);
        } else {
            return new Dimension(10, shadowSize);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        if (orientation == Orientation.RIGHT) {
            // 水平从左到右由透明过渡到阴影色，模拟从侧边栏投射到右侧的阴影
            GradientPaint gp = new GradientPaint(0, 0, new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 0),
                                                 w, 0, shadowColor);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        } else {
            // 垂直从上到下由透明到阴影色，模拟从 AppBar 向下的阴影
            GradientPaint gp = new GradientPaint(0, 0, new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 0),
                                                 0, h, shadowColor);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }

        g2.dispose();
    }
}


