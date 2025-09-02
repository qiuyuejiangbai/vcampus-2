package common.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 成绩值对象
 * 用于封装学生成绩信息在客户端和服务器端之间传输
 */
public class GradeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;                 // 成绩ID
    private Integer enrollmentId;       // 选课记录ID
    private Integer studentId;          // 学生ID
    private Integer courseId;           // 课程ID
    private Integer teacherId;          // 任课教师ID
    private String semester;            // 学期
    private BigDecimal midtermGrade;    // 期中成绩
    private BigDecimal finalGrade;      // 期末成绩
    private BigDecimal assignmentGrade; // 作业成绩
    private BigDecimal attendanceGrade; // 考勤成绩
    private BigDecimal totalGrade;      // 总成绩
    private BigDecimal gradePoint;      // 绩点
    private String gradeLevel;          // 等级(A+,A,B+,B,C+,C,D,F)
    private Boolean isRetake;           // 是否重修
    private String comments;            // 教师评语
    private Timestamp gradedTime;       // 评分时间
    private Timestamp createdTime;      // 创建时间
    private Timestamp updatedTime;      // 更新时间
    
    // 关联信息（用于显示）
    private String studentName;         // 学生姓名
    private String studentNo;           // 学号
    private String courseName;          // 课程名称
    private String courseCode;          // 课程代码
    private Integer credits;            // 学分
    private String teacherName;         // 教师姓名
    
    // 关联对象
    private StudentVO student;
    private CourseVO course;
    private TeacherVO teacher;
    private EnrollmentVO enrollment;
    
    // 构造函数
    public GradeVO() {}
    
    public GradeVO(Integer enrollmentId, Integer studentId, Integer courseId, Integer teacherId, String semester) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.semester = semester;
        this.isRetake = false; // 默认不是重修
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
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
    
    public Integer getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public BigDecimal getMidtermGrade() {
        return midtermGrade;
    }
    
    public void setMidtermGrade(BigDecimal midtermGrade) {
        this.midtermGrade = midtermGrade;
    }
    
    public BigDecimal getFinalGrade() {
        return finalGrade;
    }
    
    public void setFinalGrade(BigDecimal finalGrade) {
        this.finalGrade = finalGrade;
    }
    
    public BigDecimal getAssignmentGrade() {
        return assignmentGrade;
    }
    
    public void setAssignmentGrade(BigDecimal assignmentGrade) {
        this.assignmentGrade = assignmentGrade;
    }
    
    public BigDecimal getAttendanceGrade() {
        return attendanceGrade;
    }
    
    public void setAttendanceGrade(BigDecimal attendanceGrade) {
        this.attendanceGrade = attendanceGrade;
    }
    
    public BigDecimal getTotalGrade() {
        return totalGrade;
    }
    
    public void setTotalGrade(BigDecimal totalGrade) {
        this.totalGrade = totalGrade;
    }
    
    public BigDecimal getGradePoint() {
        return gradePoint;
    }
    
    public void setGradePoint(BigDecimal gradePoint) {
        this.gradePoint = gradePoint;
    }
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public Boolean getIsRetake() {
        return isRetake;
    }
    
    public void setIsRetake(Boolean isRetake) {
        this.isRetake = isRetake;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Timestamp getGradedTime() {
        return gradedTime;
    }
    
    public void setGradedTime(Timestamp gradedTime) {
        this.gradedTime = gradedTime;
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
    
    // 关联信息的 Getters and Setters
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
    
    // 关联对象的 Getters and Setters
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
    
    public TeacherVO getTeacher() {
        return teacher;
    }
    
    public void setTeacher(TeacherVO teacher) {
        this.teacher = teacher;
    }
    
    public EnrollmentVO getEnrollment() {
        return enrollment;
    }
    
    public void setEnrollment(EnrollmentVO enrollment) {
        this.enrollment = enrollment;
    }
    
    // 业务方法
    
    /**
     * 自动计算总成绩
     * 默认权重：期中30%，期末50%，作业15%，考勤5%
     */
    public void calculateTotalGrade() {
        calculateTotalGrade(0.3, 0.5, 0.15, 0.05);
    }
    
    /**
     * 根据指定权重计算总成绩
     * @param midtermWeight 期中权重
     * @param finalWeight 期末权重
     * @param assignmentWeight 作业权重
     * @param attendanceWeight 考勤权重
     */
    public void calculateTotalGrade(double midtermWeight, double finalWeight, 
                                   double assignmentWeight, double attendanceWeight) {
        BigDecimal total = BigDecimal.ZERO;
        
        if (midtermGrade != null) {
            total = total.add(midtermGrade.multiply(new BigDecimal(midtermWeight)));
        }
        if (finalGrade != null) {
            total = total.add(finalGrade.multiply(new BigDecimal(finalWeight)));
        }
        if (assignmentGrade != null) {
            total = total.add(assignmentGrade.multiply(new BigDecimal(assignmentWeight)));
        }
        if (attendanceGrade != null) {
            total = total.add(attendanceGrade.multiply(new BigDecimal(attendanceWeight)));
        }
        
        this.totalGrade = total;
        calculateGradePoint();
        calculateGradeLevel();
        this.gradedTime = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * 根据总成绩计算绩点
     */
    private void calculateGradePoint() {
        if (totalGrade == null) return;
        
        double score = totalGrade.doubleValue();
        if (score >= 95) {
            this.gradePoint = new BigDecimal("4.0");
        } else if (score >= 90) {
            this.gradePoint = new BigDecimal("3.7");
        } else if (score >= 85) {
            this.gradePoint = new BigDecimal("3.3");
        } else if (score >= 80) {
            this.gradePoint = new BigDecimal("3.0");
        } else if (score >= 75) {
            this.gradePoint = new BigDecimal("2.7");
        } else if (score >= 70) {
            this.gradePoint = new BigDecimal("2.3");
        } else if (score >= 65) {
            this.gradePoint = new BigDecimal("2.0");
        } else if (score >= 60) {
            this.gradePoint = new BigDecimal("1.0");
        } else {
            this.gradePoint = new BigDecimal("0.0");
        }
    }
    
    /**
     * 根据总成绩计算等级
     */
    private void calculateGradeLevel() {
        if (totalGrade == null) return;
        
        double score = totalGrade.doubleValue();
        if (score >= 95) {
            this.gradeLevel = "A+";
        } else if (score >= 90) {
            this.gradeLevel = "A";
        } else if (score >= 85) {
            this.gradeLevel = "B+";
        } else if (score >= 80) {
            this.gradeLevel = "B";
        } else if (score >= 75) {
            this.gradeLevel = "C+";
        } else if (score >= 70) {
            this.gradeLevel = "C";
        } else if (score >= 65) {
            this.gradeLevel = "D+";
        } else if (score >= 60) {
            this.gradeLevel = "D";
        } else {
            this.gradeLevel = "F";
        }
    }
    
    /**
     * 检查成绩是否及格
     * @return true表示及格，false表示不及格
     */
    public boolean isPassed() {
        return totalGrade != null && totalGrade.compareTo(new BigDecimal("60")) >= 0;
    }
    
    /**
     * 检查成绩是否优秀
     * @return true表示优秀，false表示不优秀
     */
    public boolean isExcellent() {
        return totalGrade != null && totalGrade.compareTo(new BigDecimal("90")) >= 0;
    }
    
    /**
     * 检查是否已评分
     * @return true表示已评分，false表示未评分
     */
    public boolean isGraded() {
        return totalGrade != null && gradedTime != null;
    }
    
    /**
     * 获取成绩状态
     * @return 成绩状态描述
     */
    public String getGradeStatus() {
        if (!isGraded()) return "未评分";
        if (isRetake != null && isRetake) return "重修";
        if (isExcellent()) return "优秀";
        if (isPassed()) return "及格";
        return "不及格";
    }
    
    /**
     * 获取完整的成绩信息
     * @return 格式化的成绩信息
     */
    public String getFormattedGrade() {
        if (totalGrade == null) return "未评分";
        return totalGrade.toString() + "分 (" + gradeLevel + ")";
    }
    
    @Override
    public String toString() {
        return "GradeVO{" +
                "id=" + id +
                ", studentName='" + studentName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", semester='" + semester + '\'' +
                ", totalGrade=" + totalGrade +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", gradePoint=" + gradePoint +
                ", isRetake=" + isRetake +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GradeVO gradeVO = (GradeVO) obj;
        return id != null && id.equals(gradeVO.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
