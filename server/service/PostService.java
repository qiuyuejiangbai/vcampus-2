package server.service;

import common.vo.PostVO;
import server.dao.PostDAO;
import server.dao.impl.PostDAOImpl;
import server.dao.ForumLikeDAO;

import java.util.List;

/**
 * 论坛回复服务类
 * 提供回复相关的业务逻辑处理
 */
public class PostService {
    
    private PostDAO postDAO = new PostDAOImpl();
    private ForumLikeDAO likeDAO = new ForumLikeDAO();
    
    /**
     * 获取主题的所有回复（按层级和时间排序）
     * @param threadId 主题ID
     * @return 回复列表
     */
    public List<PostVO> getPostsByThreadId(Integer threadId) {
        return getPostsByThreadId(threadId, null);
    }
    
    /**
     * 获取主题的所有回复（包含用户信息）
     * @param threadId 主题ID
     * @param currentUserId 当前用户ID（用于设置点赞状态）
     * @return 回复列表
     */
    public List<PostVO> getPostsByThreadId(Integer threadId, Integer currentUserId) {
        System.out.println("[PostService] 获取主题回复: threadId=" + threadId + ", currentUserId=" + currentUserId);
        return postDAO.findByThreadIdWithUserInfo(threadId, currentUserId);
    }
    
    /**
     * 获取子回复
     * @param parentPostId 父回复ID
     * @return 子回复列表
     */
    public List<PostVO> getSubReplies(Integer parentPostId) {
        return getSubReplies(parentPostId, null);
    }
    
    /**
     * 获取子回复（包含用户信息）
     * @param parentPostId 父回复ID
     * @param currentUserId 当前用户ID
     * @return 子回复列表
     */
    public List<PostVO> getSubReplies(Integer parentPostId, Integer currentUserId) {
        System.out.println("[PostService] 获取子回复: parentPostId=" + parentPostId + ", currentUserId=" + currentUserId);
        return postDAO.findByParentPostIdWithUserInfo(parentPostId, currentUserId);
    }
    
    /**
     * 创建对主题的回复
     * @param post 回复对象
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    public Integer createThreadReply(PostVO post, Integer authorUserId) {
        System.out.println("[PostService] 创建主题回复: threadId=" + post.getThreadId() + ", authorUserId=" + authorUserId);
        
        // 验证参数
        if (post == null || post.getThreadId() == null || post.getContent() == null || authorUserId == null) {
            System.err.println("[PostService] 创建主题回复失败：参数不完整");
            return null;
        }
        
        // 确保是顶级回复
        post.setParentPostId(null);
        post.setQuotePostId(null);
        
        return postDAO.createReply(post, authorUserId);
    }
    
    /**
     * 创建对回复的回复
     * @param post 回复对象
     * @param parentPostId 父回复ID
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    public Integer createSubReply(PostVO post, Integer parentPostId, Integer authorUserId) {
        System.out.println("[PostService] 创建子回复: parentPostId=" + parentPostId + ", authorUserId=" + authorUserId);
        
        // 验证参数
        if (post == null || parentPostId == null || post.getContent() == null || authorUserId == null) {
            System.err.println("[PostService] 创建子回复失败：参数不完整");
            return null;
        }
        
        // 验证父回复是否存在
        if (!postDAO.existsPost(parentPostId)) {
            System.err.println("[PostService] 创建子回复失败：父回复不存在");
            return null;
        }
        
        // 验证父回复是否属于同一主题
        if (!postDAO.isPostBelongsToThread(parentPostId, post.getThreadId())) {
            System.err.println("[PostService] 创建子回复失败：父回复不属于同一主题");
            return null;
        }
        
        return postDAO.createSubReply(post, parentPostId, authorUserId);
    }
    
    /**
     * 创建引用回复
     * @param post 回复对象
     * @param quotePostId 被引用的回复ID
     * @param authorUserId 作者用户ID
     * @return 创建的回复ID，失败返回null
     */
    public Integer createQuoteReply(PostVO post, Integer quotePostId, Integer authorUserId) {
        System.out.println("[PostService] 创建引用回复: quotePostId=" + quotePostId + ", authorUserId=" + authorUserId);
        
        // 验证参数
        if (post == null || quotePostId == null || post.getContent() == null || authorUserId == null) {
            System.err.println("[PostService] 创建引用回复失败：参数不完整");
            return null;
        }
        
        // 验证被引用的回复是否存在
        if (!postDAO.existsPost(quotePostId)) {
            System.err.println("[PostService] 创建引用回复失败：被引用的回复不存在");
            return null;
        }
        
        // 验证被引用的回复是否属于同一主题
        if (!postDAO.isPostBelongsToThread(quotePostId, post.getThreadId())) {
            System.err.println("[PostService] 创建引用回复失败：被引用的回复不属于同一主题");
            return null;
        }
        
        return postDAO.createQuoteReply(post, quotePostId, authorUserId);
    }
    
    /**
     * 获取回复详情
     * @param postId 回复ID
     * @return 回复对象，不存在返回null
     */
    public PostVO getPostById(Integer postId) {
        System.out.println("[PostService] 获取回复详情: postId=" + postId);
        return postDAO.findById(postId);
    }
    
    /**
     * 更新回复内容
     * @param post 回复对象
     * @param userId 用户ID（用于权限验证）
     * @return 更新成功返回true
     */
    public boolean updatePost(PostVO post, Integer userId) {
        System.out.println("[PostService] 更新回复: postId=" + post.getPostId() + ", userId=" + userId);
        
        // 验证参数
        if (post == null || post.getPostId() == null || post.getContent() == null || userId == null) {
            System.err.println("[PostService] 更新回复失败：参数不完整");
            return false;
        }
        
        // 验证回复是否存在
        PostVO existingPost = postDAO.findById(post.getPostId());
        if (existingPost == null) {
            System.err.println("[PostService] 更新回复失败：回复不存在");
            return false;
        }
        
        // 验证权限（只有作者可以编辑）
        if (!existingPost.getAuthorId().equals(userId)) {
            System.err.println("[PostService] 更新回复失败：无权限编辑");
            return false;
        }
        
        return postDAO.update(post);
    }
    
    /**
     * 删除回复（软删除）
     * @param postId 回复ID
     * @param userId 用户ID（用于权限验证）
     * @return 删除成功返回true
     */
    public boolean deletePost(Integer postId, Integer userId) {
        System.out.println("[PostService] 删除回复: postId=" + postId + ", userId=" + userId);
        
        // 验证参数
        if (postId == null || userId == null) {
            System.err.println("[PostService] 删除回复失败：参数不完整");
            return false;
        }
        
        // 验证回复是否存在
        PostVO existingPost = postDAO.findById(postId);
        if (existingPost == null) {
            System.err.println("[PostService] 删除回复失败：回复不存在");
            return false;
        }
        
        // 验证权限（只有作者可以删除）
        if (!existingPost.getAuthorId().equals(userId)) {
            System.err.println("[PostService] 删除回复失败：无权限删除");
            return false;
        }
        
        return postDAO.softDeletePost(postId);
    }
    
    /**
     * 切换回复点赞状态
     * @param postId 回复ID
     * @param userId 用户ID
     * @return true表示点赞成功，false表示取消点赞成功，null表示操作失败
     */
    public Boolean togglePostLike(Integer postId, Integer userId) {
        System.out.println("[PostService] 切换回复点赞状态: postId=" + postId + ", userId=" + userId);
        
        // 验证参数
        if (postId == null || userId == null) {
            System.err.println("[PostService] 切换回复点赞失败：参数不完整");
            return null;
        }
        
        // 验证回复是否存在
        if (!postDAO.existsPost(postId)) {
            System.err.println("[PostService] 切换回复点赞失败：回复不存在");
            return null;
        }
        
        return likeDAO.toggleLike("post", postId, userId);
    }
    
    /**
     * 检查用户是否已点赞回复
     * @param postId 回复ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    public boolean isPostLiked(Integer postId, Integer userId) {
        return likeDAO.isLiked("post", postId, userId);
    }
    
    /**
     * 获取回复点赞数量
     * @param postId 回复ID
     * @return 点赞数量
     */
    public int getPostLikeCount(Integer postId) {
        return likeDAO.getLikeCount("post", postId);
    }
    
    /**
     * 获取用户的回复列表
     * @param authorId 作者ID
     * @return 回复列表
     */
    public List<PostVO> getUserPosts(Integer authorId) {
        System.out.println("[PostService] 获取用户回复: authorId=" + authorId);
        return postDAO.findByAuthorId(authorId);
    }
    
    /**
     * 检查回复是否存在
     * @param postId 回复ID
     * @return 存在返回true
     */
    public boolean existsPost(Integer postId) {
        return postDAO.existsPost(postId);
    }
    
    /**
     * 检查回复是否属于指定主题
     * @param postId 回复ID
     * @param threadId 主题ID
     * @return 属于返回true
     */
    public boolean isPostBelongsToThread(Integer postId, Integer threadId) {
        return postDAO.isPostBelongsToThread(postId, threadId);
    }
    
    /**
     * 获取回复的层级
     * @param postId 回复ID
     * @return 回复层级
     */
    public Integer getReplyLevel(Integer postId) {
        return postDAO.getReplyLevel(postId);
    }
    
    /**
     * 获取回复的路径
     * @param postId 回复ID
     * @return 回复路径
     */
    public String getReplyPath(Integer postId) {
        return postDAO.getReplyPath(postId);
    }
}
