package server.service;

import common.vo.EnrollmentVO;
import server.dao.EnrollmentDAO;
import server.dao.impl.EnrollmentDAOImpl;

import java.util.List;

/**
 * 选课记录服务类
 * 处理选课记录相关的业务逻辑
 */
public class EnrollmentService {
    private final EnrollmentDAO enrollmentDAO;
    
    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAOImpl();
    }
    
    /**
     * 获取所有选课记录
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }
    
    /**
     * 根据学生ID获取选课记录
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getEnrollmentsByStudentId(Integer studentId) {
        if (studentId == null) return null;
        return enrollmentDAO.findByStudentId(studentId);
    }
    
    /**
     * 根据课程ID获取选课记录
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getEnrollmentsByCourseId(Integer courseId) {
        if (courseId == null) return null;
        return enrollmentDAO.findByCourseId(courseId);
    }
    
    /**
     * 根据学生ID和课程ID获取选课记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录，不存在返回null
     */
    public EnrollmentVO getEnrollmentByStudentAndCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return null;
        return enrollmentDAO.findByStudentIdAndCourseId(studentId, courseId);
    }
    
    /**
     * 检查学生是否已选某课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 已选返回true，未选返回false
     */
    public boolean isEnrolled(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return false;
        return enrollmentDAO.isEnrolled(studentId, courseId);
    }
    
    /**
     * 根据状态获取选课记录
     * @param status 状态：0-已退课，1-已选课，2-已完成
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getEnrollmentsByStatus(Integer status) {
        return enrollmentDAO.findByStatus(status);
    }
    
    /**
     * 根据学生ID和状态获取选课记录
     * @param studentId 学生ID
     * @param status 状态
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getEnrollmentsByStudentAndStatus(Integer studentId, Integer status) {
        if (studentId == null) return null;
        return enrollmentDAO.findByStudentIdAndStatus(studentId, status);
    }
    
    /**
     * 根据课程ID和状态获取选课记录
     * @param courseId 课程ID
     * @param status 状态
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getEnrollmentsByCourseAndStatus(Integer courseId, Integer status) {
        if (courseId == null) return null;
        return enrollmentDAO.findByCourseIdAndStatus(courseId, status);
    }
    
    /**
     * 获取学生成绩单
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getTranscriptByStudentId(Integer studentId) {
        if (studentId == null) return null;
        return enrollmentDAO.getTranscriptByStudentId(studentId);
    }
    
    /**
     * 获取课程学生名单
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    public List<EnrollmentVO> getStudentListByCourseId(Integer courseId) {
        if (courseId == null) return null;
        return enrollmentDAO.getStudentListByCourseId(courseId);
    }
    
    /**
     * 更新成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param grade 成绩
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateGrade(Integer studentId, Integer courseId, Double grade) {
        if (studentId == null || courseId == null || grade == null) return false;
        return enrollmentDAO.updateGrade(studentId, courseId, grade);
    }
    
    /**
     * 更新选课状态
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateStatus(Integer studentId, Integer courseId, Integer status) {
        if (studentId == null || courseId == null || status == null) return false;
        return enrollmentDAO.updateStatus(studentId, courseId, status);
    }
    
    /**
     * 退课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 退课成功返回true，失败返回false
     */
    public boolean dropCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return false;
        return enrollmentDAO.dropCourse(studentId, courseId);
    }
    
    /**
     * 统计学生当前选课数量
     * @param studentId 学生ID
     * @return 选课数量
     */
    public int countActiveEnrollmentsByStudentId(Integer studentId) {
        if (studentId == null) return 0;
        return enrollmentDAO.countActiveEnrollmentsByStudentId(studentId);
    }
    
    /**
     * 统计课程当前选课人数
     * @param courseId 课程ID
     * @return 选课人数
     */
    public int countActiveEnrollmentsByCourseId(Integer courseId) {
        if (courseId == null) return 0;
        return enrollmentDAO.countActiveEnrollmentsByCourseId(courseId);
    }
    
    /**
     * 学生选课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课成功返回true，失败返回false
     */
    public boolean enrollCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return false;
        
        // 检查是否已经选过这门课
        if (isEnrolled(studentId, courseId)) {
            System.out.println("学生 " + studentId + " 已经选过课程 " + courseId);
            return false;
        }
        
        try {
            // 获取课程信息
            server.dao.CourseDAO courseDAO = new server.dao.impl.CourseDAOImpl();
            common.vo.CourseVO course = courseDAO.findById(courseId);
            if (course == null) {
                System.out.println("课程不存在: " + courseId);
                return false;
            }
            
            // 获取学生信息
            server.dao.StudentDAO studentDAO = new server.dao.impl.StudentDAOImpl();
            common.vo.StudentVO student = studentDAO.findById(studentId);
            if (student == null) {
                System.out.println("学生不存在: " + studentId);
                return false;
            }
            
            // 创建选课记录
            EnrollmentVO enrollment = new EnrollmentVO();
            enrollment.setStudentId(studentId);
            enrollment.setCourseId(courseId);
            enrollment.setStatus("enrolled"); // enrolled表示已选课
            enrollment.setEnrollmentTime(new java.sql.Timestamp(System.currentTimeMillis()));
            
            // 设置学期和学年（从课程信息中获取）
            enrollment.setSemester(course.getSemester() != null ? course.getSemester() : "2024-1");
            enrollment.setAcademicYear(course.getAcademicYear() != null ? course.getAcademicYear() : "2024-2025");
            
            // 设置关联信息（用于显示）
            enrollment.setStudentName(student.getName());
            enrollment.setStudentNo(student.getStudentNo());
            enrollment.setCourseName(course.getCourseName());
            enrollment.setCourseCode(course.getCourseCode());
            enrollment.setCredits(course.getCredits());
            enrollment.setTeacherName(course.getTeacherName());
            
            // 插入选课记录
            Integer enrollmentId = enrollmentDAO.insert(enrollment);
            if (enrollmentId != null) {
                System.out.println("选课记录创建成功，ID: " + enrollmentId);
                return true;
            } else {
                System.out.println("选课记录创建失败");
                return false;
            }
        } catch (Exception e) {
            System.err.println("选课过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
