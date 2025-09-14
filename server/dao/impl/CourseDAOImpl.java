package server.dao.impl;

import common.vo.CourseVO;
import server.dao.CourseDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程数据访问实现类
 * 实现课程相关的数据库操作
 */
public class CourseDAOImpl implements CourseDAO {
    
    @Override
    public CourseVO findById(Integer id) {
        if (id == null) return null;
        
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCourseVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public List<CourseVO> findAll() {
        return findAllWithTeacherName();
    }
    
    @Override
    public Integer insert(CourseVO course) {
        if (course == null) return null;
        
        String sql = "INSERT INTO courses (course_code, course_name, credits, department, teacher_id, teacher_name, " +
                    "semester, academic_year, class_time, location, capacity, enrolled_count, status, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setInt(3, course.getCredits());
            ps.setString(4, course.getDepartment());
            ps.setObject(5, course.getTeacherId());
            ps.setString(6, course.getTeacherName());
            ps.setString(7, course.getSemester());
            ps.setString(8, course.getAcademicYear());
            ps.setString(9, course.getClassTime());
            ps.setString(10, course.getLocation());
            ps.setInt(11, course.getCapacity());
            ps.setInt(12, course.getEnrolledCount() != null ? course.getEnrolledCount() : 0);
            ps.setString(13, course.getStatus());
            ps.setString(14, course.getDescription());
            
            int result = ps.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("保存课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return null;
    }
    
    @Override
    public boolean existsById(Integer id) {
        return findById(id) != null;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM courses";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("统计课程数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 0;
    }
    
    @Override
    public boolean update(CourseVO course) {
        if (course == null || course.getCourseId() == null) return false;
        
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, department = ?, " +
                    "teacher_id = ?, teacher_name = ?, semester = ?, academic_year = ?, class_time = ?, " +
                    "location = ?, capacity = ?, enrolled_count = ?, status = ?, description = ? " +
                    "WHERE course_id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setInt(3, course.getCredits());
            ps.setString(4, course.getDepartment());
            ps.setObject(5, course.getTeacherId());
            ps.setString(6, course.getTeacherName());
            ps.setString(7, course.getSemester());
            ps.setString(8, course.getAcademicYear());
            ps.setString(9, course.getClassTime());
            ps.setString(10, course.getLocation());
            ps.setInt(11, course.getCapacity());
            ps.setInt(12, course.getEnrolledCount() != null ? course.getEnrolledCount() : 0);
            ps.setString(13, course.getStatus());
            ps.setString(14, course.getDescription());
            ps.setInt(15, course.getCourseId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;
        
        String sql = "DELETE FROM courses WHERE course_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public CourseVO findByCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) return null;
        
        String sql = "SELECT * FROM courses WHERE course_code = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, courseCode);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCourseVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据课程代码查询课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByCourseCode(String courseCode) {
        return findByCourseCode(courseCode) != null;
    }
    
    @Override
    public List<CourseVO> findByTeacherId(Integer teacherId) {
        if (teacherId == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM courses WHERE teacher_id = ? ORDER BY course_code";
        return executeQueryWithParams(sql, teacherId);
    }
    
    @Override
    public List<CourseVO> findByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) return new ArrayList<>();
        
        String sql = "SELECT * FROM courses WHERE department = ? ORDER BY course_code";
        return executeQueryWithParams(sql, department);
    }
    
    @Override
    public List<CourseVO> findBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) return new ArrayList<>();
        
        String sql = "SELECT * FROM courses WHERE semester = ? ORDER BY course_code";
        return executeQueryWithParams(sql, semester);
    }
    
    @Override
    public List<CourseVO> findByStatus(Integer status) {
        if (status == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM courses WHERE status = ? ORDER BY course_code";
        return executeQueryWithParams(sql, status);
    }
    
    @Override
    public List<CourseVO> findByNameLike(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) return new ArrayList<>();
        
        String sql = "SELECT * FROM courses WHERE course_name LIKE ? ORDER BY course_code";
        return executeQueryWithParams(sql, "%" + courseName + "%");
    }
    
    @Override
    public List<CourseVO> findAllEnabled() {
        String sql = "SELECT * FROM courses WHERE status = 'active' ORDER BY course_code";
        return executeQuery(sql);
    }
    
    @Override
    public List<CourseVO> findAllWithTeacherName() {
        String sql = "SELECT c.*, t.name as teacher_name FROM courses c " +
                    "LEFT JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "ORDER BY c.course_code";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CourseVO> courses = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                CourseVO course = mapResultSetToCourseVO(rs);
                // 如果teacher_name不为空，则使用查询到的教师姓名
                String teacherName = rs.getString("teacher_name");
                if (teacherName != null && !teacherName.trim().isEmpty()) {
                    course.setTeacherName(teacherName);
                }
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("查询所有课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return courses;
    }
    
    @Override
    public CourseVO findByIdWithTeacherName(Integer courseId) {
        if (courseId == null) return null;
        
        String sql = "SELECT c.*, t.name as teacher_name FROM courses c " +
                    "LEFT JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "WHERE c.course_id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, courseId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                CourseVO course = mapResultSetToCourseVO(rs);
                String teacherName = rs.getString("teacher_name");
                if (teacherName != null && !teacherName.trim().isEmpty()) {
                    course.setTeacherName(teacherName);
                }
                return course;
            }
        } catch (SQLException e) {
            System.err.println("根据ID查询课程失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public boolean updateEnrolledCount(Integer courseId, Integer enrolledCount) {
        if (courseId == null || enrolledCount == null) return false;
        
        String sql = "UPDATE courses SET enrolled_count = ? WHERE course_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, enrolledCount);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新选课人数失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public boolean incrementEnrolledCount(Integer courseId, Integer increment) {
        if (courseId == null || increment == null) return false;
        
        String sql = "UPDATE courses SET enrolled_count = enrolled_count + ? WHERE course_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, increment);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("增加选课人数失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    /**
     * 执行查询并返回课程列表
     */
    private List<CourseVO> executeQuery(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CourseVO> courses = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                courses.add(mapResultSetToCourseVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("执行查询失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return courses;
    }
    
    /**
     * 执行带参数的查询并返回课程列表
     */
    private List<CourseVO> executeQueryWithParams(String sql, Object param) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CourseVO> courses = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, param);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                courses.add(mapResultSetToCourseVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("执行参数查询失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return courses;
    }
    
    /**
     * 将ResultSet映射为CourseVO对象
     */
    private CourseVO mapResultSetToCourseVO(ResultSet rs) throws SQLException {
        CourseVO course = new CourseVO();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setCredits(rs.getInt("credits"));
        course.setDepartment(rs.getString("department"));
        course.setTeacherId(rs.getInt("teacher_id"));
        course.setTeacherName(rs.getString("teacher_name"));
        course.setSemester(rs.getString("semester"));
        course.setAcademicYear(rs.getString("academic_year"));
        course.setClassTime(rs.getString("class_time"));
        course.setLocation(rs.getString("location"));
        course.setDescription(rs.getString("description"));
        course.setCapacity(rs.getInt("capacity"));
        course.setEnrolledCount(rs.getInt("enrolled_count"));
        course.setStatus(rs.getString("status"));
        course.setCreatedTime(rs.getTimestamp("created_time"));
        course.setUpdatedTime(rs.getTimestamp("updated_time"));
        return course;
    }
}
