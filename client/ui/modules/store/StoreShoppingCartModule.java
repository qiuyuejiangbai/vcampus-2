package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.ShoppingCartItemVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

// TODO: Replace with the correct package if CheckoutDialog is in a different location
import client.ui.modules.store.CheckoutPanel;

public class StoreShoppingCartModule extends JPanel {
    private JTable tableCart;
    private CartTableModel tableModel;
    private final UserVO currentUser;
    private final StoreController controller;
    private JButton btnCheckout;
    private JLabel totalLabel;

    public StoreShoppingCartModule(StoreController controller, UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = controller;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 创建表格模型
        tableModel = new CartTableModel();
        tableCart = new JTable(tableModel);

        // 设置表格渲染
        CartButtonRenderer buttonRenderer = new CartButtonRenderer();
        TableColumnModel columnModel = tableCart.getColumnModel();

        // 设置列宽
        TableColumn idColumn = columnModel.getColumn(0);
        idColumn.setPreferredWidth(80);
        idColumn.setMaxWidth(100);

        TableColumn nameColumn = columnModel.getColumn(1);
        nameColumn.setPreferredWidth(200);
        nameColumn.setMaxWidth(300);

        TableColumn priceColumn = columnModel.getColumn(2);
        priceColumn.setPreferredWidth(100);
        priceColumn.setMaxWidth(150);

        TableColumn qtyColumn = columnModel.getColumn(3);
        qtyColumn.setPreferredWidth(100);
        qtyColumn.setMaxWidth(150);

        TableColumn subtotalColumn = columnModel.getColumn(4);
        subtotalColumn.setPreferredWidth(100);
        subtotalColumn.setMaxWidth(150);

        // 添加删除按钮列
        TableColumn deleteColumn = columnModel.getColumn(5);
        deleteColumn.setPreferredWidth(100);
        deleteColumn.setCellRenderer(buttonRenderer);

        // 初始化编辑器
        for (int i = 0; i < tableModel.getColumnCount() - 1; i++) {
            tableCart.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()));
        }
        deleteColumn.setCellEditor(new CartButtonEditor(new JCheckBox(), controller, this));

        // 设置表格样式
        tableCart.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableCart.setRowHeight(35);
        tableCart.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        tableCart.getTableHeader().setReorderingAllowed(false);
        tableCart.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // 设置表格选中颜色
        tableCart.setSelectionBackground(new Color(102, 153, 255));
        tableCart.setSelectionForeground(Color.WHITE);

        // 设置网格线
        tableCart.setGridColor(new Color(200, 200, 200));
        tableCart.setShowGrid(true);

        // 总计面板
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        totalLabel = new JLabel("总计: ¥0.00");
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        totalLabel.setForeground(new Color(220, 20, 60));
        summaryPanel.add(totalLabel);

        // 滚动面板样式
        JScrollPane scrollPane = new JScrollPane(tableCart);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 底部操作栏
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnCheckout = new JButton("去结算");
        btnCheckout.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnCheckout.setPreferredSize(new Dimension(120, 40));
        btnCheckout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnClear = new JButton("清空购物车");
        btnClear.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnClear.setPreferredSize(new Dimension(110, 35));
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
       
        btnCheckout.addActionListener(this::checkout);
        btnClear.addActionListener(this::clearCart);

        buttonPanel.add(btnClear);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(btnCheckout);

        // 添加组件到主面板
        add(summaryPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        try {
            List<ShoppingCartItemVO> items = controller.getShoppingCart();
            tableModel.setItems(items);
            updateTotal();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载购物车失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotal() {
        double total = 0;
        for (ShoppingCartItemVO item : tableModel.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        totalLabel.setText(String.format("总计: ¥%.2f", total));
    }

    private void checkout(ActionEvent e) {
        if (tableModel.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "购物车为空，无法结算！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CheckoutPanel dialog = new CheckoutPanel(controller, currentUser, tableModel.getItems());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            controller.clearCart();
            tableModel.setItems(null);
            updateTotal();
            JOptionPane.showMessageDialog(this, "下单成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearCart(ActionEvent e) {
        if (tableModel.getItemCount() == 0) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要清空购物车吗？",
            "确认清空",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.clearCart();
                tableModel.setItems(null);
                updateTotal();
                JOptionPane.showMessageDialog(this, "购物车已清空", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "清空购物车失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 购物车表格模型
    private class CartTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "商品ID", "商品名称", "单价(元)", "数量", "小计(元)", "操作"
        };

        private List<ShoppingCartItemVO> items;

        public void setItems(List<ShoppingCartItemVO> items) {
            this.items = items;
            fireTableDataChanged();
        }

        public List<ShoppingCartItemVO> getItems() {
            return items;
        }

        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getRowCount() {
            return getItemCount();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Integer.class;
                case 1: return String.class;
                case 2: return Double.class;
                case 3: return Integer.class;
                case 4: return Double.class;
                case 5: return String.class;
                default: return Object.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (items == null || rowIndex < 0 || rowIndex >= items.size()) return null;

            ShoppingCartItemVO item = items.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.getProductId();
                case 1: return item.getProductName();
                case 2: return item.getPrice();
                case 3: return item.getQuantity();
                case 4: return item.getPrice() * item.getQuantity();
                case 5: return "删除";
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // 允许编辑数量和操作列
            return columnIndex == 3 || columnIndex == 5;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 3) {
                try {
                    int newQuantity;
                    if (aValue instanceof Integer) {
                        newQuantity = (Integer) aValue;
                    } else {
                        newQuantity = Integer.parseInt(aValue.toString());
                    }
                    if (newQuantity > 0) {
                        ShoppingCartItemVO item = items.get(rowIndex);
                        controller.updateCartItem(item.getProductId(), newQuantity);
                        item.setQuantity(newQuantity);
                        fireTableCellUpdated(rowIndex, columnIndex);
                        fireTableCellUpdated(rowIndex, 4); // 更新小计
                        updateTotal(); // 更新总计
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "更新失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    fireTableDataChanged(); // 恢复原始数据
                }
            }
        }
    }

    // 按钮渲染器
    private static class CartButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public CartButtonRenderer() {
            setOpaque(true);
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            setBackground(isSelected ? new Color(153, 153, 255) :
                         new Color(230, 230, 230));
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return this;
        }
    }

    // 按钮编辑器
    private class CartButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final StoreController controller;
        private final StoreShoppingCartModule cartModule;
        private int editingRow = -1;

        public CartButtonEditor(JCheckBox checkBox, StoreController controller,
                               StoreShoppingCartModule cartModule) {
            super(checkBox);
            this.controller = controller;
            this.cartModule = cartModule;
            this.button = new JButton();
            this.button.setOpaque(true);
            this.button.setFont(new Font("微软雅黑", Font.PLAIN, 12));

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(isSelected ? new Color(153, 153, 255) :
                                 new Color(230, 230, 230));
            isPushed = true;
            editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && "删除".equals(label) && editingRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                    cartModule,
                    "确定要从购物车中移除此商品吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        ShoppingCartItemVO item = tableModel.getItems().get(editingRow);
                        controller.removeFromCart(item.getProductId());
                        tableModel.getItems().remove(editingRow);
                        tableModel.fireTableDataChanged();
                        updateTotal();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            cartModule,
                            "删除失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
            isPushed = false;
            editingRow = -1;
            return "";
        }
    }
}