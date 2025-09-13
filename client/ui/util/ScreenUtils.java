package client.ui.util;

import java.awt.*;

/**
 * 屏幕工具类
 * 提供屏幕适配和窗口定位功能
 */
public class ScreenUtils {
    
    /**
     * 获取屏幕中心位置
     * @param windowSize 窗口大小
     * @return 居中位置
     */
    public static Point getCenteredLocation(Dimension windowSize) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle screenBounds = gc.getBounds();
        
        int x = (screenBounds.width - windowSize.width) / 2;
        int y = (screenBounds.height - windowSize.height) / 2;
        
        return new Point(x, y);
    }
    
    /**
     * 获取注册窗口大小
     * @return 注册窗口大小
     */
    public static Dimension getRegisterWindowSize() {
        return new Dimension(1000, 700);
    }
    
    /**
     * 获取登录窗口大小
     * @return 登录窗口大小
     */
    public static Dimension getLoginWindowSize() {
        return new Dimension(1000, 700);
    }
    
    /**
     * 获取屏幕尺寸
     * @return 屏幕尺寸
     */
    public static Dimension getScreenSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle screenBounds = gc.getBounds();
        
        return new Dimension(screenBounds.width, screenBounds.height);
    }
    
    /**
     * 检查窗口是否超出屏幕边界
     * @param windowSize 窗口大小
     * @param location 窗口位置
     * @return 是否超出边界
     */
    public static boolean isWindowOutOfBounds(Dimension windowSize, Point location) {
        Dimension screenSize = getScreenSize();
        
        return location.x < 0 || 
               location.y < 0 || 
               location.x + windowSize.width > screenSize.width || 
               location.y + windowSize.height > screenSize.height;
    }
}
