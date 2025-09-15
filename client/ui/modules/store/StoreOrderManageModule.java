package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.OrderItemVO;
import common.vo.OrderVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.List;

public class StoreOrderManageModule extends JPanel {
    private JTable tableOrders;
    private OrderManageTableModel tableModel;
    private final UserVO currentUser;
    private final StoreController controller;

    public StoreOrderManageModule(StoreController controller, UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 创建表格模型
        tableModel = new OrderManageTableModel();
        tableOrders = new JTable(tableModel);

        // 设置按钮渲染器
        TableColumn actionColumn = tableOrders.getColumnModel().getColumn(6);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new OrderButtonEditor(new JCheckBox(), controller, this));

        // 设置表格样式
        tableOrders.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableOrders.setRowHeight(35);
        tableOrders.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        tableOrders.getTableHeader().setReorderingAllowed(false);
        tableOrders.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // 设置列宽
        tableOrders.getColumnModel().getColumn(0).setPreferredWidth(100);   // 订单ID
        tableOrders.getColumnModel().getColumn(1).setPreferredWidth(120);  // 客户ID
        tableOrders.getColumnModel().getColumn(2).setPreferredWidth(150);  // 客户名称
        tableOrders.getColumnModel().getColumn(3).setPreferredWidth(120);  // 订单日期
        tableOrders.getColumnModel().getColumn(4).setPreferredWidth(100);  // 总金额
        tableOrders.getColumnModel().getColumn(5).setPreferredWidth(100);  // 状态
        tableOrders.getColumnModel().getColumn(6).setPreferredWidth(120);  // 操作

        add(new JScrollPane(tableOrders), BorderLayout.CENTER);
        refreshTable();
    }

    public void refreshTable() {
        List<OrderVO> orders = controller.getAllUserOrders();
        tableModel.setOrders(orders);
    }

    // ===== 表格模型 =====
    private static class OrderManageTableModel extends AbstractTableModel {
        private String[] columnNames = {
            "订单ID", "客户ID", "客户名称", "订单日期", "总金额", "状态", "操作"
        };
        private List<OrderVO> orders;

        public void setOrders(List<OrderVO> orders) {
            this.orders = orders;
            fireTableDataChanged();
        }

        public OrderVO getOrderAt(int row) {
            if (orders == null || row < 0 || row >= orders.size()) return null;
            return orders.get(row);
        }

        @Override
        public int getRowCount() {
            return orders == null ? 0 : orders.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (orders == null || row < 0 || row >= orders.size()) return null;

            OrderVO order = orders.get(row);
            switch (col) {
                case 0: return order.getOrderOn();
                case 1: return order.getUserId();
                case 2: return order.getUserName();
                case 3: return order.getCreatedTime();
                case 4: return "¥" + order.getTotalAmount().toString();
                case 5: return order.getStatus();
                case 6: return "管理订单";
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 6; // 最后一列可编辑（操作按钮列）
        }
    }

    // ===== 按钮渲染器 =====
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");

            // 设置按钮样式
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(Color.WHITE);

            // 根据状态设置背景色
            if (isSelected) {
                setBackground(new Color(76, 124, 204));
            } else {
                setBackground(new Color(102, 153, 102));
            }

            return this;
        }
    }

    // ===== 按钮编辑器 =====
    private class OrderButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private final StoreController controller;
        private final StoreOrderManageModule panel;
        private int editingRow = -1;

        public OrderButtonEditor(JCheckBox checkBox, StoreController controller,
                               StoreOrderManageModule panel) {
            super(checkBox);
            this.controller = controller;
            this.panel = panel;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);

            // 设置按钮样式
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setForeground(Color.WHITE);

            if (isSelected) {
                button.setBackground(new Color(76, 124, 204));
            } else {
                button.setBackground(new Color(102, 153, 102));
            }

            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            isPushed = true;
            editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && editingRow >= 0) {
                OrderVO order = panel.tableModel.getOrderAt(editingRow);
                if (order != null) {
                    showOrderManagementDialog(order);
                }
            }
            isPushed = false;
            editingRow = -1;
            return label;
        }

        private void showOrderManagementDialog(OrderVO order) {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(panel);
            JDialog dialog = new JDialog(parentFrame,
                                      "订单管理 - 订单ON.: " + order.getOrderOn(), true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(panel);

            // 创建主面板
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // 订单信息面板
            JPanel orderInfoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            orderInfoPanel.setBorder(BorderFactory.createTitledBorder("订单信息"));
            orderInfoPanel.add(new JLabel("订单ON:"));
            orderInfoPanel.add(new JLabel(String.valueOf(order.getOrderOn())));
            orderInfoPanel.add(new JLabel("客户ID:"));
            orderInfoPanel.add(new JLabel(String.valueOf(order.getUserId())));
            orderInfoPanel.add(new JLabel("客户名称:"));
            orderInfoPanel.add(new JLabel(order.getUserName()));
            orderInfoPanel.add(new JLabel("订单日期:"));
            orderInfoPanel.add(new JLabel(order.getCreatedTime() != null ? order.getCreatedTime().toString() : ""));
            orderInfoPanel.add(new JLabel("总金额:"));
            orderInfoPanel.add(new JLabel("¥" + order.getTotalAmount().toString()));
            orderInfoPanel.add(new JLabel("当前状态:"));
            orderInfoPanel.add(new JLabel(String.valueOf(order.getStatus())));

            // 商品列表
            JPanel itemsPanel = new JPanel(new BorderLayout());
            itemsPanel.setBorder(BorderFactory.createTitledBorder("商品列表"));

            // 创建商品表格
            String[] itemColumns = {"商品ID", "商品名称", "数量", "单价"};
            DefaultTableModel itemModel = new DefaultTableModel(itemColumns, 0);

            // 添加商品数据
            if (order.getItems() != null) {
                for (OrderItemVO item : order.getItems()) {
                    itemModel.addRow(new Object[]{
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        "¥" + item.getUnitPrice().toString()
                    });
                }
            }

            JTable itemTable = new JTable(itemModel);
            itemsPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

            // 添加状态更新按钮
            JButton markShippedBtn = new JButton("标记为已发货");
            markShippedBtn.addActionListener(e -> updateOrderStatus(order, "已发货", dialog));

            JButton markDeliveredBtn = new JButton("标记为已送达");
            markDeliveredBtn.addActionListener(e -> updateOrderStatus(order, "已送达", dialog));

            JButton cancelOrderBtn = new JButton("取消订单");
            cancelOrderBtn.addActionListener(e -> updateOrderStatus(order, "已取消", dialog));

            buttonPanel.add(markShippedBtn);
            buttonPanel.add(markDeliveredBtn);
            buttonPanel.add(cancelOrderBtn);

            // 关闭按钮
            JButton closeBtn = new JButton("关闭");
            closeBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeBtn);

            // 组装面板
            mainPanel.add(orderInfoPanel, BorderLayout.NORTH);
            mainPanel.add(itemsPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel);
            dialog.setVisible(true);
        }

        private void updateOrderStatus(OrderVO order, String newStatus, JDialog dialog) {
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "确定要将订单 " + order.getOrderOn() + " 状态更新为: " + newStatus + " 吗？",
                "确认状态更新",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = controller.updateOrderStatus(order.getOrderOn(), newStatus);
                    if (success) {
                        JOptionPane.showMessageDialog(
                            dialog,
                            "订单状态已更新为: " + newStatus,
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        panel.refreshTable(); // 刷新表格数据
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                            dialog,
                            "状态更新失败，请重试",
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        dialog,
                        "更新时发生错误: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
}