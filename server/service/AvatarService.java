package server.service;

import common.vo.UserVO;
import server.dao.UserDAO;
import server.dao.impl.UserDAOImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 头像服务类
 * 处理头像上传、存储和管理相关业务逻辑
 */
public class AvatarService {
    private final UserDAO userDAO;
    
    // 头像存储目录
    private static final String AVATAR_DIR = "resources/avatars";
    private static final String DEFAULT_AVATAR_PATH = "resources/icons/默认头像.png";
    
    // 支持的头像文件类型
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    
    // 头像文件大小限制（2MB）
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    
    public AvatarService() {
        this.userDAO = new UserDAOImpl();
        // 确保头像目录存在
        createAvatarDirectory();
    }
    
    /**
     * 创建头像存储目录
     */
    private void createAvatarDirectory() {
        try {
            Path avatarDir = Paths.get(AVATAR_DIR);
            if (!Files.exists(avatarDir)) {
                Files.createDirectories(avatarDir);
            }
        } catch (IOException e) {
            System.err.println("创建头像存储目录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param fileData 文件数据
     * @param fileName 文件名
     * @return 上传成功返回头像路径，失败返回null
     */
    public String uploadAvatar(Integer userId, byte[] fileData, String fileName) {
        if (userId == null || fileData == null || fileName == null) {
            System.err.println("头像上传参数不能为空");
            return null;
        }
        
        // 验证文件大小
        if (fileData.length > MAX_FILE_SIZE) {
            System.err.println("头像文件大小超过限制: " + fileData.length + " bytes");
            return null;
        }
        
        // 验证文件类型
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isAllowedExtension(extension)) {
            System.err.println("不支持的头像文件类型: " + extension);
            return null;
        }
        
        try {
            // 生成唯一的文件名
            String uniqueFileName = generateUniqueFileName(userId, extension);
            Path avatarPath = Paths.get(AVATAR_DIR, uniqueFileName);
            
            // 保存文件
            Files.write(avatarPath, fileData);
            
            // 更新数据库中的头像路径
            String relativePath = AVATAR_DIR + "/" + uniqueFileName;
            boolean updated = userDAO.updateAvatarPath(userId, relativePath);
            
            if (updated) {
                return relativePath;
            } else {
                // 如果数据库更新失败，删除已保存的文件
                Files.deleteIfExists(avatarPath);
                System.err.println("更新数据库头像路径失败");
                return null;
            }
        } catch (IOException e) {
            System.err.println("头像文件保存失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取用户头像路径
     * @param userId 用户ID
     * @return 头像路径，如果没有头像返回默认头像路径
     */
    public String getUserAvatarPath(Integer userId) {
        if (userId == null) {
            return DEFAULT_AVATAR_PATH;
        }
        
        UserVO user = userDAO.findById(userId);
        if (user == null || user.getAvatarPath() == null || user.getAvatarPath().trim().isEmpty()) {
            return DEFAULT_AVATAR_PATH;
        }
        
        // 检查头像文件是否存在
        Path avatarPath = Paths.get(user.getAvatarPath());
        if (!Files.exists(avatarPath)) {
            System.err.println("头像文件不存在: " + avatarPath.toAbsolutePath());
            return DEFAULT_AVATAR_PATH;
        }
        
        return user.getAvatarPath();
    }
    
    /**
     * 删除用户头像
     * @param userId 用户ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUserAvatar(Integer userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            UserVO user = userDAO.findById(userId);
            if (user != null && user.getAvatarPath() != null && !user.getAvatarPath().trim().isEmpty()) {
                // 删除文件
                Path avatarPath = Paths.get(user.getAvatarPath());
                Files.deleteIfExists(avatarPath);
                
                // 更新数据库
                return userDAO.updateAvatarPath(userId, null);
            }
            return true;
        } catch (IOException e) {
            System.err.println("删除头像文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 批量更新现有没有头像的用户，设置默认头像路径
     * @return 更新的用户数量
     */
    public int updateUsersWithoutAvatar() {
        try {
            // 获取所有没有头像的用户
            List<UserVO> usersWithoutAvatar = userDAO.findUsersWithoutAvatar();
            int updatedCount = 0;
            
            for (UserVO user : usersWithoutAvatar) {
                boolean updated = userDAO.updateAvatarPath(user.getUserId(), DEFAULT_AVATAR_PATH);
                if (updated) {
                    updatedCount++;
                }
            }
            
            return updatedCount;
        } catch (Exception e) {
            System.err.println("批量更新用户头像路径失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名（包含点号）
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * 检查文件扩展名是否被允许
     * @param extension 文件扩展名
     * @return 是否允许
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 生成唯一的文件名
     * @param userId 用户ID
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(Integer userId, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "avatar_" + userId + "_" + uuid + extension;
    }
    
    /**
     * 获取头像文件数据
     * @param avatarPath 头像路径
     * @return 文件数据，失败返回null
     */
    public byte[] getAvatarData(String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 处理路径格式问题
            String normalizedPath = normalizeAvatarPath(avatarPath);
            
            Path path = Paths.get(normalizedPath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            } else {
                System.err.println("[AvatarService] 头像文件不存在: " + path.toAbsolutePath());
                
                // 尝试其他可能的路径格式
                String[] possiblePaths = generatePossiblePaths(avatarPath);
                for (String possiblePath : possiblePaths) {
                    Path testPath = Paths.get(possiblePath);
                    if (Files.exists(testPath)) {
                        return Files.readAllBytes(testPath);
                    }
                }
                
                System.err.println("[AvatarService] 所有可能的头像路径都不存在");
                return null;
            }
        } catch (IOException e) {
            System.err.println("[AvatarService] 读取头像文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 标准化头像路径
     * @param avatarPath 原始头像路径
     * @return 标准化后的路径
     */
    private String normalizeAvatarPath(String avatarPath) {
        // 如果路径已经包含resources/，直接返回
        if (avatarPath.startsWith("resources/")) {
            return avatarPath;
        }
        
        // 如果路径以avatars/开头，添加resources/前缀
        if (avatarPath.startsWith("avatars/")) {
            return "resources/" + avatarPath;
        }
        
        // 如果路径不包含目录分隔符，假设它在avatars目录下
        if (!avatarPath.contains("/") && !avatarPath.contains("\\")) {
            return "resources/avatars/" + avatarPath;
        }
        
        // 其他情况，尝试添加resources/前缀
        return "resources/" + avatarPath;
    }
    
    /**
     * 生成可能的头像路径列表
     * @param avatarPath 原始头像路径
     * @return 可能的路径数组
     */
    private String[] generatePossiblePaths(String avatarPath) {
        return new String[]{
            avatarPath,  // 原始路径
            "resources/" + avatarPath,  // 添加resources前缀
            avatarPath.replace("avatars/", "resources/avatars/"),  // 确保有resources前缀
            avatarPath.replace("avatars\\", "resources/avatars/"),  // 处理Windows路径分隔符
            "resources/avatars/" + avatarPath,  // 假设在avatars目录下
            "avatars/" + avatarPath,  // 假设在avatars目录下
        };
    }
}
