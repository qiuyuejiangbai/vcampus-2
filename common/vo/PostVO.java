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
    private Integer parentPostId;   // 父回复ID（支持嵌套回复）
    private Integer quotePostId;    // 引用回复ID
    private Integer replyLevel;     // 回复层级：0-顶级回复，1-二级回复，2-三级回复等
    private String replyPath;       // 回复路径，用于快速查询子回复
    private Timestamp createdTime;  // 创建时间
    private Integer status;         // 状态：0-已删除，1-正常
    private Integer likeCount;      // 点赞数
    private Integer replyCount;     // 回复数（子回复数量）
    private Boolean isLiked;        // 当前用户是否已点赞
    
    // 关联信息（用于显示）
    private String authorName;      // 作者姓名
    private String authorLoginId;   // 作者登录ID
    private String threadTitle;     // 主题标题
    
    // 回复相关显示信息
    private String parentAuthorName; // 父回复作者姓名
    private String quotedContent;     // 被引用的回复内容
    private String quotedAuthorName; // 被引用回复的作者姓名
    
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
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
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
    
    public Integer getParentPostId() {
        return parentPostId;
    }
    
    public void setParentPostId(Integer parentPostId) {
        this.parentPostId = parentPostId;
    }
    
    public Integer getQuotePostId() {
        return quotePostId;
    }
    
    public void setQuotePostId(Integer quotePostId) {
        this.quotePostId = quotePostId;
    }
    
    public Integer getReplyLevel() {
        return replyLevel;
    }
    
    public void setReplyLevel(Integer replyLevel) {
        this.replyLevel = replyLevel;
    }
    
    public String getReplyPath() {
        return replyPath;
    }
    
    public void setReplyPath(String replyPath) {
        this.replyPath = replyPath;
    }
    
    public String getParentAuthorName() {
        return parentAuthorName;
    }
    
    public void setParentAuthorName(String parentAuthorName) {
        this.parentAuthorName = parentAuthorName;
    }
    
    public String getQuotedContent() {
        return quotedContent;
    }
    
    public void setQuotedContent(String quotedContent) {
        this.quotedContent = quotedContent;
    }
    
    public String getQuotedAuthorName() {
        return quotedAuthorName;
    }
    
    public void setQuotedAuthorName(String quotedAuthorName) {
        this.quotedAuthorName = quotedAuthorName;
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
    
    /**
     * 检查是否为顶级回复
     * @return true表示顶级回复，false表示子回复
     */
    public boolean isTopLevelReply() {
        return parentPostId == null && (replyLevel == null || replyLevel == 0);
    }
    
    /**
     * 检查是否为子回复
     * @return true表示子回复，false表示顶级回复
     */
    public boolean isSubReply() {
        return parentPostId != null && replyLevel != null && replyLevel > 0;
    }
    
    /**
     * 获取回复层级显示文本
     * @return 层级显示文本
     */
    public String getReplyLevelText() {
        if (replyLevel == null || replyLevel == 0) {
            return "回复";
        } else {
            return "回复" + replyLevel + "级";
        }
    }
    
    /**
     * 获取回复路径的层级数组
     * @return 路径层级数组
     */
    public String[] getReplyPathArray() {
        if (replyPath == null || replyPath.isEmpty()) {
            return new String[0];
        }
        return replyPath.split("/");
    }
    
    @Override
    public String toString() {
        return "PostVO{" +
                "postId=" + postId +
                ", threadId=" + threadId +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", parentPostId=" + parentPostId +
                ", quotePostId=" + quotePostId +
                ", replyLevel=" + replyLevel +
                ", replyPath='" + replyPath + '\'' +
                ", createdTime=" + createdTime +
                ", status=" + status +
                ", likeCount=" + likeCount +
                ", replyCount=" + replyCount +
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
