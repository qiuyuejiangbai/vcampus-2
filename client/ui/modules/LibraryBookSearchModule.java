package client.ui.modules;

import common.vo.BookVO;
import common.vo.UserVO;
import client.controller.LibraryController;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
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
    private JLabel statLabel; // 右上角统计信息

    private final LibraryController Controller;
    private final UserVO currentUser;

    public LibraryBookSearchModule(LibraryController Controller, UserVO currentUser) {
        this.Controller = Controller;
        this.currentUser = currentUser;
        initUI();
        refreshTable(); // 初始化时加载所有书籍
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- 顶部容器 ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // 搜索栏（三段式布局）
        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));

        JLabel titleLabel = new JLabel("📚 图书搜索");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchField = new JTextField("请输入关键词（书名/作者/ISBN/分类）", 25);
        searchField.setForeground(Color.GRAY);

        // 提示文字效果
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

        searchButton = new JButton("搜索");
        clearButton = new JButton("清空筛选");
        centerPanel.add(searchField);
        centerPanel.add(searchButton);
        centerPanel.add(clearButton);

        statLabel = new JLabel("馆藏总数: 0 本");
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statLabel.setHorizontalAlignment(SwingConstants.LEFT);

        searchPanel.add(titleLabel, BorderLayout.WEST);
        searchPanel.add(centerPanel, BorderLayout.CENTER);
        searchPanel.add(statLabel, BorderLayout.EAST);

        topContainer.add(searchPanel);

        // 分类复选框（多行网格布局，一行 10 个，居中）
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

            // 勾选时立即刷新
            categoryChecks[i].addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    doSearch();
                }
            });
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // 中间表格（去掉“馆藏总数”列）
        String[] columnNames = {"ID", "书名", "作者", "ISBN", "出版社", "分类", "可借"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        // --- 表格美化：内容和表头居中，行高 ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setRowHeight(28);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButton = new JButton("查看");
        borrowButton = new JButton("借阅");
        bottomPanel.add(viewButton);
        bottomPanel.add(borrowButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
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

            // 关键词额外模糊匹配分类
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

        // 更新统计信息
        statLabel.setText("馆藏总数: " + books.size() + " 本");
    }

    public void refreshTable() {
        searchField.setText("请输入关键词（书名/作者/ISBN/分类）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch(); // 默认查询全部
    }
}
