package server.dao.impl;

import common.vo.EnrollmentVO;
import server.dao.EnrollmentDAO;
import server.dao.BaseDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 选课记录数据访问实现类
 */
public class EnrollmentDAOImpl implements EnrollmentDAO {
    
    @Override
    public EnrollmentVO findById(Integer id) {
        if (id == null) return null;
        
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEnrollmentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询选课记录失败: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<EnrollmentVO> findAll() {
        String sql = "SELECT * FROM enrollments ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public Integer insert(EnrollmentVO enrollment) {
        String sql = "INSERT INTO enrollments (student_id, course_id, semester, academic_year, " +
                    "enrollment_time, status, student_name, student_no, course_name, course_code, " +
                    "credits, teacher_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setString(3, enrollment.getSemester());
            stmt.setString(4, enrollment.getAcademicYear());
            stmt.setTimestamp(5, enrollment.getEnrollmentTime());
            stmt.setString(6, enrollment.getStatus());
            stmt.setString(7, enrollment.getStudentName());
            stmt.setString(8, enrollment.getStudentNo());
            stmt.setString(9, enrollment.getCourseName());
            stmt.setString(10, enrollment.getCourseCode());
            stmt.setInt(11, enrollment.getCredits());
            stmt.setString(12, enrollment.getTeacherName());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("插入选课记录失败: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean update(EnrollmentVO enrollment) {
        String sql = "UPDATE enrollments SET student_id = ?, course_id = ?, semester = ?, " +
                    "academic_year = ?, enrollment_time = ?, drop_time = ?, drop_reason = ?, " +
                    "status = ?, student_name = ?, student_no = ?, course_name = ?, " +
                    "course_code = ?, credits = ?, teacher_name = ? WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setString(3, enrollment.getSemester());
            stmt.setString(4, enrollment.getAcademicYear());
            stmt.setTimestamp(5, enrollment.getEnrollmentTime());
            stmt.setTimestamp(6, enrollment.getDropTime());
            stmt.setString(7, enrollment.getDropReason());
            stmt.setString(8, enrollment.getStatus());
            stmt.setString(9, enrollment.getStudentName());
            stmt.setString(10, enrollment.getStudentNo());
            stmt.setString(11, enrollment.getCourseName());
            stmt.setString(12, enrollment.getCourseCode());
            stmt.setInt(13, enrollment.getCredits());
            stmt.setString(14, enrollment.getTeacherName());
            stmt.setInt(15, enrollment.getEnrollmentId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新选课记录失败: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;
        
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除选课记录失败: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public List<EnrollmentVO> findByStudentId(Integer studentId) {
        if (studentId == null) return new ArrayList<>();
        
        // 只返回状态为'enrolled'的选课记录（当前有效的选课）
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND status = 'enrolled' ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据学生ID查询选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public List<EnrollmentVO> findByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM enrollments WHERE course_id = ? ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据课程ID查询选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public EnrollmentVO findByStudentIdAndCourseId(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return null;
        
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEnrollmentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据学生ID和课程ID查询选课记录失败: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean isEnrolled(Integer studentId, Integer courseId) {
        return findByStudentIdAndCourseId(studentId, courseId) != null;
    }
    
    @Override
    public List<EnrollmentVO> findByStatus(Integer status) {
        String statusStr = convertStatusToString(status);
        if (statusStr == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM enrollments WHERE status = ? ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, statusStr);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据状态查询选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public List<EnrollmentVO> findByStudentIdAndStatus(Integer studentId, Integer status) {
        if (studentId == null) return new ArrayList<>();
        
        String statusStr = convertStatusToString(status);
        if (statusStr == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND status = ? ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, statusStr);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据学生ID和状态查询选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public List<EnrollmentVO> findByCourseIdAndStatus(Integer courseId, Integer status) {
        if (courseId == null) return new ArrayList<>();
        
        String statusStr = convertStatusToString(status);
        if (statusStr == null) return new ArrayList<>();
        
        String sql = "SELECT * FROM enrollments WHERE course_id = ? AND status = ? ORDER BY enrollment_time DESC";
        List<EnrollmentVO> enrollments = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            stmt.setString(2, statusStr);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据课程ID和状态查询选课记录失败: " + e.getMessage());
        }
        return enrollments;
    }
    
    @Override
    public List<EnrollmentVO> getTranscriptByStudentId(Integer studentId) {
        return findByStudentId(studentId);
    }
    
    @Override
    public List<EnrollmentVO> getStudentListByCourseId(Integer courseId) {
        return findByCourseId(courseId);
    }
    
    @Override
    public boolean updateGrade(Integer studentId, Integer courseId, Double grade) {
        // 这个方法需要与成绩表关联，暂时返回false
        return false;
    }
    
    @Override
    public boolean updateStatus(Integer studentId, Integer courseId, Integer status) {
        if (studentId == null || courseId == null) return false;
        
        String statusStr = convertStatusToString(status);
        if (statusStr == null) return false;
        
        String sql = "UPDATE enrollments SET status = ? WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, statusStr);
            stmt.setInt(2, studentId);
            stmt.setInt(3, courseId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新选课状态失败: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean dropCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return false;
        
        String sql = "UPDATE enrollments SET status = 'dropped', drop_time = NOW() WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("退课失败: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public int countActiveEnrollmentsByStudentId(Integer studentId) {
        if (studentId == null) return 0;
        
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND status IN ('enrolled', 'completed')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("统计学生选课数量失败: " + e.getMessage());
        }
        return 0;
    }
    
    @Override
    public int countActiveEnrollmentsByCourseId(Integer courseId) {
        if (courseId == null) return 0;
        
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status IN ('enrolled', 'completed')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("统计课程选课人数失败: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 将ResultSet映射为EnrollmentVO对象
     */
    private EnrollmentVO mapResultSetToEnrollmentVO(ResultSet rs) throws SQLException {
        EnrollmentVO enrollment = new EnrollmentVO();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setCourseId(rs.getInt("course_id"));
        enrollment.setSemester(rs.getString("semester"));
        enrollment.setAcademicYear(rs.getString("academic_year"));
        enrollment.setEnrollmentTime(rs.getTimestamp("enrollment_time"));
        enrollment.setDropTime(rs.getTimestamp("drop_time"));
        enrollment.setDropReason(rs.getString("drop_reason"));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setStudentName(rs.getString("student_name"));
        enrollment.setStudentNo(rs.getString("student_no"));
        enrollment.setCourseName(rs.getString("course_name"));
        enrollment.setCourseCode(rs.getString("course_code"));
        enrollment.setCredits(rs.getInt("credits"));
        enrollment.setTeacherName(rs.getString("teacher_name"));
        return enrollment;
    }
    
    @Override
    public boolean existsById(Integer id) {
        if (id == null) return false;
        return findById(id) != null;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM enrollments";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("统计选课记录数量失败: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 将状态数字转换为字符串
     */
    private String convertStatusToString(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "dropped";
            case 1: return "enrolled";
            case 2: return "completed";
            default: return null;
        }
    }
}
