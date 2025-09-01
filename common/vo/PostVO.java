package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 论坛回复值对象
 * 用于封装论坛回复信息在客户端和服务器端之间传输
 */
public class PostVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer postId;         // 回复ID
    private Integer threadId;       // 所属主题ID
    private String content;         // 回复内容
    private Integer authorId;       // 作者ID
    private Timestamp createdTime;  // 创建时间
    private Integer status;         // 状态：0-已删除，1-正常
    
    // 关联信息（用于显示）
    private String authorName;      // 作者姓名
    private String authorLoginId;   // 作者登录ID
    private String threadTitle;     // 主题标题
    
    public PostVO() {}
    
    public PostVO(Integer threadId, String content, Integer authorId) {
        this.threadId = threadId;
        this.content = content;
        this.authorId = authorId;
        this.status = 1; // 默认正常状态
    }
    
    // Getters and Setters
    public Integer getPostId() {
        return postId;
    }
    
    public void setPostId(Integer postId) {
        this.postId = postId;
    }
    
    public Integer getThreadId() {
        return threadId;
    }
    
    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
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
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
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
    
    public String getThreadTitle() {
        return threadTitle;
    }
    
    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
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
     * 检查回复是否正常
     * @return true表示正常，false表示已删除
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    /**
     * 检查回复是否已删除
     * @return true表示已删除，false表示正常
     */
    public boolean isDeleted() {
        return status != null && status == 0;
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
        return "PostVO{" +
                "postId=" + postId +
                ", threadId=" + threadId +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", createdTime=" + createdTime +
                ", status=" + status +
                ", authorName='" + authorName + '\'' +
                ", authorLoginId='" + authorLoginId + '\'' +
                ", threadTitle='" + threadTitle + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PostVO postVO = (PostVO) obj;
        return postId != null && postId.equals(postVO.postId);
    }
    
    @Override
    public int hashCode() {
        return postId != null ? postId.hashCode() : 0;
    }
}
