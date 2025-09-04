package server.service;

import common.vo.UserVO;
import common.vo.StudentVO;
import common.vo.TeacherVO;
import server.dao.UserDAO;
import server.dao.StudentDAO;
import server.dao.TeacherDAO;
import server.dao.impl.UserDAOImpl;
import server.dao.impl.StudentDAOImpl;
import server.dao.impl.TeacherDAOImpl;
import server.util.MD5Util;

import java.util.List;

/**
 * 用户业务服务类
 * 处理用户管理相关的业务逻辑
 */
public class UserService {
    private final UserDAO userDAO;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    
    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.studentDAO = new StudentDAOImpl();
        this.teacherDAO = new TeacherDAOImpl();
    }
    
    /**
     * 用户登录验证
     * @param loginId 登录ID
     * @param password 密码（明文）
     * @return 验证成功返回用户信息，失败返回null
     */
    public UserVO login(String loginId, String password) {
        System.out.println("=== 登录调试信息 ===");
        System.out.println("登录ID: " + loginId);
        System.out.println("原始密码: " + password);
        
        if (loginId == null || password == null) {
            System.out.println("登录失败：参数为空");
            return null;
        }
        
        try {
            // 将密码进行MD5加密
            String passwordHash = MD5Util.encrypt(password);
            System.out.println("MD5加密后: " + passwordHash);
            
            UserVO result = userDAO.authenticate(loginId, passwordHash);
            if (result == null) {
                System.out.println("数据库认证失败：用户不存在或密码错误");
            } else {
                System.out.println("数据库认证成功：用户ID: " + result.getUserId() + ", 登录ID: " + result.getId() + ", 角色: " + result.getRoleName());

                // 增强：仅凭账号密码，若为学生则查询并同步姓名（及日志输出专业）
                if (result.isStudent()) {
                    try {
                        StudentVO student = studentDAO.findByUserId(result.getUserId());
                        if (student != null) {
                            if (result.getName() == null || result.getName().trim().isEmpty()) {
                                result.setName(student.getName());
                            }
                            System.out.println("登录后学生档案：姓名=" + student.getName() + ", 专业=" + student.getMajor());
                        } else {
                            System.out.println("登录后学生档案未找到（userId=" + result.getUserId() + ")");
                        }
                    } catch (Exception e) {
                        System.err.println("登录后查询学生档案异常: " + e.getMessage());
                    }
                }
            }
            System.out.println("=== 登录调试结束 ===");
            return result;
        } catch (Exception e) {
            System.err.println("用户登录验证失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 用户注册 - 支持同时注册学生和教师信息
     * @param user 用户基础信息
     * @param studentInfo 学生信息（如果是学生角色）
     * @param teacherInfo 教师信息（如果是教师角色）
     * @return 注册成功返回用户ID，失败返回null
     */
    public Integer register(UserVO user, StudentVO studentInfo, TeacherVO teacherInfo) {
        if (user == null || user.getId() == null || user.getPassword() == null) {
            return null;
        }
        
        try {
            // 检查登录ID是否已存在
            if (userDAO.existsByLoginId(user.getId())) {
                System.err.println("登录ID已存在: " + user.getId());
                return null;
            }
            
            // 加密密码
            String passwordHash = MD5Util.encrypt(user.getPassword());
            user.setPassword(passwordHash);
            
            // 设置默认值
            if (user.getRole() == null) {
                user.setRole(0); // 默认为学生
            }
            
            // 插入用户基础信息
            Integer userId = userDAO.insert(user);
            if (userId == null) {
                return null;
            }
            
            // 根据角色插入对应的详细信息
            if (user.isStudent() && studentInfo != null) {
                studentInfo.setUserId(userId);
                // 如果没有提供学号，使用用户ID作为学号
                if (studentInfo.getStudentNo() == null || studentInfo.getStudentNo().isEmpty()) {
                    studentInfo.setStudentNo(user.getId());
                }
                studentDAO.insert(studentInfo);
            } else if (user.isTeacher() && teacherInfo != null) {
                teacherInfo.setUserId(userId);
                // 如果没有提供工号，使用用户ID作为工号
                if (teacherInfo.getTeacherNo() == null || teacherInfo.getTeacherNo().isEmpty()) {
                    teacherInfo.setTeacherNo(user.getId());
                }
                teacherDAO.insert(teacherInfo);
            }
            
            return userId;
        } catch (Exception e) {
            System.err.println("用户注册失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 简化版用户注册 - 只注册基础用户信息
     * @param user 用户信息
     * @return 注册成功返回用户ID，失败返回null
     */
    public Integer register(UserVO user) {
        return register(user, null, null);
    }
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息，不存在返回null
     */
    public UserVO getUserById(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userDAO.findById(userId);
    }
    
    /**
     * 根据登录ID获取用户信息
     * @param loginId 登录ID
     * @return 用户信息，不存在返回null
     */
    public UserVO getUserByLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        return userDAO.findByLoginId(loginId);
    }
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<UserVO> getAllUsers() {
        return userDAO.findAll();
    }
    
    /**
     * 根据角色获取用户列表
     * @param role 角色：0-学生，1-教师，2-管理员
     * @return 用户列表
     */
    public List<UserVO> getUsersByRole(Integer role) {
        if (role == null) {
            return null;
        }
        return userDAO.findByRole(role);
    }
    
    /**
     * 获取未激活的用户列表（状态管理已简化）
     * @return 空列表（状态管理已移除）
     */
    public List<UserVO> getInactiveUsers() {
        // 状态管理已移除，返回空列表
        return new java.util.ArrayList<>();
    }
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateUser(UserVO user) {
        if (user == null || user.getUserId() == null) {
            return false;
        }
        
        try {
            return userDAO.update(user);
        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 修改成功返回true，失败返回false
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) {
            return false;
        }
        
        try {
            // 验证旧密码
            UserVO user = userDAO.findById(userId);
            if (user == null) {
                return false;
            }
            
            String oldPasswordHash = MD5Util.encrypt(oldPassword);
            if (!oldPasswordHash.equals(user.getPassword())) {
                System.err.println("旧密码不正确");
                return false;
            }
            
            // 更新新密码
            String newPasswordHash = MD5Util.encrypt(newPassword);
            return userDAO.updatePassword(userId, newPasswordHash);
        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 激活用户账户
     * @param userId 用户ID
     * @return 激活成功返回true，失败返回false
     */
    public boolean activateUser(Integer userId) {
        if (userId == null) {
            return false;
        }
        return userDAO.activateUser(userId);
    }
    
    /**
     * 停用用户账户
     * @param userId 用户ID
     * @return 停用成功返回true，失败返回false
     */
    public boolean deactivateUser(Integer userId) {
        if (userId == null) {
            return false;
        }
        return userDAO.deactivateUser(userId);
    }
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUser(Integer userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            return userDAO.deleteById(userId);
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查用户是否存在
     * @param userId 用户ID
     * @return 存在返回true，不存在返回false
     */
    public boolean userExists(Integer userId) {
        if (userId == null) {
            return false;
        }
        return userDAO.existsById(userId);
    }
    
    /**
     * 检查登录ID是否存在
     * @param loginId 登录ID
     * @return 存在返回true，不存在返回false
     */
    public boolean loginIdExists(String loginId) {
        if (loginId == null) {
            return false;
        }
        return userDAO.existsByLoginId(loginId);
    }
    
    /**
     * 根据姓名模糊查询用户
     * @param name 姓名关键词
     * @return 用户列表
     */
    public List<UserVO> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers();
        }
        return userDAO.findByNameLike(name.trim());
    }
    
    /**
     * 获取用户余额
     * @param userId 用户ID
     * @return 用户余额，用户不存在返回null
     */
    public Double getUserBalance(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userDAO.getBalance(userId);
    }
    
    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 金额变动（正数为增加，负数为减少）
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateUserBalance(Integer userId, Double amount) {
        if (userId == null || amount == null) {
            return false;
        }
        
        try {
            // 检查余额是否足够（如果是扣款）
            if (amount < 0) {
                Double currentBalance = userDAO.getBalance(userId);
                if (currentBalance == null || currentBalance + amount < 0) {
                    System.err.println("余额不足，当前余额: " + currentBalance + ", 扣款金额: " + Math.abs(amount));
                    return false;
                }
            }
            
            return userDAO.updateBalance(userId, amount);
        } catch (Exception e) {
            System.err.println("更新用户余额失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证用户权限
     * @param userId 用户ID
     * @param requiredRole 需要的角色等级（0-学生，1-教师，2-管理员）
     * @return 有权限返回true，无权限返回false
     */
    public boolean hasPermission(Integer userId, Integer requiredRole) {
        if (userId == null || requiredRole == null) {
            return false;
        }
        
        UserVO user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            return true;
        }
        
        // 检查角色等级
        return user.getRole() >= requiredRole;
    }
}
