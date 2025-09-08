package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.OrderVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class StoreOrderHistoryModule extends JPanel {
    private JTable tableHistory;
    private OrderHistoryTableModel tableModel;
    private final UserVO currentUser;
    private final StoreController controller;

    public StoreOrderHistoryModule(StoreController controller, UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = controller;

        setLayout(new BorderLayout());

        tableModel = new OrderHistoryTableModel();
        tableHistory = new JTable(tableModel);

        // 设置按钮渲染器和编辑器
        TableColumn colDetails = tableHistory.getColumnModel().getColumn(5);
        colDetails.setCellRenderer(new ButtonRenderer());
        colDetails.setCellEditor(new OrderButtonEditor(new JCheckBox(), controller, this, "details", -1));

        TableColumn colTrack = tableHistory.getColumnModel().getColumn(6);
        colTrack.setCellRenderer(new ButtonRenderer());
        colTrack.setCellEditor(new OrderButtonEditor(new JCheckBox(), controller, this, "track", -1));

        TableColumn colReview = tableHistory.getColumnModel().getColumn(7);
        colReview.setCellRenderer(new ButtonRenderer());
        colReview.setCellEditor(new OrderButtonEditor(new JCheckBox(), controller, this, "review", -1));
        
        TableColumn colReorder = tableHistory.getColumnModel().getColumn(8);
        colReorder.setCellRenderer(new ButtonRenderer());
        colReorder.setCellEditor(new OrderButtonEditor(new JCheckBox(), controller, this, "reorder", -1));

        // 设置表格样式
        tableHistory.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableHistory.setRowHeight(35);
        tableHistory.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        tableHistory.getTableHeader().setReorderingAllowed(false);
        
        // 设置列宽
        tableHistory.getColumnModel().getColumn(0).setPreferredWidth(100); // 订单ID
        tableHistory.getColumnModel().getColumn(1).setPreferredWidth(120); // 订单日期
        tableHistory.getColumnModel().getColumn(2).setPreferredWidth(100); // 总金额
        tableHistory.getColumnModel().getColumn(3).setPreferredWidth(100); // 状态
        tableHistory.getColumnModel().getColumn(4).setPreferredWidth(200); // 收货地址
        tableHistory.getColumnModel().getColumn(5).setPreferredWidth(80);  // 详情
        tableHistory.getColumnModel().getColumn(6).setPreferredWidth(80);  // 物流跟踪
        tableHistory.getColumnModel().getColumn(7).setPreferredWidth(80);  // 评价
        tableHistory.getColumnModel().getColumn(8).setPreferredWidth(80);  // 再次购买

        add(new JScrollPane(tableHistory), BorderLayout.CENTER);
        loadOrderHistory();
    }

    public JTable getTableHistory() {
        return tableHistory;
    }

    public void loadOrderHistory() {
        List<OrderVO> orders = controller.getUserOrders(currentUser.getUserId());
        tableModel.setOrders(orders);
    }

    public void refreshTable() {
        loadOrderHistory();
    }

    // ===== 表格模型 =====
    private static class OrderHistoryTableModel extends AbstractTableModel {
        private String[] columnNames = {
            "订单ID", "订单日期", "总金额", "状态", "收货地址", "详情", "物流跟踪", "评价", "再次购买"
        };
        private List<OrderVO> orders;

        public void setOrders(List<OrderVO> orders) {
            this.orders = orders;
            fireTableDataChanged();
        }

        public OrderVO getOrderAt(int row) {
            return orders == null ? null : orders.get(row);
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
                case 0: return order.getOrderId();
                case 1: return order.getOrderDate();
                case 2: return "¥" + order.getTotalAmount().toString();
                case 3: return order.getStatus();
                case 4: return order.getShippingAddress();
                case 5: return "查看详情";
                case 6: return order.getStatus().equals("已发货") ? "物流跟踪" : 
                         order.getStatus().equals("已完成") ? "物流跟踪" : "";
                case 7: return order.getStatus().equals("已完成") && !order.isReviewed() ? "评价" : 
                         order.getStatus().equals("已完成") && order.isReviewed() ? "已评价" : "";
                case 8: return order.getStatus().equals("已完成") ? "再次购买" : 
                         order.getStatus().equals("待发货") ? "再次购买" : "";
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex < 5 ? String.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex >= 5; // 操作列可编辑
        }
    }

    // ===== 按钮渲染器 =====
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            
            // 根据状态设置按钮状态
            if (!getText().isEmpty()) {
                setEnabled(true);
                setBackground(isSelected ? new Color(153, 153, 255) : new Color(230, 230, 230));
            } else {
                setEnabled(false);
                setBackground(isSelected ? new Color(230, 230, 230) : new Color(245, 245, 245));
            }
            
            setForeground(isSelected ? Color.WHITE : Color.BLACK);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return this;
        }
    }

    // ===== 按钮编辑器 =====
    private class OrderButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String actionType; // "details", "track", "review", "reorder"
        private boolean clicked;
        private int row;
        private StoreController controller;
        private StoreOrderHistoryModule panel;

        public OrderButtonEditor(JCheckBox checkBox, StoreController controller, 
                               StoreOrderHistoryModule panel, String actionType, int rowId) {
            super(checkBox);
            this.controller = controller;
            this.panel = panel;
            this.actionType = actionType;
            this.row = rowId;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            OrderHistoryTableModel model = (OrderHistoryTableModel) table.getModel();
            OrderVO order = model.getOrderAt(row);

            this.row = row;
            clicked = true;

            // 设置按钮文字 & 可用性
            if (order != null) {
                switch (actionType) {
                    case "details":
                        button.setText("查看详情");
                        button.setEnabled(true);
                        break;
                    case "track":
                        if (order.getStatus().equals("已发货") || order.getStatus().equals("已完成")) {
                            button.setText("物流跟踪");
                            button.setEnabled(true);
                        } else {
                            button.setText("");
                            button.setEnabled(false);
                        }
                        break;
                    case "review":
                        if (order.getStatus().equals("已完成") && !order.isReviewed()) {
                            button.setText("评价");
                            button.setEnabled(true);
                        } else if (order.getStatus().equals("已完成") && order.isReviewed()) {
                            button.setText("已评价");
                            button.setEnabled(false);
                        } else {
                            button.setText("");
                            button.setEnabled(false);
                        }
                        break;
                    case "reorder":
                        if (order.getStatus().equals("已完成") || order.getStatus().equals("待发货")) {
                            button.setText("再次购买");
                            button.setEnabled(true);
                        } else {
                            button.setText("");
                            button.setEnabled(false);
                        }
                        break;
                }
            } else {
                button.setText("");
                button.setEnabled(false);
            }

            if (isSelected) {
                button.setBackground(new Color(153, 153, 255));
            } else {
                button.setBackground(new Color(230, 230, 230));
            }
            button.setForeground(Color.BLACK);
            
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                OrderHistoryTableModel model = (OrderHistoryTableModel) ((JTable) getComponent(0)).getModel();
                OrderVO order = model.getOrderAt(row);

                if (order != null) {
                    int confirm;
                    
                    // 处理不同操作
                    switch (actionType) {
                        case "details":
                            showOrderDetails(order);
                            break;
                            
                        case "track":
                            if (order.getStatus().equals("已发货") || order.getStatus().equals("已完成")) {
                                showShippingTracking(order);
                            }
                            break;
                            
                        case "review":
                            if (order.getStatus().equals("已完成") && !order.isReviewed()) {
                                showReviewDialog(order);
                            }
                            break;
                            
                        case "reorder":
                            if (order.getStatus().equals("已完成") || order.getStatus().equals("待发货")) {
                                confirm = JOptionPane.showConfirmDialog(
                                    panel,
                                    "确定要再次购买此订单内的所有商品吗？",
                                    "再次购买",
                                    JOptionPane.YES_NO_OPTION
                                );
                                
                                if (confirm == JOptionPane.YES_OPTION) {
                                    boolean success = controller.reorderItems(order.getOrderId());
                                    JOptionPane.showMessageDialog(
                                        panel,
                                        success ? "已加入购物车！" : "操作失败，请重试",
                                        success ? "成功" : "错误",
                                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                                    );
                                }
                            }
                            break;
                    }
                }
            }
            clicked = false;
            return "";
        }

        private void showOrderDetails(OrderVO order) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "订单详情", true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(this);
            
            JPanel detailPanel = new JPanel(new BorderLayout());
            detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // 订单信息
            JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            infoPanel.setBorder(BorderFactory.createTitledBorder("订单信息"));
            infoPanel.add(new JLabel("订单ID:"));
            infoPanel.add(new JLabel(String.valueOf(order.getOrderId())));
            infoPanel.add(new JLabel("订单日期:"));
            infoPanel.add(new JLabel(order.getOrderDate()));
            infoPanel.add(new JLabel("订单状态:"));
            infoPanel.add(new JLabel(order.getStatus()));
            infoPanel.add(new JLabel("总金额:"));
            infoPanel.add(new JLabel("¥" + order.getTotalAmount().toString()));
            infoPanel.add(new JLabel("收货地址:"));
            infoPanel.add(new JLabel(order.getShippingAddress()));
            
            // 商品列表
            JTable itemsTable = new JTable();
            JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
            itemsScrollPane.setBorder(BorderFactory.createTitledBorder("商品列表"));
            
            // 布局
            detailPanel.add(infoPanel, BorderLayout.NORTH);
            detailPanel.add(itemsScrollPane, BorderLayout.CENTER);
            
            // 按钮
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = new JButton("关闭");
            closeBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeBtn);
            
            detailPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(detailPanel);
            dialog.setVisible(true);
        }
        
        private void showShippingTracking(OrderVO order) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "物流跟踪", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // 模拟物流跟踪信息
            DefaultListModel<String> listModel = new DefaultListModel<>();
            listModel.addElement("订单已创建");
            listModel.addElement("商家已接单");
            listModel.addElement("商家已发货");
            listModel.addElement("包裹在途中");
            listModel.addElement("包裹已到达目的地");
            
            JList<String> trackingList = new JList<>(listModel);
            JScrollPane trackingScrollPane = new JScrollPane(trackingList);
            trackingScrollPane.setBorder(BorderFactory.createTitledBorder("物流跟踪信息"));
            
            panel.add(trackingScrollPane, BorderLayout.CENTER);
            
            // 按钮
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = new JButton("关闭");
            closeBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeBtn);
            
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.setVisible(true);
        }
        
        private void showReviewDialog(OrderVO order) {
            ProductReviewDialog dialog = new ProductReviewDialog(SwingUtilities.getWindowAncestor(this), 
                "商品评价", true, controller, order);
            dialog.setVisible(true);
            
            // 如果评价成功，更新订单状态
            if (dialog.isSubmitted()) {
                order.setReviewed(true);
                panel.refreshTable();
            }
        }
    }
}