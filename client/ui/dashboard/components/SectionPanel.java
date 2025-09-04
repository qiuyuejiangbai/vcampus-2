package client.ui.dashboard.components;

import javax.swing.*;
import java.awt.*;

/** 统一卡片容器：用于公告/待办/今日课表等。 */
public class SectionPanel extends JPanel {
    public SectionPanel(String title, JComponent content) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(new Color(0x1B,0x1F,0x24));

        add(titleLabel, BorderLayout.NORTH);
        add(content != null ? content : new JPanel(), BorderLayout.CENTER);
    }
}


