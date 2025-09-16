package client.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import client.ui.util.FontUtil;
import client.controller.UserController;

/**
 * 头像上传对话框
 * 提供图片选择、预览和上传功能
 */
public class AvatarUploadDialog extends JDialog {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 500;
    
    // UI组件
    private JLabel previewLabel;
    private JButton selectButton;
    private JButton uploadButton;
    private JButton cancelButton;
    private JLabel fileNameLabel;
    private JLabel fileSizeLabel;
    private JLabel statusLabel;
    
    // 数据
    private File selectedFile;
    private BufferedImage previewImage;
    private byte[] fileData;
    
    // 回调接口
    public interface UploadCallback {
        void onUploadSuccess(String avatarPath);
        void onUploadFailure(String errorMessage);
    }
    
    private UploadCallback callback;
    
    // 颜色常量
    private static final Color PRIMARY_COLOR = new Color(55, 161, 101);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color ERROR_RED = new Color(220, 38, 38);
    private static final Color WARNING_ORANGE = new Color(245, 158, 11);
    private static final Color GRAY_TEXT = new Color(107, 114, 128);
    private static final Color DARK_TEXT = new Color(17, 24, 39);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public AvatarUploadDialog(Frame parent, UploadCallback callback) {
        super(parent, "上传头像", true);
        this.callback = callback;
        
        initComponents();
        setupLayout();
        setupEventListeners();
        
        // 设置对话框属性
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        // 预览区域
        previewLabel = new JLabel("点击选择图片", SwingConstants.CENTER);
        previewLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 14));
        previewLabel.setForeground(GRAY_TEXT);
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        previewLabel.setPreferredSize(new Dimension(200, 200));
        previewLabel.setMinimumSize(new Dimension(200, 200));
        previewLabel.setMaximumSize(new Dimension(200, 200));
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 文件信息标签
        fileNameLabel = new JLabel("未选择文件", SwingConstants.CENTER);
        fileNameLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 12));
        fileNameLabel.setForeground(DARK_TEXT);
        
        fileSizeLabel = new JLabel("", SwingConstants.CENTER);
        fileSizeLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 11));
        fileSizeLabel.setForeground(GRAY_TEXT);
        
        // 状态标签
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(FontUtil.getSourceHanSansFont(Font.PLAIN, 12));
        statusLabel.setVisible(false);
        
        // 按钮
        selectButton = createStyledButton("选择图片", PRIMARY_COLOR);
        uploadButton = createStyledButton("上传头像", SUCCESS_GREEN);
        uploadButton.setEnabled(false);
        cancelButton = createStyledButton("取消", GRAY_TEXT);
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                ButtonModel model = getModel();
                
                // 创建渐变背景
                Color startColor = model.isPressed() ? bgColor.darker().darker() : 
                                 model.isRollover() ? bgColor.darker() : bgColor;
                Color endColor = model.isPressed() ? bgColor.darker() :
                               model.isRollover() ? bgColor.brighter() : bgColor.brighter();
                
                GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 8, 8);
                
                // 添加高光效果
                if (!model.isPressed()) {
                    g2d.setColor(new Color(255, 255, 255, model.isRollover() ? 60 : 40));
                    g2d.fillRoundRect(2, 2, width - 4, height / 2, 6, 6);
                }
                
                // 绘制文本
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (width - fm.stringWidth(getText())) / 2;
                int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
            }
        };
        
        button.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(100, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // 标题
        JLabel titleLabel = new JLabel("上传头像", SwingConstants.CENTER);
        titleLabel.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 18));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 预览区域容器
        JPanel previewContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewContainer.setOpaque(false);
        previewContainer.add(previewLabel);
        
        // 文件信息容器
        JPanel fileInfoContainer = new JPanel();
        fileInfoContainer.setLayout(new BoxLayout(fileInfoContainer, BoxLayout.Y_AXIS));
        fileInfoContainer.setOpaque(false);
        fileInfoContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileInfoContainer.add(fileNameLabel);
        fileInfoContainer.add(Box.createVerticalStrut(4));
        fileInfoContainer.add(fileSizeLabel);
        
        // 按钮容器
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.add(selectButton);
        buttonContainer.add(uploadButton);
        buttonContainer.add(cancelButton);
        
        // 状态容器
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusContainer.setOpaque(false);
        statusContainer.add(statusLabel);
        
        // 添加到主面板
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(previewContainer);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(fileInfoContainer);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonContainer);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(statusContainer);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 选择图片按钮
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectImageFile();
            }
        });
        
        // 预览区域点击
        previewLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectImageFile();
            }
        });
        
        // 上传按钮
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadAvatar();
            }
        });
        
        // 取消按钮
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * 选择图片文件
     */
    private void selectImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择头像图片");
        
        // 设置文件过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "图片文件 (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
            "jpg", "jpeg", "png", "gif", "bmp"
        );
        fileChooser.setFileFilter(filter);
        
        // 设置默认目录
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            loadImageFile();
        }
    }
    
    /**
     * 加载图片文件
     */
    private void loadImageFile() {
        if (selectedFile == null) {
            return;
        }
        
        try {
            // 检查文件大小（2MB限制）
            long fileSize = selectedFile.length();
            if (fileSize > 2 * 1024 * 1024) {
                showStatus("文件大小不能超过2MB", ERROR_RED);
                return;
            }
            
            // 读取图片
            previewImage = ImageIO.read(selectedFile);
            if (previewImage == null) {
                showStatus("无法读取图片文件", ERROR_RED);
                return;
            }
            
            // 读取文件数据
            fileData = java.nio.file.Files.readAllBytes(selectedFile.toPath());
            
            // 更新预览
            updatePreview();
            
            // 更新文件信息
            fileNameLabel.setText(selectedFile.getName());
            fileSizeLabel.setText(formatFileSize(fileSize));
            
            // 启用上传按钮
            uploadButton.setEnabled(true);
            
            // 清除状态
            hideStatus();
            
        } catch (IOException e) {
            System.err.println("读取图片文件失败: " + e.getMessage());
            showStatus("读取图片文件失败", ERROR_RED);
        }
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        if (previewImage == null) {
            return;
        }
        
        // 计算缩放比例，保持宽高比
        int maxSize = 180;
        int width = previewImage.getWidth();
        int height = previewImage.getHeight();
        
        int newWidth, newHeight;
        if (width > height) {
            newWidth = maxSize;
            newHeight = (int) ((double) height * maxSize / width);
        } else {
            newHeight = maxSize;
            newWidth = (int) ((double) width * maxSize / height);
        }
        
        // 创建缩放后的图片
        Image scaledImage = previewImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImage);
        
        previewLabel.setIcon(icon);
        previewLabel.setText("");
    }
    
    /**
     * 上传头像
     */
    private void uploadAvatar() {
        if (fileData == null || selectedFile == null) {
            showStatus("请先选择图片", ERROR_RED);
            return;
        }
        
        // 禁用上传按钮，显示上传状态
        uploadButton.setEnabled(false);
        uploadButton.setText("上传中...");
        showStatus("正在上传头像...", WARNING_ORANGE);
        
        // 使用UserController进行真实的网络上传
        UserController userController = new UserController();
        userController.uploadAvatar(fileData, selectedFile.getName(), new UserController.AvatarUploadCallback() {
            @Override
            public void onSuccess(String avatarPath) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("头像上传成功", SUCCESS_GREEN);
                    uploadButton.setText("上传头像");
                    uploadButton.setEnabled(false);
                    
                    // 延迟关闭对话框
                    Timer timer = new Timer(1500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                            if (callback != null) {
                                callback.onUploadSuccess(avatarPath);
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("上传失败: " + errorMessage, ERROR_RED);
                    uploadButton.setText("上传头像");
                    uploadButton.setEnabled(true);
                });
            }
        });
    }
    
    /**
     * 显示状态信息
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        statusLabel.setVisible(true);
    }
    
    /**
     * 隐藏状态信息
     */
    private void hideStatus() {
        statusLabel.setVisible(false);
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取文件数据（供外部调用）
     */
    public byte[] getFileData() {
        return fileData;
    }
    
    /**
     * 获取文件名（供外部调用）
     */
    public String getFileName() {
        return selectedFile != null ? selectedFile.getName() : null;
    }
}
