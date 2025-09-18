package client.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动画工具类
 * 提供各种UI动画效果
 * 参考登录界面的优化技术，提供丝滑的动画体验
 */
public class AnimationUtil {
    
    // 预计算的缓动值数组，用于更快的动画（参考登录界面）
    private static final float[] EASE_OUT_CUBIC_TABLE = new float[101];
    static {
        for (int i = 0; i <= 100; i++) {
            float t = i / 100.0f;
            EASE_OUT_CUBIC_TABLE[i] = 1 - (1 - t) * (1 - t) * (1 - t);
        }
    }
    
    // 缓存缓动函数计算结果，避免重复计算
    private static final ConcurrentHashMap<Float, Float> easeOutCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Float, Float> easeOutCubicCache = new ConcurrentHashMap<>();
    private static final int CACHE_SIZE = 1000;
    
    /**
     * 淡入动画效果（优化版本，参考登录界面的丝滑动画）
     * @param component 要添加动画的组件
     * @param duration 动画持续时间（毫秒）
     * @param delay 动画延迟时间（毫秒）
     */
    public static void fadeIn(JComponent component, int duration, int delay) {
        if (component == null) return;
        
        // 设置初始透明度为0
        component.setVisible(false);
        
        // 使用16ms间隔（60fps）获得更流畅的动画，参考登录界面
        Timer timer = new Timer(16, new ActionListener() {
            private long startTime = System.currentTimeMillis() + delay;
            private boolean hasStarted = false;
            private float lastAlpha = 0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                
                if (!hasStarted && currentTime >= startTime) {
                    hasStarted = true;
                    component.setVisible(true);
                }
                
                if (hasStarted) {
                    long elapsed = currentTime - startTime;
                    float progress = Math.min((float) elapsed / duration, 1.0f);
                    
                    // 使用快速缓动函数实现平滑动画，参考登录界面
                    float alpha = fastEaseOutCubic(progress);
                    
                    // 只有当进度变化足够大时才更新UI，减少重绘频率
                    if (Math.abs(alpha - lastAlpha) > 0.01f) {
                        // 设置组件透明度
                        component.setOpaque(false);
                        component.putClientProperty("alpha", alpha);
                        
                        // 优化：只重绘组件区域，而不是整个容器
                        Rectangle bounds = component.getBounds();
                        if (bounds.width > 0 && bounds.height > 0) {
                            component.getParent().repaint(bounds.x, bounds.y, bounds.width, bounds.height);
                        }
                        
                        lastAlpha = alpha;
                    }
                    
                    // 动画完成
                    if (progress >= 1.0f) {
                        ((Timer) e.getSource()).stop();
                        component.putClientProperty("alpha", 1.0f);
                    }
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * 缓动函数 - ease-out（带缓存优化）
     * @param t 进度值 (0-1)
     * @return 缓动后的值
     */
    private static float easeOutCached(float t) {
        // 使用缓存避免重复计算
        Float cached = easeOutCache.get(t);
        if (cached != null) {
            return cached;
        }
        
        // 使用更平滑的缓动函数
        float result = 1 - (float) Math.pow(1 - t, 3);
        
        // 限制缓存大小
        if (easeOutCache.size() < CACHE_SIZE) {
            easeOutCache.put(t, result);
        }
        
        return result;
    }
    
    /**
     * 缓动函数 - ease-out-cubic（带缓存优化，更加丝滑）
     * @param t 进度值 (0-1)
     * @return 缓动后的值
     */
    private static float easeOutCubicCached(float t) {
        // 使用缓存避免重复计算
        Float cached = easeOutCubicCache.get(t);
        if (cached != null) {
            return cached;
        }
        
        // 使用更平滑的缓动函数：ease-out-cubic
        float result = 1 - (float) Math.pow(1 - t, 3);
        
        // 限制缓存大小
        if (easeOutCubicCache.size() < CACHE_SIZE) {
            easeOutCubicCache.put(t, result);
        }
        
        return result;
    }
    
    /**
     * 快速缓动函数：使用查表法（参考登录界面）
     * @param t 进度值 (0-1)
     * @return 缓动后的值
     */
    private static float fastEaseOutCubic(float t) {
        if (t <= 0) return 0;
        if (t >= 1) return 1;
        
        int index = (int) (t * 100);
        float fraction = t * 100 - index;
        
        if (index >= 100) return EASE_OUT_CUBIC_TABLE[100];
        
        // 线性插值
        return EASE_OUT_CUBIC_TABLE[index] + 
               (EASE_OUT_CUBIC_TABLE[index + 1] - EASE_OUT_CUBIC_TABLE[index]) * fraction;
    }
    
    /**
     * 清理缓存
     */
    private static void clearCache() {
        if (easeOutCache.size() > CACHE_SIZE / 2) {
            easeOutCache.clear();
        }
        if (easeOutCubicCache.size() > CACHE_SIZE / 2) {
            easeOutCubicCache.clear();
        }
    }
    
    /**
     * 为组件添加自定义绘制支持透明度
     * @param component 要添加透明度支持的组件
     */
    public static void enableAlphaSupport(JComponent component) {
        component.addPropertyChangeListener("alpha", evt -> {
            component.repaint();
        });
    }
    
    /**
     * 创建支持透明度的面板（优化版本）
     * @return 支持透明度的JPanel
     */
    public static JPanel createAlphaPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // 获取透明度
                Float alpha = (Float) getClientProperty("alpha");
                if (alpha != null && alpha < 1.0f) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                }
                
                // 启用抗锯齿和硬件加速
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                super.paintComponent(g2d);
                g2d.dispose();
            }
            
            @Override
            public boolean isOpaque() {
                return false; // 确保支持透明度
            }
        };
    }
    
    /**
     * 批量淡入动画（优化版本，参考登录界面的丝滑效果）
     * @param components 要添加动画的组件数组
     * @param duration 动画持续时间（毫秒）
     * @param delay 动画延迟时间（毫秒）
     * @param stagger 组件间延迟时间（毫秒）
     */
    public static void fadeInBatch(JComponent[] components, int duration, int delay, int stagger) {
        if (components == null || components.length == 0) return;
        
        // 优化：减少组件间延迟，使动画更加连贯
        int optimizedStagger = Math.max(10, stagger); // 最小10ms间隔
        
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                fadeIn(components[i], duration, delay + (i * optimizedStagger));
            }
        }
    }
}