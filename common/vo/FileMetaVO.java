package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 文件元信息值对象
 * 用于封装文件元信息在客户端和服务器端之间传输
 */
public class FileMetaVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer fileId;         // 文件ID
    private Integer courseId;       // 所属课程ID
    private String fileName;        // 文件名
    private String filePath;        // 文件存储路径
    private Long fileSize;          // 文件大小（字节）
    private Integer uploaderId;     // 上传者ID
    private String description;     // 文件描述
    private Timestamp uploadTime;   // 上传时间
    
    // 关联信息（用于显示）
    private String courseName;      // 课程名称
    private String courseCode;      // 课程代码
    private String uploaderName;    // 上传者姓名
    private String uploaderLoginId; // 上传者登录ID
    
    public FileMetaVO() {}
    
    public FileMetaVO(Integer courseId, String fileName, Long fileSize, Integer uploaderId) {
        this.courseId = courseId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploaderId = uploaderId;
    }
    
    public FileMetaVO(Integer courseId, String fileName, String filePath, Long fileSize, Integer uploaderId, String description) {
        this.courseId = courseId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploaderId = uploaderId;
        this.description = description;
    }
    
    // Getters and Setters
    public Integer getFileId() {
        return fileId;
    }
    
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
    
    public Timestamp getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
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
    
    public String getUploaderLoginId() {
        return uploaderLoginId;
    }
    
    public void setUploaderLoginId(String uploaderLoginId) {
        this.uploaderLoginId = uploaderLoginId;
    }
    
    /**
     * 获取文件扩展名
     * @return 文件扩展名
     */
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    /**
     * 获取格式化的文件大小
     * @return 格式化的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    /**
     * 检查是否为图片文件
     * @return true表示是图片文件，false表示不是
     */
    public boolean isImageFile() {
        String ext = getFileExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
               ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }
    
    /**
     * 检查是否为文档文件
     * @return true表示是文档文件，false表示不是
     */
    public boolean isDocumentFile() {
        String ext = getFileExtension();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("ppt") || ext.equals("pptx") || ext.equals("xls") || ext.equals("xlsx") ||
               ext.equals("txt") || ext.equals("rtf");
    }
    
    /**
     * 检查是否为视频文件
     * @return true表示是视频文件，false表示不是
     */
    public boolean isVideoFile() {
        String ext = getFileExtension();
        return ext.equals("mp4") || ext.equals("avi") || ext.equals("mkv") || 
               ext.equals("mov") || ext.equals("wmv") || ext.equals("flv");
    }
    
    /**
     * 获取文件类型描述
     * @return 文件类型描述
     */
    public String getFileTypeDescription() {
        if (isImageFile()) return "图片文件";
        if (isDocumentFile()) return "文档文件";
        if (isVideoFile()) return "视频文件";
        
        String ext = getFileExtension();
        switch (ext) {
            case "zip":
            case "rar":
            case "7z": return "压缩文件";
            case "mp3":
            case "wav":
            case "flac": return "音频文件";
            case "java":
            case "cpp":
            case "py":
            case "js": return "代码文件";
            default: return "其他文件";
        }
    }
    
    @Override
    public String toString() {
        return "FileMetaVO{" +
                "fileId=" + fileId +
                ", courseId=" + courseId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", uploaderId=" + uploaderId +
                ", description='" + description + '\'' +
                ", uploadTime=" + uploadTime +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", uploaderName='" + uploaderName + '\'' +
                ", uploaderLoginId='" + uploaderLoginId + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FileMetaVO that = (FileMetaVO) obj;
        return fileId != null && fileId.equals(that.fileId);
    }
    
    @Override
    public int hashCode() {
        return fileId != null ? fileId.hashCode() : 0;
    }
}
