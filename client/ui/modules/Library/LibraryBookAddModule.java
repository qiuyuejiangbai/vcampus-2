package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import java.awt.*;

public class LibraryBookAddModule extends JPanel {
    private JTextField txtBookId, txtTitle, txtAuthor, txtIsbn, txtPublisher,
            txtCategory, txtTotal, txtAvailable, txtPubDate, txtLocation;
    private final LibraryController controller;

    public LibraryBookAddModule(LibraryController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        txtBookId = addField(formPanel, gbc, row++, "书籍ID(可选):");
        txtTitle = addField(formPanel, gbc, row++, "书名:");
        txtAuthor = addField(formPanel, gbc, row++, "作者:");
        txtIsbn = addField(formPanel, gbc, row++, "ISBN:");
        txtPublisher = addField(formPanel, gbc, row++, "出版社:");
        txtCategory = addField(formPanel, gbc, row++, "分类:");
        txtTotal = addField(formPanel, gbc, row++, "总数:");
        txtAvailable = addField(formPanel, gbc, row++, "可借:");

        // 用 HTML 标签实现换行提示
        txtPubDate = addField(formPanel, gbc, row++, "<html>出版日期<br>(yyyy-MM-dd):</html>");
        txtLocation = addField(formPanel, gbc, row++, "馆藏位置:");

        add(formPanel, BorderLayout.CENTER);

        // --- 底部按钮 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSubmit = createModernButton("提交");
        JButton btnReset = createModernButton("重置");
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnReset);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件绑定
        btnSubmit.addActionListener(e -> handleSubmit());
        btnReset.addActionListener(e -> clearFields());
    }

    private JTextField addField(JPanel panel, GridBagConstraints gbc, int row, String labelText) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField field = new JTextField();
        panel.add(field, gbc);

        return field;
    }

    private JButton createModernButton(String text) {
        Color themeColor = new Color(0, 64, 0);
        Color hoverColor = new Color(0, 100, 0);

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
                int textHeight = fm.getAscent();
                int textY = (getHeight() - fm.getHeight()) / 2 + textHeight;
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, textY);

                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(90, 32));
        return button;
    }

    private void handleSubmit() {
        try {
            BookVO b = new BookVO();

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
