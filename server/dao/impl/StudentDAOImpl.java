package server.dao.impl;

import common.vo.StudentVO;
import common.vo.UserVO;
import server.dao.StudentDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问实现类
 */
public class StudentDAOImpl implements StudentDAO {
    
    @Override
    public Integer insert(StudentVO student) {
        String sql = "INSERT INTO students (user_id, student_no, major, class_name, grade, enrollment_year) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, student.getUserId());
            pstmt.setString(2, student.getStudentNo());
            pstmt.setString(3, student.getMajor());
            pstmt.setString(4, student.getClassName());
            pstmt.setString(5, student.getGrade());
            pstmt.setObject(6, student.getEnrollmentYear());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("插入学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean deleteById(Integer studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除学生失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean update(StudentVO student) {
        String sql = "UPDATE students SET major = ?, class_name = ?, grade = ?, enrollment_year = ? WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, student.getMajor());
            pstmt.setString(2, student.getClassName());
            pstmt.setString(3, student.getGrade());
            pstmt.setObject(4, student.getEnrollmentYear());
            pstmt.setInt(5, student.getStudentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新学生失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public StudentVO findById(Integer studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStudentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<StudentVO> findAll() {
        String sql = "SELECT * FROM students ORDER BY student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM students";
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
            System.err.println("统计学生数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(Integer studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查学生是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public StudentVO findByUserId(Integer userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStudentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据用户ID查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public StudentVO findByStudentNo(String studentNo) {
        String sql = "SELECT * FROM students WHERE student_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentNo);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStudentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据学号查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByStudentNo(String studentNo) {
        String sql = "SELECT 1 FROM students WHERE student_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentNo);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查学号是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<StudentVO> findByMajor(String major) {
        String sql = "SELECT * FROM students WHERE major = ? ORDER BY student_id";
        return findByStringField(sql, major);
    }
    
    @Override
    public List<StudentVO> findByClassName(String className) {
        String sql = "SELECT * FROM students WHERE class_name = ? ORDER BY student_id";
        return findByStringField(sql, className);
    }
    
    @Override
    public List<StudentVO> findByGrade(String grade) {
        String sql = "SELECT * FROM students WHERE grade = ? ORDER BY student_id";
        return findByStringField(sql, grade);
    }
    
    @Override
    public List<StudentVO> findByEnrollmentYear(Integer enrollmentYear) {
        String sql = "SELECT * FROM students WHERE enrollment_year = ? ORDER BY student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, enrollmentYear);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据入学年份查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public List<StudentVO> findAllWithUserInfo() {
        String sql = "SELECT s.*, u.login_id, u.name, u.phone, u.email, u.role, u.status, u.balance, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("查询所有学生（含用户信息）失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public StudentVO findByIdWithUserInfo(Integer studentId) {
        String sql = "SELECT s.*, u.login_id, u.name, u.phone, u.email, u.role, u.status, u.balance, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                return student;
            }
        } catch (SQLException e) {
            System.err.println("查询学生（含用户信息）失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<StudentVO> findByNameLike(String name) {
        String sql = "SELECT s.*, u.login_id, u.name, u.phone, u.email, u.role, u.status, u.balance, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id WHERE u.name LIKE ? ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("根据姓名模糊查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    /**
     * 根据字符串字段查询学生列表的通用方法
     */
    private List<StudentVO> findByStringField(String sql, String value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    /**
     * 将ResultSet映射为StudentVO对象
     */
    private StudentVO mapResultSetToStudentVO(ResultSet rs) throws SQLException {
        StudentVO student = new StudentVO();
        student.setStudentId(rs.getInt("student_id"));
        student.setUserId(rs.getInt("user_id"));
        student.setStudentNo(rs.getString("student_no"));
        student.setMajor(rs.getString("major"));
        student.setClassName(rs.getString("class_name"));
        student.setGrade(rs.getString("grade"));
        student.setEnrollmentYear((Integer) rs.getObject("enrollment_year"));
        return student;
    }
    
    /**
     * 将ResultSet映射为UserVO对象（用于关联查询）
     */
    private UserVO mapResultSetToUserVO(ResultSet rs) throws SQLException {
        UserVO user = new UserVO();
        user.setUserId(rs.getInt("user_id"));
        user.setLoginId(rs.getString("login_id"));
        user.setName(rs.getString("name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getInt("role"));
        user.setStatus(rs.getInt("status"));
        user.setBalance(rs.getDouble("balance"));
        user.setCreatedTime(rs.getTimestamp("created_time"));
        user.setUpdatedTime(rs.getTimestamp("updated_time"));
        return user;
    }
}
