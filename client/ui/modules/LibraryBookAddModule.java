package client.ui.modules;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import java.awt.*;

public class LibraryBookAddModule extends JPanel {
    private JTextField txtBookId, txtTitle, txtAuthor, txtIsbn, txtPublisher,
            txtCategory, txtTotal, txtAvailable, txtPubDate, txtLocation;
    private LibraryController controller;

    public LibraryBookAddModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new GridLayout(11, 2, 5, 5)); // 10 个字段 + 2 个按钮 = 11 行

        add(new JLabel("书籍ID(可选):"));
        txtBookId = new JTextField();
        add(txtBookId);

        add(new JLabel("书名:"));
        txtTitle = new JTextField();
        add(txtTitle);

        add(new JLabel("作者:"));
        txtAuthor = new JTextField();
        add(txtAuthor);

        add(new JLabel("ISBN:"));
        txtIsbn = new JTextField();
        add(txtIsbn);

        add(new JLabel("出版社:"));
        txtPublisher = new JTextField();
        add(txtPublisher);

        add(new JLabel("分类:"));
        txtCategory = new JTextField();
        add(txtCategory);

        add(new JLabel("总数:"));
        txtTotal = new JTextField();
        add(txtTotal);

        add(new JLabel("可借:"));
        txtAvailable = new JTextField();
        add(txtAvailable);

        add(new JLabel("出版日期(yyyy-MM-dd):"));
        txtPubDate = new JTextField();
        add(txtPubDate);

        add(new JLabel("馆藏位置:"));
        txtLocation = new JTextField();
        add(txtLocation);

        JButton btnSubmit = new JButton("提交");
        JButton btnReset = new JButton("重置");
        add(btnSubmit);
        add(btnReset);

        btnSubmit.addActionListener(e -> {
            try {
                BookVO b = new BookVO();

                // 可选 bookId
                String idStr = txtBookId.getText().trim();
                if (!idStr.isEmpty()) {
                    b.setBookId(Integer.parseInt(idStr));
                }

                b.setTitle(txtTitle.getText());
                b.setAuthor(txtAuthor.getText());
                b.setIsbn(txtIsbn.getText());
                b.setPublisher(txtPublisher.getText());
                b.setCategory(txtCategory.getText());
                b.setTotalStock(Integer.parseInt(txtTotal.getText()));
                b.setAvailableStock(Integer.parseInt(txtAvailable.getText()));

                String pubDateStr = txtPubDate.getText().trim();
                if (!pubDateStr.isEmpty()) {
                    b.setPublicationDate(java.sql.Date.valueOf(pubDateStr));
                }

                b.setLocation(txtLocation.getText());

                if (controller.submitAddBook(b)) {
                    JOptionPane.showMessageDialog(this, "新增成功！");
                    clearFields();
                } else {
                    if (!txtBookId.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "新增失败：书籍ID已存在！");
                    } else {
                        JOptionPane.showMessageDialog(this, "新增失败，请检查数据！");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入有误: " + ex.getMessage());
            }
        });

        btnReset.addActionListener(e -> clearFields());
    }

    private void clearFields() {
        txtBookId.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtIsbn.setText("");
        txtPublisher.setText("");
        txtCategory.setText("");
        txtTotal.setText("");
        txtAvailable.setText("");
        txtPubDate.setText("");
        txtLocation.setText("");
    }
}
