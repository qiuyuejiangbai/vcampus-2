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
    private String semester;        // 学期
    private String academicYear;    // 学年
    private Timestamp enrollmentTime; // 选课时间
    private Timestamp dropTime;     // 退课时间
    private String dropReason;      // 退课原因
    private String status;          // 状态：enrolled, dropped, completed
    
    // 关联信息（用于显示）
    private String studentName;     // 学生姓名
    private String studentNo;       // 学号
    private String courseName;      // 课程名称
    private String courseCode;      // 课程代码
    private Integer credits;        // 学分
    private String teacherName;     // 教师姓名
    
    // 关联对象
    private StudentVO student;
    private CourseVO course;
    private GradeVO grade;          // 成绩信息
    
    public EnrollmentVO() {}
    
    public EnrollmentVO(Integer studentId, Integer courseId, String semester, String academicYear) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.semester = semester;
        this.academicYear = academicYear;
        this.status = "enrolled"; // 默认已选课
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
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public Timestamp getEnrollmentTime() {
        return enrollmentTime;
    }
    
    public void setEnrollmentTime(Timestamp enrollmentTime) {
        this.enrollmentTime = enrollmentTime;
    }
    
    public Timestamp getDropTime() {
        return dropTime;
    }
    
    public void setDropTime(Timestamp dropTime) {
        this.dropTime = dropTime;
    }
    
    public String getDropReason() {
        return dropReason;
    }
    
    public void setDropReason(String dropReason) {
        this.dropReason = dropReason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public StudentVO getStudent() {
        return student;
    }
    
    public void setStudent(StudentVO student) {
        this.student = student;
    }
    
    public CourseVO getCourse() {
        return course;
    }
    
    public void setCourse(CourseVO course) {
        this.course = course;
    }
    
    public GradeVO getGrade() {
        return grade;
    }
    
    public void setGrade(GradeVO grade) {
        this.grade = grade;
    }
    
    // 业务方法
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case "enrolled": return "已选课";
            case "dropped": return "已退课";
            case "completed": return "已完成";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否已完成
     * @return true表示已完成，false表示未完成
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    /**
     * 检查是否已退课
     * @return true表示已退课，false表示未退课
     */
    public boolean isDropped() {
        return "dropped".equals(status);
    }
    
    /**
     * 检查是否已选课
     * @return true表示已选课，false表示未选课
     */
    public boolean isEnrolled() {
        return "enrolled".equals(status);
    }
    
    /**
     * 检查是否有成绩
     * @return true表示有成绩，false表示无成绩
     */
    public boolean hasGrade() {
        return grade != null && grade.getTotalGrade() != null;
    }
    
    /**
     * 退课操作
     * @param reason 退课原因
     */
    public void dropCourse(String reason) {
        this.status = "dropped";
        this.dropReason = reason;
        this.dropTime = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * 完成课程
     */
    public void completeCourse() {
        this.status = "completed";
    }
    
    /**
     * 获取最终成绩
     * @return 最终成绩
     */
    public String getFinalGrade() {
        if (grade != null && grade.getTotalGrade() != null) {
            return grade.getFormattedGrade();
        }
        return "未评分";
    }
    
    /**
     * 获取成绩等级
     * @return 成绩等级字符串
     */
    public String getGradeLevel() {
        return grade != null ? grade.getGradeLevel() : "未评分";
    }
    
    /**
     * 获取选课状态描述
     * @return 状态描述
     */
    public String getStatusDescription() {
        StringBuilder sb = new StringBuilder(getStatusName());
        
        if (isDropped() && dropTime != null) {
            sb.append(" (").append(dropTime.toString().substring(0, 10)).append(")");
            if (dropReason != null && !dropReason.isEmpty()) {
                sb.append(" - ").append(dropReason);
            }
        } else if (isCompleted() && hasGrade()) {
            sb.append(" - ").append(getFinalGrade());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "EnrollmentVO{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", semester='" + semester + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", enrollmentTime=" + enrollmentTime +
                ", status='" + status + '\'' +
                ", studentName='" + studentName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", credits=" + credits +
                ", teacherName='" + teacherName + '\'' +
                ", hasGrade=" + hasGrade() +
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