package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import common.vo.ThreadVO;
import common.vo.PostVO;
import common.vo.UserVO;
import common.vo.ForumSectionVO;
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
    private JLabel threadCategoryTag;
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
    
    // 公告区域引用：用于动态刷新
    private JPanel announcementContentPanel;
    
    
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

    public StudentForumModule() { 
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

        // 默认选中"最新"
        selectedCategoryButton = latestCategoryButton;

        // 点击切换选中状态并应用排序
        java.awt.event.ActionListener categoryClick = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                JButton src = (JButton) e.getSource();
                if (src == latestCategoryButton) currentSortMode = SortMode.LATEST;
                else if (src == hotCategoryButton) currentSortMode = SortMode.HOT;
                else if (src == essenceCategoryButton) currentSortMode = SortMode.ESSENCE;
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
        refreshButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc2 = 12; // 圆角

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
            // 刷新时回到列表视图，清除分区筛选，确保可见变化
            try {
                currentSectionIdFilter = null;
                if (cardLayout != null && mainPanel != null) {
                    cardLayout.show(mainPanel, "LIST");
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
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景，带阴影效果
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 12;
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
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 圆角卡片：无描边，仅白色圆角背景，带阴影效果
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int arc = 12;
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

        // 标题区域：左侧头像 + 右侧标题与专题标签
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 6, 0));

        // 左侧默认头像
        CircularAvatar titleAvatar = new CircularAvatar(40);
        Image titleAvatarImg = loadResourceImage("icons/默认头像.png");
        if (titleAvatarImg != null) titleAvatar.setAvatarImage(titleAvatarImg);
        titleAvatar.setBorderWidth(0f);
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(new EmptyBorder(0, 0, 0, 12));
        avatarWrap.add(titleAvatar, BorderLayout.NORTH);

        threadTitleLabel = new JLabel();
        // 详情标题加粗加黑
        threadTitleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 20f));
        threadTitleLabel.setForeground(new Color(31, 41, 55));
        // 标题右侧专题标签（浅色圆角淡绿色）
        threadCategoryTag = createRoundedAnimatedTag("专题", 999, 240);
        JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRight.setOpaque(false);
        titleRight.add(threadTitleLabel);
        titleRight.add(threadCategoryTag);

        titlePanel.add(avatarWrap, BorderLayout.WEST);
        titlePanel.add(titleRight, BorderLayout.CENTER);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        metaPanel.setBackground(new Color(255, 255, 255));

        threadAuthorLabel = new JLabel();
        threadAuthorLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadAuthorLabel.setForeground(new Color(156, 163, 175));

        threadTimeLabel = new JLabel();
        threadTimeLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        threadTimeLabel.setForeground(new Color(156, 163, 175));

        // 不再展示回复数
        threadReplyCountLabel = new JLabel();
        threadReplyCountLabel.setVisible(false);
        
        metaPanel.add(threadAuthorLabel);
        metaPanel.add(threadTimeLabel);
        // 不添加回复数到元信息

        // 标签行
        threadTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        threadTagPanel.setOpaque(false);
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(titlePanel);
        northStack.add(Box.createVerticalStrut(4));
        northStack.add(threadTagPanel);
        threadHeaderPanel.add(northStack, BorderLayout.NORTH);
        threadHeaderPanel.add(metaPanel, BorderLayout.SOUTH);

        // 删除操作区：点赞/收藏/分享
        // 仅保留元信息面板
        threadHeaderPanel.add(metaPanel, BorderLayout.SOUTH);
        
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

        // ESC 快捷键返回列表
        threadDetailPanel.registerKeyboardAction(
            e -> cardLayout.show(mainPanel, "LIST"),
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
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
        categoryComboBox = new JComboBox<>();
        refreshCategoryComboModel();
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
                int arc = 12;
                
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

        JPanel firstLine = new JPanel(new BorderLayout());
        firstLine.setOpaque(false);
        firstLine.add(nameTimeStack, BorderLayout.WEST);
        firstLine.add(categoryTag, BorderLayout.EAST);
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
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        footer.setOpaque(false);
        
        // 使用实际的点赞数而不是计算值
        int likeCount = thread.getLikeCount() != null ? thread.getLikeCount() : 0;
        System.out.println("[DEBUG] 帖子点赞数 - 实际值=" + likeCount);
        
        // 创建可点击的点赞按钮
        ImageIcon likeIcon = loadScaledIcon("icons/点赞.png", 16, 16);
        ImageIcon likedIcon = loadScaledIcon("icons/已点赞.png", 16, 16);
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
        likeCountLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        likeCountLabel.setForeground(new Color(156, 163, 175));
        
        // 创建点赞容器
        JPanel likeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        likeContainer.setOpaque(false);
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
        
        ImageIcon replySmall = loadScaledIcon("icons/评论.png", 16, 16);
        JLabel replyLabel = new JLabel(" " + replyCount);
        replyLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11f));
        replyLabel.setForeground(new Color(156, 163, 175));
        if (replySmall != null) {
            replyLabel.setIcon(replySmall);
            System.out.println("[DEBUG] 回复图标加载成功");
        } else {
            System.out.println("[DEBUG] 回复图标加载失败");
        }
        replyLabel.setIconTextGap(4);
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
        // 更新专题标签文本（使用已有类别推断逻辑）
        if (threadCategoryTag != null) {
            threadCategoryTag.setText(getThreadSectionName(thread));
        }
        threadContentArea.setText(thread.getContent());
        threadAuthorLabel.setText("作者: " + thread.getAuthorName());
        threadTimeLabel.setText("时间: " + formatTime(thread.getCreatedTime()));
        // 列表与详情均不展示回复数
        threadReplyCountLabel.setText("");
        
        fetchPostsFromServer(thread.getThreadId());
        
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
        
        // 设置初始状态
        boolean isLiked = reply.getIsLiked() != null ? reply.getIsLiked() : false;
        like.setSelected(isLiked);
        
        // 添加点赞按钮事件监听器
        like.addActionListener(e -> {
            togglePostLike(reply.getPostId(), like);
        });

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
        // 读取选择的分区
        Integer selectedSectionId = null;
        int selIdx = categoryComboBox != null ? categoryComboBox.getSelectedIndex() : -1;
        if (selIdx <= 0) {
            JOptionPane.showMessageDialog(createThreadDialog, "请选择分区！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            if (comboSections != null && selIdx - 1 < comboSections.size()) {
                ForumSectionVO sec = comboSections.get(selIdx - 1);
                selectedSectionId = sec != null ? sec.getSectionId() : null;
            }
        }
        
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(createThreadDialog, "请填写标题和内容！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 发送到服务器创建
        ThreadVO newThread = new ThreadVO();
        newThread.setTitle(title);
        newThread.setContent(content);
        newThread.setSectionId(selectedSectionId);
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            JOptionPane.showMessageDialog(root, "未连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        conn.setMessageListener(common.protocol.MessageType.CREATE_THREAD_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                final ThreadVO created = (ThreadVO) message.getData();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        createThreadDialog.setVisible(false);
                        threads.add(0, created);
                        refreshThreadList();
                        JOptionPane.showMessageDialog(root, "帖子发布成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
        conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.CREATE_THREAD_REQUEST, newThread));
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
        
        // 发送到服务器创建
        PostVO newReply = new PostVO();
        newReply.setThreadId(currentThread.getThreadId());
        newReply.setContent(content);
        client.net.ServerConnection conn = this.connectionRef;
        if (conn == null || !conn.isConnected()) {
            JOptionPane.showMessageDialog(root, "未连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        conn.setMessageListener(common.protocol.MessageType.CREATE_POST_SUCCESS, new client.net.ServerConnection.MessageListener() {
            @Override public void onMessageReceived(common.protocol.Message message) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        replyTextArea.setText("");
                        fetchPostsFromServer(currentThread.getThreadId());
                        JOptionPane.showMessageDialog(root, "回复发布成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
        conn.sendMessage(new common.protocol.Message(common.protocol.MessageType.CREATE_POST_REQUEST, newReply));
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

    public static void registerTo(Class<?> ignored) { ModuleRegistry.register(new StudentForumModule()); }

    private void refreshCategoryComboModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        model.addElement("选择分区");
        comboSections.clear();
        if (sections != null) {
            for (ForumSectionVO s : sections) {
                if (s != null && s.getStatus() != null && s.getStatus() == 1) {
                    model.addElement(s.getName());
                    comboSections.add(s);
                }
            }
        }
        categoryComboBox.setModel(model);
    }

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
                        refreshCategoryComboModel();
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
                            
                            if (result != null) {
                                // 更新按钮状态
                                likeButton.setSelected(result);
                                System.out.println("[Forum][Client] 回复点赞状态更新: postId=" + postId + ", isLiked=" + result);
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
}