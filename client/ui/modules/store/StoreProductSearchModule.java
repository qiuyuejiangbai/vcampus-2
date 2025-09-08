package client.ui.modules.store;

import client.controller.StoreController;
import common.vo.ProductVO;
import common.vo.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

public class StoreProductSearchModule extends JPanel {
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JButton searchButton;
    private JTable table;
    private JButton addToCartButton;
    private JButton viewDetailsButton;

    private final StoreController controller;
    private final UserVO currentUser;

    public StoreProductSearchModule(StoreController controller, UserVO currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部搜索栏
        JPanel searchPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        nameField = new JTextField();
        categoryField = new JTextField();
        minPriceField = new JTextField();
        maxPriceField = new JTextField();
        
        searchPanel.add(new JLabel("名称:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("分类:"));
        searchPanel.add(categoryField);
        searchPanel.add(new JLabel("最低价格:"));
        searchPanel.add(minPriceField);
        searchPanel.add(new JLabel("最高价格:"));
        searchPanel.add(maxPriceField);

        searchButton = new JButton("搜索");
        searchPanel.add(new JLabel()); // Empty cell for layout
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // 中间表格
        String[] columnNames = {"ID", "名称", "描述", "价格", "库存", "评分", "状态"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // Price column
                    return BigDecimal.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // 名称
        table.getColumnModel().getColumn(2).setPreferredWidth(300); // 描述
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // 价格
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // 库存
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // 评分
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // 状态
        
        // 设置字体和行高
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        
        // 表头样式
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToCartButton = new JButton("加入购物车");
        viewDetailsButton = new JButton("查看详情");
        bottomPanel.add(viewDetailsButton);
        bottomPanel.add(addToCartButton);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(bottomPanel);
        buttonPanel.add(Box.createVerticalStrut(10));
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件绑定
        bindEvents();
    }

    private void bindEvents() {
        // 搜索按钮
        searchButton.addActionListener(e -> performSearch());
        
        // 加入购物车按钮
        addToCartButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int productId = (int) table.getValueAt(selectedRow, 0);
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
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个商品！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int productId = (int) table.getValueAt(selectedRow, 0);
            ProductVO product = controller.getProductById(productId);
            
            if (product != null) {
                showProductDetails(product);
            }
        });
        
        // 回车键触发搜索
        nameField.addActionListener(e -> performSearch());
        categoryField.addActionListener(e -> performSearch());
        minPriceField.addActionListener(e -> performSearch());
        maxPriceField.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        try {
            // 构建搜索条件
            String nameKeyword = nameField.getText().trim();
            String categoryKeyword = categoryField.getText().trim();
            
            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            
            if (!minPriceField.getText().trim().isEmpty()) {
                minPrice = new BigDecimal(minPriceField.getText().trim());
            }
            if (!maxPriceField.getText().trim().isEmpty()) {
                maxPrice = new BigDecimal(maxPriceField.getText().trim());
            }
            
            // 执行搜索
            List<ProductVO> products = controller.searchProducts(nameKeyword, categoryKeyword, minPrice, maxPrice);
            
            // 更新表格
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            
            for (ProductVO p : products) {
                String status = p.getStock() > 0 ? "有货" : "缺货";
                model.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getStock(),
                    p.getRating(),
                    status
                });
            }
            
            // 显示搜索结果数量
            table.setToolTipText("找到 " + products.size() + " 个商品");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "价格输入格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "搜索失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProductDetails(ProductVO product) {
        // 创建商品详情对话框
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                   "商品详情", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        // 详情面板
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 商品信息
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("商品信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 显示商品属性
        addRowToGridBag(infoPanel, gbc, 0, "商品ID:", product.getProductId() + "");
        addRowToGridBag(infoPanel, gbc, 1, "商品名称:", product.getName());
        addRowToGridBag(infoPanel, gbc, 2, "描述:", product.getDescription());
        addRowToGridBag(infoPanel, gbc, 3, "价格:", "¥" + product.getPrice().toString());
        addRowToGridBag(infoPanel, gbc, 4, "库存数量:", product.getStock() + " 件");
        addRowToGridBag(infoPanel, gbc, 5, "用户评分:", product.getRating() + " ★");
        
        detailPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 按钮面板
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
    
    private void addRowToGridBag(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(valueLabel, gbc);
    }

    public void refreshTable() {
        nameField.setText("");
        categoryField.setText("");
        minPriceField.setText("");
        maxPriceField.setText("");
        performSearch();
    }
}