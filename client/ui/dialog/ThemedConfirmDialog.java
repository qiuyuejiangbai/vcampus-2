package client.ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 主题化的确认对话框，符合软件墨绿色主题
 */
public class ThemedConfirmDialog extends JDialog {
    private int result = JOptionPane.CANCEL_OPTION;
    private JButton yesButton;
    private JButton noButton;
    
    // 主题色配置
    private static final Color THEME_COLOR = new Color(0x2C, 0x4F, 0x3D); // 墨绿色
    private static final Color HOVER_COLOR = new Color(0x35, 0x5D, 0x49); // 悬停色
    private static final Color ACTIVE_COLOR = new Color(0x21, 0x40, 0x33); // 激活色
    private static final Color BACKGROUND_COLOR = new Color(0xF6, 0xF7, 0xF8); // 背景色
    private static final Color TEXT_COLOR = new Color(0x1B, 0x1F, 0x24); // 文字色
    
    public ThemedConfirmDialog(Frame parent, String title, String message) {
        super(parent, title, true);
        initComponents(message);
        setupDialog();
    }
    
    private void initComponents(String message) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 消息标签
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Source Han Sans SC", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        // 创建主题化按钮
        yesButton = createThemedButton("确定", THEME_COLOR);
        noButton = createThemedButton("取消", new Color(0x6B, 0x72, 0x80)); // 灰色
        
        yesButton.addActionListener(e -> {
            result = JOptionPane.YES_OPTION;
            dispose();
        });
        
        noButton.addActionListener(e -> {
            result = JOptionPane.NO_OPTION;
            dispose();
        });
        
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JButton createThemedButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Source Han Sans SC", Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (bgColor.equals(THEME_COLOR)) {
                    button.setBackground(HOVER_COLOR);
                } else {
                    button.setBackground(new Color(0x9C, 0xA3, 0xAF));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (bgColor.equals(THEME_COLOR)) {
                    button.setBackground(ACTIVE_COLOR);
                } else {
                    button.setBackground(new Color(0x6B, 0x72, 0x80));
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (bgColor.equals(THEME_COLOR)) {
                    button.setBackground(HOVER_COLOR);
                } else {
                    button.setBackground(new Color(0x9C, 0xA3, 0xAF));
                }
            }
        });
        
        return button;
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
        
        // 设置默认按钮
        getRootPane().setDefaultButton(yesButton);
        
        // 添加键盘快捷键支持
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.NO_OPTION;
                dispose();
            }
        });
    }
    
    public int showDialog() {
        setVisible(true);
        return result;
    }
    
    /**
     * 静态方法，方便调用
     */
    public static int showConfirmDialog(Component parent, String title, String message) {
        Frame frame = null;
        if (parent instanceof Frame) {
            frame = (Frame) parent;
        } else if (parent != null) {
            frame = (Frame) SwingUtilities.getWindowAncestor(parent);
        }
        
        ThemedConfirmDialog dialog = new ThemedConfirmDialog(frame, title, message);
        return dialog.showDialog();
    }
}
