package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import common.vo.ThreadVO;
import common.vo.PostVO;
import common.vo.UserVO;
import common.vo.ForumSectionVO;
import client.ui.dashboard.components.CircularAvatar;
import client.ui.dialog.CreateThreadDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** 教师版论坛模块。 */
public class TeacherForumModule implements IModuleView {
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
    private JLabel threadCategoryTag;
    private JTextArea threadContentArea;
    private JLabel threadAuthorLabel;
    private JLabel threadTimeLabel;
    private JLabel threadReplyCountLabel;
    private JLabel likeCountLabel; // 点赞数量标签
    private JPanel threadTagPanel;
    private JScrollPane replyScrollPane;
    private JPanel replyListPanel;
    private JTextArea replyTextArea;
    private JButton replyButton;
    private JButton backToListButton;
    private JLabel commentSectionTitle; // 评论区标题标签
    
    
    // 公告区域引用：用于动态刷新
    private JPanel announcementContentPanel;
    
    
    
    // 当前用户和管理员权限
    private UserVO currentUser;
    private boolean isAdmin = false;
    
    // 数据
    private List<ThreadVO> threads;
    private List<PostVO> replies;
    private List<ForumSectionVO> sections;
    private ThreadVO currentThread;
    // 当前热门板块筛选：使用分区ID（null 表示不过滤）
    private Integer currentSectionIdFilter;
    // 热门板块项引用与选中项
    private java.util.List<JPanel> hotSectionPanels;
    private JPanel selectedHotSectionPanel;
    // 热门板块内容容器（用于动态刷新）
    private JPanel hotSectionsContentPanel;
    // 发帖分区下拉的数据缓存
    private java.util.List<ForumSectionVO> comboSections;

    // 排序模式
    private enum SortMode { LATEST, HOT, ESSENCE }
    private SortMode currentSortMode = SortMode.LATEST;

    // 防止短时间内重复发送获取帖子请求
    private volatile boolean isFetchingThreads = false;
    // 避免重复初始化导致的重复首轮拉取
    private volatile boolean hasInitialized = false;
    // 刷新点击节流（间隔毫秒）
    private static final int REFRESH_CLICK_THROTTLE_MS = 500;
    private volatile long lastRefreshClickAtMs = 0L;

    // 搜索相关状态
    private boolean isSearchMode = false;        // 是否处于搜索模式
    private String currentSearchKeyword = null;  // 当前搜索关键词
    private List<ThreadVO> searchResults = new ArrayList<>(); // 搜索结果
    private JTextField searchFieldRef = null;    // 搜索框引用，用于清空文本

    public TeacherForumModule() { 
        // 先初始化数据容器，避免在构建UI过程中（如刷新下拉框）发生空指针
        threads = new ArrayList<>();
        replies = new ArrayList<>();
        sections = new ArrayList<>();
        comboSections = new ArrayList<>();
        buildUI(); 
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
        if (path == null || path.trim().isEmpty()) return null;
        final String normalized = path.replace('\\', '/');
        
        try {
            // 候选路径（类路径变体 + 资源目录前缀）
            String cp1 = normalized;
            String cp2 = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized;
            String cp3 = normalized.startsWith("resources/") ? normalized : ("resources/" + normalized);
            String cp4 = cp3.startsWith("/") ? cp3.substring(1) : "/" + cp3;
            
            String[] candidates = new String[] { cp1, cp2, cp3, cp4 };
            
            // 从当前线程的 ClassLoader 与类的 ClassLoader 依次尝试
            ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
            ClassLoader cl2 = getClass().getClassLoader();
            for (String p : candidates) {
                if (p == null || p.trim().isEmpty()) continue;
                try {
                    java.net.URL url = (cl1 != null ? cl1.getResource(p) : null);
                    if (url == null && cl2 != null) url = cl2.getResource(p);
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception ignored) { }
            }
            
            // 文件系统（相对/绝对路径）
            for (String p : new String[] { normalized, cp3 }) {
                try {
                    java.io.File file = new java.io.File(p);
                    if (file.exists()) {
                        Image img = new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.err.println("加载图标失败: " + normalized + " - " + e.getMessage());
        }
        return null;
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

        // 默认选中"最新"
        selectedCategoryButton = latestCategoryButton;

        // 点击切换选中状态并应用排序
        java.awt.event.ActionListener categoryClick = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                JButton src = (JButton) e.getSource();
                if (src == latestCategoryButton) {
                    currentSortMode = SortMode.LATEST;
                    System.out.println("[DEBUG][精华功能] 切换到最新模式");
                } else if (src == hotCategoryButton) {
                    currentSortMode = SortMode.HOT;
                    System.out.println("[DEBUG][精华功能] 切换到热门模式");
                } else if (src == essenceCategoryButton) {
                    currentSortMode = SortMode.ESSENCE;
                    System.out.println("[DEBUG][精华功能] *** 用户点击精华按钮 *** 切换到精华模式，开始筛选精华帖子");
                    System.out.println("[DEBUG][精华功能] 当前帖子总数: " + (threads != null ? threads.size() : 0));
                    if (threads != null) {
                        int essenceCount = 0;
                        for (ThreadVO t : threads) {
                            if (t.getIsEssence() != null && t.getIsEssence()) {
                                essenceCount++;
                            }
                        }
                        System.out.println("[DEBUG][精华功能] 可用精华帖子数量: " + essenceCount);
                    }
                }
                updateCategorySelection(src);
                refreshThreadList();
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

        // 无边框输入框，带占位符"搜索内容..."
        JTextField searchField = new JTextField();
        searchFieldRef = searchField; // 保存引用
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

        // 添加搜索功能：回车键触发搜索
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    String keyword = searchField.getText().trim();
                    if (!keyword.isEmpty() && !placeholder.equals(keyword)) {
                        performSearch(keyword);
                    }
                }
            }
        });
        
        // 搜索图标点击事件
        searchIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String keyword = searchField.getText().trim();
                if (!keyword.isEmpty() && !placeholder.equals(keyword)) {
                    performSearch(keyword);
                }
            }
        });

        searchBox.add(searchField, BorderLayout.CENTER);

        // 刷新图标按钮（使用资源图标，点击刷新）
        ImageIcon refreshIcon = loadScaledIcon("icons/刷新.png", 18, 18);
        refreshButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc2 = 16; // 圆角

                ButtonModel model = getModel();
                Color fill = new Color(255, 255, 255, 0); // 默认透明
                if (model.isPressed()) {
                    fill = new Color(210, 238, 224); // 按下更深的浅绿
                } else if (model.isRollover()) {
                    fill = new Color(223, 245, 232); // 悬浮浅绿
                }

                if (fill.getAlpha() > 0) {
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, arc2, arc2);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (refreshIcon != null) refreshButton.setIcon(refreshIcon);
        refreshButton.setHorizontalAlignment(SwingConstants.CENTER);
        refreshButton.setVerticalAlignment(SwingConstants.CENTER);
        refreshButton.setToolTipText("刷新");
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setOpaque(false);
        refreshButton.setRolloverEnabled(true);
        refreshButton.setPreferredSize(new Dimension(boxHeight, boxHeight));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            long now = System.currentTimeMillis();
            if (now - lastRefreshClickAtMs < REFRESH_CLICK_THROTTLE_MS) {
                System.out.println("[Forum][Client] 忽略刷新：点击过于频繁");
                return;
            }
            lastRefreshClickAtMs = now;
            System.out.println("[Forum][Client] 点击刷新按钮");
            
            // 如果处于搜索模式，退出搜索模式
            if (isSearchMode) {
                exitSearchMode();
            }
            
            // 刷新时回到列表视图，清除分区筛选，确保可见变化
            try {
                currentSectionIdFilter = null;
                if (cardLayout != null && mainPanel != null) {
                    cardLayout.show(mainPanel, "LIST");
                    // 强制完整重绘，避免列表显示问题
                    SwingUtilities.invokeLater(() -> {
                        threadListPanel.revalidate();
                        threadListPanel.repaint();
                    });
                }
            } catch (Exception ignore) {}
            // 同步刷新分区与帖子
            try { fetchSectionsFromServer(); } catch (Exception ignore) {}
            fetchThreadsFromServer();
        });

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
        // 视口尺寸变化时，同步子项宽度，保证横向始终铺满
        threadScrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                syncThreadItemsWidth();
            }
        });
        
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
        wrap.setOpaque(true);
        wrap.setBackground(new Color(248, 249, 250));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景，带阴影效果
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 16;
                int shadowOffset = 4;
                int shadowBlur = 8;
                
                // 绘制阴影
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(shadowOffset, shadowOffset, 
                    getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);
                
                // 绘制主体
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(0, 220));
        
        // 标题 - 带公告图标
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(15, 15, 10, 15));

        ImageIcon announcementIconImg = loadScaledIcon("icons/公告.png", 20, 20);
        JLabel announcementIcon = new JLabel(announcementIconImg);
        announcementIcon.setBorder(new EmptyBorder(0, 0, 0, 8));

        JLabel titleLabel = new JLabel("公告");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new Color(31, 41, 55));

        titlePanel.add(announcementIcon);
        titlePanel.add(titleLabel);
        
        // 公告内容：动态生成管理员发布的帖子标题
        announcementContentPanel = new JPanel();
        announcementContentPanel.setLayout(new BoxLayout(announcementContentPanel, BoxLayout.Y_AXIS));
        announcementContentPanel.setBackground(new Color(255, 255, 255));
        announcementContentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(announcementContentPanel, BorderLayout.CENTER);

        wrap.add(panel, BorderLayout.CENTER);
        return wrap;
    }

    // 依据 threads 刷新公告区域：仅显示管理员发帖（isAnnouncement=true）的标题
    private void refreshAnnouncements() {
        System.out.println("[DEBUG] ========== 开始刷新公告区域 ==========");
        if (announcementContentPanel == null) {
            System.out.println("[DEBUG] 公告面板为null，无法刷新");
            return;
        }
        announcementContentPanel.removeAll();
        int shown = 0;
        if (threads != null) {
            System.out.println("[DEBUG] 检查公告帖子，总帖子数: " + threads.size());
            for (ThreadVO t : threads) {
                if (t != null) {
                    System.out.println("[DEBUG] 检查帖子 - ID=" + t.getThreadId() + 
                                     ", 标题=" + t.getTitle() + 
                                     ", 是否公告=" + t.getIsAnnouncement());
                    if (t.getIsAnnouncement()) {
                        System.out.println("[DEBUG] 找到公告帖子，添加到公告区域: " + t.getTitle());
                        JLabel label = new JLabel("• " + (t.getTitle() != null ? t.getTitle() : "(无标题)"));
                        label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
                        label.setForeground(new Color(107, 114, 128));
                        label.setBorder(new EmptyBorder(4, 0, 4, 0));
                        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        // 悬停变色
                        label.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent evt) { label.setForeground(new Color(24, 121, 78)); }
                            public void mouseExited(java.awt.event.MouseEvent evt) { label.setForeground(new Color(107, 114, 128)); }
                            public void mouseClicked(java.awt.event.MouseEvent evt) { showThreadDetail(t); }
                        });
                        announcementContentPanel.add(label);
                        shown++;
                    }
                }
            }
        } else {
            System.out.println("[DEBUG] threads列表为null");
        }
        if (shown == 0) {
            System.out.println("[DEBUG] 没有找到公告帖子，显示'暂无公告'");
            JLabel empty = new JLabel("暂无公告");
            empty.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 13f));
            empty.setForeground(new Color(156, 163, 175));
            empty.setBorder(new EmptyBorder(4, 0, 4, 0));
            announcementContentPanel.add(empty);
        } else {
            System.out.println("[DEBUG] 公告区域刷新完成，显示公告数: " + shown);
        }
        announcementContentPanel.revalidate();
        announcementContentPanel.repaint();
    }
    
    private JPanel createHotSectionsPanel() {
        // 外层留白：分隔于其他区域
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(new Color(248, 249, 250));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景，带阴影效果
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 16;
                int shadowOffset = 4;
                int shadowBlur = 8;
                
                // 绘制阴影
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(shadowOffset, shadowOffset, 
                    getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);
                
                // 绘制主体
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(0, 260));
        
        // 标题 - 带火热图标
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        // 加载预售火热图标（类路径）
        ImageIcon fireIcon = loadScaledIcon("icons/预售火热.png", 20, 20);
        JLabel fireIconLabel = new JLabel(fireIcon);
        fireIconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        
        JLabel titleLabel = new JLabel("热门板块");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new Color(31, 41, 55));
        
        titlePanel.add(fireIconLabel);
        titlePanel.add(titleLabel);
        
        // 板块内容（动态）
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        hotSectionsContentPanel = contentPanel;
        
        // 选中管理
        final Color selectedBg = new Color(223, 245, 232); // 浅绿色
        hotSectionPanels = new java.util.ArrayList<JPanel>();

        // 初始化一次（空数据时显示提示）
        refreshHotSections();
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        wrap.add(panel, BorderLayout.CENTER);
        return wrap;
    }

    private void refreshHotSections() {
        if (hotSectionsContentPanel == null) return;
        hotSectionsContentPanel.removeAll();
        hotSectionPanels = new java.util.ArrayList<JPanel>();
        // 当有服务器分区列表时，按分区表显示全部分区；否则根据当前帖子聚合
        if (sections != null && !sections.isEmpty()) {
            for (ForumSectionVO sec : sections) {
                final Integer secId = sec.getSectionId();
                final String secName = sec.getName();
                JPanel sectionPanel = new JPanel(new BorderLayout());
                sectionPanel.setBackground(new Color(255, 255, 255));
                sectionPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
                sectionPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JLabel nameLabel = new JLabel(secName);
                nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
                nameLabel.setForeground(new Color(31, 41, 55));
                // 统计该分区的帖子数
                int count = 0;
                if (threads != null) {
                    for (ThreadVO t : threads) {
                        Integer sid = t != null ? t.getSectionId() : null;
                        if (sid != null && sid.equals(secId)) count++;
                    }
                }
                JLabel countLabel = new JLabel(count + " 帖子");
                countLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
                countLabel.setForeground(new Color(107, 114, 128));
                sectionPanel.add(nameLabel, BorderLayout.WEST);
                sectionPanel.add(countLabel, BorderLayout.EAST);
                sectionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                        currentSectionIdFilter = secId;
                        selectedHotSectionPanel = sectionPanel;
                        if (hotSectionPanels != null) {
                            final Color selectedBg = new Color(223, 245, 232);
                            for (JPanel p : hotSectionPanels) {
                                if (p == selectedHotSectionPanel) p.setBackground(selectedBg);
                                else p.setBackground(new Color(255, 255, 255));
                            }
                        }
                        refreshThreadList();
                        SwingUtilities.invokeLater(() -> {
                            JScrollBar bar = threadScrollPane.getVerticalScrollBar();
                            if (bar != null) bar.setValue(0);
                        });
                    }
                    @Override public void mouseEntered(java.awt.event.MouseEvent evt) {
                        // 悬浮：若非选中项，则显示浅绿色
                        if (sectionPanel != selectedHotSectionPanel) {
                            sectionPanel.setBackground(new Color(223, 245, 232));
                        }
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent evt) {
                        // 离开：若非选中项，恢复白色
                        if (sectionPanel != selectedHotSectionPanel) {
                            sectionPanel.setBackground(new Color(255, 255, 255));
                        }
                    }
                });
                hotSectionsContentPanel.add(sectionPanel);
                hotSectionPanels.add(sectionPanel);
            }
        } else {
            java.util.Map<String, Integer> sectionToCount = new java.util.LinkedHashMap<String, Integer>();
            if (threads != null) {
                for (ThreadVO t : threads) {
                    String name = getThreadSectionName(t);
                    if (name == null) name = "未分区";
                    sectionToCount.put(name, sectionToCount.getOrDefault(name, 0) + 1);
                }
            }
            if (sectionToCount.isEmpty()) {
                JLabel empty = new JLabel("暂无数据");
                empty.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 13f));
                empty.setForeground(new Color(156, 163, 175));
                empty.setBorder(new EmptyBorder(4, 0, 4, 0));
                hotSectionsContentPanel.add(empty);
            } else {
                java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<java.util.Map.Entry<String, Integer>>(sectionToCount.entrySet());
                java.util.Collections.sort(list, new java.util.Comparator<java.util.Map.Entry<String, Integer>>() {
                    @Override public int compare(java.util.Map.Entry<String, Integer> o1, java.util.Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                int limit = Math.min(8, list.size());
                final Color selectedBg = new Color(223, 245, 232);
                for (int i = 0; i < limit; i++) {
                    final String secName = list.get(i).getKey();
                    final int count = list.get(i).getValue();
                    JPanel sectionPanel = new JPanel(new BorderLayout());
                    sectionPanel.setBackground(new Color(255, 255, 255));
                    sectionPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
                    sectionPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    JLabel nameLabel = new JLabel(secName);
                    nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
                    nameLabel.setForeground(new Color(31, 41, 55));
                    JLabel countLabel = new JLabel(count + " 帖子");
                    countLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
                    countLabel.setForeground(new Color(107, 114, 128));
                    sectionPanel.add(nameLabel, BorderLayout.WEST);
                    sectionPanel.add(countLabel, BorderLayout.EAST);
                    sectionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                            // 旧数据模式：用名称筛选
                            currentSectionIdFilter = null;
                            // 为兼容旧逻辑，保留名称筛选通过 getThreadSectionName
                            // 刷新列表时将跳过ID筛选逻辑
                            selectedHotSectionPanel = sectionPanel;
                            for (JPanel p : hotSectionPanels) {
                                if (p == selectedHotSectionPanel) p.setBackground(selectedBg);
                                else p.setBackground(new Color(255, 255, 255));
                            }
                            refreshThreadList();
                            SwingUtilities.invokeLater(() -> {
                                JScrollBar bar = threadScrollPane.getVerticalScrollBar();
                                if (bar != null) bar.setValue(0);
                            });
                        }
                        @Override public void mouseEntered(java.awt.event.MouseEvent evt) {
                            if (sectionPanel != selectedHotSectionPanel) {
                                sectionPanel.setBackground(new Color(223, 245, 232));
                            }
                        }
                        @Override public void mouseExited(java.awt.event.MouseEvent evt) {
                            if (sectionPanel != selectedHotSectionPanel) {
                                sectionPanel.setBackground(new Color(255, 255, 255));
                            }
                        }
                    });
                    hotSectionsContentPanel.add(sectionPanel);
                    hotSectionPanels.add(sectionPanel);
                }
            }
        }
        hotSectionsContentPanel.revalidate();
        hotSectionsContentPanel.repaint();
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
        
        backToListButton = createBackButton("返回");
        backToListButton.addActionListener(e -> {
            // 清空回复文本框的状态和输入内容
            resetReplyInputState();
            // 返回主界面
            cardLayout.show(mainPanel, "LIST");
            
            // 强制完整重绘，避免列表显示问题
            SwingUtilities.invokeLater(() -> {
                threadListPanel.revalidate();
                threadListPanel.repaint();
            });
        });
        
        navPanel.add(backToListButton, BorderLayout.WEST);
        navPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // 主内容区域 - 采用卡片式布局
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(new Color(248, 249, 250));
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 第一部分：帖子内容卡片 - 根据内容自动调整大小
        JPanel postContentCard = createCardPanel();
        // 移除固定高度限制，让卡片根据内容自动调整
        postContentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // 帖子内容区域 - 可滚动但无滚动条
        JPanel postContentPanel = new JPanel(new BorderLayout());
        postContentPanel.setBackground(new Color(255, 255, 255));
        postContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 帖子头部信息：头像、姓名、发表时间
        JPanel postHeaderPanel = new JPanel(new BorderLayout());
        postHeaderPanel.setBackground(new Color(255, 255, 255));
        postHeaderPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // 第一行：头像 + 姓名和发表时间（确保在同一行）
        JPanel authorInfoPanel = new JPanel(new BorderLayout());
        authorInfoPanel.setOpaque(false);
        
        // 左侧头像
        CircularAvatar postAvatar = new CircularAvatar(50);
        Image postAvatarImg = loadResourceImage("icons/默认头像.png");
        if (postAvatarImg != null) postAvatar.setAvatarImage(postAvatarImg);
        postAvatar.setBorderWidth(0f);
        JPanel avatarContainer = new JPanel(new BorderLayout());
        avatarContainer.setOpaque(false);
        avatarContainer.setBorder(new EmptyBorder(0, 0, 0, 15));
        // 使用BorderLayout.CENTER确保头像垂直居中对齐
        avatarContainer.add(postAvatar, BorderLayout.CENTER);
        
        // 右侧姓名和发表时间 - 使用BorderLayout确保与头像垂直居中对齐
        JPanel nameTimePanel = new JPanel(new BorderLayout());
        nameTimePanel.setOpaque(false);
        
        threadAuthorLabel = new JLabel();
        threadAuthorLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 16f));
        threadAuthorLabel.setForeground(new Color(31, 41, 55));
        
        threadTimeLabel = new JLabel();
        threadTimeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadTimeLabel.setForeground(new Color(156, 163, 175));
        
        // 将姓名和时间放在一个垂直容器中，然后整体垂直居中
        JPanel nameTimeContainer = new JPanel();
        nameTimeContainer.setLayout(new BoxLayout(nameTimeContainer, BoxLayout.Y_AXIS));
        nameTimeContainer.setOpaque(false);
        nameTimeContainer.setBorder(new EmptyBorder(15, 0, 0, 0)); // 添加15px上边距，使姓名和发表日期进一步下移
        nameTimeContainer.add(threadAuthorLabel);
        nameTimeContainer.add(Box.createVerticalStrut(2));
        nameTimeContainer.add(threadTimeLabel);
        
        // 使用BorderLayout.CENTER确保内容垂直居中对齐
        // 这样可以让内容与头像垂直居中对齐
        nameTimePanel.add(nameTimeContainer, BorderLayout.CENTER);
        
        authorInfoPanel.add(avatarContainer, BorderLayout.WEST);
        authorInfoPanel.add(nameTimePanel, BorderLayout.CENTER);
        
        // 第二行：标题（与头像左侧对齐）
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.setBorder(new EmptyBorder(0, 0, 5, 0)); // 减少底部间距从10到5
        
        threadTitleLabel = new JLabel();
        // 标题样式：使用窗口标题栏颜色，不加粗，字体较大
        threadTitleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 20f));
        threadTitleLabel.setForeground(new Color(0x1B, 0x3A, 0x2A)); // 窗口标题栏颜色
        
        titleContainer.add(threadTitleLabel, BorderLayout.WEST);
        
        // 第三行：分区标签（在标题下方）
        JPanel sectionTagContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sectionTagContainer.setOpaque(false);
        sectionTagContainer.setBorder(new EmptyBorder(0, 0, 10, 0)); // 减少底部间距从15到10
        
        threadCategoryTag = createRoundedAnimatedTag("", 999, 240);
        sectionTagContainer.add(threadCategoryTag);
        
        // 第四行：帖子内容（与标题左侧对齐）- 自动调整高度
        threadContentArea = new JTextArea();
        threadContentArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 16f));
        threadContentArea.setForeground(new Color(31, 41, 55));
        threadContentArea.setLineWrap(true);
        threadContentArea.setWrapStyleWord(true);
        threadContentArea.setEditable(false);
        threadContentArea.setOpaque(false);
        threadContentArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // 设置文本区域根据内容自动调整高度
        threadContentArea.setRows(1); // 初始行数
        threadContentArea.setColumns(0); // 不限制列数，让宽度自适应
        
        // 组装帖子内容区域
        JPanel postContentContainer = new JPanel();
        postContentContainer.setLayout(new BoxLayout(postContentContainer, BoxLayout.Y_AXIS));
        postContentContainer.setOpaque(false);
        
        postContentContainer.add(authorInfoPanel);
        postContentContainer.add(Box.createVerticalStrut(8)); // 减少间距从10到8
        postContentContainer.add(titleContainer);
        postContentContainer.add(Box.createVerticalStrut(3)); // 减少间距从5到3
        postContentContainer.add(sectionTagContainer);
        postContentContainer.add(Box.createVerticalStrut(8)); // 减少间距从10到8
        postContentContainer.add(threadContentArea);
        
        // 在帖子内容最下方添加点赞和回复标识
        JPanel postStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        postStatsPanel.setBackground(new Color(255, 255, 255));
        postStatsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // 点赞按钮和数量
        JPanel likeStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likeStatsPanel.setOpaque(false);
        
        // 创建可点击的点赞按钮
        ImageIcon likeIcon = loadScaledIcon("icons/点赞.png", 20, 20);
        ImageIcon likedIcon = loadScaledIcon("icons/已点赞.png", 20, 20);
        JToggleButton likeButton = new JToggleButton();
        likeButton.setToolTipText("赞");
        likeButton.setIcon(likeIcon);
        if (likedIcon != null) likeButton.setSelectedIcon(likedIcon);
        likeButton.setFocusPainted(false);
        likeButton.setBorderPainted(false);
        likeButton.setContentAreaFilled(false);
        likeButton.setOpaque(false);
        likeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        likeCountLabel = new JLabel("0");
        likeCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        likeCountLabel.setForeground(new Color(107, 114, 128));
        
        // 添加点赞按钮事件监听器
        likeButton.addActionListener(e -> {
            if (currentThread != null) {
                toggleThreadLike(currentThread.getThreadId(), likeButton, likeCountLabel);
            }
        });
        
        likeStatsPanel.add(likeButton);
        likeStatsPanel.add(likeCountLabel);
        
        // 评论数量
        JPanel commentStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        commentStatsPanel.setOpaque(false);
        JLabel commentIconLabel = new JLabel();
        Image commentIcon = loadResourceImage("icons/评论.png");
        if (commentIcon != null) {
            commentIconLabel.setIcon(new ImageIcon(commentIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        }
        threadReplyCountLabel = new JLabel("0");
        threadReplyCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        threadReplyCountLabel.setForeground(new Color(107, 114, 128));
        commentStatsPanel.add(commentIconLabel);
        commentStatsPanel.add(threadReplyCountLabel);
        
        postStatsPanel.add(likeStatsPanel);
        postStatsPanel.add(commentStatsPanel);
        
        postContentContainer.add(postStatsPanel);
        
        postContentPanel.add(postContentContainer, BorderLayout.CENTER);
        
        // 将帖子内容面板添加到卡片中
        postContentCard.add(postContentPanel, BorderLayout.CENTER);
        
        // 第二部分：评论区卡片 - 固定区域（不带阴影）
        JPanel commentSectionCard = createCardPanelWithoutShadow();
        commentSectionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 700)); // 固定高度700px，增大评论区范围
        commentSectionCard.setPreferredSize(new Dimension(Integer.MAX_VALUE, 700));
        
        JPanel commentSectionPanel = new JPanel(new BorderLayout());
        commentSectionPanel.setBackground(new Color(255, 255, 255));
        commentSectionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 点赞和评论数量显示已移至帖子内容卡片底部
        
        // 第二行之后：评论区标题和评论列表
        JPanel commentContentPanel = new JPanel(new BorderLayout());
        commentContentPanel.setOpaque(true);
        commentContentPanel.setBackground(Color.WHITE);
        
        // 评论区标题
        commentSectionTitle = new JLabel("评论区");
        commentSectionTitle.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        commentSectionTitle.setForeground(new Color(31, 41, 55));
        commentSectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 可滚动的评论区
        replyListPanel = new JPanel();
        replyListPanel.setLayout(new BoxLayout(replyListPanel, BoxLayout.Y_AXIS));
        replyListPanel.setBackground(new Color(255, 255, 255));
        
        replyScrollPane = new JScrollPane(replyListPanel);
        replyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        replyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        replyScrollPane.setBorder(null);
        replyScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        
        // 增加回复区域滑动灵敏度
        JScrollBar replyVerticalScrollBar = replyScrollPane.getVerticalScrollBar();
        replyVerticalScrollBar.setUnitIncrement(8);
        replyVerticalScrollBar.setBlockIncrement(32);
        
        // 自定义回复区域滑动条样式
        customizeScrollBar(replyVerticalScrollBar);
        
        commentContentPanel.add(commentSectionTitle, BorderLayout.NORTH);
        commentContentPanel.add(replyScrollPane, BorderLayout.CENTER);
        
        commentSectionPanel.add(commentContentPanel, BorderLayout.CENTER);
        
        // 将评论区面板添加到卡片中
        commentSectionCard.add(commentSectionPanel, BorderLayout.CENTER);
        
        // 悬浮回复区卡片（不带阴影）
        JPanel floatingReplyCard = createCardPanelWithoutShadow();
        floatingReplyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // 固定高度120px
        floatingReplyCard.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // 悬浮回复区 - 使用左右分栏布局
        JPanel floatingReplyPanel = new JPanel(new BorderLayout());
        floatingReplyPanel.setBackground(new Color(248, 249, 250));
        // 去掉外边框，只保留内边距
        floatingReplyPanel.setBorder(new EmptyBorder(15, 0, 15, 20));
        
        // 左侧输入区域（占据大部分空间）
        JPanel leftInputPanel = new JPanel(new BorderLayout());
        leftInputPanel.setBackground(new Color(248, 249, 250));
        
        replyTextArea = new JTextArea();
        replyTextArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 14f));
        replyTextArea.setLineWrap(true);
        replyTextArea.setWrapStyleWord(true);
        replyTextArea.setRows(5); // 增加行数从3行到5行
        
        // 设置默认无边框样式
        replyTextArea.setBorder(new EmptyBorder(12, 20, 12, 12)); // 左边距20px，与卡片内边距对齐
        
        // 创建自定义样式的回复输入框，参考搜索框的实现方式
        final boolean[] replyHoverActive = new boolean[]{false};
        final boolean[] replyFocusActive = new boolean[]{false};
        
        // 包装回复输入框到一个自定义绘制的容器中
        JPanel replyContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // 悬停/聚焦时绘制绿色描边
                if (replyHoverActive[0] || replyFocusActive[0]) {
                    g2.setColor(new Color(76, 175, 80));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        replyContainer.setOpaque(false);
        replyContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // 设置回复输入框为透明背景
        replyTextArea.setOpaque(false);
        replyTextArea.setBorder(new EmptyBorder(12, 20, 12, 12));
        
        // 添加鼠标悬浮效果到容器
        java.awt.event.MouseAdapter replyHoverHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                replyHoverActive[0] = true;
                replyContainer.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                replyHoverActive[0] = false;
                replyContainer.repaint();
            }
        };
        replyContainer.addMouseListener(replyHoverHandler);
        replyTextArea.addMouseListener(replyHoverHandler);
        
        // 添加焦点变化效果
        replyTextArea.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                replyFocusActive[0] = true;
                replyContainer.repaint();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                replyFocusActive[0] = false;
                replyContainer.repaint();
            }
        });
        
        // 将回复输入框添加到容器中
        replyContainer.add(replyTextArea, BorderLayout.CENTER);
        
        leftInputPanel.add(replyContainer, BorderLayout.CENTER);
        
        // 右侧按钮区域（占据更小部分空间，按钮位于下方）
        JPanel rightButtonPanel = new JPanel(new BorderLayout());
        rightButtonPanel.setBackground(new Color(248, 249, 250));
        rightButtonPanel.setPreferredSize(new Dimension(90, 0)); // 减少固定宽度从100到90
        rightButtonPanel.setBorder(new EmptyBorder(0, 15, 0, 0)); // 左侧留出间距
        
        // 创建一个容器来将按钮定位到下方
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(new Color(248, 249, 250));
        buttonContainer.setPreferredSize(new Dimension(90, 100)); // 增加按钮容器高度从80到100，适应更大的文本区域
        
        replyButton = createStyledButton("回复", new Color(34, 139, 34)); // 墨绿色
        replyButton.addActionListener(e -> submitReply());
        replyButton.setPreferredSize(new Dimension(80, 35)); // 设置按钮大小
        
        // 将按钮放在容器下方
        buttonContainer.add(Box.createVerticalGlue(), BorderLayout.CENTER); // 上方弹性空间
        buttonContainer.add(replyButton, BorderLayout.SOUTH); // 按钮在下方
        
        rightButtonPanel.add(buttonContainer, BorderLayout.CENTER);
        
        // 将左右两部分添加到主面板
        floatingReplyPanel.add(leftInputPanel, BorderLayout.CENTER);
        floatingReplyPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        floatingReplyCard.add(floatingReplyPanel, BorderLayout.CENTER);
        
        // 组装主内容区域 - 卡片式布局
        mainContentPanel.add(postContentCard);
        mainContentPanel.add(Box.createVerticalStrut(10)); // 卡片间距，减少间距
        mainContentPanel.add(commentSectionCard);
        mainContentPanel.add(Box.createVerticalStrut(10)); // 卡片间距，减少间距
        mainContentPanel.add(floatingReplyCard);
        
        threadDetailPanel.add(navPanel, BorderLayout.NORTH);
        threadDetailPanel.add(mainContentPanel, BorderLayout.CENTER);

        // ESC 快捷键返回列表
        threadDetailPanel.registerKeyboardAction(
            e -> {
                cardLayout.show(mainPanel, "LIST");
                // 强制完整重绘，避免列表显示问题
                SwingUtilities.invokeLater(() -> {
                    threadListPanel.revalidate();
                    threadListPanel.repaint();
                });
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    // 创建带阴影的圆角卡片（用于帖子内容）
    private JPanel createCardPanel() {
        return createCardPanel(true);
    }
    
    // 创建不带阴影的圆角卡片（用于评论区和回复区）
    private JPanel createCardPanelWithoutShadow() {
        return createCardPanel(false);
    }
    
    private JPanel createCardPanel(boolean withShadow) {
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 16; // 增加圆角半径，使界面更时尚
                
                if (withShadow) {
                    int shadowHeight = 10; // 与AppBar相同的阴影高度
                    
                    // 绘制阴影 - 使用与AppBar相同的渐变效果
                    GradientPaint shadowPaint = new GradientPaint(
                        0, getHeight() - shadowHeight, new Color(0, 0, 0, 50), // 与AppBar相同的透明度
                        0, getHeight(), new Color(0, 0, 0, 0)
                    );
                    g2.setPaint(shadowPaint);
                    g2.fillRoundRect(0, getHeight() - shadowHeight, getWidth(), shadowHeight, arc, arc);
                    
                    // 绘制卡片背景
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() - shadowHeight, arc, arc);
                    
                    // 绘制边框
                    g2.setColor(new Color(229, 231, 235));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth(), getHeight() - shadowHeight, arc, arc);
                } else {
                    // 绘制卡片背景
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    
                    // 绘制边框
                    g2.setColor(new Color(229, 231, 235));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }
                
                g2.dispose();
            }
        };
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setOpaque(true);
        
        if (withShadow) {
            // 设置边框以提供阴影空间
            cardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // 底部留阴影空间
        } else {
            // 不带阴影的圆角卡片，不需要额外边框
            cardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        
        return cardPanel;
    }

    private JButton createBackButton(String text) {
        final Color borderColor = new Color(229, 231, 235);
        final Color fgDefault = new Color(55, 65, 81);
        final Color bgDefault = new Color(255, 255, 255);
        final Color fgHover = new Color(24, 121, 78);
        final Color bgHover = new Color(223, 245, 232);
        final Color bgPressed = new Color(210, 238, 224);

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc = Math.min(h, 20);

                ButtonModel model = getModel();
                Color fill = bgDefault;
                if (model.isPressed()) fill = bgPressed;
                else if (model.isRollover()) fill = bgHover;

                // 胶囊背景
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // 边框
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 14f));
        button.setForeground(fgDefault);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 36));
        button.setBorder(new EmptyBorder(0, 14, 0, 14));

        // 左侧返回箭头（Unicode），与文字留空隙
        button.setText("←  " + text);

        // 悬停前景色
        button.addChangeListener(e -> button.setForeground(button.getModel().isRollover() ? fgHover : fgDefault));

        return button;
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
        
        // 添加悬停和点击效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // 悬浮时颜色加深
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // 点击时颜色进一步加深
                button.setBackground(color.darker().darker());
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                // 释放时恢复悬浮状态
                button.setBackground(color.darker());
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
        
        // 添加发帖按钮点击事件
        button.addActionListener(e -> {
            showCreateThreadDialog();
        });
        
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
        // 可按需触发重新加载/排序
        refreshThreadList();
    }
    
    private void initMockData() { }
    
    private void refreshThreadList() {
        JPanel threadItemsPanel = (JPanel) threadScrollPane.getViewport().getView();
        if (threadItemsPanel == null) {
            System.out.println("[Forum][Client] 刷新列表时发现视图为空(view==null)");
            return;
        }
        System.out.println("[Forum][Client] 清空帖子列表并准备渲染，当前数据条数=" + (threads != null ? threads.size() : 0));
        System.out.println("[DEBUG] ========== 客户端开始刷新帖子列表 ==========");
        
        // 调试输出：检查接收到的所有帖子数据
        if (threads != null) {
            System.out.println("[DEBUG] 接收到的帖子总数: " + threads.size());
            for (ThreadVO thread : threads) {
                if (thread != null) {
                    System.out.println("[DEBUG] 帖子数据 - ID=" + thread.getThreadId() + 
                                     ", 标题=" + thread.getTitle() + 
                                     ", 作者=" + thread.getAuthorName() + 
                                     ", 是否公告=" + thread.getIsAnnouncement() + 
                                      ", 回复数=" + thread.getReplyCount() + 
                                      ", 分区ID=" + thread.getSectionId());
                }
            }
        }
        
        threadItemsPanel.removeAll();
        // 每次刷新先按当前模式排序
        sortThreads();
        
        int shownCount = 0;
        for (ThreadVO thread : threads) {
            // 若存在分区ID筛选，则仅显示匹配分区ID的帖子
            if (currentSectionIdFilter != null) {
                Integer sid = thread != null ? thread.getSectionId() : null;
                if (sid == null || !currentSectionIdFilter.equals(sid)) {
                    System.out.println("[DEBUG] 帖子ID=" + (thread != null ? thread.getThreadId() : "null") + " 被分区筛选过滤掉");
                    continue;
                }
            }
            
            // 精华模式筛选：只显示精华帖子
            if (currentSortMode == SortMode.ESSENCE) {
                boolean isEssence = thread.getIsEssence() != null && thread.getIsEssence();
                System.out.println("[DEBUG][精华功能] 精华模式筛选检查: 帖子ID=" + thread.getThreadId() + 
                                 ", 标题=" + thread.getTitle() + ", isEssence=" + isEssence);
                if (!isEssence) {
                    System.out.println("[DEBUG][精华功能] 非精华帖子被过滤掉，帖子ID=" + thread.getThreadId());
                    continue;
                }
                System.out.println("[DEBUG][精华功能] 精华帖子通过筛选，帖子ID=" + thread.getThreadId());
            }
            
            System.out.println("[DEBUG] 准备创建帖子项 - ID=" + thread.getThreadId() + 
                             ", 标题=" + thread.getTitle() + 
                             ", 是否公告=" + thread.getIsAnnouncement());
            
            JPanel threadItem = createThreadItem(thread);
            threadItemsPanel.add(threadItem);
            threadItemsPanel.add(Box.createVerticalStrut(12));
            shownCount++;
        }
        // 立即刷新布局，避免等待后延迟渲染
        threadItemsPanel.revalidate();
        threadItemsPanel.repaint();
        System.out.println("[Forum][Client] 列表渲染完成，显示条数=" + shownCount);
        
        // 同步每个子项宽度为可用区域宽度，避免任何情况下右侧出现空白
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { syncThreadItemsWidth(); }
        });
        // 同步刷新公告区域
        refreshAnnouncements();
    }

    /**
     * 将帖子列表所有子项的首选/最大宽度同步为视口可用宽度，确保横向铺满。
     */
    private void syncThreadItemsWidth() {
        if (threadScrollPane == null) return;
        java.awt.Component view = threadScrollPane.getViewport().getView();
        if (!(view instanceof JPanel)) return;
        JPanel threadItemsPanel = (JPanel) view;
        int availableWidth = Math.max(0, threadScrollPane.getViewport().getWidth());
        for (Component comp : threadItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                Dimension pref = comp.getPreferredSize();
                int prefHeight = pref != null ? pref.height : comp.getHeight();
                ((JPanel) comp).setMaximumSize(new Dimension(Integer.MAX_VALUE, prefHeight));
                ((JPanel) comp).setPreferredSize(new Dimension(availableWidth, prefHeight));
            }
        }
        threadItemsPanel.revalidate();
        threadItemsPanel.repaint();
    }

    private void fetchThreadsFromServer() {
        
        client.net.ServerConnection conn = this.connectionRef;
        // 并发/重复点击保护：若上一次请求仍在进行，直接忽略本次触发
        if (isFetchingThreads) {
            System.out.println("[Forum][Client] 忽略刷新：上一次请求仍在进行");
            return;
        }
        if (conn == null || !conn.isConnected()) {
            System.out.println("[Forum][Client] 刷新失败：未连接到服务器或连接对象为空");
            try {
                if (refreshButton != null) {
                    refreshButton.setEnabled(true);
                    refreshButton.setToolTipText("刷新");
                }
            } catch (Exception ignore) {}
            return;
        }
        
        // 刷新期间禁用按钮，避免重复请求
        try {
            if (refreshButton != null) {
                refreshButton.setEnabled(false);
                refreshButton.setToolTipText("正在刷新...");
            }
        } catch (Exception ignore) {}

        // 标记为进行中，避免重复发送
        isFetchingThreads = true;
        System.out.println("[Forum][Client] 发送获取帖子请求: GET_ALL_THREADS_REQUEST");

        // 超时保护：若 8 秒内未收到响应，自动恢复按钮状态，避免一直禁用
        final javax.swing.Timer timeoutTimer = new javax.swing.Timer(8000, new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    if (refreshButton != null) {
                        refreshButton.setEnabled(true);
                        refreshButton.setToolTipText("刷新");
                    }
                } catch (Exception ignore) {}
                // 超时后重置进行中标志，避免后续刷新被忽略
                isFetchingThreads = false;
            }
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();

        // 失败回调监听
        // 为避免监听器累积，先移除旧监听器（若存在）
        try { conn.removeMessageListener(common.protocol.MessageType.GET_ALL_THREADS_FAIL); } catch (Exception ignore) {}
        try { conn.removeMessageListener(common.protocol.MessageType.GET_ALL_THREADS_SUCCESS); } catch (Exception ignore) {}
        System.out.println("[Forum][Client] 注册响应监听器: SUCCESS/FAIL");
        conn.setMessageListener(common.protocol.MessageType.GET_ALL_THREADS_FAIL, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                try { if (timeoutTimer.isRunning()) timeoutTimer.stop(); } catch (Exception ignore) {}
                isFetchingThreads = false;
                System.out.println("[Forum][Client] 收到失败响应: GET_ALL_THREADS_FAIL, status=" + message.getStatusCode() + ", msg=" + message.getMessage());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        try {
                            if (refreshButton != null) {
                                refreshButton.setEnabled(true);
                                refreshButton.setToolTipText("刷新");
                            }
                        } catch (Exception ignore) {}
                    }
                });
                try { conn.removeMessageListener(common.protocol.MessageType.GET_ALL_THREADS_FAIL); } catch (Exception ignore) {}
            }
        });

        conn.setMessageListener(common.protocol.MessageType.GET_ALL_THREADS_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<common.vo.ThreadVO> list = (java.util.List<common.vo.ThreadVO>) message.getData();
                    System.out.println("[Forum][Client] 收到成功响应: GET_ALL_THREADS_SUCCESS, 条数=" + (list != null ? list.size() : -1));
                    System.out.println("[DEBUG] ========== 客户端接收到服务器数据 ==========");
                    
                    // 详细调试输出接收到的数据
                    if (list != null) {
                        System.out.println("[DEBUG] 接收到的ThreadVO列表大小: " + list.size());
                        for (ThreadVO vo : list) {
                            System.out.println("[DEBUG] 接收数据 - ID=" + vo.getThreadId() + 
                                             ", 标题=" + vo.getTitle() + 
                                             ", 作者=" + vo.getAuthorName() + 
                                             ", 是否公告=" + vo.getIsAnnouncement() + 
                                             ", 回复数=" + vo.getReplyCount() + 
                                             ", 分区ID=" + vo.getSectionId());
                        }
                    } else {
                        System.out.println("[DEBUG] 接收到的数据为null");
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            try { if (timeoutTimer.isRunning()) timeoutTimer.stop(); } catch (Exception ignore) {}
                            isFetchingThreads = false;
                            threads.clear();
                            if (list != null) threads.addAll(list);
                            System.out.println("[DEBUG] 数据已添加到本地threads列表，当前大小: " + threads.size());
                            refreshThreadList();
                            // 同步刷新热门板块
                            try { refreshHotSections(); } catch (Exception ignore) {}
                            // 回到列表顶部
                            try {
                                JScrollBar bar = threadScrollPane != null ? threadScrollPane.getVerticalScrollBar() : null;
                                if (bar != null) bar.setValue(0);
                            } catch (Exception ignore) {}
                            // 恢复按钮
                            try {
                                if (refreshButton != null) {
                                    refreshButton.setEnabled(true);
                                    refreshButton.setToolTipText("刷新");
                                }
                            } catch (Exception ignore) {}
                            System.out.println("[Forum][Client] 刷新流程完成");
                        }
                    });
                } catch (Exception e) {
                    System.out.println("[Forum][Client] 处理成功响应异常: " + e.getMessage());
                    try {
                        try { if (timeoutTimer.isRunning()) timeoutTimer.stop(); } catch (Exception ignore) {}
                        isFetchingThreads = false;
                        if (refreshButton != null) {
                            refreshButton.setEnabled(true);
                            refreshButton.setToolTipText("刷新");
                        }
                    } catch (Exception ignore) {}
                }
                // 移除本次监听器，避免占用
                try { conn.removeMessageListener(common.protocol.MessageType.GET_ALL_THREADS_SUCCESS); } catch (Exception ignore) {}
            }
        });
        
        boolean sent = conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.GET_ALL_THREADS_REQUEST));
        System.out.println("[Forum][Client] 请求发送结果 sent=" + sent);
        if (!sent) {
            try {
                try { if (timeoutTimer.isRunning()) timeoutTimer.stop(); } catch (Exception ignore) {}
                isFetchingThreads = false;
                if (refreshButton != null) {
                    refreshButton.setEnabled(true);
                    refreshButton.setToolTipText("刷新");
                }
            } catch (Exception ignore) {}
            System.out.println("[Forum][Client] 发送失败，已恢复按钮状态");
        }
    }
    
    private JPanel createThreadItem(ThreadVO thread) {
        System.out.println("[DEBUG] ========== 开始创建帖子项 ==========");
        System.out.println("[DEBUG] 帖子ID=" + thread.getThreadId() + 
                         ", 标题=" + thread.getTitle() + 
                         ", 作者=" + thread.getAuthorName() + 
                         ", 是否公告=" + thread.getIsAnnouncement() + 
                          ", 回复数=" + thread.getReplyCount());
        
        JPanel itemPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                // 横向尽可能填满，纵向不超过首选高度，避免被 BoxLayout 垂直拉伸
                return new Dimension(Integer.MAX_VALUE, pref != null ? pref.height : Integer.MAX_VALUE);
            }
        };
        itemPanel.setOpaque(false);
        // 增加边距以显示阴影效果
        itemPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 让卡片在滚动视图中横向占满：宽度填满，高度由内容自适应
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // 设置组件名称以便后续识别
        itemPanel.setName("thread_item_" + thread.getThreadId());

        final Color defaultBg = new Color(255, 255, 255);
        // 悬浮时背景：浅灰色
        final Color hoverBg = new Color(243, 244, 246);
        final Color[] currentBg = new Color[]{defaultBg};
        // 悬浮阴影标志
        final boolean[] hoverActive = new boolean[]{false};

        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int arc = 16;
                
                // 悬浮时绘制增强的多层阴影效果
                if (hoverActive[0]) {
                    // 绘制多层阴影，从外到内逐渐变淡
                    int shadowLayers = 15;
                    int maxOffset = 8;
                    for (int i = shadowLayers; i >= 1; i--) {
                        float alpha = 0.12f * (float)i / shadowLayers;
                        int offset = (int)(maxOffset * (float)i / shadowLayers);
                        g2.setColor(new Color(0f, 0f, 0f, Math.min(0.8f, alpha)));
                        g2.fillRoundRect(offset, offset, 
                                       Math.max(0, getWidth() - offset * 2), 
                                       Math.max(0, getHeight() - offset * 2), 
                                       arc, arc);
                    }
                }
                
                // 绘制主体背景
                g2.setColor(currentBg[0]);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                
                // 边框已移除
                
                g2.dispose();
            }
            
            @Override
            public boolean isOpaque() {
                return false; // 确保透明背景，让阴影可见
            }
            
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                // 横向尽可能填满，纵向不超过首选高度，避免在筛选后占满整个可视高度
                return new Dimension(Integer.MAX_VALUE, pref != null ? pref.height : Integer.MAX_VALUE);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        // 内部内容由布局计算高度，横向可拉伸
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

        // 第一行（右侧）：姓名（较大） + 发布时间（较小浅灰）上下结构
        final JLabel nameLabel = new JLabel(thread.getAuthorName());
        nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 16f));
        nameLabel.setForeground(new Color(55, 65, 81));
        JLabel timeMeta = new JLabel(formatTime(thread.getCreatedTime()));
        timeMeta.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        timeMeta.setForeground(new Color(156, 163, 175));

        JPanel nameTimeStack = new JPanel();
        nameTimeStack.setLayout(new BoxLayout(nameTimeStack, BoxLayout.Y_AXIS));
        nameTimeStack.setOpaque(false);
        nameTimeStack.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameTimeStack.add(nameLabel);
        nameTimeStack.add(Box.createVerticalStrut(2));
        nameTimeStack.add(timeMeta);

        // 第一行右端添加分类标签，悬浮整卡片时也变墨绿色
        JLabel categoryTag = createRoundedAnimatedTag(getThreadSectionName(thread), 999, 180);
        
        // 检查是否为精华帖子，如果是则添加精华图标
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightSection.setOpaque(false);
        
        boolean isEssencePost = thread.getIsEssence() != null && thread.getIsEssence();
        System.out.println("[DEBUG][精华功能] 帖子ID=" + thread.getThreadId() + 
                         ", 标题=" + thread.getTitle() + 
                         ", getIsEssence()=" + thread.getIsEssence() + 
                         ", isEssencePost=" + isEssencePost);
        
        if (isEssencePost) {
            System.out.println("[DEBUG][精华功能] *** 精华帖子确认 *** 正在为精华帖子添加图标，帖子ID=" + thread.getThreadId());
            
            // 检查图标文件是否存在
            java.io.File iconFile = new java.io.File("resources/icons/精华帖子.png");
            System.out.println("[DEBUG][精华功能] 检查图标文件: " + iconFile.getAbsolutePath() + ", 存在=" + iconFile.exists());
            
            // 尝试多个可能的路径
            String[] iconPaths = {
                "icons/精华帖子.png",
                "resources/icons/精华帖子.png",
                "/icons/精华帖子.png",
                "/resources/icons/精华帖子.png"
            };
            
            ImageIcon essenceIcon = null;
            String successPath = null;
            
            for (String path : iconPaths) {
                System.out.println("[DEBUG][精华功能] 尝试加载图标路径: " + path);
                essenceIcon = loadScaledIcon(path, 20, 20);
                if (essenceIcon != null) {
                    successPath = path;
                    System.out.println("[DEBUG][精华功能] 图标加载成功，路径: " + path);
                    break;
                }
            }
            
            if (essenceIcon != null) {
                System.out.println("[DEBUG][精华功能] *** 精华图标加载成功 *** 使用路径: " + successPath + ", 添加到界面");
                JLabel essenceLabel = new JLabel(essenceIcon);
                essenceLabel.setToolTipText("精华帖子");
                essenceLabel.setBorder(new EmptyBorder(0, 5, 0, 0)); // 添加一些间距
                rightSection.add(essenceLabel, 0); // 添加到最前面
                System.out.println("[DEBUG][精华功能] 精华图标已添加到 rightSection");
            } else {
                System.out.println("[ERROR][精华功能] *** 精华图标加载失败 *** 所有路径都尝试过了");
                // 作为备用，使用文字标签
                JLabel essenceTextLabel = new JLabel("★"); // 星星符号
                essenceTextLabel.setForeground(new Color(255, 215, 0)); // 金色
                essenceTextLabel.setFont(essenceTextLabel.getFont().deriveFont(Font.BOLD, 16f));
                essenceTextLabel.setToolTipText("精华帖子");
                essenceTextLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
                rightSection.add(essenceTextLabel, 0);
                System.out.println("[DEBUG][精华功能] 使用文字★作为精华标识");
            }
        } else {
            System.out.println("[DEBUG][精华功能] 非精华帖子，不添加图标 - 帖子ID=" + thread.getThreadId());
        }
        
        rightSection.add(categoryTag);

        JPanel firstLine = new JPanel(new BorderLayout());
        firstLine.setOpaque(false);
        firstLine.add(nameTimeStack, BorderLayout.WEST);
        firstLine.add(rightSection, BorderLayout.EAST);
        firstLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 第二行：标题（不加粗但较大），与第一行左端对齐，顶部留出适当空隙
        JLabel titleLabel = new JLabel(thread.getTitle());
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 16f));
        titleLabel.setForeground(new Color(55, 65, 81));
        titleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                System.out.println("[DEBUG] 鼠标进入帖子项，开始显示阴影效果");
                currentBg[0] = hoverBg;
                hoverActive[0] = true;
                cardPanel.repaint();
                // 悬浮整卡片时，作者名改为墨绿色
                nameLabel.setForeground(new Color(24, 121, 78));
                // 悬浮整卡片时，标签变墨绿色
                try {
                    java.lang.reflect.Method m = categoryTag.getClass().getDeclaredMethod("startAnim", Color.class, Color.class);
                    m.setAccessible(true);
                    m.invoke(categoryTag, new Color(24, 121, 78), Color.WHITE);
                } catch (Exception ignore) {}
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                System.out.println("[DEBUG] 鼠标离开帖子项，隐藏阴影效果");
                currentBg[0] = defaultBg;
                hoverActive[0] = false;
                cardPanel.repaint();
                // 离开时恢复作者名默认颜色
                nameLabel.setForeground(new Color(55, 65, 81));
                // 离开时标签恢复为浅绿色底、墨绿色字
                try {
                    java.lang.reflect.Method m = categoryTag.getClass().getDeclaredMethod("startAnim", Color.class, Color.class);
                    m.setAccessible(true);
                    m.invoke(categoryTag, new Color(223, 245, 232), new Color(24, 121, 78));
                } catch (Exception ignore) {}
            }
            public void mouseClicked(java.awt.event.MouseEvent e) { showThreadDetail(thread); }
        };
        // 递归安装悬浮监听，确保移动到子组件时不丢失"整体悬浮"效果
        installHoverListenerRecursive(cardPanel, hover);

        // 第三行：摘要（较小较灰），与标题左对齐，限制为单行显示避免挤压点赞回复区域
        String summaryText = getContentSummary(thread.getContent(), 40);
        JLabel summaryLabel = new JLabel("<html><div style='line-height:1.2; max-height: 1.2em; overflow: hidden; white-space: nowrap;'>" + summaryText + "</div></html>");
        summaryLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 13f));
        summaryLabel.setForeground(new Color(107, 114, 128));
        summaryLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // 限制摘要标签的最大高度为单行，确保点赞回复区域始终可见
        summaryLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); // 单行文本的高度

        // 点赞和回复数
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 0, 0)); // 确保无额外边距
        
        // 使用实际的点赞数而不是计算值
        int likeCount = thread.getLikeCount() != null ? thread.getLikeCount() : 0;
        System.out.println("[DEBUG] 帖子点赞数 - 实际值=" + likeCount);
        
        // 创建可点击的点赞按钮 - 增大图标尺寸
        ImageIcon likeIcon = loadScaledIcon("icons/点赞.png", 20, 20);
        ImageIcon likedIcon = loadScaledIcon("icons/已点赞.png", 20, 20);
        JToggleButton likeButton = new JToggleButton();
        likeButton.setToolTipText("赞");
        likeButton.setIcon(likeIcon);
        if (likedIcon != null) likeButton.setSelectedIcon(likedIcon);
        likeButton.setFocusPainted(false);
        likeButton.setBorderPainted(false);
        likeButton.setContentAreaFilled(false);
        likeButton.setOpaque(false);
        likeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 设置初始状态
        boolean isLiked = thread.getIsLiked() != null ? thread.getIsLiked() : false;
        likeButton.setSelected(isLiked);
        
        // 添加点赞数量标签
        JLabel likeCountLabel = new JLabel(" " + likeCount);
        likeCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        likeCountLabel.setForeground(new Color(156, 163, 175));
        
        // 创建点赞容器 - 确保左对齐
        JPanel likeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        likeContainer.setOpaque(false);
        likeContainer.setBorder(new EmptyBorder(0, -5, 0, 0)); // 向左移动点赞标识
        likeContainer.add(likeButton);
        likeContainer.add(likeCountLabel);
        
        // 添加点赞按钮事件监听器
        likeButton.addActionListener(e -> {
            toggleThreadLike(thread.getThreadId(), likeButton, likeCountLabel);
        });
        
        footer.add(likeContainer);
        
        // 添加回复数标识
        int replyCount = thread.getReplyCount() != null ? thread.getReplyCount() : 0;
        System.out.println("[DEBUG] 回复数 - 原始值=" + thread.getReplyCount() + ", 处理后=" + replyCount);
        
        // 增大回复图标尺寸
        ImageIcon replySmall = loadScaledIcon("icons/评论.png", 20, 20);
        JLabel replyLabel = new JLabel(" " + replyCount);
        replyLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        replyLabel.setForeground(new Color(156, 163, 175));
        // 设置回复标签名称以便后续更新
        replyLabel.setName("reply_count_" + thread.getThreadId());
        if (replySmall != null) {
            replyLabel.setIcon(replySmall);
            System.out.println("[DEBUG] 回复图标加载成功");
        } else {
            System.out.println("[DEBUG] 回复图标加载失败");
        }
        replyLabel.setIconTextGap(6); // 增大图标与文字间距
        replyLabel.setBorder(new EmptyBorder(0, 10, 0, 0)); // 在回复标识前添加间距，向左移动
        footer.add(replyLabel);
        
        System.out.println("[DEBUG] 点赞和回复标识创建完成 - 点赞数=" + likeCount + ", 回复数=" + replyCount);
        
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);
        centerStack.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 设置各组件的最大尺寸，确保布局稳定
        firstLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameTimeStack.getPreferredSize().height));
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getPreferredSize().height));
        // summaryLabel的最大高度已在上面设置
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footer.getPreferredSize().height));
        
        centerStack.add(firstLine);
        centerStack.add(titleLabel);
        centerStack.add(summaryLabel);
        // 正文与点赞/评论区之间留 8px 间隔（减少间隔，为点赞回复区域留出更多空间）
        centerStack.add(Box.createVerticalStrut(8));
        centerStack.add(footer);
        
        // 确保点赞回复区域始终可见，添加一个不可见的占位符
        centerStack.add(Box.createVerticalStrut(4));

        cardPanel.add(westWrap, BorderLayout.WEST);
        cardPanel.add(centerStack, BorderLayout.CENTER);

        itemPanel.add(cardPanel, BorderLayout.CENTER);

        // 姓名悬浮主题色：墨绿色
        makeNameHoverGreen(nameLabel, new Color(55, 65, 81));

        // 关键：限制垂直最大高度为其首选高度，防止在 BoxLayout(Y_AXIS) 下被拉伸占满
        // 同时保持横向最大宽度填充，确保左右填充一致 [[memory:8117340]]
        // 确保最小高度足够显示点赞回复区域
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Dimension cardPref = cardPanel.getPreferredSize();
                if (cardPref != null) {
                    // 确保最小高度为120px，足够显示所有内容
                    int minHeight = Math.max(120, cardPref.height);
                    cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, minHeight));
                    cardPanel.setMinimumSize(new Dimension(0, minHeight));
                }
                Dimension itemPref = itemPanel.getPreferredSize();
                if (itemPref != null) {
                    int minHeight = Math.max(120, itemPref.height);
                    itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, minHeight));
                    itemPanel.setMinimumSize(new Dimension(0, minHeight));
                }
            }
        });

        return itemPanel;
    }

    /**
     * 为容器及其所有子组件安装同一个鼠标监听，保证"整体悬浮"在子组件上仍然生效。
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

    private void sortThreads() {
        if (threads == null || threads.size() <= 1) return;
        java.util.Collections.sort(threads, new java.util.Comparator<ThreadVO>() {
            @Override public int compare(ThreadVO a, ThreadVO b) {
                if (a == b) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                switch (currentSortMode) {
                    case ESSENCE: {
                        // 精华模式：优先显示精华帖子，然后按创建时间倒序
                        boolean aIsEssence = a.getIsEssence() != null && a.getIsEssence();
                        boolean bIsEssence = b.getIsEssence() != null && b.getIsEssence();
                        
                        // 精华帖子排在前面
                        if (aIsEssence && !bIsEssence) return -1;
                        if (!aIsEssence && bIsEssence) return 1;
                        
                        // 两个都是精华或都不是精华时，按创建时间倒序
                        java.sql.Timestamp ca = a.getCreatedTime();
                        java.sql.Timestamp cb = b.getCreatedTime();
                        if (ca == null && cb == null) break;
                        if (ca == null) return 1;
                        if (cb == null) return -1;
                        return Long.compare(cb.getTime(), ca.getTime());
                    }
                    case HOT: {
                        int ra = a.getReplyCount() != null ? a.getReplyCount() : 0;
                        int rb = b.getReplyCount() != null ? b.getReplyCount() : 0;
                        int c = Integer.compare(rb, ra); // 回复数降序
                        if (c != 0) return c;
                        // 次级：按创建时间倒序
                        java.sql.Timestamp ca = a.getCreatedTime();
                        java.sql.Timestamp cb = b.getCreatedTime();
                        if (ca == null && cb == null) break;
                        if (ca == null) return 1;
                        if (cb == null) return -1;
                        return Long.compare(cb.getTime(), ca.getTime());
                    }
                    case LATEST:
                    default: {
                        // 最新：按创建时间倒序（发表时间）
                        java.sql.Timestamp ca = a.getCreatedTime();
                        java.sql.Timestamp cb = b.getCreatedTime();
                        if (ca == null && cb == null) break;
                        if (ca == null) return 1;
                        if (cb == null) return -1;
                        int c = Long.compare(cb.getTime(), ca.getTime());
                        if (c != 0) return c;
                        // 次级：按更新时间倒序
                        java.sql.Timestamp ua = a.getUpdatedTime();
                        java.sql.Timestamp ub = b.getUpdatedTime();
                        if (ua == null && ub == null) break;
                        if (ua == null) return 1;
                        if (ub == null) return -1;
                        return Long.compare(ub.getTime(), ua.getTime());
                    }
                }
                return 0;
            }
        });
    }

    private String getContentSummary(String content, int maxLen) {
        if (content == null) return "";
        String plain = content.replaceAll("\n", " ").trim();
        if (plain.length() <= maxLen) return plain;
        
        // 对于中文字符，适当减少字符数以确保单行显示
        String result = plain.substring(0, maxLen);
        // 如果截断位置是中文，尝试向前调整到合适的位置
        if (maxLen < plain.length() && isChineseChar(result.charAt(result.length() - 1))) {
            // 向前查找非中文字符作为截断点
            for (int i = result.length() - 1; i >= 0; i--) {
                if (!isChineseChar(result.charAt(i))) {
                    result = result.substring(0, i + 1);
                    break;
                }
            }
        }
        return result + "...";
    }
    
    private boolean isChineseChar(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }

    private String getThreadSectionName(ThreadVO t) {
        if (t == null) return null;
        if (t.getSectionName() != null && !t.getSectionName().trim().isEmpty()) return t.getSectionName();
        return "未分区";
    }

    private Image loadResourceImage(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        final String normalized = path.replace('\\', '/');
        try {
            // 候选路径（类路径变体 + 资源目录前缀 + 绝对/相对文件路径）
            String cp1 = normalized;
            String cp2 = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized; // 处理带/或不带/两种写法
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
                    if (url != null) {
                        return new ImageIcon(url).getImage();
                    }
                } catch (Exception ignored) { }
            }

            // 2) 文件系统（相对/绝对路径）
            for (String p : new String[] { normalized, cp3 }) {
                try {
                    java.io.File file = new java.io.File(p);
                    if (file.exists()) {
                        return new ImageIcon(file.getAbsolutePath()).getImage();
                    }
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.err.println("加载图片失败: " + normalized + " - " + e.getMessage());
        }
        return null;
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
        
        // 设置帖子标题
        threadTitleLabel.setText(thread.getTitle());
        
        // 设置分区标签
        if (threadCategoryTag != null) {
            String sectionName = getThreadSectionName(thread);
            threadCategoryTag.setText(sectionName);
        }
        
        // 设置帖子内容并自动调整高度
        threadContentArea.setText(thread.getContent());
        adjustTextAreaHeight(threadContentArea);
        
        // 设置作者姓名（不显示"作者:"前缀）
        threadAuthorLabel.setText(thread.getAuthorName());
        
        // 设置发表时间（不显示"时间:"前缀）
        threadTimeLabel.setText(formatTime(thread.getCreatedTime()));
        
        // 更新点赞和评论数量显示
        updateStatsDisplay(thread);
        
        // 更新评论区标题显示回复数量
        updateCommentSectionTitle(thread);
        
        // 获取并显示回复列表
        fetchPostsFromServer(thread.getThreadId());
        
        // 切换到详情视图
        cardLayout.show(mainPanel, "DETAIL");
        
        // 强制完整重绘，避免评论区空白问题
        SwingUtilities.invokeLater(() -> {
            threadDetailPanel.revalidate();
            threadDetailPanel.repaint();
        });
    }
    
    private void updateStatsDisplay(ThreadVO thread) {
        // 更新评论数量显示
        if (threadReplyCountLabel != null) {
            threadReplyCountLabel.setText(String.valueOf(thread.getReplyCount() != null ? thread.getReplyCount() : 0));
        }
        
        // 更新点赞数量显示
        if (likeCountLabel != null) {
            likeCountLabel.setText(String.valueOf(thread.getLikeCount() != null ? thread.getLikeCount() : 0));
        }
        
        // 更新点赞按钮状态
        updateLikeButtonState(thread);
    }
    
    /**
     * 更新点赞按钮状态
     * @param thread 当前帖子对象
     */
    private void updateLikeButtonState(ThreadVO thread) {
        // 查找帖子详情页面的点赞按钮
        JToggleButton likeButton = findLikeButtonInDetailPanel();
        if (likeButton != null) {
            boolean isLiked = thread.getIsLiked() != null ? thread.getIsLiked() : false;
            likeButton.setSelected(isLiked);
        }
    }
    
    /**
     * 在帖子详情面板中查找点赞按钮
     * @return 点赞按钮，如果找不到返回null
     */
    private JToggleButton findLikeButtonInDetailPanel() {
        if (threadDetailPanel == null) return null;
        
        // 递归查找点赞按钮
        return findLikeButtonRecursive(threadDetailPanel);
    }
    
    /**
     * 递归查找点赞按钮
     * @param component 要搜索的组件
     * @return 点赞按钮，如果找不到返回null
     */
    private JToggleButton findLikeButtonRecursive(Component component) {
        if (component instanceof JToggleButton) {
            JToggleButton button = (JToggleButton) component;
            if ("赞".equals(button.getToolTipText())) {
                return button;
            }
        }
        
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                JToggleButton result = findLikeButtonRecursive(child);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 同步更新帖子列表页面的点赞状态
     * @param threadId 帖子ID
     * @param isLiked 是否已点赞
     * @param likeCount 点赞数量
     */
    private void syncThreadLikeState(Integer threadId, Boolean isLiked, Integer likeCount) {
        // 更新当前帖子对象的状态
        if (currentThread != null && currentThread.getThreadId().equals(threadId)) {
            currentThread.setIsLiked(isLiked);
            currentThread.setLikeCount(likeCount);
        }
        
        // 更新帖子列表中的对应帖子状态
        if (threads != null) {
            for (ThreadVO thread : threads) {
                if (thread.getThreadId().equals(threadId)) {
                    thread.setIsLiked(isLiked);
                    thread.setLikeCount(likeCount);
                    break;
                }
            }
        }
        
        // 刷新帖子列表显示
        refreshThreadList();
    }
    
    /**
     * 更新评论区标题，显示回复数量
     * @param thread 当前帖子对象
     */
    private void updateCommentSectionTitle(ThreadVO thread) {
        if (commentSectionTitle != null) {
            int replyCount = thread.getReplyCount() != null ? thread.getReplyCount() : 0;
            commentSectionTitle.setText("评论区(" + replyCount + ")");
        }
    }
    
    /**
     * 更新帖子列表中指定帖子的信息（如回复数量）
     * @param thread 要更新的帖子对象
     */
    private void updateThreadInList(ThreadVO thread) {
        if (thread == null || thread.getThreadId() == null) {
            return;
        }
        
        // 获取帖子列表面板
        JPanel threadItemsPanel = (JPanel) threadScrollPane.getViewport().getView();
        if (threadItemsPanel == null) {
            return;
        }
        
        // 遍历所有帖子项，找到对应的帖子并更新其回复数量显示
        Component[] components = threadItemsPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                // 检查这个组件是否对应我们要更新的帖子
                String itemName = itemPanel.getName();
                if (itemName != null && itemName.equals("thread_item_" + thread.getThreadId())) {
                    updateThreadItemReplyCount(itemPanel, thread);
                    break;
                }
            }
        }
    }
    
    /**
     * 更新帖子项中的回复数量显示
     * @param itemPanel 帖子项面板
     * @param thread 帖子对象
     */
    private void updateThreadItemReplyCount(JPanel itemPanel, ThreadVO thread) {
        int replyCount = thread.getReplyCount() != null ? thread.getReplyCount() : 0;
        
        // 递归查找并更新回复数量标签
        updateReplyCountLabelInComponent(itemPanel, thread.getThreadId(), replyCount);
        
        // 重新绘制组件
        itemPanel.revalidate();
        itemPanel.repaint();
    }
    
    /**
     * 在组件中递归查找并更新回复数量标签
     * @param component 要搜索的组件
     * @param threadId 帖子ID
     * @param replyCount 新的回复数量
     */
    private void updateReplyCountLabelInComponent(Component component, Integer threadId, int replyCount) {
        if (component == null) {
            return;
        }
        
        // 检查是否是回复数量标签
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            String labelName = label.getName();
            // 如果标签名称匹配回复数量标签的命名规则
            if (labelName != null && labelName.equals("reply_count_" + threadId)) {
                label.setText(" " + replyCount);
                return;
            }
        }
        
        // 如果是容器，递归检查子组件
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                updateReplyCountLabelInComponent(child, threadId, replyCount);
            }
        }
    }
    
    private void refreshReplyList() {
        replyListPanel.removeAll();
        
        if (currentThread != null) {
            for (PostVO reply : replies) {
                if (reply.getThreadId().equals(currentThread.getThreadId())) {
                    JPanel replyItem = createReplyItem(reply);
                    
                    // 为每条评论项添加顶部灰色分割线和底部灰色分割线
                    replyItem.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)), // 浅灰色顶部边框
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235))  // 浅灰色底部边框
                        ),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                    ));
                    
                    replyListPanel.add(replyItem);
                }
            }
        }
        
        replyListPanel.revalidate();
        replyListPanel.repaint();
    }

    private void fetchPostsFromServer(Integer threadId) {
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            JOptionPane.showMessageDialog(root, "未连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        conn.setMessageListener(common.protocol.MessageType.GET_POSTS_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<common.vo.PostVO> list = (java.util.List<common.vo.PostVO>) message.getData();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            replies.clear();
                            if (list != null) replies.addAll(list);
                            refreshReplyList();
                        }
                    });
                } catch (Exception ignored) {}
            }
        });
        conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.GET_POSTS_REQUEST, threadId));
    }
    
    private JPanel createReplyItem(PostVO reply) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(new Color(255, 255, 255));
        // 移除绿色边框，只保留内边距
        itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        // 设置固定高度 - 增加高度以确保时间标签可见
        itemPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // 左侧头像：默认头像
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(new EmptyBorder(0, 0, 0, 12)); // 只保留右侧间距
        CircularAvatar avatar = new CircularAvatar(36);
        Image aimg = loadResourceImage("icons/默认头像.png");
        if (aimg != null) avatar.setAvatarImage(aimg);
        avatar.setBorderWidth(0f);
        avatarWrap.add(avatar, BorderLayout.NORTH);

        // 右侧内容 - 使用BoxLayout确保时间标签有足够空间
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(true);
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(0, 0, 0, 0)); // 移除内边距，由外层统一管理

        // 第一行：姓名和时间 - 使用BoxLayout+glue防止时间标签被挤没
        JPanel topLine = new JPanel();
        topLine.setOpaque(false);
        topLine.setLayout(new BoxLayout(topLine, BoxLayout.X_AXIS));
        topLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        topLine.setMinimumSize(new Dimension(0, 25));
        
        JLabel nameLabel = new JLabel(reply.getAuthorName());
        nameLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        nameLabel.setForeground(new Color(55, 65, 81));
        // 调试信息：检查回复时间
        System.out.println("[DEBUG] 创建回复项 - PostID=" + reply.getPostId() + 
                          ", AuthorName=" + reply.getAuthorName() + 
                          ", CreatedTime=" + reply.getCreatedTime() + 
                          ", FormattedTime=" + formatTime(reply.getCreatedTime()));
        
        JLabel timeLabel = new JLabel(formatTime(reply.getCreatedTime()));
        timeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        timeLabel.setForeground(new Color(107, 114, 128)); // 使用灰色文字
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT); // 右对齐
        // 删除对timeLabel的setPreferredSize固定宽度，让BoxLayout自然处理
        timeLabel.setOpaque(false); // 设置透明背景
        // 姓名悬浮主题色：墨绿色
        makeNameHoverGreen(nameLabel, new Color(55, 65, 81));
        
        topLine.add(nameLabel);
        topLine.add(Box.createHorizontalGlue()); // 关键：把时间推到最右侧
        topLine.add(timeLabel);

        // 限制回复内容长度以适应固定高度
        String content = reply.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(UIManager.getFont("TextArea.font").deriveFont(Font.PLAIN, 14f));
        contentArea.setForeground(new Color(31, 41, 55));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(4, 0, 6, 0));
        // 设置内容区域的最大高度以适应固定高度的评论项 - 减少高度为时间标签留出空间
        contentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // 减少间距从12到8
        ops.setOpaque(false);
        ops.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // 限制操作按钮区域高度
        ops.setBorder(new EmptyBorder(-2, -4, 0, 0)); // 向上和向左移动整个操作区域
        
        // 点赞按钮和数量
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        likePanel.setOpaque(false);
        likePanel.setBorder(new EmptyBorder(0, -2, 0, 0)); // 进一步向左移动点赞按钮
        
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
        
        // 设置初始状态
        boolean isLiked = reply.getIsLiked() != null ? reply.getIsLiked() : false;
        like.setSelected(isLiked);
        
        // 点赞数量标签
        int likeCount = reply.getLikeCount() != null ? reply.getLikeCount() : 0;
        JLabel likeCountLabel = new JLabel(String.valueOf(likeCount));
        likeCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        likeCountLabel.setForeground(new Color(156, 163, 175));
        
        // 添加点赞按钮事件监听器
        like.addActionListener(e -> {
            togglePostLike(reply.getPostId(), like, likeCountLabel);
        });
        
        likePanel.add(like);
        likePanel.add(likeCountLabel);
        
        ops.add(likePanel);

        // 使用BoxLayout添加组件，确保时间标签可见
        right.add(topLine);
        right.add(Box.createVerticalStrut(2)); // 添加小间距
        right.add(contentArea);
        right.add(Box.createVerticalStrut(2)); // 添加小间距
        right.add(ops);
        
        // 强制重新验证和重绘
        right.revalidate();
        right.repaint();

        itemPanel.add(avatarWrap, BorderLayout.WEST);
        itemPanel.add(right, BorderLayout.CENTER);

        return itemPanel;
    }
    
    
    
    
    /**
     * 重置回复输入框状态
     */
    private void resetReplyInputState() {
        // 恢复默认无边框样式
        replyTextArea.setBorder(new EmptyBorder(12, 20, 12, 12));
        replyTextArea.setText("");
        
        // 重新添加焦点监听器以确保输入框可以正常获得焦点和显示光标
        replyTextArea.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // 触发容器的重绘以显示焦点状态
                SwingUtilities.invokeLater(() -> {
                    if (replyTextArea.getParent() != null) {
                        replyTextArea.getParent().repaint();
                    }
                });
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                // 触发容器的重绘以隐藏焦点状态
                SwingUtilities.invokeLater(() -> {
                    if (replyTextArea.getParent() != null) {
                        replyTextArea.getParent().repaint();
                    }
                });
            }
        });
    }

    private void submitReply() {
        String content = replyTextArea.getText().trim();
        
        // 检查是否为空
        if (content.isEmpty()) {
            showToastMessage("请输入回复内容！", false);
            return;
        }
        
        if (currentThread == null) {
            showToastMessage("请先选择一个帖子！", false);
            return;
        }
        
        // 发送到服务器创建
        PostVO newReply = new PostVO();
        newReply.setThreadId(currentThread.getThreadId());
        newReply.setContent(content);
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            showToastMessage("未连接到服务器", false);
            return;
        }
        conn.setMessageListener(common.protocol.MessageType.CREATE_POST_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        // 重置回复输入框状态
                        resetReplyInputState();
                        
                        // 立即更新当前帖子的回复数量（+1）
                        if (currentThread != null) {
                            int currentReplyCount = currentThread.getReplyCount() != null ? currentThread.getReplyCount() : 0;
                            currentThread.setReplyCount(currentReplyCount + 1);
                            
                            // 更新帖子详情页面的回复数量显示
                            updateStatsDisplay(currentThread);
                            
                            // 更新评论区标题显示新的回复数量
                            updateCommentSectionTitle(currentThread);
                            
                            // 更新帖子列表中的回复数量（如果当前在列表中）
                            updateThreadInList(currentThread);
                        }
                        
                        // 重新获取回复列表以确保数据同步
                        fetchPostsFromServer(currentThread.getThreadId());
                        
                        showToastMessage("回复发布成功！", true);
                    }
                });
            }
        });
        conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.CREATE_POST_REQUEST, newReply));
    }
    
    /**
     * 自动调整文本区域的高度以适应内容
     * @param textArea 需要调整的文本区域
     */
    private void adjustTextAreaHeight(JTextArea textArea) {
        // 获取文本区域的文档
        javax.swing.text.Document doc = textArea.getDocument();
        if (doc == null) return;
        
        try {
            // 获取文本内容
            String text = doc.getText(0, doc.getLength());
            if (text == null || text.trim().isEmpty()) {
                textArea.setRows(1);
                return;
            }
            
            // 计算文本的行数
            String[] lines = text.split("\n");
            int lineCount = lines.length;
            
            // 计算每行的字符数，考虑自动换行
            int maxLineLength = 0;
            for (String line : lines) {
                // 估算每行能显示的字符数（基于字体大小和组件宽度）
                int estimatedCharsPerLine = Math.max(1, textArea.getWidth() / 8); // 粗略估算
                int wrappedLines = (int) Math.ceil((double) line.length() / estimatedCharsPerLine);
                maxLineLength = Math.max(maxLineLength, wrappedLines);
            }
            
            // 设置合适的行数，但不超过合理范围
            int totalRows = Math.min(Math.max(lineCount, maxLineLength), 50); // 最多50行
            textArea.setRows(totalRows);
            
            // 重新验证和重绘
            textArea.revalidate();
            textArea.repaint();
            
        } catch (Exception e) {
            // 如果出现异常，设置默认行数
            textArea.setRows(3);
        }
    }
    
    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) {
            System.out.println("[DEBUG] formatTime: timestamp为null，返回'未知时间'");
            return "未知时间";
        }
        
        // 直接显示绝对时间，格式：yyyy-MM-dd HH:mm
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedTime = sdf.format(timestamp);
        System.out.println("[DEBUG] formatTime: timestamp=" + timestamp + ", formatted=" + formattedTime);
        return formattedTime;
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
        this.connectionRef = connection;
        System.out.println("[Forum][Client] initContext: user=" + (currentUser != null ? currentUser.getLoginId() : "null") + ", connected=" + (connection != null && connection.isConnected()));
        // 初次载入时拉取服务器数据（头像仍用默认图片）
        if (!hasInitialized) {
            hasInitialized = true;
            SwingUtilities.invokeLater(new Runnable() { @Override public void run() {
                fetchSectionsFromServer();
                fetchThreadsFromServer();
            } });
        }
    }

    private client.net.ServerConnection connectionRef;

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new TeacherForumModule()); }


    private void fetchSectionsFromServer() {
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            return;
        }
        conn.setMessageListener(common.protocol.MessageType.GET_FORUM_SECTIONS_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<common.vo.ForumSectionVO> list = (java.util.List<common.vo.ForumSectionVO>) message.getData();
                    SwingUtilities.invokeLater(() -> {
                        sections.clear();
                        if (list != null) sections.addAll(list);
                        refreshHotSections();
                    });
                } catch (Exception e) {
                }
                try { conn.removeMessageListener(common.protocol.MessageType.GET_FORUM_SECTIONS_SUCCESS); } catch (Exception ignore) {}
            }
        });
        boolean sent = conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.GET_FORUM_SECTIONS_REQUEST));
        if (!sent) { }
    }
    
    /**
     * 切换主题点赞状态
     * @param threadId 主题ID
     * @param likeButton 点赞按钮
     * @param likeCountLabel 点赞数量标签
     */
    private void toggleThreadLike(Integer threadId, JToggleButton likeButton, JLabel likeCountLabel) {
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            JOptionPane.showMessageDialog(root, "未连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 设置消息监听器
        conn.setMessageListener(common.protocol.MessageType.TOGGLE_THREAD_LIKE_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override
            public void onMessageReceived(common.protocol.Message message) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> responseData = (java.util.Map<String, Object>) message.getData();
                    SwingUtilities.invokeLater(() -> {
                        if (responseData != null) {
                            Boolean result = (Boolean) responseData.get("isLiked");
                            Integer likeCount = (Integer) responseData.get("likeCount");
                            
                            if (result != null) {
                                // 更新按钮状态
                                likeButton.setSelected(result);
                                
                                // 更新点赞数量
                                if (likeCount != null) {
                                    likeCountLabel.setText(" " + likeCount);
                                }
                                
                                // 同步更新帖子列表页面的状态
                                syncThreadLikeState(threadId, result, likeCount);
                                
                                System.out.println("[Forum][Client] 主题点赞状态更新: threadId=" + threadId + ", isLiked=" + result + ", likeCount=" + likeCount);
                            } else {
                                // 操作失败，恢复按钮状态
                                likeButton.setSelected(!likeButton.isSelected());
                                JOptionPane.showMessageDialog(root, "点赞操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            // 操作失败，恢复按钮状态
                            likeButton.setSelected(!likeButton.isSelected());
                            JOptionPane.showMessageDialog(root, "点赞操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("处理点赞响应失败: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        likeButton.setSelected(!likeButton.isSelected());
                        JOptionPane.showMessageDialog(root, "点赞操作失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    try { 
                        conn.removeMessageListener(common.protocol.MessageType.TOGGLE_THREAD_LIKE_SUCCESS); 
                    } catch (Exception ignore) {}
                }
            }
        });
        
        // 发送点赞请求 - 服务器端期望直接传递threadId
        try {
            boolean sent = conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.TOGGLE_THREAD_LIKE_REQUEST, threadId));
            if (!sent) {
                SwingUtilities.invokeLater(() -> {
                    likeButton.setSelected(!likeButton.isSelected());
                    JOptionPane.showMessageDialog(root, "发送点赞请求失败", "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        } catch (Exception e) {
            System.err.println("发送点赞请求异常: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                likeButton.setSelected(!likeButton.isSelected());
                JOptionPane.showMessageDialog(root, "发送点赞请求异常: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * 切换回复点赞状态
     * @param postId 回复ID
     * @param likeButton 点赞按钮
     */
    private void togglePostLike(Integer postId, JToggleButton likeButton) {
        togglePostLike(postId, likeButton, null);
    }
    
    /**
     * 切换回复点赞状态（带数量标签更新）
     * @param postId 回复ID
     * @param likeButton 点赞按钮
     * @param likeCountLabel 点赞数量标签
     */
    private void togglePostLike(Integer postId, JToggleButton likeButton, JLabel likeCountLabel) {
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            JOptionPane.showMessageDialog(root, "未连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 设置消息监听器
        conn.setMessageListener(common.protocol.MessageType.TOGGLE_POST_LIKE_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override
            public void onMessageReceived(common.protocol.Message message) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> responseData = (java.util.Map<String, Object>) message.getData();
                    SwingUtilities.invokeLater(() -> {
                        if (responseData != null) {
                            Boolean result = (Boolean) responseData.get("isLiked");
                            Integer newLikeCount = (Integer) responseData.get("likeCount");
                            
                            if (result != null) {
                                // 更新按钮状态
                                likeButton.setSelected(result);
                                
                                // 更新点赞数量标签
                                if (likeCountLabel != null && newLikeCount != null) {
                                    likeCountLabel.setText(String.valueOf(newLikeCount));
                                }
                                
                                System.out.println("[Forum][Client] 回复点赞状态更新: postId=" + postId + ", isLiked=" + result + ", likeCount=" + newLikeCount);
                            } else {
                                // 操作失败，恢复按钮状态
                                likeButton.setSelected(!likeButton.isSelected());
                                JOptionPane.showMessageDialog(root, "点赞操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            // 操作失败，恢复按钮状态
                            likeButton.setSelected(!likeButton.isSelected());
                            JOptionPane.showMessageDialog(root, "点赞操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("处理回复点赞响应失败: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        likeButton.setSelected(!likeButton.isSelected());
                        JOptionPane.showMessageDialog(root, "点赞操作失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    try { 
                        conn.removeMessageListener(common.protocol.MessageType.TOGGLE_POST_LIKE_SUCCESS); 
                    } catch (Exception ignore) {}
                }
            }
        });
        
        // 发送点赞请求 - 服务器端期望直接传递postId
        try {
            boolean sent = conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.TOGGLE_POST_LIKE_REQUEST, postId));
            if (!sent) {
                SwingUtilities.invokeLater(() -> {
                    likeButton.setSelected(!likeButton.isSelected());
                    JOptionPane.showMessageDialog(root, "发送点赞请求失败", "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        } catch (Exception e) {
            System.err.println("发送回复点赞请求异常: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                likeButton.setSelected(!likeButton.isSelected());
                JOptionPane.showMessageDialog(root, "发送点赞请求异常: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * 显示创建帖子对话框
     */
    private void showCreateThreadDialog() {
        if (currentUser == null) {
            showToastMessage("请先登录", false);
            return;
        }
        
        // 确保有论坛分类数据
        if (sections == null || sections.isEmpty()) {
            // 如果没有分类数据，先获取
            fetchSectionsFromServer();
            showToastMessage("正在加载论坛分类，请稍后重试", false);
            return;
        }
        
        // 显示创建帖子对话框
        Window window = SwingUtilities.getWindowAncestor(root);
        Frame parentFrame = null;
        if (window instanceof Frame) {
            parentFrame = (Frame) window;
        } else if (window instanceof Dialog) {
            parentFrame = (Frame) ((Dialog) window).getParent();
        }
        
        ThreadVO newThread = CreateThreadDialog.showCreateThreadDialog(
            parentFrame, sections);
        
        if (newThread != null) {
            // 设置作者ID
            newThread.setAuthorId(currentUser.getUserId());
            newThread.setAuthorName(currentUser.getName());
            newThread.setAuthorLoginId(currentUser.getLoginId());
            
            // 发送创建帖子请求
            createThreadOnServer(newThread);
        }
    }
    
    /**
     * 发送创建帖子请求到服务器
     */
    private void createThreadOnServer(ThreadVO newThread) {
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            showToastMessage("未连接到服务器", false);
            return;
        }
        
        // 设置消息监听器
        conn.setMessageListener(common.protocol.MessageType.CREATE_THREAD_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override
            public void onMessageReceived(common.protocol.Message message) {
                SwingUtilities.invokeLater(() -> {
                    showToastMessage("帖子发布成功！", true);
                    // 刷新帖子列表
                    fetchThreadsFromServer();
                });
                try {
                    conn.removeMessageListener(common.protocol.MessageType.CREATE_THREAD_SUCCESS);
                } catch (Exception ignore) {}
            }
        });
        
        // 发送创建帖子请求
        boolean sent = conn.sendMessage(new common.protocol.Message(
            common.protocol.MessageType.CREATE_THREAD_REQUEST, newThread));
        
        if (!sent) {
            showToastMessage("发送发布请求失败", false);
        }
    }
    
    /**
     * 显示右上角提示消息
     * @param message 提示消息内容
     * @param isSuccess true表示成功消息，false表示错误消息
     */
    private void showToastMessage(String message, boolean isSuccess) {
        // 创建提示面板
        JPanel toastPanel = new JPanel(new BorderLayout());
        toastPanel.setBackground(isSuccess ? new Color(34, 197, 94) : new Color(239, 68, 68)); // 绿色成功，红色错误
        toastPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        // 添加图标和文字
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f));
        
        // 添加成功/错误图标
        String iconText = isSuccess ? "✓" : "✗";
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        toastPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 设置提示面板的样式
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // 创建弹出窗口
        JWindow toastWindow = new JWindow();
        toastWindow.setLayout(new BorderLayout());
        toastWindow.add(toastPanel, BorderLayout.CENTER);
        toastWindow.setAlwaysOnTop(true);
        toastWindow.setFocusableWindowState(false);
        
        // 计算位置（右上角）
        Point rootLocation = root.getLocationOnScreen();
        Dimension rootSize = root.getSize();
        Dimension toastSize = toastPanel.getPreferredSize();
        
        int x = rootLocation.x + rootSize.width - toastSize.width - 20;
        int y = rootLocation.y + 20;
        
        toastWindow.setLocation(x, y);
        toastWindow.setSize(toastSize);
        
        // 显示提示
        toastWindow.setVisible(true);
        
        // 3秒后自动消失
        Timer timer = new Timer(3000, e -> {
            toastWindow.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * 执行搜索操作
     * @param keyword 搜索关键词
     */
    private void performSearch(String keyword) {
        System.out.println("[Forum][UI] ========== 执行搜索操作 ==========");
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("[Forum][UI] 搜索关键词为空，取消搜索");
            return;
        }
        
        keyword = keyword.trim();
        System.out.println("[Forum][UI] 开始搜索，关键词: '" + keyword + "'");
        
        // 进入搜索模式
        isSearchMode = true;
        currentSearchKeyword = keyword;
        System.out.println("[Forum][UI] 已进入搜索模式，当前关键词: " + currentSearchKeyword);
        
        // 发送搜索请求到服务器
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null) {
            System.out.println("[Forum][UI] 服务器连接为null");
            showToastMessage("未连接到服务器", false);
            return;
        }
        if (!conn.isConnected()) {
            System.out.println("[Forum][UI] 服务器连接已断开");
            showToastMessage("未连接到服务器", false);
            return;
        }
        System.out.println("[Forum][UI] 服务器连接正常，准备发送搜索请求");
        
        // 设置搜索结果监听器
        conn.setMessageListener(common.protocol.MessageType.SEARCH_THREADS_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override
            public void onMessageReceived(common.protocol.Message message) {
                SwingUtilities.invokeLater(() -> {
                    handleSearchResults(message);
                });
                try {
                    conn.removeMessageListener(common.protocol.MessageType.SEARCH_THREADS_SUCCESS);
                } catch (Exception ignore) {}
            }
        });
        
        // 设置搜索失败监听器
        conn.setMessageListener(common.protocol.MessageType.SEARCH_THREADS_FAIL, new client.net.ServerConnection.MessageListener() {
            @Override
            public void onMessageReceived(common.protocol.Message message) {
                SwingUtilities.invokeLater(() -> {
                    showToastMessage("搜索失败: " + message.getMessage(), false);
                    isSearchMode = false;
                    currentSearchKeyword = null;
                });
                try {
                    conn.removeMessageListener(common.protocol.MessageType.SEARCH_THREADS_FAIL);
                } catch (Exception ignore) {}
            }
        });
        
        // 发送搜索请求
        System.out.println("[Forum][UI] 创建搜索请求消息，关键词: '" + keyword + "'");
        common.protocol.Message searchRequest = new common.protocol.Message(
            common.protocol.MessageType.SEARCH_THREADS_REQUEST, keyword);
        System.out.println("[Forum][UI] 搜索请求消息创建完成，类型: " + searchRequest.getType() + ", 数据: " + searchRequest.getData());
        
        boolean sent = conn.sendMessage(searchRequest);
        System.out.println("[Forum][UI] 搜索请求发送结果: " + sent);
        
        if (!sent) {
            System.out.println("[Forum][UI] 发送搜索请求失败，退出搜索模式");
            showToastMessage("发送搜索请求失败", false);
            isSearchMode = false;
            currentSearchKeyword = null;
        } else {
            System.out.println("[Forum][UI] 搜索请求发送成功，等待服务器响应");
        }
    }
    
    /**
     * 处理搜索结果
     * @param message 服务器返回的搜索结果消息
     */
    @SuppressWarnings("unchecked")
    private void handleSearchResults(common.protocol.Message message) {
        System.out.println("[Forum][UI] ========== 处理搜索结果 ==========");
        try {
            System.out.println("[Forum][UI] 收到搜索结果消息，类型: " + message.getType() + ", 状态码: " + message.getStatusCode());
            System.out.println("[Forum][UI] 搜索结果消息内容: " + message.getMessage());
            
            searchResults = (List<ThreadVO>) message.getData();
            if (searchResults == null) {
                System.out.println("[Forum][UI] 搜索结果数据为null，创建空列表");
                searchResults = new ArrayList<>();
            }
            
            System.out.println("[Forum][UI] 收到搜索结果: " + searchResults.size() + " 个帖子");
            
            // 打印前几个搜索结果的详细信息
            for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                ThreadVO thread = searchResults.get(i);
                System.out.println("[Forum][UI] 搜索结果[" + i + "]: ID=" + thread.getThreadId() + 
                                 ", 标题=" + thread.getTitle() + ", 作者=" + thread.getAuthorName());
            }
            
            // 更新帖子列表显示搜索结果
            updateThreadListWithSearchResults();
            
            System.out.println("[Forum][UI] 搜索结果处理完成");
            
        } catch (Exception e) {
            System.err.println("[Forum][UI] 处理搜索结果失败: " + e.getMessage());
            e.printStackTrace();
            showToastMessage("处理搜索结果失败", false);
            isSearchMode = false;
            currentSearchKeyword = null;
        }
    }
    
    /**
     * 更新帖子列表显示搜索结果
     */
    private void updateThreadListWithSearchResults() {
        if (threadScrollPane == null) {
            return;
        }
        
        // 获取帖子列表容器（滚动区域内的面板）
        JPanel threadItemsPanel = (JPanel) threadScrollPane.getViewport().getView();
        if (threadItemsPanel == null) {
            return;
        }
        
        // 清空当前帖子列表容器
        threadItemsPanel.removeAll();
        
        if (searchResults.isEmpty()) {
            // 显示无搜索结果的提示
            JPanel noResultPanel = createNoResultPanel();
            threadItemsPanel.add(noResultPanel);
        } else {
            // 显示搜索结果 - 使用与主列表相同的样式
            System.out.println("[Forum][UI] 显示搜索结果，数量: " + searchResults.size());
            for (ThreadVO thread : searchResults) {
                JPanel threadCard = createThreadItem(thread); // 使用与主列表相同的创建方法
                threadItemsPanel.add(threadCard);
                threadItemsPanel.add(Box.createVerticalStrut(12)); // 添加间距
            }
        }
        
        // 刷新UI
        threadItemsPanel.revalidate();
        threadItemsPanel.repaint();
        
        // 同步搜索结果中每个子项的宽度，确保与主列表样式一致
        SwingUtilities.invokeLater(new Runnable() {
            @Override 
            public void run() { 
                syncThreadItemsWidth(); 
            }
        });
    }
    
    
    /**
     * 创建无搜索结果的提示面板
     */
    private JPanel createNoResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250)); // 与帖子列表背景一致
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        
        // 设置左对齐并限制最大宽度
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        
        // 创建图标和文字
        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 36)); // 稍微缩小图标
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("没有搜索到结果");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f)); // 稍微缩小字体
        titleLabel.setForeground(new Color(107, 114, 128));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel messageLabel = new JLabel("试试其他关键词，或点击刷新查看所有帖子");
        messageLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f));
        messageLabel.setForeground(new Color(156, 163, 175));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(messageLabel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * 退出搜索模式，恢复正常帖子列表
     */
    private void exitSearchMode() {
        isSearchMode = false;
        currentSearchKeyword = null;
        searchResults.clear();
        
        // 清空搜索框文本
        if (searchFieldRef != null) {
            searchFieldRef.setText("搜索内容...");
            searchFieldRef.setForeground(new Color(156, 163, 175)); // 恢复占位符颜色
        }
        
        System.out.println("[Forum][UI] 退出搜索模式，重新加载所有帖子");
    }
    
    
    
}