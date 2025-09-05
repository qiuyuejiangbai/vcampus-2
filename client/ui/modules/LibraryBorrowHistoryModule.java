package client.ui.modules;

import client.controller.LibraryController;
import common.vo.BorrowRecordVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class LibraryBorrowHistoryModule extends JPanel {
    private JTable tableHistory;
    private BorrowHistoryTableModel tableModel;
    private final int userId;
    private final LibraryController controller;

    public LibraryBorrowHistoryModule(int userId) {
        this.userId = userId;
        this.controller = new LibraryController(userId);

        setLayout(new BorderLayout());

        tableModel = new BorrowHistoryTableModel();
        tableHistory = new JTable(tableModel);

        // 设置按钮渲染器和编辑器
        TableColumn colReturn = tableHistory.getColumnModel().getColumn(6);
        colReturn.setCellRenderer(new ButtonRenderer());
        colReturn.setCellEditor(new ButtonEditor(new JCheckBox(), controller, this, "归还"));

        TableColumn colRenew = tableHistory.getColumnModel().getColumn(7);
        colRenew.setCellRenderer(new ButtonRenderer());
        colRenew.setCellEditor(new ButtonEditor(new JCheckBox(), controller, this, "续借"));

        add(new JScrollPane(tableHistory), BorderLayout.CENTER);

        loadBorrowHistory();
    }

    public JTable getTableHistory() {
        return tableHistory;
    }

    public void loadBorrowHistory() {
        List<BorrowRecordVO> records = controller.getBorrowingsByUser();
        tableModel.setRecords(records);
    }

    public void refreshTable() {
        loadBorrowHistory();
    }

    // ===== 表格模型 =====
    static class BorrowHistoryTableModel extends AbstractTableModel {
        private String[] columnNames = {"记录ID", "书名", "借阅时间", "应还时间", "归还时间", "状态", "归还", "续借"};
        private List<BorrowRecordVO> data;

        public void setRecords(List<BorrowRecordVO> records) {
            this.data = records;
            fireTableDataChanged();
        }

        public BorrowRecordVO getRecordAt(int row) {
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
            BorrowRecordVO record = data.get(row);
            switch (col) {
                case 0: return record.getRecordId();
                case 1: return record.getBookTitle();
                case 2: return record.getBorrowTime();
                case 3: return record.getDueTime();
                case 4: return record.getReturnTime();
                case 5: return record.getStatus();
                case 6: return "归还";
                case 7: return "续借";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6 || col == 7; // 保证按钮始终可以被渲染
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
            BorrowHistoryTableModel model = (BorrowHistoryTableModel) table.getModel();
            BorrowRecordVO record = model.getRecordAt(row);

            String text = (value == null) ? "" : value.toString();
            if (record != null) {
                int status = record.getStatus();
                if ((col == 6 && (status == 2 || status == 4))) {
                    text = "不可归还";
                } else if ((col == 7 && (status == 2 || status == 4))) {
                    text = "不可续借";
                }
            }

            setText(text);
            return this;
        }
    }

    // ===== 按钮编辑器 =====
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String actionType; // "归还" 或 "续借"
        private boolean clicked;
        private int row;
        private LibraryController controller;
        private LibraryBorrowHistoryModule panel;

        public ButtonEditor(JCheckBox checkBox, LibraryController controller, LibraryBorrowHistoryModule panel, String actionType) {
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
            BorrowHistoryTableModel model = (BorrowHistoryTableModel) table.getModel();
            BorrowRecordVO record = model.getRecordAt(row);

            this.row = row;
            clicked = true;

            // 按钮文字 & 可用性
            int status = record.getStatus();
            if (status == 2 || status == 4) {
                if ("归还".equals(actionType)) {
                    button.setText("不可归还");
                } else {
                    button.setText("不可续借");
                }
                button.setEnabled(false);
            } else {
                button.setText(actionType);
                button.setEnabled(true);
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked && button.isEnabled()) {
                BorrowHistoryTableModel model = (BorrowHistoryTableModel) panel.getTableHistory().getModel();
                BorrowRecordVO record = model.getRecordAt(row);
                int borrowId = record.getRecordId();

                if ("续借".equals(actionType)) {
                    if (controller.renewBook(borrowId)) {
                        JOptionPane.showMessageDialog(null, "续借成功");
                        panel.loadBorrowHistory();
                    } else {
                        JOptionPane.showMessageDialog(null, "续借失败");
                    }
                } else if ("归还".equals(actionType)) {
                    if (controller.requestReturn(borrowId)) {
                        JOptionPane.showMessageDialog(null, "归还成功");
                        panel.loadBorrowHistory();
                    } else {
                        JOptionPane.showMessageDialog(null, "归还失败");
                    }
                }
            }
            clicked = false;
            return actionType;
        }
    }
}
