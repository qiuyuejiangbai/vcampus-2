package client.ui.modules;


import client.controller.StoreController;
import common.vo.ProductVO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class StoreProductAddModule extends JPanel {
    private JTextField txtProductId, txtProductName, txtDescription, txtCategory, txtPrice, txtStock;
    private StoreController controller;

    public StoreProductAddModule(StoreController controller) {
        this.controller = controller;
        setLayout(new GridLayout(7, 2, 5, 5)); // 6字段+2按钮=7行

        add(new JLabel("商品ID(可选):"));
        txtProductId = new JTextField();
        add(txtProductId);

        add(new JLabel("商品名称:"));
        txtProductName = new JTextField();
        add(txtProductName);

        add(new JLabel("描述:"));
        txtDescription = new JTextField();
        add(txtDescription);

        add(new JLabel("分类:"));
        txtCategory = new JTextField();
        add(txtCategory);

        add(new JLabel("价格(元):"));
        txtPrice = new JTextField();
        add(txtPrice);

        add(new JLabel("库存数量:"));
        txtStock = new JTextField();
        add(txtStock);

        JButton btnSubmit = new JButton("提交");
        JButton btnReset = new JButton("重置");
        add(btnSubmit);
        add(btnReset);

        btnSubmit.addActionListener(e -> {
            try {
                ProductVO p = new ProductVO();

                // 可选 productId
                String idStr = txtProductId.getText().trim();
                if (!idStr.isEmpty()) {
                    p.setProductId(Integer.parseInt(idStr));
                }

                p.setProductName(txtProductName.getText().trim());
                p.setDescription(txtDescription.getText().trim());
                p.setCategory(txtCategory.getText().trim());
                p.setPrice(new BigDecimal(txtPrice.getText().trim()).doubleValue());
                p.setStock(Integer.parseInt(txtStock.getText().trim()));

                if (controller.addProduct(p)) {
                    JOptionPane.showMessageDialog(this, "新增商品成功！");
                    clearFields();
                } else {
                    if (!txtProductId.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "新增失败：商品ID已存在！");
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
        txtProductId.setText("");
        txtProductName.setText("");
        txtDescription.setText("");
        txtCategory.setText("");
        txtPrice.setText("");
        txtStock.setText("");}}