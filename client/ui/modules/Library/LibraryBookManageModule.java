package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LibraryBookManageModule extends JPanel {
    // --- 保持原有字段命名，尽量不破坏外部调用 ---
    private final LibraryController controller;

    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;

    private JButton editButton;
    private JButton deleteButton;
    private JButton addButton;

    private JCheckBox[] categoryChecks;

    // 新增：卡片容器相关
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private BookCard selectedCard = null;

    // 布局自适应参数（与 booksearch 对齐）
    private int minCardWidth = 240;
    private int columnGap = 15;
    private int rowGap = 15;
    private boolean applyingLayout = false;

    public LibraryBookManageModule(LibraryController controller) {
        this.controller = controller;
        initUI();
        refreshCards(); // 初始化展示
    }

    /** 现代化按钮（搜索=深绿实体；清空=白底绿字描边）——与 booksearch 一致 */
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


    /** 与 booksearch 一致的圆角输入框（浅绿底 + 左侧 icon 位 + 聚焦高亮） */
    private static class RoundedTextField extends JTextField {
        private final int arc = 16;
        private boolean hovered = false;
        private final Image searchIcon;

        public RoundedTextField(String placeholder, int columns) {
            super(placeholder, columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 12));
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

            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setClip(clip);

            Color lightMint = new Color(234, 247, 238);
            g2.setColor(lightMint);
            g2.fill(clip);

            if (isFocusOwner() || hovered) {
                g2.setColor(new Color(0, 120, 0));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(clip);
            } else {
                g2.setColor(new Color(200, 220, 200));
                g2.draw(clip);
            }

            int iconY = (getHeight() - 16) / 2;
            g2.drawImage(searchIcon, 12, iconY, 16, 16, null);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        Color themeColor = new Color(0, 64, 0);  // 深墨绿
        Color hoverColor = new Color(0, 100, 0); // hover 墨绿
        Color lightMint  = new Color(234, 247, 238);

        // --- 顶部搜索 + 按钮，与 booksearch 对齐 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        searchField = new RoundedTextField("请输入关键词（书名/作者/ISBN/分类）", 25);
        searchField.setForeground(Color.GRAY);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (Objects.equals(searchField.getText(), "请输入关键词（书名/作者/ISBN/分类）")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
                searchField.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
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

        // --- 分类复选，与 booksearch 一致 ---
        String[] categories = {
                "文学", "计算机", "医学", "历史", "艺术",
                "经济", "教育", "哲学", "法律", "管理",
                "社会科学", "语言学", "地理", "政治", "环境",
                "工程", "心理学", "宗教", "军事", "体育"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryPanel.setOpaque(true);
        categoryPanel.setBackground(lightMint);
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryChecks[i].setOpaque(true);
            categoryChecks[i].setBackground(lightMint);
            categoryChecks[i].addItemListener(e -> doSearch());
            categoryPanel.add(categoryChecks[i]);
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.setOpaque(false);
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        // 顶部浅绿背景 + 左右装饰 icon（可复用路径）
        JPanel topBg = new JPanel(new BorderLayout());
        topBg.setBackground(lightMint);
        topBg.add(topContainer, BorderLayout.CENTER);

        JLabel leftTopIcon  = makeIconLabel("resources/icons/LibrarySearchLeft.png", 60, 60, 12);
        JLabel rightTopIcon = makeIconLabel("resources/icons/LibrarySearchRight.png", 60, 60, 12);
        topBg.add(leftTopIcon, BorderLayout.WEST);
        topBg.add(rightTopIcon, BorderLayout.EAST);

        add(topBg, BorderLayout.NORTH);

        // --- 中间卡片区（WrapFlowLayout + 自适应列宽），完全复刻风格 ---
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, columnGap, rowGap));
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

        // --- 底部管理按钮（保持原调用/语义：新增/编辑/删除） ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        addButton    = createModernButton("新增", themeColor, hoverColor);
        editButton   = createModernButton("编辑", themeColor, hoverColor);
        deleteButton = createModernButton("删除", themeColor, hoverColor);
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents();
    }

    /** 缩放并包装一个 JLabel 图标（与 booksearch 同步） */
    private JLabel makeIconLabel(String path, int w, int h, int padLR) {
        Image src = new ImageIcon(path).getImage();
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel lab = new JLabel(new ImageIcon(scaled));
        lab.setBorder(BorderFactory.createEmptyBorder(0, padLR, 0, padLR));
        return lab;
    }

    private void bindEvents() {
        searchButton.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> refreshCards());

        // 新增（保持原有调用）
        addButton.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增书籍", true);
            dialog.setSize(480, 615);
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(this);
            LibraryBookAddModule addPanel = new LibraryBookAddModule(controller);
            dialog.setContentPane(addPanel);
            dialog.setVisible(true);
            refreshCards();
        });

        // 编辑（保持原有调用）
        editButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一本书再编辑！");
                return;
            }
            int bookId = selectedCard.getBook().getBookId();
            BookVO book = controller.getBookById(bookId);
            if (book != null) {
                LibraryBookEditDialogModule dialog = new LibraryBookEditDialogModule(controller, this, book);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "未找到该书籍的详细信息！");
            }
        });

        // 删除（保持原有调用）
        deleteButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请选择要删除的书籍");
                return;
            }
            int bookId = selectedCard.getBook().getBookId();
            int confirm = JOptionPane.showConfirmDialog(this, "确认删除该书籍？", "删除确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.submitDeleteBook(bookId)) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    refreshCards();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败，可能存在未归还记录");
                }
            }
        });

        // 键盘交互：Delete 取消选择
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedCard != null) {
                    selectedCard.setSelected(false);
                    selectedCard = null;
                    repaint();
                }
            }
        });
    }

    /** 统一搜索（关键词 + 分类前端过滤），调用保持与表格版一致：controller.searchBooks(keyword) */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if ("请输入关键词（书名/作者/ISBN/分类）".equals(keyword)) keyword = "";

        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) if (cb.isSelected()) selectedCategories.add(cb.getText());

        List<BookVO> books = controller.searchBooks(keyword);

        cardsPanel.removeAll();
        selectedCard = null;

        for (BookVO b : books) {
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (b.getCategory() != null && b.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }
            if (categoryMatch) {
                BookCard card = new BookCard(b);
                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
        resizeCardsByWidthStable(); // 更新后自适应列宽
        requestFocusInWindow();
    }

    /** 与 booksearch 同步：根据可用宽度只调整卡片宽度，固定卡片高度，减少右侧空白 */
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
            int fixedH = LibraryBookSearchModule.BookCard.CARD_H;
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

    /** 对外兼容：原类里有 refreshTable，这里保留同名入口转发到卡片刷新 */
    public void refreshTable() { refreshCards(); }

    public void refreshCards() {
        searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
        searchField.setForeground(Color.GRAY);
        if (categoryChecks != null) for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch();
    }

    // ---------------------- 卡片组件（与 booksearch 风格一致）----------------------

    class BookCard extends JPanel {
        public static final int CARD_H = 180;  // 固定高度，避免只一条时被拉高
        private int cardW = 310;

        private static final int ARC = 14;

        private final BookVO book;
        private boolean isSelected = false;
        private float hoverProgress = 0f; // 0~1
        private Timer animTimer;

        public BookCard(BookVO book) {
            this.book = book;
            initCard();
        }

        public BookVO getBook() { return book; }

        private void setCardSize(int w, int h) {
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

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

            JLabel title = new JLabel(nullToDash(book.getTitle()));
            title.setFont(new Font("微软雅黑", Font.BOLD, 16));
            title.setForeground(new Color(51, 51, 51));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel author = new JLabel("作者: " + nullToDash(book.getAuthor()));
            author.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            author.setForeground(new Color(102, 102, 102));
            author.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel isbn = new JLabel("ISBN: " + nullToDash(book.getIsbn()));
            isbn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            isbn.setForeground(new Color(102, 102, 102));
            isbn.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel publisher = new JLabel("出版社: " + nullToDash(book.getPublisher()));
            publisher.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            publisher.setForeground(new Color(102, 102, 102));
            publisher.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            tagPanel.setOpaque(false);
            tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel tag = new JLabel(nullToDash(book.getCategory()));
            tag.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            tag.setForeground(Color.WHITE);
            tag.setBackground(new Color(0, 100, 0));
            tag.setOpaque(true);
            tag.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            tagPanel.add(tag);

            JPanel stockPanel = new JPanel(new GridLayout(1, 0, 12, 0));
            stockPanel.setOpaque(false);
            stockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel total = new JLabel("总数: " + book.getTotalStock());
            total.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            JLabel avail = new JLabel("可借: " + book.getAvailableStock());
            avail.setFont(new Font("微软雅黑", Font.BOLD, 12));
            avail.setForeground(book.getAvailableStock() > 0 ?
                    new Color(34, 139, 34) : new Color(220, 53, 69));
            JLabel loc = new JLabel("位置: " + nullToDash(book.getLocation()));
            loc.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            stockPanel.add(total);
            stockPanel.add(avail);
            stockPanel.add(loc);

            content.add(title);
            content.add(Box.createVerticalStrut(8));
            content.add(author);
            content.add(Box.createVerticalStrut(4));
            content.add(isbn);
            content.add(Box.createVerticalStrut(4));
            content.add(publisher);
            content.add(Box.createVerticalStrut(8));
            content.add(tagPanel);
            content.add(Box.createVerticalGlue());
            content.add(stockPanel);

            add(content, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectCard();
                    // 管理页：双击默认走“编辑”比较顺手
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        editButton.doClick();
                    }
                }
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e)  { startAnim(false); }
            });

            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        editButton.doClick();
                    }
                }
            });
        }

        private void selectCard() {
            if (selectedCard != null && selectedCard != this) selectedCard.setSelected(false);
            selectedCard = this;
            setSelected(true);
            requestFocusInWindow();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        private void startAnim(boolean hoverIn) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            final float start = hoverProgress;
            final float end = hoverIn ? 1f : 0f;
            final int duration = 160;
            final long t0 = System.currentTimeMillis();

            animTimer = new Timer(10, e -> {
                float t = Math.min(1f, (System.currentTimeMillis() - t0) / (float) duration);
                float eased = 1f - (float) Math.pow(1 - t, 3);
                hoverProgress = start + (end - start) * eased;
                repaint();
                if (t >= 1f) ((Timer) e.getSource()).stop();
            });
            animTimer.setCoalesce(true);
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // 选中浅绿底，否则白底
            g2.setColor(isSelected ? new Color(240, 248, 240) : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

            // 常规描边
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

            // 悬浮浅绿边框（平滑）
            if (!isSelected && hoverProgress > 0f) {
                Color glow = new Color(0, 128, 0, Math.min(200, (int) (140 + 80 * hoverProgress)));
                g2.setColor(glow);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, ARC - 2, ARC - 2);
            }

            // 选中高亮边框 + 左侧色条
            if (isSelected) {
                g2.setColor(new Color(0, 100, 0));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, ARC - 2, ARC - 2);
                g2.fillRoundRect(0, 0, 6, h - 1, ARC, ARC);
            }

            g2.dispose();
        }
    }

    // ---------------------- WrapFlowLayout，与 booksearch 等效 ----------------------
    class WrapFlowLayout extends FlowLayout {
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

    // 便捷函数
    private static String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
