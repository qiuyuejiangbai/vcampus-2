package client.ui.dashboard.layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 中心卡槽容器：封装 CardLayout，提供按 key 切换页面。
 */
public class ContentHost extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final Map<String, JComponent> keyToComponent = new HashMap<String, JComponent>();
    private String currentKey = null;

    public enum TransitionType { FADE, SLIDE }

    public ContentHost() {
        setLayout(cardLayout);
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
    }
    
    public void addPage(String key, JComponent component) {
        if (key == null || component == null) return;
        keyToComponent.put(key, component);
        add(component, key);
    }

    public void showPage(String key) {
        if (!keyToComponent.containsKey(key)) return;
        cardLayout.show(this, key);
        currentKey = key;
        revalidate();
        repaint();
    }

    /**
     * 使用动画切换到目标页面。
     * 若当前无页面或目标等于当前，直接显示。
     */
    public void showPageAnimated(final String targetKey, final TransitionType type) {
        if (!keyToComponent.containsKey(targetKey) || targetKey.equals(currentKey)) {
            showPage(targetKey);
            return;
        }
        
        final JComponent currentComponent = keyToComponent.get(currentKey);
        final JComponent targetComponent = keyToComponent.get(targetKey);
        
        if (currentComponent == null || targetComponent == null) {
            showPage(targetKey);
            return;
        }
        
        // 开始动画切换
        startTransitionAnimation(currentComponent, targetComponent, targetKey, type);
    }
    
    /**
     * 开始过渡动画
     */
    private void startTransitionAnimation(final JComponent currentComp, final JComponent targetComp, 
                                        final String targetKey, final TransitionType type) {
        final Dimension size = getSize();
        if (size.width <= 0 || size.height <= 0) {
            showPage(targetKey);
            return;
        }
        
        // 渲染当前页面为图片
        final BufferedImage currentImage = renderToImage(currentComp, size);
        final BufferedImage targetImage = renderToImage(targetComp, size);
        
        if (currentImage == null || targetImage == null) {
            showPage(targetKey);
            return;
        }
        
        // 创建动画面板
        final JPanel animationPanel = new JPanel() {
            private float progress = 0.0f;
            
            public void setProgress(float progress) {
                this.progress = Math.max(0.0f, Math.min(1.0f, progress));
                repaint();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (type == TransitionType.FADE) {
                    // 淡入淡出效果
                    float currentAlpha = 1.0f - progress;
                    float targetAlpha = progress;
                    
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
                    g2d.drawImage(currentImage, 0, 0, null);
                    
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, targetAlpha));
                    g2d.drawImage(targetImage, 0, 0, null);
                } else if (type == TransitionType.SLIDE) {
                    // 滑动效果
                    int slideDistance = size.width;
                    int currentX = (int) (-slideDistance * progress);
                    int targetX = (int) (slideDistance * (1.0f - progress));
                    
                    g2d.drawImage(currentImage, currentX, 0, null);
                    g2d.drawImage(targetImage, targetX, 0, null);
                }
            }
        };
        
        animationPanel.setPreferredSize(size);
        animationPanel.setSize(size);
        
        add(animationPanel, "animation");
        cardLayout.show(this, "animation");
        
        // 启动动画
        Timer animationTimer = new Timer(16, new ActionListener() { // 60fps
            private int frame = 0;
            private final int totalFrames = 30; // 500ms动画
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    ((Timer) e.getSource()).stop();
                    
                    // 动画完成，切换到目标页面
                    remove(animationPanel);
                    showPage(targetKey);
                    return;
                }
                
                // 使用缓动函数
                float easedProgress = easeOutCubic(progress);
                try {
                    java.lang.reflect.Method setProgressMethod = animationPanel.getClass().getMethod("setProgress", float.class);
                    setProgressMethod.invoke(animationPanel, easedProgress);
                } catch (Exception ex) {
                    // 如果无法设置进度，继续执行动画
                }
            }
        });
        animationTimer.start();
    }
    
    /**
     * 缓动函数：三次方缓出
     */
    private float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    private BufferedImage renderToImage(JComponent comp, Dimension size) {
        if (comp == null || size == null) return null;
        try {
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 在 comp 的坐标系原样绘制，不修改其 size/layout，避免引发布局波动
            comp.printAll(g2);
            g2.dispose();
            return img;
        } catch (Throwable t) {
            return null;
        }
    }

    private BufferedImage renderSelfToImage(Dimension size) {
        if (size == null) return null;
        try {
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.printAll(g2);
            g2.dispose();
            return img;
        } catch (Throwable t) {
            return null;
        }
    }
}


