package client.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

/**
 * 动画工具类
 * 提供常用的动画效果和缓动函数
 */
public class AnimationUtil {
    
    /**
     * 缓动函数类型
     */
    public enum EasingType {
        LINEAR, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC, EASE_OUT_BACK, EASE_OUT_ELASTIC
    }
    
    /**
     * 线性插值
     */
    public static float linear(float t) {
        return t;
    }
    
    /**
     * 三次方缓出
     */
    public static float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }
    
    /**
     * 三次方缓入缓出
     */
    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
    
    /**
     * 回弹缓出
     */
    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }
    
    /**
     * 弹性缓出
     */
    public static float easeOutElastic(float t) {
        float c4 = (2 * (float) Math.PI) / 3;
        return t == 0 ? 0 : t == 1 ? 1 : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;
    }
    
    /**
     * 根据类型获取缓动函数
     */
    public static float getEasing(EasingType type, float t) {
        switch (type) {
            case LINEAR: return linear(t);
            case EASE_OUT_CUBIC: return easeOutCubic(t);
            case EASE_IN_OUT_CUBIC: return easeInOutCubic(t);
            case EASE_OUT_BACK: return easeOutBack(t);
            case EASE_OUT_ELASTIC: return easeOutElastic(t);
            default: return linear(t);
        }
    }
    
    /**
     * 创建淡入动画
     */
    public static Timer createFadeInAnimation(JComponent component, int duration, ActionListener onComplete) {
        return createOpacityAnimation(component, 0.0f, 1.0f, duration, EasingType.EASE_OUT_CUBIC, onComplete);
    }
    
    /**
     * 创建淡出动画
     */
    public static Timer createFadeOutAnimation(JComponent component, int duration, ActionListener onComplete) {
        return createOpacityAnimation(component, 1.0f, 0.0f, duration, EasingType.EASE_OUT_CUBIC, onComplete);
    }
    
    /**
     * 创建透明度动画
     */
    public static Timer createOpacityAnimation(JComponent component, float startAlpha, float endAlpha, 
                                            int duration, EasingType easingType, ActionListener onComplete) {
        Timer timer = new Timer(16, new ActionListener() {
            private int frame = 0;
            private final int totalFrames = duration / 16; // 60fps
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        onComplete.actionPerformed(e);
                    }
                    return;
                }
                
                float easedProgress = getEasing(easingType, progress);
                float currentAlpha = startAlpha + (endAlpha - startAlpha) * easedProgress;
                
                // 设置透明度
                if (component instanceof JComponent) {
                    try {
                        java.lang.reflect.Method setAlphaMethod = component.getClass().getMethod("setAlpha", float.class);
                        setAlphaMethod.invoke(component, currentAlpha);
                    } catch (Exception ex) {
                        // 如果组件不支持透明度，使用重绘
                        component.repaint();
                    }
                }
            }
        });
        timer.start();
        return timer;
    }
    
    /**
     * 创建缩放动画
     */
    public static Timer createScaleAnimation(JComponent component, float startScale, float endScale, 
                                           int duration, EasingType easingType, ActionListener onComplete) {
        Timer timer = new Timer(16, new ActionListener() {
            private int frame = 0;
            private final int totalFrames = duration / 16;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        onComplete.actionPerformed(e);
                    }
                    return;
                }
                
                float easedProgress = getEasing(easingType, progress);
                float currentScale = startScale + (endScale - startScale) * easedProgress;
                
                // 应用缩放变换
                component.setSize((int)(component.getWidth() * currentScale), (int)(component.getHeight() * currentScale));
                component.revalidate();
                component.repaint();
            }
        });
        timer.start();
        return timer;
    }
    
    /**
     * 创建位移动画
     */
    public static Timer createMoveAnimation(JComponent component, Point startPos, Point endPos, 
                                          int duration, EasingType easingType, ActionListener onComplete) {
        Timer timer = new Timer(16, new ActionListener() {
            private int frame = 0;
            private final int totalFrames = duration / 16;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        onComplete.actionPerformed(e);
                    }
                    return;
                }
                
                float easedProgress = getEasing(easingType, progress);
                int currentX = (int) (startPos.x + (endPos.x - startPos.x) * easedProgress);
                int currentY = (int) (startPos.y + (endPos.y - startPos.y) * easedProgress);
                
                component.setLocation(currentX, currentY);
            }
        });
        timer.start();
        return timer;
    }
    
    /**
     * 创建摇摆动画
     */
    public static Timer createShakeAnimation(JComponent component, int intensity, int duration, ActionListener onComplete) {
        Point originalPos = component.getLocation();
        Timer timer = new Timer(16, new ActionListener() {
            private int frame = 0;
            private final int totalFrames = duration / 16;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    component.setLocation(originalPos);
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        onComplete.actionPerformed(e);
                    }
                    return;
                }
                
                // 摇摆效果：使用正弦波
                float shakeIntensity = intensity * (1.0f - progress); // 逐渐减弱
                int offsetX = (int) (Math.sin(frame * 0.5) * shakeIntensity);
                int offsetY = (int) (Math.cos(frame * 0.3) * shakeIntensity * 0.5);
                
                component.setLocation(originalPos.x + offsetX, originalPos.y + offsetY);
            }
        });
        timer.start();
        return timer;
    }
    
    /**
     * 创建脉冲动画
     */
    public static Timer createPulseAnimation(JComponent component, float minScale, float maxScale, 
                                           int duration, ActionListener onComplete) {
        Timer timer = new Timer(16, new ActionListener() {
            private int frame = 0;
            private final int totalFrames = duration / 16;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float progress = (float) frame / totalFrames;
                
                if (progress >= 1.0f) {
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        onComplete.actionPerformed(e);
                    }
                    return;
                }
                
                // 脉冲效果：使用正弦波
                float pulseScale = minScale + (maxScale - minScale) * (float) Math.sin(progress * Math.PI);
                
                // 应用缩放
                component.setSize((int)(component.getWidth() * pulseScale), (int)(component.getHeight() * pulseScale));
                component.revalidate();
                component.repaint();
            }
        });
        timer.start();
        return timer;
    }
}
