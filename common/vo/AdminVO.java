package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 管理员值对象
 * 用于封装管理员信息在客户端和服务器端之间传输
 */
public class AdminVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;             // 管理员ID
    private Integer userId;         // 关联User表的id
    private String username;        // 用户名
    private String realName;        // 真实姓名
    private String adminLevel;      // 管理员级别：super, normal
    private Map<String, Object> permissions; // 权限配置
    private Timestamp createdTime;  // 创建时间
    private Timestamp updatedTime;  // 更新时间
    
    // 关联对象
    private UserVO user;            // 关联的UserVO对象
    
    // 构造函数
    public AdminVO() {}
    
    public AdminVO(String username, String realName) {
        this.username = username;
        this.realName = realName;
        this.adminLevel = "normal"; // 默认普通管理员
    }
    
    public AdminVO(String username, String realName, String adminLevel) {
        this.username = username;
        this.realName = realName;
        this.adminLevel = adminLevel;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getAdminLevel() {
        return adminLevel;
    }
    
    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }
    
    public Map<String, Object> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Map<String, Object> permissions) {
        this.permissions = permissions;
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
    
    public UserVO getUser() {
        return user;
    }
    
    public void setUser(UserVO user) {
        this.user = user;
    }
    
    // 业务方法
    
    /**
     * 检查是否为超级管理员
     * @return true表示是超级管理员，false表示不是
     */
    public boolean isSuperAdmin() {
        return "super".equals(adminLevel);
    }
    
    /**
     * 检查是否为普通管理员
     * @return true表示是普通管理员，false表示不是
     */
    public boolean isNormalAdmin() {
        return "normal".equals(adminLevel);
    }
    
    /**
     * 获取管理员级别中文名称
     * @return 管理员级别中文名称
     */
    public String getAdminLevelName() {
        if (adminLevel == null) return "未知";
        switch (adminLevel) {
            case "super": return "超级管理员";
            case "normal": return "普通管理员";
            default: return "未知";
        }
    }
    
    /**
     * 检查是否有指定权限
     * @param permission 权限名称
     * @return true表示有权限，false表示无权限
     */
    public boolean hasPermission(String permission) {
        if (isSuperAdmin()) return true; // 超级管理员拥有所有权限
        if (permissions == null) return false;
        
        Object permissionValue = permissions.get(permission);
        if (permissionValue instanceof Boolean) {
            return (Boolean) permissionValue;
        }
        return false;
    }
    
    /**
     * 获取显示名称（优先使用真实姓名，否则使用用户名）
     * @return 显示名称
     */
    public String getDisplayName() {
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        return username;
    }
    
    /**
     * 获取完整标识（显示名称 + 管理员级别）
     * @return 完整标识
     */
    public String getFullIdentity() {
        return getDisplayName() + " (" + getAdminLevelName() + ")";
    }
    
    /**
     * 添加权限
     * @param permission 权限名称
     * @param value 权限值
     */
    public void addPermission(String permission, boolean value) {
        if (permissions == null) {
            permissions = new java.util.HashMap<>();
        }
        permissions.put(permission, value);
    }
    
    /**
     * 移除权限
     * @param permission 权限名称
     */
    public void removePermission(String permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
    
    /**
     * 获取权限列表
     * @return 权限列表
     */
    public List<String> getPermissionList() {
        if (permissions == null) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(permissions.keySet());
    }
    
    /**
     * 检查是否有用户管理权限
     * @return true表示有权限，false表示无权限
     */
    public boolean canManageUsers() {
        return hasPermission("manage_users");
    }
    
    /**
     * 检查是否有课程管理权限
     * @return true表示有权限，false表示无权限
     */
    public boolean canManageCourses() {
        return hasPermission("manage_courses");
    }
    
    /**
     * 检查是否有系统配置权限
     * @return true表示有权限，false表示无权限
     */
    public boolean canManageSystem() {
        return hasPermission("manage_system");
    }
    
    /**
     * 检查是否有数据查看权限
     * @return true表示有权限，false表示无权限
     */
    public boolean canViewData() {
        return hasPermission("view_data");
    }
    
    @Override
    public String toString() {
        return "AdminVO{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", adminLevel='" + adminLevel + '\'' +
                ", permissionCount=" + (permissions != null ? permissions.size() : 0) +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AdminVO adminVO = (AdminVO) obj;
        return id != null && id.equals(adminVO.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
