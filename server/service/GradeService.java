package server.service;

import common.vo.GradeVO;
import server.dao.GradeDAO;
import server.dao.impl.GradeDAOImpl;

import java.util.List;

/**
 * 成绩服务类
 * 处理成绩相关的业务逻辑
 */
public class GradeService {
    private final GradeDAO gradeDAO;
    
    public GradeService() {
        this.gradeDAO = new GradeDAOImpl();
    }
    
    /**
     * 获取所有成绩记录
     * @return 成绩列表
     */
    public List<GradeVO> getAllGrades() {
        return gradeDAO.findAllWithDetails();
    }
    
    /**
     * 根据学生ID获取成绩记录
     * @param studentId 学生ID
     * @return 成绩列表
     */
    public List<GradeVO> getGradesByStudentId(Integer studentId) {
        if (studentId == null) {
            return new java.util.ArrayList<>();
        }
        return gradeDAO.findByStudentId(studentId);
    }
    
    /**
     * 根据课程ID获取成绩记录
     * @param courseId 课程ID
     * @return 成绩列表
     */
    public List<GradeVO> getGradesByCourseId(Integer courseId) {
        if (courseId == null) {
            return new java.util.ArrayList<>();
        }
        return gradeDAO.findByCourseId(courseId);
    }
    
    /**
     * 根据教师ID获取成绩记录
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    public List<GradeVO> getGradesByTeacherId(Integer teacherId) {
        if (teacherId == null) {
            return new java.util.ArrayList<>();
        }
        return gradeDAO.findByTeacherId(teacherId);
    }
    
    /**
     * 根据学期获取成绩记录
     * @param semester 学期
     * @return 成绩列表
     */
    public List<GradeVO> getGradesBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return gradeDAO.findBySemester(semester);
    }
    
    /**
     * 根据学生ID和课程ID获取成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象，不存在返回null
     */
    public GradeVO getGradeByStudentAndCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) {
            return null;
        }
        return gradeDAO.findByStudentAndCourse(studentId, courseId);
    }
    
    /**
     * 根据选课记录ID获取成绩
     * @param enrollmentId 选课记录ID
     * @return 成绩对象，不存在返回null
     */
    public GradeVO getGradeByEnrollmentId(Integer enrollmentId) {
        if (enrollmentId == null) {
            return null;
        }
        return gradeDAO.findByEnrollmentId(enrollmentId);
    }
    
    /**
     * 添加成绩记录
     * @param grade 成绩对象
     * @return 添加成功返回true，失败返回false
     */
    public boolean addGrade(GradeVO grade) {
        if (grade == null) {
            return false;
        }
        
        // 检查是否已存在该学生该课程的成绩
        if (grade.getStudentId() != null && grade.getCourseId() != null) {
            if (gradeDAO.existsByStudentAndCourse(grade.getStudentId(), grade.getCourseId())) {
                System.err.println("该学生该课程的成绩已存在");
                return false;
            }
        }
        
        // 设置创建时间
        grade.setCreatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        grade.setUpdatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        
        return gradeDAO.insert(grade) != null;
    }
    
    /**
     * 更新成绩记录
     * @param grade 成绩对象
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateGrade(GradeVO grade) {
        if (grade == null || grade.getId() == null) {
            return false;
        }
        
        // 设置更新时间
        grade.setUpdatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        
        return gradeDAO.update(grade);
    }
    
    /**
     * 删除成绩记录
     * @param gradeId 成绩ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteGrade(Integer gradeId) {
        if (gradeId == null) {
            return false;
        }
        return gradeDAO.deleteById(gradeId);
    }
    
    /**
     * 根据成绩等级获取成绩记录
     * @param gradeLevel 成绩等级
     * @return 成绩列表
     */
    public List<GradeVO> getGradesByGradeLevel(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return gradeDAO.findByGradeLevel(gradeLevel);
    }
    
    /**
     * 统计学生已评分课程数量
     * @param studentId 学生ID
     * @return 已评分课程数量
     */
    public int countGradedCoursesByStudentId(Integer studentId) {
        if (studentId == null) {
            return 0;
        }
        return gradeDAO.countGradedCoursesByStudentId(studentId);
    }
    
    /**
     * 统计课程已评分学生数量
     * @param courseId 课程ID
     * @return 已评分学生数量
     */
    public int countGradedStudentsByCourseId(Integer courseId) {
        if (courseId == null) {
            return 0;
        }
        return gradeDAO.countGradedStudentsByCourseId(courseId);
    }
}
