package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 教师值对象
 * 用于封装教师信息在客户端和服务器端之间传输
 */
public class TeacherVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;             // 教师ID
    private Integer userId;         // 关联User表的id
    private String name;            // 姓名
    private String teacherNo;       // 工号
    private String phone;           // 联系方式
    private String email;           // 邮箱
    private String department;      // 院系
    private String title;           // 职称
    private String office;          // 办公室
    private String researchArea;    // 研究方向
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    // 关联对象
    private UserVO user;            // 关联的UserVO对象
    private List<CourseVO> teachingCourses; // 教授课程列表
    private List<GradeVO> gradingRecords;   // 成绩记录列表
    
    // 构造函数
    public TeacherVO() {}
    
    public TeacherVO(String name, String teacherNo, String phone, String email, String department) {
        this.name = name;
        this.teacherNo = teacherNo;
        this.phone = phone;
        this.email = email;
        this.department = department;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTeacherNo() {
        return teacherNo;
    }
    
    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getOffice() {
        return office;
    }
    
    public void setOffice(String office) {
        this.office = office;
    }
    
    public String getResearchArea() {
        return researchArea;
    }
    
    public void setResearchArea(String researchArea) {
        this.researchArea = researchArea;
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
    
    public UserVO getUser() {
        return user;
    }
    
    public void setUser(UserVO user) {
        this.user = user;
    }
    
    public List<CourseVO> getTeachingCourses() {
        return teachingCourses;
    }
    
    public void setTeachingCourses(List<CourseVO> teachingCourses) {
        this.teachingCourses = teachingCourses;
    }
    
    public List<GradeVO> getGradingRecords() {
        return gradingRecords;
    }
    
    public void setGradingRecords(List<GradeVO> gradingRecords) {
        this.gradingRecords = gradingRecords;
    }
    
    // 业务方法
    
    /**
     * 获取教授课程数量
     * @return 教授课程数量
     */
    public int getTeachingCourseCount() {
        return teachingCourses != null ? teachingCourses.size() : 0;
    }
    
    /**
     * 获取当前活跃课程数量
     * @return 活跃课程数量
     */
    public int getActiveCourseCount() {
        if (teachingCourses == null) return 0;
        return (int) teachingCourses.stream()
                .filter(course -> "active".equals(course.getStatus()))
                .count();
    }
    
    /**
     * 获取所有课程的学生总数
     * @return 学生总数
     */
    public int getTotalStudents() {
        if (teachingCourses == null) return 0;
        return teachingCourses.stream()
                .mapToInt(course -> course.getEnrolledCount() != null ? course.getEnrolledCount() : 0)
                .sum();
    }
    
    /**
     * 检查是否教授某门课程
     * @param courseId 课程ID
     * @return true表示教授该课程，false表示不教授
     */
    public boolean isTeachingCourse(Integer courseId) {
        if (teachingCourses == null) return false;
        return teachingCourses.stream()
                .anyMatch(course -> course.getCourseId().equals(courseId));
    }
    
    /**
     * 获取本学期教授的课程数量
     * @param semester 学期
     * @return 本学期课程数量
     */
    public int getCurrentSemesterCourseCount(String semester) {
        if (teachingCourses == null || semester == null) return 0;
        return (int) teachingCourses.stream()
                .filter(course -> semester.equals(course.getSemester()))
                .count();
    }
    
    /**
     * 获取完整姓名和职称
     * @return 姓名和职称的组合
     */
    public String getFullTitle() {
        if (title != null && !title.isEmpty()) {
            return name + " " + title;
        }
        return name;
    }
    
    @Override
    public String toString() {
        return "TeacherVO{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", teacherNo='" + teacherNo + '\'' +
                ", department='" + department + '\'' +
                ", title='" + title + '\'' +
                ", office='" + office + '\'' +
                ", teachingCourseCount=" + getTeachingCourseCount() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TeacherVO teacherVO = (TeacherVO) obj;
        return id != null && id.equals(teacherVO.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
