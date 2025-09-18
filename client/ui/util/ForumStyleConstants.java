package client.ui.util;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * 论坛模块样式常量类
 * 统一管理颜色、边框、字体等样式定义，避免重复代码
 */
public class ForumStyleConstants {
    
    // ========== 颜色常量 ==========
    
    // 背景色
    public static final Color BACKGROUND_LIGHT = new Color(248, 249, 250);
    public static final Color BACKGROUND_WHITE = new Color(255, 255, 255);
    public static final Color BACKGROUND_HOVER = new Color(243, 244, 246);
    
    // 主题色
    public static final Color PRIMARY_GREEN = new Color(24, 121, 78);
    public static final Color LIGHT_GREEN = new Color(223, 245, 232);
    public static final Color DARK_GREEN = new Color(210, 238, 224);
    public static final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);
    
    // 文字色
    public static final Color TEXT_PRIMARY = new Color(55, 65, 81);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    public static final Color TEXT_MUTED = new Color(156, 163, 175);
    public static final Color TEXT_PLACEHOLDER = new Color(156, 163, 175);
    public static final Color TEXT_DARK = new Color(31, 41, 55);
    
    // 边框色
    public static final Color BORDER_LIGHT = new Color(229, 231, 235);
    
    // 状态色
    public static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    public static final Color ERROR_RED = new Color(239, 68, 68);
    public static final Color GOLD = new Color(255, 215, 0);
    
    // ========== 边框常量 ==========
    
    public static final Border NONE = new EmptyBorder(0, 0, 0, 0);
    public static final Border STANDARD = new EmptyBorder(12, 12, 12, 12);
    public static final Border CARD_INNER = new EmptyBorder(16, 18, 16, 18);
    public static final Border CARD_OUTER = new EmptyBorder(12, 16, 12, 16);
    public static final Border SEARCH_BOX = new EmptyBorder(0, 10, 0, 10);
    public static final Border TITLE_PANEL = new EmptyBorder(15, 15, 10, 15);
    public static final Border CONTENT_PANEL = new EmptyBorder(0, 15, 15, 15);
    public static final Border NAV_PANEL = new EmptyBorder(10, 20, 10, 20);
    public static final Border MAIN_CONTENT = new EmptyBorder(20, 20, 20, 20);
    public static final Border POST_CONTENT = new EmptyBorder(20, 20, 20, 20);
    public static final Border POST_HEADER = new EmptyBorder(0, 0, 15, 0);
    public static final Border AVATAR_CONTAINER = new EmptyBorder(0, 0, 0, 15);
    public static final Border NAME_TIME_CONTAINER = new EmptyBorder(15, 0, 0, 0);
    public static final Border TITLE_CONTAINER = new EmptyBorder(0, 0, 5, 0);
    public static final Border SECTION_TAG_CONTAINER = new EmptyBorder(0, 0, 10, 0);
    public static final Border POST_STATS = new EmptyBorder(15, 0, 0, 0);
    public static final Border COMMENT_SECTION = new EmptyBorder(20, 20, 20, 20);
    public static final Border COMMENT_TITLE = new EmptyBorder(0, 0, 10, 0);
    public static final Border FLOATING_REPLY = new EmptyBorder(15, 0, 15, 20);
    public static final Border REPLY_TEXT_AREA = new EmptyBorder(12, 20, 12, 12);
    public static final Border RIGHT_BUTTON_PANEL = new EmptyBorder(0, 15, 0, 0);
    public static final Border BUTTON_STANDARD = new EmptyBorder(0, 14, 0, 14);
    public static final Border BUTTON_CATEGORY = new EmptyBorder(0, 18, 0, 18);
    public static final Border THREAD_ITEMS = new EmptyBorder(10, 0, 10, 0);
    public static final Border WEST_WRAP = new EmptyBorder(0, 0, 0, 12);
    public static final Border ESSENCE_ICON = new EmptyBorder(0, 5, 0, 0);
    public static final Border TITLE_LABEL = new EmptyBorder(6, 0, 0, 0);
    public static final Border SUMMARY_LABEL = new EmptyBorder(6, 0, 0, 0);
    public static final Border FOOTER_NONE = new EmptyBorder(0, 0, 0, 0);
    public static final Border LIKE_CONTAINER = new EmptyBorder(0, -5, 0, 0);
    public static final Border REPLY_LABEL = new EmptyBorder(0, 10, 0, 0);
    public static final Border TAG_STANDARD = new EmptyBorder(2, 8, 2, 8);
    public static final Border TAG_LARGE = new EmptyBorder(3, 12, 3, 12);
    public static final Border REPLY_ITEM = new EmptyBorder(12, 12, 12, 12);
    public static final Border REPLY_AVATAR_WRAP = new EmptyBorder(0, 0, 0, 12);
    public static final Border REPLY_RIGHT = new EmptyBorder(0, 0, 0, 0);
    public static final Border REPLY_CONTENT_AREA = new EmptyBorder(4, 0, 6, 0);
    public static final Border REPLY_OPS = new EmptyBorder(-2, -4, 0, 0);
    public static final Border REPLY_LIKE_PANEL = new EmptyBorder(0, -2, 0, 0);
    public static final Border TOAST = new EmptyBorder(12, 16, 12, 16);
    public static final Border ANNOUNCEMENT_ICON = new EmptyBorder(0, 0, 0, 8);
    public static final Border FIRE_ICON = new EmptyBorder(0, 0, 0, 8);
    public static final Border SECTION_PANEL = new EmptyBorder(5, 0, 5, 0);
    public static final Border EMPTY_LABEL = new EmptyBorder(4, 0, 4, 0);
    
    // ========== 字体常量 ==========
    
    public static final Font TITLE_LARGE = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 16f);
    public static final Font TITLE_MEDIUM = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f);
    public static final Font BODY_TEXT = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f);
    public static final Font SMALL_TEXT = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f);
    public static final Font CAPTION_TEXT = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 13f);
    public static final Font BUTTON_TEXT = UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 16f);
    public static final Font SEARCH_ICON = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f);
    public static final Font TEXT_FIELD = UIManager.getFont("TextField.font").deriveFont(Font.PLAIN, 14f);
    public static final Font BOLD_TITLE = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f);
    public static final Font ESSENCE_STAR = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f);
    
    // ========== 尺寸常量 ==========
    
    public static final int SEARCH_BOX_HEIGHT = 36;
    public static final int SEARCH_BOX_WIDTH = 240;
    public static final int TOOLBAR_HEIGHT = 50;
    public static final int NAV_HEIGHT = 60;
    public static final int RIGHT_PANEL_WIDTH = 300;
    public static final int ANNOUNCEMENT_HEIGHT = 220;
    public static final int HOT_SECTIONS_HEIGHT = 260;
    public static final int BUTTON_STANDARD_WIDTH = 110;
    public static final int BUTTON_STANDARD_HEIGHT = 36;
    public static final int BUTTON_CATEGORY_WIDTH = 72;
    public static final int BUTTON_CATEGORY_HEIGHT = 34;
    public static final int RIGHT_BUTTON_WIDTH = 90;
    public static final int AVATAR_SIZE_LARGE = 48;
    public static final int AVATAR_SIZE_MEDIUM = 36;
    public static final int ICON_SIZE_SMALL = 16;
    public static final int ICON_SIZE_MEDIUM = 20;
    public static final int CORNER_ARC = 16;
    public static final int MIN_CARD_HEIGHT = 120;
    
    // ========== 工具方法 ==========
    
    /**
     * 应用卡片样式
     */
    public static void applyCardStyle(javax.swing.JPanel panel) {
        panel.setBackground(BACKGROUND_WHITE);
        panel.setBorder(CARD_INNER);
        panel.setOpaque(false);
    }
    
    /**
     * 应用背景样式
     */
    public static void applyBackgroundStyle(javax.swing.JPanel panel) {
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(NONE);
    }
    
    /**
     * 应用透明样式
     */
    public static void applyTransparentStyle(javax.swing.JPanel panel) {
        panel.setOpaque(false);
        panel.setBorder(NONE);
    }
    
    /**
     * 应用白色背景样式
     */
    public static void applyWhiteBackgroundStyle(javax.swing.JPanel panel) {
        panel.setBackground(BACKGROUND_WHITE);
        panel.setOpaque(true);
    }
}
