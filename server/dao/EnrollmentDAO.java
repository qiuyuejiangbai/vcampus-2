package server.dao;

import common.vo.EnrollmentVO;
import java.util.List;

/**
 * 选课记录数据访问接口
 * 定义选课记录相关的数据库操作方法
 */
public interface EnrollmentDAO extends BaseDAO<EnrollmentVO, Integer> {
    
    /**
     * 根据学生ID查询选课记录
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<EnrollmentVO> findByStudentId(Integer studentId);
    
    /**
     * 根据课程ID查询选课记录
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    List<EnrollmentVO> findByCourseId(Integer courseId);
    
    /**
     * 根据学生ID和课程ID查询选课记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录，不存在返回null
     */
    EnrollmentVO findByStudentIdAndCourseId(Integer studentId, Integer courseId);
    
    /**
     * 检查学生是否已选某课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 已选返回true，未选返回false
     */
    boolean isEnrolled(Integer studentId, Integer courseId);
    
    /**
     * 根据状态查询选课记录
     * @param status 状态：0-已退课，1-已选课，2-已完成
     * @return 选课记录列表
     */
    List<EnrollmentVO> findByStatus(Integer status);
    
    /**
     * 根据学生ID和状态查询选课记录
     * @param studentId 学生ID
     * @param status 状态
     * @return 选课记录列表
     */
    List<EnrollmentVO> findByStudentIdAndStatus(Integer studentId, Integer status);
    
    /**
     * 根据课程ID和状态查询选课记录
     * @param courseId 课程ID
     * @param status 状态
     * @return 选课记录列表
     */
    List<EnrollmentVO> findByCourseIdAndStatus(Integer courseId, Integer status);
    
    /**
     * 查询学生的成绩单（包含课程信息）
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<EnrollmentVO> getTranscriptByStudentId(Integer studentId);
    
    /**
     * 查询课程的学生名单（包含学生信息）
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    List<EnrollmentVO> getStudentListByCourseId(Integer courseId);
    
    /**
     * 更新成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param grade 成绩
     * @return 更新成功返回true，失败返回false
     */
    boolean updateGrade(Integer studentId, Integer courseId, Double grade);
    
    /**
     * 更新选课状态
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Integer studentId, Integer courseId, Integer status);
    
    /**
     * 退课（将状态改为已退课）
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 退课成功返回true，失败返回false
     */
    boolean dropCourse(Integer studentId, Integer courseId);
    
    /**
     * 统计学生当前选课数量（状态为已选课或已完成）
     * @param studentId 学生ID
     * @return 选课数量
     */
    int countActiveEnrollmentsByStudentId(Integer studentId);
    
    /**
     * 统计课程当前选课人数（状态为已选课或已完成）
     * @param courseId 课程ID
     * @return 选课人数
     */
    int countActiveEnrollmentsByCourseId(Integer courseId);
}
