package client.ui.dashboard.titlebar;

import client.ui.util.FontUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** 自绘标题栏：最小化/最大化/关闭/拖拽/双击。 */
public class AppTitleBar extends JPanel {
    public interface WindowControl {
        void minimize();
        void toggleMaximize();
        void close();
        void toggleTheme();
    }

    private Point dragOffset;
    private Color baseBackground;
    private JLabel logo;

    public AppTitleBar(final JFrame frame, final WindowControl control) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setBackground(new Color(0x1B, 0x3A, 0x2A)); // 更深的墨绿色背景，用于区分
        baseBackground = getBackground();

        logo = new JLabel("vCampus-学生端");
        logo.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 14f));
        logo.setForeground(Color.WHITE);
        add(logo, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JButton theme = createButton("☀");
        JButton min = createButton("—");
        JButton max = createButton("□");
        JButton close = createButton("×");
        theme.addActionListener(e -> control.toggleTheme());
        min.addActionListener(e -> control.minimize());
        max.addActionListener(e -> control.toggleMaximize());
        close.addActionListener(e -> control.close());
        right.add(theme); right.add(min); right.add(max); right.add(close);
        add(right, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) {
                    Point p = e.getLocationOnScreen();
                    frame.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                }
            }
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) control.toggleMaximize(); }
        };
        addMouseListener(drag); addMouseMotionListener(drag);
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        b.setContentAreaFilled(false);
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setRolloverEnabled(true);
        // 悬停加深效果
        final Color hoverBg = new Color(0x0E, 0x20, 0x16); // 明显更深的墨绿色
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setOpaque(true);
                b.setContentAreaFilled(true);
                b.setBackground(hoverBg);
                b.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setOpaque(false);
                b.setContentAreaFilled(false);
                b.repaint();
            }
        });
        return b;
    }

    /** 切换标题栏背景色。 */
    public void setBarBackground(Color color) {
        if (color == null) return;
        setBackground(color);
        repaint();
    }

    /** 恢复为初始墨绿色背景。 */
    public void resetBarBackground() {
        setBackground(baseBackground != null ? baseBackground : new Color(0x1B, 0x3A, 0x2A));
        repaint();
    }

    /** 设置左侧标题文本（用于区分学生端/教师端等）。 */
    public void setTitleText(String text) {
        if (logo != null && text != null) {
            logo.setText(text);
            repaint();
        }
    }
}


