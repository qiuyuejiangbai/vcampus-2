package client.ui.dashboard.components;

import client.ui.util.FontUtil;
import javax.swing.*;
import java.awt.*;

/** 指标卡片：标题 + 数值 + 次级文本。 */
public class StatCard extends JPanel {
    private final JLabel titleLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();
    private final JLabel subLabel = new JLabel();

    public StatCard(String title, String value, String subText) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Color.WHITE);

        titleLabel.setText(title);
        titleLabel.setForeground(new Color(0x6B,0x72,0x80));
        
        valueLabel.setText(value);
        valueLabel.setFont(FontUtil.getSourceHanSansFont(Font.BOLD, 28f));
        valueLabel.setForeground(new Color(0x1B,0x1F,0x24));
        
        subLabel.setText(subText);
        subLabel.setForeground(new Color(0x6B,0x72,0x80));

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(subLabel, BorderLayout.SOUTH);
    }
}


