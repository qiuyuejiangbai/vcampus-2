package client.ui.modules;

import common.vo.BookVO;
import common.vo.UserVO;
import client.controller.LibraryController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibraryBookSearchModule extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JTable table;
    private JButton borrowButton;

    private final LibraryController Controller;
    private final UserVO currentUser;

    public LibraryBookSearchModule(LibraryController Controller, UserVO currentUser) {
        this.Controller = Controller;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部搜索栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("搜索");
        topPanel.add(new JLabel("关键词:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // 中间表格
        String[] columnNames = {"ID", "书名", "作者", "ISBN", "出版社", "出版日期", "分类", "总数", "可借"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 底部借阅按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        borrowButton = new JButton("借阅");
        bottomPanel.add(borrowButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
        bindEvents();
    }

    private void bindEvents() {
        // 搜索按钮
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            List<BookVO> books = Controller.searchBooks(keyword);

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0); // 清空表格
            for (BookVO b : books) {
                model.addRow(new Object[]{
                        b.getBookId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getPublisher(),
                        b.getPublicationDate(),
                        b.getCategory(),
                        b.getTotalStock(),
                        b.getAvailableStock()
                });
            }
        });

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
        });
    }

    public void refreshTable() {
        List<BookVO> books = Controller.searchBooks(""); // 查询所有书
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (BookVO b : books) {
            model.addRow(new Object[]{
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getPublisher(),
                    b.getPublicationDate(),
                    b.getCategory(),
                    b.getTotalStock(),
                    b.getAvailableStock()
            });
        }
    }

}
