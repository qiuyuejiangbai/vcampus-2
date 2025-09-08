package client.ui.modules;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class LibraryDocumentSearchModule extends JPanel {
    private JTextField keywordField;
    private JComboBox<String> subjectBox;
    private JComboBox<String> categoryBox;
    private JTextField startYearField;
    private JTextField endYearField;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    private LibraryController controller;

    public LibraryDocumentSearchModule(LibraryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // 顶部搜索栏
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        keywordField = new JTextField(15);
        searchPanel.add(new JLabel("关键词:"));
        searchPanel.add(keywordField);

        subjectBox = new JComboBox<>(new String[]{"", "计算机", "文学", "管理"});
        searchPanel.add(new JLabel("学科:"));
        searchPanel.add(subjectBox);

        categoryBox = new JComboBox<>(new String[]{"", "期刊", "会议", "教材", "报告"});
        searchPanel.add(new JLabel("类别:"));
        searchPanel.add(categoryBox);

        startYearField = new JTextField(4);
        endYearField = new JTextField(4);
        searchPanel.add(new JLabel("年份:"));
        searchPanel.add(startYearField);
        searchPanel.add(new JLabel("-"));
        searchPanel.add(endYearField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> doSearch());
        searchPanel.add(searchBtn);

        add(searchPanel, BorderLayout.NORTH);

        // 表格
        tableModel = new DefaultTableModel(new Object[]{"ID", "标题", "作者", "年份", "学科", "类别", "操作"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6; // 只允许操作列有按钮
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(30);

        // 渲染按钮
        resultTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        resultTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(resultTable), BorderLayout.CENTER);
    }

    private void doSearch() {
        String keyword = keywordField.getText().trim();
        String subject = (String) subjectBox.getSelectedItem();
        String category = (String) categoryBox.getSelectedItem();

        Integer startYear = null, endYear = null;
        try {
            if (!startYearField.getText().trim().isEmpty()) {
                startYear = Integer.parseInt(startYearField.getText().trim());
            }
            if (!endYearField.getText().trim().isEmpty()) {
                endYear = Integer.parseInt(endYearField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年份必须是数字");
            return;
        }

        List<DocumentVO> docs = controller.searchDocuments(keyword, subject, category, startYear, endYear);
        refreshTable(docs);
    }

    private void refreshTable(List<DocumentVO> docs) {
        tableModel.setRowCount(0);
        for (DocumentVO doc : docs) {
            tableModel.addRow(new Object[]{
                    doc.getDocId(),
                    doc.getTitle(),
                    doc.getAuthors(),
                    doc.getYear(),
                    doc.getSubject(),
                    doc.getCategory(),
                    "下载"
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
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            selectedRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                int docId = (int) tableModel.getValueAt(selectedRow, 0);
                doDownload(docId);
            }
            clicked = false;
            return label;
        }
    }

    // 下载文献
    private void doDownload(int docId) {
        byte[] data = controller.downloadDocument(docId);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "下载失败");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("document_" + docId + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(data);
                JOptionPane.showMessageDialog(this, "下载成功: " + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
            }
        }
    }
}

