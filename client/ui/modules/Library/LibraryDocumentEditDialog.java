package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.DocumentVO;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

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

    // 定义主题颜色和悬停颜色
    private Color themeColor = new Color(0, 64, 0); // 深绿色
    private Color hoverColor = new Color(0, 100, 0); // 浅一点的绿色

    public LibraryDocumentEditDialog(Window owner, LibraryController controller, DocumentVO doc) {
        super(owner, "编辑文献信息", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.document = doc;
        // 增大对话框尺寸，减少滚动条出现
        setSize(600, 650); // 调整尺寸
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
                // 绘制圆角矩形背景
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                // 绘制文本
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE); // 确保文字是白色
                g2.setFont(getFont().deriveFont(Font.BOLD)); // 加粗字体，更突出
                g2.drawString(getText(), textX, textY);
                g2.dispose();
                // 移除 super.paintComponent(g); 以避免默认绘制覆盖
            }
        };
        button.setContentAreaFilled(false); // 不绘制默认内容区域
        button.setBorderPainted(false); // 不绘制边框
        button.setFocusPainted(false); // 不绘制焦点边框
        button.setPreferredSize(new Dimension(100, 35)); // 稍微增大按钮尺寸
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
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; titleField = new JTextField(document.getTitle(), 30); panel.add(titleField, gbc);
        row++;

        // 作者
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; authorsField = new JTextField(document.getAuthors(), 30); panel.add(authorsField, gbc);
        row++;

        // 年份
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("年份:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; yearField = new JTextField(String.valueOf(document.getYear()), 10); panel.add(yearField, gbc);
        row++;

        // 学科
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("学科:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; subjectBox = new JComboBox<>(new String[]{"计算机", "文学", "管理", "经济", "历史", "艺术", "医学", "物理", "化学", "生物", "地理", "哲学", "法学", "教育学"});
        subjectBox.setSelectedItem(document.getSubject());
        panel.add(subjectBox, gbc);
        row++;

        // 类别
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("类别:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; categoryBox = new JComboBox<>(new String[]{"期刊", "会议", "教材", "报告", "学位论文", "专利", "标准"});
        categoryBox.setSelectedItem(document.getCategory());
        panel.add(categoryBox, gbc);
        row++;

        // 关键词
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("关键词:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; keywordsField = new JTextField(document.getKeywords(), 30); panel.add(keywordsField, gbc);
        row++;

        // 摘要
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHEAST; panel.add(new JLabel("摘要:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weighty = 1.0; // 占据更多垂直空间
        absArea = new JTextArea(document.getAbstractTxt(), 8, 30); // 增大初始行数
        absArea.setLineWrap(true);
        absArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(absArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 按需显示滚动条
        panel.add(scrollPane, gbc);
        row++;


        JButton saveBtn = createModernButton("保存");
        saveBtn.addActionListener(e -> doSave());
        JButton cancelBtn = createModernButton("取消");
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 增加按钮面板边距
        btnPanel.add(saveBtn);
        btnPanel.add(Box.createHorizontalStrut(20)); // 增加按钮间距
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER); // 直接添加panel，而不是JScrollPane(panel)
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    private void doSave() {
        try {
            document.setTitle(titleField.getText().trim());
            document.setAuthors(authorsField.getText().trim());
            document.setYear(Integer.parseInt(yearField.getText().trim()));
            document.setSubject((String) Objects.requireNonNull(subjectBox.getSelectedItem()));
            document.setCategory((String) Objects.requireNonNull(categoryBox.getSelectedItem()));
            document.setKeywords(keywordsField.getText().trim());
            document.setAbstractTxt(absArea.getText().trim());

            boolean ok = controller.updateDocument(document);
            if (ok) {
                JOptionPane.showMessageDialog(this, "更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "更新失败", "失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "年份必须是有效的数字。", "输入错误", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}