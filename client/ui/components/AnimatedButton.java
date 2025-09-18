package client.ui.components;

import client.ui.util.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 带动画效果的按钮组件
 */
public class AnimatedButton extends JButton {
    
    // 动画状态
    private boolean isHovered = false;
    private boolean isPressed = false;
    private float hoverProgress = 0.0f;
    private float pressProgress = 0.0f;
    
    // 动画定时器
    private Timer hoverTimer;
    private Timer pressTimer;
    
    // 样式配置
    private Color normalColor = new Color(55, 161, 101);
    private Color hoverColor = new Color(46, 139, 87);
    private Color pressedColor = new Color(37, 118, 73);
    private Color textColor = Color.WHITE;
    private int cornerRadius = 14;
    private int shadowOffset = 4;
    
    public AnimatedButton(String text) {
        super(text);
        initButton();
    }
    
    public AnimatedButton(String text, Icon icon) {
        super(text, icon);
        initButton();
    }
    
    private void initButton() {
        // 基本设置
        setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 16));
        setForeground(textColor);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 初始化定时器
        initTimers();
        
        // 添加鼠标监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                startHoverAnimation(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                startHoverAnimation(false);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                startPressAnimation(true);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                startPressAnimation(false);
            }
        });
    }
    
    private void initTimers() {
        // 悬停动画定时器
        hoverTimer = new Timer(16, e -> {
            float target = isHovered ? 1.0f : 0.0f;
            float diff = target - hoverProgress;
            
            if (Math.abs(diff) < 0.01f) {
                hoverProgress = target;
                ((Timer) e.getSource()).stop();
            } else {
                hoverProgress += diff * 0.2f; // 平滑过渡
            }
            
            repaint();
        });
        
        // 按下动画定时器
        pressTimer = new Timer(16, e -> {
            float target = isPressed ? 1.0f : 0.0f;
            float diff = target - pressProgress;
            
            if (Math.abs(diff) < 0.01f) {
                pressProgress = target;
                ((Timer) e.getSource()).stop();
            } else {
                pressProgress += diff * 0.3f; // 更快的过渡
            }
            
            repaint();
        });
    }
    
    private void startHoverAnimation(boolean hover) {
        if (!hoverTimer.isRunning()) {
            hoverTimer.start();
        }
    }
    
    private void startPressAnimation(boolean press) {
        if (!pressTimer.isRunning()) {
            pressTimer.start();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // 计算当前颜色
        Color currentColor = calculateCurrentColor();
        
        // 绘制阴影（悬停时）
        if (hoverProgress > 0) {
            drawShadow(g2d, width, height);
        }
        
        // 绘制按钮背景
        drawButtonBackground(g2d, width, height, currentColor);
        
        // 绘制按钮内容
        drawButtonContent(g2d, width, height);
        
        g2d.dispose();
    }
    
    private Color calculateCurrentColor() {
        Color baseColor = normalColor;
        
        // 悬停效果
        if (hoverProgress > 0) {
            baseColor = interpolateColor(normalColor, hoverColor, hoverProgress);
        }
        
        // 按下效果
        if (pressProgress > 0) {
            baseColor = interpolateColor(baseColor, pressedColor, pressProgress);
        }
        
        return baseColor;
    }
    
    private Color interpolateColor(Color start, Color end, float progress) {
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    private void drawShadow(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, (int)(30 * hoverProgress)));
        g2d.fill(new RoundRectangle2D.Float(
            shadowOffset, shadowOffset, 
            width - shadowOffset, height - shadowOffset, 
            cornerRadius, cornerRadius
        ));
    }
    
    private void drawButtonBackground(Graphics2D g2d, int width, int height, Color color) {
        // 主背景
        g2d.setColor(color);
        g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        
        // 高光效果
        if (!isPressed) {
            g2d.setColor(new Color(255, 255, 255, (int)(60 * (1 - pressProgress))));
            g2d.fill(new RoundRectangle2D.Float(2, 2, width - 4, height / 2, cornerRadius - 2, cornerRadius - 2));
        }
    }
    
    private void drawButtonContent(Graphics2D g2d, int width, int height) {
        // 绘制图标
        Icon icon = getIcon();
        if (icon != null) {
            int iconX = (width - icon.getIconWidth()) / 2;
            int iconY = (height - icon.getIconHeight()) / 2;
            
            // 按下时稍微下移
            if (isPressed) {
                iconY += 1;
            }
            
            icon.paintIcon(this, g2d, iconX, iconY);
        }
        
        // 绘制文字
        String text = getText();
        if (text != null && !text.isEmpty()) {
            g2d.setColor(getForeground());
            g2d.setFont(getFont());
            
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (width - fm.stringWidth(text)) / 2;
            int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
            
            // 按下时稍微下移
            if (isPressed) {
                textY += 1;
            }
            
            g2d.drawString(text, textX, textY);
        }
    }
    
    // 设置样式的方法
    public void setButtonColors(Color normal, Color hover, Color pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        repaint();
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    public void setShadowOffset(int offset) {
        this.shadowOffset = offset;
        repaint();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setCursor(Cursor.getDefaultCursor());
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
