package server.service;

import common.vo.UserVO;
import server.dao.UserDAO;
import server.dao.impl.UserDAOImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                System.out.println("创建头像存储目录: " + avatarDir.toAbsolutePath());
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
                System.out.println("头像上传成功: " + avatarPath.toAbsolutePath());
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
            Path path = Paths.get(avatarPath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            } else {
                System.err.println("头像文件不存在: " + path.toAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            System.err.println("读取头像文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
