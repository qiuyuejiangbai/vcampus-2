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
        if (userId == null) {
            return null;
        }
        return studentDAO.findByUserId(userId);
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
