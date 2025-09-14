package client.ui.modules;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import java.awt.*;

public class LibraryDocumentEditDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorsField;
    private JTextField yearField;
    private JComboBox<String> subjectBox;
    private JComboBox<String> categoryBox;
    private JTextField keywordsField;
    private JTextArea absArea;

    private LibraryController controller;
    private DocumentVO document;

    public LibraryDocumentEditDialog(Window owner, LibraryController controller, DocumentVO doc) {
        super(owner, "编辑文献信息", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.document = doc;
        setSize(500, 500);
        setLocationRelativeTo(owner);
        initUI();
    }


    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        titleField = new JTextField(document.getTitle(), 20);
        authorsField = new JTextField(document.getAuthors(), 20);
        yearField = new JTextField(String.valueOf(document.getYear()), 5);

        subjectBox = new JComboBox<>(new String[]{"计算机", "文学", "管理"});
        subjectBox.setSelectedItem(document.getSubject());

        categoryBox = new JComboBox<>(new String[]{"期刊", "会议", "教材", "报告"});
        categoryBox.setSelectedItem(document.getCategory());

        keywordsField = new JTextField(document.getKeywords(), 20);
        absArea = new JTextArea(document.getAbstractTxt(), 5, 20);

        panel.add(new JLabel("标题:")); panel.add(titleField);
        panel.add(new JLabel("作者:")); panel.add(authorsField);
        panel.add(new JLabel("年份:")); panel.add(yearField);
        panel.add(new JLabel("学科:")); panel.add(subjectBox);
        panel.add(new JLabel("类别:")); panel.add(categoryBox);
        panel.add(new JLabel("关键词:")); panel.add(keywordsField);
        panel.add(new JLabel("摘要:")); panel.add(new JScrollPane(absArea));

        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> doSave());
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    private void doSave() {
        try {
            document.setTitle(titleField.getText().trim());
            document.setAuthors(authorsField.getText().trim());
            document.setYear(Integer.parseInt(yearField.getText().trim()));
            document.setSubject((String) subjectBox.getSelectedItem());
            document.setCategory((String) categoryBox.getSelectedItem());
            document.setKeywords(keywordsField.getText().trim());
            document.setAbstractTxt(absArea.getText().trim());

            boolean ok = controller.updateDocument(document);
            if (ok) {
                JOptionPane.showMessageDialog(this, "更新成功");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "更新失败");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "错误: " + ex.getMessage());
        }
    }
}
