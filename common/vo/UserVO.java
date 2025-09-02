package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户值对象
 * 用于封装用户信息在客户端和服务器端之间传输
 */
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;         // 数据库主键ID（自增）
    private String id;              // 登录ID（对应学生的学号、老师的工号、管理员id）
    private String password;        // 登录密码
    private Integer role;           // 角色：0-学生，1-教师，2-管理员
    private String name;            // 姓名
    private String phone;           // 电话
    private String email;           // 邮箱
    private Integer status;         // 状态
    private Double balance;         // 余额
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    public UserVO() {}
    
    public UserVO(String id, String password, Integer role) {
        this.id = id;
        this.password = password;
        this.role = role;
    }
    
    public UserVO(Integer userId, String id, String password, Integer role) {
        this.userId = userId;
        this.id = id;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Integer getRole() {
        return role;
    }
    
    public void setRole(Integer role) {
        this.role = role;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    public Timestamp getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    // 别名方法，用于兼容性
    public String getLoginId() {
        return this.id;
    }
    
    public void setLoginId(String loginId) {
        this.id = loginId;
    }
    
    // 业务方法
    
    /**
     * 获取角色名称
     * @return 角色名称字符串
     */
    public String getRoleName() {
        if (role == null) return "未知";
        switch (role) {
            case 0: return "学生";
            case 1: return "教师";
            case 2: return "管理员";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否为学生
     * @return true表示是学生，false表示不是
     */
    public boolean isStudent() {
        return role != null && role == 0;
    }
    
    /**
     * 检查是否为教师
     * @return true表示是教师，false表示不是
     */
    public boolean isTeacher() {
        return role != null && role == 1;
    }
    
    /**
     * 检查是否为管理员
     * @return true表示是管理员，false表示不是
     */
    public boolean isAdmin() {
        return role != null && role == 2;
    }
    
    @Override
    public String toString() {
        return "UserVO{" +
                "userId=" + userId +
                ", id='" + id + '\'' +
                ", role=" + role +
                ", roleName='" + getRoleName() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserVO userVO = (UserVO) obj;
        return id != null && id.equals(userVO.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
