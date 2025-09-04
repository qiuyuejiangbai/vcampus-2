package client.ui.dashboard.layout;

import client.ui.util.FontUtil;
import client.ui.dashboard.components.CircularAvatar;
import client.controller.StudentController;
import common.vo.UserVO;
import common.vo.StudentVO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.LinearGradientPaint;
import java.awt.AlphaComposite;
import javax.swing.Timer;

/** 左侧可折叠导航，包含Logo、个人信息和导航菜单，使用墨绿色配色方案。 */
public class SideNav extends JPanel {
    public interface NavListener { void onNavSelected(String key); }

    private int expandedWidth = 240;
    private int collapsedWidth = 80;
    private boolean expanded = true;
    private final Map<String, NavButton> keyToButton = new LinkedHashMap<String, NavButton>();
    private NavListener listener;
    private String selectedKey = null;
    // 可变强调色：默认墨绿色，点击标题栏“主题”按钮后可切换为灰色
    private Color accentColor = new Color(0x2C, 0x4F, 0x3D);
    private final Color textDefault = Color.BLACK;
    private final Color hoverBg = new Color(0xF5, 0xF5, 0xF5);
    private final Color selectedBg = new Color(0xE9, 0xEA, 0xEC);
    private int topShadowSkipPx = 0; // 顶部跳过阴影的高度（用于让 AppBar 覆盖阴影）
    
    // 新增组件
    private JLabel logoLabel;
    private JPanel logoPanel;
    private JPanel userInfoPanel;
    private CircularAvatar avatarLabel;
    private JLabel nameLabel;
    private JLabel majorLabel;
    private JLabel gradeLabel;
    private UserVO currentUser;
    private StudentController studentController;

    public SideNav() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE); // 改为白色背景
        setPreferredSize(new Dimension(expandedWidth, getHeight()));
        this.studentController = new StudentController();

        // 使用自定义 Border 在右侧绘制渐变阴影，并通过 Insets 预留空间避免被子组件覆盖
        setBorder(new AbstractBorder() {
            private final int shadowWidth = 12;

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sx = width - shadowWidth;
                if (sx < 0) sx = 0;
                LinearGradientPaint paint = new LinearGradientPaint(
                    sx, 0, width, 0,
                    new float[] { 0f, 1f },
                    new Color[] { new Color(0, 0, 0, 45), new Color(0, 0, 0, 0) }
                );
                g2.setPaint(paint);
                int skipTop = Math.max(0, topShadowSkipPx);
                if (skipTop >= height) {
                    // 全部被跳过，不绘制
                } else {
                    g2.fillRect(sx, skipTop, shadowWidth, height - skipTop);
                }
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                // 为阴影预留 12px 的可见区域（仅用于显示阴影，不在顶部 skip 区域绘制）
                return new Insets(0, 0, 0, shadowWidth);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.left = 0; insets.top = 0; insets.bottom = 0; insets.right = shadowWidth;
                return insets;
            }
        });
    }
    
    public void setNavListener(SideNav.NavListener l) { this.listener = l; }

    /** 设置顶部需要跳过绘制阴影的像素高度（通常为 AppBar 的高度）。 */
    public void setTopShadowSkipPx(int px) {
        this.topShadowSkipPx = Math.max(0, px);
        repaint();
    }
    
    public void setCurrentUser(UserVO user) {
        this.currentUser = user;
        initUserInfo();
        // 如果是学生，获取详细信息
        if (user != null && user.isStudent()) {
            loadStudentInfo();
        } else if (user != null && user.isTeacher()) {
            // 教师端：第二行显示为“教师”占位，后续如需可扩展加载 TeacherVO
            if (majorLabel != null) {
                majorLabel.setText("教师");
            }
        }
    }

    // 阴影已移至 Border 实现；保持默认背景绘制

    private void loadStudentInfo() {
        // 即使 userId 为空也不跳过请求，由服务器基于会话判定 [[memory:8117340]]
        studentController.getStudentInfo(currentUser.getUserId(), new StudentController.GetStudentInfoCallback() {
            @Override
            public void onSuccess(StudentVO student) {
                SwingUtilities.invokeLater(() -> {
                    updateStudentInfo(student);
                });
            }
            
            @Override
            public void onFailure(String error) {
                // 如果获取失败，使用默认信息
                System.err.println("获取学生信息失败: " + error);
                SwingUtilities.invokeLater(() -> {
                    if (majorLabel != null) {
                        majorLabel.setText("专业未知");
                    }
                });
            }
        });
    }
    
    private void updateStudentInfo(StudentVO student) {
        if (student == null) {
            if (majorLabel != null) {
                majorLabel.setText("专业未知");
            }
            return;
        }
        // 姓名以学生档案为准
        if (nameLabel != null && student.getName() != null && !student.getName().trim().isEmpty()) {
            nameLabel.setText(student.getName());
        }
        // 专业为空则显示“专业未知” [[memory:8117340]]
        if (majorLabel != null) {
            String major = student.getMajor();
            majorLabel.setText(major != null && !major.trim().isEmpty() ? major : "专业未知");
        }
    }

    private void initUserInfo() {
        // Logo区域 - 固定高度80px
        logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(accentColor);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        logoPanel.setPreferredSize(new Dimension(expandedWidth, 80));
        logoPanel.setMaximumSize(new Dimension(expandedWidth, 80));
        logoPanel.setMinimumSize(new Dimension(expandedWidth, 80));
        
        logoLabel = new JLabel("vCampus");
        // 使用 Pacifico 字体作为 Logo 字体
        logoLabel.setFont(FontUtil.getPacificoFont(Font.PLAIN, 28f));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        
        // 个人信息区域 - 使用水平布局，保证头像与文字垂直居中
        userInfoPanel = new JPanel(new BorderLayout(16, 0));
        userInfoPanel.setBackground(Color.WHITE);
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        userInfoPanel.setPreferredSize(new Dimension(expandedWidth, 96));
        userInfoPanel.setMaximumSize(new Dimension(expandedWidth, 96));
        userInfoPanel.setMinimumSize(new Dimension(expandedWidth, 96));
        
        // 头像 - 左侧
        avatarLabel = new CircularAvatar(56);
        avatarLabel.setBorderWidth(0f);
        // 设置默认头像
        setDefaultAvatar();
        
        // 信息面板 - 右侧，使用GridBagLayout在可用高度内垂直居中
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        
        // 创建一个垂直面板来容纳文字信息，并设置垂直居中对齐
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        // 姓名
        String fallbackName = "用户";
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().trim().isEmpty()) {
            fallbackName = currentUser.getName();
        }
        nameLabel = new JLabel(fallbackName);
        nameLabel.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 15f));
        nameLabel.setForeground(accentColor); // 改为可变强调色
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 第二行：学生为“正在获取专业…”，教师为“教师”
        String secondLine = "";
        if (currentUser != null && currentUser.isStudent()) secondLine = "正在获取专业…";
        else if (currentUser != null && currentUser.isTeacher()) secondLine = "教师";
        else if (currentUser != null && currentUser.isAdmin()) secondLine = "管理员";
        else secondLine = "";
        majorLabel = new JLabel(secondLine);
        majorLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 12f));
        majorLabel.setForeground(accentColor); // 改为可变强调色
        majorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(majorLabel);
        // 仅两行显示：姓名、专业
        
        // 将文字面板添加到信息面板中，并用GridBagLayout实现垂直居中
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(textPanel, gbc);
        
        // 将头像和信息面板添加到个人信息区域
        userInfoPanel.add(avatarLabel, BorderLayout.WEST);
        userInfoPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 导航菜单区域 - 使用剩余空间
        navPanel = new JPanel();
        navPanel.setLayout(new GridBagLayout());
        navPanel.setBackground(Color.WHITE); // 改为白色背景
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        // 添加组件到主面板
        add(logoPanel);
        add(userInfoPanel);
        add(navPanel);
    }
    
    private JPanel navPanel; // 导航菜单面板
    
    private void setDefaultAvatar() {
        // 尝试加载默认头像图片
        try {
            // 尝试多个可能的路径
            String[] possiblePaths = {
                "resources/icons/默认头像.png",
                "icons/默认头像.png",
                "../resources/icons/默认头像.png",
                "./resources/icons/默认头像.png"
            };
            
            ImageIcon icon = null;
            for (String path : possiblePaths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        icon = new ImageIcon(file.getAbsolutePath());
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个路径
                }
            }
            
            // 如果文件路径都失败，尝试从类路径加载
            if (icon == null) {
                icon = new ImageIcon(getClass().getClassLoader().getResource("icons/默认头像.png"));
            }
            
            if (icon != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image img = icon.getImage();
                if (img != null) {
                    // 与 CircularAvatar(56) 尺寸一致，避免插值裁切
                    Image scaledImg = img.getScaledInstance(56, 56, Image.SCALE_SMOOTH);
                    avatarLabel.setAvatarImage(scaledImg);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("加载头像图片失败: " + e.getMessage());
        }
        
        // 如果所有方法都失败，使用默认头像
        avatarLabel.setDefaultText(currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty() 
            ? currentUser.getName().substring(0, 1) : "U");
    }

    public void addItem(final String key, String text, Icon icon) {
        Icon scaled = scaleIconIfNeeded(icon, 20, 20);
        final NavButton btn = new NavButton(text, scaled);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 16f));
        btn.setForeground(textDefault);
        // 让按钮在 BoxLayout 下水平拉伸，占满一整行
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        // 仅限制高度，避免限制宽度导致收缩
        btn.setMinimumSize(new Dimension(0, 50));
        btn.setOpaque(false);
        btn.addActionListener(e -> {
            setSelectedKey(key);
            if (listener != null) listener.onNavSelected(key);
        });
        keyToButton.put(key, btn);
        relayout();
    }

    /**
     * 将传入图标按比例缩放并居中到指定尺寸画布，避免被裁切。
     */
    private Icon scaleIconIfNeeded(Icon icon, int targetW, int targetH) {
        try {
            if (icon instanceof ImageIcon) {
                Image src = ((ImageIcon) icon).getImage();
                if (src == null) return icon;
                int iw = src.getWidth(null);
                int ih = src.getHeight(null);
                if (iw <= 0 || ih <= 0) return icon;

                double scale = Math.min((double) targetW / iw, (double) targetH / ih);
                int nw = Math.max(1, (int) Math.round(iw * scale));
                int nh = Math.max(1, (int) Math.round(ih * scale));

                BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = canvas.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = (targetW - nw) / 2;
                int y = (targetH - nh) / 2;
                // 直接在绘制时缩放原始图像，避免 getScaledInstance 懒加载导致的空白
                g2.drawImage(src, x, y, nw, nh, null);
                g2.dispose();
                return new ImageIcon(canvas);
            }
        } catch (Exception ignored) {}
        return icon;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        for (JButton b : keyToButton.values()) {
            b.setHorizontalTextPosition(SwingConstants.RIGHT);
            b.setIconTextGap(expanded ? 12 : 0);
            b.setText(expanded ? b.getText() : "");
        }
        Dimension size = new Dimension(expanded ? expandedWidth : collapsedWidth, getHeight());
        setPreferredSize(size);
        revalidate();
        repaint();
    }

    public boolean isExpanded() { return expanded; }

    private void relayout() {
        if (navPanel == null) return;
        
        navPanel.removeAll();
        
        int row = 0;
        for (JButton btn : keyToButton.values()) {
            // 固定按钮高度，水平铺满
            Dimension fixed = new Dimension(expandedWidth, 50);
            btn.setPreferredSize(fixed);
            btn.setMinimumSize(fixed);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 2, 0); // 行间距 2px
            navPanel.add(btn, gbc);
        }

        // 占位填充，推挤按钮至顶部
        GridBagConstraints fillerGbc = new GridBagConstraints();
        fillerGbc.gridx = 0;
        fillerGbc.gridy = row;
        fillerGbc.weightx = 1.0;
        fillerGbc.weighty = 1.0;
        fillerGbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        navPanel.add(filler, fillerGbc);

        navPanel.revalidate(); 
        navPanel.repaint();
    }

    private void setSelectedKey(String key) {
        if (key == null || key.equals(selectedKey)) return;
        String old = selectedKey;
        selectedKey = key;
        if (old != null) {
            NavButton oldBtn = keyToButton.get(old);
            if (oldBtn != null) oldBtn.setSelectedState(false);
        }
        NavButton newBtn = keyToButton.get(selectedKey);
        if (newBtn != null) newBtn.setSelectedState(true);
    }

    /** 对外暴露的选中指定 key 的方法（用于初始化默认选中项）。 */
    public void selectKey(String key) {
        setSelectedKey(key);
    }

    private class NavButton extends JButton {
        private final Icon originalIcon;
        private Icon blackIcon;
        private Icon greenIcon;
        private Icon grayIcon;
        private boolean hovered = false;
        private boolean selected = false;
        private float indicatorProgress = 0f; // 0.0~1.0，表示竖线高度从中间向上下延伸的进度
        private final int indicatorMax = 3; // 竖线固定宽度
        private final Timer hoverTimer;

        NavButton(String text, Icon icon) {
            super(text, icon);
            this.originalIcon = icon;
            this.blackIcon = tintIcon(icon, textDefault);
            this.greenIcon = tintIcon(icon, accentColor);
            this.grayIcon = tintIcon(icon, new Color(0x88, 0x88, 0x88));
            setIcon(blackIcon);
            setForeground(textDefault);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; startIndicator(true); repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; if (!selected) startIndicator(false); repaint(); }
            });

            // 改为竖直方向的延伸动画：固定宽度，进度从 0 -> 1 控制高度从中间向两端扩展
            hoverTimer = new Timer(30, e -> {
                float step = 0.12f;
                if (hovered || selected) {
                    if (indicatorProgress < 1f) {
                        indicatorProgress = Math.min(1f, indicatorProgress + step);
                        repaint();
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                } else {
                    if (indicatorProgress > 0f) {
                        indicatorProgress = Math.max(0f, indicatorProgress - step);
                        repaint();
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
        }

        void retintAccent(Color color) {
            this.greenIcon = tintIcon(originalIcon, color);
            // 若当前为选中态，立即应用新的前景与图标
            if (selected) {
                setForeground(color);
                setIcon(greenIcon);
            }
            repaint();
        }

        void setSelectedState(boolean sel) {
            this.selected = sel;
            indicatorProgress = sel ? 1f : (hovered ? indicatorProgress : 0f);
            setForeground(sel ? accentColor : textDefault);
            setIcon(sel ? greenIcon : blackIcon);
            repaint();
        }

        private void startIndicator(boolean expand) {
            if (hoverTimer.isRunning()) hoverTimer.stop();
            hoverTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 背景
            if (selected) {
                g2.setColor(selectedBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else if (hovered) {
                g2.setColor(hoverBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // 左侧竖线指示：固定宽度，从中线向上下延伸
            if (indicatorProgress > 0f) {
                int topPad = 6;
                int bottomPad = 6;
                int contentHeight = Math.max(0, getHeight() - topPad - bottomPad);
                int fullX = 0;
                int centerY = topPad + contentHeight / 2;
                int halfVisible = Math.max(0, Math.round(contentHeight * indicatorProgress / 2f));
                int visibleY = centerY - halfVisible;
                int visibleH = Math.min(contentHeight, halfVisible * 2);
                g2.setColor(accentColor);
                g2.fillRect(fullX, visibleY, indicatorMax, visibleH);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private Icon tintIcon(Icon icon, Color color) {
        if (!(icon instanceof ImageIcon)) return icon;
        Image src = ((ImageIcon) icon).getImage();
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        BufferedImage tinted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tinted.createGraphics();
        g2.drawImage(src, 0, 0, w, h, null);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return new ImageIcon(tinted);
    }

    /**
     * 应用灰色主题：将侧边Logo区背景与强调文字颜色改为灰色，并刷新按钮选中态颜色与指示条。
     */
    public void applyGrayTheme() {
        // 统一使用中性灰
        Color gray = new Color(0x88, 0x88, 0x88);
        this.accentColor = gray;

        if (logoPanel != null) logoPanel.setBackground(gray);
        if (nameLabel != null) nameLabel.setForeground(gray);
        if (majorLabel != null) majorLabel.setForeground(gray);
        if (gradeLabel != null) gradeLabel.setForeground(gray);

        // 刷新所有 NavButton 的选中态与图标着色
        for (Map.Entry<String, NavButton> e : keyToButton.entrySet()) {
            NavButton btn = e.getValue();
            if (btn != null) {
                btn.retintAccent(accentColor);
                // 保留黑色图标与 hover 背景；更新选中态前景色
                btn.setSelectedState(e.getKey().equals(selectedKey));
            }
        }

        revalidate();
        repaint();
    }

    /**
     * 应用墨绿色主题：恢复默认强调色，并刷新按钮与展示区域。
     */
    public void applyGreenTheme() {
        Color green = new Color(0x2C, 0x4F, 0x3D);
        this.accentColor = green;

        if (logoPanel != null) logoPanel.setBackground(green);
        if (nameLabel != null) nameLabel.setForeground(green);
        if (majorLabel != null) majorLabel.setForeground(green);

        for (Map.Entry<String, NavButton> e : keyToButton.entrySet()) {
            NavButton btn = e.getValue();
            if (btn != null) {
                btn.retintAccent(accentColor);
                btn.setSelectedState(e.getKey().equals(selectedKey));
            }
        }

        revalidate();
        repaint();
    }
}


