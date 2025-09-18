package client.ui.modules.Library;

import common.vo.BookVO;
import common.vo.UserVO;
import client.controller.LibraryController;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class LibraryBookSearchModule extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private JCheckBox[] categoryChecks;

    private final LibraryController Controller;
    private final UserVO currentUser;

    // 当前选中的书籍卡片
    private BookCard selectedCard = null;

    // ★ 新增：自适应列数（仅 UI）
    private int minCardWidth = 240;
    private int columnGap = 15;
    private int rowGap = 15;

    private boolean applyingLayout = false;

    public LibraryBookSearchModule(LibraryController Controller, UserVO currentUser) {
        this.Controller = Controller;
        this.currentUser = currentUser;
        initUI();
        refreshCards(); // 初始化时加载所有书籍
    }

    /** 创建现代化按钮（圆角 + hover/press 效果） */
    private JButton createModernButton(String text, Color themeColor, Color hoverColor) {
        final boolean outline = (text != null && text.contains("清空")); // 不改调用，靠文案判断
        JButton button = new JButton(text) {
            private boolean pressed = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), r = 18;

                boolean hover = getModel().isRollover();
                Color lightMint = new Color(234, 247, 238);   // 浅绿色底
                Color deepGreen = new Color(0, 120, 0);       // 悬停时更深的描边

                if (!outline) {
                    // 实体款（用于“搜索”等）
                    g2.setColor(hover ? hoverColor : themeColor);
                    g2.fillRoundRect(0, 0, w, h, r, r);
                } else {
                    // 描边款（用于“清空筛选”）
                    // 悬停：浅绿填充 + 深绿描边；默认：白底 + 绿描边
                    g2.setColor(hover ? lightMint : Color.WHITE);
                    g2.fillRoundRect(0, 0, w, h, r, r);

                    g2.setColor(hover ? deepGreen : themeColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, r - 2, r - 2);
                }

                if (pressed) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, r, r);
                }

                // 文案
                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(outline ? new Color(0, 80, 0) : Color.WHITE);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, textY);
                g2.dispose();
            }
            {
                // 保持按下态反馈
                addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                    @Override public void mouseExited(MouseEvent e)   { pressed = false; repaint(); }
                });
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(98, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        button.setRolloverEnabled(true); // 确保能拿到 hover 状态
        return button;
    }

    // ★ 圆角输入框（浅绿底 + 左侧icon位 + hover效果 + 完全圆角）
    private static class RoundedTextField extends JTextField {
        private final int arc = 16;
        private boolean hovered = false;
        private final Image searchIcon;

        public RoundedTextField(String placeholder, int columns) {
            super(placeholder, columns);
            setOpaque(false); // 关闭默认背景
            setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 12)); // 左边留 40px
            searchIcon = new ImageIcon("resources/icons/LibrarySearch.png").getImage();

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ★ 设置圆角裁剪区，保证文字背景也是圆角
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setClip(clip);

            // 浅绿色底
            Color lightMint = new Color(234, 247, 238);
            g2.setColor(lightMint);
            g2.fill(clip);

            // 悬浮/聚焦时加深边框 + 阴影
            if (isFocusOwner() || hovered) {
                g2.setColor(new Color(0, 120, 0));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(clip);
            } else {
                g2.setColor(new Color(200, 220, 200));
                g2.draw(clip);
            }

            // 左侧 icon
            int iconY = (getHeight() - 16) / 2;
            g2.drawImage(searchIcon, 12, iconY, 16, 16, null);

            g2.dispose();

            // ★ super 要最后画，让文字出现在圆角内
            super.paintComponent(g);
        }
    }


    private void initUI() {
        setLayout(new BorderLayout());

        Color themeColor = new Color(0, 64, 0);        // 深墨绿色（按钮主色）
        Color hoverColor = new Color(0, 100, 0);       // hover 墨绿色
        Color lightMint = new Color(234, 247, 238);

        // --- 顶部搜索栏 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        // 圆角输入框 + placeholder
        searchField = new RoundedTextField("请输入关键词（书名/作者/ISBN/分类）", 25);
        searchField.setForeground(Color.GRAY);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (Objects.equals(searchField.getText(), "请输入关键词（书名/作者/ISBN/分类）")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
                searchField.repaint();
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
                    searchField.setForeground(Color.GRAY);
                }
                searchField.repaint();
            }
        });
        searchField.addActionListener(e -> doSearch());

        searchButton = createModernButton("搜索", themeColor, hoverColor);
        clearButton  = createModernButton("清空筛选", themeColor, hoverColor);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(searchPanel);

        // 分类复选框
        String[] categories = {
                "文学", "计算机", "医学", "历史", "艺术",
                "经济", "教育", "哲学", "法律", "管理",
                "社会科学", "语言学", "地理", "政治", "环境",
                "工程", "心理学", "宗教", "军事", "体育"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryPanel.setOpaque(true);
        categoryChecks = new JCheckBox[categories.length];
        categoryPanel.setBackground(lightMint);
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryChecks[i].setOpaque(true);
            categoryChecks[i].setBackground(lightMint);
            categoryPanel.add(categoryChecks[i]);
            categoryChecks[i].addItemListener(e -> doSearch());
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.setOpaque(false);
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        // 顶部浅色背景 + 左右装饰 icon
        JPanel topBg = new JPanel(new BorderLayout());
        topBg.setBackground(lightMint);
        topBg.add(topContainer, BorderLayout.CENTER);

        JLabel leftTopIcon  = makeIconLabel("resources/icons/LibrarySearchLeft.png", 60, 60, 12);
        JLabel rightTopIcon = makeIconLabel("resources/icons/LibrarySearchRight.png", 60, 60, 12);

        topBg.add(leftTopIcon, BorderLayout.WEST);
        topBg.add(rightTopIcon, BorderLayout.EAST);

        add(topBg, BorderLayout.NORTH);

        // --- 卡片容器 ---
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 15, 15));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cardsPanel.setBackground(new Color(248, 250, 252));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                resizeCardsByWidthStable();
            }
        });
        SwingUtilities.invokeLater(this::resizeCardsByWidthStable);

        // --- 底部按钮 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton viewButton   = createModernButton("查看详情", themeColor, hoverColor);
        JButton borrowButton = createModernButton("借阅图书", themeColor, hoverColor);
        bottomPanel.add(viewButton);
        bottomPanel.add(borrowButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents(viewButton, borrowButton);

        // 初始加载
        refreshCards();
    }

    /** 缩放并包装一个 JLabel 图标 */
    private JLabel makeIconLabel(String path, int w, int h, int padLR) {
        Image src = new ImageIcon(path).getImage();
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel lab = new JLabel(new ImageIcon(scaled));
        lab.setBorder(BorderFactory.createEmptyBorder(0, padLR, 0, padLR));
        return lab;
    }

    // ★ 自适应列数：根据可视宽度动态计算列数（2~5列）
    private GridLayout makeGridLayoutByWidth() {
        int width = (scrollPane != null && scrollPane.getViewport() != null)
                ? scrollPane.getViewport().getWidth() : getWidth();
        int columns = 3;
        if (width > 0) {
            int usable = Math.max(0, width - 40); // 左右内边距估算
            int per = minCardWidth + columnGap;
            columns = Math.max(2, Math.min(5, usable / per));
        }
        return new GridLayout(0, columns, columnGap, rowGap);
    }

    private void bindEvents(JButton viewButton, JButton borrowButton) {
        // 搜索按钮
        searchButton.addActionListener(e -> doSearch());

        // 清空按钮
        clearButton.addActionListener(e -> refreshCards());

        // 借阅按钮
        borrowButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一本书！");
                return;
            }
            boolean success = Controller.requestBorrow(selectedCard.getBook().getBookId());
            JOptionPane.showMessageDialog(this,
                    success ? "借阅成功！" : "借阅失败！");
            refreshCards();
        });

        // 查看按钮（保持原有逻辑）
        viewButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一本书！");
                return;
            }
            BookVO book = selectedCard.getBook();
            showBookDetails(book);
        });

        // ★ 键盘快捷键：Enter 查看，Delete 取消选择
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedCard != null) {
                    showBookDetails(selectedCard.getBook());
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedCard != null) {
                    selectedCard.setSelected(false);
                    selectedCard = null;
                    repaint();
                }
            }
        });
    }

    private void showBookDetails(BookVO book) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "图书详情", true);
        dialog.setLayout(new BorderLayout());

        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);

        // ---- 新增：格式化出版日期（null 安全）----
        String pubDateStr = "未知";
        java.util.Date pubDate = (book.getPublicationDate() != null) ? book.getPublicationDate() : null;
        if (pubDate != null) {
            pubDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(pubDate);
        }

        String[] labels = {
                "书名:", "作者:", "ISBN:", "出版社:", "分类:",
                "馆藏总数:", "可借数量:", "状态:", "位置:", "出版日期:"   // 新增一项
        };
        String[] values = {
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublisher(),
                book.getCategory(),
                String.valueOf(book.getTotalStock()),
                String.valueOf(book.getAvailableStock()),
                book.getStatus(),
                book.getLocation(),
                pubDateStr                                        // 对应新增项
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("微软雅黑", Font.BOLD, 12));
            detailPanel.add(label, gbc);

            gbc.gridx = 1;
            JLabel value = new JLabel(values[i]);
            value.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            detailPanel.add(value, gbc);
        }

        dialog.add(detailPanel, BorderLayout.CENTER);
        dialog.setSize(420, 320);               // 若显示略拥挤，可适当调大，如 440x360
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    /** 统一的搜索方法（关键词 + 分类，模糊匹配） */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if ("请输入关键词（书名/作者/ISBN/分类）".equals(keyword)) {
            keyword = "";
        }

        // 收集选中的分类
        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) {
            if (cb.isSelected()) selectedCategories.add(cb.getText());
        }

        // 调用后端搜索（关键词）
        List<BookVO> books = Controller.searchBooks(keyword);

        // 更新卡片
        cardsPanel.removeAll();
        selectedCard = null;

        for (BookVO book : books) {
            // 前端过滤分类
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (book.getCategory() != null && book.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }
            if (categoryMatch) {
                BookCard card = new BookCard(book);
                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();

        // ★ 新增：数据更新后，按当前可用宽度均分卡片“宽度”（固定高度），减少右侧空白
        resizeCardsByWidthStable();

        requestFocusInWindow(); // 便于键盘交互
    }

    /** 根据可用宽度计算列数，只调整卡片宽度；高度固定为 BookCard.CARD_H
     * 口径与 WrapFlowLayout 完全一致，避免右侧“空一列”
     */
    private void resizeCardsByWidthStable() {
        if (applyingLayout || scrollPane == null || cardsPanel == null) return;

        applyingLayout = true;
        try {
            // —— 1) 拿到容器宽度：优先用 cardsPanel 的当前宽度（与 WrapFlowLayout 一致）
            int containerW = cardsPanel.getWidth();
            if (containerW <= 0) {
                // 退化：取 viewport 宽度
                JViewport vp = scrollPane.getViewport();
                if (vp != null) containerW = vp.getWidth();
            }
            if (containerW <= 0) return;

            // —— 2) 取 insets 与水平间距（与 WrapFlowLayout 逻辑一致）
            Insets insets = cardsPanel.getInsets();
            int hgap = 15;
            LayoutManager lm = cardsPanel.getLayout();
            if (lm instanceof FlowLayout) hgap = ((FlowLayout) lm).getHgap();

            int insetsLR = (insets != null ? insets.left + insets.right : 0);

            // —— 3) 如果竖滚动条此时是可见的，减去其宽度（避免列数抖动）
            int sbW = 0;
            JScrollBar vsb = scrollPane.getVerticalScrollBar();
            if (vsb != null && vsb.isShowing()) {
                sbW = vsb.getWidth(); // 一般 14~18px
            }

            // —— 4) WrapFlowLayout 的可用宽度 = 容器宽 - insets - hgap*2 - 可见滚动条
            int available = containerW - insetsLR - (hgap * 2) - sbW;
            if (available <= 0) return;

            // —— 5) 算列数与单卡宽度（确保 >= 最小宽度）
            int minW = Math.max(220, minCardWidth);
            int maxCols = 5; // 需要更多列可放大
            int bestCols = 1;
            int bestW = available; // 兜底全宽

            for (int cols = maxCols; cols >= 1; cols--) {
                int totalGaps = (cols - 1) * hgap;
                int w = (available - totalGaps) / cols; // 与 WrapFlowLayout 的排布口径一致
                if (w >= minW) {
                    bestCols = cols;
                    bestW = w;
                    break;
                }
            }

            // —— 6) 应用到每张卡片（固定高不拉长）
            int fixedH = BookCard.CARD_H;
            Dimension d = new Dimension(bestW, fixedH);
            for (Component c : cardsPanel.getComponents()) {
                c.setPreferredSize(d);
                c.setMinimumSize(d);
                // 最大高度不限制，宽度固定
                c.setMaximumSize(new Dimension(bestW, Integer.MAX_VALUE));
            }

            // —— 7) 重新布局
            cardsPanel.revalidate();
            cardsPanel.repaint();
        } finally {
            applyingLayout = false;
        }
    }


    public void refreshCards() {
        searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch(); // 默认查询全部
    }

    // 自定义圆角边框
    class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }

    // 图书卡片组件
    class BookCard extends JPanel {
        public static final int CARD_H = 180; // 固定高度，避免只搜到一本时被拉高
        private int cardW = 310;              // 宽度由外部均分计算
        private static final int ARC = 14;

        private final BookVO book;
        private boolean isSelected = false;

        // 悬停动画
        private float hoverProgress = 0f; // 0~1（用于平滑过渡）
        private Timer animTimer;

        public BookCard(BookVO book) {
            this.book = book;
            initCard();
        }

        public BookVO getBook() { return book; }

        /** 只改宽度，不改高度，避免纵向被拉伸 */
        public void setCardSize(int w, int h) {
            this.cardW = w;
            Dimension d = new Dimension(w, h);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            revalidate();
        }

        private void initCard() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setCardSize(cardW, CARD_H);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);

            // ===== 内容区（保持你原有的 UI 风格）=====
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

            JLabel titleLabel = new JLabel(book.getTitle());
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            titleLabel.setForeground(new Color(51, 51, 51));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel authorLabel = new JLabel("作者: " + book.getAuthor());
            authorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            authorLabel.setForeground(new Color(102, 102, 102));
            authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel isbnLabel = new JLabel("ISBN: " + book.getIsbn());
            isbnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            isbnLabel.setForeground(new Color(102, 102, 102));
            isbnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            categoryPanel.setOpaque(false);
            categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel categoryTag = new JLabel(book.getCategory());
            categoryTag.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            categoryTag.setForeground(Color.WHITE);
            categoryTag.setBackground(new Color(0, 100, 0));
            categoryTag.setOpaque(true);
            categoryTag.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            categoryPanel.add(categoryTag);

            JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            stockPanel.setOpaque(false);
            stockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel stockLabel = new JLabel("可借: " + book.getAvailableStock() + " 本");
            stockLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
            stockLabel.setForeground(book.getAvailableStock() > 0
                    ? new Color(34, 139, 34) : new Color(220, 53, 69));
            stockPanel.add(stockLabel);

            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(8));
            contentPanel.add(authorLabel);
            contentPanel.add(Box.createVerticalStrut(4));
            contentPanel.add(isbnLabel);
            contentPanel.add(Box.createVerticalStrut(8));
            contentPanel.add(categoryPanel);
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.add(stockPanel);

            add(contentPanel, BorderLayout.CENTER);

            // ===== 统一事件桥（关键修复）=====
            MouseAdapter bridge = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    // 第一次点击就选中，不等 mouseClicked，避免子组件吞掉点击
                    selectCard();
                    requestFocusInWindow();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        // 双击打开详情
                        showBookDetails(book);
                    }
                }
                @Override public void mouseEntered(MouseEvent e) {
                    startAnim(true);
                }
                @Override public void mouseExited(MouseEvent e) {
                    // 仅当鼠标真正离开卡片区域时才结束 hover
                    if (!isMouseStillInsideCard(e)) {
                        startAnim(false);
                    }
                }
            };
            installMouseBridgeRecursive(this, bridge); // 递归安装到所有子组件

            // 键盘：Enter 打开详情
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        showBookDetails(book);
                    }
                }
            });
        }

        private void selectCard() {
            if (selectedCard != null && selectedCard != this) selectedCard.setSelected(false);
            selectedCard = this;
            setSelected(true);
            repaint();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        private void startAnim(boolean hoverIn) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            final float start = hoverProgress;
            final float end = hoverIn ? 1f : 0f;
            final int duration = 160; // 140~200ms 均可
            final long t0 = System.currentTimeMillis();

            animTimer = new Timer(10, e -> { // ~100 FPS，保持你之前的顺滑感
                float t = Math.min(1f, (System.currentTimeMillis() - t0) / (float) duration);
                float eased = 1f - (float) Math.pow(1 - t, 3);
                hoverProgress = start + (end - start) * eased;
                repaint();
                if (t >= 1f) ((Timer) e.getSource()).stop();
            });
            animTimer.setCoalesce(true);
            animTimer.start();
        }

        // 递归安装鼠标监听，避免子组件吞掉点击与进入/离开事件
        private void installMouseBridgeRecursive(Component c, MouseListener ml) {
            c.addMouseListener(ml);
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    installMouseBridgeRecursive(child, ml);
                }
            }
        }

        // 判断鼠标是否仍在卡片区域（用于过滤“父->子”的 mouseExited）
        private boolean isMouseStillInsideCard(MouseEvent e) {
            Component src = (Component) e.getSource();
            Point p = SwingUtilities.convertPoint(src, e.getPoint(), this);
            return p.x >= 0 && p.y >= 0 && p.x < getWidth() && p.y < getHeight();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // 底色：选中浅绿；未选白色
            g2.setColor(isSelected ? new Color(240, 248, 240) : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

            // 普通描边
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

            // 悬浮浅绿色边框（仅 hover 时显示，保持原有动画）
            if (!isSelected && hoverProgress > 0f) {
                Color glow = new Color(0, 128, 0, Math.min(200, (int) (140 + 80 * hoverProgress)));
                g2.setColor(glow);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, ARC - 2, ARC - 2);
            }

            // 选中态高亮边框 + 左侧细条
            if (isSelected) {
                g2.setColor(new Color(0, 100, 0));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, ARC - 2, ARC - 2);
                g2.fillRoundRect(0, 0, 6, h - 1, ARC, ARC);
            }

            g2.dispose();
        }
    }



    /** WrapFlowLayout：在 JScrollPane 中也能尊重 preferredSize 的换行 FlowLayout */
    class WrapFlowLayout extends FlowLayout {
        public WrapFlowLayout() { super(); }
        public WrapFlowLayout(int align) { super(align); }
        public WrapFlowLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false);
            d.width -= (getHgap() + 1);
            return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = target.getWidth();
                if (maxWidth <= 0) {
                    Container p = target.getParent();
                    if (p instanceof JViewport) maxWidth = ((JViewport) p).getWidth();
                }
                if (maxWidth <= 0) maxWidth = Integer.MAX_VALUE;
                int available = maxWidth - (insets.left + insets.right + hgap * 2);

                int x = 0, y = insets.top + vgap, rowHeight = 0, reqWidth = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (x == 0 || x + d.width <= available) {
                        if (x > 0) x += hgap;
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        y += rowHeight + vgap;
                        reqWidth = Math.max(reqWidth, x);
                        x = d.width;
                        rowHeight = d.height;
                    }
                }
                y += rowHeight;
                reqWidth = Math.max(reqWidth, x);
                return new Dimension(reqWidth + insets.left + insets.right + hgap * 2,
                        y + insets.bottom + vgap);
            }
        }

        @Override public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                int max = target.getWidth() - (insets.left + insets.right + getHgap() * 2);
                int x = insets.left + getHgap(), y = insets.top + getVgap(), rowH = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible()) continue;
                    Dimension d = m.getPreferredSize();
                    if (x > insets.left + getHgap() && x + d.width > max + insets.left + getHgap()) {
                        x = insets.left + getHgap();
                        y += rowH + getVgap();
                        rowH = 0;
                    }
                    m.setBounds(x, y, d.width, d.height);
                    x += d.width + getHgap();
                    rowH = Math.max(rowH, d.height);
                }
            }
        }
    }


}
