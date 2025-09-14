package client.ui.modules;

import client.controller.StoreController;
import common.vo.ProductVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class StoreProductManageModule extends JPanel {
    private StoreController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public StoreProductManageModule(StoreController controller, common.vo.UserVO currentUser) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("新增");
        JButton btnEdit = new JButton("编辑");
        JButton btnDelete = new JButton("删除");
        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);

        // 表格
        String[] columns = {"ID", "名称", "描述", "分类", "价格", "库存"};
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

        // 新增按钮
        btnAdd.addActionListener(e -> {
            StoreProductAddModule addPanel = new StoreProductAddModule(controller);
            int result = JOptionPane.showConfirmDialog(this, addPanel, "新增商品", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // 这里的提交逻辑已在 StoreProductAddModule 内部处理
            if (result == JOptionPane.OK_OPTION) {
                refreshTable();
            }
        });

        // 编辑按钮
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int productId = (int) tableModel.getValueAt(row, 0);
                ProductVO product = controller.getProductById(productId);
                if (product != null) {
                    StoreProductEditDialogModule dialog = new StoreProductEditDialogModule(controller, this, product);
                    dialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "未找到该商品的详细信息！");
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一个商品再编辑！");
            }
        });

        // 删除按钮
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int productId = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "确认删除该商品？", "删除确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.deleteProduct(productId)) {
                        JOptionPane.showMessageDialog(this, "删除成功");
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败，可能存在未完成订单");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "请选择要删除的商品");
            }
        });
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<ProductVO> products = controller.searchProducts(""); // 搜索全部
        for (ProductVO p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getProductName(),
                    p.getDescription(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getStock()
            });
        }
    }
}
