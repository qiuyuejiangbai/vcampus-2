package server.service;

import common.vo.TeacherVO;
import server.dao.TeacherDAO;
import server.dao.impl.TeacherDAOImpl;

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
        if (teacher == null || teacher.getId() == null) {
            return false;
        }
        
        try {
            return teacherDAO.update(teacher);
        } catch (Exception e) {
            System.err.println("更新教师信息失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除教师信息
     * @param teacherId 教师ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteTeacher(Integer teacherId) {
        if (teacherId == null) {
            return false;
        }
        
        try {
            return teacherDAO.deleteById(teacherId);
        } catch (Exception e) {
            System.err.println("删除教师信息失败: " + e.getMessage());
            return false;
        }
    }
}
