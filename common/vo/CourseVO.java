package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

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
    private String academicYear;    // 学年
    private String classTime;       // 上课时间描述
    private String location;        // 上课地点
    private String description;     // 课程描述
    private String prerequisites;   // 先修课程要求
    private String syllabus;        // 教学大纲
    private Integer capacity;       // 课程容量
    private Integer enrolledCount;  // 已选人数
    private String status;          // 状态：planning, active, inactive, completed
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    // 关联对象
    private TeacherVO teacher;      // 任课教师
    private List<StudentVO> enrolledStudents; // 选课学生名单
    private List<CourseScheduleVO> schedules; // 课程时间安排
    private List<CourseResourceVO> resources; // 课程资源
    private List<EnrollmentVO> enrollments;   // 选课记录
    
    public CourseVO() {}
    
    public CourseVO(String courseCode, String courseName, Integer credits, String department, 
                    String semester, String classTime, String location, Integer capacity) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.semester = semester;
        this.classTime = classTime;
        this.location = location;
        this.capacity = capacity != null ? capacity : 50; // 默认容量
        this.enrolledCount = 0; // 默认已选人数为0
        this.status = "planning"; // 默认计划中
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
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public String getClassTime() {
        return classTime;
    }
    
    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPrerequisites() {
        return prerequisites;
    }
    
    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }
    
    public String getSyllabus() {
        return syllabus;
    }
    
    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public TeacherVO getTeacher() {
        return teacher;
    }
    
    public void setTeacher(TeacherVO teacher) {
        this.teacher = teacher;
    }
    
    public List<StudentVO> getEnrolledStudents() {
        return enrolledStudents;
    }
    
    public void setEnrolledStudents(List<StudentVO> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
    
    public List<CourseScheduleVO> getSchedules() {
        return schedules;
    }
    
    public void setSchedules(List<CourseScheduleVO> schedules) {
        this.schedules = schedules;
    }
    
    public List<CourseResourceVO> getResources() {
        return resources;
    }
    
    public void setResources(List<CourseResourceVO> resources) {
        this.resources = resources;
    }
    
    public List<EnrollmentVO> getEnrollments() {
        return enrollments;
    }
    
    public void setEnrollments(List<EnrollmentVO> enrollments) {
        this.enrollments = enrollments;
    }
    
    // 业务方法
    
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
     * 检查课程是否激活
     * @return true表示激活，false表示非激活
     */
    public boolean isActive() {
        return "active".equals(status);
    }
    
    /**
     * 检查课程是否已完成
     * @return true表示已完成，false表示未完成
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    /**
     * 检查课程是否在计划中
     * @return true表示在计划中，false表示不在计划中
     */
    public boolean isPlanning() {
        return "planning".equals(status);
    }
    
    /**
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case "planning": return "计划中";
            case "active": return "激活";
            case "inactive": return "停用";
            case "completed": return "已完成";
            default: return "未知";
        }
    }
    
    /**
     * 获取课程资源数量
     * @return 资源数量
     */
    public int getResourceCount() {
        return resources != null ? resources.size() : 0;
    }
    
    /**
     * 获取课程时间安排数量
     * @return 时间安排数量
     */
    public int getScheduleCount() {
        return schedules != null ? schedules.size() : 0;
    }
    
    /**
     * 检查是否有先修课程要求
     * @return true表示有先修要求，false表示无先修要求
     */
    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.trim().isEmpty();
    }
    
    /**
     * 获取课程的完整信息
     * @return 格式化的课程信息
     */
    public String getFullCourseInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(courseCode).append(" - ").append(courseName);
        if (teacherName != null) {
            sb.append(" (").append(teacherName).append(")");
        }
        sb.append(" - ").append(credits).append("学分");
        if (semester != null) {
            sb.append(" - ").append(semester);
        }
        return sb.toString();
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
                ", academicYear='" + academicYear + '\'' +
                ", classTime='" + classTime + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", enrolledCount=" + enrolledCount +
                ", status='" + status + '\'' +
                ", resourceCount=" + getResourceCount() +
                ", scheduleCount=" + getScheduleCount() +
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
