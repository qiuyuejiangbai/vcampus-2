package server.dao;

import common.vo.UserVO;
import java.util.List;

/**
 * 用户数据访问接口
 * 定义用户相关的数据库操作方法
 */
public interface UserDAO extends BaseDAO<UserVO, Integer> {
    
    /**
     * 根据登录ID查询用户
     * @param loginId 登录ID
     * @return 用户对象，不存在返回null
     */
    UserVO findByLoginId(String loginId);
    
    /**
     * 根据登录ID和密码验证用户
     * @param loginId 登录ID
     * @param password 密码哈希
     * @return 验证成功返回用户对象，失败返回null
     */
    UserVO authenticate(String loginId, String password);
    
    /**
     * 检查登录ID是否存在
     * @param loginId 登录ID
     * @return 存在返回true，不存在返回false
     */
    boolean existsByLoginId(String loginId);
    
    /**
     * 根据角色查询用户列表
     * @param role 角色：0-学生，1-教师，2-管理员
     * @return 用户列表
     */
    List<UserVO> findByRole(Integer role);
    
    /**
     * 根据状态查询用户列表
     * @param status 状态：0-未激活，1-已激活
     * @return 用户列表
     */
    List<UserVO> findByStatus(Integer status);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码哈希
     * @return 更新成功返回true，失败返回false
     */
    boolean updatePassword(Integer userId, String newPassword);
    
    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 金额（正数为增加，负数为减少）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateBalance(Integer userId, Double amount);
    
    /**
     * 激活用户账户
     * @param userId 用户ID
     * @return 激活成功返回true，失败返回false
     */
    boolean activateUser(Integer userId);
    
    /**
     * 停用用户账户
     * @param userId 用户ID
     * @return 停用成功返回true，失败返回false
     */
    boolean deactivateUser(Integer userId);
    
    /**
     * 根据姓名模糊查询用户
     * @param name 姓名关键词
     * @return 用户列表
     */
    List<UserVO> findByNameLike(String name);
    
    /**
     * 获取用户余额
     * @param userId 用户ID
     * @return 用户余额，用户不存在返回null
     */
    Double getBalance(Integer userId);
}
