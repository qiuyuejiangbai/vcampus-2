package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 订单值对象
 * 用于封装订单信息在客户端和服务器端之间传输
 */
public class OrderVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer orderId;        // 订单ID
    private Integer userId;         // 用户ID
    private Double totalAmount;     // 订单总额
    private Integer status;         // 状态：1-已完成，0-已取消
    private Timestamp createdTime;  // 创建时间
    
    // 关联信息（用于显示）
    private String userName;        // 用户姓名
    private String userLoginId;     // 用户登录ID
    private List<OrderItemVO> items; // 订单明细列表
    
    public OrderVO() {}
    
    public OrderVO(Integer userId, Double totalAmount) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = 1; // 默认已完成
    }
    
    // Getters and Setters
    public Integer getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserLoginId() {
        return userLoginId;
    }
    
    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }
    
    public List<OrderItemVO> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemVO> items) {
        this.items = items;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已取消";
            case 1: return "已完成";
            default: return "未知";
        }
    }
    
    /**
     * 检查订单是否已完成
     * @return true表示已完成，false表示未完成
     */
    public boolean isCompleted() {
        return status != null && status == 1;
    }
    
    /**
     * 检查订单是否已取消
     * @return true表示已取消，false表示未取消
     */
    public boolean isCancelled() {
        return status != null && status == 0;
    }
    
    /**
     * 获取订单商品总数量
     * @return 商品总数量
     */
    public int getTotalQuantity() {
        if (items == null || items.isEmpty()) return 0;
        return items.stream().mapToInt(OrderItemVO::getQuantity).sum();
    }
    
    /**
     * 获取格式化的总金额字符串
     * @return 格式化的总金额字符串
     */
    public String getFormattedTotalAmount() {
        return totalAmount != null ? String.format("%.2f", totalAmount) : "0.00";
    }
    
    @Override
    public String toString() {
        return "OrderVO{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", userName='" + userName + '\'' +
                ", userLoginId='" + userLoginId + '\'' +
                ", items=" + items +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderVO orderVO = (OrderVO) obj;
        return orderId != null && orderId.equals(orderVO.orderId);
    }
    
    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }
}
