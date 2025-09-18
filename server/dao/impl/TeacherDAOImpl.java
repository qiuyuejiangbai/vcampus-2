package server.dao.impl;

import common.vo.TeacherVO;
import common.vo.UserVO;
import server.dao.TeacherDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师数据访问实现类
 */
public class TeacherDAOImpl implements TeacherDAO {
    
    @Override
    public Integer insert(TeacherVO teacher) {
        System.out.println("[DEBUG][TeacherDAOImpl] ========== 开始插入教师到数据库 ==========");
            System.out.println("[DEBUG][TeacherDAOImpl] SQL: INSERT INTO teachers (user_id, name, teacher_no, phone, email, department, title, office, research_area, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        String sql = "INSERT INTO teachers (user_id, name, teacher_no, phone, email, department, title, office, research_area, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[DEBUG][TeacherDAOImpl] 获取数据库连接");
            conn = DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][TeacherDAOImpl] 数据库连接获取失败");
                return null;
            }
            System.out.println("[DEBUG][TeacherDAOImpl] 数据库连接获取成功");
            
            System.out.println("[DEBUG][TeacherDAOImpl] 准备SQL语句");
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            System.out.println("[DEBUG][TeacherDAOImpl] 设置SQL参数：");
            pstmt.setInt(1, teacher.getUserId());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数1 (user_id): " + teacher.getUserId());
            pstmt.setString(2, teacher.getName());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数2 (name): " + teacher.getName());
            pstmt.setString(3, teacher.getTeacherNo());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数3 (teacher_no): " + teacher.getTeacherNo());
            pstmt.setString(4, teacher.getPhone());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数4 (phone): " + teacher.getPhone());
            pstmt.setString(5, teacher.getEmail());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数5 (email): " + teacher.getEmail());
            pstmt.setString(6, teacher.getDepartment());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数6 (department): " + teacher.getDepartment());
            pstmt.setString(7, teacher.getTitle());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数7 (title): " + teacher.getTitle());
            pstmt.setString(8, teacher.getOffice());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数8 (office): " + teacher.getOffice());
            pstmt.setString(9, teacher.getResearchArea());
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数9 (research_area): " + teacher.getResearchArea());
            pstmt.setBigDecimal(10, teacher.getBalance() != null ? teacher.getBalance() : java.math.BigDecimal.ZERO);
            System.out.println("[DEBUG][TeacherDAOImpl] - 参数10 (balance): " + (teacher.getBalance() != null ? teacher.getBalance() : java.math.BigDecimal.ZERO));
            
            System.out.println("[DEBUG][TeacherDAOImpl] 执行SQL插入操作");
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[DEBUG][TeacherDAOImpl] SQL执行完成，影响行数: " + affectedRows);
            
            if (affectedRows > 0) {
                System.out.println("[DEBUG][TeacherDAOImpl] 获取生成的主键");
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer teacherId = rs.getInt(1);
                    System.out.println("[DEBUG][TeacherDAOImpl] 教师插入成功，生成的教师ID: " + teacherId);
                    System.out.println("[DEBUG][TeacherDAOImpl] ========== 教师插入完成 ==========");
                    return teacherId;
                } else {
                    System.err.println("[DEBUG][TeacherDAOImpl] 无法获取生成的主键");
                }
            } else {
                System.err.println("[DEBUG][TeacherDAOImpl] 没有行被插入");
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG][TeacherDAOImpl] 插入教师失败: " + e.getMessage());
            System.err.println("[DEBUG][TeacherDAOImpl] SQL错误代码: " + e.getErrorCode());
            System.err.println("[DEBUG][TeacherDAOImpl] SQL状态: " + e.getSQLState());
            e.printStackTrace();
        } finally {
            System.out.println("[DEBUG][TeacherDAOImpl] 关闭数据库资源");
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        System.out.println("[DEBUG][TeacherDAOImpl] ========== 教师插入失败 ==========");
        return null;
    }
    
    @Override
    public boolean deleteById(Integer teacherId) {
        String sql = "DELETE FROM teachers WHERE teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除教师失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean update(TeacherVO teacher) {
        System.out.println("[DEBUG][TeacherDAOImpl] ========== 开始更新教师信息 ==========");
        System.out.println("[DEBUG][TeacherDAOImpl] 教师ID=" + teacher.getId() + ", 用户ID=" + teacher.getUserId());
        System.out.println("[DEBUG][TeacherDAOImpl] 基本信息：name=" + teacher.getName() + ", phone=" + teacher.getPhone() + ", email=" + teacher.getEmail());
        System.out.println("[DEBUG][TeacherDAOImpl] 专业信息：teacherNo=" + teacher.getTeacherNo() + ", title=" + teacher.getTitle() + ", office=" + teacher.getOffice() + ", researchArea=" + teacher.getResearchArea());
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("[DEBUG][TeacherDAOImpl] 获取数据库连接");
            conn = DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][TeacherDAOImpl] 数据库连接获取失败");
                return false;
            }
            System.out.println("[DEBUG][TeacherDAOImpl] 数据库连接获取成功，开启事务");
            conn.setAutoCommit(false); // 开启事务
            
            // 一次性更新teachers表的所有字段
            String teacherSql = "UPDATE teachers SET name = ?, teacher_no = ?, phone = ?, email = ?, department = ?, title = ?, office = ?, research_area = ?, balance = ?, updated_time = CURRENT_TIMESTAMP WHERE teacher_id = ?";
            System.out.println("[DEBUG][TeacherDAOImpl] 准备teachers表更新SQL：" + teacherSql);
            pstmt = conn.prepareStatement(teacherSql);
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getTeacherNo());
            pstmt.setString(3, teacher.getPhone());
            pstmt.setString(4, teacher.getEmail());
            pstmt.setString(5, teacher.getDepartment());
            pstmt.setString(6, teacher.getTitle());
            pstmt.setString(7, teacher.getOffice());
            pstmt.setString(8, teacher.getResearchArea());
            pstmt.setBigDecimal(9, teacher.getBalance() != null ? teacher.getBalance() : java.math.BigDecimal.ZERO);
            pstmt.setInt(10, teacher.getId());
            System.out.println("[DEBUG][TeacherDAOImpl] teachers表更新参数：name=" + teacher.getName() + ", teacherNo=" + teacher.getTeacherNo() + ", phone=" + teacher.getPhone() + ", email=" + teacher.getEmail() + ", department=" + teacher.getDepartment() + ", title=" + teacher.getTitle() + ", office=" + teacher.getOffice() + ", researchArea=" + teacher.getResearchArea() + ", balance=" + (teacher.getBalance() != null ? teacher.getBalance() : java.math.BigDecimal.ZERO) + ", teacherId=" + teacher.getId());
            
            int teacherRows = pstmt.executeUpdate();
            System.out.println("[DEBUG][TeacherDAOImpl] teachers表更新影响行数：" + teacherRows);
            
            // 提交事务
            System.out.println("[DEBUG][TeacherDAOImpl] 提交事务");
            conn.commit();
            System.out.println("[DEBUG][TeacherDAOImpl] 事务提交成功，teacherRows=" + teacherRows);
            
            boolean success = teacherRows > 0;
            System.out.println("[DEBUG][TeacherDAOImpl] 更新结果：" + success);
            System.out.println("[DEBUG][TeacherDAOImpl] ========== 教师信息更新完成 ==========");
            return success;
        } catch (SQLException e) {
            System.err.println("[DEBUG][TeacherDAOImpl] 更新教师失败: " + e.getMessage());
            System.err.println("[DEBUG][TeacherDAOImpl] SQL异常详情：");
            e.printStackTrace();
            try {
                if (conn != null) {
                    System.out.println("[DEBUG][TeacherDAOImpl] 开始回滚事务");
                    conn.rollback(); // 回滚事务
                    System.out.println("[DEBUG][TeacherDAOImpl] 事务回滚成功");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("[DEBUG][TeacherDAOImpl] 回滚事务失败: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            System.out.println("[DEBUG][TeacherDAOImpl] 释放数据库资源");
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public TeacherVO findById(Integer teacherId) {
        String sql = "SELECT * FROM teachers WHERE teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTeacherVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询教师失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<TeacherVO> findAll() {
        String sql = "SELECT * FROM teachers ORDER BY teacher_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TeacherVO> teachers = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                teachers.add(mapResultSetToTeacherVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有教师失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return teachers;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM teachers";
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
            System.err.println("统计教师数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(Integer teacherId) {
        String sql = "SELECT 1 FROM teachers WHERE teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查教师是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public TeacherVO findByUserId(Integer userId) {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time " +
                    "FROM teachers t JOIN users u ON t.user_id = u.user_id WHERE t.user_id = ?";
        System.out.println("[DEBUG][TeacherDAOImpl] 执行SQL查询：" + sql + ", userId=" + userId);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            System.out.println("[DEBUG][TeacherDAOImpl] 数据库连接获取成功");
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            System.out.println("[DEBUG][TeacherDAOImpl] 准备执行查询，参数userId=" + userId);
            
            rs = pstmt.executeQuery();
            System.out.println("[DEBUG][TeacherDAOImpl] 查询执行完成");
            
            if (rs.next()) {
                System.out.println("[DEBUG][TeacherDAOImpl] 找到匹配记录，开始映射数据");
                TeacherVO teacher = mapResultSetToTeacherVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                teacher.setUser(user);
                System.out.println("[DEBUG][TeacherDAOImpl] 数据映射完成，教师信息：" + 
                    (teacher != null ? ("ID=" + teacher.getId() + ", 姓名=" + teacher.getName()) : "null"));
                return teacher;
            } else {
                System.err.println("[DEBUG][TeacherDAOImpl] 未找到匹配的教师记录，userId=" + userId);
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG][TeacherDAOImpl] 根据用户ID查询教师失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
            System.out.println("[DEBUG][TeacherDAOImpl] 数据库资源已释放");
        }
        return null;
    }
    
    @Override
    public TeacherVO findByTeacherNo(String teacherNo) {
        String sql = "SELECT * FROM teachers WHERE teacher_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, teacherNo);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTeacherVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据工号查询教师失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByTeacherNo(String teacherNo) {
        String sql = "SELECT 1 FROM teachers WHERE teacher_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, teacherNo);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查工号是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<TeacherVO> findByDepartment(String department) {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id WHERE t.department = ? ORDER BY t.teacher_id";
        return findByStringFieldWithUserInfo(sql, department);
    }
    
    @Override
    public List<TeacherVO> findByTitle(String title) {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id WHERE t.title = ? ORDER BY t.teacher_id";
        return findByStringFieldWithUserInfo(sql, title);
    }
    
    @Override
    public List<TeacherVO> findAllWithUserInfo() {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id ORDER BY t.teacher_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TeacherVO> teachers = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                TeacherVO teacher = mapResultSetToTeacherVO(rs);
                // 创建用户信息对象，只包含users表中实际存在的字段
                UserVO user = new UserVO();
                user.setUserId(rs.getInt("user_id"));
                user.setLoginId(rs.getString("login_id"));
                user.setRole(rs.getInt("role"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setCreatedTime(rs.getTimestamp("created_time"));
                user.setUpdatedTime(rs.getTimestamp("updated_time"));
                teacher.setUser(user);
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            System.err.println("查询所有教师（含用户信息）失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return teachers;
    }
    
    @Override
    public TeacherVO findByIdWithUserInfo(Integer teacherId) {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id WHERE t.teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                TeacherVO teacher = mapResultSetToTeacherVO(rs);
                // 创建用户信息对象，只包含users表中实际存在的字段
                UserVO user = new UserVO();
                user.setUserId(rs.getInt("user_id"));
                user.setLoginId(rs.getString("login_id"));
                user.setRole(rs.getInt("role"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setCreatedTime(rs.getTimestamp("created_time"));
                user.setUpdatedTime(rs.getTimestamp("updated_time"));
                teacher.setUser(user);
                return teacher;
            }
        } catch (SQLException e) {
            System.err.println("查询教师（含用户信息）失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<TeacherVO> findByNameLike(String name) {
        String sql = "SELECT t.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time FROM teachers t " +
                    "JOIN users u ON t.user_id = u.user_id WHERE t.name LIKE ? ORDER BY t.teacher_id";
        return findByStringFieldWithUserInfo(sql, "%" + name + "%");
    }
    
    @Override
    public int getCourseCount(Integer teacherId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("获取教师课程数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public int getStudentCount(Integer teacherId) {
        String sql = "SELECT SUM(c.enrolled_count) FROM courses c WHERE c.teacher_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("获取教师学生数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    
    /**
     * 通用的字符串字段查询方法（包含用户信息）
     */
    private List<TeacherVO> findByStringFieldWithUserInfo(String sql, String value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TeacherVO> teachers = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                TeacherVO teacher = mapResultSetToTeacherVO(rs);
                // 创建用户信息对象，只包含users表中实际存在的字段
                UserVO user = new UserVO();
                user.setUserId(rs.getInt("user_id"));
                user.setLoginId(rs.getString("login_id"));
                user.setRole(rs.getInt("role"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setCreatedTime(rs.getTimestamp("created_time"));
                user.setUpdatedTime(rs.getTimestamp("updated_time"));
                teacher.setUser(user);
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            System.err.println("查询教师（含用户信息）失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return teachers;
    }
    
    /**
     * 将ResultSet映射为TeacherVO对象
     */
    private TeacherVO mapResultSetToTeacherVO(ResultSet rs) throws SQLException {
        TeacherVO teacher = new TeacherVO();
        teacher.setId(rs.getInt("teacher_id"));
        teacher.setUserId(rs.getInt("user_id"));
        teacher.setName(rs.getString("name"));
        teacher.setTeacherNo(rs.getString("teacher_no"));
        teacher.setPhone(rs.getString("phone"));
        teacher.setEmail(rs.getString("email"));
        teacher.setDepartment(rs.getString("department"));
        teacher.setTitle(rs.getString("title"));
        teacher.setOffice(rs.getString("office"));
        teacher.setResearchArea(rs.getString("research_area"));
        teacher.setBalance(rs.getBigDecimal("balance"));
        teacher.setCreatedTime(rs.getTimestamp("created_time"));
        teacher.setUpdatedTime(rs.getTimestamp("updated_time"));
        return teacher;
    }
    
    
    /**
     * 将ResultSet映射为UserVO对象（用于关联查询）
     */
    private UserVO mapResultSetToUserVO(ResultSet rs) throws SQLException {
        UserVO user = new UserVO();
        user.setUserId(rs.getInt("user_id"));
        user.setId(rs.getString("login_id"));
        user.setRole(rs.getInt("role"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setCreatedTime(rs.getTimestamp("created_time"));
        user.setUpdatedTime(rs.getTimestamp("updated_time"));
        return user;
    }
}
