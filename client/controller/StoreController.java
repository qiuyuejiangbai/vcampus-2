package client.controller;

import common.protocol.Message;
import common.protocol.MessageType;
import common.protocol.StatusCode;
import common.vo.ProductVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import common.vo.ShoppingCartItemVO;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

/**
 * 客户端商店模块控制器
 * 负责 UI 与服务器的通信
 */
public class StoreController {
    private final Integer currentUserId;   // 当前登录用户ID
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public StoreController(Integer userId) {
        this.currentUserId = userId;
        try {
            // 连接服务器
            this.socket = new Socket("127.0.0.1", 8888);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一请求方法
     */
    private Message sendRequest(Message request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Message) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "请求失败");
        }
    }

    // ===== 商品管理 =====
   
    /**
     * 搜索商品
     */
    public List<ProductVO> searchProducts(String keyword) {
        Message request = new Message(MessageType.SEARCH_PRODUCTS_REQUEST, StatusCode.SUCCESS, keyword);
        Message response = sendRequest(request);
       if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<ProductVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 获取商品详情
     */
    public ProductVO getProductById(Integer productId) {
        Message request = new Message(MessageType.GET_PRODUCT_BY_ID_REQUEST, StatusCode.SUCCESS, productId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (ProductVO) response.getData();
        }
        return null;
    }

    /**
     * 添加新商品（管理员功能）
     */
    public boolean addProduct(ProductVO product) {
        Message request = new Message(MessageType.ADD_PRODUCT_REQUEST, StatusCode.SUCCESS, product);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 更新商品信息
     */
    public boolean updateProduct(ProductVO product) {
        Message request = new Message(MessageType.UPDATE_PRODUCT_REQUEST, StatusCode.SUCCESS, product);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 删除商品
     */
    public boolean deleteProduct(Integer productId) {
        Message request = new Message(MessageType.DELETE_PRODUCT_REQUEST, StatusCode.SUCCESS, productId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 调整商品库存
     */
    public boolean adjustStock(Integer productId, int quantityDelta) {
        Object[] data = {productId, quantityDelta};
        Message request = new Message(MessageType.ADJUST_STOCK_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    // ===== 购物车管理 =====
    /**
     * 获取用户购物车
     */
    public List<ShoppingCartItemVO> getShoppingCart() {
        Message request = new Message(MessageType.GET_SHOPPING_CART_REQUEST, StatusCode.SUCCESS, currentUserId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<ShoppingCartItemVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 添加商品到购物车
     */
    public boolean addToCart(Integer productId, int quantity) {
        Object[] data = {currentUserId, productId, quantity};
        Message request = new Message(MessageType.ADD_TO_CART_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 从购物车移除商品
     */
    public boolean removeFromCart(Integer productId) {
        Message request = new Message(MessageType.REMOVE_FROM_CART_REQUEST, StatusCode.SUCCESS, 
                                     new Object[]{currentUserId, productId});
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 更新购物车商品数量
     */
    public boolean updateCartItem(Integer productId, int newQuantity) {
        Object[] data = {currentUserId, productId, newQuantity};
        Message request = new Message(MessageType.UPDATE_CART_ITEM_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 清空购物车
     */
    public boolean clearCart() {
        Message request = new Message(MessageType.CLEAR_CART_REQUEST, StatusCode.SUCCESS, currentUserId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    // ===== 订单管理 =====
    /**
     * 创建订单
     */
    public OrderVO createOrder(List<Integer> productIds, List<Integer> quantities) {
        Object[] data = {currentUserId, productIds, quantities};
        Message request = new Message(MessageType.CREATE_ORDER_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (OrderVO) response.getData();
        }
        return null;
    }

    /**
     * 管理员获取订单详情
     */
    public OrderVO getAdminOrderDetail(Integer orderId) {
        Message request = new Message(MessageType.GET_ADMIN_ORDER_DETAIL_REQUEST, StatusCode.SUCCESS, orderId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (OrderVO) response.getData();
        }
        return null;
    }

     // 添加订单状态更新方法
    public boolean updateOrderStatus(String orderId, String newStatus) {
        // TODO: 实现订单状态更新逻辑，返回true表示成功，false表示失败
        // 例如：调用后端服务或数据库更新订单状态
        return false;
    }
    
    /**
     * 管理员获取所有用户消费记录
     */
    public List<OrderVO> getAllUserOrders() {
        Message request = new Message(MessageType.GET_ALL_USER_ORDERS_REQUEST, StatusCode.SUCCESS, null);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<OrderVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 消费者获取订单历史
     */
    public List<OrderItemVO> getUserOrderHistory() {
        Message request = new Message(MessageType.GET_USER_ORDER_HISTORY_REQUEST, StatusCode.SUCCESS, currentUserId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<OrderItemVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 消费者获取订单商品项
     */
    public List<OrderItemVO> getOrderItems(Integer orderId) {
        Object[] data = {orderId, currentUserId};
        Message request = new Message(MessageType.GET_ORDER_ITEMS_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<OrderItemVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 取消订单
     */
    public boolean cancelOrder(Integer orderId) {
        Message request = new Message(MessageType.CANCEL_ORDER_REQUEST, StatusCode.SUCCESS, 
                                     new Object[]{orderId, currentUserId});
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 支付订单
     */
    public boolean payOrder(Integer orderId) {
        Message request = new Message(MessageType.PAY_ORDER_REQUEST, StatusCode.SUCCESS, orderId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 发货处理（管理员功能）
     */
    public boolean shipOrder(Integer orderId) {
        Message request = new Message(MessageType.SHIP_ORDER_REQUEST, StatusCode.SUCCESS, orderId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    // ===== 余额管理 =====
    /**
     * 获取用户余额
     */
    public double getUserBalance() {
        Message request = new Message(MessageType.GET_USER_BALANCE_REQUEST, StatusCode.SUCCESS, currentUserId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (Double) response.getData();
        }
        return 0.0;
    }

    /**
     * 用户充值
     */
    public boolean rechargeBalance(double amount) {
        Object[] data = {currentUserId, amount};
        Message request = new Message(MessageType.RECHARGE_BALANCE_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 使用余额支付订单
     */
    public boolean payWithBalance(Integer orderId) {
        Object[] data = {orderId, currentUserId};
        Message request = new Message(MessageType.PAY_WITH_BALANCE_REQUEST, StatusCode.SUCCESS, data);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 获取当前用户ID
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}