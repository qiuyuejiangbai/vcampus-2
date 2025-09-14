package server.service;

import java.util.List;
import common.vo.*;
import common.vo.ProductVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import common.vo.ShoppingCartItemVO;

/**
 * 商店模块业务服务接口
 * 包含商品管理、购物车系统、订单管理和余额支付功能
 */
public interface StoreService {
    
    // ===== 商品管理 =====
    /** 按关键字搜索商品 */
    List<ProductVO> searchProducts(String keyword);
    
    /** 按ID获取商品详情 */
    ProductVO getProductById(Integer productId);
    
    /** 添加新商品（管理员功能） */
    boolean addProduct(ProductVO product);
    
    /** 更新商品信息 */
    boolean updateProduct(ProductVO product);
    
    /** 删除商品 */
    boolean deleteProduct(Integer productId);
    
    /** 调整商品库存 */
    boolean adjustStock(Integer productId, int quantityDelta);
    
    // ===== 购物车管理 =====
    /** 获取用户购物车 */
    List<ShoppingCartItemVO> getShoppingCart(Integer userId);
    
    /** 添加商品到购物车 */
    boolean addToCart(Integer userId, Integer productId, int quantity);
    
    /** 从购物车移除商品 */
    boolean removeFromCart(Integer userId, Integer productId);
    
    /** 更新购物车商品数量 */
    boolean updateCartItem(Integer userId, Integer productId, int newQuantity);
    
    /** 清空购物车 */
    boolean clearCart(Integer userId);
    
    // ===== 订单管理 =====
    // 管理员视图 - 获取所有用户的消费记录
    List<OrderVO> getAllUserOrders();
    
    /** 获取订单详情（管理员视图 - 包含用户信息和完整订单数据） */
    OrderVO getAdminOrderDetail(Integer orderId);
    
    // 消费者视图 - 创建订单
    OrderVO createOrder(Integer userId, List<Integer> productIds, List<Integer> quantities);
    
    /** 消费者订单历史 - 仅返回订单项（OrderItemVO列表） */
    List<OrderItemVO> getUserOrderHistory(Integer userId);
    
    /** 消费者查看订单 - 返回订单项（OrderItemVO列表） */
    List<OrderItemVO> getOrderItems(Integer orderId, Integer userId);
    
    /** 取消订单（消费者） */
    boolean cancelOrder(Integer orderId, Integer userId);
    
    /** 支付订单（消费者） */
    boolean payOrder(Integer orderId);
    
    /** 订单发货处理（管理员功能） */
    boolean shipOrder(Integer orderId);
    
    // ===== 余额管理 =====
    /** 获取用户余额 */
    double getUserBalance(Integer userId);
    
    /** 用户充值 */
    boolean rechargeBalance(Integer userId, double amount);
    
    /** 使用余额支付订单 */
    boolean payWithBalance(Integer orderId, Integer userId);
}