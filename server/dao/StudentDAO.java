package server.dao;

import common.vo.StudentVO;
import java.util.List;

/**
 * 学生数据访问接口
 * 定义学生相关的数据库操作方法
 */
public interface StudentDAO extends BaseDAO<StudentVO, Integer> {
    
    /**
     * 根据用户ID查询学生信息
     * @param userId 用户ID
     * @return 学生对象，不存在返回null
     */
    StudentVO findByUserId(Integer userId);
    
    /**
     * 根据学号查询学生信息
     * @param studentNo 学号
     * @return 学生对象，不存在返回null
     */
    StudentVO findByStudentNo(String studentNo);
    
    /**
     * 检查学号是否存在
     * @param studentNo 学号
     * @return 存在返回true，不存在返回false
     */
    boolean existsByStudentNo(String studentNo);
    
    /**
     * 根据专业查询学生列表
     * @param major 专业
     * @return 学生列表
     */
    List<StudentVO> findByMajor(String major);
    
    /**
     * 根据班级查询学生列表
     * @param className 班级
     * @return 学生列表
     */
    List<StudentVO> findByClassName(String className);
    
    /**
     * 根据年级查询学生列表
     * @param grade 年级
     * @return 学生列表
     */
    List<StudentVO> findByGrade(String grade);
    
    /**
     * 根据入学年份查询学生列表
     * @param enrollmentYear 入学年份
     * @return 学生列表
     */
    List<StudentVO> findByEnrollmentYear(Integer enrollmentYear);
    
    /**
     * 查询所有学生信息（包含用户信息）
     * @return 学生列表
     */
    List<StudentVO> findAllWithUserInfo();
    
    /**
     * 根据学生ID查询学生信息（包含用户信息）
     * @param studentId 学生ID
     * @return 学生对象，不存在返回null
     */
    StudentVO findByIdWithUserInfo(Integer studentId);
    
    /**
     * 根据姓名模糊查询学生列表
     * @param name 姓名关键词
     * @return 学生列表
     */
    List<StudentVO> findByNameLike(String name);
}
