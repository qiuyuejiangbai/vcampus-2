package client.ui.dashboard.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 一個自訂的漸層按鈕元件。
 * 它可以處於「啟用」(Active) 或「停用」(Inactive) 狀態，並顯示不同的顏色。
 * Library 模块使用
 */
public class GradientButton extends JComponent {

    // --- 顏色定義 ---
    // 停用狀態的顏色 (灰色漸層)
    private final Color inactiveStartColor = new Color(100, 100, 100);
    private final Color inactiveEndColor = new Color(60, 60, 60);

    // 啟用狀態的顏色 (深綠色漸層)
    private final Color activeStartColor = new Color(20, 140, 60);
    private final Color activeEndColor = new Color(10, 100, 40);

    // 滑鼠懸停時的顏色 (比原色稍亮)
    private final Color hoverModifier = new Color(25, 25, 25, 200); // 半透明亮色

    // --- 狀態變數 ---
    private String text;
    private boolean isActive = false; // 按鈕是否處於啟用狀態
    private boolean isHover = false;  // 滑鼠是否懸停在按鈕上

    // 使用標準的 ActionListener 來處理點擊事件
    private final List<ActionListener> listeners = new ArrayList<>();

    public GradientButton(String text) {
        this.text = text;
        setFont(new Font("微軟正黑體", Font.BOLD, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // 使用 GradientButton.this 來明確指定事件來源是按鈕本身，而不是監聽器
                setActive(true);
                fireActionPerformed(new ActionEvent(GradientButton.this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
    }

    /**
     * 公開的方法，用於從外部設定按鈕的啟用狀態。
     * @param active true 為啟用 (深綠色)，false 為停用 (灰色)
     */
    public void setActive(boolean active) {
        this.isActive = active;
        repaint(); // 狀態改變後立即重繪
    }

    public boolean isActive() {
        return this.isActive;
    }

    public String getText() {
        return text;
    }

    // --- 事件處理 ---
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    protected void fireActionPerformed(ActionEvent e) {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 根據 isActive 狀態選擇基礎顏色
        Color startColor = isActive ? activeStartColor : inactiveStartColor;
        Color endColor = isActive ? activeEndColor : inactiveEndColor;

        // 如果滑鼠懸停，讓顏色變亮一點
        if (isHover) {
            startColor = startColor.brighter();
            endColor = endColor.brighter();
        }

        // 繪製漸層背景
        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, height, endColor);
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, width, height, 20, 20); // 圓角矩形

        // 繪製文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(getFont());
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        // 提供一個預設大小
        return new Dimension(150, 50);
    }
}