package client.ui.modules.Library;

import common.vo.BookVO;
import common.vo.UserVO;
import client.controller.LibraryController;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class LibraryBookSearchModule extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JTable table;
    private JButton borrowButton;
    private JButton viewButton;

    private JCheckBox[] categoryChecks;

    private final LibraryController Controller;
    private final UserVO currentUser;

    // 鼠标悬停行索引
    private int hoverRow = -1;

    public LibraryBookSearchModule(LibraryController Controller, UserVO currentUser) {
        this.Controller = Controller;
        this.currentUser = currentUser;
        initUI();
        refreshTable(); // 初始化时加载所有书籍
    }

    /** 创建现代化按钮（圆角 + hover 效果） */
    private JButton createModernButton(String text, Color themeColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景色（hover 时变浅）
                if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(themeColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 文字
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

        Color themeColor = new Color(0, 64, 0);        // 深墨绿色（按钮主色）
        Color hoverColor = new Color(0, 100, 0);       // hover 墨绿色
        Color headerColor = new Color(0, 100, 0);      // 表头绿色（介于深墨绿和森林绿之间）
        Color rowAltColor = new Color(220, 245, 220);  // 表格斑马纹浅绿色
        Color rowHoverColor = new Color(255, 250, 205); // 浅黄色（鼠标悬停行）

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

        // 绑定回车触发搜索
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

        // 表格
        String[] columnNames = {"ID", "书名", "作者", "ISBN", "出版社", "分类", "可借"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row == hoverRow) {
                        c.setBackground(rowHoverColor); // 鼠标悬停高亮
                    } else {
                        c.setBackground(row % 2 == 0 ? rowAltColor : Color.WHITE); // 斑马纹
                    }
                }
                return c;
            }
        };

        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(180, 180, 180)); // 分割线颜色

        // 鼠标移动事件：更新 hoverRow
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

        // 鼠标移出表格时取消高亮
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

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButton = createModernButton("查看", themeColor, hoverColor);
        borrowButton = createModernButton("借阅", themeColor, hoverColor);
        bottomPanel.add(viewButton);
        bottomPanel.add(borrowButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bindEvents();
    }

    private void bindEvents() {
        // 搜索按钮
        searchButton.addActionListener(e -> doSearch());

        // 清空按钮
        clearButton.addActionListener(e -> refreshTable());

        // 借阅按钮
        borrowButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一本书！");
                return;
            }
            int bookId = (int) table.getValueAt(row, 0);
            boolean success = Controller.requestBorrow(bookId);
            JOptionPane.showMessageDialog(this,
                    success ? "借阅成功！" : "借阅失败！");
            refreshTable();
        });

        // 查看按钮
        viewButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一本书！");
                return;
            }
            int bookId = (int) table.getValueAt(row, 0);
            BookVO book = Controller.getBookById(bookId);
            if (book != null) {
                JOptionPane.showMessageDialog(this,
                        "书名: " + book.getTitle() + "\n" +
                                "作者: " + book.getAuthor() + "\n" +
                                "ISBN: " + book.getIsbn() + "\n" +
                                "出版社: " + book.getPublisher() + "\n" +
                                "分类: " + book.getCategory() + "\n" +
                                "馆藏总数: " + book.getTotalStock() + "\n" +
                                "可借数量: " + book.getAvailableStock() + "\n" +
                                "状态: " + book.getStatus() + "\n" +
                                "位置: " + book.getLocation()
                );
            }
        });
    }

    /** 统一的搜索方法（关键词 + 分类，模糊匹配） */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.equals("请输入关键词（书名/作者/ISBN/分类）")) {
            keyword = "";
        }

        // 收集选中的分类
        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) {
            if (cb.isSelected()) {
                selectedCategories.add(cb.getText());
            }
        }

        List<BookVO> books = Controller.searchBooks(keyword);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (BookVO b : books) {
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (b.getCategory() != null && b.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }

            boolean keywordMatch = keyword.isEmpty()
                    || (b.getCategory() != null && b.getCategory().contains(keyword));

            if (categoryMatch && keywordMatch) {
                model.addRow(new Object[]{
                        b.getBookId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getPublisher(),
                        b.getCategory(),
                        b.getAvailableStock()
                });
            }
        }
    }

    public void refreshTable() {
        searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch(); // 默认查询全部
    }
}
