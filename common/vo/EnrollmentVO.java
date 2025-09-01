package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 选课记录值对象
 * 用于封装选课记录信息在客户端和服务器端之间传输
 */
public class EnrollmentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer enrollmentId;   // 选课记录ID
    private Integer studentId;      // 学生ID
    private Integer courseId;       // 课程ID
    private Timestamp enrollmentTime; // 选课时间
    private Double grade;           // 成绩
    private Integer status;         // 状态：0-已退课，1-已选课，2-已完成
    
    // 关联信息（用于显示）
    private String studentName;     // 学生姓名
    private String studentNo;       // 学号
    private String courseName;      // 课程名称
    private String courseCode;      // 课程代码
    private Integer credits;        // 学分
    private String semester;        // 学期
    
    public EnrollmentVO() {}
    
    public EnrollmentVO(Integer studentId, Integer courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = 1; // 默认已选课
    }
    
    // Getters and Setters
    public Integer getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public Timestamp getEnrollmentTime() {
        return enrollmentTime;
    }
    
    public void setEnrollmentTime(Timestamp enrollmentTime) {
        this.enrollmentTime = enrollmentTime;
    }
    
    public Double getGrade() {
        return grade;
    }
    
    public void setGrade(Double grade) {
        this.grade = grade;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentNo() {
        return studentNo;
    }
    
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
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
    
    public Integer getCredits() {
        return credits;
    }
    
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已退课";
            case 1: return "已选课";
            case 2: return "已完成";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否已完成
     * @return true表示已完成，false表示未完成
     */
    public boolean isCompleted() {
        return status != null && status == 2;
    }
    
    /**
     * 检查是否已退课
     * @return true表示已退课，false表示未退课
     */
    public boolean isDropped() {
        return status != null && status == 0;
    }
    
    /**
     * 检查是否有成绩
     * @return true表示有成绩，false表示无成绩
     */
    public boolean hasGrade() {
        return grade != null && grade >= 0;
    }
    
    /**
     * 获取成绩等级
     * @return 成绩等级字符串
     */
    public String getGradeLevel() {
        if (grade == null) return "未录入";
        if (grade >= 90) return "优秀";
        if (grade >= 80) return "良好";
        if (grade >= 70) return "中等";
        if (grade >= 60) return "及格";
        return "不及格";
    }
    
    @Override
    public String toString() {
        return "EnrollmentVO{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", enrollmentTime=" + enrollmentTime +
                ", grade=" + grade +
                ", status=" + status +
                ", studentName='" + studentName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", credits=" + credits +
                ", semester='" + semester + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnrollmentVO that = (EnrollmentVO) obj;
        return enrollmentId != null && enrollmentId.equals(that.enrollmentId);
    }
    
    @Override
    public int hashCode() {
        return enrollmentId != null ? enrollmentId.hashCode() : 0;
    }
}
