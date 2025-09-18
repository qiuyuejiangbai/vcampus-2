package server.dao;

import common.vo.GradeVO;
import java.util.List;

/**
 * 成绩数据访问接口
 * 定义成绩相关的数据库操作方法
 */
public interface GradeDAO extends BaseDAO<GradeVO, Integer> {
    
    /**
     * 根据学生ID查询成绩列表
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<GradeVO> findByStudentId(Integer studentId);
    
    /**
     * 根据课程ID查询成绩列表
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<GradeVO> findByCourseId(Integer courseId);
    
    /**
     * 根据教师ID查询成绩列表
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    List<GradeVO> findByTeacherId(Integer teacherId);
    
    /**
     * 根据学期查询成绩列表
     * @param semester 学期
     * @return 成绩列表
     */
    List<GradeVO> findBySemester(String semester);
    
    /**
     * 查询所有成绩（包含关联信息）
     * @return 成绩列表
     */
    List<GradeVO> findAllWithDetails();
    
    /**
     * 根据学生ID和课程ID查询成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象，不存在返回null
     */
    GradeVO findByStudentAndCourse(Integer studentId, Integer courseId);
    
    /**
     * 根据选课记录ID查询成绩
     * @param enrollmentId 选课记录ID
     * @return 成绩对象，不存在返回null
     */
    GradeVO findByEnrollmentId(Integer enrollmentId);
    
    /**
     * 检查成绩是否存在
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 存在返回true，不存在返回false
     */
    boolean existsByStudentAndCourse(Integer studentId, Integer courseId);
    
    /**
     * 根据成绩等级查询成绩列表
     * @param gradeLevel 成绩等级
     * @return 成绩列表
     */
    List<GradeVO> findByGradeLevel(String gradeLevel);
    
    /**
     * 统计学生已评分课程数量
     * @param studentId 学生ID
     * @return 已评分课程数量
     */
    int countGradedCoursesByStudentId(Integer studentId);
    
    /**
     * 统计课程已评分学生数量
     * @param courseId 课程ID
     * @return 已评分学生数量
     */
    int countGradedStudentsByCourseId(Integer courseId);
}
