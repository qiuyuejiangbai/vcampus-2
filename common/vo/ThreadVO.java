package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 论坛主题值对象
 * 用于封装论坛主题信息在客户端和服务器端之间传输
 */
public class ThreadVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer threadId;       // 主题ID
    private String title;           // 主题标题
    private String content;         // 内容
    private Integer authorId;       // 作者ID
    private Integer replyCount;     // 回复数
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    private Integer status;         // 状态：0-已删除，1-正常
    
    // 关联信息（用于显示）
    private String authorName;      // 作者姓名
    private String authorLoginId;   // 作者登录ID
    
    public ThreadVO() {}
    
    public ThreadVO(String title, String content, Integer authorId) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.replyCount = 0; // 默认回复数为0
        this.status = 1; // 默认正常状态
    }
    
    // Getters and Setters
    public Integer getThreadId() {
        return threadId;
    }
    
    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
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
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorLoginId() {
        return authorLoginId;
    }
    
    public void setAuthorLoginId(String authorLoginId) {
        this.authorLoginId = authorLoginId;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已删除";
            case 1: return "正常";
            default: return "未知";
        }
    }
    
    /**
     * 检查主题是否正常
     * @return true表示正常，false表示已删除
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    /**
     * 检查主题是否已删除
     * @return true表示已删除，false表示正常
     */
    public boolean isDeleted() {
        return status != null && status == 0;
    }
    
    /**
     * 增加回复数
     */
    public void incrementReplyCount() {
        if (this.replyCount == null) {
            this.replyCount = 1;
        } else {
            this.replyCount++;
        }
    }
    
    /**
     * 减少回复数
     */
    public void decrementReplyCount() {
        if (this.replyCount != null && this.replyCount > 0) {
            this.replyCount--;
        }
    }
    
    /**
     * 获取内容摘要（用于列表显示）
     * @param maxLength 最大长度
     * @return 内容摘要
     */
    public String getContentSummary(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    @Override
    public String toString() {
        return "ThreadVO{" +
                "threadId=" + threadId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", replyCount=" + replyCount +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", status=" + status +
                ", authorName='" + authorName + '\'' +
                ", authorLoginId='" + authorLoginId + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ThreadVO threadVO = (ThreadVO) obj;
        return threadId != null && threadId.equals(threadVO.threadId);
    }
    
    @Override
    public int hashCode() {
        return threadId != null ? threadId.hashCode() : 0;
    }
}
