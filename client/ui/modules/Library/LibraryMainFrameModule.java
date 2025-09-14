package client.ui.modules.Library;

import client.controller.LibraryController;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LibraryMainFrameModule extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private final UserVO currentUser;
    private final LibraryController controller;

    // 各个子模块
    private LibraryBookSearchModule libraryBookSearchModule;
    private LibraryBorrowHistoryModule libraryBorrowHistoryModule;
    private LibraryBookManageModule libraryBookManageModule;
    private LibraryMainPageModule libraryMainPageModule;

    // 新增的文献模块
    private LibraryDocumentSearchModule libraryDocumentSearchModule;
    private LibraryDocumentManageModule libraryDocumentManageModule;

    public LibraryMainFrameModule(UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = new LibraryController(currentUser.getUserId());
        initUI(currentUser);
    }

    @Override
    public void dispose() {
        if (controller != null) {
            controller.close();
        }
        super.dispose();
    }

    public LibraryController getController() {
        return controller;
    }

    private void initUI(UserVO user) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // 内容区
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        // ==== 顶部 AppBar ====
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(0x0B, 0x3D, 0x2E),   // 深绿色
                        getWidth(), getHeight(),
                        new Color(0x14, 0x66, 0x4E)); // 稍浅绿色
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(getWidth(), 56));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(0, 0, 0, 30)));

        // 左侧标题
        JLabel titleLabel = new JLabel("图书馆主页");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        topBar.add(titleLabel, BorderLayout.WEST);

        // 右侧导航按钮容器
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        navPanel.setOpaque(false);
        topBar.add(navPanel, BorderLayout.EAST);

        ButtonGroup navGroup = new ButtonGroup();

        // 工具方法：创建带圆角背景效果的按钮
        java.util.function.Function<String, JToggleButton> createNavButton = (text) -> {
            JToggleButton btn = new JToggleButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isSelected()) {
                        g2.setColor(new Color(0x0B, 0x3D, 0x2E)); // 深绿背景
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(20, 100, 80, 120)); // 浅绿透明
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    }
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };

            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setForeground(Color.WHITE);
            btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
            btn.setBorder(new EmptyBorder(8, 18, 8, 18)); // 增大尺寸
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            navGroup.add(btn);
            navPanel.add(btn);
            return btn;
        };

        // === 学生 / 普通用户 ===
        if (user.getRole() == 0 || user.getRole() == 1) {
            libraryMainPageModule = new LibraryMainPageModule(currentUser);
            contentPanel.add(libraryMainPageModule, "mainPage");
            libraryBookSearchModule = new LibraryBookSearchModule(controller, currentUser);
            libraryBorrowHistoryModule = new LibraryBorrowHistoryModule(user.getUserId());
            libraryDocumentSearchModule = new LibraryDocumentSearchModule(controller);

            contentPanel.add(libraryBookSearchModule, "search");
            contentPanel.add(libraryBorrowHistoryModule, "history");
            contentPanel.add(libraryDocumentSearchModule, "docSearch");

            JToggleButton btnHome = createNavButton.apply("主页");
            JToggleButton btnSearch = createNavButton.apply("图书检索");
            JToggleButton btnHistory = createNavButton.apply("借阅记录");
            JToggleButton btnDocSearch = createNavButton.apply("文献检索");

            btnHome.addActionListener(e -> {
                cardLayout.show(contentPanel, "mainPage");
                titleLabel.setText("图书馆主页");
            });

            btnSearch.addActionListener(e -> {
                libraryBookSearchModule.refreshTable();
                cardLayout.show(contentPanel, "search");
                titleLabel.setText("图书检索");
            });

            btnHistory.addActionListener(e -> {
                libraryBorrowHistoryModule.refreshData();
                cardLayout.show(contentPanel, "history");
                titleLabel.setText("借阅记录");
            });

            btnDocSearch.addActionListener(e -> {
                libraryDocumentSearchModule.refreshTable();
                cardLayout.show(contentPanel, "docSearch");
                titleLabel.setText("文献检索");
            });

            btnHome.setSelected(true);
            cardLayout.show(contentPanel, "mainPage");
        }

        // === 管理员 ===
        if (user.getRole() == 2) {
            libraryBookManageModule = new LibraryBookManageModule(controller);
            libraryDocumentManageModule = new LibraryDocumentManageModule(controller);

            contentPanel.add(libraryBookManageModule, "manage");
            contentPanel.add(libraryDocumentManageModule, "docManage");

            JToggleButton btnManage = createNavButton.apply("图书管理");
            JToggleButton btnDocManage = createNavButton.apply("文献管理");

            btnManage.addActionListener(e -> {
                libraryBookManageModule.refreshTable();
                cardLayout.show(contentPanel, "manage");
                titleLabel.setText("图书管理");
            });

            btnDocManage.addActionListener(e -> {
                libraryDocumentManageModule.refreshTable();
                cardLayout.show(contentPanel, "docManage");
                titleLabel.setText("文献管理");
            });

            btnManage.setSelected(true);
            cardLayout.show(contentPanel, "manage");
        }

        // 总布局
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }
}
