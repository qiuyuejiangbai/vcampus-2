package server.dao.impl;

import server.service.StoreService;
import common.vo.ProductVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import common.vo.ShoppingCartItemVO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商店服务实现类
 * 负责商店模块的数据库操作
 */
public class StoreServiceImpl implements StoreService {

    private static final String URL = "jdbc:mysql://localhost:3306/vcampus?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Eva-Huang041022";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ===== 商品管理 =====
    @Override
    public List<ProductVO> getAllProducts() {
        List<ProductVO> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<ProductVO> searchProducts(String keyword) {
        List<ProductVO> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public ProductVO getProductById(Integer productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean addProduct(ProductVO product) {
        String sql = "INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProduct(ProductVO product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setInt(5, product.getProductId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProduct(Integer productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean adjustStock(Integer productId, int quantityDelta) {
        String sql = "UPDATE products SET stock = stock + ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quantityDelta);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== 购物车管理 =====
    @Override
    public List<ShoppingCartItemVO> getShoppingCart(Integer userId) {
        List<ShoppingCartItemVO> cartItems = new ArrayList<>();
        String sql = "SELECT sc.product_id, p.name, p.price, sc.quantity, p.image_url " +
                     "FROM shopping_cart sc " +
                     "JOIN products p ON sc.product_id = p.product_id " +
                     "WHERE sc.user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShoppingCartItemVO item = new ShoppingCartItemVO();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("name"));
                    item.setPrice(rs.getDouble("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setSubtotal(item.getPrice() * item.getQuantity());
                    
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    @Override
    public boolean addToCart(Integer userId, Integer productId, int quantity) {
        // First check if product exists
        ProductVO product = getProductById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        
        // Check if item already exists in cart
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Item exists, update quantity
                    String updateSql = "UPDATE shopping_cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, quantity);
                        updatePs.setInt(2, userId);
                        updatePs.setInt(3, productId);
                        
                        return updatePs.executeUpdate() > 0;
                    }
                } else {
                    // Item doesn't exist, insert new
                    String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, productId);
                        insertPs.setInt(3, quantity);
                        
                        return insertPs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeFromCart(Integer userId, Integer productId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCartItem(Integer userId, Integer productId, int newQuantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, newQuantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean clearCart(Integer userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== 订单管理 =====
    // 管理员视图 - 获取所有用户消费记录
    @Override
    public List<OrderVO> getAllUserOrders() {
        List<OrderVO> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.user_id, u.name AS user_name, o.order_date, o.total_amount, o.status " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "ORDER BY o.order_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                OrderVO order = new OrderVO();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setUserName(rs.getString("user_name"));
                order.setCreatedTime(rs.getTimestamp("order_date"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getInt("status"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public OrderVO getAdminOrderDetail(Integer orderId) {
        OrderVO order = null;
        String sql = "SELECT o.order_id, o.user_id, u.name AS user_name, u.email, u.phone, " +
                     "o.order_date, o.total_amount, o.status, o.shipping_address " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.order_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = new OrderVO();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setUserName(rs.getString("user_name"));
                    order.setCreatedTime(rs.getTimestamp("order_date"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getInt("status"));

                    
                    // Get order items
                    List<OrderItemVO> items = getOrderItemsForOrder(orderId);
                    order.setItems(items);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    // 消费者订单历史
    @Override
    public List<OrderItemVO> getUserOrderHistory(Integer userId) {
        List<OrderItemVO> orderItems = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, p.name AS product_name, oi.quantity, oi.price, o.order_date, o.status " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.order_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemVO item = new OrderItemVO();
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("price"));
                    item.setCreatedTime(rs.getTimestamp("order_date"));
                    item.setStatus(rs.getInt("status"));
                    orderItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    // 消费者查看订单
    @Override
    public List<OrderItemVO> getOrderItems(Integer orderId, Integer userId) {
        List<OrderItemVO> orderItems = new ArrayList<>();
        String sql = "SELECT oi.product_id, p.name AS product_name, oi.quantity, oi.price, p.image_url " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ? AND oi.order_id IN (SELECT order_id FROM orders WHERE user_id = ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemVO item = new OrderItemVO();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("price"));
                    //item.setImageUrl(rs.getString("image_url"));
                    orderItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    @Override
    public OrderVO createOrder(Integer userId, List<Integer> productIds, List<Integer> quantities) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Calculate total
            double totalAmount = 0.0;
            for (int i = 0; i < productIds.size(); i++) {
                Integer productId = productIds.get(i);
                Integer quantity = quantities.get(i);
                
                ProductVO product = getProductById(productId);
                if (product == null) {
                    conn.rollback();
                    return null;
                }
                
                totalAmount += product.getPrice() * quantity;
            }
            
            // Create order
            String orderSql = "INSERT INTO orders (user_id, total_amount, status, order_date, shipping_address) " +
                              "VALUES (?, ?, 'pending', NOW(), (SELECT address FROM users WHERE user_id = ?))";
            
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setDouble(2, totalAmount);
                ps.setInt(3, userId);
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        
                        // Create order items
                        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
                            for (int i = 0; i < productIds.size(); i++) {
                                Integer productId = productIds.get(i);
                                Integer quantity = quantities.get(i);
                                
                                ProductVO product = getProductById(productId);
                                
                                itemPs.setInt(1, orderId);
                                itemPs.setInt(2, productId);
                                itemPs.setInt(3, quantity);
                                itemPs.setDouble(4, product.getPrice());
                                itemPs.addBatch();
                            }
                            itemPs.executeBatch();
                        }
                        
                        // Update stock
                        String stockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
                        try (PreparedStatement stockPs = conn.prepareStatement(stockSql)) {
                            for (int i = 0; i < productIds.size(); i++) {
                                Integer productId = productIds.get(i);
                                Integer quantity = quantities.get(i);
                                
                                stockPs.setInt(1, quantity);
                                stockPs.setInt(2, productId);
                                stockPs.addBatch();
                            }
                            stockPs.executeBatch();
                        }
                        
                        conn.commit();
                        
                        // Return created order
                        OrderVO order = new OrderVO();
                        order.setOrderId(orderId);
                        order.setUserId(userId);
                        order.setTotalAmount(totalAmount);
                       // order.setStatus("pending");
                        
                        // Get order items
                        List<OrderItemVO> items = getOrderItemsForOrder(orderId);
                        order.setItems(items);
                        
                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean cancelOrder(Integer orderId, Integer userId) {
        String sql = "UPDATE orders SET status = 'cancelled' WHERE order_id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                // Restore stock
                String stockSql = "UPDATE products p " +
                                  "JOIN order_items oi ON p.product_id = oi.product_id " +
                                  "SET p.stock = p.stock + oi.quantity " +
                                  "WHERE oi.order_id = ?";
                
                try (PreparedStatement stockPs = conn.prepareStatement(stockSql)) {
                    stockPs.setInt(1, orderId);
                    stockPs.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean payOrder(Integer orderId) {
        String sql = "UPDATE orders SET status = 'paid' WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean shipOrder(Integer orderId) {
        String sql = "UPDATE orders SET status = 'shipped', ship_date = NOW() WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== 余额管理 =====
    @Override
    public double getUserBalance(Integer userId) {
        String sql = "SELECT balance FROM user_balance WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public boolean rechargeBalance(Integer userId, double amount) {
        String sql = "UPDATE user_balance SET balance = balance + ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean payWithBalance(Integer orderId, Integer userId) {
        // Get order total
        String getAmountSql = "SELECT total_amount FROM orders WHERE order_id = ?";
        double amount = 0.0;
        
        try (Connection conn = getConnection();
             PreparedStatement amountPs = conn.prepareStatement(getAmountSql)) {
            
            amountPs.setInt(1, orderId);
            try (ResultSet rs = amountPs.executeQuery()) {
                if (rs.next()) {
                    amount = rs.getDouble("total_amount");
                } else {
                    return false; // Order not found
                }
            }
            
            // Check balance
            double balance = getUserBalance(userId);
            if (balance < amount) {
                return false;
            }
            
            // Deduct balance
            String deductSql = "UPDATE user_balance SET balance = balance - ? WHERE user_id = ?";
            try (PreparedStatement deductPs = conn.prepareStatement(deductSql)) {
                deductPs.setDouble(1, amount);
                deductPs.setInt(2, userId);
                deductPs.executeUpdate();
                
                // Update order status
                payOrder(orderId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== 辅助方法 =====
    private ProductVO extractProduct(ResultSet rs) throws SQLException {
        ProductVO product = new ProductVO();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
       // product.setImageUrl(rs.getString("image_url"));
        return product;
    }

    private List<OrderItemVO> getOrderItemsForOrder(Integer orderId) {
        List<OrderItemVO> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name AS product_name, p.image_url " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemVO item = new OrderItemVO();
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("price"));
                   // item.setImageUrl(rs.getString("image_url"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}