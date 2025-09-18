package server.util;

import server.service.AvatarService;

/**
 * 头像更新工具类
 * 用于批量更新现有没有头像的用户
 */
public class AvatarUpdateUtil {
    
    /**
     * 批量更新现有没有头像的用户，设置默认头像路径
     * @return 更新的用户数量
     */
    public static int updateUsersWithoutAvatar() {
        try {
            AvatarService avatarService = new AvatarService();
            int updatedCount = avatarService.updateUsersWithoutAvatar();
            
            if (updatedCount > 0) {
            } else {
            }
            
            return updatedCount;
        } catch (Exception e) {
            System.err.println("批量更新用户头像路径失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 主方法，用于直接运行批量更新
     */
    public static void main(String[] args) {
        int updatedCount = updateUsersWithoutAvatar();
        System.out.println("批量更新完成，共更新 " + updatedCount + " 个用户");
    }
}
