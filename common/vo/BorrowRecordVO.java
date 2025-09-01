package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 借阅记录值对象
 * 用于封装借阅记录信息在客户端和服务器端之间传输
 */
public class BorrowRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer recordId;       // 记录ID
    private Integer userId;         // 用户ID
    private Integer bookId;         // 图书ID
    private Timestamp borrowTime;   // 借出时间
    private Timestamp dueTime;      // 应还时间
    private Timestamp returnTime;   // 实际归还时间
    private Integer status;         // 状态：1-已借出，2-已归还，3-逾期
    
    // 关联信息（用于显示）
    private String userName;        // 用户姓名
    private String userLoginId;     // 用户登录ID
    private String bookTitle;       // 图书标题
    private String bookAuthor;      // 图书作者
    private String isbn;            // ISBN号
    
    public BorrowRecordVO() {}
    
    public BorrowRecordVO(Integer userId, Integer bookId, Timestamp dueTime) {
        this.userId = userId;
        this.bookId = bookId;
        this.dueTime = dueTime;
        this.status = 1; // 默认已借出
    }
    
    // Getters and Setters
    public Integer getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public Timestamp getBorrowTime() {
        return borrowTime;
    }
    
    public void setBorrowTime(Timestamp borrowTime) {
        this.borrowTime = borrowTime;
    }
    
    public Timestamp getDueTime() {
        return dueTime;
    }
    
    public void setDueTime(Timestamp dueTime) {
        this.dueTime = dueTime;
    }
    
    public Timestamp getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(Timestamp returnTime) {
        this.returnTime = returnTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
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
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "已借出";
            case 2: return "已归还";
            case 3: return "逾期";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否已归还
     * @return true表示已归还，false表示未归还
     */
    public boolean isReturned() {
        return status != null && status == 2;
    }
    
    /**
     * 检查是否逾期
     * @return true表示逾期，false表示未逾期
     */
    public boolean isOverdue() {
        if (status != null && status == 3) return true;
        if (dueTime == null || isReturned()) return false;
        return System.currentTimeMillis() > dueTime.getTime();
    }
    
    /**
     * 获取剩余天数
     * @return 剩余天数，负数表示逾期天数
     */
    public long getRemainingDays() {
        if (dueTime == null || isReturned()) return 0;
        long diff = dueTime.getTime() - System.currentTimeMillis();
        return diff / (1000 * 60 * 60 * 24);
    }
    
    /**
     * 获取借阅天数
     * @return 借阅天数
     */
    public long getBorrowDays() {
        if (borrowTime == null) return 0;
        long endTime = returnTime != null ? returnTime.getTime() : System.currentTimeMillis();
        long diff = endTime - borrowTime.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }
    
    @Override
    public String toString() {
        return "BorrowRecordVO{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", borrowTime=" + borrowTime +
                ", dueTime=" + dueTime +
                ", returnTime=" + returnTime +
                ", status=" + status +
                ", userName='" + userName + '\'' +
                ", userLoginId='" + userLoginId + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", bookAuthor='" + bookAuthor + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BorrowRecordVO that = (BorrowRecordVO) obj;
        return recordId != null && recordId.equals(that.recordId);
    }
    
    @Override
    public int hashCode() {
        return recordId != null ? recordId.hashCode() : 0;
    }
}
