package common.vo;

import java.io.Serializable;
import common.vo.ProductVO;
import java.util.List;
import java.sql.Timestamp;

/**
 * 购物车商品项
 * 表示用户购物车中的一项商品
 */
public class ShoppingCartItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 商品ID */
    private Integer productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 商品单价 */
    private double price;
    
    /** 购买数量 */
    private int quantity;
    
    /** 小计金额（价格×数量） */
    private double subtotal;
    
    /** 商品图片URL */
    private String imageUrl;
    
    /** 商品库存状态 */
    private boolean inStock;

    private Integer id;

    private String description;

    private String category;
    
    // 构造方法
    public ShoppingCartItemVO() {}
    
    public ShoppingCartItemVO(Integer productId, double price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    // Getter和Setter方法
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.subtotal = price * quantity; // 更新小计
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = price * quantity; // 更新小计
    }

    public double getSubtotal() {
        return subtotal;
    }
    
    public double setSubtotal(double subtotal) {
    	this.subtotal = subtotal;
        return subtotal;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public Integer getId() {
        return id;
    }   

    public void setId(Integer id) {
        this.id = id;
    }   

    public String getCategory() {
        return category;
    }       

    public void setCategory(String category) {
        this.category = category;
    }
    
    // 业务方法
    /**
     * 更新购买数量并自动重新计算小计
     */
    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        this.subtotal = price * newQuantity;
    }
    
    /**
     * 增加购买数量并更新小计
     */
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        this.subtotal = price * this.quantity;
    }
    
    /**
     * 减少购买数量并更新小计
     */
    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            this.subtotal = price * this.quantity;
        }
    }
    
    // 重写toString方法用于调试
    @Override
    public String toString() {
        return "ShoppingCartItemVO{" +
               "id=" + id +
               "productId=" + productId +
               ", productName='" + productName + '\'' +
               ", price=" + price +
               ", quantity=" + quantity +
               ", subtotal=" + subtotal +
               ", imageUrl='" + imageUrl + '\'' +
               ", inStock=" + inStock +
               '}';
    }
}