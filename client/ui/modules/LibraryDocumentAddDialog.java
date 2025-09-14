package client.ui.modules;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class LibraryDocumentAddDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorsField;
    private JTextField yearField;
    private JComboBox<String> subjectBox;
    private JComboBox<String> categoryBox;
    private JTextField keywordsField;
    private JTextArea absArea;
    private JLabel fileLabel;

    private File selectedFile;
    private LibraryController controller;

    public LibraryDocumentAddDialog(Window owner, LibraryController controller) {
        super(owner, "上传文献", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        setSize(500, 500);
        setLocationRelativeTo(owner);
        initUI();
    }


    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        titleField = new JTextField(20);
        authorsField = new JTextField(20);
        yearField = new JTextField(5);
        subjectBox = new JComboBox<>(new String[]{"计算机", "文学", "管理"});
        categoryBox = new JComboBox<>(new String[]{"期刊", "会议", "教材", "报告"});
        keywordsField = new JTextField(20);
        absArea = new JTextArea(5, 20);

        fileLabel = new JLabel("未选择文件");
        JButton chooseFileBtn = new JButton("选择文件");
        chooseFileBtn.addActionListener(e -> chooseFile());

        panel.add(new JLabel("标题:")); panel.add(titleField);
        panel.add(new JLabel("作者:")); panel.add(authorsField);
        panel.add(new JLabel("年份:")); panel.add(yearField);
        panel.add(new JLabel("学科:")); panel.add(subjectBox);
        panel.add(new JLabel("类别:")); panel.add(categoryBox);
        panel.add(new JLabel("关键词:")); panel.add(keywordsField);
        panel.add(new JLabel("摘要:")); panel.add(new JScrollPane(absArea));
        panel.add(new JLabel("文件:")); panel.add(fileLabel);
        panel.add(new JLabel("")); panel.add(chooseFileBtn);

        JButton uploadBtn = new JButton("上传");
        uploadBtn.addActionListener(e -> doUpload());
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(uploadBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileLabel.setText(selectedFile.getName());
        }
    }

    private void doUpload() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件");
            return;
        }
        try {
            DocumentVO doc = new DocumentVO();
            doc.setTitle(titleField.getText().trim());
            doc.setAuthors(authorsField.getText().trim());
            doc.setYear(Integer.parseInt(yearField.getText().trim()));
            doc.setSubject((String) subjectBox.getSelectedItem());
            doc.setCategory((String) categoryBox.getSelectedItem());
            doc.setKeywords(keywordsField.getText().trim());
            doc.setAbstractTxt(absArea.getText().trim());
            doc.setFileType(getFileExtension(selectedFile));
            doc.setFileSize(selectedFile.length());
            doc.setUploaderId(controller.getCurrentUserId());
            doc.setPublic(true);

            byte[] fileBytes = readFileToBytes(selectedFile);

            boolean ok = controller.uploadDocument(doc, fileBytes);
            if (ok) {
                JOptionPane.showMessageDialog(this, "上传成功");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "上传失败");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "错误: " + ex.getMessage());
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? "" : name.substring(dot + 1);
    }

    private byte[] readFileToBytes(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        int read = fis.read(data);
        fis.close();
        if (read != file.length()) throw new Exception("文件读取不完整");
        return data;
    }
}
