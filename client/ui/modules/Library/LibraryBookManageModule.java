package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryBookManageModule extends JPanel {
    private final LibraryController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton addButton;

    private JCheckBox[] categoryChecks;

    // 鼠标悬停行索引
    private int hoverRow = -1;

    public LibraryBookManageModule(LibraryController controller) {
        this.controller = controller;
        initUI();
        refreshTable();
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
        setLayout(new BorderLayout());

        Color themeColor = new Color(0, 64, 0);
        Color hoverColor = new Color(0, 100, 0);
        Color headerColor = new Color(0, 100, 0);
        Color rowAltColor = new Color(220, 245, 220);
        Color rowHoverColor = new Color(255, 250, 205);

        // --- 顶部搜索栏 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchField = new JTextField("请输入关键词（书名/作者/ISBN/分类）", 25);
        searchField.setForeground(Color.GRAY);

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("请输入关键词（书名/作者/ISBN/分类）")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.addActionListener(e -> doSearch());

        searchButton = createModernButton("搜索", themeColor, hoverColor);
        clearButton = createModernButton("清空筛选", themeColor, hoverColor);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(searchPanel);

        // 分类复选框
        String[] categories = {
                "文学", "计算机", "医学", "历史", "艺术",
                "经济", "教育", "哲学", "法律", "管理",
                "社会科学", "语言学", "地理", "政治", "环境",
                "工程", "心理学", "宗教", "军事", "体育"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryPanel.add(categoryChecks[i]);
            categoryChecks[i].addItemListener(e -> doSearch());
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // --- 表格 ---
        String[] columns = {"ID", "书名", "作者", "ISBN", "出版社", "分类", "总数", "可借", "位置"};
        tableModel = new DefaultTableModel(columns, 0);
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

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                table.repaint();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- 底部按钮 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = createModernButton("新增", themeColor, hoverColor);
        editButton = createModernButton("编辑", themeColor, hoverColor);
        deleteButton = createModernButton("删除", themeColor, hoverColor);
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents();
    }

    private void bindEvents() {
        searchButton.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> refreshTable());

        // 新增按钮
        addButton.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增书籍", true);
            dialog.setSize(480, 615);   // 更紧凑的大小
            dialog.setResizable(false); // 固定大小
            dialog.setLocationRelativeTo(this);

            LibraryBookAddModule addPanel = new LibraryBookAddModule(controller);
            dialog.setContentPane(addPanel);
            dialog.setVisible(true);

            refreshTable(); // 新增后刷新表格
        });


        // 编辑按钮
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int bookId = (int) tableModel.getValueAt(row, 0);
                BookVO book = controller.getBookById(bookId);
                if (book != null) {
                    LibraryBookEditDialogModule dialog = new LibraryBookEditDialogModule(controller, this, book);
                    dialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "未找到该书籍的详细信息！");
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一本书再编辑！");
            }
        });

        // 删除按钮
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int bookId = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "确认删除该书籍？", "删除确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.submitDeleteBook(bookId)) {
                        JOptionPane.showMessageDialog(this, "删除成功");
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败，可能存在未归还记录");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "请选择要删除的书籍");
            }
        });
    }

    /** 搜索方法（关键词 + 分类，模糊匹配） */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.equals("请输入关键词（书名/作者/ISBN/分类）")) {
            keyword = "";
        }

        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) {
            if (cb.isSelected()) {
                selectedCategories.add(cb.getText());
            }
        }

        List<BookVO> books = controller.searchBooks(keyword);
        tableModel.setRowCount(0);

        for (BookVO b : books) {
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (b.getCategory() != null && b.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }
            if (categoryMatch) {
                tableModel.addRow(new Object[]{
                        b.getBookId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getPublisher(),
                        b.getCategory(),
                        b.getTotalStock(),
                        b.getAvailableStock(),
                        b.getLocation()
                });
            }
        }
    }

    public void refreshTable() {
        searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch();
    }
}
