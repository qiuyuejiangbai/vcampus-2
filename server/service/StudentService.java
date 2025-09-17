package server.service;

import common.vo.StudentVO;
import server.dao.StudentDAO;
import server.dao.impl.StudentDAOImpl;
import server.util.ValidationUtil;

/**
 * 学生服务类
 * 处理学生相关的业务逻辑
 */
public class StudentService {
    private final StudentDAO studentDAO;
    
    public StudentService() {
        this.studentDAO = new StudentDAOImpl();
    }
    
    /**
     * 根据用户ID获取学生详细信息
     * @param userId 用户ID
     * @return 学生详细信息，不存在返回null
     */
    public StudentVO getStudentByUserId(Integer userId) {
        System.out.println("[DEBUG][StudentService] ========== 开始查询学生信息 ==========");
        System.out.println("[DEBUG][StudentService] 输入参数 - userId: " + userId);
        
        if (userId == null) {
            System.err.println("[DEBUG][StudentService] 用户ID为null，返回null");
            return null;
        }
        
        System.out.println("[DEBUG][StudentService] 用户ID验证通过，调用DAO层查询");
        System.out.println("[DEBUG][StudentService] 调用studentDAO.findByUserId(" + userId + ")");
        
        try {
            StudentVO student = studentDAO.findByUserId(userId);
            System.out.println("[DEBUG][StudentService] DAO查询完成，结果：" + (student != null ? "找到学生" : "未找到学生"));
            
            if (student != null) {
                System.out.println("[DEBUG][StudentService] 查询到的学生信息：");
                System.out.println("[DEBUG][StudentService] - 学生ID: " + student.getStudentId());
                System.out.println("[DEBUG][StudentService] - 用户ID: " + student.getUserId());
                System.out.println("[DEBUG][StudentService] - 学号: " + student.getStudentNo());
                System.out.println("[DEBUG][StudentService] - 姓名: " + student.getName());
                System.out.println("[DEBUG][StudentService] - 专业: " + student.getMajor());
                System.out.println("[DEBUG][StudentService] - 班级: " + student.getClassName());
                System.out.println("[DEBUG][StudentService] - 院系: " + student.getDepartment());
                System.out.println("[DEBUG][StudentService] - 联系电话: " + student.getPhone());
                System.out.println("[DEBUG][StudentService] - 邮箱: " + student.getEmail());
                System.out.println("[DEBUG][StudentService] - 地址: " + student.getAddress());
                System.out.println("[DEBUG][StudentService] - 入学年份: " + student.getEnrollmentYear());
                System.out.println("[DEBUG][StudentService] - 年级: " + student.getGrade());
                System.out.println("[DEBUG][StudentService] - 账户余额: " + student.getBalance());
                System.out.println("[DEBUG][StudentService] - 出生日期: " + student.getBirthDate());
                System.out.println("[DEBUG][StudentService] - 性别: " + student.getGender());
            } else {
                System.err.println("[DEBUG][StudentService] 未找到用户ID为 " + userId + " 的学生记录");
            }
            
            System.out.println("[DEBUG][StudentService] ========== 学生信息查询完成 ==========");
            return student;
        } catch (Exception e) {
            System.err.println("[DEBUG][StudentService] 查询学生信息时发生异常：" + e.getMessage());
            e.printStackTrace();
            System.out.println("[DEBUG][StudentService] ========== 学生信息查询异常结束 ==========");
            return null;
        }
    }
    
    /**
     * 根据学号获取学生详细信息
     * @param studentNo 学号
     * @return 学生详细信息，不存在返回null
     */
    public StudentVO getStudentByStudentNo(String studentNo) {
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return null;
        }
        return studentDAO.findByStudentNo(studentNo);
    }
    
    /**
     * 根据学生ID获取学生详细信息（包含用户信息）
     * @param studentId 学生ID
     * @return 学生详细信息，不存在返回null
     */
    public StudentVO getStudentById(Integer studentId) {
        if (studentId == null) {
            return null;
        }
        return studentDAO.findByIdWithUserInfo(studentId);
    }
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateStudent(StudentVO student) {
        System.out.println("[DEBUG][StudentService] 开始更新学生信息");
        
        if (student == null) {
            System.err.println("[DEBUG][StudentService] 学生信息为空");
            return false;
        }
        
        if (student.getId() == null) {
            System.err.println("[DEBUG][StudentService] 学生ID为空");
            return false;
        }
        
        // 数据验证
        String validationError = ValidationUtil.validateStudent(student);
        if (validationError != null) {
            System.err.println("[DEBUG][StudentService] 学生信息验证失败: " + validationError);
            return false;
        }
        
        System.out.println("[DEBUG][StudentService] 学生信息验证通过：");
        System.out.println("  - 学生ID=" + student.getId());
        System.out.println("  - 用户ID=" + student.getUserId());
        System.out.println("  - 学号=" + student.getStudentNo());
        System.out.println("  - 姓名=" + student.getName());
        System.out.println("  - 电话=" + student.getPhone());
        System.out.println("  - 邮箱=" + student.getEmail());
        System.out.println("  - 院系=" + student.getDepartment());
        System.out.println("  - 班级=" + student.getClassName());
        System.out.println("  - 专业=" + student.getMajor());
        System.out.println("  - 年级=" + student.getGrade());
        System.out.println("  - 入学年份=" + student.getEnrollmentYear());
        
        try {
            System.out.println("[DEBUG][StudentService] 调用studentDAO.update方法");
            boolean result = studentDAO.update(student);
            System.out.println("[DEBUG][StudentService] DAO更新结果：" + result);
            return result;
        } catch (Exception e) {
            System.err.println("[DEBUG][StudentService] 更新学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取所有学生信息
     * @return 学生列表
     */
    public java.util.List<StudentVO> getAllStudents() {
        try {
            return studentDAO.findAllWithUserInfo();
        } catch (Exception e) {
            System.err.println("获取所有学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 添加学生（使用事务确保原子性）
     * @param student 学生信息
     * @return 添加成功返回true，失败返回false
     */
    public boolean addStudent(StudentVO student) {
        System.out.println("[DEBUG][StudentService] ========== 开始添加学生（事务模式） ==========");
        
        if (student == null) {
            System.err.println("[DEBUG][StudentService] 学生信息为空，添加失败");
            return false;
        }
        
        // 数据验证
        String validationError = ValidationUtil.validateStudent(student);
        if (validationError != null) {
            System.err.println("[DEBUG][StudentService] 学生信息验证失败: " + validationError);
            return false;
        }
        
        System.out.println("[DEBUG][StudentService] 接收到的学生信息：");
        System.out.println("[DEBUG][StudentService] - 学号: " + student.getStudentNo());
        System.out.println("[DEBUG][StudentService] - 姓名: " + student.getName());
        System.out.println("[DEBUG][StudentService] - 性别: " + student.getGender());
        System.out.println("[DEBUG][StudentService] - 出生日期: " + student.getBirthDate());
        System.out.println("[DEBUG][StudentService] - 联系电话: " + student.getPhone());
        System.out.println("[DEBUG][StudentService] - 邮箱: " + student.getEmail());
        System.out.println("[DEBUG][StudentService] - 地址: " + student.getAddress());
        System.out.println("[DEBUG][StudentService] - 院系: " + student.getDepartment());
        System.out.println("[DEBUG][StudentService] - 班级: " + student.getClassName());
        System.out.println("[DEBUG][StudentService] - 专业: " + student.getMajor());
        System.out.println("[DEBUG][StudentService] - 年级: " + student.getGrade());
        System.out.println("[DEBUG][StudentService] - 入学年份: " + student.getEnrollmentYear());
        System.out.println("[DEBUG][StudentService] - 账户余额: " + student.getBalance());
        System.out.println("[DEBUG][StudentService] - 用户ID: " + student.getUserId());
        
        // 使用事务确保原子性
        return addStudentWithTransaction(student);
    }
    
    /**
     * 使用事务添加学生（确保原子性）
     * @param student 学生信息
     * @return 添加成功返回true，失败返回false
     */
    private boolean addStudentWithTransaction(StudentVO student) {
        java.sql.Connection conn = null;
        try {
            // 获取数据库连接
            conn = server.util.DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][StudentService] 无法获取数据库连接");
                return false;
            }
            
            // 开始事务
            conn.setAutoCommit(false);
            System.out.println("[DEBUG][StudentService] 开始事务");
            
            // 1. 检查学号是否已存在
            System.out.println("[DEBUG][StudentService] 检查学号是否已存在: " + student.getStudentNo());
            if (studentDAO.existsByStudentNo(student.getStudentNo())) {
                System.err.println("[DEBUG][StudentService] 学号已存在: " + student.getStudentNo());
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 学号检查通过");
            
            // 2. 检查登录ID是否已存在
            System.out.println("[DEBUG][StudentService] 检查登录ID是否已存在: " + student.getStudentNo());
            server.service.UserService userService = new server.service.UserService();
            if (userService.loginIdExists(student.getStudentNo())) {
                System.err.println("[DEBUG][StudentService] 登录ID已存在: " + student.getStudentNo());
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 登录ID检查通过");
            
            // 3. 创建用户账户
            System.out.println("[DEBUG][StudentService] 开始创建用户账户");
            common.vo.UserVO user = new common.vo.UserVO();
            user.setLoginId(student.getStudentNo()); // 使用学号作为登录ID
            user.setPassword(server.util.MD5Util.encrypt("123456")); // 默认密码（MD5加密）
            user.setRole(0); // 学生角色
            user.setStatus(1); // 激活状态
            
            System.out.println("[DEBUG][StudentService] 用户账户信息：");
            System.out.println("[DEBUG][StudentService] - 登录ID: " + user.getLoginId());
            System.out.println("[DEBUG][StudentService] - 密码: " + user.getPassword() + " (MD5加密)");
            System.out.println("[DEBUG][StudentService] - 角色: " + user.getRole());
            System.out.println("[DEBUG][StudentService] - 状态: " + user.getStatus());
            
            // 直接使用DAO插入用户（避免服务层重复检查）
            server.dao.UserDAO userDAO = new server.dao.impl.UserDAOImpl();
            Integer userId = userDAO.insert(user);
            if (userId == null) {
                System.err.println("[DEBUG][StudentService] 创建用户账户失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 用户账户创建成功，用户ID: " + userId);
            
            // 4. 设置学生信息的用户ID和默认余额
            student.setUserId(userId);
            if (student.getBalance() == null) {
                student.setBalance(java.math.BigDecimal.ZERO);
                System.out.println("[DEBUG][StudentService] 设置学生默认余额为0");
            }
            System.out.println("[DEBUG][StudentService] 设置学生用户ID: " + userId);
            
            // 5. 插入学生信息
            System.out.println("[DEBUG][StudentService] 开始插入学生信息到数据库");
            Integer studentId = studentDAO.insert(student);
            if (studentId == null) {
                System.err.println("[DEBUG][StudentService] 学生信息插入失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 学生信息插入成功，学生ID: " + studentId);
            
            // 6. 提交事务
            conn.commit();
            System.out.println("[DEBUG][StudentService] 事务提交成功");
            System.out.println("[DEBUG][StudentService] ========== 学生添加完成 ==========");
            return true;
            
        } catch (Exception e) {
            System.err.println("[DEBUG][StudentService] 添加学生失败: " + e.getMessage());
            e.printStackTrace();
            
            // 回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("[DEBUG][StudentService] 事务已回滚");
                } catch (java.sql.SQLException rollbackEx) {
                    System.err.println("[DEBUG][StudentService] 事务回滚失败: " + rollbackEx.getMessage());
                }
            }
            
            System.out.println("[DEBUG][StudentService] ========== 学生添加异常结束 ==========");
            return false;
        } finally {
            // 恢复自动提交并关闭连接
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException closeEx) {
                    System.err.println("[DEBUG][StudentService] 关闭连接失败: " + closeEx.getMessage());
                }
            }
        }
    }
    
    /**
     * 删除学生
     * @param studentId 学生ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteStudent(Integer studentId) {
        System.out.println("[DEBUG][StudentService] ========== 开始删除学生（事务模式） ==========");
        
        if (studentId == null) {
            System.err.println("[DEBUG][StudentService] 学生ID为空，删除失败");
            return false;
        }
        
        java.sql.Connection conn = null;
        try {
            // 获取数据库连接
            conn = server.util.DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][StudentService] 无法获取数据库连接");
                return false;
            }
            
            // 开始事务
            conn.setAutoCommit(false);
            System.out.println("[DEBUG][StudentService] 开始事务");
            
            // 1. 先获取学生信息
            System.out.println("[DEBUG][StudentService] 查询学生信息，studentId=" + studentId);
            StudentVO student = studentDAO.findById(studentId);
            if (student == null) {
                System.err.println("[DEBUG][StudentService] 学生不存在: " + studentId);
                conn.rollback();
                return false;
            }
            
            System.out.println("[DEBUG][StudentService] 找到学生信息：");
            System.out.println("[DEBUG][StudentService] - 学生ID=" + student.getStudentId());
            System.out.println("[DEBUG][StudentService] - 用户ID=" + student.getUserId());
            System.out.println("[DEBUG][StudentService] - 姓名=" + student.getName());
            System.out.println("[DEBUG][StudentService] - 学号=" + student.getStudentNo());
            
            // 2. 删除学生信息
            System.out.println("[DEBUG][StudentService] 开始删除学生信息");
            boolean studentDeleted = studentDAO.deleteById(studentId);
            if (!studentDeleted) {
                System.err.println("[DEBUG][StudentService] 删除学生信息失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 学生信息删除成功");
            
            // 3. 删除用户信息
            System.out.println("[DEBUG][StudentService] 开始删除用户信息，userId=" + student.getUserId());
            server.dao.UserDAO userDAO = new server.dao.impl.UserDAOImpl();
            boolean userDeleted = userDAO.deleteById(student.getUserId());
            if (!userDeleted) {
                System.err.println("[DEBUG][StudentService] 删除用户信息失败");
                conn.rollback();
                return false;
            }
            System.out.println("[DEBUG][StudentService] 用户信息删除成功");
            
            // 4. 提交事务
            conn.commit();
            System.out.println("[DEBUG][StudentService] 事务提交成功");
            System.out.println("[DEBUG][StudentService] 学生删除成功: " + student.getName() + " (" + student.getStudentNo() + ")");
            System.out.println("[DEBUG][StudentService] ========== 学生删除完成 ==========");
            return true;
            
        } catch (Exception e) {
            System.err.println("[DEBUG][StudentService] 删除学生失败: " + e.getMessage());
            e.printStackTrace();
            
            // 回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("[DEBUG][StudentService] 事务已回滚");
                } catch (java.sql.SQLException rollbackEx) {
                    System.err.println("[DEBUG][StudentService] 事务回滚失败: " + rollbackEx.getMessage());
                }
            }
            
            System.out.println("[DEBUG][StudentService] ========== 学生删除异常结束 ==========");
            return false;
        } finally {
            // 恢复自动提交并关闭连接
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException closeEx) {
                    System.err.println("[DEBUG][StudentService] 关闭连接失败: " + closeEx.getMessage());
                }
            }
        }
    }
}
