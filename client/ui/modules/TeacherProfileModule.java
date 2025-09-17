package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.controller.TeacherController;
import common.vo.TeacherVO;
import common.vo.UserVO;
import client.ui.dashboard.components.CircularAvatar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.AlphaComposite;

/**
 * 教师个人信息管理模块
 * 实现教师个人档案的查询和修改功能
 */
public class TeacherProfileModule implements IModuleView {
    private JPanel root;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // 当前用户和连接
    private UserVO currentUser;

    // 控制器
    private TeacherController teacherController;

    // 数据
    private TeacherVO currentTeacher;

    // 查看模式组件
    private JPanel viewPanel;
    private CircularAvatar avatarLabel;
    private InfoLabel nameLabel;
    private InfoLabel teacherNoLabel;
    private InfoLabel phoneLabel;
    private InfoLabel emailLabel;
    private InfoLabel departmentLabel;
    private InfoLabel titleLabel;
    private InfoLabel officeLabel;
    private InfoLabel researchAreaLabel;
    private JButton editButton;

    // 编辑模式组件
    private JPanel editPanel;
    private JTextField nameField;
    private JTextField teacherNoField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField titleField;
    private JTextField officeField;
    private JTextArea researchAreaArea;
    private JButton saveButton;
    private JButton cancelButton;

    // 状态标签
    private JLabel statusLabel;

    public TeacherProfileModule() {
        this.teacherController = new TeacherController();
        buildUI();
    }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // 创建卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 创建查看面板
        createViewPanel();

        // 创建编辑面板
        createEditPanel();

        // 创建状态栏
        createStatusBar();

        mainPanel.add(viewPanel, "view");
        mainPanel.add(editPanel, "edit");

        root.add(mainPanel, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        // 默认显示查看模式
        cardLayout.show(mainPanel, "view");
    }

    private void createViewPanel() {
        viewPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // 启用抗锯齿渲染
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        viewPanel.setBackground(Color.WHITE); // 纯白色背景
        viewPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 创建主卡片容器
        JPanel cardContainer = createCardContainer();

        viewPanel.add(cardContainer, BorderLayout.CENTER);
    }

    private JPanel createCardContainer() {
        // —— 仅改动阴影与裁剪的实现，其余保持不变 ——
        JPanel card = new JPanel(new BorderLayout()) {
            // 卡片圆角与阴影留白
            private final int arc = 20;
            private final int pad = 12; // 与 setBorder 对齐，保证外部留白足够

            // 双层投影参数（Material 风格）
            // Ambient：大半径、低透明、稍大偏移；Key：小半径、较高透明、轻微偏移
            private final ShadowSpec AMBIENT = new ShadowSpec(10, 0f, 4f, 0.18f);
            private final ShadowSpec KEY     = new ShadowSpec(4,  0f, 1.5f, 0.22f);

            // 缓存以减少重复模糊计算
            private BufferedImage ambientCache, keyCache;
            private Dimension cacheSize;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                ensureShadowCache();

                // 1) 先画阴影层（在卡片下）
                if (ambientCache != null) g2d.drawImage(ambientCache, 0, 0, null);
                if (keyCache != null)     g2d.drawImage(keyCache, 0, 0, null);

                // 2) 画卡片本体
                int x = pad;
                int y = pad;
                int w = getWidth()  - pad * 2;
                int h = getHeight() - pad * 2;

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x, y, w, h, arc, arc);

                // 3) 1px 细描边
                g2d.setColor(new Color(229, 231, 235));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(x, y, w, h, arc, arc);

                // 4) 顶部内侧高光（很淡的白色到透明，增强"悬浮"感）
                Paint glow = new GradientPaint(0, y, new Color(255, 255, 255, 60),
                        0, y + 10, new Color(255, 255, 255, 0));
                Shape clip = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
                Paint old = g2d.getPaint();
                g2d.setPaint(glow);
                g2d.setClip(clip);
                g2d.fillRect(x, y, w, 12);
                g2d.setPaint(old);
                g2d.setClip(null);

                g2d.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                // 子组件按同一圆角裁剪，避免圆角边像素缝隙
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = pad, y = pad, w = getWidth() - pad * 2, h = getHeight() - pad * 2;
                g2.setClip(new RoundRectangle2D.Float(x, y, w, h, arc, arc));
                super.paintChildren(g2);
                g2.dispose();
            }

            private void ensureShadowCache() {
                int w = getWidth(), h = getHeight();
                if (w <= 0 || h <= 0) return;

                if (cacheSize != null && cacheSize.width == w && cacheSize.height == h
                        && ambientCache != null && keyCache != null) {
                    return;
                }
                cacheSize = new Dimension(w, h);

                // 生成两层阴影缓存
                ambientCache = createShadowLayer(w, h, arc, pad, AMBIENT);
                keyCache     = createShadowLayer(w, h, arc, pad, KEY);
            }

            private BufferedImage createShadowLayer(int width, int height, int arc, int pad, ShadowSpec spec) {
                // 以整个组件大小为画布
                BufferedImage src = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = src.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = pad;
                int cy = pad;
                int cw = width  - pad * 2;
                int ch = height - pad * 2;

                // 1) 画"投影形状"，按偏移量位移
                g.setColor(new Color(0, 0, 0, Math.round(spec.alpha * 255)));
                g.fill(new RoundRectangle2D.Float(
                        cx + spec.offsetX, cy + spec.offsetY, cw, ch, arc, arc
                ));
                g.dispose();

                // 2) 高斯模糊
                BufferedImage blurred = gaussianBlur(src, Math.max(1, Math.round(spec.radius)));

                // 3) 挖空卡片本体（只保留外部阴影）
                Graphics2D g2 = blurred.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.DstOut);
                g2.fill(new RoundRectangle2D.Float(cx, cy, cw, ch, arc, arc));
                g2.dispose();

                return blurred;
            }

            private BufferedImage gaussianBlur(BufferedImage src, int radius) {
                if (radius < 1) return src;
                float sigma = radius / 3f;
                int size = radius * 2 + 1;
                float[] k = createGaussianKernel(size, sigma);

                ConvolveOp opX = new ConvolveOp(new Kernel(size, 1, k), ConvolveOp.EDGE_NO_OP, null);
                ConvolveOp opY = new ConvolveOp(new Kernel(1, size, k), ConvolveOp.EDGE_NO_OP, null);

                BufferedImage tmp = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
                BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);

                opX.filter(src, tmp);
                opY.filter(tmp, dst);
                return dst;
            }

            private float[] createGaussianKernel(int size, float sigma) {
                float[] kernel = new float[size];
                float sum = 0f;
                int r = size / 2;
                for (int i = 0; i < size; i++) {
                    int x = i - r;
                    float v = (float) Math.exp(-(x * x) / (2f * sigma * sigma));
                    kernel[i] = v;
                    sum += v;
                }
                for (int i = 0; i < size; i++) kernel[i] /= sum;
                return kernel;
            }

            @Override
            public boolean isOpaque() {
                return false; // 保持透明以显示阴影
            }

            // 阴影规格
            class ShadowSpec {
                final float radius;   // 模糊半径
                final float offsetX;  // x 偏移
                final float offsetY;  // y 偏移
                final float alpha;    // 透明度
                ShadowSpec(float radius, float offsetX, float offsetY, float alpha) {
                    this.radius = radius;
                    this.offsetX = offsetX;
                    this.offsetY = offsetY;
                    this.alpha = alpha;
                }
            }
        };

        // 阴影留白（保持原值 = 12，不改你的布局）
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(900, 700));

        // 创建上半部分墨绿色区域
        JPanel topSection = createTopSection();

        // 创建下半部分白色区域
        JPanel bottomSection = createBottomSection();

        card.add(topSection, BorderLayout.NORTH);
        card.add(bottomSection, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopSection() {
        JPanel topSection = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制墨绿色背景，只圆角上半部分
                g2d.setColor(new Color(0x2C, 0x4F, 0x3D));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 20, 20);

                g2d.dispose();
            }
        };
        topSection.setOpaque(false);
        topSection.setPreferredSize(new Dimension(0, 150)); // 四分之一高度

        // 在交接线上添加头像 - 使用绝对定位使其中心位于交界线
        JPanel avatarContainer = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                // 不绘制背景，保持透明
            }
        };
        avatarContainer.setOpaque(false);
        avatarContainer.setPreferredSize(new Dimension(0, 50)); // 给头像留出空间

        avatarLabel = new CircularAvatar(100);
        // 移除白色边框，设置为无边框
        avatarLabel.setBorder(null);
        // 设置头像位置使其中心位于交界线（向上偏移50px，这样头像中心就在交界线上）
        avatarLabel.setBounds(40, 0, 100, 100);

        avatarContainer.add(avatarLabel);
        topSection.add(avatarContainer, BorderLayout.SOUTH);

        return topSection;
    }

    private JPanel createBottomSection() {
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setBackground(Color.WHITE);
        bottomSection.setBorder(new EmptyBorder(80, 40, 30, 40)); // 增加顶部留白给头像更多空间

        // 创建个人信息展示区域
        JPanel infoPanel = createInfoDisplayPanel();

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();

        bottomSection.add(infoPanel, BorderLayout.CENTER);
        bottomSection.add(buttonPanel, BorderLayout.SOUTH);

        return bottomSection;
    }

    private JPanel createInfoDisplayPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // 启用抗锯齿渲染
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // 创建信息标签，使用更大的字体
        nameLabel = createInfoLabel("姓名", "", 16);
        teacherNoLabel = createInfoLabel("工号", "", 14);
        phoneLabel = createInfoLabel("联系电话", "", 14);
        emailLabel = createInfoLabel("邮箱", "", 14);
        departmentLabel = createInfoLabel("院系", "", 14);
        titleLabel = createInfoLabel("职称", "", 14);
        officeLabel = createInfoLabel("办公室", "", 14);
        researchAreaLabel = createInfoLabel("研究方向", "", 14);

        // 添加标签到面板
        int row = 0;
        addInfoRow(panel, gbc, nameLabel, teacherNoLabel, row++);
        addInfoRow(panel, gbc, phoneLabel, emailLabel, row++);
        addInfoRow(panel, gbc, departmentLabel, titleLabel, row++);
        addInfoRow(panel, gbc, officeLabel, researchAreaLabel, row++);

        return panel;
    }

    private InfoLabel createInfoLabel(String label, String value, int fontSize) {
        return new InfoLabel(label, value, fontSize);
    }
    
    // 自定义信息标签类
    private class InfoLabel extends JPanel {
        private JLabel labelComponent;
        private JLabel valueComponent;
        
        public InfoLabel(String label, String value, int fontSize) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBackground(Color.WHITE);
            setOpaque(true);

            labelComponent = new JLabel(label + ": ");
            labelComponent.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
            labelComponent.setForeground(new Color(0x66, 0x66, 0x66));
            // 启用抗锯齿渲染
            labelComponent.putClientProperty("awt.font.desktophints", 
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            valueComponent = new JLabel(value);
            valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, fontSize));
            valueComponent.setForeground(new Color(0x66, 0x66, 0x66));
            // 启用抗锯齿渲染
            valueComponent.putClientProperty("awt.font.desktophints", 
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            add(labelComponent);
            add(valueComponent);
        }
        
        public void setText(String text) {
            valueComponent.setText(text);
            // 强制刷新
            revalidate();
            repaint();
        }
        
        public String getText() {
            return valueComponent.getText();
        }
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, InfoLabel left, InfoLabel right, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(left, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(right, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        editButton = new JButton("编辑信息") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(0x2C, 0x4F, 0x3D).darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(0x2C, 0x4F, 0x3D).brighter());
                } else {
                    g2d.setColor(new Color(0x2C, 0x4F, 0x3D));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();

                super.paintComponent(g);
            }
        };
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        editButton.setPreferredSize(new Dimension(110, 40));
        editButton.setForeground(Color.WHITE);
        editButton.setContentAreaFilled(false);
        editButton.setBorderPainted(false);
        editButton.addActionListener(e -> switchToEditMode());

        panel.add(editButton);

        return panel;
    }

    private void createEditPanel() {
        editPanel = new JPanel(new BorderLayout());
        editPanel.setBackground(Color.WHITE);
        editPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 创建标题
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("编辑个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0x2C, 0x4F, 0x3D));
        titlePanel.add(titleLabel);

        // 创建表单面板
        JPanel formPanel = createFormPanel();

        // 创建按钮面板
        JPanel buttonPanel = createEditButtonPanel();

        editPanel.add(titlePanel, BorderLayout.NORTH);
        editPanel.add(formPanel, BorderLayout.CENTER);
        editPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 创建可编辑的表单字段（只允许修改的字段）
        nameField = createTextField("姓名", 20);
        phoneField = createTextField("联系电话", 20);
        emailField = createTextField("邮箱", 20);
        officeField = createTextField("办公室", 20);
        researchAreaArea = createTextArea("研究方向", 3, 20);

        // 创建只读字段（不可修改的字段）
        teacherNoField = createReadOnlyTextField("工号", 20);
        departmentField = createReadOnlyTextField("院系", 20);
        titleField = createReadOnlyTextField("职称", 20);

        // 添加字段到面板
        int row = 0;
        addFormRow(panel, gbc, nameField, phoneField, row++);
        addFormRow(panel, gbc, emailField, officeField, row++);

        // 研究方向单独一行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 为研究方向字段创建包装组件
        JPanel researchAreaWrapper = new JPanel(new BorderLayout());
        researchAreaWrapper.setBackground(Color.WHITE);
        
        JLabel researchAreaLabel = new JLabel("研究方向:");
        researchAreaLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        researchAreaLabel.setForeground(new Color(0x66, 0x66, 0x66));
        
        JPanel researchAreaContainer = new JPanel(new BorderLayout());
        researchAreaContainer.setBackground(Color.WHITE);
        researchAreaContainer.add(researchAreaLabel, BorderLayout.NORTH);
        researchAreaContainer.add(researchAreaArea, BorderLayout.CENTER);
        
        researchAreaWrapper.add(researchAreaContainer, BorderLayout.CENTER);
        panel.add(researchAreaWrapper, gbc);
        row++;

        // 添加只读字段显示
        addFormRow(panel, gbc, teacherNoField, departmentField, row++);
        addFormRow(panel, gbc, titleField, new JPanel(), row++); // 空面板占位

        return panel;
    }

    private JTextField createTextField(String label, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(new Color(0x66, 0x66, 0x66));

        JTextField field = new JTextField(columns);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);

        // 创建一个包装组件
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);

        // 将字段存储到容器中以便访问
        field.putClientProperty("wrapper", wrapper);

        return field;
    }

    private JTextField createReadOnlyTextField(String label, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(new Color(0x66, 0x66, 0x66));

        JTextField field = new JTextField(columns);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        field.setEditable(false); // 设置为只读
        field.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 设置只读背景色
        field.setForeground(new Color(0x99, 0x99, 0x99)); // 设置只读文字颜色
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);

        // 创建一个包装组件
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);

        // 将字段存储到容器中以便访问
        field.putClientProperty("wrapper", wrapper);

        return field;
    }

    private JTextArea createTextArea(String label, int rows, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(new Color(0x66, 0x66, 0x66));

        JTextArea area = new JTextArea(rows, columns);
        area.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(area, BorderLayout.CENTER);

        return area;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, JComponent left, JComponent right, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // 获取包装组件（包含标签的容器）
        JPanel leftWrapper = (JPanel) left.getClientProperty("wrapper");
        if (leftWrapper != null) {
            panel.add(leftWrapper, gbc);
        } else {
            panel.add(left, gbc);
        }

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        
        // 获取包装组件（包含标签的容器）
        JPanel rightWrapper = (JPanel) right.getClientProperty("wrapper");
        if (rightWrapper != null) {
            panel.add(rightWrapper, gbc);
        } else {
            panel.add(right, gbc);
        }
    }

    private JPanel createEditButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.addActionListener(e -> switchToViewMode());

        saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        saveButton.setPreferredSize(new Dimension(80, 35));
        saveButton.setBackground(new Color(0x2C, 0x4F, 0x3D));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveTeacherInfo());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void createStatusBar() {
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0x66, 0x66, 0x66));
        statusLabel.setBorder(new EmptyBorder(8, 20, 8, 20));
        statusLabel.setBackground(new Color(0xF5, 0xF5, 0xF5));
        statusLabel.setOpaque(true);
    }

    private void switchToEditMode() {
        if (currentTeacher == null) {
            showStatus("请先加载教师信息", true);
            return;
        }

        // 填充表单数据
        fillEditForm();

        // 切换到编辑模式
        cardLayout.show(mainPanel, "edit");
        showStatus("编辑模式", false);
    }

    private void switchToViewMode() {
        // 切换到查看模式
        cardLayout.show(mainPanel, "view");
        showStatus("查看模式", false);
    }

    private void fillEditForm() {
        if (currentTeacher == null) return;

        nameField.setText(currentTeacher.getName());
        teacherNoField.setText(currentTeacher.getTeacherNo());
        phoneField.setText(currentTeacher.getPhone());
        emailField.setText(currentTeacher.getEmail());
        departmentField.setText(currentTeacher.getDepartment());
        titleField.setText(currentTeacher.getTitle());
        officeField.setText(currentTeacher.getOffice());
        researchAreaArea.setText(currentTeacher.getResearchArea());
    }

    private void refreshTeacherInfo() {
        if (currentUser == null) {
            showStatus("用户信息未初始化", true);
            return;
        }
        
        showStatus("正在加载教师信息...", false);

        teacherController.getTeacherInfo(currentUser.getUserId(), new TeacherController.GetTeacherInfoCallback() {
            @Override
            public void onSuccess(TeacherVO teacher) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][TeacherProfileModule] 教师信息加载成功回调");
                    System.out.println("  - teacher = " + (teacher != null ? "存在" : "null"));
                    if (teacher != null) {
                        System.out.println("  - teacher.getUser() = " + (teacher.getUser() != null ? "存在" : "null"));
                        if (teacher.getUser() != null) {
                            System.out.println("  - avatarPath = " + teacher.getUser().getAvatarPath());
                        }
                    }
                    
                    currentTeacher = teacher;
                    updateViewDisplay();
                    showStatus("教师信息加载成功", false);
                });
            }

            @Override
            public void onFailure(String error) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("加载失败: " + error, true);
                });
            }
        });
    }

    private void saveTeacherInfo() {
        System.out.println("[DEBUG][TeacherProfileModule] ========== 开始保存教师信息 ==========");
        
        if (currentTeacher == null) {
            System.err.println("[DEBUG][TeacherProfileModule] 教师信息未加载");
            showStatus("教师信息未加载", true);
            return;
        }

        System.out.println("[DEBUG][TeacherProfileModule] 当前教师信息：ID=" + currentTeacher.getId() + ", userId=" + currentTeacher.getUserId());

        // 验证必填字段（只验证可编辑的字段）
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String office = officeField.getText().trim();
        String researchArea = researchAreaArea.getText().trim();

        System.out.println("[DEBUG][TeacherProfileModule] 表单数据：");
        System.out.println("  - 姓名=" + name);
        System.out.println("  - 电话=" + phone);
        System.out.println("  - 邮箱=" + email);
        System.out.println("  - 办公室=" + office);
        System.out.println("  - 研究方向=" + researchArea);

        if (name.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 姓名不能为空");
            showStatus("姓名不能为空", true);
            return;
        }

        if (phone.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 联系电话不能为空");
            showStatus("联系电话不能为空", true);
            return;
        }

        if (email.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 邮箱不能为空");
            showStatus("邮箱不能为空", true);
            return;
        }

        System.out.println("[DEBUG][TeacherProfileModule] 字段验证通过，开始构建更新对象");
        showStatus("正在保存信息...", false);

        // 更新教师信息（只更新允许修改的字段）
        TeacherVO updatedTeacher = new TeacherVO();
        updatedTeacher.setId(currentTeacher.getId());
        updatedTeacher.setUserId(currentTeacher.getUserId());
        
        // 设置可修改的字段
        updatedTeacher.setName(name);
        updatedTeacher.setPhone(phone);
        updatedTeacher.setEmail(email);
        updatedTeacher.setOffice(office);
        updatedTeacher.setResearchArea(researchArea);
        
        // 设置只读字段（保持原有值）
        updatedTeacher.setTeacherNo(currentTeacher.getTeacherNo());
        updatedTeacher.setTitle(currentTeacher.getTitle());
        updatedTeacher.setDepartment(currentTeacher.getDepartment());

        System.out.println("[DEBUG][TeacherProfileModule] 构建的更新对象：");
        System.out.println("  - ID=" + updatedTeacher.getId());
        System.out.println("  - userId=" + updatedTeacher.getUserId());
        System.out.println("  - name=" + updatedTeacher.getName());
        System.out.println("  - phone=" + updatedTeacher.getPhone());
        System.out.println("  - email=" + updatedTeacher.getEmail());
        System.out.println("  - office=" + updatedTeacher.getOffice());
        System.out.println("  - researchArea=" + updatedTeacher.getResearchArea());
        System.out.println("  - teacherNo=" + updatedTeacher.getTeacherNo());
        System.out.println("  - title=" + updatedTeacher.getTitle());
        System.out.println("  - department=" + updatedTeacher.getDepartment());

        System.out.println("[DEBUG][TeacherProfileModule] 调用teacherController.updateTeacher");
        // 更新教师信息
        teacherController.updateTeacher(updatedTeacher, new TeacherController.UpdateTeacherCallback() {
            @Override
            public void onSuccess(String message) {
                System.out.println("[DEBUG][TeacherProfileModule] 更新成功回调：" + message);
                SwingUtilities.invokeLater(() -> {
                    // 刷新教师信息
                    refreshTeacherInfo();
                    switchToViewMode();
                    showStatus("保存成功", false);
                });
            }

            @Override
            public void onFailure(String error) {
                System.err.println("[DEBUG][TeacherProfileModule] 更新失败回调：" + error);
                SwingUtilities.invokeLater(() -> {
                    showStatus("保存失败: " + error, true);
                });
            }
        });
    }

    private void updateViewDisplay() {
        if (currentTeacher == null) {
            return;
        }

        try {
            nameLabel.setText(currentTeacher.getName());
            teacherNoLabel.setText(currentTeacher.getTeacherNo());
            phoneLabel.setText(currentTeacher.getPhone());
            emailLabel.setText(currentTeacher.getEmail());
            departmentLabel.setText(currentTeacher.getDepartment());
            titleLabel.setText(currentTeacher.getTitle());
            officeLabel.setText(currentTeacher.getOffice());
            researchAreaLabel.setText(currentTeacher.getResearchArea());

            // 更新头像
            System.out.println("[DEBUG][TeacherProfileModule] 开始更新头像");
            System.out.println("  - currentTeacher.getUser() = " + (currentTeacher.getUser() != null ? "存在" : "null"));
            if (currentTeacher.getUser() != null) {
                System.out.println("  - avatarPath = " + currentTeacher.getUser().getAvatarPath());
            }
            
            // 先清除之前的头像
            avatarLabel.setAvatarImage(null);
            
            if (currentTeacher.getUser() != null && currentTeacher.getUser().getAvatarPath() != null 
                && !currentTeacher.getUser().getAvatarPath().trim().isEmpty()) {
                // 加载头像图片
                try {
                    String avatarPath = currentTeacher.getUser().getAvatarPath();
                    System.out.println("[DEBUG][TeacherProfileModule] 尝试加载头像图片: " + avatarPath);
                    
                    // 修复头像路径：如果路径不以resources/开头，则添加resources/前缀
                    String fullAvatarPath = avatarPath;
                    if (!avatarPath.startsWith("resources/")) {
                        fullAvatarPath = "resources/" + avatarPath;
                        System.out.println("[DEBUG][TeacherProfileModule] 修正头像路径: " + fullAvatarPath);
                    }
                    
                    // 检查文件是否存在
                    java.io.File avatarFile = new java.io.File(fullAvatarPath);
                    if (avatarFile.exists() && avatarFile.isFile()) {
                        ImageIcon icon = new ImageIcon(fullAvatarPath);
                        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                            avatarLabel.setAvatarImage(icon.getImage());
                            System.out.println("[DEBUG][TeacherProfileModule] 头像图片加载成功");
                        } else {
                            throw new Exception("图片尺寸无效");
                        }
                    } else {
                        throw new Exception("头像文件不存在: " + fullAvatarPath);
                    }
                } catch (Exception e) {
                    System.err.println("[DEBUG][TeacherProfileModule] 头像图片加载失败: " + e.getMessage());
                    avatarLabel.setDefaultText(currentTeacher.getName().substring(0, 1).toUpperCase());
                    System.out.println("[DEBUG][TeacherProfileModule] 使用默认头像文字: " + currentTeacher.getName().substring(0, 1).toUpperCase());
                }
            } else {
                avatarLabel.setDefaultText(currentTeacher.getName().substring(0, 1).toUpperCase());
                System.out.println("[DEBUG][TeacherProfileModule] 使用默认头像文字: " + currentTeacher.getName().substring(0, 1).toUpperCase());
            }
            
            // 强制刷新UI
            viewPanel.revalidate();
            viewPanel.repaint();
            root.revalidate();
            root.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        if (isError) {
            statusLabel.setForeground(new Color(0xD3, 0x2F, 0x2F));
        } else {
            statusLabel.setForeground(new Color(0x66, 0x66, 0x66));
        }
    }

    @Override
    public String getKey() {
        return ModuleKeys.TEACHER_PROFILE;
    }

    @Override
    public String getDisplayName() {
        return "个人信息";
    }

    @Override
    public String getIconPath() {
        return "icons/学籍.png"; // 使用学籍图标
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void initContext(UserVO currentUser, client.net.ServerConnection connection) {
        this.currentUser = currentUser;

        // 初始化时加载教师信息
        SwingUtilities.invokeLater(this::refreshTeacherInfo);
    }
}
