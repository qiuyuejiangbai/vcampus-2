package server.service;

import common.vo.TeacherVO;
import server.dao.TeacherDAO;
import server.dao.impl.TeacherDAOImpl;
import server.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师业务服务类
 * 处理教师管理相关的业务逻辑
 */
public class TeacherService {
    private final TeacherDAO teacherDAO;
    
    public TeacherService() {
        this.teacherDAO = new TeacherDAOImpl();
    }
    
    /**
     * 根据用户ID获取教师信息
     * @param userId 用户ID
     * @return 教师信息，不存在返回null
     */
    public TeacherVO getTeacherByUserId(Integer userId) {
        System.out.println("[DEBUG][TeacherService] 开始根据用户ID查询教师信息，userId=" + userId);
        
        if (userId == null) {
            System.err.println("[DEBUG][TeacherService] 获取教师信息失败：用户ID为空");
            return null;
        }
        
        try {
            System.out.println("[DEBUG][TeacherService] 调用teacherDAO.findByUserId(userId=" + userId + ")");
            
            if (teacherDAO == null) {
                System.err.println("[DEBUG][TeacherService] teacherDAO为null！");
                return null;
            }
            
            TeacherVO teacher = teacherDAO.findByUserId(userId);
            System.out.println("[DEBUG][TeacherService] DAO查询完成，结果：" + (teacher != null ? "找到教师记录" : "未找到教师记录"));
            
            if (teacher != null) {
                System.out.println("[DEBUG][TeacherService] 教师信息查询成功：");
                System.out.println("  - 教师ID=" + teacher.getId());
                System.out.println("  - 用户ID=" + teacher.getUserId());
                System.out.println("  - 姓名=" + teacher.getName());
                System.out.println("  - 工号=" + teacher.getTeacherNo());
                System.out.println("  - 学院=" + teacher.getDepartment());
                System.out.println("  - 职称=" + teacher.getTitle());
                System.out.println("  - 电话=" + teacher.getPhone());
                System.out.println("  - 邮箱=" + teacher.getEmail());
            } else {
                System.err.println("[DEBUG][TeacherService] 未找到教师信息，userId=" + userId);
                System.err.println("[DEBUG][TeacherService] 请检查：");
                System.err.println("  1. 用户ID是否正确");
                System.err.println("  2. teachers表中是否存在该用户的教师记录");
                System.err.println("  3. 数据库连接是否正常");
            }
            return teacher;
        } catch (Exception e) {
            System.err.println("[DEBUG][TeacherService] 根据用户ID查询教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据教师ID获取教师信息
     * @param teacherId 教师ID
     * @return 教师信息，不存在返回null
     */
    public TeacherVO getTeacherById(Integer teacherId) {
        if (teacherId == null) {
            return null;
        }
        
        try {
            return teacherDAO.findById(teacherId);
        } catch (Exception e) {
            System.err.println("根据教师ID查询教师信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 根据工号获取教师信息
     * @param teacherNo 工号
     * @return 教师信息，不存在返回null
     */
    public TeacherVO getTeacherByNo(String teacherNo) {
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            return null;
        }
        
        try {
            return teacherDAO.findByTeacherNo(teacherNo.trim());
        } catch (Exception e) {
            System.err.println("根据工号查询教师信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建教师信息
     * @param teacher 教师信息
     * @return 创建成功返回教师ID，失败返回null
     */
    public Integer createTeacher(TeacherVO teacher) {
        if (teacher == null) {
            return null;
        }
        
        // 数据验证
        String validationError = ValidationUtil.validateTeacher(teacher);
        if (validationError != null) {
            System.err.println("[DEBUG][TeacherService] 教师信息验证失败: " + validationError);
            return null;
        }
        
        try {
            return teacherDAO.insert(teacher);
        } catch (Exception e) {
            System.err.println("创建教师信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 更新教师信息
     * @param teacher 教师信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateTeacher(TeacherVO teacher) {
        System.out.println("[DEBUG][TeacherService] 开始更新教师信息");
        
        if (teacher == null) {
            System.err.println("[DEBUG][TeacherService] 教师信息为空");
            return false;
        }
        
        if (teacher.getId() == null) {
            System.err.println("[DEBUG][TeacherService] 教师ID为空");
            return false;
        }
        
        // 数据验证
        String validationError = ValidationUtil.validateTeacher(teacher);
        if (validationError != null) {
            System.err.println("[DEBUG][TeacherService] 教师信息验证失败: " + validationError);
            return false;
        }
        
        System.out.println("[DEBUG][TeacherService] 教师信息验证通过：");
        System.out.println("  - 教师ID=" + teacher.getId());
        System.out.println("  - 用户ID=" + teacher.getUserId());
        System.out.println("  - 姓名=" + teacher.getName());
        System.out.println("  - 电话=" + teacher.getPhone());
        System.out.println("  - 邮箱=" + teacher.getEmail());
        System.out.println("  - 工号=" + teacher.getTeacherNo());
        System.out.println("  - 职称=" + teacher.getTitle());
        System.out.println("  - 办公室=" + teacher.getOffice());
        System.out.println("  - 研究方向=" + teacher.getResearchArea());
        
        try {
            System.out.println("[DEBUG][TeacherService] 调用teacherDAO.update方法");
            boolean result = teacherDAO.update(teacher);
            System.out.println("[DEBUG][TeacherService] DAO更新结果：" + result);
            return result;
        } catch (Exception e) {
            System.err.println("[DEBUG][TeacherService] 更新教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除教师信息
     * @param teacherId 教师ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteTeacher(Integer teacherId) {
        System.out.println("[DEBUG][TeacherService] ========== 开始删除教师（事务模式） ==========");
        
        if (teacherId == null) {
            System.err.println("[DEBUG][TeacherService] 教师ID为空，删除失败");
            return false;
        }
        
        java.sql.Connection conn = null;
        try {
            // 获取数据库连接
            conn = server.util.DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][TeacherService] 无法获取数据库连接");
                return false;
            }
            
            // 开始事务
            conn.setAutoCommit(false);
            System.out.println("[DEBUG][TeacherService] 开始事务");
            
            // 1. 先获取教师信息
            System.out.println("[DEBUG][TeacherService] 查询教师信息，teacherId=" + teacherId);
            TeacherVO teacher = teacherDAO.findById(teacherId);
            if (teacher == null) {
                System.err.println("[DEBUG][TeacherService] 教师不存在: " + teacherId);
                conn.rollback();
                return false;
            }
            
            System.out.println("[DEBUG][TeacherService] 找到教师信息：");
            System.out.println("[DEBUG][TeacherService] - 教师ID=" + teacher.getId());
            System.out.println("[DEBUG][TeacherService] - 用户ID=" + teacher.getUserId());
            System.out.println("[DEBUG][TeacherService] - 姓名=" + teacher.getName());
            System.out.println("[DEBUG][TeacherService] - 工号=" + teacher.getTeacherNo());
            
            // 2. 删除教师信息
            System.out.println("[DEBUG][TeacherService] 开始删除教师信息");
            boolean teacherDeleted = teacherDAO.deleteById(teacherId);
            if (!teacherDeleted) {
                System.err.println("[DEBUG][TeacherService] 删除教师信息失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 教师信息删除成功");
            
            // 3. 删除用户信息
            System.out.println("[DEBUG][TeacherService] 开始删除用户信息，userId=" + teacher.getUserId());
            server.dao.UserDAO userDAO = new server.dao.impl.UserDAOImpl();
            boolean userDeleted = userDAO.deleteById(teacher.getUserId());
            if (!userDeleted) {
                System.err.println("[DEBUG][TeacherService] 删除用户信息失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 用户信息删除成功");
            
            // 4. 提交事务
            conn.commit();
            System.out.println("[DEBUG][TeacherService] 事务提交成功");
            System.out.println("[DEBUG][TeacherService] 教师删除成功: " + teacher.getName() + " (" + teacher.getTeacherNo() + ")");
            System.out.println("[DEBUG][TeacherService] ========== 教师删除完成 ==========");
            return true;
            
        } catch (Exception e) {
            System.err.println("[DEBUG][TeacherService] 删除教师失败: " + e.getMessage());
            e.printStackTrace();
            
            // 回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("[DEBUG][TeacherService] 事务已回滚");
                } catch (java.sql.SQLException rollbackEx) {
                    System.err.println("[DEBUG][TeacherService] 事务回滚失败: " + rollbackEx.getMessage());
                }
            }
            
            System.out.println("[DEBUG][TeacherService] ========== 教师删除异常结束 ==========");
            return false;
        } finally {
            // 恢复自动提交并关闭连接
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException closeEx) {
                    System.err.println("[DEBUG][TeacherService] 关闭连接失败: " + closeEx.getMessage());
                }
            }
        }
    }
    
    /**
     * 获取所有教师信息
     * @return 教师列表
     */
    public java.util.List<TeacherVO> getAllTeachers() {
        try {
            return teacherDAO.findAllWithUserInfo();
        } catch (Exception e) {
            System.err.println("获取所有教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 添加教师（使用事务确保原子性）
     * @param teacher 教师信息
     * @return 添加成功返回true，失败返回false
     */
    public boolean addTeacher(TeacherVO teacher) {
        System.out.println("[DEBUG][TeacherService] ========== 开始添加教师（事务模式） ==========");
        
        if (teacher == null) {
            System.err.println("[DEBUG][TeacherService] 教师信息为空，添加失败");
            return false;
        }
        
        System.out.println("[DEBUG][TeacherService] 接收到的教师信息：");
        System.out.println("[DEBUG][TeacherService] - 工号: " + teacher.getTeacherNo());
        System.out.println("[DEBUG][TeacherService] - 姓名: " + teacher.getName());
        System.out.println("[DEBUG][TeacherService] - 联系电话: " + teacher.getPhone());
        System.out.println("[DEBUG][TeacherService] - 邮箱: " + teacher.getEmail());
        System.out.println("[DEBUG][TeacherService] - 院系: " + teacher.getDepartment());
        System.out.println("[DEBUG][TeacherService] - 职称: " + teacher.getTitle());
        System.out.println("[DEBUG][TeacherService] - 办公室: " + teacher.getOffice());
        System.out.println("[DEBUG][TeacherService] - 研究方向: " + teacher.getResearchArea());
        System.out.println("[DEBUG][TeacherService] - 用户ID: " + teacher.getUserId());
        
        // 使用事务确保原子性
        return addTeacherWithTransaction(teacher);
    }
    
    /**
     * 使用事务添加教师（确保原子性）
     * @param teacher 教师信息
     * @return 添加成功返回true，失败返回false
     */
    private boolean addTeacherWithTransaction(TeacherVO teacher) {
        java.sql.Connection conn = null;
        try {
            // 获取数据库连接
            conn = server.util.DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][TeacherService] 无法获取数据库连接");
                return false;
            }
            
            // 开始事务
            conn.setAutoCommit(false);
            System.out.println("[DEBUG][TeacherService] 开始事务");
            
            // 1. 检查工号是否已存在
            System.out.println("[DEBUG][TeacherService] 检查工号是否已存在: " + teacher.getTeacherNo());
            if (teacherDAO.existsByTeacherNo(teacher.getTeacherNo())) {
                System.err.println("[DEBUG][TeacherService] 工号已存在: " + teacher.getTeacherNo());
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 工号检查通过");
            
            // 2. 检查登录ID是否已存在
            System.out.println("[DEBUG][TeacherService] 检查登录ID是否已存在: " + teacher.getTeacherNo());
            server.service.UserService userService = new server.service.UserService();
            if (userService.loginIdExists(teacher.getTeacherNo())) {
                System.err.println("[DEBUG][TeacherService] 登录ID已存在: " + teacher.getTeacherNo());
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 登录ID检查通过");
            
            // 3. 创建用户账户
            System.out.println("[DEBUG][TeacherService] 开始创建用户账户");
            common.vo.UserVO user = new common.vo.UserVO();
            user.setLoginId(teacher.getTeacherNo()); // 使用工号作为登录ID
            user.setPassword(server.util.MD5Util.encrypt("123456")); // 默认密码（MD5加密）
            user.setRole(1); // 教师角色
            user.setStatus(1); // 激活状态
            
            System.out.println("[DEBUG][TeacherService] 用户账户信息：");
            System.out.println("[DEBUG][TeacherService] - 登录ID: " + user.getLoginId());
            System.out.println("[DEBUG][TeacherService] - 密码: " + user.getPassword() + " (MD5加密)");
            System.out.println("[DEBUG][TeacherService] - 角色: " + user.getRole());
            System.out.println("[DEBUG][TeacherService] - 状态: " + user.getStatus());
            
            // 直接使用DAO插入用户（避免服务层重复检查）
            server.dao.UserDAO userDAO = new server.dao.impl.UserDAOImpl();
            Integer userId = userDAO.insert(user);
            if (userId == null) {
                System.err.println("[DEBUG][TeacherService] 创建用户账户失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 用户账户创建成功，用户ID: " + userId);
            
            // 4. 设置教师信息的用户ID和默认余额
            teacher.setUserId(userId);
            if (teacher.getBalance() == null) {
                teacher.setBalance(java.math.BigDecimal.ZERO);
                System.out.println("[DEBUG][TeacherService] 设置教师默认余额为0");
            }
            System.out.println("[DEBUG][TeacherService] 设置教师用户ID: " + userId);
            
            // 5. 插入教师信息
            System.out.println("[DEBUG][TeacherService] 开始插入教师信息到数据库");
            Integer teacherId = teacherDAO.insert(teacher);
            if (teacherId == null) {
                System.err.println("[DEBUG][TeacherService] 教师信息插入失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][TeacherService] 教师信息插入成功，教师ID: " + teacherId);
            
            // 6. 提交事务
            conn.commit();
            System.out.println("[DEBUG][TeacherService] 事务提交成功");
            System.out.println("[DEBUG][TeacherService] ========== 教师添加完成 ==========");
            return true;
            
        } catch (Exception e) {
            System.err.println("[DEBUG][TeacherService] 添加教师失败: " + e.getMessage());
            e.printStackTrace();
            
            // 回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("[DEBUG][TeacherService] 事务已回滚");
                } catch (java.sql.SQLException rollbackEx) {
                    System.err.println("[DEBUG][TeacherService] 事务回滚失败: " + rollbackEx.getMessage());
                }
            }
            
            System.out.println("[DEBUG][TeacherService] ========== 教师添加异常结束 ==========");
            return false;
        } finally {
            // 恢复自动提交并关闭连接
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException closeEx) {
                    System.err.println("[DEBUG][TeacherService] 关闭连接失败: " + closeEx.getMessage());
                }
            }
        }
    }
}
