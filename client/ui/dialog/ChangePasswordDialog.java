package client.ui.dialog;

import client.net.ServerConnection;
import client.ui.util.FontUtil;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 修改密码对话框
 */
public class ChangePasswordDialog extends JDialog {
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private final ServerConnection connection;
    private final Integer userId;
    private final Frame parentFrame;
    
    public ChangePasswordDialog(Frame parent, ServerConnection connection, Integer userId) {
        super(parent, "修改密码", true);
        this.connection = connection;
        this.userId = userId;
        this.parentFrame = parent;
        
        initComponents();
        layoutComponents();
        setupEventHandlers();
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        // 创建组件
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        confirmButton = new JButton("确认修改");
        cancelButton = new JButton("取消");
        statusLabel = new JLabel(" ");
        
        // 设置字体
        Font font = FontUtil.getSourceHanSansFont(Font.PLAIN, 12f);
        currentPasswordField.setFont(font);
        newPasswordField.setFont(font);
        confirmPasswordField.setFont(font);
        confirmButton.setFont(font);
        cancelButton.setFont(font);
        statusLabel.setFont(font);
        
        // 设置状态标签颜色
        statusLabel.setForeground(Color.RED);
        
        // 设置按钮样式
        confirmButton.setBackground(new Color(0x2E, 0x7D, 0x32));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        
        cancelButton.setBackground(new Color(0x75, 0x75, 0x75));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 当前密码
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("当前密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(currentPasswordField, gbc);
        
        // 新密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(newPasswordField, gbc);
        
        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("确认新密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(confirmPasswordField, gbc);
        
        // 状态标签
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(statusLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // 确认按钮事件
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        
        // 取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 回车键事件
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    changePassword();
                }
            }
        };
        
        currentPasswordField.addKeyListener(enterKeyAdapter);
        newPasswordField.addKeyListener(enterKeyAdapter);
        confirmPasswordField.addKeyListener(enterKeyAdapter);
        
        // ESC键关闭对话框
        KeyAdapter escKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        };
        
        currentPasswordField.addKeyListener(escKeyAdapter);
        newPasswordField.addKeyListener(escKeyAdapter);
        confirmPasswordField.addKeyListener(escKeyAdapter);
    }
    
    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // 验证输入
        if (currentPassword.trim().isEmpty()) {
            showStatus("请输入当前密码", true);
            return;
        }
        
        if (newPassword.trim().isEmpty()) {
            showStatus("请输入新密码", true);
            return;
        }
        
        if (confirmPassword.trim().isEmpty()) {
            showStatus("请确认新密码", true);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showStatus("两次输入的新密码不一致", true);
            return;
        }
        
        if (newPassword.length() < 6) {
            showStatus("新密码长度至少6位", true);
            return;
        }
        
        // 禁用按钮防止重复提交
        confirmButton.setEnabled(false);
        cancelButton.setEnabled(false);
        showStatus("正在修改密码...", false);
        
        // 发送修改密码请求
        try {
            Message request = new Message();
            request.setType(MessageType.CHANGE_PASSWORD_REQUEST);
            request.setData(new Object[]{userId, currentPassword, newPassword});
            
            // 设置消息监听器
            connection.setMessageListener(MessageType.CHANGE_PASSWORD_SUCCESS, response -> {
                SwingUtilities.invokeLater(() -> {
                    confirmButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    showStatus("密码修改成功！正在登出...", false);
                    statusLabel.setForeground(new Color(0x2E, 0x7D, 0x32));
                    
                    // 延迟关闭对话框并登出
                    Timer timer = new Timer(2000, e -> {
                        connection.removeMessageListener(MessageType.CHANGE_PASSWORD_SUCCESS);
                        connection.removeMessageListener(MessageType.CHANGE_PASSWORD_FAILURE);
                        dispose();
                        
                        // 执行登出操作
                        performLogout();
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            });
            
            connection.setMessageListener(MessageType.CHANGE_PASSWORD_FAILURE, response -> {
                SwingUtilities.invokeLater(() -> {
                    confirmButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    String errorMsg = "密码修改失败";
                    if (response.getData() != null && response.getData() instanceof String) {
                        errorMsg = (String) response.getData();
                    }
                    showStatus(errorMsg, true);
                    connection.removeMessageListener(MessageType.CHANGE_PASSWORD_SUCCESS);
                    connection.removeMessageListener(MessageType.CHANGE_PASSWORD_FAILURE);
                });
            });
            
            connection.sendMessage(request);
            
        } catch (Exception e) {
            confirmButton.setEnabled(true);
            cancelButton.setEnabled(true);
            showStatus("发送请求失败，请重试", true);
        }
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        if (isError) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(new Color(0x2E, 0x7D, 0x32));
        }
    }
    
    /**
     * 执行登出操作
     */
    private void performLogout() {
        try {
            // 发送登出请求
            Message logoutRequest = new Message();
            logoutRequest.setType(MessageType.LOGOUT_REQUEST);
            connection.sendMessage(logoutRequest);
            
            // 关闭当前父窗口（仪表板）
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            
            // 创建新的登录界面并显示密码修改提示
            SwingUtilities.invokeLater(() -> {
                client.ui.LoginFrame loginFrame = new client.ui.LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.showPasswordChangeNotification();
            });
            
        } catch (Exception e) {
            System.err.println("登出操作失败: " + e.getMessage());
            e.printStackTrace();
            
            // 即使登出失败，也要关闭当前窗口并显示登录界面
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            
            SwingUtilities.invokeLater(() -> {
                client.ui.LoginFrame loginFrame = new client.ui.LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.showPasswordChangeNotification();
            });
        }
    }
}
