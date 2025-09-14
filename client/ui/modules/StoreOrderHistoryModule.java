package client.ui.modules;

import client.controller.StoreController;
import common.vo.OrderItemVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class StoreOrderHistoryModule extends JPanel {
    private JTable tableHistory;
    private OrderHistoryTableModel tableModel;
    private final StoreController controller;
    private final common.vo.UserVO currentUser;

    public StoreOrderHistoryModule(StoreController controller, common.vo.UserVO currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());

        tableModel = new OrderHistoryTableModel();
        tableHistory = new JTable(tableModel);

        // 设置按钮渲染器和编辑器
        TableColumn colCancel = tableHistory.getColumnModel().getColumn(6);
        colCancel.setCellRenderer(new ButtonRenderer());
        colCancel.setCellEditor(new ButtonEditor(new JCheckBox(), controller, this, "取消订单"));

        add(new JScrollPane(tableHistory), BorderLayout.CENTER);

        loadOrderHistory();
    }

    public JTable getTableHistory() {
        return tableHistory;
    }

    public void loadOrderHistory() {
        List<OrderItemVO> orders = controller.getUserOrderHistory();
        tableModel.setRecords(orders);
    }

    public void refreshTable() {
        loadOrderHistory();
    }

    // ===== 表格模型 =====
    static class OrderHistoryTableModel extends AbstractTableModel {
        private String[] columnNames = {"订单ID", "商品名", "数量", "总价", "下单时间", "状态", "操作"};
        private List<OrderItemVO> data;

        public void setRecords(List<OrderItemVO> records) {
            this.data = records;
            fireTableDataChanged();
        }

        public OrderItemVO getRecordAt(int row) {
            return data == null ? null : data.get(row);
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
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
            OrderItemVO order = data.get(row);
            switch (col) {
                case 0: return order.getItemId();
                case 1: return order.getProductName(); 
                case 3: return order.getUnitPrice();
                case 4: return order.getCreatedTime();
                case 5: return order.getStatus();
                case 6:
                    // 只有未发货状态可取消
                    return "未发货".equals(order.getStatus()) ? "取消订单" : "不可取消";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6;
        }
    }

    // ===== 按钮渲染器 =====
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            OrderHistoryTableModel model = (OrderHistoryTableModel) table.getModel();
            OrderItemVO order = model.getRecordAt(row);

            String text = (value == null) ? "" : value.toString();
            if (order != null) {
                if (col == 6 && !"未发货".equals(order.getStatus())) {
                    text = "不可取消";
                }
            }
            setText(text);
            setEnabled("未发货".equals(order.getStatus()));
            return this;
        }
    }

    // ===== 按钮编辑器 =====
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String actionType; // "取消订单"
        private boolean clicked;
        private int row;
        private StoreController controller;
        private StoreOrderHistoryModule panel;

        public ButtonEditor(JCheckBox checkBox, StoreController controller, StoreOrderHistoryModule panel, String actionType) {
            super(checkBox);
            this.controller = controller;
            this.panel = panel;
            this.actionType = actionType;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            OrderHistoryTableModel model = (OrderHistoryTableModel) table.getModel();
            OrderItemVO order = model.getRecordAt(row);

            this.row = row;
            clicked = true;

            if ("未发货".equals(order.getStatus())) {
                button.setText(actionType);
                button.setEnabled(true);
            } else {
                button.setText("不可取消");
                button.setEnabled(false);
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked && button.isEnabled()) {
                OrderHistoryTableModel model = (OrderHistoryTableModel) panel.getTableHistory().getModel();
                OrderItemVO order = model.getRecordAt(row);
                int orderId = order.getOrderId();

                int confirm = JOptionPane.showConfirmDialog(null, "确定要取消该订单吗？", "取消订单", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.cancelOrder(orderId)) {
                        JOptionPane.showMessageDialog(null, "订单已取消");
                        panel.loadOrderHistory();
                    } else {
                        JOptionPane.showMessageDialog(null, "取消失败");
                    }
                }
            }
            clicked = false;
            return actionType;}}}