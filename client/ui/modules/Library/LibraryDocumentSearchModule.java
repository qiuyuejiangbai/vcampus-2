package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class LibraryDocumentSearchModule extends JPanel {
    private JTextField keywordField;
    private JButton searchButton, clearButton;
    private JButton viewButton, downloadButton,previewButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    private JCheckBox[] subjectChecks;
    private JCheckBox[] categoryChecks;

    private JTextField startYearField, endYearField;

    private final LibraryController controller;

    // 鼠标悬停行索引
    private int hoverRow = -1;

    public LibraryDocumentSearchModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initUI();
    }

    /** 创建现代化按钮（圆角 + hover 效果） */
    private JButton createModernButton(String text, Color themeColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(themeColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                FontMetrics fm = g2.getFontMetrics();
                Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
                int textHeight = fm.getAscent();
                int textY = rect.y + (rect.height - fm.getHeight()) / 2 + textHeight;
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, textY);

                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(90, 30));
        return button;
    }

    private void initUI() {
        Color themeColor = new Color(0, 64, 0);
        Color hoverColor = new Color(0, 100, 0);
        Color headerColor = new Color(0, 100, 0);
        Color rowAltColor = new Color(220, 245, 220);
        Color rowHoverColor = new Color(255, 250, 205);

        // --- 顶部搜索栏 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // 年份输入框放在关键词左边
        startYearField = new JTextField(4);
        endYearField = new JTextField(4);
        searchPanel.add(new JLabel("年份:"));
        searchPanel.add(startYearField);
        searchPanel.add(new JLabel("-"));
        searchPanel.add(endYearField);

        keywordField = new JTextField("请输入关键词（标题/作者/学科/类别）", 25);
        keywordField.setForeground(Color.GRAY);

        keywordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (keywordField.getText().equals("请输入关键词（标题/作者/学科/类别）")) {
                    keywordField.setText("");
                    keywordField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (keywordField.getText().isEmpty()) {
                    keywordField.setText("请输入关键词（标题/作者/学科/类别）");
                    keywordField.setForeground(Color.GRAY);
                }
            }
        });

        // 回车触发搜索
        keywordField.addActionListener(e -> doSearch());
        startYearField.addActionListener(e -> doSearch());
        endYearField.addActionListener(e -> doSearch());

        searchButton = createModernButton("搜索", themeColor, hoverColor);
        clearButton = createModernButton("清空筛选", themeColor, hoverColor);

        searchPanel.add(keywordField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(searchPanel);

        // 学科复选框
        String[] subjects = {
                "计算机", "文学", "管理", "医学", "历史",
                "艺术", "经济", "教育", "哲学", "法律",
                "社会科学", "语言学", "地理", "政治", "环境",
                "工程", "心理学", "宗教", "军事", "体育"
        };
        JPanel subjectPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        subjectChecks = new JCheckBox[subjects.length];
        for (int i = 0; i < subjects.length; i++) {
            subjectChecks[i] = new JCheckBox(subjects[i]);
            subjectPanel.add(subjectChecks[i]);
            subjectChecks[i].addItemListener(e -> doSearch());
        }
        JPanel subjectWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subjectWrapper.add(new JLabel("学科:"));
        subjectWrapper.add(subjectPanel);
        topContainer.add(subjectWrapper);

        // 类别复选框
        String[] categories = {
                "期刊", "会议", "教材", "报告", "专利",
                "标准", "学位论文", "技术文档", "白皮书", "数据集"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryPanel.add(categoryChecks[i]);
            categoryChecks[i].addItemListener(e -> doSearch());
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(new JLabel("类别:"));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // --- 表格 ---
        tableModel = new DefaultTableModel(new Object[]{"ID", "标题", "作者", "年份", "学科", "类别"}, 0);

        resultTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row == hoverRow) {
                        c.setBackground(rowHoverColor);
                    } else {
                        c.setBackground(row % 2 == 0 ? rowAltColor : Color.WHITE);
                    }
                }
                return c;
            }
        };

        resultTable.setRowHeight(28);
        resultTable.setShowGrid(true);
        resultTable.setGridColor(new Color(180, 180, 180));

        // 鼠标悬停高亮
        resultTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = resultTable.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    resultTable.repaint();
                }
            }
        });
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoverRow = -1;
                resultTable.repaint();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = resultTable.getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // --- 底部按钮 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButton = createModernButton("查看", themeColor, hoverColor);
        previewButton = createModernButton("预览PDF", themeColor, hoverColor); // 新增预览按钮
        downloadButton = createModernButton("下载", themeColor, hoverColor);
        bottomPanel.add(viewButton);
        bottomPanel.add(previewButton); // 添加预览按钮
        bottomPanel.add(downloadButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents();
    }

    private void bindEvents() {
        searchButton.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> refreshTable());

        // 查看按钮
        viewButton.addActionListener(e -> {
            int row = resultTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            int docId = (int) tableModel.getValueAt(row, 0);
            DocumentVO doc = controller.getDocumentById(docId);
            if (doc != null) {
                showDocumentDetailDialog(doc);
            } else {
                JOptionPane.showMessageDialog(this, "未找到文献信息！");
            }
        });

        // 下载按钮
        downloadButton.addActionListener(e -> {
            int row = resultTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            int docId = (int) tableModel.getValueAt(row, 0);
            doDownload(docId);
        });

        // 预览按钮事件
        previewButton.addActionListener(e -> {
            int row = resultTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条文献！");
                return;
            }
            int docId = (int) tableModel.getValueAt(row, 0);
            previewPDF(docId);
        });
    }

    // ================= PDF预览功能 =================
    private void previewPDF(int docId) {
        byte[] pdfData = controller.downloadDocument(docId);
        if (pdfData == null) {
            JOptionPane.showMessageDialog(this, "获取PDF失败");
            return;
        }

        try {
            // 创建临时PDF文件
            File tempFile = File.createTempFile("preview", ".pdf");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfData);
            }

            // 使用系统默认程序打开PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "当前环境不支持直接预览PDF");
                // 如果无法预览，提供下载选项
                int option = JOptionPane.showConfirmDialog(this,
                        "无法直接预览，是否下载PDF文件？",
                        "预览失败",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    doDownload(docId);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "预览PDF失败: " + ex.getMessage());
        }
    }

    private void doSearch() {
        String keyword = keywordField.getText().trim();
        if (keyword.equals("请输入关键词（标题/作者/学科/类别）")) {
            keyword = "";
        }

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

        List<DocumentVO> docs = controller.searchDocuments(keyword,
                selectedSubjects.isEmpty() ? null : String.join(",", selectedSubjects),
                selectedCategories.isEmpty() ? null : String.join(",", selectedCategories),
                startYear, endYear);
        refreshTable(docs);
    }

    private void refreshTable(List<DocumentVO> docs) {
        tableModel.setRowCount(0);
        for (DocumentVO doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getDocId(),
                    doc.getTitle(),
                    doc.getAuthors(),
                    doc.getYear(),
                    doc.getSubject(),
                    doc.getCategory()
            });
        }
    }

    public void refreshTable() {
        keywordField.setText("请输入关键词（标题/作者/学科/类别）");
        keywordField.setForeground(Color.GRAY);
        for (JCheckBox cb : subjectChecks) cb.setSelected(false);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        startYearField.setText("");
        endYearField.setText("");
        doSearch();
    }

    // ================= 文献详情对话框 =================
    private void showDocumentDetailDialog(DocumentVO doc) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "文献详情", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        BiConsumer<String, Object> addRow = (label, value) -> {
            gbc.gridx = 0;
            JLabel l = new JLabel(label + ":");
            l.setFont(new Font("微软雅黑", Font.BOLD, 12));
            infoPanel.add(l, gbc);

            gbc.gridx = 1;
            String text = (value == null) ? "" : value.toString();
            JLabel v = new JLabel(text);
            infoPanel.add(v, gbc);

            // 分隔线
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.gridy++;
            JSeparator sep = new JSeparator();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            infoPanel.add(sep, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;
        };

        addRow.accept("文献ID", doc.getDocId());
        addRow.accept("标题", doc.getTitle());
        addRow.accept("作者", doc.getAuthors());
        addRow.accept("年份", doc.getYear());
        addRow.accept("学科", doc.getSubject());
        addRow.accept("类别", doc.getCategory());
        addRow.accept("关键词", doc.getKeywords());
        addRow.accept("文件类型", doc.getFileType());
        addRow.accept("文件大小", formatFileSize(doc.getFileSize()));
        addRow.accept("存储路径", doc.getStoragePath());
        addRow.accept("上传者ID", doc.getUploaderId());
        addRow.accept("上传时间", doc.getUploadTime());
        addRow.accept("是否公开", doc.isPublic() ? "是" : "否");

        if (doc.getAbstractTxt() != null && !doc.getAbstractTxt().isEmpty()) {
            gbc.gridx = 0;
            JLabel l = new JLabel("摘要:");
            l.setFont(new Font("微软雅黑", Font.BOLD, 12));
            infoPanel.add(l, gbc);

            gbc.gridx = 1;
            JTextArea abstractArea = new JTextArea(doc.getAbstractTxt());
            abstractArea.setLineWrap(true);
            abstractArea.setWrapStyleWord(true);
            abstractArea.setEditable(false);
            JScrollPane sp = new JScrollPane(abstractArea);
            sp.setPreferredSize(new Dimension(350, 120));
            infoPanel.add(sp, gbc);

            gbc.gridy++;
        }

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "";
        if (size < 1024) return size + " B";
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double) size / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    // ================= 下载 =================
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
}
