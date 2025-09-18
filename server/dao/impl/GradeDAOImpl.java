package server.dao.impl;

import common.vo.GradeVO;
import server.dao.GradeDAO;
import server.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩数据访问实现类
 */
public class GradeDAOImpl implements GradeDAO {
    
    @Override
    public Integer insert(GradeVO grade) {
        String sql = "INSERT INTO grades (enrollment_id, student_id, course_id, teacher_id, semester, " +
                    "midterm_grade, final_grade, assignment_grade, attendance_grade, total_grade, " +
                    "grade_point, grade_level, is_retake, comments, graded_time, created_time, updated_time, " +
                    "student_name, student_no, course_name, course_code, credits, teacher_name) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setObject(1, grade.getEnrollmentId());
            pstmt.setObject(2, grade.getStudentId());
            pstmt.setObject(3, grade.getCourseId());
            pstmt.setObject(4, grade.getTeacherId());
            pstmt.setString(5, grade.getSemester());
            pstmt.setObject(6, grade.getMidtermGrade());
            pstmt.setObject(7, grade.getFinalGrade());
            pstmt.setObject(8, grade.getAssignmentGrade());
            pstmt.setObject(9, grade.getAttendanceGrade());
            pstmt.setObject(10, grade.getTotalGrade());
            pstmt.setObject(11, grade.getGradePoint());
            pstmt.setString(12, grade.getGradeLevel());
            pstmt.setObject(13, grade.getIsRetake());
            pstmt.setString(14, grade.getComments());
            pstmt.setTimestamp(15, grade.getGradedTime());
            pstmt.setTimestamp(16, grade.getCreatedTime());
            pstmt.setTimestamp(17, grade.getUpdatedTime());
            pstmt.setString(18, grade.getStudentName());
            pstmt.setString(19, grade.getStudentNo());
            pstmt.setString(20, grade.getCourseName());
            pstmt.setString(21, grade.getCourseCode());
            pstmt.setObject(22, grade.getCredits());
            pstmt.setString(23, grade.getTeacherName());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("插入成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean update(GradeVO grade) {
        String sql = "UPDATE grades SET enrollment_id = ?, student_id = ?, course_id = ?, teacher_id = ?, " +
                    "semester = ?, midterm_grade = ?, final_grade = ?, assignment_grade = ?, attendance_grade = ?, " +
                    "total_grade = ?, grade_point = ?, grade_level = ?, is_retake = ?, comments = ?, " +
                    "graded_time = ?, updated_time = ?, student_name = ?, student_no = ?, course_name = ?, " +
                    "course_code = ?, credits = ?, teacher_name = ? WHERE grade_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setObject(1, grade.getEnrollmentId());
            pstmt.setObject(2, grade.getStudentId());
            pstmt.setObject(3, grade.getCourseId());
            pstmt.setObject(4, grade.getTeacherId());
            pstmt.setString(5, grade.getSemester());
            pstmt.setObject(6, grade.getMidtermGrade());
            pstmt.setObject(7, grade.getFinalGrade());
            pstmt.setObject(8, grade.getAssignmentGrade());
            pstmt.setObject(9, grade.getAttendanceGrade());
            pstmt.setObject(10, grade.getTotalGrade());
            pstmt.setObject(11, grade.getGradePoint());
            pstmt.setString(12, grade.getGradeLevel());
            pstmt.setObject(13, grade.getIsRetake());
            pstmt.setString(14, grade.getComments());
            pstmt.setTimestamp(15, grade.getGradedTime());
            pstmt.setTimestamp(16, grade.getUpdatedTime());
            pstmt.setString(17, grade.getStudentName());
            pstmt.setString(18, grade.getStudentNo());
            pstmt.setString(19, grade.getCourseName());
            pstmt.setString(20, grade.getCourseCode());
            pstmt.setObject(21, grade.getCredits());
            pstmt.setString(22, grade.getTeacherName());
            pstmt.setInt(23, grade.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新成绩失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM grades WHERE grade_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除成绩失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean existsById(Integer id) {
        if (id == null) return false;
        String sql = "SELECT 1 FROM grades WHERE grade_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查成绩是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM grades";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("统计成绩数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public GradeVO findById(Integer id) {
        String sql = "SELECT * FROM grades WHERE grade_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToGradeVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据ID查询成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<GradeVO> findAll() {
        String sql = "SELECT * FROM grades ORDER BY graded_time DESC, grade_id DESC";
        return findByStringField(sql, null);
    }
    
    @Override
    public List<GradeVO> findByStudentId(Integer studentId) {
        if (studentId == null) return new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id = ? ORDER BY graded_time DESC, grade_id DESC";
        return findByIntegerField(sql, studentId);
    }
    
    @Override
    public List<GradeVO> findByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE course_id = ? ORDER BY graded_time DESC, grade_id DESC";
        return findByIntegerField(sql, courseId);
    }
    
    @Override
    public List<GradeVO> findByTeacherId(Integer teacherId) {
        if (teacherId == null) return new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE teacher_id = ? ORDER BY graded_time DESC, grade_id DESC";
        return findByIntegerField(sql, teacherId);
    }
    
    @Override
    public List<GradeVO> findBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) return new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE semester = ? ORDER BY graded_time DESC, grade_id DESC";
        return findByStringField(sql, semester);
    }
    
    @Override
    public List<GradeVO> findAllWithDetails() {
        String sql = "SELECT * FROM grades ORDER BY graded_time DESC, grade_id DESC";
        return findByStringField(sql, null);
    }
    
    @Override
    public GradeVO findByStudentAndCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return null;
        String sql = "SELECT * FROM grades WHERE student_id = ? AND course_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToGradeVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据学生和课程查询成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public GradeVO findByEnrollmentId(Integer enrollmentId) {
        if (enrollmentId == null) return null;
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, enrollmentId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToGradeVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据选课记录ID查询成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByStudentAndCourse(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) return false;
        String sql = "SELECT 1 FROM grades WHERE student_id = ? AND course_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查成绩是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<GradeVO> findByGradeLevel(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) return new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE grade_level = ? ORDER BY graded_time DESC, grade_id DESC";
        return findByStringField(sql, gradeLevel);
    }
    
    @Override
    public int countGradedCoursesByStudentId(Integer studentId) {
        if (studentId == null) return 0;
        String sql = "SELECT COUNT(*) FROM grades WHERE student_id = ? AND total_grade IS NOT NULL";
        return countByIntegerField(sql, studentId);
    }
    
    @Override
    public int countGradedStudentsByCourseId(Integer courseId) {
        if (courseId == null) return 0;
        String sql = "SELECT COUNT(*) FROM grades WHERE course_id = ? AND total_grade IS NOT NULL";
        return countByIntegerField(sql, courseId);
    }
    
    // 辅助方法
    private List<GradeVO> findByStringField(String sql, String value) {
        List<GradeVO> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (value != null) {
                pstmt.setString(1, value);
            }
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                grades.add(mapResultSetToGradeVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return grades;
    }
    
    private List<GradeVO> findByIntegerField(String sql, Integer value) {
        List<GradeVO> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, value);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                grades.add(mapResultSetToGradeVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询成绩失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return grades;
    }
    
    private int countByIntegerField(String sql, Integer value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, value);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("统计成绩数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    private GradeVO mapResultSetToGradeVO(ResultSet rs) throws SQLException {
        GradeVO grade = new GradeVO();
        grade.setId(rs.getInt("grade_id"));
        grade.setEnrollmentId(rs.getObject("enrollment_id", Integer.class));
        grade.setStudentId(rs.getObject("student_id", Integer.class));
        grade.setCourseId(rs.getObject("course_id", Integer.class));
        grade.setTeacherId(rs.getObject("teacher_id", Integer.class));
        grade.setSemester(rs.getString("semester"));
        grade.setMidtermGrade(rs.getObject("midterm_grade", BigDecimal.class));
        grade.setFinalGrade(rs.getObject("final_grade", BigDecimal.class));
        grade.setAssignmentGrade(rs.getObject("assignment_grade", BigDecimal.class));
        grade.setAttendanceGrade(rs.getObject("attendance_grade", BigDecimal.class));
        grade.setTotalGrade(rs.getObject("total_grade", BigDecimal.class));
        grade.setGradePoint(rs.getObject("grade_point", BigDecimal.class));
        grade.setGradeLevel(rs.getString("grade_level"));
        grade.setIsRetake(rs.getObject("is_retake", Boolean.class));
        grade.setComments(rs.getString("comments"));
        grade.setGradedTime(rs.getTimestamp("graded_time"));
        grade.setCreatedTime(rs.getTimestamp("created_time"));
        grade.setUpdatedTime(rs.getTimestamp("updated_time"));
        grade.setStudentName(rs.getString("student_name"));
        grade.setStudentNo(rs.getString("student_no"));
        grade.setCourseName(rs.getString("course_name"));
        grade.setCourseCode(rs.getString("course_code"));
        grade.setCredits(rs.getObject("credits", Integer.class));
        grade.setTeacherName(rs.getString("teacher_name"));
        return grade;
    }
}
