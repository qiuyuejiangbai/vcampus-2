package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 文献检索 - 卡片式 UI（与 LibraryBookSearchModule 统一风格）
 */
public class LibraryDocumentSearchModule extends JPanel {
    // 顶部搜索控件
    private JTextField startYearField, endYearField;
    private RoundedTextField keywordField;
    private JButton searchButton, clearButton;

    // 复选框
    private JCheckBox[] subjectChecks;
    private JCheckBox[] categoryChecks;

    // 卡片容器
    private JPanel cardsPanel;
    private JScrollPane scrollPane;

    // 底部操作按钮
    private JButton viewButton, previewButton, downloadButton;

    // 控制器
    private final LibraryController controller;

    // 当前选中的文献卡片
    private DocumentCard selectedCard = null;

    // 自适应列宽参数（与 booksearch 一致）
    private int minCardWidth = 240;
    private int columnGap = 15;
    private int rowGap = 15;
    private boolean applyingLayout = false;

    public LibraryDocumentSearchModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initUI();
        refreshTable(); // 初始加载
    }

    // =============== UI 初始化 ===============

    private void initUI() {
        Color themeColor = new Color(0, 64, 0);        // 深墨绿色（主色）
        Color hoverColor = new Color(0, 100, 0);       // hover 墨绿色
        Color lightMint = new Color(234, 247, 238);    // 浅绿背景

        // --- 顶部搜索栏（圆角输入框 + 年份区间 + 按钮） ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        // 年份输入
        startYearField = makeYearField();
        endYearField = makeYearField();
        searchPanel.add(new JLabel("年份:"));
        searchPanel.add(startYearField);
        searchPanel.add(new JLabel("-"));
        searchPanel.add(endYearField);

        // 圆角关键词输入（左留 icon 位）
        keywordField = new RoundedTextField("请输入关键词（标题/作者/学科/类别）", 25);
        keywordField.setForeground(Color.GRAY);
        keywordField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        keywordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (Objects.equals(keywordField.getText(), "请输入关键词（标题/作者/学科/类别）")) {
                    keywordField.setText("");
                    keywordField.setForeground(Color.BLACK);
                }
                keywordField.repaint();
            }
            public void focusLost(FocusEvent e) {
                if (keywordField.getText().isEmpty()) {
                    keywordField.setText("请输入关键词（标题/作者/学科/类别）");
                    keywordField.setForeground(Color.GRAY);
                }
                keywordField.repaint();
            }
        });
        keywordField.addActionListener(e -> doSearch());
        startYearField.addActionListener(e -> doSearch());
        endYearField.addActionListener(e -> doSearch());

        searchButton = createModernButton("搜索", themeColor, hoverColor);
        clearButton  = createOutlineButton("清空筛选", themeColor, lightMint); // ← 白底绿字

        searchPanel.add(keywordField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // 顶部容器 + 学科/类别复选框
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(searchPanel);

        // 学科
        String[] subjects = {
                "计算机","文学","管理","医学","历史",
                "艺术","经济","教育","哲学","法律",
                "社会科学","语言学","地理","政治","环境",
                "工程","心理学","宗教","军事","体育"
        };
        JPanel subjectPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        subjectPanel.setOpaque(true);
        subjectPanel.setBackground(lightMint);
        subjectChecks = new JCheckBox[subjects.length];
        for (int i = 0; i < subjects.length; i++) {
            subjectChecks[i] = new JCheckBox(subjects[i]);
            subjectChecks[i].setOpaque(true);
            subjectChecks[i].setBackground(lightMint);
            subjectChecks[i].addItemListener(e -> doSearch());
            subjectPanel.add(subjectChecks[i]);
        }
        JPanel subjectWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subjectWrapper.setOpaque(false);
        subjectWrapper.add(subjectPanel);
        topContainer.add(subjectWrapper);

        // 类别
        String[] categories = {
                "期刊","会议","教材","报告","专利",
                "标准","学位论文","技术文档","白皮书","数据集"
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

        // 顶部浅色背景 + 左右装饰 icon（路径与 booksearch 相同，后续可替换）
        JPanel topBg = new JPanel(new BorderLayout());
        topBg.setBackground(lightMint);
        topBg.add(topContainer, BorderLayout.CENTER);
        JLabel leftTopIcon  = makeIconLabel("resources/icons/LibrarySearchLeft.png", 60, 60, 12);
        JLabel rightTopIcon = makeIconLabel("resources/icons/LibraryDocument.png", 60, 60, 12);
        topBg.add(leftTopIcon, BorderLayout.WEST);
        topBg.add(rightTopIcon, BorderLayout.EAST);
        add(topBg, BorderLayout.NORTH);

        // --- 卡片容器（WrapFlowLayout + 自适应列宽） ---
        cardsPanel = new JPanel(new WrapFlowLayout(FlowLayout.LEFT, columnGap, rowGap));
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
            @Override public void componentResized(ComponentEvent e) { resizeCardsByWidthStable(); }
        });
        SwingUtilities.invokeLater(this::resizeCardsByWidthStable);

        // --- 底部按钮 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        viewButton    = createModernButton("查看", themeColor, hoverColor);
        previewButton = createModernButton("预览PDF", themeColor, hoverColor);
        downloadButton= createModernButton("下载", themeColor, hoverColor);
        bottomPanel.add(viewButton);
        bottomPanel.add(previewButton);
        bottomPanel.add(downloadButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents();
    }

    private JTextField makeYearField() {
        JTextField tf = new JTextField(4);
        tf.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return tf;
    }

    private JLabel makeIconLabel(String path, int w, int h, int padLR) {
        Image src = new ImageIcon(path).getImage();
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel lab = new JLabel(new ImageIcon(scaled));
        lab.setBorder(BorderFactory.createEmptyBorder(0, padLR, 0, padLR));
        return lab;
    }

    /** 实心主色按钮（与 booksearch 一致） */
    private JButton createModernButton(String text, Color themeColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean pressed = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), r = 18;

                Color bg = getModel().isRollover() ? hoverColor : themeColor;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, r, r);

                if (pressed) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, r, r);
                }

                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, textY);
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { pressed = false; repaint(); }
                });
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(98, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return button;
    }

    /** 白底绿字轮廓按钮（用于“清空筛选”） */
    private JButton createOutlineButton(String text, Color themeColor, Color hoverBg) {
        JButton button = new JButton(text) {
            private boolean pressed = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), r = 18;

                // 背景：默认白，悬停浅绿
                g2.setColor(getModel().isRollover() ? hoverBg : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, r, r);

                // 轮廓
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(themeColor);
                g2.drawRoundRect(1, 1, w - 3, h - 3, r - 2, r - 2);

                // 按下暗化
                if (pressed) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, r, r);
                }

                // 文字：绿色
                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(themeColor);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, textY);
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { pressed = false; repaint(); }
                });
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(98, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return button;
    }

    // =============== 事件绑定 ===============

    private void bindEvents() {
        // 搜索
        searchButton.addActionListener(e -> doSearch());

        // 清空
        clearButton.addActionListener(e -> refreshTable());

        // 查看详情
        viewButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            int docId = selectedCard.getDoc().getDocId();
            DocumentVO doc = controller.getDocumentById(docId);
            if (doc != null) showDocumentDetailDialog(doc);
            else JOptionPane.showMessageDialog(this, "未找到文献信息！");
        });

        // 预览 PDF
        previewButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            previewPDF(selectedCard.getDoc().getDocId());
        });

        // 下载
        downloadButton.addActionListener(e -> {
            if (selectedCard == null) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            doDownload(selectedCard.getDoc().getDocId());
        });

        // 键盘：Enter 查看详情；Delete 取消选择
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedCard != null) {
                    int docId = selectedCard.getDoc().getDocId();
                    DocumentVO doc = controller.getDocumentById(docId);
                    if (doc != null) showDocumentDetailDialog(doc);
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedCard != null) {
                    selectedCard.setSelected(false);
                    selectedCard = null;
                    repaint();
                }
            }
        });
    }

    // =============== 搜索/刷新 ===============

    /** 统一搜索方法（关键词 + 学科/类别 + 年份） */
    private void doSearch() {
        String keyword = keywordField.getText().trim();
        if ("请输入关键词（标题/作者/学科/类别）".equals(keyword)) keyword = "";

        Set<String> selectedSubjects = new HashSet<>();
        for (JCheckBox cb : subjectChecks) if (cb.isSelected()) selectedSubjects.add(cb.getText());

        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) if (cb.isSelected()) selectedCategories.add(cb.getText());

        Integer startYear = null, endYear = null;
        try {
            if (!startYearField.getText().trim().isEmpty()) {
                startYear = Integer.parseInt(startYearField.getText().trim());
            }
            if (!endYearField.getText().trim().isEmpty()) {
                endYear = Integer.parseInt(endYearField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年份必须是数字");
            return;
        }

        // ★ 保持不变的后端调用
        List<DocumentVO> docs = controller.searchDocuments(
                keyword,
                selectedSubjects.isEmpty() ? null : String.join(",", selectedSubjects),
                selectedCategories.isEmpty() ? null : String.join(",", selectedCategories),
                startYear, endYear
        );

        refreshTable(docs);
    }

    /** 接收搜索结果并刷新卡片 */
    private void refreshTable(List<DocumentVO> docs) {
        cardsPanel.removeAll();
        selectedCard = null;

        if (docs != null) {
            for (DocumentVO doc : docs) {
                DocumentCard card = new DocumentCard(doc);
                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
        resizeCardsByWidthStable();
        requestFocusInWindow();
    }

    /** 清空筛选并重查 */
    public void refreshTable() {
        keywordField.setText("请输入关键词（标题/作者/学科/类别）");
        keywordField.setForeground(Color.GRAY);
        for (JCheckBox cb : subjectChecks) cb.setSelected(false);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        startYearField.setText("");
        endYearField.setText("");
        doSearch();
    }

    // =============== 预览/下载/详情 ===============

    private void previewPDF(int docId) {
        byte[] pdfData = controller.downloadDocument(docId);
        if (pdfData == null) {
            JOptionPane.showMessageDialog(this, "获取PDF失败");
            return;
        }
        try {
            File tempFile = File.createTempFile("preview", ".pdf");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfData);
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "当前环境不支持直接预览PDF");
                int option = JOptionPane.showConfirmDialog(this,
                        "无法直接预览，是否下载PDF文件？", "预览失败", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    doDownload(docId);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "预览PDF失败: " + ex.getMessage());
        }
    }

    private void doDownload(int docId) {
        byte[] data = controller.downloadDocument(docId);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "下载失败");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("document_" + docId + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(data);
                JOptionPane.showMessageDialog(this, "下载成功: " + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
            }
        }
    }

    private void showDocumentDetailDialog(DocumentVO doc) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "文献详情", true);
        dialog.setSize(600, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(infoPanel, gbc, "文献ID", String.valueOf(doc.getDocId()));
        addRow(infoPanel, gbc, "标题", doc.getTitle());
        addRow(infoPanel, gbc, "作者", doc.getAuthors());
        addRow(infoPanel, gbc, "年份", String.valueOf(doc.getYear()));
        addRow(infoPanel, gbc, "学科", doc.getSubject());
        addRow(infoPanel, gbc, "类别", doc.getCategory());
        addRow(infoPanel, gbc, "关键词", doc.getKeywords());
        addRow(infoPanel, gbc, "文件类型", doc.getFileType());
        addRow(infoPanel, gbc, "文件大小", formatFileSize(doc.getFileSize()));
        addRow(infoPanel, gbc, "存储路径", doc.getStoragePath());
        addRow(infoPanel, gbc, "上传者ID", String.valueOf(doc.getUploaderId()));
        addRow(infoPanel, gbc, "上传时间", String.valueOf(doc.getUploadTime()));
        addRow(infoPanel, gbc, "是否公开", doc.isPublic() ? "是" : "否");

        if (doc.getAbstractTxt() != null && !doc.getAbstractTxt().isEmpty()) {
            addRow(infoPanel, gbc, "摘要", doc.getAbstractTxt(), true);
        }

        JScrollPane sp = new JScrollPane(infoPanel);
        dialog.add(sp, BorderLayout.CENTER);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        addRow(panel, gbc, label, value, false);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, String value, boolean isLongText) {
        int y = panel.getComponentCount(); // 简化计算
        gbc.gridx = 0; gbc.gridy = y;
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(l, gbc);

        gbc.gridx = 1;
        if (isLongText) {
            JTextArea area = new JTextArea(value);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(350, 120));
            panel.add(sp, gbc);
        } else {
            JLabel v = new JLabel(value == null ? "" : value);
            panel.add(v, gbc);
        }

        gbc.gridx = 0; gbc.gridwidth = 2; gbc.gridy++;
        JSeparator sep = new JSeparator();
        panel.add(sep, gbc);
        gbc.gridwidth = 1;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "";
        if (size < 1024) return size + " B";
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double) size / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    // =============== 自适应列宽（与 booksearch 一致） ===============

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

    // =============== 卡片定义（与 BookCard 同风格） ===============

    class DocumentCard extends JPanel {
        static final int CARD_H = 196;    // 稍微加高以容纳“关键词”
        private int cardW = 310;          // 宽度由自适应计算
        private static final int ARC = 14;

        private final DocumentVO doc;
        private boolean isSelected = false;
        private float hoverProgress = 0f; // 0~1
        private Timer animTimer;

        public DocumentCard(DocumentVO doc) {
            this.doc = doc;
            initCard();
        }

        public DocumentVO getDoc() { return doc; }

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

            // ===== 内容区 =====
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

            JLabel title = new JLabel(doc.getTitle() == null ? "(无标题)" : doc.getTitle());
            title.setFont(new Font("微软雅黑", Font.BOLD, 16));
            title.setForeground(new Color(51, 51, 51));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel authors = new JLabel("作者: " + (doc.getAuthors() == null ? "-" : doc.getAuthors()));
            authors.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            authors.setForeground(new Color(102, 102, 102));
            authors.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel year = new JLabel("年份: " + (doc.getYear() == 0 ? "-" : doc.getYear()));
            year.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            year.setForeground(new Color(102, 102, 102));
            year.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 关键词
            String kwText = (doc.getKeywords() == null || doc.getKeywords().trim().isEmpty()) ? "-" : doc.getKeywords();
            JLabel keywords = new JLabel("关键词: " + kwText);
            keywords.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            keywords.setForeground(new Color(102, 102, 102));
            keywords.setAlignmentX(Component.LEFT_ALIGNMENT);

            // 标签行：学科 + 类别
            JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            tags.setOpaque(false);
            tags.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel subjectTag = makeTag(doc.getSubject());
            JLabel categoryTag = makeTag(doc.getCategory());
            if (subjectTag != null) tags.add(subjectTag);
            if (categoryTag != null) tags.add(categoryTag);

            // 文件信息行：类型 + 大小
            JPanel fileInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            fileInfo.setOpaque(false);
            fileInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
            String ft = doc.getFileType() == null ? "" : doc.getFileType();
            String fs = formatFileSize(doc.getFileSize());
            JLabel fi = new JLabel((ft.isEmpty() ? "" : (ft + "  ")) + (fs.isEmpty() ? "" : fs));
            fi.setFont(new Font("微软雅黑", Font.BOLD, 12));
            fi.setForeground(new Color(34, 139, 34));
            fileInfo.add(fi);

            content.add(title);
            content.add(Box.createVerticalStrut(8));
            content.add(authors);
            content.add(Box.createVerticalStrut(4));
            content.add(year);
            content.add(Box.createVerticalStrut(4));
            content.add(keywords);
            content.add(Box.createVerticalStrut(8));
            content.add(tags);
            content.add(Box.createVerticalGlue());
            content.add(fileInfo);

            add(content, BorderLayout.CENTER);

            // ===== 事件桥（关键修复）：把同一个 MouseAdapter 递归挂到卡片及其所有子组件 =====
            MouseAdapter bridge = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    selectCard();
                    requestFocusInWindow();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    // 双击打开详情（仅左键）
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        int id = doc.getDocId();
                        DocumentVO full = controller.getDocumentById(id);
                        if (full != null) showDocumentDetailDialog(full);
                    }
                }
                @Override public void mouseEntered(MouseEvent e) {
                    startAnim(true);
                }
                @Override public void mouseExited(MouseEvent e) {
                    // 父 -> 子 移动会触发 mouseExited，这里做命中测试避免误关 hover
                    if (!isMouseStillInsideCard(e)) {
                        startAnim(false);
                    }
                }
            };
            installMouseBridgeRecursive(this, bridge);  // 递归挂载（写在类内）

            // 键盘：Enter 打开详情
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        int id = doc.getDocId();
                        DocumentVO full = controller.getDocumentById(id);
                        if (full != null) showDocumentDetailDialog(full);
                    }
                }
            });
        }

        private JLabel makeTag(String text) {
            if (text == null || text.isEmpty()) return null;
            JLabel tag = new JLabel(text);
            tag.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            tag.setForeground(Color.WHITE);
            tag.setBackground(new Color(0, 100, 0));
            tag.setOpaque(true);
            tag.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return tag;
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

        // 命中测试：鼠标是否仍在卡片区域内（过滤父->子的“误离开”）
        private boolean isMouseStillInsideCard(MouseEvent e) {
            Component src = (Component) e.getSource();
            Point p = SwingUtilities.convertPoint(src, e.getPoint(), this);
            return p.x >= 0 && p.y >= 0 && p.x < getWidth() && p.y < getHeight();
        }

        // 递归把同一个监听器挂到所有子组件（避免子组件吃掉点击/进入/离开事件）
        private void installMouseBridgeRecursive(Component c, MouseListener ml) {
            c.addMouseListener(ml);
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    installMouseBridgeRecursive(child, ml);
                }
            }
        }

        // 本类自带的文件大小格式化（不依赖外层方法，便于单独替换类）
        private String formatFileSize(long size) {
            if (size <= 0) return "";
            if (size < 1024) return size + " B";
            int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
            return String.format("%.1f %sB",
                    (double) size / (1L << (z * 10)),
                    " KMGTPE".charAt(z));
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

            // 悬浮浅绿色边框（按进度渐变）
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


    // =============== 圆角输入框（左侧 icon 位 + 悬浮/聚焦边框） ===============

    private static class RoundedTextField extends JTextField {
        private final int arc = 16;
        private boolean hovered = false;
        private final Image searchIcon;

        public RoundedTextField(String placeholder, int columns) {
            super(placeholder, columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 12)); // 左侧留出 40px 放 icon
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

    // =============== WrapFlowLayout（JScrollPane 内换行） ===============

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

    class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        public RoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }
}
