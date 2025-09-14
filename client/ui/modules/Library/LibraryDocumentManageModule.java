package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryDocumentManageModule extends JPanel {
    private JTextField keywordField, startYearField, endYearField;
    private JButton searchButton, clearButton, uploadButton;
    private JCheckBox[] subjectChecks, categoryChecks;
    private JTable table;
    private DefaultTableModel tableModel;
    private int hoverRow = -1;

    private final LibraryController controller;

    // 主题色
    private final Color themeColor = new Color(0, 64, 0);
    private final Color hoverColor = new Color(0, 100, 0);
    private final Color headerColor = new Color(0, 100, 0);
    private final Color rowAltColor = new Color(220, 245, 220);
    private final Color rowHoverColor = new Color(255, 250, 205);

    public LibraryDocumentManageModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initUI();
        doSearch();
    }

    /** 创建圆角按钮 */
    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverColor : themeColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
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
        // ===== 顶部搜索栏 =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

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

        keywordField.addActionListener(e -> doSearch());
        startYearField.addActionListener(e -> doSearch());
        endYearField.addActionListener(e -> doSearch());

        searchButton = createModernButton("搜索");
        clearButton = createModernButton("清空筛选");
        searchButton.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> refreshTable());

        searchPanel.add(keywordField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(searchPanel);

        // ===== 学科筛选（20个） =====
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
            subjectChecks[i].addItemListener(e -> doSearch());
            subjectPanel.add(subjectChecks[i]);
        }
        JPanel subjectWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subjectWrapper.add(new JLabel("学科:"));
        subjectWrapper.add(subjectPanel);
        topContainer.add(subjectWrapper);

        // ===== 类别筛选（10个） =====
        String[] categories = {
                "期刊", "会议", "教材", "报告", "专利",
                "标准", "学位论文", "技术文档", "白皮书", "数据集"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryChecks[i].addItemListener(e -> doSearch());
            categoryPanel.add(categoryChecks[i]);
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(new JLabel("类别:"));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // ===== 表格 =====
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "标题", "作者", "年份", "学科", "类别", "文件类型", "大小(MB)", "操作"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 8; }
        };

        table = new JTable(tableModel) {
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
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(180, 180, 180));

        // 悬停高亮
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                table.repaint();
            }
        });

        // 表头样式
        JTableHeader header = table.getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // 文本居中
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // 操作列
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== 底部按钮（靠右） =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        uploadButton = createModernButton("上传文献");
        uploadButton.addActionListener(e -> {
            LibraryDocumentAddDialog dialog = new LibraryDocumentAddDialog(
                    SwingUtilities.getWindowAncestor(this), controller);
            dialog.setVisible(true);
            doSearch();
        });
        bottomPanel.add(uploadButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void doSearch() {
        String keyword = keywordField.getText().trim();
        if (keyword.equals("请输入关键词（标题/作者/学科/类别）")) keyword = "";

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

        List<DocumentVO> docs = controller.searchDocuments(
                keyword,
                selectedSubjects.isEmpty() ? null : String.join(",", selectedSubjects),
                selectedCategories.isEmpty() ? null : String.join(",", selectedCategories),
                startYear, endYear
        );
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
                    doc.getCategory(),
                    doc.getFileType(),
                    String.format("%.2f", doc.getFileSize() / 1024.0 / 1024.0),
                    "编辑/删除"
            });
        }
    }

    public void refreshTable() {
        keywordField.setText("请输入关键词（标题/作者/学科/类别）");
        keywordField.setForeground(Color.GRAY);
        startYearField.setText("");
        endYearField.setText("");
        for (JCheckBox cb : subjectChecks) cb.setSelected(false);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch();
    }

    // =============== 操作列渲染/编辑 ===============
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); setText("编辑/删除"); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText("编辑/删除");
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("编辑/删除");
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            this.clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int docId = (int) tableModel.getValueAt(row, 0);
                DocumentVO doc = controller.getDocumentById(docId);

                String[] options = {"编辑", "删除", "取消"};
                int choice = JOptionPane.showOptionDialog(LibraryDocumentManageModule.this,
                        "请选择操作", "文献管理",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (choice == 0) { // 编辑
                    LibraryDocumentEditDialog dialog = new LibraryDocumentEditDialog(
                            SwingUtilities.getWindowAncestor(LibraryDocumentManageModule.this),
                            controller, doc);
                    dialog.setVisible(true);
                    doSearch();
                } else if (choice == 1) { // 删除
                    int confirm = JOptionPane.showConfirmDialog(LibraryDocumentManageModule.this,
                            "确认删除文献: " + doc.getTitle() + " ?", "删除确认",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean ok = controller.deleteDocument(docId);
                        JOptionPane.showMessageDialog(LibraryDocumentManageModule.this,
                                ok ? "删除成功" : "删除失败");
                        if (ok) doSearch();
                    }
                }
            }
            clicked = false;
            return "编辑/删除";
        }
    }
}
