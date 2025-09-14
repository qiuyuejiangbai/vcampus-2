package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

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

    private Color themeColor = new Color(0, 64, 0); // 深绿色
    private Color hoverColor = new Color(0, 100, 0); // 浅一点的绿色

    public LibraryDocumentAddDialog(Window owner, LibraryController controller) {
        super(owner, "上传文献", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        // 增大对话框尺寸，减少滚动条出现
        setSize(600, 700); // 调整尺寸
        setLocationRelativeTo(owner);
        initUI();
    }

    // 共享的创建现代风格按钮的方法
    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color backgroundColor = getModel().isRollover() ? hoverColor : themeColor;
                g2.setColor(backgroundColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE); // 确保文字是白色
                g2.setFont(getFont().deriveFont(Font.BOLD)); // 加粗字体
                g2.drawString(getText(), textX, textY);
                g2.dispose();
                // 移除 super.paintComponent(g); 以避免默认绘制覆盖
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        return button;
    }

    private void initUI() {
        // 使用GridBagLayout以更灵活地控制组件大小和位置，减少滚动
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 增加边距
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // 增加组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充

        int row = 0;

        // 标题
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("标题:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; titleField = new JTextField(30); panel.add(titleField, gbc);
        row++;

        // 作者
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; authorsField = new JTextField(30); panel.add(authorsField, gbc);
        row++;

        // 年份
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("年份:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; yearField = new JTextField(10); panel.add(yearField, gbc);
        row++;

        // 学科
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("学科:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; subjectBox = new JComboBox<>(new String[]{"计算机", "文学", "管理", "经济", "历史", "艺术", "医学", "物理", "化学", "生物", "地理", "哲学", "法学", "教育学"});
        panel.add(subjectBox, gbc);
        row++;

        // 类别
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("类别:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; categoryBox = new JComboBox<>(new String[]{"期刊", "会议", "教材", "报告", "学位论文", "专利", "标准"});
        panel.add(categoryBox, gbc);
        row++;

        // 关键词
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("关键词:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; keywordsField = new JTextField(30); panel.add(keywordsField, gbc);
        row++;

        // 摘要
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHEAST; panel.add(new JLabel("摘要:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weighty = 1.0;
        absArea = new JTextArea(8, 30); // 增大初始行数
        absArea.setLineWrap(true);
        absArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(absArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, gbc);
        row++;

        // 文件选择
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; gbc.weighty = 0; panel.add(new JLabel("文件:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; fileLabel = new JLabel("未选择文件"); panel.add(fileLabel, gbc);
        row++;

        gbc.gridx = 1; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST; // 调整按钮位置
        JButton chooseFileBtn = createModernButton("选择文件");
        chooseFileBtn.addActionListener(e -> chooseFile());
        panel.add(chooseFileBtn, gbc);
        row++;


        JButton uploadBtn = createModernButton("上传");
        uploadBtn.addActionListener(e -> doUpload());
        JButton cancelBtn = createModernButton("取消");
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnPanel.add(uploadBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
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
            JOptionPane.showMessageDialog(this, "请先选择文件", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            DocumentVO doc = new DocumentVO();
            doc.setTitle(titleField.getText().trim());
            doc.setAuthors(authorsField.getText().trim());
            doc.setYear(Integer.parseInt(yearField.getText().trim()));
            doc.setSubject((String) Objects.requireNonNull(subjectBox.getSelectedItem()));
            doc.setCategory((String) Objects.requireNonNull(categoryBox.getSelectedItem()));
            doc.setKeywords(keywordsField.getText().trim());
            doc.setAbstractTxt(absArea.getText().trim());
            doc.setFileType(getFileExtension(selectedFile));
            doc.setFileSize(selectedFile.length());
            doc.setUploaderId(controller.getCurrentUserId());
            doc.setPublic(true);

            byte[] fileBytes = readFileToBytes(selectedFile);
            boolean ok = controller.uploadDocument(doc, fileBytes);
            if (ok) {
                JOptionPane.showMessageDialog(this, "上传成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "上传失败", "失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "年份必须是有效的数字。", "输入错误", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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