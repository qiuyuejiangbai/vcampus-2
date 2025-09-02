package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 课程资源值对象
 * 用于封装课程相关资源信息在客户端和服务器端之间传输
 */
public class CourseResourceVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;             // 资源ID
    private Integer courseId;       // 课程ID
    private String resourceName;    // 资源名称
    private String resourceType;    // 资源类型：document, video, audio, image, link, other
    private String filePath;        // 文件路径
    private String fileUrl;         // 外部链接
    private Long fileSize;          // 文件大小(字节)
    private Integer uploaderId;     // 上传者ID
    private String description;     // 描述
    private Boolean isPublic;       // 是否公开
    private Integer downloadCount;  // 下载次数
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    // 关联信息（用于显示）
    private String courseName;      // 课程名称
    private String courseCode;      // 课程代码
    private String uploaderName;    // 上传者姓名
    private String uploaderType;    // 上传者类型（teacher/student）
    
    // 关联对象
    private CourseVO course;
    private UserVO uploader;
    
    // 构造函数
    public CourseResourceVO() {}
    
    public CourseResourceVO(Integer courseId, String resourceName, String resourceType, 
                           String filePath, Integer uploaderId) {
        this.courseId = courseId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.filePath = filePath;
        this.uploaderId = uploaderId;
        this.isPublic = true; // 默认公开
        this.downloadCount = 0; // 默认下载次数为0
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Integer getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
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
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getUploaderName() {
        return uploaderName;
    }
    
    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
    
    public String getUploaderType() {
        return uploaderType;
    }
    
    public void setUploaderType(String uploaderType) {
        this.uploaderType = uploaderType;
    }
    
    public CourseVO getCourse() {
        return course;
    }
    
    public void setCourse(CourseVO course) {
        this.course = course;
    }
    
    public UserVO getUploader() {
        return uploader;
    }
    
    public void setUploader(UserVO uploader) {
        this.uploader = uploader;
    }
    
    // 业务方法
    
    /**
     * 获取资源类型的中文名称
     * @return 资源类型中文名称
     */
    public String getResourceTypeName() {
        if (resourceType == null) return "未知";
        switch (resourceType) {
            case "document": return "文档";
            case "video": return "视频";
            case "audio": return "音频";
            case "image": return "图片";
            case "link": return "链接";
            case "other": return "其他";
            default: return "未知";
        }
    }
    
    /**
     * 获取格式化的文件大小
     * @return 格式化的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) return "0 B";
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double sizeDouble = size;
        
        while (sizeDouble >= 1024 && unitIndex < units.length - 1) {
            sizeDouble /= 1024;
            unitIndex++;
        }
        
        if (unitIndex == 0) {
            return String.format("%d %s", (int) sizeDouble, units[unitIndex]);
        } else {
            return String.format("%.1f %s", sizeDouble, units[unitIndex]);
        }
    }
    
    /**
     * 检查是否为文档类型
     * @return true表示是文档，false表示不是
     */
    public boolean isDocument() {
        return "document".equals(resourceType);
    }
    
    /**
     * 检查是否为视频类型
     * @return true表示是视频，false表示不是
     */
    public boolean isVideo() {
        return "video".equals(resourceType);
    }
    
    /**
     * 检查是否为音频类型
     * @return true表示是音频，false表示不是
     */
    public boolean isAudio() {
        return "audio".equals(resourceType);
    }
    
    /**
     * 检查是否为图片类型
     * @return true表示是图片，false表示不是
     */
    public boolean isImage() {
        return "image".equals(resourceType);
    }
    
    /**
     * 检查是否为外部链接
     * @return true表示是外部链接，false表示不是
     */
    public boolean isLink() {
        return "link".equals(resourceType);
    }
    
    /**
     * 检查是否为本地文件
     * @return true表示是本地文件，false表示不是
     */
    public boolean isLocalFile() {
        return filePath != null && !filePath.isEmpty();
    }
    
    /**
     * 检查是否为外部URL
     * @return true表示是外部URL，false表示不是
     */
    public boolean isExternalUrl() {
        return fileUrl != null && !fileUrl.isEmpty();
    }
    
    /**
     * 获取文件扩展名
     * @return 文件扩展名
     */
    public String getFileExtension() {
        if (resourceName == null) return "";
        
        int lastDotIndex = resourceName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < resourceName.length() - 1) {
            return resourceName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        if (downloadCount == null) {
            downloadCount = 1;
        } else {
            downloadCount++;
        }
    }
    
    /**
     * 检查用户是否可以访问该资源
     * @param userRole 用户角色
     * @return true表示可以访问，false表示不能访问
     */
    public boolean canAccess(String userRole) {
        // 公开资源任何人都可以访问
        if (isPublic != null && isPublic) {
            return true;
        }
        
        // 教师和管理员可以访问所有资源
        if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            return true;
        }
        
        // 私有资源只有选课学生可以访问（需要在业务层进一步验证）
        return false;
    }
    
    /**
     * 获取访问路径（优先返回外部链接，否则返回本地路径）
     * @return 访问路径
     */
    public String getAccessPath() {
        if (isExternalUrl()) {
            return fileUrl;
        } else if (isLocalFile()) {
            return filePath;
        }
        return "";
    }
    
    /**
     * 获取资源的完整描述
     * @return 资源描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceName);
        sb.append(" (").append(getResourceTypeName()).append(")");
        
        if (fileSize != null && fileSize > 0) {
            sb.append(" - ").append(getFormattedFileSize());
        }
        
        if (downloadCount != null && downloadCount > 0) {
            sb.append(" - 下载").append(downloadCount).append("次");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "CourseResourceVO{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", fileSize=" + fileSize +
                ", uploaderName='" + uploaderName + '\'' +
                ", isPublic=" + isPublic +
                ", downloadCount=" + downloadCount +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseResourceVO that = (CourseResourceVO) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
