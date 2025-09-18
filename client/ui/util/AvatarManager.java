package client.ui.util;

import client.controller.UserController;
import client.ui.dashboard.components.CircularAvatar;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一头像管理类
 * 提供统一的头像加载、更新和管理功能
 * 支持从服务器下载、本地加载、默认头像等多种方式
 */
public class AvatarManager {
    
    // 默认头像路径
    private static final String DEFAULT_AVATAR_PATH = "resources/icons/默认头像.png";
    
    // 头像缓存（简单的内存缓存）
    private static final java.util.Map<String, Image> avatarCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 头像更新回调接口
     */
    public interface AvatarUpdateCallback {
        void onAvatarUpdated(Image avatarImage);
        void onUpdateFailed(String errorMessage);
    }
    
    /**
     * 更新头像显示（推荐使用的方法）
     * @param avatarComponent 头像组件
     * @param avatarPath 头像路径
     * @param userName 用户名（用于默认头像文字）
     * @param callback 更新回调
     */
    public static void updateAvatar(CircularAvatar avatarComponent, String avatarPath, String userName, AvatarUpdateCallback callback) {
        if (avatarComponent == null) {
            if (callback != null) {
                callback.onUpdateFailed("头像组件为空");
            }
            return;
        }
        
        // 先清除之前的头像
        avatarComponent.setAvatarImage(null);
        
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            // 使用默认头像
            loadDefaultAvatar(avatarComponent, userName, callback);
            return;
        }
        
        // 检查是否是默认头像路径
        if (isDefaultAvatarPath(avatarPath)) {
            loadDefaultAvatar(avatarComponent, userName, callback);
            return;
        }
        
        // 检查缓存
        Image cachedAvatar = avatarCache.get(avatarPath);
        if (cachedAvatar != null) {
            avatarComponent.setAvatarImage(cachedAvatar);
            if (callback != null) {
                callback.onAvatarUpdated(cachedAvatar);
            }
            return;
        }
        
        // 尝试同步加载
        Image syncAvatar = downloadAvatarFromServerSync(avatarPath);
        if (syncAvatar != null) {
            avatarCache.put(avatarPath, syncAvatar);
            avatarComponent.setAvatarImage(syncAvatar);
            if (callback != null) {
                callback.onAvatarUpdated(syncAvatar);
            }
            return;
        }
        
        // 同步加载失败，使用异步加载
        downloadAvatarFromServerAsync(avatarPath, new UserController.AvatarDownloadCallback() {
            @Override
            public void onSuccess(byte[] avatarData, String avatarPath) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        ImageIcon icon = new ImageIcon(avatarData);
                        Image img = icon.getImage();
                        if (img != null) {
                            avatarCache.put(avatarPath, img);
                            avatarComponent.setAvatarImage(img);
                            if (callback != null) {
                                callback.onAvatarUpdated(img);
                            }
                        } else {
                            loadDefaultAvatar(avatarComponent, userName, callback);
                        }
                    } catch (Exception e) {
                        System.err.println("[AvatarManager] 处理下载的头像数据失败: " + e.getMessage());
                        loadDefaultAvatar(avatarComponent, userName, callback);
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    System.err.println("[AvatarManager] 从服务器下载头像失败: " + errorMessage);
                    // 尝试本地加载作为备选方案
                    Image localAvatar = tryLoadLocalAvatar(avatarPath);
                    if (localAvatar != null) {
                        avatarCache.put(avatarPath, localAvatar);
                        avatarComponent.setAvatarImage(localAvatar);
                        if (callback != null) {
                            callback.onAvatarUpdated(localAvatar);
                        }
                    } else {
                        loadDefaultAvatar(avatarComponent, userName, callback);
                    }
                });
            }
        });
    }
    
    /**
     * 更新头像显示（简化版本，无回调）
     * @param avatarComponent 头像组件
     * @param avatarPath 头像路径
     * @param userName 用户名
     */
    public static void updateAvatar(CircularAvatar avatarComponent, String avatarPath, String userName) {
        updateAvatar(avatarComponent, avatarPath, userName, null);
    }
    
    /**
     * 从服务器同步下载头像
     * @param avatarPath 头像路径
     * @return 头像图片，失败返回null
     */
    private static Image downloadAvatarFromServerSync(String avatarPath) {
        try {
            UserController userController = new UserController();
            
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Image> result = new AtomicReference<>();
            AtomicReference<String> error = new AtomicReference<>();
            
            userController.downloadAvatar(avatarPath, new UserController.AvatarDownloadCallback() {
                @Override
                public void onSuccess(byte[] avatarData, String avatarPath) {
                    try {
                        ImageIcon icon = new ImageIcon(avatarData);
                        Image img = icon.getImage();
                        if (img != null) {
                            result.set(img);
                        } else {
                            error.set("头像数据无效");
                        }
                    } catch (Exception e) {
                        error.set("处理下载的头像数据失败: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    error.set("从服务器下载头像失败: " + errorMessage);
                    latch.countDown();
                }
            });
            
            // 等待下载完成，最多等待3秒
            if (latch.await(3, TimeUnit.SECONDS)) {
                if (error.get() != null) {
                    System.err.println("[AvatarManager] " + error.get());
                    return null;
                }
                return result.get();
            } else {
                System.err.println("[AvatarManager] 从服务器下载头像超时");
                return null;
            }
        } catch (Exception e) {
            System.err.println("[AvatarManager] 同步下载头像时发生异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从服务器异步下载头像
     * @param avatarPath 头像路径
     * @param callback 下载回调
     */
    private static void downloadAvatarFromServerAsync(String avatarPath, UserController.AvatarDownloadCallback callback) {
        try {
            UserController userController = new UserController();
            userController.downloadAvatar(avatarPath, callback);
        } catch (Exception e) {
            System.err.println("[AvatarManager] 下载头像时发生异常: " + e.getMessage());
            callback.onFailure("下载头像时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 尝试本地加载头像
     * @param avatarPath 头像路径
     * @return 头像图片，失败返回null
     */
    private static Image tryLoadLocalAvatar(String avatarPath) {
        try {
            // 修复头像路径：如果路径不以resources/开头，则添加resources/前缀
            String fullAvatarPath = avatarPath;
            if (!avatarPath.startsWith("resources/")) {
                fullAvatarPath = "resources/" + avatarPath;
            }
            
            // 检查文件是否存在
            java.io.File avatarFile = new java.io.File(fullAvatarPath);
            if (avatarFile.exists() && avatarFile.isFile()) {
                ImageIcon icon = new ImageIcon(fullAvatarPath);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    return icon.getImage();
                }
            }
            
            // 尝试其他可能的路径
            String[] possiblePaths = {
                avatarPath,
                "resources/" + avatarPath,
                avatarPath.replace("avatars/", "resources/avatars/"),
                "resources/avatars/" + avatarPath,
                "avatars/" + avatarPath
            };
            
            for (String path : possiblePaths) {
                java.io.File file = new java.io.File(path);
                if (file.exists() && file.isFile()) {
                    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                    if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                        return icon.getImage();
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("[AvatarManager] 本地头像加载失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 加载默认头像
     * @param avatarComponent 头像组件
     * @param userName 用户名
     * @param callback 回调
     */
    private static void loadDefaultAvatar(CircularAvatar avatarComponent, String userName, AvatarUpdateCallback callback) {
        try {
            // 尝试多个可能的默认头像路径
            String[] possiblePaths = {
                DEFAULT_AVATAR_PATH,
                "icons/默认头像.png",
                "../resources/icons/默认头像.png",
                "./resources/icons/默认头像.png"
            };
            
            Image defaultImage = null;
            for (String path : possiblePaths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                            defaultImage = icon.getImage();
                            break;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个路径
                }
            }
            
            // 如果文件路径都失败，尝试从类路径加载
            if (defaultImage == null) {
                try {
                    ImageIcon icon = new ImageIcon(AvatarManager.class.getClassLoader().getResource("icons/默认头像.png"));
                    if (icon != null && icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                        defaultImage = icon.getImage();
                    }
                } catch (Exception e) {
                    try {
                        ImageIcon icon = new ImageIcon(AvatarManager.class.getClassLoader().getResource("resources/icons/默认头像.png"));
                        if (icon != null && icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                            defaultImage = icon.getImage();
                        }
                    } catch (Exception e2) {
                        // 忽略
                    }
                }
            }
            
            if (defaultImage != null) {
                avatarComponent.setAvatarImage(defaultImage);
                if (callback != null) {
                    callback.onAvatarUpdated(defaultImage);
                }
            } else {
                // 使用文字默认头像
                String defaultText = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1) : "U";
                avatarComponent.setDefaultText(defaultText);
                if (callback != null) {
                    callback.onAvatarUpdated(null);
                }
            }
        } catch (Exception e) {
            System.err.println("[AvatarManager] 加载默认头像失败: " + e.getMessage());
            String defaultText = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1) : "U";
            avatarComponent.setDefaultText(defaultText);
            if (callback != null) {
                callback.onUpdateFailed("加载默认头像失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 检查是否是默认头像路径
     * @param avatarPath 头像路径
     * @return 是否是默认头像路径
     */
    private static boolean isDefaultAvatarPath(String avatarPath) {
        return avatarPath.equals(DEFAULT_AVATAR_PATH) || 
               avatarPath.equals("icons/默认头像.png") ||
               avatarPath.equals("resources/icons/默认头像.png");
    }
    
    /**
     * 清除头像缓存
     */
    public static void clearAvatarCache() {
        avatarCache.clear();
    }
    
    /**
     * 清除指定头像的缓存
     * @param avatarPath 头像路径
     */
    public static void clearAvatarCache(String avatarPath) {
        avatarCache.remove(avatarPath);
    }
    
    /**
     * 获取缓存的头像数量
     * @return 缓存的头像数量
     */
    public static int getCacheSize() {
        return avatarCache.size();
    }
    
    /**
     * 强制刷新头像（清除缓存后重新加载）
     * @param avatarComponent 头像组件
     * @param avatarPath 头像路径
     * @param userName 用户名
     * @param callback 回调
     */
    public static void refreshAvatar(CircularAvatar avatarComponent, String avatarPath, String userName, AvatarUpdateCallback callback) {
        // 清除缓存
        if (avatarPath != null) {
            clearAvatarCache(avatarPath);
        }
        // 重新加载
        updateAvatar(avatarComponent, avatarPath, userName, callback);
    }
    
    /**
     * 强制刷新头像（简化版本）
     * @param avatarComponent 头像组件
     * @param avatarPath 头像路径
     * @param userName 用户名
     */
    public static void refreshAvatar(CircularAvatar avatarComponent, String avatarPath, String userName) {
        refreshAvatar(avatarComponent, avatarPath, userName, null);
    }
}
