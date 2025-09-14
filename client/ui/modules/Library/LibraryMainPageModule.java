package client.ui.modules.Library;

import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LibraryMainPageModule extends JPanel {
    private final UserVO currentUser;
    private Image backgroundImage;

    public LibraryMainPageModule(UserVO currentUser) {
        this.currentUser = currentUser;

        // 加载背景图片
        try {
            // 请确保 "background.jpg" 文件位于项目的根目录或资源路径下
            ImageIcon imageIcon = new ImageIcon("resources/images/LibraryBackgroundImage.jpg");
            this.backgroundImage = imageIcon.getImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("背景图片加载失败，将使用默认背景。");
        }

        initUI();
    }

    private void initUI() {
        // 使用 GridBagLayout 进行精确布局
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(40, 60, 40, 60));

        // 中心面板，用于承载欢迎信息
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false); // 设置为非透明，透出背景图片
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 欢迎标题
        JLabel titleLabel = new JLabel(currentUser.getName() + "，欢迎你来到图书馆!");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 30f));
        titleLabel.setForeground(new Color(0x0B, 0x3D, 0x2E));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 标语
        JLabel mottoLabel = new JLabel("知识是通往未来的阶梯!");
        mottoLabel.setFont(mottoLabel.getFont().deriveFont(Font.ITALIC, 20f));
        mottoLabel.setForeground(new Color(50, 5, 120));
        mottoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 垂直间距
        // 移除第一个 verticalGlue，文字会从顶部开始布局
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(mottoLabel);
        contentPanel.add(Box.createVerticalGlue());

        // 使用 GridBagConstraints 将 contentPanel 向上对齐
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH; // 核心改动：将组件固定到顶部
        gbc.weighty = 1.0; // 核心改动：让组件垂直方向的额外空间都分配给它，但由于 anchor 的作用，它不会被拉伸
        add(contentPanel, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // 绘制背景图片，使其填充整个面板
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}