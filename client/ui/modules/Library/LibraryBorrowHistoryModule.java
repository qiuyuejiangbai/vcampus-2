package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.BorrowRecordVO;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class LibraryBorrowHistoryModule extends JPanel {
    private JTable tableHistory;
    private BorrowHistoryTableModel tableModel;
    private final int userId;
    private final LibraryController controller;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton searchButton;
    private JButton clearButton;

    private int hoverRow = -1;

    public LibraryBorrowHistoryModule(int userId) {
        this.userId = userId;
        this.controller = new LibraryController(userId);

        initUI();
        refreshData();
    }

    private JButton createModernButton(String text, Color themeColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverColor : themeColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(90, 30));
        return button;
    }

    private void initUI() {
        setLayout(new BorderLayout());

        Color headerColor = new Color(0, 100, 0);
        Color rowAltColor = new Color(220, 245, 220);
        Color rowHoverColor = new Color(255, 250, 205);

        // ===== 顶部搜索栏（居中对齐） =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        statusFilter = new JComboBox<>(new String[]{"全部", "未归还", "已归还"});
        searchField = new JTextField("请输入书名关键词", 20);
        searchField.setForeground(Color.GRAY);

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("请输入书名关键词")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入书名关键词");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.addActionListener(e -> refreshData());

        searchButton = createModernButton("搜索", new Color(0, 64, 0), new Color(0, 100, 0));
        clearButton = createModernButton("清空筛选", new Color(0, 64, 0), new Color(0, 100, 0));

        searchPanel.add(new JLabel("状态:"));
        searchPanel.add(statusFilter);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        add(searchPanel, BorderLayout.NORTH);

        // ===== 表格 =====
        tableModel = new BorrowHistoryTableModel();
        tableHistory = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row == hoverRow) c.setBackground(rowHoverColor);
                    else c.setBackground(row % 2 == 0 ? rowAltColor : Color.WHITE);
                }
                return c;
            }
        };

        tableHistory.setRowHeight(30);
        tableHistory.setShowGrid(true);
        tableHistory.setGridColor(new Color(180, 180, 180));

        // 鼠标悬停高亮
        tableHistory.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = tableHistory.rowAtPoint(e.getPoint());
                if (row != hoverRow) { hoverRow = row; tableHistory.repaint(); }
            }
        });
        tableHistory.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoverRow = -1; tableHistory.repaint(); }
        });

        // 居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tableHistory.getColumnCount(); i++) {
            tableHistory.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 状态列文字染色
        tableHistory.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        JTableHeader header = tableHistory.getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // 操作列（归还按钮浅绿，续借按钮深绿）
        TableColumn colReturn = tableHistory.getColumnModel().getColumn(6);
        colReturn.setCellRenderer(new ButtonRenderer(new Color(60, 179, 113), new Color(80, 200, 130))); // 浅绿
        colReturn.setCellEditor(new ButtonEditor(new JCheckBox(), controller, this, "归还",
                new Color(60, 179, 113), new Color(80, 200, 130)));

        TableColumn colRenew = tableHistory.getColumnModel().getColumn(7);
        colRenew.setCellRenderer(new ButtonRenderer(new Color(0, 80, 0), new Color(0, 110, 0))); // 深绿
        colRenew.setCellEditor(new ButtonEditor(new JCheckBox(), controller, this, "续借",
                new Color(0, 80, 0), new Color(0, 110, 0)));

        add(new JScrollPane(tableHistory), BorderLayout.CENTER);

        // 事件绑定
        searchButton.addActionListener(e -> refreshData());
        statusFilter.addActionListener(e -> refreshData());
        clearButton.addActionListener(e -> {
            searchField.setText("请输入书名关键词");
            searchField.setForeground(Color.GRAY);
            statusFilter.setSelectedIndex(0);
            refreshData();
        });
    }

    public JTable getTableHistory() { return tableHistory; }

    public void refreshData() {
        String keyword = searchField.getText().trim();
        if (keyword.equals("请输入书名关键词")) keyword = "";
        String statusChoice = (String) statusFilter.getSelectedItem();
        List<BorrowRecordVO> records;

        if (keyword.isEmpty()) records = controller.getBorrowingsByUser();
        else records = controller.searchBorrowHistory(keyword);

        if ("未归还".equals(statusChoice)) {
            records.removeIf(r -> r.getStatus() == 2 || r.getStatus() == 4);
        } else if ("已归还".equals(statusChoice)) {
            records.removeIf(r -> r.getStatus() == 1 || r.getStatus() == 3);
        }

        tableModel.setRecords(records);
    }

    // ===== 表格模型 =====
    static class BorrowHistoryTableModel extends AbstractTableModel {
        private String[] columnNames = {"记录ID", "书名", "借阅时间", "应还时间", "归还时间", "状态", "归还", "续借"};
        private List<BorrowRecordVO> data;
        private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public void setRecords(List<BorrowRecordVO> records) { this.data = records; fireTableDataChanged(); }
        public BorrowRecordVO getRecordAt(int row) { return data == null ? null : data.get(row); }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            BorrowRecordVO r = data.get(row);
            switch (col) {
                case 0: return r.getRecordId();
                case 1: return r.getBookTitle();
                case 2: return r.getBorrowTime() == null ? "" : fmt.format(r.getBorrowTime());
                case 3: return r.getDueTime() == null ? "" : fmt.format(r.getDueTime());
                case 4: return r.getReturnTime() == null ? "" : fmt.format(r.getReturnTime());
                case 5:
                    switch (r.getStatus()) {
                        case 1: return "借出中";
                        case 2: return "已归还";
                        case 3: return "逾期";
                        case 4: return "丢失";
                        default: return "未知";
                    }
                case 6: return (r.getStatus() == 2 || r.getStatus() == 4) ? "不可归还" : "归还";
                case 7: return (r.getStatus() == 2 || r.getStatus() == 4) ? "不可续借" : "续借";
                default: return "";
            }
        }
        @Override public boolean isCellEditable(int row, int col) { return col == 6 || col == 7; }
    }

    // ===== 状态文字染色 =====
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "" : value.toString();
            switch (status) {
                case "借出中": c.setForeground(Color.BLUE); break;
                case "逾期": c.setForeground(Color.RED); break;
                case "已归还": c.setForeground(Color.GRAY); break;
                case "丢失": c.setForeground(new Color(255, 140, 0)); break;
                default: c.setForeground(Color.BLACK);
            }
            setHorizontalAlignment(CENTER);
            return c;
        }
    }

    // ===== 按钮渲染器 =====
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        private final Color themeColor, hoverColor;
        public ButtonRenderer(Color themeColor, Color hoverColor) {
            this.themeColor = themeColor; this.hoverColor = hoverColor;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setOpaque(true);
            setForeground(Color.WHITE);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled() || getText().startsWith("不可")) {
                g2.setColor(new Color(200, 200, 200)); // 灰底
                g2.fillRect(0, 0, getWidth(), getHeight()); // 填满单元格
                g2.setColor(Color.DARK_GRAY);
            } else {
                g2.setColor(getModel().isRollover() ? hoverColor : themeColor);
                g2.fillRect(0, 0, getWidth(), getHeight()); // 填满单元格
                g2.setColor(Color.WHITE);
            }

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // ===== 按钮编辑器 =====
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String actionType;
        private boolean clicked;
        private int row;
        private LibraryController controller;
        private LibraryBorrowHistoryModule panel;

        public ButtonEditor(JCheckBox checkBox, LibraryController controller,
                            LibraryBorrowHistoryModule panel, String actionType,
                            Color themeColor, Color hoverColor) {
            super(checkBox);
            this.controller = controller;
            this.panel = panel;
            this.actionType = actionType;
            button = new ButtonRenderer(themeColor, hoverColor);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            BorrowHistoryTableModel model = (BorrowHistoryTableModel) table.getModel();
            BorrowRecordVO record = model.getRecordAt(row);
            this.row = row;
            clicked = true;

            if (record.getStatus() == 2 || record.getStatus() == 4) {
                button.setText("归还".equals(actionType) ? "不可归还" : "不可续借");
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
                BorrowRecordVO r = model.getRecordAt(row);
                int borrowId = r.getRecordId();

                if ("续借".equals(actionType)) {
                    if (controller.renewBook(borrowId)) {
                        JOptionPane.showMessageDialog(null, "续借成功");
                        panel.refreshData();
                    } else JOptionPane.showMessageDialog(null, "续借失败");
                } else if ("归还".equals(actionType)) {
                    if (controller.requestReturn(borrowId)) {
                        JOptionPane.showMessageDialog(null, "归还成功");
                        panel.refreshData();
                    } else JOptionPane.showMessageDialog(null, "归还失败");
                }
            }
            clicked = false;
            return actionType;
        }
    }
}
