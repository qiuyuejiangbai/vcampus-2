package server.dao.impl;

import common.vo.StudentVO;
import common.vo.UserVO;
import server.dao.StudentDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问实现类
 */
public class StudentDAOImpl implements StudentDAO {
    
    @Override
    public Integer insert(StudentVO student) {
        System.out.println("[DEBUG][StudentDAOImpl] ========== 开始插入学生到数据库 ==========");
        System.out.println("[DEBUG][StudentDAOImpl] SQL: INSERT INTO students (user_id, name, student_no, gender, birth_date, phone, email, address, department, class_name, major, grade, enrollment_year, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        String sql = "INSERT INTO students (user_id, name, student_no, gender, birth_date, phone, email, address, department, class_name, major, grade, enrollment_year, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[DEBUG][StudentDAOImpl] 获取数据库连接");
            conn = DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][StudentDAOImpl] 数据库连接获取失败");
                return null;
            }
            System.out.println("[DEBUG][StudentDAOImpl] 数据库连接获取成功");
            
            System.out.println("[DEBUG][StudentDAOImpl] 准备SQL语句");
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            System.out.println("[DEBUG][StudentDAOImpl] 设置SQL参数：");
            pstmt.setInt(1, student.getUserId());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数1 (user_id): " + student.getUserId());
            pstmt.setString(2, student.getName());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数2 (name): " + student.getName());
            pstmt.setString(3, student.getStudentNo());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数3 (student_no): " + student.getStudentNo());
            pstmt.setString(4, student.getGender());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数4 (gender): " + student.getGender());
            pstmt.setDate(5, student.getBirthDate());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数5 (birth_date): " + student.getBirthDate());
            pstmt.setString(6, student.getPhone());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数6 (phone): " + student.getPhone());
            pstmt.setString(7, student.getEmail());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数7 (email): " + student.getEmail());
            pstmt.setString(8, student.getAddress());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数8 (address): " + student.getAddress());
            pstmt.setString(9, student.getDepartment());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数9 (department): " + student.getDepartment());
            pstmt.setString(10, student.getClassName());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数10 (class_name): " + student.getClassName());
            pstmt.setString(11, student.getMajor());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数11 (major): " + student.getMajor());
            pstmt.setString(12, student.getGrade());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数12 (grade): " + student.getGrade());
            pstmt.setObject(13, student.getEnrollmentYear());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数13 (enrollment_year): " + student.getEnrollmentYear());
            pstmt.setBigDecimal(14, student.getBalance());
            System.out.println("[DEBUG][StudentDAOImpl] - 参数14 (balance): " + student.getBalance());
            
            System.out.println("[DEBUG][StudentDAOImpl] 执行SQL插入操作");
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[DEBUG][StudentDAOImpl] SQL执行完成，影响行数: " + affectedRows);
            
            if (affectedRows > 0) {
                System.out.println("[DEBUG][StudentDAOImpl] 获取生成的主键");
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer studentId = rs.getInt(1);
                    System.out.println("[DEBUG][StudentDAOImpl] 学生插入成功，生成的学生ID: " + studentId);
                    System.out.println("[DEBUG][StudentDAOImpl] ========== 学生插入完成 ==========");
                    return studentId;
                } else {
                    System.err.println("[DEBUG][StudentDAOImpl] 无法获取生成的主键");
                }
            } else {
                System.err.println("[DEBUG][StudentDAOImpl] 没有行被插入");
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG][StudentDAOImpl] 插入学生失败: " + e.getMessage());
            System.err.println("[DEBUG][StudentDAOImpl] SQL错误代码: " + e.getErrorCode());
            System.err.println("[DEBUG][StudentDAOImpl] SQL状态: " + e.getSQLState());
            e.printStackTrace();
        } finally {
            System.out.println("[DEBUG][StudentDAOImpl] 关闭数据库资源");
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        System.out.println("[DEBUG][StudentDAOImpl] ========== 学生插入失败 ==========");
        return null;
    }
    
    @Override
    public boolean deleteById(Integer studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除学生失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean update(StudentVO student) {
        // 只更新允许修改的字段：姓名、出生日期、联系电话、邮箱、家庭住址
        String sql = "UPDATE students SET name = ?, birth_date = ?, phone = ?, email = ?, address = ? WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, student.getName());
            pstmt.setDate(2, student.getBirthDate());
            pstmt.setString(3, student.getPhone());
            pstmt.setString(4, student.getEmail());
            pstmt.setString(5, student.getAddress());
            pstmt.setInt(6, student.getStudentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新学生失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, null);
        }
    }
    
    @Override
    public StudentVO findById(Integer studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStudentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<StudentVO> findAll() {
        String sql = "SELECT * FROM students ORDER BY student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM students";
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
            System.err.println("统计学生数量失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
    
    @Override
    public boolean existsById(Integer studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查学生是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public StudentVO findByUserId(Integer userId) {
        System.out.println("[DEBUG][StudentDAOImpl] ========== 开始根据用户ID查询学生 ==========");
        System.out.println("[DEBUG][StudentDAOImpl] 输入参数 - userId: " + userId);
        
        // 修改SQL查询，包含用户信息
        String sql = "SELECT s.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.user_id = ?";
        System.out.println("[DEBUG][StudentDAOImpl] SQL查询语句: " + sql);
        System.out.println("[DEBUG][StudentDAOImpl] 查询参数: user_id = " + userId);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[DEBUG][StudentDAOImpl] 获取数据库连接");
            conn = DatabaseUtil.getConnection();
            if (conn == null) {
                System.err.println("[DEBUG][StudentDAOImpl] 数据库连接为null");
                return null;
            }
            System.out.println("[DEBUG][StudentDAOImpl] 数据库连接获取成功");
            
            System.out.println("[DEBUG][StudentDAOImpl] 创建PreparedStatement");
            pstmt = conn.prepareStatement(sql);
            System.out.println("[DEBUG][StudentDAOImpl] 设置查询参数");
            pstmt.setInt(1, userId);
            System.out.println("[DEBUG][StudentDAOImpl] 参数设置完成，执行查询");
            
            rs = pstmt.executeQuery();
            System.out.println("[DEBUG][StudentDAOImpl] 查询执行完成");
            
            if (rs.next()) {
                System.out.println("[DEBUG][StudentDAOImpl] 找到学生记录，开始映射数据");
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                System.out.println("[DEBUG][StudentDAOImpl] 数据映射完成，用户信息已设置");
                System.out.println("[DEBUG][StudentDAOImpl] ========== 学生查询成功 ==========");
                return student;
            } else {
                System.err.println("[DEBUG][StudentDAOImpl] 未找到用户ID为 " + userId + " 的学生记录");
                System.out.println("[DEBUG][StudentDAOImpl] ========== 学生查询完成（无结果） ==========");
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG][StudentDAOImpl] 根据用户ID查询学生失败: " + e.getMessage());
            System.err.println("[DEBUG][StudentDAOImpl] SQL异常详情:");
            e.printStackTrace();
            System.out.println("[DEBUG][StudentDAOImpl] ========== 学生查询异常结束 ==========");
        } finally {
            System.out.println("[DEBUG][StudentDAOImpl] 关闭数据库资源");
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public StudentVO findByStudentNo(String studentNo) {
        String sql = "SELECT * FROM students WHERE student_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentNo);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStudentVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据学号查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean existsByStudentNo(String studentNo) {
        String sql = "SELECT 1 FROM students WHERE student_no = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentNo);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查学号是否存在失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<StudentVO> findByMajor(String major) {
        String sql = "SELECT * FROM students WHERE major = ? ORDER BY student_id";
        return findByStringField(sql, major);
    }
    
    @Override
    public List<StudentVO> findByClassName(String className) {
        String sql = "SELECT * FROM students WHERE class_name = ? ORDER BY student_id";
        return findByStringField(sql, className);
    }
    
    @Override
    public List<StudentVO> findByGrade(String grade) {
        String sql = "SELECT * FROM students WHERE grade = ? ORDER BY student_id";
        return findByStringField(sql, grade);
    }
    
    @Override
    public List<StudentVO> findByEnrollmentYear(Integer enrollmentYear) {
        String sql = "SELECT * FROM students WHERE enrollment_year = ? ORDER BY student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, enrollmentYear);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据入学年份查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public List<StudentVO> findAllWithUserInfo() {
        String sql = "SELECT s.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                // 创建用户信息对象，只包含users表中实际存在的字段
                UserVO user = new UserVO();
                user.setUserId(rs.getInt("user_id"));
                user.setLoginId(rs.getString("login_id"));
                user.setRole(rs.getInt("role"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setCreatedTime(rs.getTimestamp("created_time"));
                user.setUpdatedTime(rs.getTimestamp("updated_time"));
                student.setUserInfo(user);
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("查询所有学生（含用户信息）失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    @Override
    public StudentVO findByIdWithUserInfo(Integer studentId) {
        String sql = "SELECT s.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                return student;
            }
        } catch (SQLException e) {
            System.err.println("查询学生（含用户信息）失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }
    
    @Override
    public List<StudentVO> findByNameLike(String name) {
        String sql = "SELECT s.*, u.login_id, u.role, u.avatar_path, u.created_time, u.updated_time " +
                    "FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.name LIKE ? ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StudentVO student = mapResultSetToStudentVO(rs);
                UserVO user = mapResultSetToUserVO(rs);
                student.setUserInfo(user);
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("根据姓名模糊查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    /**
     * 根据字符串字段查询学生列表的通用方法
     */
    private List<StudentVO> findByStringField(String sql, String value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<StudentVO> students = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapResultSetToStudentVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询学生失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeAll(conn, pstmt, rs);
        }
        return students;
    }
    
    /**
     * 将ResultSet映射为StudentVO对象
     */
    private StudentVO mapResultSetToStudentVO(ResultSet rs) throws SQLException {
        System.out.println("[DEBUG][StudentDAOImpl] ========== 开始映射ResultSet到StudentVO ==========");
        
        StudentVO student = new StudentVO();
        
        try {
            System.out.println("[DEBUG][StudentDAOImpl] 开始读取数据库字段");
            
            // 读取基本字段
            int studentId = rs.getInt("student_id");
            student.setStudentId(studentId);
            System.out.println("[DEBUG][StudentDAOImpl] - student_id: " + studentId);
            
            int userId = rs.getInt("user_id");
            student.setUserId(userId);
            System.out.println("[DEBUG][StudentDAOImpl] - user_id: " + userId);
            
            String studentNo = rs.getString("student_no");
            student.setStudentNo(studentNo);
            System.out.println("[DEBUG][StudentDAOImpl] - student_no: " + studentNo);
            
            String name = rs.getString("name");
            student.setName(name);
            System.out.println("[DEBUG][StudentDAOImpl] - name: " + name);
            
            String phone = rs.getString("phone");
            student.setPhone(phone);
            System.out.println("[DEBUG][StudentDAOImpl] - phone: " + phone);
            
            String email = rs.getString("email");
            student.setEmail(email);
            System.out.println("[DEBUG][StudentDAOImpl] - email: " + email);
            
            String address = rs.getString("address");
            student.setAddress(address);
            System.out.println("[DEBUG][StudentDAOImpl] - address: " + address);
            
            String department = rs.getString("department");
            student.setDepartment(department);
            System.out.println("[DEBUG][StudentDAOImpl] - department: " + department);
            
            String major = rs.getString("major");
            student.setMajor(major);
            System.out.println("[DEBUG][StudentDAOImpl] - major: " + major);
            
            String className = rs.getString("class_name");
            student.setClassName(className);
            System.out.println("[DEBUG][StudentDAOImpl] - class_name: " + className);
            
            String gender = rs.getString("gender");
            student.setGender(gender);
            System.out.println("[DEBUG][StudentDAOImpl] - gender: " + gender);
            
            java.sql.Date birthDate = rs.getDate("birth_date");
            student.setBirthDate(birthDate);
            System.out.println("[DEBUG][StudentDAOImpl] - birth_date: " + birthDate);
            
            java.math.BigDecimal balance = rs.getBigDecimal("balance");
            student.setBalance(balance);
            System.out.println("[DEBUG][StudentDAOImpl] - balance: " + balance);
            
            Integer enrollmentYear = (Integer) rs.getObject("enrollment_year");
            student.setEnrollmentYear(enrollmentYear);
            System.out.println("[DEBUG][StudentDAOImpl] - enrollment_year: " + enrollmentYear);
            
            java.sql.Timestamp createdTime = rs.getTimestamp("created_time");
            student.setCreatedTime(createdTime);
            System.out.println("[DEBUG][StudentDAOImpl] - created_time: " + createdTime);
            
            java.sql.Timestamp updatedTime = rs.getTimestamp("updated_time");
            student.setUpdatedTime(updatedTime);
            System.out.println("[DEBUG][StudentDAOImpl] - updated_time: " + updatedTime);
            
            System.out.println("[DEBUG][StudentDAOImpl] 基本字段读取完成，开始计算年级信息");
            
            // 从入学年份推导年级信息
            if (enrollmentYear != null) {
                int currentYear = java.time.Year.now().getValue();
                int gradeLevel = currentYear - enrollmentYear + 1;
                System.out.println("[DEBUG][StudentDAOImpl] 年级计算：当前年份=" + currentYear + 
                    ", 入学年份=" + enrollmentYear + ", 年级=" + gradeLevel);
                
                if (gradeLevel >= 1 && gradeLevel <= 4) {
                    String grade = gradeLevel + "年级";
                    student.setGrade(grade);
                    System.out.println("[DEBUG][StudentDAOImpl] 设置年级: " + grade);
                } else {
                    student.setGrade("已毕业");
                    System.out.println("[DEBUG][StudentDAOImpl] 设置年级: 已毕业");
                }
            } else {
                student.setGrade("未知");
                System.out.println("[DEBUG][StudentDAOImpl] 入学年份为空，设置年级: 未知");
            }
            
            System.out.println("[DEBUG][StudentDAOImpl] ========== StudentVO映射完成 ==========");
            System.out.println("[DEBUG][StudentDAOImpl] 最终学生对象信息：");
            System.out.println("[DEBUG][StudentDAOImpl] - 学生ID: " + student.getStudentId());
            System.out.println("[DEBUG][StudentDAOImpl] - 用户ID: " + student.getUserId());
            System.out.println("[DEBUG][StudentDAOImpl] - 学号: " + student.getStudentNo());
            System.out.println("[DEBUG][StudentDAOImpl] - 姓名: " + student.getName());
            System.out.println("[DEBUG][StudentDAOImpl] - 专业: " + student.getMajor());
            System.out.println("[DEBUG][StudentDAOImpl] - 班级: " + student.getClassName());
            System.out.println("[DEBUG][StudentDAOImpl] - 年级: " + student.getGrade());
            
        } catch (SQLException e) {
            System.err.println("[DEBUG][StudentDAOImpl] 映射StudentVO时发生SQL异常：" + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("[DEBUG][StudentDAOImpl] 映射StudentVO时发生未知异常：" + e.getMessage());
            e.printStackTrace();
            throw new SQLException("映射StudentVO失败", e);
        }
        
        return student;
    }
    
    /**
     * 将ResultSet映射为UserVO对象（用于关联查询）
     */
    private UserVO mapResultSetToUserVO(ResultSet rs) throws SQLException {
        UserVO user = new UserVO();
        user.setUserId(rs.getInt("user_id"));
        user.setLoginId(rs.getString("login_id"));
        user.setRole(rs.getInt("role"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setCreatedTime(rs.getTimestamp("created_time"));
        user.setUpdatedTime(rs.getTimestamp("updated_time"));
        return user;
    }
}
