package server.dao.impl;

import common.vo.UserVO;
import server.dao.UserDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问实现类
 */
public class UserDAOImpl implements UserDAO {
    
    @Override
    public Integer insert(UserVO user) {
        String sql = "INSERT INTO users (login_id, password, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, user.getId()); // 使用id作为login_id
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getRole() != null ? user.getRole() : 0);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer userId = rs.getInt(1);
                    user.setUserId(userId); // 设置生成的用户ID
                    return userId;
                }
            }
        } catch (SQLException e) {
            System.err.println("插入用户失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean deleteById(Integer userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除用户失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean update(UserVO user) {
        String sql = "UPDATE users SET login_id = ?, password = ?, role = ?, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getRole() != null ? user.getRole() : 0);
            pstmt.setInt(4, user.getUserId()); // 使用userId作为更新条件
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public UserVO findById(Integer userId) {
        String sql = "SELECT user_id, login_id, password, role FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUserVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<UserVO> findAll() {
        String sql = "SELECT user_id, login_id, password, role FROM users ORDER BY user_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<UserVO> users = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUserVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return users;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
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
            System.err.println("统计用户数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(Integer userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查用户是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public UserVO findByLoginId(String loginId) {
        String sql = "SELECT user_id, login_id, password, role FROM users WHERE login_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUserVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据登录ID查询用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public UserVO authenticate(String loginId, String password) {
        String sql = "SELECT user_id, login_id, password, role FROM users WHERE login_id = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);
            pstmt.setString(2, password);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUserVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("用户认证失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByLoginId(String loginId) {
        String sql = "SELECT 1 FROM users WHERE login_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查登录ID是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<UserVO> findByRole(Integer role) {
        String sql = "SELECT user_id, login_id, password, role FROM users WHERE role = ? ORDER BY user_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<UserVO> users = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, role);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUserVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据角色查询用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return users;
    }
    
    @Override
    public List<UserVO> findByStatus(Integer status) {
        // 状态管理已移除，返回所有用户
        System.out.println("根据状态查询用户: " + status + " (状态管理已简化，返回所有用户)");
        return findAll();
    }
    
    @Override
    public boolean updatePassword(Integer userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新用户密码失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean updateBalance(Integer userId, Double amount) {
        // 余额信息存储在students表中
        String sql = "UPDATE students SET balance = balance + ?, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新用户余额失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean activateUser(Integer userId) {
        // 状态管理已移除，此方法保留为兼容性，直接返回true
        System.out.println("激活用户: " + userId + " (状态管理已简化)");
        return true;
    }
    
    @Override
    public boolean deactivateUser(Integer userId) {
        // 状态管理已移除，此方法保留为兼容性，直接返回true
        System.out.println("停用用户: " + userId + " (状态管理已简化)");
        return true;
    }
    
    @Override
    public List<UserVO> findByNameLike(String name) {
        // 由于users表没有name字段，这里根据login_id进行模糊查询
        String sql = "SELECT user_id, login_id, password, role FROM users WHERE login_id LIKE ? ORDER BY user_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<UserVO> users = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUserVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据登录ID模糊查询用户失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return users;
    }
    
    @Override
    public Double getBalance(Integer userId) {
        // 余额信息存储在students表中
        String sql = "SELECT balance FROM students WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("获取用户余额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    /**
     * 将ResultSet映射为UserVO对象
     * @param rs ResultSet对象
     * @return UserVO对象
     * @throws SQLException SQL异常
     */
    private UserVO mapResultSetToUserVO(ResultSet rs) throws SQLException {
        UserVO user = new UserVO();
        user.setUserId(rs.getInt("user_id")); // 设置数据库主键ID
        user.setId(rs.getString("login_id")); // 设置登录ID
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getInt("role"));
        return user;
    }
}
