package client.ui.modules;

import client.controller.StoreController;
import client.ui.modules.StoreProductManageModule;
import common.vo.ProductVO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class StoreProductEditDialogModule extends JDialog {
    private JTextField txtProductName, txtDescription, txtCategory, txtPrice, txtStock;
    private JComboBox<String> comboStatus;

    private final StoreController controller;
    private final StoreProductManageModule parentPanel;
    private final ProductVO editingProduct;

    public StoreProductEditDialogModule(StoreController controller, StoreProductManageModule parent, ProductVO product) {
        this.controller = controller;
        this.parentPanel = parent;
        this.editingProduct = product;

        setTitle("编辑商品");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new GridLayout(7, 2, 5, 5)); // 6字段+2按钮

        // ==== 表单字段 ====
        add(new JLabel("商品名称:"));
        txtProductName = new JTextField(product.getProductName());
        add(txtProductName);

        add(new JLabel("描述:"));
        txtDescription = new JTextField(product.getDescription());
        add(txtDescription);

        add(new JLabel("分类:"));
        txtCategory = new JTextField(product.getCategory());
        add(txtCategory);

        add(new JLabel("价格(元):"));
        txtPrice = new JTextField(product.getPrice() != null ? product.getPrice().toString() : "");
        add(txtPrice);

        add(new JLabel("库存数量:"));
        txtStock = new JTextField(String.valueOf(product.getStock()));
        add(txtStock);

        add(new JLabel("状态:"));
        comboStatus = new JComboBox<>(new String[]{"有货", "缺货"});
        comboStatus.setSelectedItem(product.getStock() > 0 ? "有货" : "缺货");
        add(comboStatus);

        // ==== 按钮 ====
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");
        add(btnSave);
        add(btnCancel);

        // ==== 保存逻辑 ====
        btnSave.addActionListener(e -> {
            try {
                editingProduct.setProductName(txtProductName.getText().trim());
                editingProduct.setDescription(txtDescription.getText().trim());
                editingProduct.setCategory(txtCategory.getText().trim());
                editingProduct.setPrice(Double.valueOf(txtPrice.getText().trim()));
                editingProduct.setStock(Integer.parseInt(txtStock.getText().trim()));
                // 状态由库存决定，也可单独存储
                if ("缺货".equals(comboStatus.getSelectedItem())) {
                    editingProduct.setStock(0);
                }

                if (controller.updateProduct(editingProduct)) {
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

        btnCancel.addActionListener(e ->dispose());
    }
}
