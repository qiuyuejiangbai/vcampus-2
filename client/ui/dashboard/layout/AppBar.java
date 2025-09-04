package client.ui.dashboard.layout;

import client.ui.util.FontUtil;
import javax.swing.*;
import java.awt.*;

/** 顶部应用栏（仅 UI 展示，不触发业务）。 */
public class AppBar extends JPanel {
    private final JLabel semesterLabel = new JLabel("学期：2025-2026 1");
    private final JLabel moduleLabel = new JLabel("");
    private final int shadowHeight = 10;

    public AppBar() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // 底部阴影渐变
                    GradientPaint paint = new GradientPaint(0, height - AppBar.this.shadowHeight, new Color(0, 0, 0, 50), 0, height, new Color(0, 0, 0, 0));
                    g2.setPaint(paint);
                    g2.fillRect(x, height - AppBar.this.shadowHeight, width, AppBar.this.shadowHeight);
                    g2.dispose();
                }

                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(0, 0, AppBar.this.shadowHeight, 0);
                }

                @Override
                public Insets getBorderInsets(Component c, Insets insets) {
                    insets.left = 0; insets.top = 0; insets.bottom = AppBar.this.shadowHeight; insets.right = 0;
                    return insets;
                }
            },
            BorderFactory.createEmptyBorder(8, 0, 8, 16)
        ));
        setBackground(Color.WHITE); // 改为白色背景

        semesterLabel.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 16f));
        semesterLabel.setForeground(new Color(0x2C, 0x4F, 0x3D));
        moduleLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 16f));
        moduleLabel.setForeground(new Color(0x2C, 0x4F, 0x3D));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        row.add(semesterLabel);
        row.add(moduleLabel);

        add(row, BorderLayout.WEST);
    }

    /** 更新当前模块名显示 */
    public void setModuleName(String moduleName) {
        moduleLabel.setText(moduleName != null && moduleName.trim().length() > 0 ? moduleName : "");
        revalidate();
        repaint();
    }

    /** 返回 AppBar 底部阴影高度（用于侧边栏跳过区域计算）。 */
    public int getShadowHeight() { return shadowHeight; }
}

