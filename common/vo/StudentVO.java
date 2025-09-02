package common.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * 学生值对象
 * 用于封装学生信息在客户端和服务器端之间传输
 */
public class StudentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;             // 学生ID
    private Integer userId;         // 关联User表的id
    private String name;            // 姓名
    private String studentNo;       // 学号
    private String gender;          // 性别：male, female, other
    private Date birthDate;         // 出生日期
    private String phone;           // 联系方式
    private String email;           // 邮箱
    private String address;         // 家庭地址
    private String department;      // 院系
    private String className;       // 所属班级
    private String major;           // 专业
    private String grade;           // 年级
    private String gradeTableKey;   // 成绩数据表对应的键
    private BigDecimal balance;     // 账户余额
    private Integer enrollmentYear; // 入学年份
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    // 关联对象
    private UserVO user;            // 关联的UserVO对象
    private List<CourseVO> enrolledCourses; // 已选课程列表
    private List<EnrollmentVO> enrollments; // 选课记录列表
    private List<GradeVO> grades;   // 成绩列表
    
    public StudentVO() {}
    
    public StudentVO(String name, String studentNo, String gender, String phone, String email, 
                     String department, String className, String major) {
        this.name = name;
        this.studentNo = studentNo;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.className = className;
        this.major = major;
        this.balance = BigDecimal.ZERO;
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
    
    public String getStudentNo() {
        return studentNo;
    }
    
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public Date getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public String getGradeTableKey() {
        return gradeTableKey;
    }
    
    public void setGradeTableKey(String gradeTableKey) {
        this.gradeTableKey = gradeTableKey;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }
    
    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
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
    
    // 别名方法，用于兼容性
    public Integer getStudentId() {
        return this.id;
    }
    
    public void setStudentId(Integer studentId) {
        this.id = studentId;
    }
    
    public void setUserInfo(UserVO user) {
        this.user = user;
    }
    
    public List<CourseVO> getEnrolledCourses() {
        return enrolledCourses;
    }
    
    public void setEnrolledCourses(List<CourseVO> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }
    
    public List<EnrollmentVO> getEnrollments() {
        return enrollments;
    }
    
    public void setEnrollments(List<EnrollmentVO> enrollments) {
        this.enrollments = enrollments;
    }
    
    public List<GradeVO> getGrades() {
        return grades;
    }
    
    public void setGrades(List<GradeVO> grades) {
        this.grades = grades;
    }
    
    // 业务方法
    
    /**
     * 获取已选课程数量
     * @return 已选课程数量
     */
    public int getEnrolledCourseCount() {
        return enrolledCourses != null ? enrolledCourses.size() : 0;
    }
    
    /**
     * 获取有效选课记录数量
     * @return 有效选课记录数量
     */
    public int getActiveEnrollmentCount() {
        if (enrollments == null) return 0;
        return (int) enrollments.stream()
                .filter(enrollment -> "enrolled".equals(enrollment.getStatusName()))
                .count();
    }
    
    /**
     * 计算GPA
     * @return GPA值
     */
    public BigDecimal getGPA() {
        if (grades == null || grades.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        
        for (GradeVO grade : grades) {
            if (grade.getGradePoint() != null && grade.getCredits() != null) {
                totalPoints = totalPoints.add(grade.getGradePoint().multiply(new BigDecimal(grade.getCredits())));
                totalCredits = totalCredits.add(new BigDecimal(grade.getCredits()));
            }
        }
        
        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return totalPoints.divide(totalCredits, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 获取总学分
     * @return 总学分
     */
    public int getTotalCredits() {
        if (grades == null) return 0;
        return grades.stream()
                .filter(grade -> grade.getCredits() != null)
                .mapToInt(grade -> grade.getCredits())
                .sum();
    }
    
    /**
     * 检查是否已选择某门课程
     * @param courseId 课程ID
     * @return true表示已选择，false表示未选择
     */
    public boolean isEnrolledInCourse(Integer courseId) {
        if (enrolledCourses == null) return false;
        return enrolledCourses.stream()
                .anyMatch(course -> course.getCourseId().equals(courseId));
    }
    
    /**
     * 获取性别中文名称
     * @return 性别中文名称
     */
    public String getGenderName() {
        if (gender == null) return "未知";
        switch (gender) {
            case "male": return "男";
            case "female": return "女";
            case "other": return "其他";
            default: return "未知";
        }
    }
    
    @Override
    public String toString() {
        return "StudentVO{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", gender='" + gender + '\'' +
                ", department='" + department + '\'' +
                ", className='" + className + '\'' +
                ", major='" + major + '\'' +
                ", balance=" + balance +
                ", enrollmentYear=" + enrollmentYear +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StudentVO studentVO = (StudentVO) obj;
        return id != null && id.equals(studentVO.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
