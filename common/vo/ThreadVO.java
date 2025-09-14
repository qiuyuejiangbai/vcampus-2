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
    private Integer viewCount;      // 浏览数
    private Integer likeCount;      // 点赞数
    private Integer favoriteCount;  // 收藏数
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    private Integer status;         // 状态：0-已删除，1-正常
    // 分区/板块
    private Integer sectionId;      // 板块ID
    private String sectionName;     // 板块名称（用于展示）
    
    // 关联信息（用于显示）
    private String authorName;      // 作者姓名
    private String authorLoginId;   // 作者登录ID

    // 是否公告（管理员发布的帖子视为公告）
    private Boolean isAnnouncement;
    
    // 当前用户是否已点赞
    private Boolean isLiked; 
    
    // 是否精华帖
    private Boolean isEssence; 
    
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
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
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

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
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

    public Boolean getIsAnnouncement() {
        return isAnnouncement != null && isAnnouncement;
    }

    public void setIsAnnouncement(Boolean isAnnouncement) {
        this.isAnnouncement = isAnnouncement;
    }
    
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
    
    public Boolean getIsEssence() {
        return isEssence;
    }
    
    public void setIsEssence(Boolean isEssence) {
        this.isEssence = isEssence;
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
     * @param by 增加的数量（默认为1）
     */
    public void incrementReplyCount(int by) {
        if (this.replyCount == null) {
            this.replyCount = Math.max(0, by);
        } else {
            this.replyCount += Math.max(0, by);
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
                ", isAnnouncement=" + getIsAnnouncement() +
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
