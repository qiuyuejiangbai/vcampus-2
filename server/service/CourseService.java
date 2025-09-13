package server.service;

import common.vo.CourseVO;
import server.dao.CourseDAO;
import server.dao.impl.CourseDAOImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程服务类
 * 处理课程相关的业务逻辑
 */
public class CourseService {
    private final CourseDAO courseDAO;
    
    public CourseService() {
        this.courseDAO = new CourseDAOImpl();
    }
    
    /**
     * 获取所有课程（包含教师姓名）
     * @return 课程列表
     */
    public List<CourseVO> getAllCourses() {
        return courseDAO.findAllWithTeacherName();
    }
    
    /**
     * 获取所有启用的课程
     * @return 启用的课程列表
     */
    public List<CourseVO> getActiveCourses() {
        return courseDAO.findAllEnabled();
    }
    
    /**
     * 根据课程ID获取课程信息
     * @param courseId 课程ID
     * @return 课程信息，不存在返回null
     */
    public CourseVO getCourseById(Integer courseId) {
        if (courseId == null) return null;
        return courseDAO.findByIdWithTeacherName(courseId);
    }
    
    /**
     * 根据课程代码获取课程信息
     * @param courseCode 课程代码
     * @return 课程信息，不存在返回null
     */
    public CourseVO getCourseByCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) return null;
        return courseDAO.findByCourseCode(courseCode);
    }
    
    /**
     * 根据教师ID获取课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    public List<CourseVO> getCoursesByTeacher(Integer teacherId) {
        if (teacherId == null) return new ArrayList<>();
        return courseDAO.findByTeacherId(teacherId);
    }
    
    /**
     * 根据院系获取课程列表
     * @param department 院系
     * @return 课程列表
     */
    public List<CourseVO> getCoursesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) return new ArrayList<>();
        return courseDAO.findByDepartment(department);
    }
    
    /**
     * 根据学期获取课程列表
     * @param semester 学期
     * @return 课程列表
     */
    public List<CourseVO> getCoursesBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) return new ArrayList<>();
        return courseDAO.findBySemester(semester);
    }
    
    /**
     * 根据课程名称搜索课程
     * @param courseName 课程名称关键词
     * @return 课程列表
     */
    public List<CourseVO> searchCoursesByName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) return new ArrayList<>();
        return courseDAO.findByNameLike(courseName);
    }
    
    /**
     * 创建新课程
     * @param course 课程信息
     * @return 创建成功返回true，失败返回false
     */
    public boolean createCourse(CourseVO course) {
        if (course == null) return false;
        
        // 检查课程代码是否已存在
        if (courseDAO.existsByCourseCode(course.getCourseCode())) {
            System.err.println("课程代码已存在: " + course.getCourseCode());
            return false;
        }
        
        Integer courseId = courseDAO.insert(course);
        if (courseId != null) {
            course.setCourseId(courseId);
            return true;
        }
        return false;
    }
    
    /**
     * 更新课程信息
     * @param course 课程信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateCourse(CourseVO course) {
        if (course == null || course.getCourseId() == null) return false;
        return courseDAO.update(course);
    }
    
    /**
     * 删除课程
     * @param courseId 课程ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteCourse(Integer courseId) {
        if (courseId == null) return false;
        return courseDAO.deleteById(courseId);
    }
    
    /**
     * 更新课程选课人数
     * @param courseId 课程ID
     * @param enrolledCount 选课人数
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateEnrolledCount(Integer courseId, Integer enrolledCount) {
        if (courseId == null || enrolledCount == null) return false;
        return courseDAO.updateEnrolledCount(courseId, enrolledCount);
    }
    
    /**
     * 增加课程选课人数
     * @param courseId 课程ID
     * @param increment 增加数量（可为负数表示减少）
     * @return 更新成功返回true，失败返回false
     */
    public boolean incrementEnrolledCount(Integer courseId, Integer increment) {
        if (courseId == null || increment == null) return false;
        return courseDAO.incrementEnrolledCount(courseId, increment);
    }
    
    /**
     * 检查课程是否已满
     * @param courseId 课程ID
     * @return 已满返回true，未满返回false
     */
    public boolean isCourseFull(Integer courseId) {
        CourseVO course = getCourseById(courseId);
        if (course == null) return true;
        return course.isFull();
    }
    
    /**
     * 获取课程剩余容量
     * @param courseId 课程ID
     * @return 剩余容量，课程不存在返回0
     */
    public Integer getAvailableCapacity(Integer courseId) {
        CourseVO course = getCourseById(courseId);
        if (course == null) return 0;
        return course.getAvailableCapacity();
    }
}
