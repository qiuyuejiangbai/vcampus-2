package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 商品值对象
 * 用于封装商品信息在客户端和服务器端之间传输
 */
public class ProductVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer productId;      // 商品ID
    private String productName;     // 商品名称
    private String description;     // 商品描述
    private Double price;           // 价格
    private Integer stock;          // 库存
    private String category;        // 分类
    private Integer status;         // 状态：0-下架，1-上架
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    public ProductVO() {}
    
    public ProductVO(String productName, String description, Double price, Integer stock, String category) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = 1; // 默认上架
    }
    
    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public Timestamp getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "下架";
            case 1: return "上架";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否上架
     * @return true表示上架，false表示下架
     */
    public boolean isAvailable() {
        return status != null && status == 1;
    }
    
    /**
     * 检查库存是否充足
     * @param quantity 需要的数量
     * @return true表示库存充足，false表示库存不足
     */
    public boolean hasEnoughStock(int quantity) {
        return stock != null && stock >= quantity;
    }
    
    /**
     * 检查是否缺货
     * @return true表示缺货，false表示有库存
     */
    public boolean isOutOfStock() {
        return stock == null || stock <= 0;
    }
    
    /**
     * 获取格式化的价格字符串
     * @return 格式化的价格字符串
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f", price) : "0.00";
    }
    
    @Override
    public String toString() {
        return "ProductVO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductVO productVO = (ProductVO) obj;
        return productId != null && productId.equals(productVO.productId);
    }
    
    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }
}
