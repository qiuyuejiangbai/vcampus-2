package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 图书值对象
 * 用于封装图书信息在客户端和服务器端之间传输
 */
public class BookVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer bookId;         // 图书ID
    private String isbn;            // ISBN号
    private String title;           // 书名
    private String author;          // 作者
    private String publisher;       // 出版社
    private String category;        // 分类
    private Integer totalStock;     // 总库存
    private Integer availableStock; // 可借库存
    private Timestamp createdTime;  // 创建时间
    
    public BookVO() {}
    
    public BookVO(String isbn, String title, String author, String publisher, String category, Integer totalStock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.category = category;
        this.totalStock = totalStock;
        this.availableStock = totalStock; // 默认可借库存等于总库存
    }
    
    // Getters and Setters
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Integer getTotalStock() {
        return totalStock;
    }
    
    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * 获取已借出数量
     * @return 已借出数量
     */
    public Integer getBorrowedCount() {
        if (totalStock == null || availableStock == null) return 0;
        return totalStock - availableStock;
    }
    
    /**
     * 检查是否可借
     * @return true表示可借，false表示不可借
     */
    public boolean isAvailable() {
        return availableStock != null && availableStock > 0;
    }
    
    /**
     * 检查库存是否充足
     * @param quantity 需要的数量
     * @return true表示库存充足，false表示库存不足
     */
    public boolean hasEnoughStock(int quantity) {
        return availableStock != null && availableStock >= quantity;
    }
    
    @Override
    public String toString() {
        return "BookVO{" +
                "bookId=" + bookId +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", category='" + category + '\'' +
                ", totalStock=" + totalStock +
                ", availableStock=" + availableStock +
                ", createdTime=" + createdTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BookVO bookVO = (BookVO) obj;
        return bookId != null && bookId.equals(bookVO.bookId);
    }
    
    @Override
    public int hashCode() {
        return bookId != null ? bookId.hashCode() : 0;
    }
}
