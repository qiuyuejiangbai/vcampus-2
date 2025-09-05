package client.ui.modules;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import java.awt.*;

public class LibraryBookEditDialogModule extends JDialog {
    private JTextField txtTitle, txtAuthor, txtIsbn, txtPublisher,
            txtCategory, txtTotal, txtAvailable, txtPubDate, txtLocation;
    private JComboBox<String> comboStatus;

    private final LibraryController controller;
    private final LibraryBookManageModule parentPanel;
    private final BookVO editingBook;

    public LibraryBookEditDialogModule(LibraryController controller, LibraryBookManageModule parent, BookVO book) {
        this.controller = controller;
        this.parentPanel = parent;
        this.editingBook = book;

        setTitle("编辑书籍");
        setSize(400, 550);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new GridLayout(11, 2, 5, 5)); // 10 个字段 + 2 按钮

        // ==== 表单字段 ====
        add(new JLabel("书名:"));
        txtTitle = new JTextField(book.getTitle());
        add(txtTitle);

        add(new JLabel("作者:"));
        txtAuthor = new JTextField(book.getAuthor());
        add(txtAuthor);

        add(new JLabel("ISBN:"));
        txtIsbn = new JTextField(book.getIsbn());
        txtIsbn.setEditable(false); // ISBN 通常不允许改
        add(txtIsbn);

        add(new JLabel("出版社:"));
        txtPublisher = new JTextField(book.getPublisher());
        add(txtPublisher);

        add(new JLabel("分类:"));
        txtCategory = new JTextField(book.getCategory());
        add(txtCategory);

        add(new JLabel("总数:"));
        txtTotal = new JTextField(String.valueOf(book.getTotalStock()));
        add(txtTotal);

        add(new JLabel("可借:"));
        txtAvailable = new JTextField(String.valueOf(book.getAvailableStock()));
        add(txtAvailable);

        add(new JLabel("出版日期(yyyy-MM-dd):"));
        txtPubDate = new JTextField(book.getPublicationDate() != null ? book.getPublicationDate().toString() : "");
        add(txtPubDate);

        add(new JLabel("馆藏位置:"));
        txtLocation = new JTextField(book.getLocation());
        add(txtLocation);

        add(new JLabel("状态:"));
        comboStatus = new JComboBox<>(new String[]{"available", "unavailable"});
        comboStatus.setSelectedItem(book.getStatus() != null ? book.getStatus() : "available");
        add(comboStatus);

        // ==== 按钮 ====
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");
        add(btnSave);
        add(btnCancel);

        // ==== 保存逻辑 ====
        btnSave.addActionListener(e -> {
            try {
                editingBook.setTitle(txtTitle.getText());
                editingBook.setAuthor(txtAuthor.getText());
                editingBook.setPublisher(txtPublisher.getText());
                editingBook.setCategory(txtCategory.getText());
                editingBook.setTotalStock(Integer.parseInt(txtTotal.getText()));
                editingBook.setAvailableStock(Integer.parseInt(txtAvailable.getText()));

                String pubDateStr = txtPubDate.getText().trim();
                if (!pubDateStr.isEmpty()) {
                    editingBook.setPublicationDate(java.sql.Date.valueOf(pubDateStr));
                } else {
                    editingBook.setPublicationDate(null);
                }

                editingBook.setLocation(txtLocation.getText());
                editingBook.setStatus(comboStatus.getSelectedItem().toString());

                if (controller.submitUpdateBook(editingBook)) {
                    JOptionPane.showMessageDialog(this, "修改成功！");
                    parentPanel.refreshTable();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败！");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入有误: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }
}
