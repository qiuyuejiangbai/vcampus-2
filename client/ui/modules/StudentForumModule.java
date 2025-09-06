package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import common.vo.ThreadVO;
import common.vo.PostVO;
import common.vo.UserVO;
import client.ui.dashboard.components.CircularAvatar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** 学生版论坛模块。 */
public class StudentForumModule implements IModuleView {
    private JPanel root;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // 帖子列表视图组件
    private JPanel threadListPanel;
    private JScrollPane threadScrollPane;
    private JButton createThreadButton;
    private JButton refreshButton;
    // 分类筛选按钮：最新/最热/精华
    private JButton latestCategoryButton;
    private JButton hotCategoryButton;
    private JButton essenceCategoryButton;
    private JButton selectedCategoryButton;
    
    // 帖子详情视图组件
    private JPanel threadDetailPanel;
    private JLabel threadTitleLabel;
    private JTextArea threadContentArea;
    private JLabel threadAuthorLabel;
    private JLabel threadTimeLabel;
    private JLabel threadReplyCountLabel;
    private JPanel threadTagPanel;
    private JScrollPane replyScrollPane;
    private JPanel replyListPanel;
    private JTextArea replyTextArea;
    private JButton replyButton;
    private JButton backToListButton;
    private JPanel actionPanel;
    private JToggleButton likeToggle;
    private JToggleButton favoriteToggle;
    private JButton shareButton;
    
    // 发帖对话框组件
    private JDialog createThreadDialog;
    private JTextField threadTitleField;
    private JTextArea threadContentField;
    private JComboBox<String> categoryComboBox;
    private JLabel contentCounterLabel;
    private JButton insertImageButton;
    private JButton insertAttachmentButton;
    private JButton submitThreadButton;
    private JButton cancelThreadButton;
    
    // 当前用户和管理员权限
    private UserVO currentUser;
    private boolean isAdmin = false;
    
    // 模拟数据
    private List<ThreadVO> threads;
    private List<PostVO> replies;
    private ThreadVO currentThread;

    public StudentForumModule() { 
        buildUI(); 
        initMockData();
    }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 249, 250));
        
        // 创建卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(248, 249, 250));
        
        // 创建帖子列表视图
        createThreadListView();
        
        // 创建帖子详情视图
        createThreadDetailView();
        
        // 创建发帖对话框
        createThreadDialog();
        
        // 添加视图到主面板
        mainPanel.add(threadListPanel, "LIST");
        mainPanel.add(threadDetailPanel, "DETAIL");
        
        root.add(mainPanel, BorderLayout.CENTER);
        
        // 默认显示列表视图
        cardLayout.show(mainPanel, "LIST");
    }

    /**
     * 加载并缩放图标到指定尺寸（用于按钮/标签的统一大小）。
     */
    private ImageIcon loadScaledIcon(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignored) { return null; }
    }
    
    private void createThreadListView() {
        threadListPanel = new JPanel(new BorderLayout());
        threadListPanel.setBackground(new Color(248, 249, 250));
        threadListPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // 顶部工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(new Color(255, 255, 255));
        toolbarPanel.setBorder(null);
        toolbarPanel.setPreferredSize(new Dimension(0, 50));
        
        // 分类选择按钮组
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoryPanel.setBackground(new Color(255, 255, 255));
        
        latestCategoryButton = createCategoryButton("最新", true);
        hotCategoryButton = createCategoryButton("最热", false);
        essenceCategoryButton = createCategoryButton("精华", false);

        // 默认选中“最新”
        selectedCategoryButton = latestCategoryButton;

        // 点击切换选中状态（仅样式切换，当前不改变排序逻辑）
        java.awt.event.ActionListener categoryClick = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                JButton src = (JButton) e.getSource();
                updateCategorySelection(src);
            }
        };
        latestCategoryButton.addActionListener(categoryClick);
        hotCategoryButton.addActionListener(categoryClick);
        essenceCategoryButton.addActionListener(categoryClick);

        categoryPanel.add(latestCategoryButton);
        categoryPanel.add(hotCategoryButton);
        categoryPanel.add(essenceCategoryButton);
        
        // 搜索框和刷新按钮
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(new Color(255, 255, 255));

        // 自定义圆角搜索框容器：默认无边框，悬停/聚焦时显示墨绿色边框
        final Color green = new Color(24, 121, 78);
        final int arc = 16;
        final int boxHeight = 36;
        final int boxWidth = 240;
        final boolean[] hoverActive = new boolean[]{false};
        final boolean[] focusActive = new boolean[]{false};

        JPanel searchBox = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 背景
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // 悬停/聚焦时绘制墨绿色描边
                if (hoverActive[0] || focusActive[0]) {
                    g2.setColor(green);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(boxWidth, boxHeight));
        searchBox.setBorder(new EmptyBorder(0, 10, 0, 10));

        // 搜索图标（使用资源图标）
        ImageIcon searchIconImg = loadScaledIcon("icons/搜索.png", 16, 16);
        JLabel searchIcon = new JLabel(searchIconImg);
        if (searchIconImg == null) {
            // 资源缺失时回退到 Unicode 图标
            searchIcon.setText("🔍");
            searchIcon.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
            searchIcon.setForeground(new Color(107, 114, 128));
        }
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 0));
        searchBox.add(searchIcon, BorderLayout.WEST);

        // 无边框输入框，带占位符“搜索内容...”
        JTextField searchField = new JTextField();
        searchField.setFont(UIManager.getFont("TextField.font").deriveFont(Font.PLAIN, 14f));
        searchField.setBorder(new EmptyBorder(0, 0, 0, 0));
        searchField.setOpaque(false);
        final String placeholder = "搜索内容...";
        final Color placeholderColor = new Color(156, 163, 175);
        final Color textColor = new Color(31, 41, 55);
        searchField.setForeground(placeholderColor);
        searchField.setText(placeholder);

        // 占位符与聚焦状态
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                focusActive[0] = true; searchBox.repaint();
                if (placeholder.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(textColor);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                focusActive[0] = false; searchBox.repaint();
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(placeholderColor);
                }
            }
        });

        // 悬停状态（容器与子组件均触发）
        java.awt.event.MouseAdapter hoverHandler = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { hoverActive[0] = true; searchBox.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { hoverActive[0] = false; searchBox.repaint(); }
        };
        searchBox.addMouseListener(hoverHandler);
        searchField.addMouseListener(hoverHandler);
        searchIcon.addMouseListener(hoverHandler);

        searchBox.add(searchField, BorderLayout.CENTER);

        // 刷新图标按钮（使用资源图标，点击刷新）
        ImageIcon refreshIcon = loadScaledIcon("icons/刷新.png", 18, 18);
        refreshButton = new JButton();
        if (refreshIcon != null) refreshButton.setIcon(refreshIcon);
        refreshButton.setToolTipText("刷新");
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setOpaque(false);
        refreshButton.setPreferredSize(new Dimension(boxHeight, boxHeight));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { refreshButton.setOpaque(true); refreshButton.setBackground(new Color(243, 244, 246)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { refreshButton.setOpaque(false); refreshButton.setBackground(new Color(0,0,0,0)); }
        });
        refreshButton.addActionListener(e -> refreshThreadList());

        searchPanel.add(searchBox);
        searchPanel.add(refreshButton);
        
        toolbarPanel.add(categoryPanel, BorderLayout.WEST);
        toolbarPanel.add(searchPanel, BorderLayout.EAST);
        
        // 主要内容区域 - 左右分栏布局
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(new Color(248, 249, 250));
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 左侧帖子列表区域
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(248, 249, 250));
        leftPanel.setBorder(null);
        
        // 发帖按钮（改为悬浮在滚动区域右下角）
        createThreadButton = createCirclePlusButton();
        createThreadButton.addActionListener(e -> showCreateThreadDialog());
        
        JPanel threadItemsPanel = new JPanel();
        threadItemsPanel.setLayout(new BoxLayout(threadItemsPanel, BoxLayout.Y_AXIS));
        threadItemsPanel.setBackground(new Color(248, 249, 250));
        threadItemsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        // 关键：使子项在 Y 轴 BoxLayout 下能够横向铺满可用宽度
        threadItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        threadScrollPane = new JScrollPane(threadItemsPanel);
        threadScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        threadScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        threadScrollPane.setBorder(null);
        threadScrollPane.getViewport().setBackground(new Color(248, 249, 250));
        
        // 增加滑动灵敏度
        JScrollBar verticalScrollBar = threadScrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(8); // 减少滚动单位，提高灵敏度
        verticalScrollBar.setBlockIncrement(32); // 减少块滚动单位
        
        // 自定义滑动条样式 - 现代化低调设计
        customizeScrollBar(verticalScrollBar);
        
        // 使用分层面板将按钮悬浮在滚动区域右下角
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        // 用于存储右侧信息面板的期望右内边距（随右侧栏宽度而定）
        final int[] rightSidebarPad = new int[]{0};
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                threadScrollPane.setBounds(0, 0, size.width, size.height);
                int margin = 16;
                Dimension btnSize = createThreadButton.getPreferredSize();
                createThreadButton.setBounds(
                    Math.max(0, size.width - btnSize.width - margin),
                    Math.max(0, size.height - btnSize.height - margin),
                    btnSize.width,
                    btnSize.height
                );

                // 确保右侧内容不被悬浮按钮遮挡：为滚动列表添加与按钮宽度相当的右侧内边距
                try {
                    javax.swing.border.Border b = threadItemsPanel.getBorder();
                    int top = 10, left = 0, bottom = 10;
                    if (b instanceof javax.swing.border.EmptyBorder) {
                        java.awt.Insets ins = ((javax.swing.border.EmptyBorder) b).getBorderInsets();
                        top = ins.top; left = ins.left; bottom = ins.bottom;
                    }
                    // 预留按钮宽度 + 边距，且至少包含右侧公告/热门栏的宽度
                    int rightPad = Math.max(btnSize.width + margin + 8, rightSidebarPad[0]);
                    threadItemsPanel.setBorder(new javax.swing.border.EmptyBorder(top, left, bottom, rightPad));
                } catch (Exception ignore) {}
            }
        });
        layeredPane.add(threadScrollPane, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(createThreadButton, JLayeredPane.PALETTE_LAYER);
        leftPanel.add(layeredPane, BorderLayout.CENTER);
        
        // 右侧信息面板
        JPanel rightPanel = createRightInfoPanel();
        // 计算右侧面板所需为左侧内容预留的右内边距（含额外间距）
        int sidebarWidth = Math.max(0, rightPanel.getPreferredSize() != null ? rightPanel.getPreferredSize().width : 300);
        rightSidebarPad[0] = sidebarWidth + 16; // 右侧栏宽度 + 与内容间距
        // 初始化时也同步一次右内边距，避免首次展示被遮挡
        try {
            javax.swing.border.Border b = threadItemsPanel.getBorder();
            int top = 10, left = 0, bottom = 10;
            if (b instanceof javax.swing.border.EmptyBorder) {
                java.awt.Insets ins = ((javax.swing.border.EmptyBorder) b).getBorderInsets();
                top = ins.top; left = ins.left; bottom = ins.bottom;
            }
            threadItemsPanel.setBorder(new javax.swing.border.EmptyBorder(top, left, bottom, rightSidebarPad[0]));
        } catch (Exception ignore) {}
        
        mainContentPanel.add(leftPanel, BorderLayout.CENTER);
        mainContentPanel.add(rightPanel, BorderLayout.EAST);
        
        // 发帖按钮已悬浮显示，无需再添加底部面板
        
        threadListPanel.add(toolbarPanel, BorderLayout.NORTH);
        threadListPanel.add(mainContentPanel, BorderLayout.CENTER);
    }
    
    
    private JPanel createRightInfoPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(new Color(248, 249, 250));
        
        // 公告展示模块
        JPanel announcementPanel = createAnnouncementPanel();
        
        // 热门板块模块
        JPanel hotSectionsPanel = createHotSectionsPanel();
        
        rightPanel.add(announcementPanel, BorderLayout.NORTH);
        rightPanel.add(hotSectionsPanel, BorderLayout.CENTER);
        
        return rightPanel;
    }
    
    private JPanel createAnnouncementPanel() {
        // 外层留白：与右侧容器产生间距
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                int arc = 12;
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(0, 180));
        
        // 标题
        JLabel titleLabel = new JLabel("公告");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        // 公告内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        // 模拟公告数据
        String[] announcements = {
            "欢迎使用校园论坛系统！",
            "请遵守论坛规则，文明发言。",
            "期末考试安排已发布，请查看。",
            "校园活动报名开始，欢迎参与。"
        };
        
        for (String announcement : announcements) {
            JLabel announcementLabel = new JLabel("• " + announcement);
            announcementLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
            announcementLabel.setForeground(new Color(107, 114, 128));
            announcementLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            contentPanel.add(announcementLabel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        wrap.add(panel, BorderLayout.CENTER);
        return wrap;
    }
    
    private JPanel createHotSectionsPanel() {
        // 外层留白：分隔于其他区域
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                int arc = 12;
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(0, 260));
        
        // 标题
        JLabel titleLabel = new JLabel("热门板块");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        // 板块内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        // 模拟热门板块数据
        String[][] hotSections = {
            {"学习交流", "156"},
            {"校园生活", "89"},
            {"技术讨论", "67"},
            {"课程分享", "45"},
            {"活动信息", "32"}
        };
        
        for (String[] section : hotSections) {
            JPanel sectionPanel = new JPanel(new BorderLayout());
            sectionPanel.setBackground(new Color(255, 255, 255));
            sectionPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
            sectionPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel nameLabel = new JLabel(section[0]);
            nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
            nameLabel.setForeground(new Color(31, 41, 55));
            
            JLabel countLabel = new JLabel(section[1] + " 帖子");
            countLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
            countLabel.setForeground(new Color(107, 114, 128));
            
            sectionPanel.add(nameLabel, BorderLayout.WEST);
            sectionPanel.add(countLabel, BorderLayout.EAST);
            
            // 添加悬停效果
            sectionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    sectionPanel.setBackground(new Color(249, 250, 251));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    sectionPanel.setBackground(new Color(255, 255, 255));
                }
            });
            
            contentPanel.add(sectionPanel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        wrap.add(panel, BorderLayout.CENTER);
        return wrap;
    }
    
    private void createThreadDetailView() {
        threadDetailPanel = new JPanel(new BorderLayout());
        threadDetailPanel.setBackground(new Color(248, 249, 250));
        threadDetailPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // 顶部导航栏
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(255, 255, 255));
        navPanel.setBorder(null);
        navPanel.setPreferredSize(new Dimension(0, 60));
        
        backToListButton = createStyledButton("← 返回列表", new Color(107, 114, 128));
        backToListButton.addActionListener(e -> cardLayout.show(mainPanel, "LIST"));
        
        navPanel.add(backToListButton, BorderLayout.WEST);
        navPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // 帖子内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(null);
        
        // 帖子标题和元信息
        JPanel threadHeaderPanel = new JPanel(new BorderLayout());
        threadHeaderPanel.setBackground(new Color(255, 255, 255));
        threadHeaderPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        threadTitleLabel = new JLabel();
        // 详情标题不太黑不太粗
        threadTitleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 20f));
        threadTitleLabel.setForeground(new Color(55, 65, 81));
        threadTitleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        metaPanel.setBackground(new Color(255, 255, 255));
        
        threadAuthorLabel = new JLabel();
        threadAuthorLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadAuthorLabel.setForeground(new Color(156, 163, 175));
        
        threadTimeLabel = new JLabel();
        threadTimeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadTimeLabel.setForeground(new Color(156, 163, 175));
        
        threadReplyCountLabel = new JLabel();
        threadReplyCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadReplyCountLabel.setForeground(new Color(156, 163, 175));
        
        metaPanel.add(threadAuthorLabel);
        metaPanel.add(threadTimeLabel);
        metaPanel.add(threadReplyCountLabel);
        
        // 标签行
        threadTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        threadTagPanel.setOpaque(false);
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(threadTitleLabel);
        northStack.add(Box.createVerticalStrut(4));
        northStack.add(threadTagPanel);
        threadHeaderPanel.add(northStack, BorderLayout.NORTH);
        threadHeaderPanel.add(metaPanel, BorderLayout.SOUTH);

        // 操作区：点赞/收藏/分享
        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setOpaque(false);
        // 点赞使用资源图标（未点赞/已点赞）
        ImageIcon likeIcon = loadScaledIcon("icons/点赞.png", 16, 16);
        ImageIcon likedIcon = loadScaledIcon("icons/已点赞.png", 16, 16);
        likeToggle = new JToggleButton();
        likeToggle.setToolTipText("点赞");
        likeToggle.setIcon(likeIcon);
        if (likedIcon != null) likeToggle.setSelectedIcon(likedIcon);
        likeToggle.setFocusPainted(false);
        likeToggle.setBorderPainted(false);
        likeToggle.setContentAreaFilled(false);
        likeToggle.setOpaque(false);
        likeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 收藏与分享保持文字按钮风格，后续如需资源再替换
        favoriteToggle = createIconToggle("★", "收藏", new Color(234, 179, 8));
        shareButton = createIconButton("⤴", "分享", new Color(59, 130, 246));
        actionPanel.add(likeToggle);
        actionPanel.add(favoriteToggle);
        actionPanel.add(shareButton);
        JPanel headerSouth = new JPanel(new BorderLayout());
        headerSouth.setOpaque(false);
        headerSouth.add(metaPanel, BorderLayout.NORTH);
        headerSouth.add(actionPanel, BorderLayout.SOUTH);
        threadHeaderPanel.remove(metaPanel);
        threadHeaderPanel.add(headerSouth, BorderLayout.SOUTH);
        
        // 帖子内容
        threadContentArea = new JTextArea();
        threadContentArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 16f));
        threadContentArea.setForeground(new Color(31, 41, 55));
        threadContentArea.setLineWrap(true);
        threadContentArea.setWrapStyleWord(true);
        threadContentArea.setEditable(false);
        threadContentArea.setOpaque(false);
        threadContentArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 回复区域
        JPanel replySectionPanel = new JPanel(new BorderLayout());
        replySectionPanel.setBackground(new Color(255, 255, 255));
        replySectionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel replySectionTitle = new JLabel("回复");
        replySectionTitle.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        replySectionTitle.setForeground(new Color(31, 41, 55));
        
        replyListPanel = new JPanel();
        replyListPanel.setLayout(new BoxLayout(replyListPanel, BoxLayout.Y_AXIS));
        replyListPanel.setBackground(new Color(255, 255, 255));
        
        replyScrollPane = new JScrollPane(replyListPanel);
        replyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        replyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        replyScrollPane.setBorder(null);
        replyScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        replyScrollPane.setPreferredSize(new Dimension(0, 300));
        
        // 增加回复区域滑动灵敏度
        JScrollBar replyVerticalScrollBar = replyScrollPane.getVerticalScrollBar();
        replyVerticalScrollBar.setUnitIncrement(8); // 减少滚动单位，提高灵敏度
        replyVerticalScrollBar.setBlockIncrement(32); // 减少块滚动单位
        
        // 自定义回复区域滑动条样式
        customizeScrollBar(replyVerticalScrollBar);
        
        // 回复输入区域
        JPanel replyInputPanel = new JPanel(new BorderLayout());
        replyInputPanel.setBackground(new Color(248, 249, 250));
        replyInputPanel.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        
        replyTextArea = new JTextArea();
        replyTextArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 14f));
        replyTextArea.setLineWrap(true);
        replyTextArea.setWrapStyleWord(true);
        replyTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        replyTextArea.setRows(3);
        
        JPanel replyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        replyButtonPanel.setBackground(new Color(248, 249, 250));
        
        replyButton = createStyledButton("回复", new Color(59, 130, 246));
        replyButton.addActionListener(e -> submitReply());
        
        replyButtonPanel.add(replyButton);
        
        replyInputPanel.add(replyTextArea, BorderLayout.CENTER);
        replyInputPanel.add(replyButtonPanel, BorderLayout.SOUTH);
        
        replySectionPanel.add(replySectionTitle, BorderLayout.NORTH);
        replySectionPanel.add(replyScrollPane, BorderLayout.CENTER);
        replySectionPanel.add(replyInputPanel, BorderLayout.SOUTH);
        
        contentPanel.add(threadHeaderPanel, BorderLayout.NORTH);
        contentPanel.add(threadContentArea, BorderLayout.CENTER);
        contentPanel.add(replySectionPanel, BorderLayout.SOUTH);
        
        threadDetailPanel.add(navPanel, BorderLayout.NORTH);
        threadDetailPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createThreadDialog() {
        createThreadDialog = new JDialog((Frame) null, "发布新帖", true);
        createThreadDialog.setSize(680, 520);
        createThreadDialog.setLocationRelativeTo(null);
        createThreadDialog.setResizable(false);
        
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 顶部：分类下拉 + 标题输入
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        String[] categories = {"选择分类", "学术交流", "校园生活", "二手交易", "失物招领", "求助咨询"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setFont(UIManager.getFont("ComboBox.font").deriveFont(Font.PLAIN, 14f));
        categoryComboBox.setPreferredSize(new Dimension(160, 35));
        topPanel.add(categoryComboBox, BorderLayout.WEST);

        threadTitleField = new JTextField();
        threadTitleField.setFont(UIManager.getFont("TextField.font").deriveFont(Font.PLAIN, 14f));
        threadTitleField.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        threadTitleField.setPreferredSize(new Dimension(0, 35));
        threadTitleField.setToolTipText("请输入标题");
        topPanel.add(threadTitleField, BorderLayout.CENTER);

        // 中部：正文编辑 + 工具栏 + 计数
        JPanel centerPanel = new JPanel(new BorderLayout());
        threadContentField = new JTextArea();
        threadContentField.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 14f));
        threadContentField.setLineWrap(true);
        threadContentField.setWrapStyleWord(true);
        threadContentField.setBorder(new LineBorder(new Color(229, 231, 235), 1));

        // 底部工具栏：插入图片/附件 + 计数
        JPanel toolPanel = new JPanel(new BorderLayout());
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        insertImageButton = createStyledButton("插入图片", new Color(31, 41, 55));
        insertAttachmentButton = createStyledButton("插入附件", new Color(31, 41, 55));
        leftTools.setOpaque(false);
        // 置为浅色按钮风格
        insertImageButton.setBackground(new Color(243, 244, 246));
        insertImageButton.setForeground(new Color(55, 65, 81));
        insertAttachmentButton.setBackground(new Color(243, 244, 246));
        insertAttachmentButton.setForeground(new Color(55, 65, 81));
        leftTools.add(insertImageButton);
        leftTools.add(insertAttachmentButton);

        contentCounterLabel = new JLabel("0/500");
        contentCounterLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        contentCounterLabel.setForeground(new Color(107, 114, 128));
        JPanel counterWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        counterWrap.setOpaque(false);
        counterWrap.add(contentCounterLabel);

        toolPanel.add(leftTools, BorderLayout.WEST);
        toolPanel.add(counterWrap, BorderLayout.EAST);

        // 文本变化监听：限制500并更新计数
        threadContentField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void handle() {
                String text = threadContentField.getText();
                if (text.length() > 500) {
                    threadContentField.setText(text.substring(0, 500));
                }
                contentCounterLabel.setText(threadContentField.getText().length() + "/500");
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { handle(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { handle(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { handle(); }
        });

        centerPanel.add(new JScrollPane(threadContentField), BorderLayout.CENTER);
        centerPanel.add(toolPanel, BorderLayout.SOUTH);

        // 底部按钮：取消/发布帖子
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        cancelThreadButton = createStyledButton("取消", new Color(107, 114, 128));
        cancelThreadButton.addActionListener(e -> createThreadDialog.setVisible(false));
        submitThreadButton = createStyledButton("发布帖子", new Color(24, 121, 78));
        submitThreadButton.addActionListener(e -> submitThread());
        buttonPanel.add(cancelThreadButton);
        buttonPanel.add(submitThreadButton);

        dialogPanel.add(topPanel, BorderLayout.NORTH);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        createThreadDialog.add(dialogPanel);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 14f));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 35));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private JToggleButton createIconToggle(String iconText, String tooltip, Color activeColor) {
        JToggleButton toggle = new JToggleButton(iconText);
        toggle.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 16f));
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(false);
        toggle.setOpaque(false);
        toggle.setForeground(new Color(107, 114, 128));
        toggle.setToolTipText(tooltip);
        toggle.addChangeListener(e -> {
            if (toggle.isSelected()) {
                toggle.setForeground(activeColor);
            } else {
                toggle.setForeground(new Color(107, 114, 128));
            }
        });
        return toggle;
    }

    private JButton createIconButton(String iconText, String tooltip, Color color) {
        JButton button = new JButton(iconText);
        button.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 16f));
        button.setForeground(new Color(107, 114, 128));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setToolTipText(tooltip);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setForeground(color); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setForeground(new Color(107, 114, 128)); }
        });
        return button;
    }

    private JButton createCirclePlusButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int diameter = Math.min(getWidth(), getHeight());
                ButtonModel model = getModel();
                Color base = model.isRollover() ? new Color(19, 101, 65) : new Color(24, 121, 78); // 墨绿色/悬停更深

                // 圆形背景
                g2.setColor(base);
                g2.fillOval(0, 0, diameter, diameter);

                // 加号
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = diameter / 2;
                int cy = diameter / 2;
                int len = Math.round(diameter * 0.35f);
                g2.drawLine(cx - len / 2, cy, cx + len / 2, cy);
                g2.drawLine(cx, cy - len / 2, cx, cy + len / 2);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(56, 56));
        button.setSize(new Dimension(56, 56));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createCategoryButton(String text, boolean selected) {
        final Color lightGreen = new Color(223, 245, 232); // 浅绿色（悬浮）
        final Color inkGreen = new Color(24, 121, 78);     // 墨绿色（选中）

        JButton button = new JButton(text);
        button.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 14f));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 略微减小尺寸
        button.setPreferredSize(new Dimension(72, 34));
        button.setBorder(new EmptyBorder(0, 18, 0, 18));

        // 初始样式
        styleCategoryButton(button, selected);

        // 悬浮：未选中时浅绿色，选中时维持墨绿
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button != selectedCategoryButton) {
                    button.setBackground(lightGreen);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (button != selectedCategoryButton) {
                    button.setBackground(Color.WHITE);
                }
            }
        });

        return button;
    }

    private void styleCategoryButton(JButton button, boolean selected) {
        final Color inkGreen = new Color(24, 121, 78);
        if (selected) {
            button.setBackground(inkGreen);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(107, 114, 128));
        }
    }

    private void updateCategorySelection(JButton selected) {
        if (selected == null || selected == selectedCategoryButton) return;
        // 取消之前选中样式
        styleCategoryButton(selectedCategoryButton, false);
        // 应用当前选中样式
        styleCategoryButton(selected, true);
        selectedCategoryButton = selected;
        // 可按需触发重新加载/排序，这里仅刷新界面
        threadListPanel.revalidate();
        threadListPanel.repaint();
    }
    
    private void initMockData() {
        threads = new ArrayList<>();
        replies = new ArrayList<>();
        
        // 创建模拟帖子数据
        ThreadVO thread1 = new ThreadVO();
        thread1.setThreadId(1);
        thread1.setTitle("欢迎来到校园论坛！");
        thread1.setContent("欢迎大家使用校园论坛系统！这里可以分享学习心得、讨论课程内容、交流校园生活。希望大家能够文明发言，共同营造良好的讨论氛围。");
        thread1.setAuthorName("系统管理员");
        thread1.setAuthorLoginId("admin");
        thread1.setCreatedTime(new Timestamp(System.currentTimeMillis() - 86400000));
        thread1.setReplyCount(3);
        threads.add(thread1);
        
        ThreadVO thread2 = new ThreadVO();
        thread2.setThreadId(2);
        thread2.setTitle("Java编程学习心得分享");
        thread2.setContent("最近在学习Java编程，发现了一些不错的学习方法和资源。想和大家分享一下我的学习心得，希望对初学者有所帮助。\n\n1. 基础语法要扎实\n2. 多动手实践\n3. 阅读优秀的开源项目\n4. 参与社区讨论");
        thread2.setAuthorName("张三");
        thread2.setAuthorLoginId("2021001");
        thread2.setCreatedTime(new Timestamp(System.currentTimeMillis() - 172800000));
        thread2.setReplyCount(5);
        threads.add(thread2);
        
        ThreadVO thread3 = new ThreadVO();
        thread3.setThreadId(3);
        thread3.setTitle("校园食堂新菜品推荐");
        thread3.setContent("今天在食堂发现了几道新菜品，味道很不错！推荐给大家：\n\n1. 红烧肉 - 肥瘦相间，入口即化\n2. 宫保鸡丁 - 麻辣鲜香，很下饭\n3. 糖醋里脊 - 酸甜可口，老少皆宜\n\n大家还有什么好吃的推荐吗？");
        thread3.setAuthorName("李四");
        thread3.setAuthorLoginId("2021002");
        thread3.setCreatedTime(new Timestamp(System.currentTimeMillis() - 259200000));
        thread3.setReplyCount(8);
        threads.add(thread3);
        
        ThreadVO thread4 = new ThreadVO();
        thread4.setThreadId(4);
        thread4.setTitle("期末考试复习计划");
        thread4.setContent("期末考试快到了，想和大家讨论一下复习计划。我准备按照以下步骤进行：\n\n1. 整理各科重点知识点\n2. 制定每日复习计划\n3. 多做练习题\n4. 与同学互相讨论\n\n大家有什么好的复习方法吗？");
        thread4.setAuthorName("王五");
        thread4.setAuthorLoginId("2021003");
        thread4.setCreatedTime(new Timestamp(System.currentTimeMillis() - 345600000));
        thread4.setReplyCount(12);
        threads.add(thread4);
        
        ThreadVO thread5 = new ThreadVO();
        thread5.setThreadId(5);
        thread5.setTitle("校园社团活动招募");
        thread5.setContent("我们计算机社团正在招募新成员！如果你对编程、算法、人工智能等感兴趣，欢迎加入我们。\n\n社团活动包括：\n- 每周技术分享会\n- 编程竞赛训练\n- 项目开发实践\n- 企业参观交流\n\n有意向的同学请联系我！");
        thread5.setAuthorName("赵六");
        thread5.setAuthorLoginId("2021004");
        thread5.setCreatedTime(new Timestamp(System.currentTimeMillis() - 432000000));
        thread5.setReplyCount(6);
        threads.add(thread5);
        
        // 增加更多示例帖子以测试滚动效果
        ThreadVO thread6 = new ThreadVO();
        thread6.setThreadId(6);
        thread6.setTitle("数据结构与算法学习心得");
        thread6.setContent("最近在学习数据结构与算法，发现了一些重要的学习要点：\n\n1. 理解基本概念比死记硬背更重要\n2. 多画图理解算法流程\n3. 动手实现每个数据结构\n4. 多做练习题巩固知识\n\n大家有什么好的学习资源推荐吗？");
        thread6.setAuthorName("钱七");
        thread6.setAuthorLoginId("2021008");
        thread6.setCreatedTime(new Timestamp(System.currentTimeMillis() - 518400000));
        thread6.setReplyCount(9);
        threads.add(thread6);
        
        ThreadVO thread7 = new ThreadVO();
        thread7.setThreadId(7);
        thread7.setTitle("校园图书馆使用指南");
        thread7.setContent("图书馆是学习的好地方，这里分享一些使用技巧：\n\n1. 提前预约座位，避免排队\n2. 利用电子资源，查找学术论文\n3. 参加图书馆举办的讲座活动\n4. 合理利用自习室和讨论室\n\n希望大家都能充分利用图书馆资源！");
        thread7.setAuthorName("孙八");
        thread7.setAuthorLoginId("2021009");
        thread7.setCreatedTime(new Timestamp(System.currentTimeMillis() - 604800000));
        thread7.setReplyCount(4);
        threads.add(thread7);
        
        ThreadVO thread8 = new ThreadVO();
        thread8.setThreadId(8);
        thread8.setTitle("Python爬虫技术分享");
        thread8.setContent("最近在学习Python爬虫技术，发现了很多有趣的应用：\n\n1. 网页数据抓取\n2. 自动化数据收集\n3. 网站监控\n4. 价格比较工具\n\n需要注意的是要遵守网站的robots.txt规则，合理使用爬虫技术。");
        thread8.setAuthorName("周九");
        thread8.setAuthorLoginId("2021010");
        thread8.setCreatedTime(new Timestamp(System.currentTimeMillis() - 691200000));
        thread8.setReplyCount(7);
        threads.add(thread8);
        
        ThreadVO thread9 = new ThreadVO();
        thread9.setThreadId(9);
        thread9.setTitle("校园生活小贴士");
        thread9.setContent("分享一些校园生活的小贴士：\n\n1. 合理安排作息时间\n2. 多参加社团活动\n3. 与室友和谐相处\n4. 注意饮食健康\n5. 定期锻炼身体\n\n希望大家都能度过美好的大学生活！");
        thread9.setAuthorName("吴十");
        thread9.setAuthorLoginId("2021011");
        thread9.setCreatedTime(new Timestamp(System.currentTimeMillis() - 777600000));
        thread9.setReplyCount(11);
        threads.add(thread9);
        
        ThreadVO thread10 = new ThreadVO();
        thread10.setThreadId(10);
        thread10.setTitle("机器学习入门教程");
        thread10.setContent("机器学习是当前热门的技术领域，入门建议：\n\n1. 先学好数学基础（线性代数、概率论）\n2. 学习Python编程\n3. 了解常用的机器学习库（scikit-learn、tensorflow）\n4. 多做实际项目\n\n有同学一起学习吗？");
        thread10.setAuthorName("郑十一");
        thread10.setAuthorLoginId("2021012");
        thread10.setCreatedTime(new Timestamp(System.currentTimeMillis() - 864000000));
        thread10.setReplyCount(15);
        threads.add(thread10);
        
        ThreadVO thread11 = new ThreadVO();
        thread11.setThreadId(11);
        thread11.setTitle("数据库设计最佳实践");
        thread11.setContent("数据库设计是软件开发的重要环节：\n\n1. 合理设计表结构\n2. 选择合适的字段类型\n3. 建立适当的索引\n4. 考虑数据完整性约束\n5. 优化查询性能\n\n大家有什么数据库设计经验可以分享吗？");
        thread11.setAuthorName("王十二");
        thread11.setAuthorLoginId("2021013");
        thread11.setCreatedTime(new Timestamp(System.currentTimeMillis() - 950400000));
        thread11.setReplyCount(6);
        threads.add(thread11);
        
        ThreadVO thread12 = new ThreadVO();
        thread12.setThreadId(12);
        thread12.setTitle("前端开发技术栈推荐");
        thread12.setContent("前端开发技术更新很快，推荐一些主流技术：\n\n1. HTML5 + CSS3 基础\n2. JavaScript ES6+\n3. React/Vue.js 框架\n4. Webpack 打包工具\n5. Node.js 后端开发\n\n前端开发需要不断学习新技术，保持技术敏感度。");
        thread12.setAuthorName("李十三");
        thread12.setAuthorLoginId("2021014");
        thread12.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1036800000));
        thread12.setReplyCount(8);
        threads.add(thread12);
        
        ThreadVO thread13 = new ThreadVO();
        thread13.setThreadId(13);
        thread13.setTitle("校园网络安全意识");
        thread13.setContent("网络安全很重要，分享一些安全知识：\n\n1. 设置强密码\n2. 不点击可疑链接\n3. 定期更新软件\n4. 使用VPN保护隐私\n5. 备份重要数据\n\n希望大家都能提高网络安全意识！");
        thread13.setAuthorName("张十四");
        thread13.setAuthorLoginId("2021015");
        thread13.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1123200000));
        thread13.setReplyCount(3);
        threads.add(thread13);
        
        ThreadVO thread14 = new ThreadVO();
        thread14.setThreadId(14);
        thread14.setTitle("云计算技术发展趋势");
        thread14.setContent("云计算是未来IT发展的重要方向：\n\n1. 公有云、私有云、混合云\n2. 容器化技术（Docker、Kubernetes）\n3. 微服务架构\n4. DevOps 实践\n5. 边缘计算\n\n云计算技术正在改变传统的IT架构模式。");
        thread14.setAuthorName("刘十五");
        thread14.setAuthorLoginId("2021016");
        thread14.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1209600000));
        thread14.setReplyCount(5);
        threads.add(thread14);
        
        ThreadVO thread15 = new ThreadVO();
        thread15.setThreadId(15);
        thread15.setTitle("移动应用开发经验");
        thread15.setContent("移动应用开发需要注意的要点：\n\n1. 用户体验设计\n2. 性能优化\n3. 跨平台开发\n4. 安全考虑\n5. 版本管理\n\n移动应用市场竞争激烈，需要不断创新。");
        thread15.setAuthorName("陈十六");
        thread15.setAuthorLoginId("2021017");
        thread15.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1296000000));
        thread15.setReplyCount(7);
        threads.add(thread15);
        
        ThreadVO thread16 = new ThreadVO();
        thread16.setThreadId(16);
        thread16.setTitle("人工智能伦理思考");
        thread16.setContent("AI技术发展迅速，但也要考虑伦理问题：\n\n1. 算法偏见\n2. 隐私保护\n3. 就业影响\n4. 决策透明度\n5. 责任归属\n\n技术发展需要与伦理考量并重。");
        thread16.setAuthorName("杨十七");
        thread16.setAuthorLoginId("2021018");
        thread16.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1382400000));
        thread16.setReplyCount(12);
        threads.add(thread16);
        
        ThreadVO thread17 = new ThreadVO();
        thread17.setThreadId(17);
        thread17.setTitle("开源软件贡献指南");
        thread17.setContent("参与开源项目是提升技术的好方法：\n\n1. 选择合适的项目\n2. 阅读项目文档\n3. 从小问题开始\n4. 遵循代码规范\n5. 积极参与讨论\n\n开源社区需要大家的参与和贡献！");
        thread17.setAuthorName("黄十八");
        thread17.setAuthorLoginId("2021019");
        thread17.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1468800000));
        thread17.setReplyCount(9);
        threads.add(thread17);
        
        ThreadVO thread18 = new ThreadVO();
        thread18.setThreadId(18);
        thread18.setTitle("软件测试最佳实践");
        thread18.setContent("软件测试是保证质量的重要环节：\n\n1. 单元测试\n2. 集成测试\n3. 系统测试\n4. 自动化测试\n5. 性能测试\n\n好的测试策略能大大提高软件质量。");
        thread18.setAuthorName("赵十九");
        thread18.setAuthorLoginId("2021020");
        thread18.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1555200000));
        thread18.setReplyCount(6);
        threads.add(thread18);
        
        ThreadVO thread19 = new ThreadVO();
        thread19.setThreadId(19);
        thread19.setTitle("区块链技术应用前景");
        thread19.setContent("区块链技术有广阔的应用前景：\n\n1. 数字货币\n2. 供应链管理\n3. 身份认证\n4. 智能合约\n5. 去中心化应用\n\n区块链技术正在改变传统的信任机制。");
        thread19.setAuthorName("钱二十");
        thread19.setAuthorLoginId("2021021");
        thread19.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1641600000));
        thread19.setReplyCount(4);
        threads.add(thread19);
        
        ThreadVO thread20 = new ThreadVO();
        thread20.setThreadId(20);
        thread20.setTitle("职业规划与技能提升");
        thread20.setContent("IT行业职业规划建议：\n\n1. 明确职业目标\n2. 持续学习新技术\n3. 积累项目经验\n4. 建立人脉网络\n5. 关注行业趋势\n\n职业发展需要长期规划和持续努力。");
        thread20.setAuthorName("孙二一");
        thread20.setAuthorLoginId("2021022");
        thread20.setCreatedTime(new Timestamp(System.currentTimeMillis() - 1728000000));
        thread20.setReplyCount(13);
        threads.add(thread20);
        
        // 创建模拟回复数据
        PostVO reply1 = new PostVO();
        reply1.setPostId(1);
        reply1.setThreadId(1);
        reply1.setContent("感谢管理员！期待在这里和大家交流学习。");
        reply1.setAuthorName("学生A");
        reply1.setAuthorLoginId("2021005");
        reply1.setCreatedTime(new Timestamp(System.currentTimeMillis() - 82800000));
        replies.add(reply1);
        
        PostVO reply2 = new PostVO();
        reply2.setPostId(2);
        reply2.setThreadId(1);
        reply2.setContent("论坛界面很漂亮，使用体验很好！");
        reply2.setAuthorName("学生B");
        reply2.setAuthorLoginId("2021006");
        reply2.setCreatedTime(new Timestamp(System.currentTimeMillis() - 79200000));
        replies.add(reply2);
        
        PostVO reply3 = new PostVO();
        reply3.setPostId(3);
        reply3.setThreadId(2);
        reply3.setContent("感谢分享！我也是Java初学者，这些建议很有用。");
        reply3.setAuthorName("学生C");
        reply3.setAuthorLoginId("2021007");
        reply3.setCreatedTime(new Timestamp(System.currentTimeMillis() - 165600000));
        replies.add(reply3);
        
        refreshThreadList();
    }
    
    private void refreshThreadList() {
        JPanel threadItemsPanel = (JPanel) threadScrollPane.getViewport().getView();
        threadItemsPanel.removeAll();
        
        for (ThreadVO thread : threads) {
            JPanel threadItem = createThreadItem(thread);
            threadItemsPanel.add(threadItem);
            threadItemsPanel.add(Box.createVerticalStrut(12));
        }
        
        threadItemsPanel.revalidate();
        threadItemsPanel.repaint();
    }
    
    private JPanel createThreadItem(ThreadVO thread) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);
        itemPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 让卡片在滚动视图中横向占满：宽度填满，高度由内容自适应
        itemPanel.setMinimumSize(null);
        itemPanel.setPreferredSize(null);
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Color defaultBg = new Color(255, 255, 255);
        // 悬浮时背景：浅灰色
        final Color hoverBg = new Color(243, 244, 246);
        final Color[] currentBg = new Color[]{defaultBg};

        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBg[0]);
                int arc = 12;
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        // 内部内容由布局计算高度，横向可拉伸
        cardPanel.setMinimumSize(null);
        cardPanel.setPreferredSize(null);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        

        // 左上角头像
        CircularAvatar avatar = new CircularAvatar(48);
        Image img = loadResourceImage("icons/默认头像.png");
        if (img != null) avatar.setAvatarImage(img);
        avatar.setBorderWidth(0f);
        JPanel westWrap = new JPanel(new BorderLayout());
        westWrap.setOpaque(false);
        westWrap.setBorder(new EmptyBorder(0, 0, 0, 12));
        westWrap.add(avatar, BorderLayout.NORTH);

        // 顶部：姓名/院系在左，标签紧随其右侧
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel nameDept = new JPanel();
        nameDept.setOpaque(false);
        nameDept.setLayout(new BoxLayout(nameDept, BoxLayout.Y_AXIS));
        final JLabel nameLabel = new JLabel(thread.getAuthorName());
        // 姓名：加粗黑体，字号略大
        nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        nameLabel.setForeground(new Color(31, 41, 55));
        JLabel deptLabel = new JLabel("计算机科学与技术");
        // 专业：非常浅的较小字体
        deptLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        deptLabel.setForeground(new Color(156, 163, 175));
        nameDept.add(nameLabel);
        nameDept.add(deptLabel);

        final JLabel tag = createRoundedAnimatedTag(getThreadCategory(thread), 999, 280);

        // 行容器：左对齐紧凑排列，避免标签跑到整行最右
        JPanel nameAndTagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameAndTagRow.setOpaque(false);
        nameAndTagRow.add(nameDept);
        nameAndTagRow.add(tag);

        header.add(nameAndTagRow, BorderLayout.WEST);

        // 将帖子卡片的悬浮与标签颜色联动：平时浅绿，悬浮墨绿
        final Color tagBaseBg = new Color(223, 245, 232);
        final Color tagBaseFg = new Color(24, 121, 78);
        final Color tagHoverBg = new Color(24, 121, 78);
        final Color tagHoverFg = Color.WHITE;
        java.awt.event.MouseAdapter tagSync = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                try {
                    java.lang.reflect.Method m = tag.getClass().getDeclaredMethod("startAnim", Color.class, Color.class);
                    m.setAccessible(true);
                    m.invoke(tag, tagHoverBg, tagHoverFg);
                } catch (Exception ignored) {
                    tag.setBackground(tagHoverBg);
                    tag.setForeground(tagHoverFg);
                    tag.repaint();
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                try {
                    java.lang.reflect.Method m = tag.getClass().getDeclaredMethod("startAnim", Color.class, Color.class);
                    m.setAccessible(true);
                    m.invoke(tag, tagBaseBg, tagBaseFg);
                } catch (Exception ignored) {
                    tag.setBackground(tagBaseBg);
                    tag.setForeground(tagBaseFg);
                    tag.repaint();
                }
            }
        };
        installHoverListenerRecursive(cardPanel, tagSync);

        // 悬浮联动需要在 nameLabel 定义之后使用它
        java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                currentBg[0] = hoverBg;
                cardPanel.repaint();
                // 悬浮整卡片时，姓名改为墨绿色
                nameLabel.setForeground(new Color(24, 121, 78));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                currentBg[0] = defaultBg;
                cardPanel.repaint();
                // 离开时恢复姓名默认颜色
                nameLabel.setForeground(new Color(31, 41, 55));
            }
            public void mouseClicked(java.awt.event.MouseEvent e) { showThreadDetail(thread); }
        };
        // 递归安装悬浮监听，确保移动到子组件时不丢失“整体悬浮”效果
        installHoverListenerRecursive(cardPanel, hover);

        // 标题与摘要
        JLabel titleLabel = new JLabel(thread.getTitle());
        // 标题：不加粗，字号比姓名小一些；与专业之间留出更明显空隙
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        titleLabel.setForeground(new Color(55, 65, 81));
        // 与姓名/专业行之间 12px 间距；左侧对齐补齐 8px
        titleLabel.setBorder(new EmptyBorder(12, 8, 6, 0));
        JLabel summaryLabel = new JLabel("<html><div style='line-height:1.6;'>" + getContentSummary(thread.getContent(), 60) + "</div></html>");
        // 预展示内容：不加粗，字号小于标题略大于专业；灰色，颜色略深于专业
        summaryLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 13f));
        summaryLabel.setForeground(new Color(107, 114, 128));
        // 同步补齐左边距 8px，保证与标题、姓名/专业左侧齐平
        summaryLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
        // 左对齐并占满行宽
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 底部点赞与评论
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        footer.setOpaque(false);
        int likeCount = Math.max(0, (thread.getReplyCount() * 23) % 300);
        ImageIcon likeSmall = loadScaledIcon("icons/点赞.png", 16, 16);
        ImageIcon commentSmall = loadScaledIcon("icons/评论.png", 16, 16);
        JLabel likeLabel = new JLabel(" " + likeCount);
        // 底部信息：更小更灰更不显眼
        likeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        likeLabel.setForeground(new Color(156, 163, 175));
        if (likeSmall != null) likeLabel.setIcon(likeSmall);
        likeLabel.setIconTextGap(4);
        JLabel commentLabel = new JLabel(" " + thread.getReplyCount());
        commentLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        commentLabel.setForeground(new Color(156, 163, 175));
        if (commentSmall != null) commentLabel.setIcon(commentSmall);
        commentLabel.setIconTextGap(4);
        footer.add(likeLabel);
        footer.add(commentLabel);
        // 左对齐并占满行宽
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);
        centerStack.setAlignmentX(Component.LEFT_ALIGNMENT);
        // 最大宽度填充，避免被居中造成左侧空白
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getPreferredSize().height));
        summaryLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, summaryLabel.getPreferredSize().height));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footer.getPreferredSize().height));
        centerStack.add(header);
        centerStack.add(titleLabel);
        centerStack.add(summaryLabel);
        // 正文与点赞/评论区之间留 12px 间隔
        centerStack.add(Box.createVerticalStrut(12));
        centerStack.add(footer);

        cardPanel.add(westWrap, BorderLayout.WEST);
        cardPanel.add(centerStack, BorderLayout.CENTER);

        itemPanel.add(cardPanel, BorderLayout.CENTER);

        // 姓名悬浮主题色：墨绿色
        makeNameHoverGreen(nameLabel, new Color(55, 65, 81));
        return itemPanel;
    }

    /**
     * 为容器及其所有子组件安装同一个鼠标监听，保证“整体悬浮”在子组件上仍然生效。
     */
    private void installHoverListenerRecursive(java.awt.Component comp, java.awt.event.MouseListener listener) {
        if (comp == null || listener == null) return;
        comp.addMouseListener(listener);
        if (comp instanceof java.awt.Container) {
            java.awt.Component[] children = ((java.awt.Container) comp).getComponents();
            if (children != null) {
                for (java.awt.Component child : children) {
                    installHoverListenerRecursive(child, listener);
                }
            }
        }
    }

    private JLabel createTagLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        label.setForeground(Color.WHITE);
        Color base = new Color(24, 121, 78);
        Color hover = new Color(19, 101, 65);
        label.setOpaque(true);
        label.setBackground(base);
        label.setBorder(new EmptyBorder(2, 8, 2, 8));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { label.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { label.setBackground(base); }
        });
        return label;
    }

    private JLabel createRoundedAnimatedTag(String text, int cornerArc, int durationMs) {
        final Color baseBg = new Color(223, 245, 232);
        final Color baseFg = new Color(24, 121, 78);
        final Color hoverBg = new Color(24, 121, 78);
        final Color hoverFg = Color.WHITE;

        JLabel label = new JLabel(text) {
            private Color currentBg = baseBg;
            private Color currentFg = baseFg;
            private javax.swing.Timer animTimer;
            private long animStart;
            private Color fromBg, toBg, fromFg, toFg;
            private int animDuration = Math.max(120, durationMs);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = getHeight();
                int w = getWidth();
                int arc = Math.min(h, cornerArc); // 胶囊圆角
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }

            private void startAnim(Color tBg, Color tFg) {
                if (animTimer != null && animTimer.isRunning()) animTimer.stop();
                fromBg = currentBg; fromFg = currentFg;
                toBg = tBg; toFg = tFg;
                animStart = System.currentTimeMillis();
                animTimer = new javax.swing.Timer(15, e -> {
                    float t = (System.currentTimeMillis() - animStart) / (float) animDuration;
                    if (t >= 1f) { t = 1f; animTimer.stop(); }
                    currentBg = lerpColor(fromBg, toBg, t);
                    currentFg = lerpColor(fromFg, toFg, t);
                    setForeground(currentFg);
                    repaint();
                });
                animTimer.start();
            }
        };

        label.setOpaque(false);
        label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        label.setForeground(baseFg);
        label.setBorder(new EmptyBorder(3, 12, 3, 12));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(new Cursor(Cursor.HAND_CURSOR));
                ((JLabel) e.getSource()).setForeground(hoverFg);
                ((JLabel) e.getSource()).repaint();
                try { java.lang.reflect.Method m = e.getSource().getClass().getDeclaredMethod("startAnim", Color.class, Color.class); m.setAccessible(true); m.invoke(e.getSource(), hoverBg, hoverFg); } catch (Exception ignored) {}
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                try { java.lang.reflect.Method m = e.getSource().getClass().getDeclaredMethod("startAnim", Color.class, Color.class); m.setAccessible(true); m.invoke(e.getSource(), baseBg, baseFg); } catch (Exception ignored) {}
            }
        });
        return label;
    }

    private Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    private String getContentSummary(String content, int maxLen) {
        if (content == null) return "";
        String plain = content.replaceAll("\n", " ").trim();
        if (plain.length() <= maxLen) return plain;
        return plain.substring(0, maxLen) + "...";
    }

    private String getThreadCategory(ThreadVO t) {
        // 模拟发帖区域标签（无后端字段时）
        String[] categories = {"学术交流", "校园生活", "技术讨论", "课程分享", "资源推荐"};
        int idx = t.getThreadId() != null ? Math.abs(t.getThreadId()) % categories.length : 0;
        return categories[idx];
    }

    private Image loadResourceImage(String path) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path);
            return url != null ? new ImageIcon(url).getImage() : null;
        } catch (Exception ignored) { return null; }
    }
    
    /**
     * 让姓名标签在鼠标悬浮时变为主题色墨绿色，移出时恢复。
     */
    private void makeNameHoverGreen(JLabel label, Color defaultColor) {
        final Color hoverGreen = new Color(24, 121, 78);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { label.setForeground(hoverGreen); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { label.setForeground(defaultColor); }
        });
    }
    
    private void showThreadDetail(ThreadVO thread) {
        currentThread = thread;
        
        threadTitleLabel.setText(thread.getTitle());
        threadContentArea.setText(thread.getContent());
        threadAuthorLabel.setText("作者: " + thread.getAuthorName());
        threadTimeLabel.setText("时间: " + formatTime(thread.getCreatedTime()));
        threadReplyCountLabel.setText("回复数: " + thread.getReplyCount());
        
        refreshReplyList();
        
        cardLayout.show(mainPanel, "DETAIL");
    }
    
    private void refreshReplyList() {
        replyListPanel.removeAll();
        
        if (currentThread != null) {
            for (PostVO reply : replies) {
                if (reply.getThreadId().equals(currentThread.getThreadId())) {
                    JPanel replyItem = createReplyItem(reply);
                    replyListPanel.add(replyItem);
                    replyListPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        
        replyListPanel.revalidate();
        replyListPanel.repaint();
    }
    
    private JPanel createReplyItem(PostVO reply) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(new Color(255, 255, 255));
        itemPanel.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        // 自适应高度，避免底部点赞/回复被裁剪
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // 左侧头像：默认头像
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(new EmptyBorder(12, 12, 12, 0));
        CircularAvatar avatar = new CircularAvatar(36);
        Image aimg = loadResourceImage("icons/默认头像.png");
        if (aimg != null) avatar.setAvatarImage(aimg);
        avatar.setBorderWidth(0f);
        avatarWrap.add(avatar, BorderLayout.NORTH);

        // 右侧内容
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topLine = new JPanel(new BorderLayout());
        topLine.setOpaque(false);
        JLabel nameLabel = new JLabel(reply.getAuthorName());
        nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        nameLabel.setForeground(new Color(55, 65, 81));
        JLabel timeLabel = new JLabel(formatTime(reply.getCreatedTime()));
        timeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        timeLabel.setForeground(new Color(156, 163, 175));
        // 姓名悬浮主题色：墨绿色
        makeNameHoverGreen(nameLabel, new Color(55, 65, 81));
        topLine.add(nameLabel, BorderLayout.WEST);
        topLine.add(timeLabel, BorderLayout.EAST);

        JTextArea contentArea = new JTextArea(reply.getContent());
        contentArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 14f));
        contentArea.setForeground(new Color(31, 41, 55));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(4, 0, 6, 0));

        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        ops.setOpaque(false);
        // 回复项操作：点赞/回复图标
        ImageIcon likeIcon2 = loadScaledIcon("icons/点赞.png", 16, 16);
        ImageIcon likedIcon2 = loadScaledIcon("icons/已点赞.png", 16, 16);
        JToggleButton like = new JToggleButton();
        like.setToolTipText("赞");
        like.setIcon(likeIcon2);
        if (likedIcon2 != null) like.setSelectedIcon(likedIcon2);
        like.setFocusPainted(false);
        like.setBorderPainted(false);
        like.setContentAreaFilled(false);
        like.setOpaque(false);
        like.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon replyIcon = loadScaledIcon("icons/评论.png", 16, 16);
        JButton replyBtn = new JButton();
        replyBtn.setToolTipText("回复");
        replyBtn.setIcon(replyIcon);
        replyBtn.setFocusPainted(false);
        replyBtn.setBorderPainted(false);
        replyBtn.setContentAreaFilled(false);
        replyBtn.setOpaque(false);
        replyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton more = createIconButton("⋮", "更多", new Color(107, 114, 128));
        ops.add(like);
        ops.add(replyBtn);
        ops.add(more);

        right.add(topLine, BorderLayout.NORTH);
        right.add(contentArea, BorderLayout.CENTER);
        right.add(ops, BorderLayout.SOUTH);

        itemPanel.add(avatarWrap, BorderLayout.WEST);
        itemPanel.add(right, BorderLayout.CENTER);

        return itemPanel;
    }
    
    private void showCreateThreadDialog() {
        threadTitleField.setText("");
        threadContentField.setText("");
        if (categoryComboBox != null) { categoryComboBox.setSelectedIndex(0); }
        if (contentCounterLabel != null) { contentCounterLabel.setText("0/500"); }
        createThreadDialog.setVisible(true);
    }
    
    private void submitThread() {
        String title = threadTitleField.getText().trim();
        String content = threadContentField.getText().trim();
        String category = categoryComboBox != null ? (String) categoryComboBox.getSelectedItem() : null;
        
        if (categoryComboBox != null && (category == null || category.equals("选择分类"))) {
            JOptionPane.showMessageDialog(createThreadDialog, "请选择分类！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(createThreadDialog, "请填写标题和内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 创建新帖子
        ThreadVO newThread = new ThreadVO();
        newThread.setThreadId(threads.size() + 1);
        newThread.setTitle(title);
        newThread.setContent(content);
        newThread.setAuthorName(currentUser != null ? currentUser.getName() : "匿名用户");
        newThread.setAuthorLoginId(currentUser != null ? currentUser.getId() : "anonymous");
        newThread.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        newThread.setReplyCount(0);
        
        threads.add(0, newThread); // 添加到列表开头
        
        createThreadDialog.setVisible(false);
        refreshThreadList();
        
        JOptionPane.showMessageDialog(root, "帖子发布成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void submitReply() {
        String content = replyTextArea.getText().trim();
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(root, "请输入回复内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentThread == null) {
            JOptionPane.showMessageDialog(root, "请先选择一个帖子！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 创建新回复
        PostVO newReply = new PostVO();
        newReply.setPostId(replies.size() + 1);
        newReply.setThreadId(currentThread.getThreadId());
        newReply.setContent(content);
        newReply.setAuthorName(currentUser != null ? currentUser.getName() : "匿名用户");
        newReply.setAuthorLoginId(currentUser != null ? currentUser.getId() : "anonymous");
        newReply.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        
        replies.add(newReply);
        
        // 更新帖子回复数
        currentThread.setReplyCount(currentThread.getReplyCount() + 1);
        
        replyTextArea.setText("");
        refreshReplyList();
        
        JOptionPane.showMessageDialog(root, "回复发布成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "未知时间";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(timestamp);
    }
    
    /**
     * 自定义滑动条样式 - 现代化低调设计
     */
    private void customizeScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(209, 213, 219); // 浅灰色滑块
                this.trackColor = new Color(243, 244, 246); // 更浅的轨道色
                this.thumbDarkShadowColor = new Color(156, 163, 175); // 悬停时的深色
                this.thumbLightShadowColor = new Color(156, 163, 175);
                this.thumbHighlightColor = new Color(156, 163, 175);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollBar.isEnabled()) {
                    return;
                }
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 根据鼠标状态选择颜色
                Color thumbColor = this.thumbColor;
                if (isThumbRollover()) {
                    thumbColor = new Color(156, 163, 175); // 悬停时稍深
                }
                
                // 绘制圆角矩形滑块
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, 
                               thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
                
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制轨道
                g2.setColor(this.trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                
                g2.dispose();
            }
        });
        
        // 设置滑动条宽度
        scrollBar.setPreferredSize(new Dimension(8, 0));
    }

    @Override public String getKey() { return ModuleKeys.STUDENT_FORUM; }
    @Override public String getDisplayName() { return "论坛"; }
    @Override public String getIconPath() { return "icons/论坛.png"; }
    @Override public JComponent getComponent() { return root; }
    
    @Override 
    public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) { 
        this.currentUser = currentUser;
        this.isAdmin = currentUser != null && currentUser.isAdmin();
    }

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new StudentForumModule()); }
}
