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

    // 子模块
    private LibraryBookSearchModule libraryBookSearchModule;
    private LibraryBorrowHistoryModule libraryBorrowHistoryModule;
    private LibraryBookManageModule libraryBookManageModule;
    private LibraryMainPageModule libraryMainPageModule;
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
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x0B, 0x3D, 0x2E),
                        getWidth(), getHeight(), new Color(0x14, 0x66, 0x4E)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(getWidth(), 56));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(0, 0, 0, 30)));

        JLabel titleLabel = new JLabel("图书馆主页");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        topBar.add(titleLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        navPanel.setOpaque(false);
        topBar.add(navPanel, BorderLayout.EAST);

        ButtonGroup navGroup = new ButtonGroup();

        // ========= 带下划线动画的按钮 =========
        // ========= 带下划线动画的按钮 =========
        class AnimatedUnderlineButton extends JToggleButton {
            private float progress = 0f;
            private boolean expanding = false;
            private final javax.swing.Timer anim;

            public AnimatedUnderlineButton(String text) {
                super(text);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD, 14f));
                setBorder(new EmptyBorder(8, 18, 14, 18));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                anim = new javax.swing.Timer(1000 / 60, null);
                anim.addActionListener(e -> {
                    float speed = 0.12f;
                    if (expanding) {
                        progress = Math.min(1f, progress + speed);
                    } else {
                        progress = Math.max(0f, progress - speed);
                    }
                    repaint();
                    if (progress == 0f || progress == 1f) {
                        ((javax.swing.Timer) e.getSource()).stop();
                    }
                });

                // 悬停驱动的展开/收回
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { startAnim(true); }
                    @Override public void mouseExited (java.awt.event.MouseEvent e) {
                        // 未选中时才收回；选中保持满宽
                        if (!isSelected()) startAnim(false);
                    }
                });

                // 选中状态变化驱动的展开/收回（修复首次切换不收回的问题）
                getModel().addChangeListener(ev -> {
                    if (getModel().isSelected()) {
                        // 选中：立即满宽并停止动画（避免闪烁）
                        progress = 1f;
                        anim.stop();
                        repaint();
                    } else {
                        // 取消选中：若鼠标不在其上，启动收回动画
                        if (!getModel().isRollover()) {
                            expanding = false;
                            if (!anim.isRunning()) anim.start();
                        }
                    }
                });
            }

            private void startAnim(boolean expand) {
                expanding = expand;
                if (!anim.isRunning()) anim.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()) {
                    g2.setColor(new Color(0x0B, 0x3D, 0x2E));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    // 选中保持满宽（如果外部先置选中，这里确保一致）
                    progress = 1f;
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(20, 100, 80, 120));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }

                super.paintComponent(g2);

                int underlineThickness = 2;
                int y = getHeight() - 6;
                int fullW = getWidth() - 24;
                int w = Math.max(0, Math.round(fullW * progress));
                int x = (getWidth() - w) / 2;

                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(x, y, w, underlineThickness, underlineThickness, underlineThickness);

                g2.dispose();
            }
        }

        java.util.function.Function<String, JToggleButton> createNavButton = (text) -> {
            JToggleButton btn = new AnimatedUnderlineButton(text);
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
                libraryBookSearchModule.refreshCards();
                cardLayout.show(contentPanel, "search");
                titleLabel.setText("图书检索");
            });
            btnHistory.addActionListener(e -> {
                libraryBorrowHistoryModule.refreshCards();
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

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }
}
