package server.net;

import common.protocol.Message;
import common.protocol.MessageType;
import common.protocol.StatusCode;
import common.vo.UserVO;
import server.service.UserService;

import java.util.List;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * 客户端处理器
 * 每个连接的客户端对应一个ClientHandler实例
 * 负责处理客户端请求并返回响应
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final VCampusServer server;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private Integer currentUserId; // 当前登录用户ID
    private UserVO currentUser;    // 当前登录用户信息
    private boolean isConnected = true;
    
    // 业务服务
    private final UserService userService;
    
    public ClientHandler(Socket clientSocket, VCampusServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.userService = new UserService();
        
        try {
            // 创建输入输出流
            this.objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            this.objectIn = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("创建客户端流失败: " + e.getMessage());
            disconnect();
        }
    }
    
    @Override
    public void run() {
        String clientInfo = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("客户端处理线程启动: " + clientInfo);
        
        try {
            // 主循环：处理客户端消息
            while (isConnected && !clientSocket.isClosed()) {
                try {
                    Object receivedObject = objectIn.readObject();
                    
                    if (receivedObject instanceof Message) {
                        Message request = (Message) receivedObject;
                        handleMessage(request);
                    } else {
                        System.err.println("收到无效消息类型: " + receivedObject.getClass().getName());
                        sendErrorMessage("无效的消息格式");
                    }
                    
                } catch (SocketException e) {
                    // 客户端正常断开连接
                    System.out.println("客户端断开连接: " + clientInfo);
                    break;
                } catch (EOFException e) {
                    // 客户端关闭连接
                    System.out.println("客户端关闭连接: " + clientInfo);
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("反序列化消息失败: " + e.getMessage());
                    sendErrorMessage("消息格式错误");
                } catch (IOException e) {
                    System.err.println("读取客户端消息失败: " + e.getMessage());
                    break;
                }
            }
            
        } finally {
            disconnect();
        }
    }
    
    /**
     * 处理客户端消息
     * @param request 请求消息
     */
    private void handleMessage(Message request) {
        if (request == null || request.getType() == null) {
            sendErrorMessage("无效的请求");
            return;
        }
        
        System.out.println("处理消息: " + request.getType() + " from " + 
                          (currentUser != null ? currentUser.getLoginId() : "未登录用户"));
        
        try {
            switch (request.getType()) {
                case LOGIN_REQUEST:
                    handleLogin(request);
                    break;
                    
                case REGISTER_REQUEST:
                    handleRegister(request);
                    break;
                    
                case LOGOUT_REQUEST:
                    handleLogout(request);
                    break;
                    
                case GET_USER_INFO_REQUEST:
                    handleGetUserInfo(request);
                    break;
                    
                case UPDATE_USER_REQUEST:
                    handleUpdateUser(request);
                    break;
                    
                case GET_ALL_USERS_REQUEST:
                    handleGetAllUsers(request);
                    break;
                    
                case HEARTBEAT:
                    handleHeartbeat(request);
                    break;
                    
                default:
                    handleUnsupportedRequest(request);
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理消息异常: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理登录请求
     */
    private void handleLogin(Message request) {
        System.out.println("=== 处理登录请求 ===");
        if (request.getData() instanceof UserVO) {
            UserVO loginUser = (UserVO) request.getData();
            System.out.println("收到登录请求 - ID: " + loginUser.getLoginId());
            
            UserVO user = userService.login(loginUser.getLoginId(), loginUser.getPassword());
            if (user != null) {
                // 登录成功
                this.currentUserId = user.getUserId();
                this.currentUser = user;
                
                // 添加到在线用户列表
                server.addOnlineUser(currentUserId, this);
                
                // 清除密码信息（安全考虑）
                user.setPassword(null);
                
                Message response = new Message(MessageType.LOGIN_SUCCESS, StatusCode.SUCCESS, user, "登录成功");
                sendMessage(response);
                
                System.out.println("用户登录成功: " + user.getLoginId() + " (" + user.getRoleName() + ")");
            } else {
                // 登录失败
                System.out.println("登录失败，发送失败响应");
                Message response = new Message(MessageType.LOGIN_FAIL, StatusCode.INVALID_PASSWORD, null, "用户名或密码错误");
                sendMessage(response);
            }
        } else {
            System.out.println("登录数据格式错误");
            sendErrorMessage("登录数据格式错误");
        }
        System.out.println("=== 登录请求处理完成 ===");
    }
    
    /**
     * 处理注册请求
     */
    private void handleRegister(Message request) {
        if (request.getData() instanceof UserVO) {
            UserVO newUser = (UserVO) request.getData();
            
            Integer userId = userService.register(newUser);
            if (userId != null) {
                // 注册成功
                Message response = new Message(MessageType.REGISTER_SUCCESS, StatusCode.CREATED, userId, "注册成功，等待管理员激活");
                sendMessage(response);
                
                System.out.println("新用户注册: " + newUser.getLoginId());
            } else {
                // 注册失败
                String errorMsg = "注册失败";
                if (userService.loginIdExists(newUser.getLoginId())) {
                    errorMsg = "登录ID已存在";
                }
                Message response = new Message(MessageType.REGISTER_FAIL, StatusCode.USER_EXISTS, null, errorMsg);
                sendMessage(response);
            }
        } else {
            sendErrorMessage("注册数据格式错误");
        }
    }
    
    /**
     * 处理登出请求
     */
    private void handleLogout(Message request) {
        if (currentUserId != null) {
            server.removeOnlineUser(currentUserId);
            System.out.println("用户登出: " + (currentUser != null ? currentUser.getLoginId() : currentUserId));
        }
        
        currentUserId = null;
        currentUser = null;
        
        Message response = new Message(MessageType.LOGOUT_SUCCESS, StatusCode.SUCCESS, null, "登出成功");
        sendMessage(response);
    }
    
    /**
     * 处理获取用户信息请求
     */
    private void handleGetUserInfo(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        UserVO user = userService.getUserById(currentUserId);
        if (user != null) {
            // 清除密码信息
            user.setPassword(null);
            Message response = new Message(MessageType.GET_USER_INFO_SUCCESS, StatusCode.SUCCESS, user);
            sendMessage(response);
        } else {
            sendErrorMessage("获取用户信息失败");
        }
    }
    
    /**
     * 处理更新用户信息请求
     */
    private void handleUpdateUser(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        if (request.getData() instanceof UserVO) {
            UserVO updateUser = (UserVO) request.getData();
            
            // 安全检查：只能更新自己的信息（除非是管理员）
            if (!currentUser.isAdmin() && !currentUserId.equals(updateUser.getUserId())) {
                Message response = new Message(MessageType.UPDATE_USER_FAIL, StatusCode.FORBIDDEN, null, "无权限修改他人信息");
                sendMessage(response);
                return;
            }
            
            boolean success = userService.updateUser(updateUser);
            if (success) {
                // 如果更新的是当前用户，刷新当前用户信息
                if (currentUserId.equals(updateUser.getUserId())) {
                    currentUser = userService.getUserById(currentUserId);
                }
                
                Message response = new Message(MessageType.UPDATE_USER_SUCCESS, StatusCode.SUCCESS, null, "更新成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.UPDATE_USER_FAIL, StatusCode.INTERNAL_ERROR, null, "更新失败");
                sendMessage(response);
            }
        } else {
            sendErrorMessage("更新数据格式错误");
        }
    }
    
    /**
     * 处理获取所有用户请求（管理员功能）
     */
    private void handleGetAllUsers(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            Message response = new Message(MessageType.GET_ALL_USERS_SUCCESS, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        List<UserVO> users = userService.getAllUsers();
        // 清除所有用户的密码信息
        users.forEach(user -> user.setPassword(null));
        
        Message response = new Message(MessageType.GET_ALL_USERS_SUCCESS, StatusCode.SUCCESS, users);
        sendMessage(response);
    }
    
    /**
     * 处理心跳请求
     */
    private void handleHeartbeat(Message request) {
        Message response = new Message(MessageType.HEARTBEAT, StatusCode.SUCCESS, System.currentTimeMillis());
        sendMessage(response);
    }
    
    /**
     * 处理不支持的请求
     */
    private void handleUnsupportedRequest(Message request) {
        System.err.println("不支持的请求类型: " + request.getType());
        Message response = new Message(MessageType.INVALID_REQUEST, StatusCode.BAD_REQUEST, null, "不支持的请求类型");
        sendMessage(response);
    }
    
    /**
     * 发送消息到客户端
     * @param message 消息对象
     */
    public void sendMessage(Object message) {
        if (!isConnected || objectOut == null) {
            return;
        }
        
        try {
            objectOut.writeObject(message);
            objectOut.flush();
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            disconnect();
        }
    }
    
    /**
     * 发送错误消息
     * @param errorMsg 错误信息
     */
    private void sendErrorMessage(String errorMsg) {
        Message response = new Message(MessageType.ERROR, StatusCode.BAD_REQUEST, null, errorMsg);
        sendMessage(response);
    }
    
    /**
     * 发送未授权消息
     */
    private void sendUnauthorizedMessage() {
        Message response = new Message(MessageType.ERROR, StatusCode.UNAUTHORIZED, null, "请先登录");
        sendMessage(response);
    }
    
    /**
     * 检查用户是否已登录
     * @return 已登录返回true，未登录返回false
     */
    private boolean isLoggedIn() {
        return currentUserId != null && currentUser != null;
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        isConnected = false;
        
        // 从服务器移除
        server.removeClientHandler(clientSocket);
        
        // 关闭流
        try {
            if (objectIn != null) {
                objectIn.close();
            }
            if (objectOut != null) {
                objectOut.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息，未登录返回null
     */
    public UserVO getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 检查连接是否活跃
     * @return 连接活跃返回true，已断开返回false
     */
    public boolean isConnected() {
        return isConnected && clientSocket != null && !clientSocket.isClosed();
    }
}
