package client.ui.util;

import client.ui.modules.course.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * 支持悬浮效果的自定义表格单元格渲染器
 */
public class HoverableTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
    private int hoveredRow = -1;
    
    public void setHoveredRow(int row) {
        if (hoveredRow != row) {
            hoveredRow = row;
        }
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // 设置字体和颜色
        c.setFont(UITheme.CONTENT_FONT);
        
        if (isSelected) {
            // 选中状态：使用更深的绿色
            c.setBackground(UITheme.TABLE_SELECTED);
            c.setForeground(UITheme.DARK_GRAY);
        } else if (row == hoveredRow) {
            // 悬浮状态：使用悬浮色
            c.setBackground(UITheme.TABLE_HOVER);
            c.setForeground(UITheme.DARK_GRAY);
        } else {
            // 交替行颜色
            if (row % 2 == 0) {
                c.setBackground(UITheme.WHITE);
            } else {
                c.setBackground(new Color(248, 250, 252)); // 浅灰背景
            }
            c.setForeground(UITheme.DARK_GRAY);
        }
        
        // 设置内边距
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
        
        return c;
    }
}
