package server.dao;

import server.util.DatabaseUtil;

import java.sql.*;

/**
 * 论坛点赞数据访问对象
 * 处理论坛主题和回复的点赞相关数据库操作
 */
public class ForumLikeDAO {
    
    /**
     * 检查用户是否已点赞某个实体（主题或回复）
     * @param entityType 实体类型：'thread' 或 'post'
     * @param entityId 实体ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    public boolean isLiked(String entityType, int entityId, int userId) {
        String sql = "SELECT COUNT(*) FROM forum_likes WHERE entity_type = ? AND entity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.setInt(3, userId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查点赞状态失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return false;
    }
    
    /**
     * 添加点赞记录
     * @param entityType 实体类型：'thread' 或 'post'
     * @param entityId 实体ID
     * @param userId 用户ID
     * @return true表示成功，false表示失败
     */
    public boolean addLike(String entityType, int entityId, int userId) {
        String sql = "INSERT INTO forum_likes (entity_type, entity_id, user_id, created_time) VALUES (?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.setInt(3, userId);
            
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("添加点赞失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    /**
     * 删除点赞记录
     * @param entityType 实体类型：'thread' 或 'post'
     * @param entityId 实体ID
     * @param userId 用户ID
     * @return true表示成功，false表示失败
     */
    public boolean removeLike(String entityType, int entityId, int userId) {
        String sql = "DELETE FROM forum_likes WHERE entity_type = ? AND entity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.setInt(3, userId);
            
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("删除点赞失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    /**
     * 获取实体的点赞数量
     * @param entityType 实体类型：'thread' 或 'post'
     * @param entityId 实体ID
     * @return 点赞数量
     */
    public int getLikeCount(String entityType, int entityId) {
        String sql;
        if ("thread".equals(entityType)) {
            // 从forum_threads表读取like_count字段，这样更可靠
            sql = "SELECT like_count FROM forum_threads WHERE thread_id = ?";
        } else if ("post".equals(entityType)) {
            // 从forum_posts表读取like_count字段
            sql = "SELECT like_count FROM forum_posts WHERE post_id = ?";
        } else {
            return 0;
        }
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entityId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("[Forum][DAO] 获取点赞数量: entityType=" + entityType + ", entityId=" + entityId + ", count=" + count);
                return count;
            }
        } catch (SQLException e) {
            System.err.println("获取点赞数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 0;
    }
    
    /**
     * 更新主题的点赞数量
     * @param threadId 主题ID
     * @return true表示成功，false表示失败
     */
    public boolean updateThreadLikeCount(int threadId) {
        String sql = "UPDATE forum_threads SET like_count = (SELECT COUNT(*) FROM forum_likes WHERE entity_type = 'thread' AND entity_id = ?) WHERE thread_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threadId);
            ps.setInt(2, threadId);
            
            int affected = ps.executeUpdate();
            System.out.println("[Forum][DAO] 更新主题点赞数量: threadId=" + threadId + ", affected=" + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("更新主题点赞数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    /**
     * 更新回复的点赞数量
     * @param postId 回复ID
     * @return true表示成功，false表示失败
     */
    public boolean updatePostLikeCount(int postId) {
        String sql = "UPDATE forum_posts SET like_count = (SELECT COUNT(*) FROM forum_likes WHERE entity_type = 'post' AND entity_id = ?) WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.setInt(2, postId);
            
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("更新回复点赞数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    /**
     * 切换点赞状态（点赞/取消点赞）
     * @param entityType 实体类型：'thread' 或 'post'
     * @param entityId 实体ID
     * @param userId 用户ID
     * @return true表示点赞成功，false表示取消点赞成功，null表示操作失败
     */
    public Boolean toggleLike(String entityType, int entityId, int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务
            
            // 在同一个事务中检查是否已点赞，避免死锁
            String checkSql = "SELECT COUNT(*) FROM forum_likes WHERE entity_type = ? AND entity_id = ? AND user_id = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.setInt(3, userId);
            
            boolean isLiked = false;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isLiked = rs.getInt(1) > 0;
                }
            }
            
            if (isLiked) {
                // 已点赞，执行取消点赞
                String deleteSql = "DELETE FROM forum_likes WHERE entity_type = ? AND entity_id = ? AND user_id = ?";
                ps = conn.prepareStatement(deleteSql);
                ps.setString(1, entityType);
                ps.setInt(2, entityId);
                ps.setInt(3, userId);
                
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    // 在同一个事务中更新点赞数量
                    if ("thread".equals(entityType)) {
                        updateThreadLikeCountInTransaction(conn, entityId);
                    } else if ("post".equals(entityType)) {
                        updatePostLikeCountInTransaction(conn, entityId);
                    }
                    conn.commit();
                    return false; // 取消点赞成功
                }
            } else {
                // 未点赞，执行点赞
                String insertSql = "INSERT INTO forum_likes (entity_type, entity_id, user_id, created_time) VALUES (?, ?, ?, NOW())";
                ps = conn.prepareStatement(insertSql);
                ps.setString(1, entityType);
                ps.setInt(2, entityId);
                ps.setInt(3, userId);
                
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    // 在同一个事务中更新点赞数量
                    if ("thread".equals(entityType)) {
                        updateThreadLikeCountInTransaction(conn, entityId);
                    } else if ("post".equals(entityType)) {
                        updatePostLikeCountInTransaction(conn, entityId);
                    }
                    conn.commit();
                    return true; // 点赞成功
                }
            }
            
            conn.rollback();
            return null; // 操作失败
            
        } catch (SQLException e) {
            System.err.println("切换点赞状态失败: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚事务失败: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("恢复自动提交失败: " + e.getMessage());
            }
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return null;
    }
    
    /**
     * 在事务中更新主题的点赞数量
     * @param conn 数据库连接
     * @param threadId 主题ID
     * @return true表示成功，false表示失败
     */
    private boolean updateThreadLikeCountInTransaction(Connection conn, int threadId) {
        String sql = "UPDATE forum_threads SET like_count = (SELECT COUNT(*) FROM forum_likes WHERE entity_type = 'thread' AND entity_id = ?) WHERE thread_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ps.setInt(2, threadId);
            int affected = ps.executeUpdate();
            System.out.println("[Forum][DAO] 在事务中更新主题点赞数量: threadId=" + threadId + ", affected=" + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("在事务中更新主题点赞数量失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 在事务中更新回复的点赞数量
     * @param conn 数据库连接
     * @param postId 回复ID
     * @return true表示成功，false表示失败
     */
    private boolean updatePostLikeCountInTransaction(Connection conn, int postId) {
        String sql = "UPDATE forum_posts SET like_count = (SELECT COUNT(*) FROM forum_likes WHERE entity_type = 'post' AND entity_id = ?) WHERE post_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, postId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("在事务中更新回复点赞数量失败: " + e.getMessage());
            return false;
        }
    }
}
