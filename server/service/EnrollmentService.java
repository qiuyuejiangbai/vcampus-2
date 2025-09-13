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
}
