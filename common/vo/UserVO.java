package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户值对象
 * 用于封装用户信息在客户端和服务器端之间传输
 */
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;         // 用户ID
    private String loginId;         // 登录ID（学号/教工号）
    private String name;            // 姓名
    private String password;        // 密码（传输时为明文，存储时为哈希值）
    private Integer role;           // 角色：0-学生，1-教师，2-管理员
    private Integer status;         // 状态：0-未激活，1-已激活
    private String phone;           // 联系电话
    private String email;           // 邮箱
    private Double balance;         // 账户余额
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    public UserVO() {}
    
    public UserVO(String loginId, String name, String password, Integer role) {
        this.loginId = loginId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.status = 0; // 默认未激活
        this.balance = 0.0; // 默认余额为0
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getLoginId() {
        return loginId;
    }
    
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
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
     * 获取状态名称
     * @return 状态名称字符串
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未激活";
            case 1: return "已激活";
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
    
    /**
     * 检查账户是否已激活
     * @return true表示已激活，false表示未激活
     */
    public boolean isActivated() {
        return status != null && status == 1;
    }
    
    @Override
    public String toString() {
        return "UserVO{" +
                "userId=" + userId +
                ", loginId='" + loginId + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", balance=" + balance +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserVO userVO = (UserVO) obj;
        return userId != null && userId.equals(userVO.userId);
    }
    
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
