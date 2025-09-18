package client.ui.dashboard.components;

import javax.swing.*;
import java.awt.*;
import client.ui.util.FontUtil;
import java.awt.geom.Ellipse2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import client.ui.dialog.AvatarUploadDialog;

/**
 * 圆形头像组件
 * 支持显示图片或默认头像
 */
public class CircularAvatar extends JPanel {
    private Image avatarImage;
    private String defaultText;
    private int size;
    private Color backgroundColor;
    private Color textColor;
    // 新增：可配置边框
    private float borderWidth = 0f; // 默认无边框
    private Color borderColor = new Color(0x2C, 0x4F, 0x3D);
    
    // 头像上传功能
    private boolean clickable = false;
    private AvatarUploadCallback uploadCallback;
    
    public interface AvatarUploadCallback {
        void onAvatarUploaded(String avatarPath);
    }
    
    public CircularAvatar(int size) {
        this.size = size;
        this.backgroundColor = new Color(0xE0, 0xE0, 0xE0); // 改为浅灰色，避免蓝色闪烁
        this.textColor = new Color(0x66, 0x66, 0x66); // 改为深灰色文字
        this.defaultText = "U";
        setPreferredSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);
        
        // 添加鼠标监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickable && uploadCallback != null) {
                    openAvatarUploadDialog();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (clickable) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (clickable) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }
    
    public void setAvatarImage(Image image) {
        this.avatarImage = image;
        repaint();
    }
    
    public void setDefaultText(String text) {
        this.defaultText = text;
        repaint();
    }
    
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    public void setTextColor(Color color) {
        this.textColor = color;
        repaint();
    }
    
    public void setBorderWidth(float w) {
        this.borderWidth = Math.max(0f, w);
        repaint();
    }
    
    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }
    
    /**
     * 设置是否可点击上传
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    
    /**
     * 设置上传回调
     */
    public void setUploadCallback(AvatarUploadCallback callback) {
        this.uploadCallback = callback;
    }
    
    /**
     * 打开头像上传对话框
     */
    private void openAvatarUploadDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AvatarUploadDialog dialog = new AvatarUploadDialog(parentFrame, new AvatarUploadDialog.UploadCallback() {
            @Override
            public void onUploadSuccess(String avatarPath) {
                if (uploadCallback != null) {
                    uploadCallback.onAvatarUploaded(avatarPath);
                }
            }
            
            @Override
            public void onUploadFailure(String errorMessage) {
                JOptionPane.showMessageDialog(CircularAvatar.this, 
                    "头像上传失败: " + errorMessage, 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        // 启用所有抗锯齿和高质量渲染设置
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        
        if (avatarImage != null) {
            // 绘制圆形图片
            drawCircularImage(g2d, avatarImage, x, y, size);
        } else {
            // 绘制默认头像
            drawDefaultAvatar(g2d, x, y, size);
        }
        
        g2d.dispose();
    }
    
    private void drawCircularImage(Graphics2D g2d, Image image, int x, int y, int size) {
        float w = borderWidth;
        float inset = w / 2f;
        // 使用浮点坐标以获得更平滑的边缘
        Ellipse2D.Float circle = new Ellipse2D.Float(x + inset, y + inset, size - w, size - w);
        
        Shape oldClip = g2d.getClip();
        g2d.setClip(circle);
        
        // 使用高质量图像渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(image,
                Math.round(x + inset), Math.round(y + inset),
                Math.round(size - w), Math.round(size - w),
                this);
        
        g2d.setClip(oldClip);
        
        if (borderColor != null && w > 0f) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(circle);
        }
    }
    
    private void drawDefaultAvatar(Graphics2D g2d, int x, int y, int size) {
        float w = borderWidth;
        float inset = w / 2f;
        // 使用浮点坐标以获得更平滑的边缘
        Ellipse2D.Float circle = new Ellipse2D.Float(x + inset, y + inset, size - w, size - w);
        g2d.setColor(backgroundColor);
        g2d.fill(circle);
        
        g2d.setColor(textColor);
        Font baseFont = FontUtil.getAppropriateFont(defaultText, Font.BOLD, Math.max(12f, size * 0.44f));
        g2d.setFont(baseFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = Math.round(x + inset + (size - w - fm.stringWidth(defaultText)) / 2f);
        int textY = Math.round(y + inset + (size - w - fm.getHeight()) / 2f + fm.getAscent());
        g2d.drawString(defaultText, textX, textY);
        
        if (borderColor != null && w > 0f) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(circle);
        }
    }
}
