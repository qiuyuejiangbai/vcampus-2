package client.ui.modules;

import client.controller.StoreController;
import common.vo.order.OrderVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class StoreOrderHistoryModule extends JPanel {
    private JTable tableOrders;
    private OrderTableModel tableModel;
    private final UserVO currentUser;
    private final StoreController controller;
    private JLabel statusLabel;

    public StoreOrderHistoryModule(UserVO user, StoreController controller) {
        this.currentUser = user;
        this.controller = controller;
        initUI();
        loadOrders();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 创建表格模型
        tableModel = new OrderTableModel();
        tableOrders = new JTable(tableModel);

        // 设置表格列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // 订单ID
        table.getColumnModel().getColumn(1).setPreferredWidth(180);  // 下单时间
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // 状态
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // 金额
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // 操作按钮列

        // 设置表格样式
        tableOrders.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableOrders.setRowHeight(35);
        tableOrders.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 状态标签
        statusLabel = new JLabel("订单总数: 0");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        // 添加组件
        add(statusLabel, BorderLayout.NORTH);
        add(new JScrollPane(tableOrders), BorderLayout.CENTER);
    }

    private void loadOrders() {
        List<OrderVO> orders = controller.getOrderHistory(currentUser.getUserId());
        tableModel.setOrders(orders);
        statusLabel.setText("订单总数: " + orders.size());
    }

    private static class OrderTableModel extends AbstractTableModel {
        private static final String[] columnNames = {
            "订单ID", "下单时间", "订单状态", "订单金额", "操作"
        };
        
        private List<OrderVO> orders;
        private final String[] actions = {
            "查看详情", "取消订单", "确认收货"
        };

        public void setOrders(List<OrderVO> orders) {
            this.orders = orders;
            fireTableDataChanged();
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
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (orders == null || row >= orders.size()) return null;
            
            OrderVO order = orders.get(row);
            switch (column) {
                case 0: return order.getOrderId();
                case 1: return order.getOrderTime().toString();
                case 2: return getOrderStatusText(order.getStatus());
                case 3: return String.format("¥%.2f", order.getTotalAmount());
                case 4: return getAvailableActions(order.getStatus());
                default: return null;
            }
        }

        private String getOrderStatusText(int status) {
            switch (status) {
                case 0: return "待支付";
                case 1: return "已支付";
                case 2: return "已发货";
                case 3: return "已完成";
                case 4: return "已取消";
                case 5: return "退货中";
                case 6: return "已退货";
                default: return "未知";
            }
        }

        private String getAvailableActions(int status) {
            switch (status) {
                case 0: return actions[0] + "," + actions[1];
                case 1: return actions[0] + "," + actions[1];
                case 2: return actions[0] + "," + actions[2];
                case 3: return actions[0];
                case 4: return actions[0];
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }
    }

    public void refreshOrders() {
        loadOrders();
    }
}