package client.controller;

import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;
import common.vo.UserVO;
import java.util.Map;
import java.util.HashMap;

/**
 * 用户控制器
 * 处理用户相关的客户端业务逻辑
 */
public class UserController {
    private ServerConnection serverConnection;
    
    // 单例实例
    private static UserController instance;
    
    public UserController() {
        this.serverConnection = ServerConnection.getInstance();
        setupMessageListeners();
    }
    
    /**
     * 获取单例实例
     * @return UserController实例
     */
    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }
    
    // 标记是否已经设置过监听器
    private static boolean listenersSetup = false;
    
    /**
     * 设置消息监听器
     */
    private void setupMessageListeners() {
        // 避免重复设置监听器
        if (listenersSetup) {
            return;
        }
        listenersSetup = true;
        // 登录响应监听器
        serverConnection.setMessageListener(MessageType.LOGIN_SUCCESS, message -> {
            if (currentLoginCallback != null) {
                UserVO user = (UserVO) message.getData();
                currentLoginCallback.onSuccess(user);
                currentLoginCallback = null;
            }
        });
        
        serverConnection.setMessageListener(MessageType.LOGIN_FAIL, message -> {
            if (currentLoginCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "登录失败";
                currentLoginCallback.onFailure(errorMsg);
                currentLoginCallback = null;
            }
        });
        
        // 注册响应监听器
        serverConnection.setMessageListener(MessageType.REGISTER_SUCCESS, message -> {
            if (currentRegisterCallback != null) {
                currentRegisterCallback.onSuccess("注册成功，账户已激活");
                currentRegisterCallback = null;
            }
        });
        
        serverConnection.setMessageListener(MessageType.REGISTER_FAIL, message -> {
            if (currentRegisterCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "注册失败";
                currentRegisterCallback.onFailure(errorMsg);
                currentRegisterCallback = null;
            }
        });
        
        // 头像上传响应监听器
        serverConnection.setMessageListener(MessageType.UPLOAD_AVATAR_SUCCESS, message -> {
            if (currentAvatarUploadCallback != null) {
                String avatarPath = (String) message.getData();
                currentAvatarUploadCallback.onSuccess(avatarPath);
                currentAvatarUploadCallback = null;
            }
        });
        
        serverConnection.setMessageListener(MessageType.UPLOAD_AVATAR_FAIL, message -> {
            if (currentAvatarUploadCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "头像上传失败";
                currentAvatarUploadCallback.onFailure(errorMsg);
                currentAvatarUploadCallback = null;
            }
        });
        
        // 头像下载响应监听器
        serverConnection.setMessageListener(MessageType.DOWNLOAD_AVATAR_SUCCESS, message -> {
            if (currentAvatarDownloadCallback != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) message.getData();
                byte[] avatarData = (byte[]) data.get("avatarData");
                String avatarPath = (String) data.get("avatarPath");
                currentAvatarDownloadCallback.onSuccess(avatarData, avatarPath);
                currentAvatarDownloadCallback = null;
            }
        });
        
        serverConnection.setMessageListener(MessageType.DOWNLOAD_AVATAR_FAIL, message -> {
            if (currentAvatarDownloadCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "头像下载失败";
                currentAvatarDownloadCallback.onFailure(errorMsg);
                currentAvatarDownloadCallback = null;
            }
        });
    }
    
    // 回调接口
    private LoginCallback currentLoginCallback;
    private RegisterCallback currentRegisterCallback;
    private AvatarUploadCallback currentAvatarUploadCallback;
    private AvatarDownloadCallback currentAvatarDownloadCallback;
    
    /**
     * 用户登录
     * @param loginId 登录ID
     * @param password 密码
     * @param callback 登录回调
     */
    public void login(String loginId, String password, LoginCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (loginId == null || loginId.trim().isEmpty()) {
            callback.onFailure("请输入登录ID");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            callback.onFailure("请输入密码");
            return;
        }
        
        // 创建登录用户对象
        UserVO loginUser = new UserVO();
        loginUser.setId(loginId.trim());
        loginUser.setPassword(password);
        
        // 创建登录请求消息
        Message request = new Message(MessageType.LOGIN_REQUEST, loginUser);
        
        // 设置回调
        this.currentLoginCallback = callback;
        
        // 发送登录请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            this.currentLoginCallback = null;
            callback.onFailure("发送登录请求失败");
        }
    }
    
    /**
     * 用户注册
     * @param user 用户信息
     * @param callback 注册回调
     */
    public void register(UserVO user, RegisterCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (user == null) {
            callback.onFailure("用户信息不能为空");
            return;
        }
        
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            callback.onFailure("请输入登录ID");
            return;
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            callback.onFailure("请输入密码");
            return;
        }
        
        // 创建注册请求消息
        Message request = new Message(MessageType.REGISTER_REQUEST, user);
        
        // 设置回调
        this.currentRegisterCallback = callback;
        
        // 发送注册请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            this.currentRegisterCallback = null;
            callback.onFailure("发送注册请求失败");
        }
    }
    
    /**
     * 增强版用户注册 - 支持学生和教师详细信息
     * @param user 用户基础信息
     * @param name 姓名
     * @param phone 电话
     * @param email 邮箱
     * @param department 院系
     * @param major 专业（学生用）
     * @param title 职称（教师用）
     * @param callback 注册回调
     */
    public void registerWithDetails(UserVO user, String name, String phone, String email, 
                                   String department, String major, String title, RegisterCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (user == null) {
            callback.onFailure("用户信息不能为空");
            return;
        }
        
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            callback.onFailure("请输入登录ID");
            return;
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            callback.onFailure("请输入密码");
            return;
        }
        
        // 创建详细信息对象
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("user", user);
        details.put("name", name);
        details.put("phone", phone);
        details.put("email", email);
        details.put("department", department);
        
        if (user.isStudent() && major != null && !major.trim().isEmpty()) {
            details.put("major", major);
        }
        
        if (user.isTeacher() && title != null && !title.trim().isEmpty()) {
            details.put("title", title);
        }
        
        // 创建注册请求消息
        Message request = new Message(MessageType.REGISTER_REQUEST, details);
        
        // 设置回调
        this.currentRegisterCallback = callback;
        
        // 发送注册请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            this.currentRegisterCallback = null;
            callback.onFailure("发送注册请求失败");
        }
    }
    
    /**
     * 用户登出
     * @param callback 登出回调
     */
    public void logout(LogoutCallback callback) {
        if (!serverConnection.isConnected()) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        
        // 设置登出响应监听器
        serverConnection.setMessageListener(MessageType.LOGOUT_SUCCESS, message -> {
            if (callback != null) {
                callback.onSuccess();
            }
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.LOGOUT_SUCCESS);
        });
        
        // 创建登出请求消息
        Message request = new Message(MessageType.LOGOUT_REQUEST);
        
        // 发送登出请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent && callback != null) {
            callback.onSuccess(); // 发送失败也算登出成功
        }
    }
    
    /**
     * 获取用户信息
     * @param callback 获取用户信息回调
     */
    public void getUserInfo(GetUserInfoCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        // 设置响应监听器
        serverConnection.setMessageListener(MessageType.GET_USER_INFO_SUCCESS, message -> {
            UserVO user = (UserVO) message.getData();
            callback.onSuccess(user);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.GET_USER_INFO_SUCCESS);
        });
        
        // 创建请求消息
        Message request = new Message(MessageType.GET_USER_INFO_REQUEST);
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            callback.onFailure("发送请求失败");
        }
    }
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @param callback 更新用户信息回调
     */
    public void updateUser(UserVO user, UpdateUserCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (user == null) {
            callback.onFailure("用户信息不能为空");
            return;
        }
        
        // 设置响应监听器
        serverConnection.setMessageListener(MessageType.UPDATE_USER_SUCCESS, message -> {
            callback.onSuccess("更新成功");
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.UPDATE_USER_SUCCESS);
            serverConnection.removeMessageListener(MessageType.UPDATE_USER_FAIL);
        });
        
        serverConnection.setMessageListener(MessageType.UPDATE_USER_FAIL, message -> {
            String errorMsg = message.getMessage() != null ? message.getMessage() : "更新失败";
            callback.onFailure(errorMsg);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.UPDATE_USER_SUCCESS);
            serverConnection.removeMessageListener(MessageType.UPDATE_USER_FAIL);
        });
        
        // 创建请求消息
        Message request = new Message(MessageType.UPDATE_USER_REQUEST, user);
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            callback.onFailure("发送请求失败");
        }
    }
    
    /**
     * 检查服务器连接状态
     * @return 连接正常返回true，断开返回false
     */
    public boolean isConnected() {
        return serverConnection.isConnected();
    }
    
    /**
     * 登录回调接口
     */
    public interface LoginCallback {
        void onSuccess(UserVO user);
        void onFailure(String errorMessage);
    }
    
    /**
     * 注册回调接口
     */
    public interface RegisterCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
    
    /**
     * 登出回调接口
     */
    public interface LogoutCallback {
        void onSuccess();
    }
    
    /**
     * 获取用户信息回调接口
     */
    public interface GetUserInfoCallback {
        void onSuccess(UserVO user);
        void onFailure(String errorMessage);
    }
    
    /**
     * 更新用户信息回调接口
     */
    public interface UpdateUserCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
    
    /**
     * 上传头像
     * @param fileData 文件数据
     * @param fileName 文件名
     * @param callback 上传回调
     */
    public void uploadAvatar(byte[] fileData, String fileName, AvatarUploadCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (fileData == null || fileName == null) {
            callback.onFailure("头像数据不能为空");
            return;
        }
        
        // 创建上传数据
        Map<String, Object> uploadData = new HashMap<>();
        uploadData.put("fileData", fileData);
        uploadData.put("fileName", fileName);
        
        // 创建上传请求消息
        Message request = new Message(MessageType.UPLOAD_AVATAR_REQUEST, uploadData);
        
        // 设置回调
        this.currentAvatarUploadCallback = callback;
        
        // 发送上传请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            this.currentAvatarUploadCallback = null;
            callback.onFailure("发送头像上传请求失败");
        }
    }
    
    /**
     * 下载头像
     * @param avatarPath 头像路径
     * @param callback 下载回调
     */
    public void downloadAvatar(String avatarPath, AvatarDownloadCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            callback.onFailure("头像路径不能为空");
            return;
        }
        
        // 创建下载数据
        Map<String, Object> downloadData = new HashMap<>();
        downloadData.put("avatarPath", avatarPath);
        
        // 创建下载请求消息
        Message request = new Message(MessageType.DOWNLOAD_AVATAR_REQUEST, downloadData);
        
        // 设置回调
        this.currentAvatarDownloadCallback = callback;
        
        // 发送下载请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            this.currentAvatarDownloadCallback = null;
            callback.onFailure("发送头像下载请求失败");
        }
    }
    
    /**
     * 头像上传回调接口
     */
    public interface AvatarUploadCallback {
        void onSuccess(String avatarPath);
        void onFailure(String errorMessage);
    }
    
    /**
     * 头像下载回调接口
     */
    public interface AvatarDownloadCallback {
        void onSuccess(byte[] avatarData, String avatarPath);
        void onFailure(String errorMessage);
    }
}
