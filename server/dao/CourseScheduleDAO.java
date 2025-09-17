package server.dao;

import common.vo.CourseScheduleVO;
import java.util.List;

/**
 * 课程时间表数据访问接口
 * 定义课程时间表相关的数据库操作方法
 */
public interface CourseScheduleDAO extends BaseDAO<CourseScheduleVO, Integer> {
    
    /**
     * 根据课程ID查询课程时间表
     * @param courseId 课程ID
     * @return 课程时间表列表
     */
    List<CourseScheduleVO> findByCourseId(Integer courseId);
    
    /**
     * 根据多个课程ID查询课程时间表
     * @param courseIds 课程ID列表
     * @return 课程时间表列表
     */
    List<CourseScheduleVO> findByCourseIds(List<Integer> courseIds);
    
    /**
     * 根据星期几查询课程时间表
     * @param dayOfWeek 星期几(1-7, 1=周一)
     * @return 课程时间表列表
     */
    List<CourseScheduleVO> findByDayOfWeek(Integer dayOfWeek);
    
    /**
     * 根据教室查询课程时间表
     * @param classroom 教室
     * @return 课程时间表列表
     */
    List<CourseScheduleVO> findByClassroom(String classroom);
    
    /**
     * 检查时间冲突
     * @param dayOfWeek 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param classroom 教室
     * @param excludeScheduleId 排除的时间表ID（用于更新时排除自己）
     * @return 有冲突返回true，无冲突返回false
     */
    boolean hasTimeConflict(Integer dayOfWeek, java.sql.Time startTime, java.sql.Time endTime, 
                          String classroom, Integer excludeScheduleId);
    
    /**
     * 根据课程ID删除所有时间表
     * @param courseId 课程ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteByCourseId(Integer courseId);
}
