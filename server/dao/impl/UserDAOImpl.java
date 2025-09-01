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
        String sql = "INSERT INTO users (login_id, name, password, role, status, phone, email, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, user.getLoginId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getRole() != null ? user.getRole() : 0);
            pstmt.setInt(5, user.getStatus() != null ? user.getStatus() : 0);
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getEmail());
            pstmt.setDouble(8, user.getBalance() != null ? user.getBalance() : 0.0);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("插入用户失败: " + e.getMessage());
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
        String sql = "UPDATE users SET name = ?, phone = ?, email = ?, role = ?, status = ?, balance = ?, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPhone());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getRole() != null ? user.getRole() : 0);
            pstmt.setInt(5, user.getStatus() != null ? user.getStatus() : 0);
            pstmt.setDouble(6, user.getBalance() != null ? user.getBalance() : 0.0);
            pstmt.setInt(7, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新用户失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public UserVO findById(Integer userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
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
        String sql = "SELECT * FROM users ORDER BY user_id";
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
        String sql = "SELECT * FROM users WHERE login_id = ?";
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
        String sql = "SELECT * FROM users WHERE login_id = ? AND password = ? AND status = 1";
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
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY user_id";
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
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY user_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<UserVO> users = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUserVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据状态查询用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return users;
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
        String sql = "UPDATE users SET balance = balance + ?, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
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
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean activateUser(Integer userId) {
        String sql = "UPDATE users SET status = 1, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("激活用户失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean deactivateUser(Integer userId) {
        String sql = "UPDATE users SET status = 0, updated_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("停用用户失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public List<UserVO> findByNameLike(String name) {
        String sql = "SELECT * FROM users WHERE name LIKE ? ORDER BY user_id";
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
            System.err.println("根据姓名模糊查询用户失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return users;
    }
    
    @Override
    public Double getBalance(Integer userId) {
        String sql = "SELECT balance FROM users WHERE user_id = ?";
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
        user.setUserId(rs.getInt("user_id"));
        user.setLoginId(rs.getString("login_id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getInt("role"));
        user.setStatus(rs.getInt("status"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setBalance(rs.getDouble("balance"));
        user.setCreatedTime(rs.getTimestamp("created_time"));
        user.setUpdatedTime(rs.getTimestamp("updated_time"));
        return user;
    }
}
