package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BookVO;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

public class LibraryBookEditDialogModule extends JDialog {
    private JTextField txtTitle, txtAuthor, txtIsbn, txtPublisher,
            txtCategory, txtTotal, txtAvailable, txtPubDate, txtLocation;
    private JComboBox<String> comboStatus;

    private final LibraryController controller;
    private final LibraryBookManageModule parentPanel; // 假设有这个父面板用于刷新
    private final BookVO editingBook;

    private Color themeColor = new Color(0, 64, 0); // 深绿色
    private Color hoverColor = new Color(0, 100, 0); // 浅一点的绿色

    public LibraryBookEditDialogModule(LibraryController controller, LibraryBookManageModule parent, BookVO book) {
        this.controller = controller;
        this.parentPanel = parent;
        this.editingBook = book;

        setTitle("编辑书籍");
        setSize(450, 600); // 调整尺寸
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());
        initUI();
    }

    // 共享的创建现代风格按钮的方法
    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color backgroundColor = getModel().isRollover() ? hoverColor : themeColor;
                g2.setColor(backgroundColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE); // 确保文字是白色
                g2.setFont(getFont().deriveFont(Font.BOLD)); // 加粗字体
                g2.drawString(getText(), textX, textY);
                g2.dispose();
                // 移除 super.paintComponent(g); 以避免默认绘制覆盖
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        return button;
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout()); // 改用GridBagLayout
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 增加边距
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // 增加组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充

        int row = 0;

        // 书名
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("书名:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; txtTitle = new JTextField(editingBook.getTitle(), 25); formPanel.add(txtTitle, gbc);
        row++;

        // 作者
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtAuthor = new JTextField(editingBook.getAuthor(), 25); formPanel.add(txtAuthor, gbc);
        row++;

        // ISBN (不可编辑)
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtIsbn = new JTextField(editingBook.getIsbn(), 25); txtIsbn.setEditable(false); formPanel.add(txtIsbn, gbc);
        row++;

        // 出版社
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("出版社:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtPublisher = new JTextField(editingBook.getPublisher(), 25); formPanel.add(txtPublisher, gbc);
        row++;

        // 分类
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtCategory = new JTextField(editingBook.getCategory(), 25); formPanel.add(txtCategory, gbc);
        row++;

        // 总数
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("总数:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtTotal = new JTextField(String.valueOf(editingBook.getTotalStock()), 10); formPanel.add(txtTotal, gbc);
        row++;

        // 可借
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("可借:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtAvailable = new JTextField(String.valueOf(editingBook.getAvailableStock()), 10); formPanel.add(txtAvailable, gbc);
        row++;

        // 出版日期
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("出版日期(yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtPubDate = new JTextField(editingBook.getPublicationDate() != null ? editingBook.getPublicationDate().toString() : "", 15); formPanel.add(txtPubDate, gbc);
        row++;

        // 馆藏位置
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("馆藏位置:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; txtLocation = new JTextField(editingBook.getLocation(), 25); formPanel.add(txtLocation, gbc);
        row++;

        // 状态
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; formPanel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; comboStatus = new JComboBox<>(new String[]{"available", "unavailable"});
        comboStatus.setSelectedItem(editingBook.getStatus() != null ? editingBook.getStatus() : "available");
        formPanel.add(comboStatus, gbc);
        row++;


        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JButton btnSave = createModernButton("保存");
        JButton btnCancel = createModernButton("取消");
        btnPanel.add(btnSave);
        btnPanel.add(Box.createHorizontalStrut(20)); // 增加按钮间距
        btnPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            try {
                editingBook.setTitle(txtTitle.getText().trim());
                editingBook.setAuthor(txtAuthor.getText().trim());
                editingBook.setPublisher(txtPublisher.getText().trim());
                editingBook.setCategory(txtCategory.getText().trim());
                editingBook.setTotalStock(Integer.parseInt(txtTotal.getText().trim()));
                editingBook.setAvailableStock(Integer.parseInt(txtAvailable.getText().trim()));

                String pubDateStr = txtPubDate.getText().trim();
                if (!pubDateStr.isEmpty()) {
                    editingBook.setPublicationDate(java.sql.Date.valueOf(pubDateStr));
                } else {
                    editingBook.setPublicationDate(null);
                }

                editingBook.setLocation(txtLocation.getText().trim());
                editingBook.setStatus(Objects.requireNonNull(comboStatus.getSelectedItem()).toString());

                if (controller.submitUpdateBook(editingBook)) {
                    JOptionPane.showMessageDialog(this, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    if (parentPanel != null) {
                        parentPanel.refreshTable(); // 刷新父面板的表格
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败！", "失败", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "总数和可借数量必须是有效的数字。", "输入错误", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "出版日期格式不正确，请使用 YYYY-MM-DD 格式。", "输入错误", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}