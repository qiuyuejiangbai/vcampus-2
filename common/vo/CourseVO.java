package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 课程值对象
 * 用于封装课程信息在客户端和服务器端之间传输
 */
public class CourseVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer courseId;       // 课程ID
    private String courseCode;      // 课程代码
    private String courseName;      // 课程名称
    private Integer credits;        // 学分
    private String department;      // 开课院系
    private Integer teacherId;      // 任课教师ID
    private String teacherName;     // 任课教师姓名（用于显示）
    private String semester;        // 开课学期
    private String description;     // 课程描述
    private Integer capacity;       // 课程容量
    private Integer enrolledCount;  // 已选人数
    private Integer status;         // 状态：0-停用，1-启用
    private Timestamp createdTime;  // 创建时间
    
    public CourseVO() {}
    
    public CourseVO(String courseCode, String courseName, Integer credits, String department, Integer teacherId, String semester) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.teacherId = teacherId;
        this.semester = semester;
        this.capacity = 50; // 默认容量
        this.enrolledCount = 0; // 默认已选人数为0
        this.status = 1; // 默认启用
    }
    
    // Getters and Setters
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public Integer getCredits() {
        return credits;
    }
    
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Integer getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public Integer getEnrolledCount() {
        return enrolledCount;
    }
    
    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * 获取剩余容量
     * @return 剩余容量
     */
    public Integer getAvailableCapacity() {
        if (capacity == null || enrolledCount == null) return 0;
        return capacity - enrolledCount;
    }
    
    /**
     * 检查课程是否已满
     * @return true表示已满，false表示未满
     */
    public boolean isFull() {
        return getAvailableCapacity() <= 0;
    }
    
    /**
     * 检查课程是否启用
     * @return true表示启用，false表示停用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "停用";
            case 1: return "启用";
            default: return "未知";
        }
    }
    
    @Override
    public String toString() {
        return "CourseVO{" +
                "courseId=" + courseId +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                ", department='" + department + '\'' +
                ", teacherId=" + teacherId +
                ", teacherName='" + teacherName + '\'' +
                ", semester='" + semester + '\'' +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", enrolledCount=" + enrolledCount +
                ", status=" + status +
                ", createdTime=" + createdTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseVO courseVO = (CourseVO) obj;
        return courseId != null && courseId.equals(courseVO.courseId);
    }
    
    @Override
    public int hashCode() {
        return courseId != null ? courseId.hashCode() : 0;
    }
}
