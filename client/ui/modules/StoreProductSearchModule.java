/*package client.ui.modules;

import client.controller.StoreController;
import common.vo.ProductVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class StoreProductSearchModule extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JTable table;
    private JButton addToCartButton;
    private JButton viewDetailsButton;
    private JLabel statLabel;

    private JCheckBox[] categoryChecks;
   
    private final StoreController controller;
    private final UserVO currentUser;

    public StoreProductSearchModule(StoreController controller, UserVO currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- 顶部容器 ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // 搜索栏（三段式布局）
        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));

        JLabel titleLabel = new JLabel("🛒 商品搜索");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchField = new JTextField("请输入关键词（商品名/分类/描述）", 25);
        searchField.setForeground(Color.GRAY);

        // 提示文字效果
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("请输入关键词（商品名/分类/描述）")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入关键词（商品名/分类/描述）");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchButton = new JButton("搜索");
        clearButton = new JButton("清空筛选");
        centerPanel.add(searchField);
        centerPanel.add(searchButton);
        centerPanel.add(clearButton);

        statLabel = new JLabel("商品总数: 0 个");
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statLabel.setHorizontalAlignment(SwingConstants.LEFT);

        searchPanel.add(titleLabel, BorderLayout.WEST);
        searchPanel.add(centerPanel, BorderLayout.CENTER);
        searchPanel.add(statLabel, BorderLayout.EAST);

        topContainer.add(searchPanel);

        // 分类复选框（多行网格布局，一行 10 个，居中）
        String[] categories = {
                "文具用品", "电子产品", "饮品食品"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryPanel.add(categoryChecks[i]);

            // 勾选时立即刷新
            categoryChecks[i].addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    doSearch();
                }
            });
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // 中间表格
        String[] columnNames = {"ID", "名称", "描述", "价格", "库存", "状态"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) ;
        table = new JTable(model);

        // 表格美化
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setRowHeight(28);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewDetailsButton = new JButton("查看详情");
        addToCartButton = new JButton("加入购物车");
        bottomPanel.add(viewDetailsButton);
        bottomPanel.add(addToCartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
        bindEvents();
    }

    private void bindEvents() {
        // 搜索按钮
        searchButton.addActionListener(e -> doSearch());

        // 清空按钮
        clearButton.addActionListener(e -> refreshTable());

        // 加入购物车按钮
        addToCartButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！");
                return;
            }
            int productId = (int) table.getValueAt(row, 0);
            String quantityStr = JOptionPane.showInputDialog(this, "请输入数量:", "加入购物车", JOptionPane.PLAIN_MESSAGE);
            if (quantityStr == null) return;
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "数量必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ProductVO product = controller.getProductById(productId);
                if (product == null) {
                    JOptionPane.showMessageDialog(this, "商品信息错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (quantity > product.getStock()) {
                    JOptionPane.showMessageDialog(this,
                            "库存不足！当前库存：" + product.getStock(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean success = controller.addToCart(productId, quantity);
                JOptionPane.showMessageDialog(this,
                        success ? "成功加入购物车！" : "加入购物车失败！",
                        success ? "成功" : "失败",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数量！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 查看详情按钮
        viewDetailsButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！");
                return;
            }
            int productId = (int) table.getValueAt(row, 0);
            ProductVO product = controller.getProductById(productId);
            if (product != null) {
                JOptionPane.showMessageDialog(this,
                        "商品名称: " + product.getProductName() + "\n" +
                                "商品描述: " + product.getDescription() + "\n" +
                                "分类: " + product.getCategory() + "\n" +
                                "价格: " + product.getPrice() + "\n" +
                                "库存: " + product.getStock() + "\n" 
                );
            }
        });

        // 回车键触发搜索
        searchField.addActionListener(e -> doSearch());
    }

    /** 统一的搜索方法（关键词模糊匹配） */
   /*  private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.equals("请输入关键词（商品名/分类/描述）")) {
            keyword = "";
        }

        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) {
            if (cb.isSelected()) {
                selectedCategories.add(cb.getText());
            }
        }

        List<ProductVO> products = controller.searchProducts(keyword);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (ProductVO p : products) {
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (p.getCategory() != null && p.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }

              // 关键词额外模糊匹配分类
            boolean keywordMatch = keyword.isEmpty()
                    || (p.getCategory() != null && p.getCategory().contains(keyword));

            if (categoryMatch && keywordMatch) {
                String status = p.getStock() > 0 ? "有货" : "缺货";
                model.addRow(new Object[]{
                        p.getProductId(),
                    p.getProductName(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getStock(),
                    status
                });
            }
        }

        // 更新统计信息
        statLabel.setText("商品总数: " + products.size() + " 个");

        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            balancePanel.setOpaque(false);
            balancePanel.add(new JLabel("用户余额:"));
            balancePanel.setForeground(Color.WHITE);

            double balance = controller.getUserBalance();
            JLabel balanceValueLabel = new JLabel(String.format("%.2f", balance));
            balanceValueLabel.setForeground(Color.BLUE);
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
    }

    private void showProductDetails(ProductVO product) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "商品详情", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("商品信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRowToGridBag(infoPanel, gbc, 0, "商品ID:", product.getProductId() + "");
        addRowToGridBag(infoPanel, gbc, 1, "商品名称:", product.getProductName());
        addRowToGridBag(infoPanel, gbc, 2, "描述:", product.getDescription());
        addRowToGridBag(infoPanel, gbc, 3, "价格:", "¥" + product.getPrice().toString());
        addRowToGridBag(infoPanel, gbc, 4, "库存数量:", product.getStock() + " 件");

        detailPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addToCartBtn = new JButton("加入购物车");
        JButton closeBtn = new JButton("关闭");

        addToCartBtn.addActionListener(e -> {
            try {
                String quantityStr = JOptionPane.showInputDialog(dialog, "请输入数量:", "加入购物车", JOptionPane.PLAIN_MESSAGE);
                if (quantityStr == null) return;
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "数量必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (quantity > product.getStock()) {
                    JOptionPane.showMessageDialog(dialog,
                            "库存不足！当前库存：" + product.getStock(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean success = controller.addToCart(product.getProductId(), quantity);
                JOptionPane.showMessageDialog(dialog,
                        success ? "成功加入购物车！" : "加入购物车失败！",
                        success ? "成功" : "失败",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) {
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数量！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addToCartBtn);
        buttonPanel.add(closeBtn);
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(detailPanel);
        dialog.setVisible(true);
    }
    private void addRowToGridBag(JPanel panel, GridBagConstraints gbc,
                                 int row, String label, String value) {
        gbc.gridy = row;

        gbc.gridx = 0;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(valueLabel, gbc);
    }

    public void refreshTable() {
        searchField.setText("请输入关键词（商品名/分类/描述）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch();
    }
}*/
package client.ui.modules;

import client.controller.StoreController;
import common.vo.ProductVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class StoreProductSearchModule extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JTable table;
    private JButton addToCartButton;
    private JButton viewDetailsButton;
    private JLabel statLabel;

    private JCheckBox[] categoryChecks;
   
    private final StoreController controller;
    private final UserVO currentUser;

    public StoreProductSearchModule(StoreController controller, UserVO currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- 顶部容器 ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // 搜索栏（三段式布局）
        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));

        JLabel titleLabel = new JLabel("🛒 商品搜索");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchField = new JTextField("请输入关键词（商品名/分类/描述）", 25);
        searchField.setForeground(Color.GRAY);

        // 提示文字效果
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("请输入关键词（商品名/分类/描述）")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("请输入关键词（商品名/分类/描述）");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchButton = new JButton("搜索");
        clearButton = new JButton("清空筛选");
        centerPanel.add(searchField);
        centerPanel.add(searchButton);
        centerPanel.add(clearButton);

        statLabel = new JLabel("商品总数: 0 个");
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statLabel.setHorizontalAlignment(SwingConstants.LEFT);

        searchPanel.add(titleLabel, BorderLayout.WEST);
        searchPanel.add(centerPanel, BorderLayout.CENTER);
        searchPanel.add(statLabel, BorderLayout.EAST);

        topContainer.add(searchPanel);

        // 分类复选框（多行网格布局，一行 10 个，居中）
        String[] categories = {
                "文具用品", "电子产品", "饮品食品"
        };
        JPanel categoryPanel = new JPanel(new GridLayout(0, 10, 8, 5));
        categoryChecks = new JCheckBox[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryChecks[i] = new JCheckBox(categories[i]);
            categoryPanel.add(categoryChecks[i]);

            // 勾选时立即刷新
            categoryChecks[i].addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    doSearch();
                }
            });
        }
        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        categoryWrapper.add(categoryPanel);
        topContainer.add(categoryWrapper);

        add(topContainer, BorderLayout.NORTH);

        // 中间表格
        String[] columnNames = {"ID", "名称", "描述", "价格", "库存", "状态"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) ;
        table = new JTable(model);

        // 表格美化
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setRowHeight(28);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewDetailsButton = new JButton("查看详情");
        addToCartButton = new JButton("加入购物车");
        bottomPanel.add(viewDetailsButton);
        bottomPanel.add(addToCartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
        bindEvents();
    }

    private void bindEvents() {
        // 搜索按钮
        searchButton.addActionListener(e -> doSearch());

        // 清空按钮
        clearButton.addActionListener(e -> refreshTable());

        // 加入购物车按钮
        addToCartButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！");
                return;
            }
            int productId = (int) table.getValueAt(row, 0);
            String quantityStr = JOptionPane.showInputDialog(this, "请输入数量:", "加入购物车", JOptionPane.PLAIN_MESSAGE);
            if (quantityStr == null) return;
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "数量必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ProductVO product = controller.getProductById(productId);
                if (product == null) {
                    JOptionPane.showMessageDialog(this, "商品信息错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (quantity > product.getStock()) {
                    JOptionPane.showMessageDialog(this,
                            "库存不足！当前库存：" + product.getStock(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean success = controller.addToCart(productId, quantity);
                JOptionPane.showMessageDialog(this,
                        success ? "成功加入购物车！" : "加入购物车失败！",
                        success ? "成功" : "失败",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数量！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 查看详情按钮
        viewDetailsButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！");
                return;
            }
            int productId = (int) table.getValueAt(row, 0);
            ProductVO product = controller.getProductById(productId);
            if (product != null) {
                JOptionPane.showMessageDialog(this,
                        "商品名称: " + product.getProductName() + "\n" +
                                "商品描述: " + product.getDescription() + "\n" +
                                "分类: " + product.getCategory() + "\n" +
                                "价格: " + product.getPrice() + "\n" +
                                "库存: " + product.getStock() + "\n" 
                );
            }
        });

        // 回车键触发搜索
        searchField.addActionListener(e -> doSearch());
    }

    /** 统一的搜索方法（关键词模糊匹配） */
    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.equals("请输入关键词（商品名/分类/描述）")) {
            keyword = "";
        }

        Set<String> selectedCategories = new HashSet<>();
        for (JCheckBox cb : categoryChecks) {
            if (cb.isSelected()) {
                selectedCategories.add(cb.getText());
            }
        }

        List<ProductVO> products = controller.searchProducts(keyword);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (ProductVO p : products) {
            boolean categoryMatch = selectedCategories.isEmpty();
            for (String cat : selectedCategories) {
                if (p.getCategory() != null && p.getCategory().contains(cat)) {
                    categoryMatch = true;
                    break;
                }
            }

              // 关键词额外模糊匹配分类
            boolean keywordMatch = keyword.isEmpty()
                    || (p.getCategory() != null && p.getCategory().contains(keyword));

            if (categoryMatch && keywordMatch) {
                String status = p.getStock() > 0 ? "有货" : "缺货";
                model.addRow(new Object[]{
                        p.getProductId(),
                    p.getProductName(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getStock(),
                    status
                });
            }
        }

        // 更新统计信息
        statLabel.setText("商品总数: " + products.size() + " 个");

        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            balancePanel.setOpaque(false);
            balancePanel.add(new JLabel("用户余额:"));
            balancePanel.setForeground(Color.WHITE);

            double balance = controller.getUserBalance();
            JLabel balanceValueLabel = new JLabel(String.format("%.2f", balance));
            balanceValueLabel.setForeground(Color.BLUE);
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
    }

    private void showProductDetails(ProductVO product) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "商品详情", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("商品信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRowToGridBag(infoPanel, gbc, 0, "商品ID:", product.getProductId() + "");
        addRowToGridBag(infoPanel, gbc, 1, "商品名称:", product.getProductName());
        addRowToGridBag(infoPanel, gbc, 2, "描述:", product.getDescription());
        addRowToGridBag(infoPanel, gbc, 3, "价格:", "¥" + product.getPrice().toString());
        addRowToGridBag(infoPanel, gbc, 4, "库存数量:", product.getStock() + " 件");

        detailPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addToCartBtn = new JButton("加入购物车");
        JButton closeBtn = new JButton("关闭");

        addToCartBtn.addActionListener(e -> {
            try {
                String quantityStr = JOptionPane.showInputDialog(dialog, "请输入数量:", "加入购物车", JOptionPane.PLAIN_MESSAGE);
                if (quantityStr == null) return;
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "数量必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (quantity > product.getStock()) {
                    JOptionPane.showMessageDialog(dialog,
                            "库存不足！当前库存：" + product.getStock(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean success = controller.addToCart(product.getProductId(), quantity);
                JOptionPane.showMessageDialog(dialog,
                        success ? "成功加入购物车！" : "加入购物车失败！",
                        success ? "成功" : "失败",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (success) {
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数量！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addToCartBtn);
        buttonPanel.add(closeBtn);
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(detailPanel);
        dialog.setVisible(true);
    }
    private void addRowToGridBag(JPanel panel, GridBagConstraints gbc,
                                 int row, String label, String value) {
        gbc.gridy = row;

        gbc.gridx = 0;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        panel.add(valueLabel, gbc);
    }

    public void refreshTable() {
        searchField.setText("请输入关键词（商品名/分类/描述）");
        searchField.setForeground(Color.GRAY);
        for (JCheckBox cb : categoryChecks) cb.setSelected(false);
        doSearch();
    }
}
