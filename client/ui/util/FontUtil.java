package client.ui.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * 字体工具类
 * 负责加载自定义字体并设置全局字体
 */
public class FontUtil {
    
    // 字体文件路径
    private static final String SOURCE_HAN_SANS_PATH = "resources/fonts/SourceHanSansSC-Regular-2.otf";
    private static final String INTER_FONT_PATH = "resources/fonts/Inter-UI-Regular-2.ttf";
    private static final String PACIFICO_FONT_PATH = "resources/fonts/Pacifico-1.ttf";
    
    // 字体实例
    private static Font sourceHanSansFont;
    private static Font interFont;
    private static Font pacificoFont;
    
    /**
     * 加载自定义字体
     */
    public static void loadCustomFonts() {
        try {
            // 加载思源黑体
            File sourceHanFile = new File(SOURCE_HAN_SANS_PATH);
            if (sourceHanFile.exists()) {
                sourceHanSansFont = Font.createFont(Font.TRUETYPE_FONT, sourceHanFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sourceHanSansFont);
                System.out.println("思源黑体加载成功");
            } else {
                System.err.println("思源黑体文件不存在: " + SOURCE_HAN_SANS_PATH);
            }
            
            // 加载Inter字体
            File interFile = new File(INTER_FONT_PATH);
            if (interFile.exists()) {
                interFont = Font.createFont(Font.TRUETYPE_FONT, interFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(interFont);
                System.out.println("Inter字体加载成功");
            } else {
                System.err.println("Inter字体文件不存在: " + INTER_FONT_PATH);
            }

            // 加载Pacifico字体
            File pacificoFile = new File(PACIFICO_FONT_PATH);
            if (pacificoFile.exists()) {
                pacificoFont = Font.createFont(Font.TRUETYPE_FONT, pacificoFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pacificoFont);
                System.out.println("Pacifico字体加载成功");
            } else {
                System.err.println("Pacifico字体文件不存在: " + PACIFICO_FONT_PATH);
            }
            
        } catch (FontFormatException | IOException e) {
            System.err.println("加载自定义字体失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取思源黑体
     */
    public static Font getSourceHanSansFont(int style, float size) {
        if (sourceHanSansFont != null) {
            return sourceHanSansFont.deriveFont(style, size);
        }
        // 回退到系统默认中文字体
        return new Font("Microsoft YaHei UI", style, (int)size);
    }
    
    /**
     * 获取Inter字体
     */
    public static Font getInterFont(int style, float size) {
        if (interFont != null) {
            return interFont.deriveFont(style, size);
        }
        // 回退到系统默认英文字体
        return new Font("Segoe UI", style, (int)size);
    }

    /**
     * 获取Pacifico字体
     */
    public static Font getPacificoFont(int style, float size) {
        if (pacificoFont != null) {
            return pacificoFont.deriveFont(style, size);
        }
        // 回退：若未加载，尝试临时加载一次
        try {
            File pacificoFile = new File(PACIFICO_FONT_PATH);
            if (pacificoFile.exists()) {
                pacificoFont = Font.createFont(Font.TRUETYPE_FONT, pacificoFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pacificoFont);
                return pacificoFont.deriveFont(style, size);
            }
        } catch (Exception ignored) {}
        return new Font("Segoe Script", style, (int)size);
    }
    
    /**
     * 设置全局默认字体
     * 中文使用思源黑体，英文使用Inter字体
     */
    public static void setGlobalFont() {
        try {
            // 首先加载自定义字体
            loadCustomFonts();
            
            // 设置默认字体为思源黑体
            Font defaultFont = getSourceHanSansFont(Font.PLAIN, 14);
            
            // 设置所有UI组件的默认字体
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    UIManager.put(key, defaultFont);
                }
            }
            
            System.out.println("全局字体设置完成");
            
        } catch (Exception e) {
            System.err.println("设置全局字体失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根据文本内容选择合适的字体
     * 中文使用思源黑体，英文使用Inter字体
     */
    public static Font getAppropriateFont(String text, int style, float size) {
        if (text == null || text.isEmpty()) {
            return getSourceHanSansFont(style, size);
        }
        
        // 检查是否包含中文字符
        boolean containsChinese = text.matches(".*[\\u4e00-\\u9fa5].*");
        
        if (containsChinese) {
            return getSourceHanSansFont(style, size);
        } else {
            return getInterFont(style, size);
        }
    }
    
    /**
     * 设置组件字体，根据文本内容自动选择
     */
    public static void setComponentFont(JComponent component, String text, int style, float size) {
        component.setFont(getAppropriateFont(text, style, size));
    }
    
    /**
     * 设置标签字体，根据文本内容自动选择
     */
    public static void setLabelFont(JLabel label, int style, float size) {
        String text = label.getText();
        label.setFont(getAppropriateFont(text, style, size));
    }
    
    /**
     * 设置按钮字体，根据文本内容自动选择
     */
    public static void setButtonFont(JButton button, int style, float size) {
        String text = button.getText();
        button.setFont(getAppropriateFont(text, style, size));
    }
    
    /**
     * 设置文本框字体，根据文本内容自动选择
     */
    public static void setTextFieldFont(JTextField textField, int style, float size) {
        String text = textField.getText();
        textField.setFont(getAppropriateFont(text, style, size));
    }
}
