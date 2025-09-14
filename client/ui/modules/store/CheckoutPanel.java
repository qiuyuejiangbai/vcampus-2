package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.ShoppingCartItemVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

public class CheckoutPanel extends JDialog {
    private final StoreController controller;
    private final UserVO currentUser;
    private final List<ShoppingCartItemVO> items;
    private double totalAmount;
    private boolean confirmed = false;
    
    private JComboBox<String> paymentMethod;
    private JTextArea addressArea;
    private JCheckBox termsCheck;

    public CheckoutPanel(StoreController controller, UserVO user, List<ShoppingCartItemVO> items) {
        this.controller = controller;
        this.currentUser = user;
        this.items = items;
        this.totalAmount = calculateTotal();
        
        setTitle("订单结算");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 订单商品区域
        JPanel orderPanel = createOrderPanel();
        
        // 用户信息区域
        JPanel userPanel = createUserPanel();
        
        // 结算区域
        JPanel checkoutPanel = createCheckoutPanel();
        
        // 按钮区域
        JPanel buttonPanel = createButtonPanel();
        
        // 组装面板
        mainPanel.add(orderPanel, BorderLayout.CENTER);
        mainPanel.add(userPanel, BorderLayout.NORTH);
        mainPanel.add(checkoutPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(mainPanel);
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("订单商品"));
        
        // 创建商品表格
        JTable table = createOrderTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JTable createOrderTable() {
        // 使用匿名表格模型
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new Object[]{"商品名称", "单价", "数量", "小计"}
        );
        
        for (ShoppingCartItemVO item : items) {
            model.addRow(new Object[]{
                item.getProductName(),
                String.format("¥%.2f", item.getPrice()),
                item.getQuantity(),
                String.format("¥%.2f", item.getPrice() * item.getQuantity())
            });
        }
        
        // 添加总计行
        model.addRow(new Object[]{
            "总计", "", "", String.format("¥%.2f", totalAmount)
        });
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setEnabled(false); // 只读
        table.getTableHeader().setReorderingAllowed(false);
        
        return table;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("收货信息"));
        
        // 用户名
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("收货人:"));
        JLabel nameLabel = new JLabel(currentUser.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        namePanel.add(nameLabel);
        
        // 地址
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.add(new JLabel("收货地址:"), BorderLayout.NORTH);
        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        addressPanel.add(new JScrollPane(addressArea), BorderLayout.CENTER);
        
        panel.add(namePanel, BorderLayout.NORTH);
        panel.add(addressPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("支付信息"));
        
        // 支付方式
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.add(new JLabel("支付方式:"));
        paymentMethod = new JComboBox<>(new String[]{
            "支付宝", "微信支付", "银行卡支付", "货到付款"
        });
        paymentMethod.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        paymentPanel.add(paymentMethod);
        
        // 总金额
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel(String.format("订单金额: ¥%.2f", totalAmount));
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        totalPanel.add(totalLabel);
        
        // 同意条款
        JPanel termsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        termsCheck = new JCheckBox("我已阅读并同意《用户服务协议》");
        termsCheck.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        termsPanel.add(termsCheck);
        
        panel.add(paymentPanel, BorderLayout.NORTH);
        panel.add(totalPanel, BorderLayout.CENTER);
        panel.add(termsPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        JButton submitBtn = new JButton("提交订单");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitBtn.setBackground(new Color(76, 175, 80));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitOrder());
        
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());
        
        panel.add(submitBtn);
        panel.add(cancelBtn);
        
        return panel;
    }

    private double calculateTotal() {
        double total = 0;
        for (ShoppingCartItemVO item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private void submitOrder() {
        // 验证地址
        String address = addressArea.getText().trim();
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写收货地址！", "错误", JOptionPane.ERROR_MESSAGE);
            addressArea.requestFocus();
            return;
        }
        
        // 验证条款
        if (!termsCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "请同意用户服务协议！", "错误", JOptionPane.ERROR_MESSAGE);
            termsCheck.requestFocus();
            return;
        }
        
        // 提交订单
        try {
            // Adjust the arguments to match StoreController's createOrder(List<Integer>, List<Integer>)
            // Example: assuming you want to pass productIds and quantities from items
            java.util.List<Integer> productIds = new java.util.ArrayList<>();
            java.util.List<Integer> quantities = new java.util.ArrayList<>();
            for (ShoppingCartItemVO item : items) {
                productIds.add(item.getProductId());
                quantities.add(item.getQuantity());
            }
            common.vo.OrderVO order = controller.createOrder(productIds, quantities);
            
            if (order != null) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "订单提交失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "系统错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}