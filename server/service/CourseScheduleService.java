package server.service;

import common.vo.CourseScheduleVO;
import server.dao.CourseScheduleDAO;
import server.dao.impl.CourseScheduleDAOImpl;

import java.util.List;

/**
 * 课程时间表服务类
 * 处理课程时间表相关的业务逻辑
 */
public class CourseScheduleService {
    private final CourseScheduleDAO courseScheduleDAO;
    
    public CourseScheduleService() {
        this.courseScheduleDAO = new CourseScheduleDAOImpl();
    }
    
    /**
     * 获取所有课程时间表
     * @return 课程时间表列表
     */
    public List<CourseScheduleVO> getAllSchedules() {
        return courseScheduleDAO.findAll();
    }
    
    /**
     * 根据课程ID获取课程时间表
     * @param courseId 课程ID
     * @return 课程时间表列表
     */
    public List<CourseScheduleVO> getSchedulesByCourseId(Integer courseId) {
        if (courseId == null) return null;
        return courseScheduleDAO.findByCourseId(courseId);
    }
    
    /**
     * 根据多个课程ID获取课程时间表
     * @param courseIds 课程ID列表
     * @return 课程时间表列表
     */
    public List<CourseScheduleVO> getSchedulesByCourseIds(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return null;
        return courseScheduleDAO.findByCourseIds(courseIds);
    }
    
    /**
     * 根据星期几获取课程时间表
     * @param dayOfWeek 星期几(1-7, 1=周一)
     * @return 课程时间表列表
     */
    public List<CourseScheduleVO> getSchedulesByDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek == null) return null;
        return courseScheduleDAO.findByDayOfWeek(dayOfWeek);
    }
    
    /**
     * 根据教室获取课程时间表
     * @param classroom 教室
     * @return 课程时间表列表
     */
    public List<CourseScheduleVO> getSchedulesByClassroom(String classroom) {
        if (classroom == null || classroom.trim().isEmpty()) return null;
        return courseScheduleDAO.findByClassroom(classroom);
    }
    
    /**
     * 创建课程时间表
     * @param schedule 课程时间表信息
     * @return 创建成功返回true，失败返回false
     */
    public boolean createSchedule(CourseScheduleVO schedule) {
        if (schedule == null) return false;
        
        // 检查时间冲突
        if (courseScheduleDAO.hasTimeConflict(
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getClassroom(),
                null)) {
            System.err.println("时间冲突：该时间段和教室已被占用");
            return false;
        }
        
        Integer scheduleId = courseScheduleDAO.insert(schedule);
        if (scheduleId != null) {
            schedule.setId(scheduleId);
            return true;
        }
        return false;
    }
    
    /**
     * 更新课程时间表
     * @param schedule 课程时间表信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateSchedule(CourseScheduleVO schedule) {
        if (schedule == null || schedule.getId() == null) return false;
        
        // 检查时间冲突（排除自己）
        if (courseScheduleDAO.hasTimeConflict(
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getClassroom(),
                schedule.getId())) {
            System.err.println("时间冲突：该时间段和教室已被其他课程占用");
            return false;
        }
        
        return courseScheduleDAO.update(schedule);
    }
    
    /**
     * 删除课程时间表
     * @param scheduleId 时间表ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteSchedule(Integer scheduleId) {
        if (scheduleId == null) return false;
        return courseScheduleDAO.deleteById(scheduleId);
    }
    
    /**
     * 根据课程ID删除所有时间表
     * @param courseId 课程ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteSchedulesByCourseId(Integer courseId) {
        if (courseId == null) return false;
        return courseScheduleDAO.deleteByCourseId(courseId);
    }
    
    /**
     * 检查时间冲突
     * @param dayOfWeek 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param classroom 教室
     * @param excludeScheduleId 排除的时间表ID
     * @return 有冲突返回true，无冲突返回false
     */
    public boolean hasTimeConflict(Integer dayOfWeek, java.sql.Time startTime, 
                                 java.sql.Time endTime, String classroom, Integer excludeScheduleId) {
        if (dayOfWeek == null || startTime == null || endTime == null || classroom == null) {
            return false;
        }
        return courseScheduleDAO.hasTimeConflict(dayOfWeek, startTime, endTime, classroom, excludeScheduleId);
    }
}
