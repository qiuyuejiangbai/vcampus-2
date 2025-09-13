package client.ui.modules.course;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UITheme {
    // 字体常量
    public static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 16);
    public static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 18);
    public static final Font SUBTITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 16);
    public static final Font CONTENT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);

    // 墨绿配色方案
    public static final Color PRIMARY_GREEN = new Color(44, 79, 61);      // #2C4F3D - 主色调
    public static final Color HOVER_GREEN = new Color(53, 93, 73);        // #355D49 - 悬停色
    public static final Color ACTIVE_GREEN = new Color(33, 64, 51);       // #214033 - 激活色
    public static final Color LIGHT_GREEN = new Color(67, 120, 95);       // #43785F - 浅绿色
    public static final Color VERY_LIGHT_GREEN = new Color(240, 248, 245); // #F0F8F5 - 极浅绿背景
    
    // 中性色
    public static final Color DARK_GRAY = new Color(27, 31, 36);          // #1B1F24 - 深灰文字
    public static final Color MEDIUM_GRAY = new Color(107, 114, 128);     // #6B7280 - 中灰文字
    public static final Color LIGHT_GRAY = new Color(229, 231, 235);      // #E5E7EB - 浅灰分割线
    public static final Color BACKGROUND_GRAY = new Color(246, 247, 248); // #F6F7F8 - 背景灰
    public static final Color WHITE = new Color(255, 255, 255);           // #FFFFFF - 白色
    
    // 功能色
    public static final Color SUCCESS_GREEN = new Color(34, 197, 94);     // #22C55E - 成功绿
    public static final Color WARNING_YELLOW = new Color(255, 193, 7);    // #FFC107 - 警告黄
    public static final Color ERROR_RED = new Color(220, 53, 69);         // #DC3545 - 错误红
    public static final Color INFO_BLUE = new Color(59, 130, 246);        // #3B82F6 - 信息蓝
    
    // 边框和阴影
    public static final Color BORDER_COLOR = new Color(229, 231, 235);    // #E5E7EB
    public static final Color CARD_BORDER = new Color(209, 213, 219);     // #D1D5DB
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 0.1f);    // 10% 透明度黑色
    
    // 圆角半径
    public static final int RADIUS_SMALL = 6;
    public static final int RADIUS_MEDIUM = 8;
    public static final int RADIUS_LARGE = 12;
    public static final int RADIUS_XLARGE = 16;
    
    // 间距常量
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 12;
    public static final int PADDING_LARGE = 16;
    public static final int PADDING_XLARGE = 24;
    
    // 组件尺寸
    public static final int BUTTON_HEIGHT = 36;
    public static final int INPUT_HEIGHT = 40;
    public static final int CARD_HEIGHT = 200;
    public static final int TABLE_ROW_HEIGHT = 48;
    
    // 创建常用边框的方法
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        );
    }
    
    public static Border createRoundedBorder(Color color, int thickness, int radius) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, thickness),
            BorderFactory.createEmptyBorder(radius, radius, radius, radius)
        );
    }
    
    public static Border createEmptyBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
    
    // 创建渐变背景的方法
    public static GradientPaint createGreenGradient(Rectangle bounds) {
        return new GradientPaint(
            0, bounds.y, PRIMARY_GREEN,
            0, bounds.y + bounds.height, HOVER_GREEN
        );
    }
    
    // 创建按钮样式的方法
    public static void styleButton(JButton button) {
        button.setFont(CONTENT_FONT);
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_LARGE, PADDING_SMALL, PADDING_LARGE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // 创建输入框样式的方法
    public static void styleTextField(JTextField textField) {
        textField.setFont(CONTENT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        textField.setBackground(WHITE);
    }
}
