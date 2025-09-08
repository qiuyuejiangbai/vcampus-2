package client.ui.modules;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibraryDocumentManageModule extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private LibraryController controller;

    public LibraryDocumentManageModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initUI();
        refreshTable();
    }

    private void initUI() {
        // 顶部工具栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadBtn = new JButton("上传文献");
        uploadBtn.addActionListener(e -> {
            LibraryDocumentAddDialog dialog = new LibraryDocumentAddDialog(
                    SwingUtilities.getWindowAncestor(this), controller);
            dialog.setVisible(true);
            refreshTable();
        });
        topPanel.add(uploadBtn);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "标题", "作者", "年份", "学科", "类别", "文件类型", "大小(MB)", "操作"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 8; // 只允许操作列
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);

        // 渲染按钮
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<DocumentVO> docs = controller.searchDocuments("", null, null, null, null);
        for (DocumentVO doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getDocId(),
                    doc.getTitle(),
                    doc.getAuthors(),
                    doc.getYear(),
                    doc.getSubject(),
                    doc.getCategory(),
                    doc.getFileType(),
                    String.format("%.2f", doc.getFileSize() / 1024.0 / 1024.0),
                    "编辑/删除"
            });
        }
    }

    // =============== 表格按钮渲染 ===============
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText("编辑/删除");
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("编辑/删除");
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                int docId = (int) tableModel.getValueAt(row, 0);
                DocumentVO doc = controller.getDocumentById(docId);

                String[] options = {"编辑", "删除", "取消"};
                int choice = JOptionPane.showOptionDialog(LibraryDocumentManageModule.this,
                        "请选择操作", "文献管理",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (choice == 0) { // 编辑
                    LibraryDocumentEditDialog dialog = new LibraryDocumentEditDialog(
                            SwingUtilities.getWindowAncestor(LibraryDocumentManageModule.this),
                            controller, doc);
                    dialog.setVisible(true);
                    refreshTable();
                } else if (choice == 1) { // 删除
                    int confirm = JOptionPane.showConfirmDialog(LibraryDocumentManageModule.this,
                            "确认删除文献: " + doc.getTitle() + " ?", "删除确认",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean ok = controller.deleteDocument(docId);
                        if (ok) {
                            JOptionPane.showMessageDialog(LibraryDocumentManageModule.this, "删除成功");
                            refreshTable();
                        } else {
                            JOptionPane.showMessageDialog(LibraryDocumentManageModule.this, "删除失败");
                        }
                    }
                }
            }
            clicked = false;
            return "编辑/删除";
        }
    }
}
