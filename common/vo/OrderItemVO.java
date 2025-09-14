package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 订单明细值对象
 * 用于封装订单明细信息在客户端和服务器端之间传输
 */
public class OrderItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer itemId;         // 明细ID
    private Integer orderId;        // 订单ID
    private Integer productId;      // 商品ID
    private Integer quantity;       // 数量
    private Double unitPrice;       // 单价
    private Double subtotal;        // 小计
    private String status;         // 状态：1-已完成，0-已取消
    private Timestamp createdTime;  // 创建时间
    
    // 关联信息（用于显示）
    private String productName;     // 商品名称
    private String productDescription; // 商品描述
    private String category;        // 商品分类
    
    public OrderItemVO() {}
    
    public OrderItemVO(Integer productId, Integer quantity, Double unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
        this.status = "已完成"; // 默认已完成
    }
    
    public OrderItemVO(Integer orderId, Integer productId, Integer quantity, Double unitPrice) {
        this(productId, quantity, unitPrice);
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public Integer getItemId() {
        return itemId;
    }
    
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    
    public Integer getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // 自动重新计算小计
        if (this.unitPrice != null && quantity != null) {
            this.subtotal = quantity * this.unitPrice;
        }
    }
    
    public Double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        // 自动重新计算小计
        if (this.quantity != null && unitPrice != null) {
            this.subtotal = this.quantity * unitPrice;
        }
    }
    
    public Double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * 计算小计
     * @return 小计金额
     */
    public Double calculateSubtotal() {
        if (quantity == null || unitPrice == null) return 0.0;
        this.subtotal = quantity * unitPrice;
        return this.subtotal;
    }
    
    /**
     * 获取格式化的单价字符串
     * @return 格式化的单价字符串
     */
    public String getFormattedUnitPrice() {
        return unitPrice != null ? String.format("%.2f", unitPrice) : "0.00";
    }
    
    /**
     * 获取格式化的小计字符串
     * @return 格式化的小计字符串
     */
    public String getFormattedSubtotal() {
        return subtotal != null ? String.format("%.2f", subtotal) : "0.00";
    }
    
    @Override
    public String toString() {
        return "OrderItemVO{" +
                "itemId=" + itemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItemVO that = (OrderItemVO) obj;
        return itemId != null && itemId.equals(that.itemId);
    }
    
    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }
}
