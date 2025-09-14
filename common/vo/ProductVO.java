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
    private String category;        // 分类
    private Double price;           // 价格
    private Integer stock;          // 库存
    private Timestamp createdTime;  // 创建时间
 
    public ProductVO() {}

    public ProductVO(String productName, String description, String category, Double price, Integer stock) {
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }


    public Timestamp getCreatedTime() { return createdTime; }
    public void setCreatedTime(Timestamp createdTime) { this.createdTime = createdTime; }

   
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
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", createdTime=" + createdTime +
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