package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

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
        setSize(1200, 800);
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
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(45, 45, 45), 0, getHeight(), new Color(25, 25, 25));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(15, 20, 0, 20));
        JLabel titleLabel = new JLabel("商店管理系统", JLabel.LEFT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 用户信息
        JLabel userLabel = new JLabel("欢迎, " + user.getUsername() + " (ID:" + user.getUserId() + ")");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titlePanel.add(userLabel, BorderLayout.EAST);

        // 导航区域
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navPanel.setOpaque(false);
        
        ButtonGroup navGroup = new ButtonGroup();
        JToggleButton selectedBtn = null;
        
        // 创建导航按钮的工具方法
        java.util.function.Function<String, JToggleButton> createNavButton = text -> {
            JToggleButton btn = new JToggleButton(text);
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

        topBar.setLayout(new BorderLayout());
        topBar.add(titlePanel, BorderLayout.NORTH);
        topBar.add(navPanel, BorderLayout.CENTER);

        // === 学生 / 普通用户 ===
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

            // 平衡管理面板
            JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            balancePanel.setOpaque(false);
            balancePanel.add(new JLabel("用户余额:"));
            
            double balance = controller.getUserBalance();
            balancePanel.add(new JLabel(String.format("%.2f", balance)));
            
            JButton rechargeBtn = new JButton("充值");
            rechargeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            rechargeBtn.addActionListener(e -> {
                try {
                    double amount = Double.parseDouble(JOptionPane.showInputDialog(this, "请输入充值金额:"));
                    if (amount > 0 && controller.rechargeBalance(amount)) {
                        JOptionPane.showMessageDialog(this, "充值成功!");
                        balancePanel.remove(1);
                        balancePanel.add(new JLabel(String.format("%.2f", controller.getUserBalance())));
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
            
            topBar.add(balancePanel, BorderLayout.SOUTH);

            btnSearch.addActionListener(e -> {
                storeProductSearchModule.refreshTable();
                cardLayout.show(contentPanel, "search");
                titleLabel.setText("商品搜索");
                btnSearch.setSelected(true);
            });

            btnCart.addActionListener(e -> {
                storeShoppingCartModule.refreshTable();
                cardLayout.show(contentPanel, "cart");
                titleLabel.setText("购物车");
                btnCart.setSelected(true);
            });

            btnOrders.addActionListener(e -> {
                storeOrderHistoryModule.refreshTable();
                cardLayout.show(contentPanel, "orders");
                titleLabel.setText("订单历史");
                btnOrders.setSelected(true);
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
                btnManage.setSelected(true);
            });

            btnOrders.addActionListener(e -> {
                storeOrderManageModule.refreshTable();
                cardLayout.show(contentPanel, "orders");
                titleLabel.setText("订单管理");
                btnOrders.setSelected(true);
            });

            btnManage.setSelected(true);
            cardLayout.show(contentPanel, "manage");
        }

        // 总布局
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // 加载初始数据
        SwingUtilities.invokeLater(() -> {
            if (user.getRole() == 0 || user.getRole() == 1) {
                storeProductSearchModule.refreshTable();
            } else {
                storeProductManageModule.refreshTable();
            }
        });
    }
}