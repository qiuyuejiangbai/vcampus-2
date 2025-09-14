package server.dao.impl;

import server.service.StoreService;
import common.vo.ProductVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import common.vo.ShoppingCartItemVO;
import server.util.DatabaseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 商店服务实现类
 * 负责商店模块的数据库操作
 */
public class StoreServiceImpl implements StoreService {

    private static String URL ;
    private static String USER ;
    private static String PASSWORD ;
    private static String DRIVER;

    static {
        Properties props = new Properties();
        try {
            // 从 resources/config.local.properties 加载
            String configPath = System.getProperty("user.dir") + File.separator
                    + "resources" + File.separator + "config.local.properties";
            try (InputStream in = new FileInputStream(configPath)) {
                props.load(in);
            }

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password");
            DRIVER = props.getProperty("db.driver");

            // 加载数据库驱动
            Class.forName(DRIVER);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("加载数据库配置失败", e);
        }
    }

    // ✅ 项目根目录下的 resources 文件夹
    private static final String BASE_PATH =
            System.getProperty("user.dir") + File.separator + "resources" + File.separator;

    private Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    // ===== 商品管理 =====
    @Override
    public List<ProductVO> searchProducts(String keyword) {
        List<ProductVO> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_name LIKE ? OR description LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductVO product = new ProductVO();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setCategory(rs.getString("category"));
                list.add(product);
        } }catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ProductVO getProductById(Integer productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
             ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProductVO product = new ProductVO();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setCategory(rs.getString("category"));
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean addProduct(ProductVO product) {
        String sqlWithId = "INSERT INTO products (product_id, product_name, description, price, stock, category) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlWithoutId = "INSERT INTO products ( product_name, description, price, stock, category) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            PreparedStatement ps;
            if (product.getProductId() != null) {
                ps = conn.prepareStatement(sqlWithId);
                ps.setInt(1, product.getProductId());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getDescription());
                ps.setDouble(4, product.getPrice());
                ps.setInt(5, product.getStock());
                ps.setString(6, product.getCategory());
            } else {
                ps = conn.prepareStatement(sqlWithoutId);
                ps.setString(1, product.getProductName());
                ps.setString(2, product.getDescription());
                ps.setDouble(3, product.getPrice());
                ps.setInt(4, product.getStock());
                ps.setString(5, product.getCategory());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProduct(ProductVO product) {
        String sql = "UPDATE products SET product_name = ?, description = ?, price = ?, stock = ?, category=? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getCategory());
            ps.setInt(6, product.getProductId());
            
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
            }
         catch (SQLException e) {
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
        String sql = "SELECT sc.product_id, p.product_name, p.price, sc.quantity, p.image_url " +
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
                    item.setProductName(rs.getString("product_name"));
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
    // 1. 基础参数校验：避免空值和非法数量
    if (userId == null || productId == null || quantity <= 0) {
        return false;
    }

    // 2. 查询商品信息（获取单价、库存，确保商品合法）
    ProductVO product = getProductById(productId);
    if (product == null) {
        return false; // 商品不存在
    }
    if (product.getStock() < quantity) {
        return false; // 库存不足
    }
    Double productPrice = product.getPrice();
    if (productPrice == null || productPrice <= 0) {
        return false; // 商品价格非法
    }
    // 计算小计：subtotal = 单价 * 数量（解决subtotal无默认值问题）
    double subtotal = productPrice * quantity;


    // 3. 关键：复用同一个Connection，确保事务控制有效（避免回滚错误）
    try (Connection conn = getConnection()) { // 外层try-with-resources管理连接，确保自动关闭
        conn.setAutoCommit(false); // 关闭自动提交，开启事务
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        // 3.1 检查购物车是否已有该商品
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            checkPs.setInt(2, productId);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    // 3.2 已有商品：更新数量和小计
                    int existingQuantity = rs.getInt("quantity"); // 获取原数量
                    int newTotalQuantity = existingQuantity + quantity; // 新总数量
                    double newSubtotal = productPrice * newTotalQuantity; // 新小计（单价*新总数量）

                    String updateSql = "UPDATE shopping_cart " +
                                       "SET quantity = ?, price = ?, subtotal = ? " + // 包含subtotal更新
                                       "WHERE user_id = ? AND product_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, newTotalQuantity);  // 1. 新总数量
                        updatePs.setDouble(2, productPrice);   // 2. 最新单价（可选：同步价格）
                        updatePs.setDouble(3, newSubtotal);    // 3. 新小计（解决subtotal问题）
                        updatePs.setInt(4, userId);            // 4. 用户ID
                        updatePs.setInt(5, productId);         // 5. 商品ID

                        int affectedRows = updatePs.executeUpdate();
                        conn.commit(); // 事务提交
                        return affectedRows > 0;
                    }
                } else {
                    // 3.3 无该商品：新增购物车记录（包含subtotal字段）
                    String insertSql = "INSERT INTO shopping_cart " +
                                       "(user_id, product_id, quantity, price, subtotal) " + // 显式包含subtotal
                                       "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);          // 1. 用户ID
                        insertPs.setInt(2, productId);       // 2. 商品ID
                        insertPs.setInt(3, quantity);        // 3. 数量
                        insertPs.setDouble(4, productPrice); // 4. 单价
                        insertPs.setDouble(5, subtotal);     // 5. 小计（解决subtotal无默认值问题）

                        int affectedRows = insertPs.executeUpdate();
                        conn.commit(); // 事务提交
                        return affectedRows > 0;
                    }
                }
            }
        }

    } catch (SQLException e) {
        // 4. 异常回滚：复用之前的Connection（外层try-with-resources已获取，确保autocommit=false）
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
                // 仅当连接未关闭且未自动提交时，才执行回滚
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
         // 先检查库存
        ProductVO product = getProductById(productId);
        if (product == null || product.getStock() < newQuantity) {
            return false;
        }
        
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
        String sql = "SELECT o.order_no, o.user_id, o.created_time, o.total_amount, o.status " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "ORDER BY o.created_time DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                OrderVO order = new OrderVO();
                order.setOrderOn(rs.getString("order_no"));
                order.setUserId(rs.getInt("user_id"));
                order.setCreatedTime(rs.getTimestamp("created_time"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status")=="paid"?1:0);
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
                    order.setOrderOn(rs.getString("order_on"));
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

    // 1. 未登录用户校验：userId为null时直接返回空列表（符合业务逻辑，避免无效查询）
    if (userId == null) {
        return orderItems;
    }

    // 2. 修正SQL：确保查询的单价字段名与代码读取的字段名一致
    // （若表中实际字段是unit_price，代码中就用unit_price；若想简化列名，可给字段加别名）
    String sql = "SELECT " +
                 "oi.order_id, " +          // 订单ID
                 "oi.product_id, " +        // 商品ID
                 "p.product_name, " +       // 商品名称（无需重复AS，原字段名已清晰）
                 "oi.quantity, " +          // 购买数量
                 "oi.unit_price, " +        // 单价（表中实际字段，代码将用此名读取）
                 "o.created_time, " +       // 订单创建时间
                 "o.status " +              // 订单状态
                 "FROM order_items oi " +
                 "JOIN products p ON oi.product_id = p.product_id " + // 关联商品表拿名称
                 "JOIN orders o ON oi.order_id = o.order_id " +       // 关联订单表拿时间和状态
                 "WHERE o.user_id = ? " +                              // 筛选当前用户订单
                 "ORDER BY o.created_time DESC";                      // 按创建时间倒序

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId); // 设置用户ID参数（筛选当前用户的订单）

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderItemVO item = new OrderItemVO();
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                // 3. 关键修正：用SQL中查询的"unit_price"读取单价，与表字段名匹配
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setCreatedTime(rs.getTimestamp("created_time"));
                item.setStatus(rs.getString("status"));
                orderItems.add(item);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        // 可选：添加日志记录，便于排查问题（如用户ID、异常信息）
        // log.error("获取用户订单历史失败，用户ID：{}", userId, e);
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
                   conn.setAutoCommit(false); // 开启事务
        
        // 1. 创建订单主记录
        String orderSql = "INSERT INTO orders (user_id, total_amount, status, created_time) VALUES (?, ?, '待支付', NOW())";
        PreparedStatement orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
        orderPs.setInt(1, userId);
        
        // 计算总金额并检查库存
        double total = 0;
        for (int i = 0; i < productIds.size(); i++) {
            ProductVO product = getProductById(productIds.get(i));
            if (product == null || product.getStock() < quantities.get(i)) {
                conn.rollback();
                return null; // 库存不足
            }
            total += product.getPrice() * quantities.get(i);
        }
        orderPs.setDouble(2, total);
        orderPs.executeUpdate();
        
        // 获取生成的订单ID
        ResultSet rs = orderPs.getGeneratedKeys();
        rs.next();
        String orderOn = rs.getString(1);
        
        // 2. 创建订单项
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        PreparedStatement itemPs = conn.prepareStatement(itemSql);
        
        // 3. 扣减库存
        String stockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
        PreparedStatement stockPs = conn.prepareStatement(stockSql);
        
        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            int qty = quantities.get(i);
            ProductVO product = getProductById(productId);
            
            // 添加订单项
            itemPs.setString(1, orderOn);
            itemPs.setInt(2, productId);
            itemPs.setInt(3, qty);
            itemPs.setDouble(4, product.getPrice());
            itemPs.addBatch();
            
            // 扣减库存
            stockPs.setInt(1, qty);
            stockPs.setInt(2, productId);
            stockPs.addBatch();
        }
        
        itemPs.executeBatch();
        stockPs.executeBatch();
        
        // 4. 清空购物车
        String cartSql = "DELETE FROM shopping_cart WHERE user_id = ?";
        PreparedStatement cartPs = conn.prepareStatement(cartSql);
        cartPs.setInt(1, userId);
        cartPs.executeUpdate();
        
        conn.commit();
        
        // 返回创建的订单
        OrderVO order = new OrderVO();
        order.setOrderOn(orderOn);
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(1);
        return order;
        
    } catch (SQLException e) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
        e.printStackTrace();
        return null;
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
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

 /*    @Override
public boolean updateOrderStatus(Integer orderId, Integer newStatus) {
    String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, newStatus);
        ps.setInt(2, orderId);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}*/
    // ===== 余额管理 =====
public double getUserBalance(Integer userId) {
    int role = -1;
    // 1. 先查询用户角色
    String roleSql = "SELECT role FROM users WHERE user_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement rolePs = conn.prepareStatement(roleSql)) {
        
        rolePs.setInt(1, userId);
        try (ResultSet roleRs = rolePs.executeQuery()) {
            if (roleRs.next()) {
                role = roleRs.getInt("role");
            } else {
                return -1; // 用户不存在
            }
        }
        
        // 2. 根据角色查询对应表的 balance
        String balanceSql;
        if (role == 0) {
            balanceSql = "SELECT balance FROM students WHERE user_id = ?"; // 学生表
        } else if (role == 1) {
            balanceSql = "SELECT balance FROM teachers WHERE user_id = ?"; // 教师表
        } else {
            return -1; // 未知角色
        }
        
        try (PreparedStatement balancePs = conn.prepareStatement(balanceSql)) {
            balancePs.setInt(1, userId);
            try (ResultSet balanceRs = balancePs.executeQuery()) {
                if (balanceRs.next()) {
                    return balanceRs.getDouble("balance"); // 这里才会正确找到 balance 列
                } else {
                    return -1; // 该角色对应的表中无此用户
                }
            }
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
        return -1;
    }
}

@Override
public boolean rechargeBalance(Integer userId, double amount) {
    int role = -1;
    String selectSql = "SELECT role FROM users WHERE user_id = ?";
    
    // 1. 查询用户角色
    try (Connection conn = getConnection();
         PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
        
        selectPs.setInt(1, userId);
        try (ResultSet rs = selectPs.executeQuery()) {
            if (rs.next()) {
                role = rs.getInt("role");
            }
        }
        
        // 2. 根据角色确定更新语句
        String updateSql;
        if (role == 0) {
            updateSql = "UPDATE students SET balance = balance + ? WHERE user_id = ?";
        } else if (role == 1) {
            updateSql = "UPDATE teachers SET balance = balance + ? WHERE user_id = ?";
        } else {
            return false; // 未知角色，更新失败
        }
        
        // 3. 创建新的PreparedStatement执行更新（关键修正：使用新的PS对象）
        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            updatePs.setDouble(1, amount); // 第一个占位符：充值金额
            updatePs.setInt(2, userId);    // 第二个占位符：用户ID
            return updatePs.executeUpdate() > 0; // 执行更新并返回结果
        }
        
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
                    int r=2;
        String sql = "SELECT role FROM users WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql) ;
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r=rs.getInt("role");
                }
            }
        if (r==0){
            sql = "UPDATE students SET balance = balance + ? WHERE user_id = ?";
        } else if (r==1){
            sql = "UPDATE teachers SET balance = balance + ? WHERE user_id = ?";
        } else {
            return false; // Unknown role
        }
            
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
                
                // Update order status
                payOrder(orderId);
                return true;
            
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