package server.dao.impl;

import common.vo.PostVO;
import server.dao.PostDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛回复数据访问实现类
 */
public class PostDAOImpl implements PostDAO {
    
    @Override
    public Integer insert(PostVO post) {
        return createReply(post, post.getAuthorId());
    }
    
    @Override
    public boolean deleteById(Integer id) {
        return softDeletePost(id);
    }
    
    @Override
    public boolean update(PostVO post) {
        String sql = "UPDATE forum_posts SET content = ?, edited_time = NOW(), edit_count = edit_count + 1 WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, post.getContent());
            ps.setInt(2, post.getPostId());
            
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("更新回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public PostVO findById(Integer id) {
        String sql = "SELECT p.*, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE p.post_id = ? AND p.status = 1";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPostVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public List<PostVO> findAll() {
        // 这个方法通常不会被调用，因为回复数量可能很大
        return new ArrayList<>();
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM forum_posts WHERE status = 1";
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
            System.err.println("统计回复数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(Integer id) {
        return existsPost(id);
    }
    
    @Override
    public List<PostVO> findByThreadId(Integer threadId) {
        return findByThreadIdWithUserInfo(threadId, null);
    }
    
    @Override
    public List<PostVO> findByThreadIdWithUserInfo(Integer threadId, Integer currentUserId) {
        System.out.println("[PostDAO] 查询主题回复: threadId=" + threadId + ", currentUserId=" + currentUserId);
        List<PostVO> list = new ArrayList<>();
        
        String sql = "SELECT p.*, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id, " +
                "COALESCE(ps.name, pte.name, pa.username, pu.login_id) AS parent_author_name, " +
                "pq.content AS quoted_content, " +
                "COALESCE(qs.name, qte.name, qa.username, qu.login_id) AS quoted_author_name " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "LEFT JOIN forum_posts pp ON p.parent_post_id = pp.post_id " +
                "LEFT JOIN users pu ON pp.author_id = pu.user_id " +
                "LEFT JOIN students ps ON ps.user_id = pu.user_id " +
                "LEFT JOIN teachers pte ON pte.user_id = pu.user_id " +
                "LEFT JOIN admins pa ON pa.user_id = pu.user_id " +
                "LEFT JOIN forum_posts pq ON p.quote_post_id = pq.post_id " +
                "LEFT JOIN users qu ON pq.author_id = qu.user_id " +
                "LEFT JOIN students qs ON qs.user_id = qu.user_id " +
                "LEFT JOIN teachers qte ON qte.user_id = qu.user_id " +
                "LEFT JOIN admins qa ON qa.user_id = qu.user_id " +
                "WHERE p.thread_id = ? AND p.status = 1 " +
                "ORDER BY p.reply_level ASC, p.reply_path ASC, p.created_time ASC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threadId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PostVO post = mapResultSetToPostVO(rs);
                
                // 设置父回复作者信息
                post.setParentAuthorName(rs.getString("parent_author_name"));
                
                // 设置引用回复信息
                post.setQuotedContent(rs.getString("quoted_content"));
                post.setQuotedAuthorName(rs.getString("quoted_author_name"));
                
                // 设置用户点赞状态
                if (currentUserId != null) {
                    boolean isLiked = isPostLiked(post.getPostId(), currentUserId);
                    post.setIsLiked(isLiked);
                } else {
                    post.setIsLiked(false);
                }
                
                list.add(post);
            }
            
            System.out.println("[PostDAO] 查询到回复数量: " + list.size());
        } catch (SQLException e) {
            System.err.println("查询主题回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }
    
    @Override
    public List<PostVO> findByParentPostId(Integer parentPostId) {
        return findByParentPostIdWithUserInfo(parentPostId, null);
    }
    
    @Override
    public List<PostVO> findByParentPostIdWithUserInfo(Integer parentPostId, Integer currentUserId) {
        List<PostVO> list = new ArrayList<>();
        
        String sql = "SELECT p.*, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE p.parent_post_id = ? AND p.status = 1 " +
                "ORDER BY p.created_time ASC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, parentPostId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PostVO post = mapResultSetToPostVO(rs);
                
                // 设置用户点赞状态
                if (currentUserId != null) {
                    boolean isLiked = isPostLiked(post.getPostId(), currentUserId);
                    post.setIsLiked(isLiked);
                } else {
                    post.setIsLiked(false);
                }
                
                list.add(post);
            }
        } catch (SQLException e) {
            System.err.println("查询子回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }
    
    @Override
    public List<PostVO> findByReplyPath(String replyPath) {
        List<PostVO> list = new ArrayList<>();
        
        String sql = "SELECT p.*, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE p.reply_path LIKE ? AND p.status = 1 " +
                "ORDER BY p.created_time ASC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, replyPath + "%");
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PostVO post = mapResultSetToPostVO(rs);
                list.add(post);
            }
        } catch (SQLException e) {
            System.err.println("根据路径查询回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }
    
    @Override
    public List<PostVO> findByAuthorId(Integer authorId) {
        List<PostVO> list = new ArrayList<>();
        
        String sql = "SELECT p.*, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE p.author_id = ? AND p.status = 1 " +
                "ORDER BY p.created_time DESC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, authorId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PostVO post = mapResultSetToPostVO(rs);
                list.add(post);
            }
        } catch (SQLException e) {
            System.err.println("查询用户回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }
    
    @Override
    public Integer createReply(PostVO post, Integer authorUserId) {
        String sql = "INSERT INTO forum_posts (thread_id, content, author_id, parent_post_id, quote_post_id, reply_level, reply_path, like_count, created_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW(), 1)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, post.getThreadId());
            ps.setString(2, post.getContent());
            ps.setInt(3, authorUserId);
            
            // 设置父回复ID
            if (post.getParentPostId() != null) {
                ps.setInt(4, post.getParentPostId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            
            // 设置引用回复ID
            if (post.getQuotePostId() != null) {
                ps.setInt(5, post.getQuotePostId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            // 计算回复层级和路径
            Integer replyLevel = calculateReplyLevel(post.getParentPostId());
            String replyPath = calculateReplyPath(post.getParentPostId());
            
            ps.setInt(6, replyLevel);
            ps.setString(7, replyPath);
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    Integer postId = rs.getInt(1);
                    
                    // 更新主题回复统计
                    updateThreadReplyStats(post.getThreadId());
                    
                    System.out.println("[PostDAO] 创建回复成功: postId=" + postId + ", threadId=" + post.getThreadId() + ", replyLevel=" + replyLevel + ", replyPath=" + replyPath);
                    return postId;
                }
            }
        } catch (SQLException e) {
            System.err.println("创建回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public Integer createSubReply(PostVO post, Integer parentPostId, Integer authorUserId) {
        post.setParentPostId(parentPostId);
        return createReply(post, authorUserId);
    }
    
    @Override
    public Integer createQuoteReply(PostVO post, Integer quotePostId, Integer authorUserId) {
        post.setQuotePostId(quotePostId);
        return createReply(post, authorUserId);
    }
    
    @Override
    public Integer getReplyLevel(Integer postId) {
        String sql = "SELECT reply_level FROM forum_posts WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("reply_level");
            }
        } catch (SQLException e) {
            System.err.println("获取回复层级失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 0;
    }
    
    @Override
    public String getReplyPath(Integer postId) {
        String sql = "SELECT reply_path FROM forum_posts WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("reply_path");
            }
        } catch (SQLException e) {
            System.err.println("获取回复路径失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }
    
    @Override
    public String calculateReplyPath(Integer parentPostId) {
        if (parentPostId == null) {
            // 顶级回复，生成新的路径
            String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(reply_path, '/', 1) AS UNSIGNED)), 0) + 1 FROM forum_posts WHERE parent_post_id IS NULL";
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                conn = DatabaseUtil.getConnection();
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("计算顶级回复路径失败: " + e.getMessage());
            } finally {
                DatabaseUtil.closeAll(conn, ps, rs);
            }
            return "1";
        } else {
            // 子回复，基于父回复路径生成
            String parentPath = getReplyPath(parentPostId);
            if (parentPath != null) {
                String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(reply_path, '/', -1) AS UNSIGNED)), 0) + 1 FROM forum_posts WHERE reply_path LIKE ?";
                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                
                try {
                    conn = DatabaseUtil.getConnection();
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, parentPath + "/%");
                    rs = ps.executeQuery();
                    
                    if (rs.next()) {
                        return parentPath + "/" + rs.getInt(1);
                    }
                } catch (SQLException e) {
                    System.err.println("计算子回复路径失败: " + e.getMessage());
                } finally {
                    DatabaseUtil.closeAll(conn, ps, rs);
                }
                return parentPath + "/1";
            }
        }
        return null;
    }
    
    @Override
    public boolean updateThreadReplyStats(Integer threadId) {
        String sql = "UPDATE forum_threads SET reply_count = (SELECT COUNT(*) FROM forum_posts WHERE thread_id = ? AND status = 1), last_post_time = NOW() WHERE thread_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threadId);
            ps.setInt(2, threadId);
            
            int affected = ps.executeUpdate();
            System.out.println("[PostDAO] 更新主题回复统计: threadId=" + threadId + ", affected=" + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("更新主题回复统计失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public boolean softDeletePost(Integer postId) {
        String sql = "UPDATE forum_posts SET status = 0 WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                // 更新主题回复统计
                String threadSql = "SELECT thread_id FROM forum_posts WHERE post_id = ?";
                PreparedStatement threadPs = conn.prepareStatement(threadSql);
                threadPs.setInt(1, postId);
                ResultSet rs = threadPs.executeQuery();
                
                if (rs.next()) {
                    updateThreadReplyStats(rs.getInt("thread_id"));
                }
                
                DatabaseUtil.closeAll(null, threadPs, rs);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("软删除回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
        return false;
    }
    
    @Override
    public boolean existsPost(Integer postId) {
        String sql = "SELECT COUNT(*) FROM forum_posts WHERE post_id = ? AND status = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查回复是否存在失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return false;
    }
    
    @Override
    public boolean isPostBelongsToThread(Integer postId, Integer threadId) {
        String sql = "SELECT COUNT(*) FROM forum_posts WHERE post_id = ? AND thread_id = ? AND status = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.setInt(2, threadId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查回复是否属于主题失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return false;
    }
    
    /**
     * 计算回复层级
     * @param parentPostId 父回复ID
     * @return 回复层级
     */
    private Integer calculateReplyLevel(Integer parentPostId) {
        if (parentPostId == null) {
            return 0; // 顶级回复
        }
        
        String sql = "SELECT reply_level FROM forum_posts WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, parentPostId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("reply_level") + 1;
            }
        } catch (SQLException e) {
            System.err.println("计算回复层级失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 1; // 默认二级回复
    }
    
    /**
     * 获取回复的子回复数量
     * @param postId 回复ID
     * @return 子回复数量
     */
    private int getReplyCount(Integer postId) {
        String sql = "SELECT COUNT(*) FROM forum_posts WHERE parent_post_id = ? AND status = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("获取回复数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return 0;
    }
    
    /**
     * 检查用户是否已点赞回复
     * @param postId 回复ID
     * @param userId 用户ID
     * @return true表示已点赞
     */
    private boolean isPostLiked(Integer postId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM forum_likes WHERE entity_type = 'post' AND entity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查回复点赞状态失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return false;
    }
    
    /**
     * 将ResultSet映射为PostVO对象
     * @param rs ResultSet
     * @return PostVO对象
     * @throws SQLException SQL异常
     */
    private PostVO mapResultSetToPostVO(ResultSet rs) throws SQLException {
        PostVO post = new PostVO();
        post.setPostId(rs.getInt("post_id"));
        post.setThreadId(rs.getInt("thread_id"));
        post.setContent(rs.getString("content"));
        post.setAuthorId(rs.getInt("author_id"));
        post.setParentPostId((Integer) rs.getObject("parent_post_id"));
        post.setQuotePostId((Integer) rs.getObject("quote_post_id"));
        post.setReplyLevel((Integer) rs.getObject("reply_level"));
        post.setReplyPath(rs.getString("reply_path"));
        
        post.setCreatedTime(rs.getTimestamp("created_time"));
        
        post.setStatus((Integer) rs.getObject("status"));
        post.setLikeCount((Integer) rs.getObject("like_count"));
        
        // 计算回复数（子回复数量）
        int replyCount = getReplyCount(post.getPostId());
        post.setReplyCount(replyCount);
        
        post.setAuthorName(rs.getString("author_name"));
        post.setAuthorLoginId(rs.getString("author_login_id"));
        return post;
    }
}
