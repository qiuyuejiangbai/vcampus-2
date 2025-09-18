package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BorrowRecordVO;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryBorrowHistoryModule extends JPanel {

    // 顶部筛选
    private RoundedTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JComboBox<String> statusFilter;

    // 卡片区
    private JPanel cardsPanel;
    private JScrollPane scrollPane;

    // 控制器
    private final int userId;
    private final LibraryController controller;

    // 选中卡片
    private BorrowCard selectedCard = null;

    // 自适应列宽参数（与 BookSearch 一致）
    private int minCardWidth = 240;
    private int columnGap = 15;
    private int rowGap = 15;
    private boolean applyingLayout = false;

    private final Color themeColor = new Color(0, 64, 0);
    private final Color hoverColor  = new Color(0, 100, 0);
    private final Color lightMint   = new Color(234, 247, 238);

    public LibraryBorrowHistoryModule(int userId) {
        this.userId = userId;
        this.controller = new LibraryController(userId);
        initUI();
        refreshCards();  // 初始加载
    }

    /** 现代化按钮（搜索=深绿实体，清空=同色描边样式可复用文本判断） */
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


    /** 圆角输入框（浅绿底 + 左侧icon位 + hover/聚焦描边） —— 与 BookSearch 保持一致 */
    private static class RoundedTextField extends JTextField {
        private final int arc = 16;
        private boolean hovered = false;
        private final Image searchIcon;

        public RoundedTextField(String placeholder, int columns) {
            super(placeholder, columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 12)); // 左边留 40px 放 icon
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

            g2.setColor(new Color(234, 247, 238));
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

    private JLabel makeIconLabel(String path, int w, int h, int padLR) {
        Image src = new ImageIcon(path).getImage();
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel lab = new JLabel(new ImageIcon(scaled));
        lab.setBorder(BorderFactory.createEmptyBorder(0, padLR, 0, padLR));
        return lab;
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部搜索 + 状态筛选，与 BookSearch 同风格
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        statusFilter = new JComboBox<>(new String[]{"全部", "未归还", "已归还"});
        searchPanel.add(new JLabel("状态:"));
        searchPanel.add(statusFilter);

        searchField = new RoundedTextField("请输入书名关键词", 25);
        searchField.setForeground(Color.GRAY);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if ("请输入书名关键词".equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
                searchField.repaint();
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入书名关键词");
                    searchField.setForeground(Color.GRAY);
                }
                searchField.repaint();
            }
        });
        searchField.addActionListener(e -> doSearch());
        searchPanel.add(searchField);

        searchButton = createModernButton("搜索", themeColor, hoverColor);
        clearButton  = createModernButton("清空筛选", themeColor, hoverColor);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(searchPanel);

        JPanel topBg = new JPanel(new BorderLayout());
        topBg.setBackground(lightMint);
        topBg.add(topContainer, BorderLayout.CENTER);
        JLabel leftTopIcon  = makeIconLabel("resources/icons/LibrarySearchLeft.png", 36, 36, 12);
        JLabel rightTopIcon = makeIconLabel("resources/icons/LibraryReturn.png", 36, 36, 12);
        topBg.add(leftTopIcon, BorderLayout.WEST);
        topBg.add(rightTopIcon, BorderLayout.EAST);

        add(topBg, BorderLayout.NORTH);

        // 卡片容器（WrapFlowLayout + 自适应列宽，与 BookSearch 保持一致）
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

        // 事件绑定
        searchButton.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> {
            searchField.setText("请输入书名关键词");
            searchField.setForeground(Color.GRAY);
            statusFilter.setSelectedIndex(0);
            doSearch();
        });
        statusFilter.addActionListener(e -> doSearch());
    }

    /** 核心查询 + 前端状态过滤 + 卡片刷新 */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if ("请输入书名关键词".equals(keyword)) keyword = "";

        String statusChoice = (String) statusFilter.getSelectedItem();

        List<BorrowRecordVO> records = keyword.isEmpty()
                ? controller.getBorrowingsByUser()
                : controller.searchBorrowHistory(keyword);

        // 前端过滤状态
        if ("未归还".equals(statusChoice)) {
            records = records.stream()
                    .filter(r -> r.getStatus() != 2 && r.getStatus() != 4)
                    .collect(Collectors.toList());
        } else if ("已归还".equals(statusChoice)) {
            records = records.stream()
                    .filter(r -> r.getStatus() == 2 || r.getStatus() == 4)
                    .collect(Collectors.toList());
        }

        // 刷新卡片
        cardsPanel.removeAll();
        selectedCard = null;

        for (BorrowRecordVO r : records) {
            BorrowCard card = new BorrowCard(r);
            cardsPanel.add(card);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
        resizeCardsByWidthStable();
        requestFocusInWindow();
    }

    /** 重置为默认筛选（与 BookSearch 的 refreshCards 一致思路） */
    public void refreshCards() {
        searchField.setText("请输入书名关键词");
        searchField.setForeground(Color.GRAY);
        statusFilter.setSelectedIndex(0);
        doSearch();
    }

    /** 等宽自适应（与 BookSearch 同策略，仅改“组件高度固定”） */
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

    /** 借阅记录卡片（复用 BookSearch 的卡片动效与选中态样式/尺寸） */
    /** 借阅记录卡片（复用 BookSearch 的卡片动效与选中态样式/尺寸） */
    class BorrowCard extends JPanel {
        public static final int CARD_H = 180;
        private int cardW = 310;
        private static final int ARC = 14;

        private final BorrowRecordVO record;
        private boolean isSelected = false;
        private float hoverProgress = 0f;
        private Timer animTimer;

        private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // --- 新增：将按钮区与按钮做成字段，便于控制可见性 ---
        private JPanel actions;
        private JButton btnReturn;
        private JButton btnRenew;

        BorrowCard(BorrowRecordVO record) {
            this.record = record;
            initCard();
        }

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

            // 内容区
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

            JLabel title = new JLabel(record.getBookTitle());
            title.setFont(new Font("微软雅黑", Font.BOLD, 16));
            title.setForeground(new Color(51, 51, 51));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            statusRow.setOpaque(false);
            statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel statusTag = new JLabel(statusText(record.getStatus()));
            statusTag.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            statusTag.setForeground(Color.WHITE);
            statusTag.setBackground(statusColor(record.getStatus()));
            statusTag.setOpaque(true);
            statusTag.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            statusRow.add(statusTag);

            JLabel borrow = new JLabel("借阅时间: " + (record.getBorrowTime() == null ? "-" : fmt.format(record.getBorrowTime())));
            borrow.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            borrow.setForeground(new Color(102, 102, 102));
            borrow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel due = new JLabel("应还时间: " + (record.getDueTime() == null ? "-" : fmt.format(record.getDueTime())));
            due.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            due.setForeground(new Color(102, 102, 102));
            due.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel ret = new JLabel("归还时间: " + (record.getReturnTime() == null ? "-" : fmt.format(record.getReturnTime())));
            ret.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            ret.setForeground(new Color(102, 102, 102));
            ret.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(title);
            content.add(Box.createVerticalStrut(8));
            content.add(statusRow);
            content.add(Box.createVerticalStrut(8));
            content.add(borrow);
            content.add(Box.createVerticalStrut(4));
            content.add(due);
            content.add(Box.createVerticalStrut(4));
            content.add(ret);

            add(content, BorderLayout.CENTER);

            // 操作区（右下角）：归还 / 续借
            actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
            actions.setOpaque(false);
            btnReturn = createModernButton("归还", new Color(60, 179, 113), new Color(80, 200, 130)); // 浅绿
            btnRenew  = createModernButton("续借", new Color(0, 80, 0),    new Color(0, 110, 0));    // 深绿
            actions.add(btnReturn);
            actions.add(btnRenew);
            add(actions, BorderLayout.SOUTH);

            // 按状态禁用
            boolean doneOrLost = (record.getStatus() == 2 || record.getStatus() == 4);
            btnReturn.setEnabled(!doneOrLost);
            btnRenew.setEnabled(!doneOrLost);

            // --- 新增：若已归还，则直接隐藏整个按钮区 ---
            if (record.getStatus() == 2) {
                actions.setVisible(false);
            }

            // 交互与动效
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectCard();
                }
                @Override public void mouseEntered(MouseEvent e) { startAnim(true); }
                @Override public void mouseExited(MouseEvent e)  { startAnim(false); }
            });

            btnReturn.addActionListener(e -> {
                if (!btnReturn.isEnabled()) return;
                int id = record.getRecordId();
                if (controller.requestReturn(id)) {
                    JOptionPane.showMessageDialog(LibraryBorrowHistoryModule.this, "归还成功");
                    // --- 新增：立即隐藏按钮与按钮区，带来“秒变已归还”的体验 ---
                    btnReturn.setVisible(false);
                    btnRenew.setVisible(false);
                    actions.setVisible(false);
                    actions.revalidate();
                    BorrowCard.this.revalidate();
                    BorrowCard.this.repaint();

                    doSearch(); // 保持你原有的整体刷新
                } else {
                    JOptionPane.showMessageDialog(LibraryBorrowHistoryModule.this, "归还失败");
                }
            });

            btnRenew.addActionListener(e -> {
                if (!btnRenew.isEnabled()) return;
                int id = record.getRecordId();
                if (controller.renewBook(id)) {
                    JOptionPane.showMessageDialog(LibraryBorrowHistoryModule.this, "续借成功");
                    doSearch();
                } else {
                    JOptionPane.showMessageDialog(LibraryBorrowHistoryModule.this, "续借失败");
                }
            });
        }

        private void selectCard() {
            if (selectedCard != null && selectedCard != this) selectedCard.setSelected(false);
            selectedCard = this;
            setSelected(true);
            requestFocusInWindow();
        }

        private void setSelected(boolean sel) {
            this.isSelected = sel;
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

        private String statusText(int s) {
            switch (s) {
                case 1: return "借出中";
                case 2: return "已归还";
                case 3: return "逾期";
                case 4: return "丢失";
                default: return "未知";
            }
        }

        private Color statusColor(int s) {
            switch (s) {
                case 1: return new Color(0, 102, 204);
                case 2: return new Color(128, 128, 128);
                case 3: return new Color(220, 53, 69);
                case 4: return new Color(255, 140, 0);
                default: return new Color(120, 120, 120);
            }
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

            // 悬浮浅绿色描边
            if (!isSelected && hoverProgress > 0f) {
                Color glow = new Color(0, 128, 0, Math.min(200, (int) (140 + 80 * hoverProgress)));
                g2.setColor(glow);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, ARC - 2, ARC - 2);
            }

            // 选中态额外描边 + 左侧竖条
            if (isSelected) {
                g2.setColor(new Color(0, 100, 0));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, ARC - 2, ARC - 2);
                g2.fillRoundRect(0, 0, 6, h - 1, ARC, ARC);
            }

            g2.dispose();
        }
    }


    /** WrapFlowLayout（与 BookSearch 一致，在 JScrollPane 中尊重 preferredSize 的换行 FlowLayout） */
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
