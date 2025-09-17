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
        void logout();
        void changePassword();
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
        JButton changePassword = createIconButton("resources/icons/修改密码.png");
        JButton logout = createIconButton("resources/icons/登出.png");
        JButton min = createButton("—");
        JButton max = createButton("□");
        JButton close = createButton("×");
        changePassword.addActionListener(e -> control.changePassword());
        logout.addActionListener(e -> control.logout());
        min.addActionListener(e -> control.minimize());
        max.addActionListener(e -> control.toggleMaximize());
        close.addActionListener(e -> control.close());
        right.add(changePassword); right.add(logout); right.add(min); right.add(max); right.add(close);
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

    private JButton createIconButton(String iconPath) {
        JButton b = new JButton();
        b.setFocusable(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setRolloverEnabled(true);
        
        // 加载图标并缩放
        Icon originalIcon = loadIcon(iconPath);
        if (originalIcon != null) {
            // 设置图标大小为合适的大小，与文字按钮保持一致
            int iconSize = 16; // 16x16像素，与文字按钮的字体大小相匹配
            ImageIcon imageIcon = (ImageIcon) originalIcon;
            Image originalImage = imageIcon.getImage();
            
            // 确保图像已加载
            if (originalImage.getWidth(null) > 0 && originalImage.getHeight(null) > 0) {
                Image scaledImage = originalImage.getScaledInstance(
                    iconSize, iconSize, Image.SCALE_SMOOTH);
                
                // 将图标转换为白色
                Image whiteImage = convertToWhite(scaledImage);
                b.setIcon(new ImageIcon(whiteImage));
            } else {
                // 如果图像尺寸无效，使用默认文字
                b.setText("图标"); 
                b.setForeground(Color.WHITE);
            }
        } else {
            b.setText("图标"); // 如果图标加载失败，显示文字
            b.setForeground(Color.WHITE);
        }
        
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

    private Image convertToWhite(Image image) {
        if (image == null) return null;
        
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        // 如果图像尺寸无效，返回原图像
        if (width <= 0 || height <= 0) {
            System.err.println("警告: 图像尺寸无效 (" + width + "x" + height + ")，跳过颜色转换");
            return image;
        }
        
        // 创建BufferedImage来处理图像
        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // 绘制原始图像
        g2d.drawImage(image, 0, 0, null);
        
        // 将图像转换为白色
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = bufferedImage.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                
                // 保持透明度，但将颜色改为白色
                if (alpha > 0) { // 如果不是完全透明
                    int whiteRgb = (alpha << 24) | 0xFFFFFF; // 白色，保持原透明度
                    bufferedImage.setRGB(x, y, whiteRgb);
                }
            }
        }
        
        g2d.dispose();
        return bufferedImage;
    }

    private Icon loadIcon(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        final String normalized = path.replace('\\', '/');
        try {
            // 候选路径（类路径变体 + 资源目录前缀 + 绝对/相对文件路径）
            String cp1 = normalized;
            String cp2 = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized;
            String cp3 = normalized.startsWith("resources/") ? normalized : ("resources/" + normalized);
            String cp4 = cp3.startsWith("/") ? cp3.substring(1) : "/" + cp3;

            String[] candidates = new String[] { cp1, cp2, cp3, cp4 };

            // 1) 从当前线程的 ClassLoader 与类的 ClassLoader 依次尝试
            ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
            ClassLoader cl2 = getClass().getClassLoader();
            for (String p : candidates) {
                if (p == null || p.trim().isEmpty()) continue;
                try {
                    java.net.URL url = (cl1 != null ? cl1.getResource(p) : null);
                    if (url == null && cl2 != null) url = cl2.getResource(p);
                    if (url != null) return new ImageIcon(url);
                } catch (Exception ignored) { }
            }

            // 2) 文件系统（相对/绝对路径）
            for (String p : new String[] { normalized, cp3 }) {
                try {
                    java.io.File file = new java.io.File(p);
                    if (file.exists()) return new ImageIcon(file.getAbsolutePath());
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.err.println("加载图标失败: " + normalized + " - " + e.getMessage());
        }
        System.err.println("未找到图标资源: " + normalized + " （请确认位于 classpath 或 resources 目录）");
        return null;
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


