package server.dao;

import common.vo.PostVO;
import java.util.List;

/**
 * 论坛回复数据访问接口
 * 定义回复相关的数据库操作方法
 */
public interface PostDAO extends BaseDAO<PostVO, Integer> {
    
    /**
     * 根据主题ID查询所有回复（按层级和时间排序）
     * @param threadId 主题ID
     * @return 回复列表
     */
    List<PostVO> findByThreadId(Integer threadId);
    
    /**
     * 根据主题ID查询所有回复（包含用户信息）
     * @param threadId 主题ID
     * @param currentUserId 当前用户ID（用于设置点赞状态）
     * @return 回复列表
     */
    List<PostVO> findByThreadIdWithUserInfo(Integer threadId, Integer currentUserId);
    
    /**
     * 根据父回复ID查询子回复
     * @param parentPostId 父回复ID
     * @return 子回复列表
     */
    List<PostVO> findByParentPostId(Integer parentPostId);
    
    /**
     * 根据父回复ID查询子回复（包含用户信息）
     * @param parentPostId 父回复ID
     * @param currentUserId 当前用户ID
     * @return 子回复列表
     */
    List<PostVO> findByParentPostIdWithUserInfo(Integer parentPostId, Integer currentUserId);
    
    /**
     * 根据回复路径查询子回复
     * @param replyPath 回复路径
     * @return 子回复列表
     */
    List<PostVO> findByReplyPath(String replyPath);
    
    /**
     * 根据作者ID查询回复列表
     * @param authorId 作者ID
     * @return 回复列表
     */
    List<PostVO> findByAuthorId(Integer authorId);
    
    /**
     * 创建回复（支持嵌套回复）
     * @param post 回复对象
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    Integer createReply(PostVO post, Integer authorUserId);
    
    /**
     * 创建对回复的回复
     * @param post 回复对象
     * @param parentPostId 父回复ID
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    Integer createSubReply(PostVO post, Integer parentPostId, Integer authorUserId);
    
    /**
     * 创建引用回复
     * @param post 回复对象
     * @param quotePostId 被引用的回复ID
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    Integer createQuoteReply(PostVO post, Integer quotePostId, Integer authorUserId);
    
    /**
     * 获取回复的层级
     * @param postId 回复ID
     * @return 层级，顶级回复返回0
     */
    Integer getReplyLevel(Integer postId);
    
    /**
     * 获取回复的路径
     * @param postId 回复ID
     * @return 回复路径
     */
    String getReplyPath(Integer postId);
    
    /**
     * 计算回复路径
     * @param parentPostId 父回复ID
     * @return 计算出的回复路径
     */
    String calculateReplyPath(Integer parentPostId);
    
    /**
     * 更新主题的回复统计信息
     * @param threadId 主题ID
     * @return 更新成功返回true
     */
    boolean updateThreadReplyStats(Integer threadId);
    
    /**
     * 删除回复（软删除）
     * @param postId 回复ID
     * @return 删除成功返回true
     */
    boolean softDeletePost(Integer postId);
    
    /**
     * 检查回复是否存在
     * @param postId 回复ID
     * @return 存在返回true
     */
    boolean existsPost(Integer postId);
    
    /**
     * 检查回复是否属于指定主题
     * @param postId 回复ID
     * @param threadId 主题ID
     * @return 属于返回true
     */
    boolean isPostBelongsToThread(Integer postId, Integer threadId);
}
