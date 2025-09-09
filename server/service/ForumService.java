package server.service;

import common.vo.PostVO;
import common.vo.ThreadVO;
import server.util.DatabaseUtil;
import common.vo.ForumSectionVO;
import server.dao.ForumLikeDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛服务：提供主题与回复的基础查询/创建能力
 */
public class ForumService {
    
    private ForumLikeDAO likeDAO = new ForumLikeDAO();

    public List<ThreadVO> getAllThreads() {
        return getAllThreads(null);
    }
    
    public List<ThreadVO> getAllThreads(Integer currentUserId) {
        System.out.println("[Forum][Server][DAO] 准备执行SQL: 查询所有主题");
        System.out.println("[DEBUG] ========== 开始查询所有论坛主题 ==========");
        List<ThreadVO> list = new ArrayList<ThreadVO>();
        String sql = "SELECT t.thread_id, t.title, t.content, t.author_id, t.reply_count, t.view_count, t.like_count, t.favorite_count, t.created_time, t.updated_time, t.status, " +
                "t.section_id, fs.name AS section_name, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id, u.role AS author_role " +
                "FROM forum_threads t " +
                "LEFT JOIN forum_sections fs ON t.section_id = fs.section_id " +
                "LEFT JOIN users u ON t.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE t.status = 1 ORDER BY t.is_pinned DESC, t.last_post_time DESC, t.created_time DESC";
        System.out.println("[DEBUG] SQL查询语句: " + sql);

        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            System.out.println("[Forum][Server][DAO] 获取连接成功: " + (conn != null));
            
            ps = conn.prepareStatement(sql);
            System.out.println("[Forum][Server][DAO] 预编译完成，开始执行查询");
            rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                ThreadVO vo = new ThreadVO();
                vo.setThreadId(rs.getInt("thread_id"));
                vo.setTitle(rs.getString("title"));
                vo.setContent(rs.getString("content"));
                vo.setAuthorId((Integer) rs.getObject("author_id"));
                vo.setReplyCount((Integer) rs.getObject("reply_count"));
                vo.setViewCount((Integer) rs.getObject("view_count"));
                vo.setLikeCount((Integer) rs.getObject("like_count"));
                vo.setFavoriteCount((Integer) rs.getObject("favorite_count"));
                vo.setCreatedTime(rs.getTimestamp("created_time"));
                vo.setUpdatedTime(rs.getTimestamp("updated_time"));
                vo.setStatus((Integer) rs.getObject("status"));
                vo.setAuthorName(rs.getString("author_name"));
                vo.setAuthorLoginId(rs.getString("author_login_id"));
                
                // 分区
                try {
                    vo.setSectionId((Integer) rs.getObject("section_id"));
                    vo.setSectionName(rs.getString("section_name"));
                } catch (Exception ignore) {}
                
                // 管理员发布的帖子标记为公告（users.role = 2）
                boolean isAnnouncement = false;
                try {
                    int role = rs.getInt("author_role");
                    isAnnouncement = (role == 2);
                    vo.setIsAnnouncement(isAnnouncement);
                    System.out.println("[DEBUG] 帖子ID=" + vo.getThreadId() + ", 作者ID=" + vo.getAuthorId() + ", 角色=" + role + ", 是否公告=" + isAnnouncement);
                } catch (Exception e) {
                    vo.setIsAnnouncement(false);
                    System.out.println("[DEBUG] 获取角色信息失败: " + e.getMessage());
                }
                
                // 调试输出：检查点赞和回复数据
                try {
                    Integer replyCount = (Integer) rs.getObject("reply_count");
                    Integer likeCount = (Integer) rs.getObject("like_count");
                    Integer viewCount = (Integer) rs.getObject("view_count");
                    Integer favoriteCount = (Integer) rs.getObject("favorite_count");
                    System.out.println("[DEBUG] 帖子ID=" + vo.getThreadId() + ", 标题=" + vo.getTitle() + 
                                     ", 回复数=" + replyCount + ", 点赞数=" + likeCount + 
                                     ", 浏览数=" + viewCount + ", 收藏数=" + favoriteCount);
                } catch (Exception e) {
                    System.out.println("[DEBUG] 获取统计数据失败: " + e.getMessage());
                }
                
                // 设置用户点赞状态
                if (currentUserId != null) {
                    boolean isLiked = likeDAO.isLiked("thread", vo.getThreadId(), currentUserId);
                    vo.setIsLiked(isLiked);
                } else {
                    vo.setIsLiked(false);
                }
                
                list.add(vo);
                count++;
                
                if (count <= 3) {
                    System.out.println("[Forum][Server][DAO] 示例数据: id=" + vo.getThreadId() + ", title=" + vo.getTitle());
                }
            }
            System.out.println("[Forum][Server][DAO] 查询结束，总数=" + count);
            System.out.println("[DEBUG] ========== 查询完成，返回数据列表 ==========");
            System.out.println("[DEBUG] 返回的ThreadVO列表大小: " + list.size());
            for (ThreadVO vo : list) {
                System.out.println("[DEBUG] 最终返回数据 - ID=" + vo.getThreadId() + 
                                 ", 标题=" + vo.getTitle() + 
                                 ", 作者=" + vo.getAuthorName() + 
                                 ", 是否公告=" + vo.getIsAnnouncement() + 
                                 ", 回复数=" + vo.getReplyCount());
            }
        } catch (SQLException e) {
            System.err.println("查询主题失败: " + e.getMessage());
            System.out.println("[DEBUG] SQL异常详情: " + e.toString());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }

    public List<PostVO> getPostsByThreadId(int threadId) {
        return getPostsByThreadId(threadId, null);
    }
    
    public List<PostVO> getPostsByThreadId(int threadId, Integer currentUserId) {
        System.out.println("[Forum][Server][DAO] 准备执行SQL: 查询回复 threadId=" + threadId);
        List<PostVO> list = new ArrayList<PostVO>();
        String sql = "SELECT p.post_id, p.thread_id, p.content, p.author_id, p.created_time, p.status, p.like_count, " +
                "COALESCE(s.name, te.name, a.username, u.login_id) AS author_name, u.login_id AS author_login_id " +
                "FROM forum_posts p " +
                "LEFT JOIN users u ON p.author_id = u.user_id " +
                "LEFT JOIN students s ON s.user_id = u.user_id " +
                "LEFT JOIN teachers te ON te.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE p.thread_id = ? AND p.status = 1 ORDER BY p.created_time ASC";

        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threadId);
            System.out.println("[Forum][Server][DAO] 参数绑定完成，开始执行查询");
            rs = ps.executeQuery();
            while (rs.next()) {
                PostVO vo = new PostVO();
                vo.setPostId(rs.getInt("post_id"));
                vo.setThreadId(rs.getInt("thread_id"));
                vo.setContent(rs.getString("content"));
                vo.setAuthorId((Integer) rs.getObject("author_id"));
                vo.setCreatedTime(rs.getTimestamp("created_time"));
                vo.setStatus((Integer) rs.getObject("status"));
                vo.setLikeCount((Integer) rs.getObject("like_count"));
                vo.setAuthorName(rs.getString("author_name"));
                vo.setAuthorLoginId(rs.getString("author_login_id"));
                
                // 设置用户点赞状态
                if (currentUserId != null) {
                    boolean isLiked = likeDAO.isLiked("post", vo.getPostId(), currentUserId);
                    vo.setIsLiked(isLiked);
                } else {
                    vo.setIsLiked(false);
                }
                
                list.add(vo);
            }
            System.out.println("[Forum][Server][DAO] 回复查询结束，返回条数=" + list.size());
        } catch (SQLException e) {
            System.err.println("查询回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return list;
    }

    /**
     * 查询所有启用的分区（板块），按 sort_order 升序
     */
    public List<ForumSectionVO> getAllSections() {
        System.out.println("[Forum][Server][DAO] 准备执行SQL: 查询分区列表");
        List<ForumSectionVO> sections = new ArrayList<ForumSectionVO>();
        String sql = "SELECT section_id, name, description, sort_order, status, created_time FROM forum_sections WHERE status = 1 ORDER BY sort_order ASC, section_id ASC";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ForumSectionVO vo = new ForumSectionVO();
                vo.setSectionId((Integer) rs.getObject("section_id"));
                vo.setName(rs.getString("name"));
                vo.setDescription(rs.getString("description"));
                vo.setSortOrder((Integer) rs.getObject("sort_order"));
                vo.setStatus((Integer) rs.getObject("status"));
                vo.setCreatedTime(rs.getTimestamp("created_time"));
                sections.add(vo);
            }
            System.out.println("[Forum][Server][DAO] 分区查询结束，返回条数=" + sections.size());
        } catch (SQLException e) {
            System.err.println("查询论坛分区失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return sections;
    }

    public Integer createThread(ThreadVO thread, int authorUserId) {
        String sql = "INSERT INTO forum_threads (title, content, author_id, section_id, category, is_essence, reply_count, view_count, like_count, favorite_count, is_pinned, is_locked, status, created_time, last_post_time) " +
                "VALUES (?, ?, ?, ?, NULL, FALSE, 0, 0, 0, 0, FALSE, FALSE, 1, NOW(), NOW())";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, thread.getTitle());
            ps.setString(2, thread.getContent());
            ps.setInt(3, authorUserId);
            if (thread.getSectionId() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, thread.getSectionId());
            }
            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("创建主题失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }

    public Integer createPost(PostVO post, int authorUserId) {
        String sql = "INSERT INTO forum_posts (thread_id, content, author_id, parent_post_id, like_count, created_time, status) " +
                "VALUES (?, ?, ?, NULL, 0, NOW(), 1)";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, post.getThreadId());
            ps.setString(2, post.getContent());
            ps.setInt(3, authorUserId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    // 同步更新主题回复数与最后回复时间
                    updateThreadReplyMeta(post.getThreadId());
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("创建回复失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, rs);
        }
        return null;
    }

    private void updateThreadReplyMeta(int threadId) {
        String sql = "UPDATE forum_threads SET reply_count = (SELECT COUNT(*) FROM forum_posts WHERE thread_id = ? AND status = 1), last_post_time = NOW() WHERE thread_id = ?";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, threadId);
            ps.setInt(2, threadId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新主题元信息失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, ps, null);
        }
    }
    
    /**
     * 切换主题点赞状态
     * @param threadId 主题ID
     * @param userId 用户ID
     * @return true表示点赞成功，false表示取消点赞成功，null表示操作失败
     */
    public Boolean toggleThreadLike(int threadId, int userId) {
        System.out.println("[Forum][Service] 切换主题点赞状态: threadId=" + threadId + ", userId=" + userId);
        return likeDAO.toggleLike("thread", threadId, userId);
    }
    
    /**
     * 切换回复点赞状态
     * @param postId 回复ID
     * @param userId 用户ID
     * @return true表示点赞成功，false表示取消点赞成功，null表示操作失败
     */
    public Boolean togglePostLike(int postId, int userId) {
        System.out.println("[Forum][Service] 切换回复点赞状态: postId=" + postId + ", userId=" + userId);
        return likeDAO.toggleLike("post", postId, userId);
    }
    
    /**
     * 检查用户是否已点赞主题
     * @param threadId 主题ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    public boolean isThreadLiked(int threadId, int userId) {
        return likeDAO.isLiked("thread", threadId, userId);
    }
    
    /**
     * 检查用户是否已点赞回复
     * @param postId 回复ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    public boolean isPostLiked(int postId, int userId) {
        return likeDAO.isLiked("post", postId, userId);
    }
    
    /**
     * 获取主题点赞数量
     * @param threadId 主题ID
     * @return 点赞数量
     */
    public int getThreadLikeCount(int threadId) {
        return likeDAO.getLikeCount("thread", threadId);
    }
    
    /**
     * 获取回复点赞数量
     * @param postId 回复ID
     * @return 点赞数量
     */
    public int getPostLikeCount(int postId) {
        return likeDAO.getLikeCount("post", postId);
    }
}


