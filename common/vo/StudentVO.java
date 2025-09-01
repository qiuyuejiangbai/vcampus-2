package common.vo;

import java.io.Serializable;

/**
 * 学生值对象
 * 用于封装学生信息在客户端和服务器端之间传输
 */
public class StudentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer studentId;      // 学生ID
    private Integer userId;         // 关联的用户ID
    private String studentNo;       // 学号
    private String major;           // 专业
    private String className;       // 班级
    private String grade;           // 年级
    private Integer enrollmentYear; // 入学年份
    
    // 关联的用户信息（用于显示）
    private UserVO userInfo;
    
    public StudentVO() {}
    
    public StudentVO(Integer userId, String studentNo, String major, String className, String grade, Integer enrollmentYear) {
        this.userId = userId;
        this.studentNo = studentNo;
        this.major = major;
        this.className = className;
        this.grade = grade;
        this.enrollmentYear = enrollmentYear;
    }
    
    // Getters and Setters
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getStudentNo() {
        return studentNo;
    }
    
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }
    
    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }
    
    public UserVO getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserVO userInfo) {
        this.userInfo = userInfo;
    }
    
    /**
     * 获取学生姓名（从关联的用户信息中获取）
     * @return 学生姓名
     */
    public String getName() {
        return userInfo != null ? userInfo.getName() : null;
    }
    
    /**
     * 获取联系电话（从关联的用户信息中获取）
     * @return 联系电话
     */
    public String getPhone() {
        return userInfo != null ? userInfo.getPhone() : null;
    }
    
    /**
     * 获取邮箱（从关联的用户信息中获取）
     * @return 邮箱
     */
    public String getEmail() {
        return userInfo != null ? userInfo.getEmail() : null;
    }
    
    @Override
    public String toString() {
        return "StudentVO{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", studentNo='" + studentNo + '\'' +
                ", major='" + major + '\'' +
                ", className='" + className + '\'' +
                ", grade='" + grade + '\'' +
                ", enrollmentYear=" + enrollmentYear +
                ", userInfo=" + userInfo +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StudentVO studentVO = (StudentVO) obj;
        return studentId != null && studentId.equals(studentVO.studentId);
    }
    
    @Override
    public int hashCode() {
        return studentId != null ? studentId.hashCode() : 0;
    }
}
