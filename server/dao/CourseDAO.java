package server.dao;

import common.vo.CourseVO;
import java.util.List;

/**
 * 课程数据访问接口
 * 定义课程相关的数据库操作方法
 */
public interface CourseDAO extends BaseDAO<CourseVO, Integer> {
    
    /**
     * 根据课程代码查询课程
     * @param courseCode 课程代码
     * @return 课程对象，不存在返回null
     */
    CourseVO findByCourseCode(String courseCode);
    
    /**
     * 检查课程代码是否存在
     * @param courseCode 课程代码
     * @return 存在返回true，不存在返回false
     */
    boolean existsByCourseCode(String courseCode);
    
    /**
     * 根据教师ID查询课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<CourseVO> findByTeacherId(Integer teacherId);
    
    /**
     * 根据院系查询课程列表
     * @param department 院系
     * @return 课程列表
     */
    List<CourseVO> findByDepartment(String department);
    
    /**
     * 根据学期查询课程列表
     * @param semester 学期
     * @return 课程列表
     */
    List<CourseVO> findBySemester(String semester);
    
    /**
     * 根据状态查询课程列表
     * @param status 状态：0-停用，1-启用
     * @return 课程列表
     */
    List<CourseVO> findByStatus(Integer status);
    
    /**
     * 根据课程名称模糊查询
     * @param courseName 课程名称关键词
     * @return 课程列表
     */
    List<CourseVO> findByNameLike(String courseName);
    
    /**
     * 查询所有启用的课程
     * @return 启用的课程列表
     */
    List<CourseVO> findAllEnabled();
    
    /**
     * 查询所有课程（包含教师姓名）
     * @return 课程列表
     */
    List<CourseVO> findAllWithTeacherName();
    
    /**
     * 根据课程ID查询课程（包含教师姓名）
     * @param courseId 课程ID
     * @return 课程对象，不存在返回null
     */
    CourseVO findByIdWithTeacherName(Integer courseId);
    
    /**
     * 更新课程选课人数
     * @param courseId 课程ID
     * @param enrolledCount 选课人数
     * @return 更新成功返回true，失败返回false
     */
    boolean updateEnrolledCount(Integer courseId, Integer enrolledCount);
    
    /**
     * 增加课程选课人数
     * @param courseId 课程ID
     * @param increment 增加数量（可为负数表示减少）
     * @return 更新成功返回true，失败返回false
     */
    boolean incrementEnrolledCount(Integer courseId, Integer increment);
}
