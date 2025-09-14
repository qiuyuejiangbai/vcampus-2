package client.ui.modules;

import client.controller.StoreController;
import client.ui.modules.store.*;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class StoreMainFrameModule extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private final UserVO currentUser;
    private final StoreController controller;

    // 各个子模块
    private StoreProductSearchModule storeProductSearchModule;
    private StoreShoppingCartModule storeShoppingCartModule;
    private StoreOrderHistoryModule storeOrderHistoryModule;
    private StoreProductManageModule storeProductManageModule;
    private StoreOrderManageModule storeOrderManageModule;
    private StoreProductEditDialogModule storeProductEditDialogModule;
    private StoreProductAddModule storeProductAddModule;
    private CheckoutPanel checkoutPanel;

    public StoreMainFrameModule(UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = new StoreController(currentUser.getUserId());
        initUI(currentUser);
    }

    @Override
    public void dispose() {
        if (controller != null) {
            controller.close();
        }
        super.dispose();
    }

    public StoreController getController() {
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
        JLabel titleLabel = new JLabel("商店");
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
        Function<String, JToggleButton> createNavButton = (text) -> {
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
            btn.setBorder(new EmptyBorder(8, 18, 8, 18));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            navGroup.add(btn);
            navPanel.add(btn);
            return btn;
        };

        // === 学生 / 普通用户 ===
       // ---------- 右侧：导航按钮（商品搜索、购物车、订单历史） ----------
 if (user.getRole() == 0 || user.getRole() == 1) {
            storeProductSearchModule = new StoreProductSearchModule(controller, currentUser);
            storeShoppingCartModule = new StoreShoppingCartModule(controller, currentUser);
            storeOrderHistoryModule = new StoreOrderHistoryModule(controller, currentUser);

            contentPanel.add(storeProductSearchModule, "search");
            contentPanel.add(storeShoppingCartModule, "cart");
            contentPanel.add(storeOrderHistoryModule, "orders");

            JToggleButton btnSearch = createNavButton.apply("商品搜索");
            JToggleButton btnCart = createNavButton.apply("购物车");
            JToggleButton btnOrders = createNavButton.apply("订单历史");

           // 余额管理面板
           /*  JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            balancePanel.setOpaque(false);
            balancePanel.add(new JLabel("用户余额:"));
            balancePanel.setForeground(Color.WHITE);

            double balance = controller.getUserBalance();
            JLabel balanceValueLabel = new JLabel(String.format("%.2f", balance));
            balancePanel.add(balanceValueLabel);

            JButton rechargeBtn = new JButton("充值");
            //rechargeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            rechargeBtn.addActionListener(e -> {
                try {
                    String input = JOptionPane.showInputDialog(this, "请输入充值金额:");
                    if (input == null) return;
                    double amount = Double.parseDouble(input);
                    if (amount > 0 && controller.rechargeBalance(amount)) {
                        JOptionPane.showMessageDialog(this, "充值成功!");
                        balanceValueLabel.setText(String.format("%.2f", controller.getUserBalance()));
                        balancePanel.revalidate();
                        balancePanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "充值失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "请输入有效金额", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            balancePanel.add(rechargeBtn);

            topBar.add(balancePanel, BorderLayout.SOUTH);*/

            btnSearch.addActionListener(e -> {
                storeProductSearchModule.refreshTable();
                cardLayout.show(contentPanel, "search");
                titleLabel.setText("商品搜索");
            });

            btnCart.addActionListener(e -> {
                storeShoppingCartModule.refreshTable();
                cardLayout.show(contentPanel, "cart");
                titleLabel.setText("购物车");
            });

            btnOrders.addActionListener(e -> {
                storeOrderHistoryModule.refreshTable();
                cardLayout.show(contentPanel, "orders");
                titleLabel.setText("订单历史");
            });

            btnSearch.setSelected(true);
            cardLayout.show(contentPanel, "search");
        }

        // === 管理员 ===
        if (user.getRole() == 2) {
            storeProductManageModule = new StoreProductManageModule(controller, currentUser);
            storeOrderManageModule = new StoreOrderManageModule(controller, currentUser);

            contentPanel.add(storeProductManageModule, "manage");
            contentPanel.add(storeOrderManageModule, "orders");

            JToggleButton btnManage = createNavButton.apply("商品管理");
            JToggleButton btnOrders = createNavButton.apply("订单管理");

            btnManage.addActionListener(e -> {
                storeProductManageModule.refreshTable();
                cardLayout.show(contentPanel, "manage");
                titleLabel.setText("商品管理");
            });

            btnOrders.addActionListener(e -> {
                storeOrderManageModule.refreshTable();
                cardLayout.show(contentPanel, "orders");
                titleLabel.setText("订单管理");
            });

            btnManage.setSelected(true);
            cardLayout.show(contentPanel, "manage");
        }

        // 总布局
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

}}