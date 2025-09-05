package client.ui.modules;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibraryBookManageModule extends JPanel {
    private LibraryController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public LibraryBookManageModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEdit = new JButton("编辑");
        JButton btnDelete = new JButton("删除");
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);

        // 表格
        String[] columns = {"ID", "书名", "作者", "ISBN", "出版社", "分类", "总数", "可借", "位置"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // 表格只展示，不直接编辑
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 初始化数据
        refreshTable();

        // 编辑按钮
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int bookId = (int) tableModel.getValueAt(row, 0); // 只取 book_id
                BookVO book = controller.getBookById(bookId);     // ✅ 用 controller 查完整信息
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
        btnDelete.addActionListener(e -> {
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

    private BookVO getBookFromRow(int row) {
        BookVO book = new BookVO();
        book.setBookId((int) tableModel.getValueAt(row, 0));
        book.setTitle((String) tableModel.getValueAt(row, 1));
        book.setAuthor((String) tableModel.getValueAt(row, 2));
        book.setIsbn((String) tableModel.getValueAt(row, 3));
        book.setPublisher((String) tableModel.getValueAt(row, 4));
        book.setCategory((String) tableModel.getValueAt(row, 5));
        book.setTotalStock((int) tableModel.getValueAt(row, 6));
        book.setAvailableStock((int) tableModel.getValueAt(row, 7));
        book.setLocation((String) tableModel.getValueAt(row, 8));
        return book;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<BookVO> books = controller.searchBooks(""); // 搜索全部
        for (BookVO b : books) {
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
