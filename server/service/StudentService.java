package server.service;

import common.vo.StudentVO;
import server.dao.StudentDAO;
import server.dao.impl.StudentDAOImpl;

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
        if (student == null || student.getStudentId() == null) {
            return false;
        }
        return studentDAO.update(student);
    }
}
