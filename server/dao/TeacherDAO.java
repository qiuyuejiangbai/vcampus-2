package server.dao;

import common.vo.TeacherVO;
import java.util.List;

/**
 * 教师数据访问接口
 * 定义教师相关的数据库操作方法
 */
public interface TeacherDAO extends BaseDAO<TeacherVO, Integer> {
    
    /**
     * 根据用户ID查询教师信息
     * @param userId 用户ID
     * @return 教师对象，不存在返回null
     */
    TeacherVO findByUserId(Integer userId);
    
    /**
     * 根据工号查询教师信息
     * @param teacherNo 工号
     * @return 教师对象，不存在返回null
     */
    TeacherVO findByTeacherNo(String teacherNo);
    
    /**
     * 检查工号是否存在
     * @param teacherNo 工号
     * @return 存在返回true，不存在返回false
     */
    boolean existsByTeacherNo(String teacherNo);
    
    /**
     * 根据院系查询教师列表
     * @param department 院系
     * @return 教师列表
     */
    List<TeacherVO> findByDepartment(String department);
    
    /**
     * 根据职称查询教师列表
     * @param title 职称
     * @return 教师列表
     */
    List<TeacherVO> findByTitle(String title);
    
    /**
     * 查询所有教师信息（包含用户信息）
     * @return 教师列表
     */
    List<TeacherVO> findAllWithUserInfo();
    
    /**
     * 根据教师ID查询教师信息（包含用户信息）
     * @param teacherId 教师ID
     * @return 教师对象，不存在返回null
     */
    TeacherVO findByIdWithUserInfo(Integer teacherId);
    
    /**
     * 根据姓名模糊查询教师列表
     * @param name 姓名关键词
     * @return 教师列表
     */
    List<TeacherVO> findByNameLike(String name);
    
    /**
     * 获取教师的课程数量
     * @param teacherId 教师ID
     * @return 课程数量
     */
    int getCourseCount(Integer teacherId);
    
    /**
     * 获取教师的学生数量
     * @param teacherId 教师ID
     * @return 学生数量
     */
    int getStudentCount(Integer teacherId);
}
