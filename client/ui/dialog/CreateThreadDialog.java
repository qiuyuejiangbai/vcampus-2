package client.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import common.vo.ForumSectionVO;
import common.vo.ThreadVO;

/**
 * 创建新帖子的对话框
 * 模态对话框，包含分类选择、标题输入和内容输入
 */
public class CreateThreadDialog extends JDialog {
    private JComboBox<ForumSectionVO> sectionComboBox;
    private JTextField titleField;
    private JTextArea contentArea;
    private JButton publishButton;
    private JButton cancelButton;
    
    private boolean published = false;
    private ThreadVO createdThread = null;
    
    public CreateThreadDialog(Frame parent, List<ForumSectionVO> sections) {
        super(parent, "发布公告", true); // 模态对话框
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // 设置对话框大小和位置
        setSize(600, 500);
        setLocationRelativeTo(parent);
        
        // 创建主面板
        createMainPanel(sections);
        
        // 设置对话框关闭行为
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void createMainPanel(List<ForumSectionVO> sections) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // 设置对话框背景色为浅绿色
        getContentPane().setBackground(new Color(240, 255, 240));
        
        // 标题
        JLabel titleLabel = new JLabel("发布公告");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 创建内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 第一行：分类选择框和标题输入框
        JPanel firstRowPanel = new JPanel(new BorderLayout(10, 0));
        firstRowPanel.setBackground(Color.WHITE);
        
        // 分类选择框
        sectionComboBox = new JComboBox<>();
        sectionComboBox.setBackground(Color.WHITE);
        sectionComboBox.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        sectionComboBox.setPreferredSize(new Dimension(150, 35));
        
        // 添加分类选项
        if (sections != null && !sections.isEmpty()) {
            for (ForumSectionVO section : sections) {
                sectionComboBox.addItem(section);
            }
        }
        
        // 设置自定义渲染器来显示栏目名称而不是对象信息
        sectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ForumSectionVO) {
                    ForumSectionVO section = (ForumSectionVO) value;
                    setText(section.getName());
                }
                return this;
            }
        });
        
        // 标题输入框
        titleField = new JTextField();
        titleField.setBackground(Color.WHITE);
        titleField.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        titleField.setPreferredSize(new Dimension(300, 35));
        titleField.setText("请输入标题...");
        titleField.setForeground(new Color(150, 150, 150));
        
        // 标题输入框焦点事件
        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (titleField.getText().equals("请输入标题...")) {
                    titleField.setText("");
                    titleField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (titleField.getText().trim().isEmpty()) {
                    titleField.setText("请输入标题...");
                    titleField.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        firstRowPanel.add(sectionComboBox, BorderLayout.WEST);
        firstRowPanel.add(titleField, BorderLayout.CENTER);
        
        // 正文区域
        contentArea = new JTextArea();
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        contentArea.setPreferredSize(new Dimension(0, 200));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setText("请输入正文...");
        contentArea.setForeground(new Color(150, 150, 150));
        contentArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        
        // 正文区域焦点事件
        contentArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (contentArea.getText().equals("请输入正文...")) {
                    contentArea.setText("");
                    contentArea.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (contentArea.getText().trim().isEmpty()) {
                    contentArea.setText("请输入正文...");
                    contentArea.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // 取消按钮
        cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(new Color(100, 100, 100));
        cancelButton.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 发布按钮
        publishButton = new JButton("发布");
        publishButton.setPreferredSize(new Dimension(80, 35));
        publishButton.setBackground(new Color(34, 139, 34)); // 墨绿色
        publishButton.setForeground(Color.WHITE);
        publishButton.setBorder(null);
        publishButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 发布按钮悬停效果
        publishButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                publishButton.setBackground(new Color(25, 120, 25)); // 深一点的绿色
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                publishButton.setBackground(new Color(34, 139, 34)); // 恢复墨绿色
            }
        });
        
        // 按钮事件
        cancelButton.addActionListener(e -> {
            published = false;
            dispose();
        });
        
        publishButton.addActionListener(e -> publishThread());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(publishButton);
        
        // 组装内容面板
        contentPanel.add(firstRowPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(new JScrollPane(contentArea));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(buttonPanel);
        
        // 组装主面板
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void publishThread() {
        // 获取输入内容
        ForumSectionVO selectedSection = (ForumSectionVO) sectionComboBox.getSelectedItem();
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        // 验证输入
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "请选择帖子分类", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (title.isEmpty() || title.equals("请输入标题...")) {
            JOptionPane.showMessageDialog(this, "请输入帖子标题", "输入错误", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }
        
        if (content.isEmpty() || content.equals("请输入正文...")) {
            JOptionPane.showMessageDialog(this, "请输入帖子内容", "输入错误", JOptionPane.WARNING_MESSAGE);
            contentArea.requestFocus();
            return;
        }
        
        // 创建ThreadVO对象
        createdThread = new ThreadVO();
        createdThread.setTitle(title);
        createdThread.setContent(content);
        createdThread.setSectionId(selectedSection.getSectionId());
        createdThread.setSectionName(selectedSection.getName());
        
        // 设置作者ID（这里需要从当前登录用户获取）
        // createdThread.setAuthorId(currentUserId);
        
        published = true;
        dispose();
    }
    
    /**
     * 获取是否已发布
     */
    public boolean isPublished() {
        return published;
    }
    
    /**
     * 获取创建的帖子对象
     */
    public ThreadVO getCreatedThread() {
        return createdThread;
    }
    
    /**
     * 显示对话框并返回创建的帖子对象
     */
    public static ThreadVO showCreateThreadDialog(Frame parent, List<ForumSectionVO> sections) {
        CreateThreadDialog dialog = new CreateThreadDialog(parent, sections);
        dialog.setVisible(true);
        
        if (dialog.isPublished()) {
            return dialog.getCreatedThread();
        }
        
        return null;
    }
}
