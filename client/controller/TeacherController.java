package client.controller;

import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;
import common.vo.TeacherVO;

/**
 * 教师控制器
 * 处理教师相关的客户端业务逻辑
 */
public class TeacherController {
    private ServerConnection serverConnection;
    private GetTeacherInfoCallback currentGetTeacherInfoCallback;
    private UpdateTeacherCallback currentUpdateTeacherCallback;
    private static volatile boolean listenersSetup = false;
    
    public TeacherController() {
        this.serverConnection = ServerConnection.getInstance();
        setupMessageListeners();
    }
    
    /**
     * 设置消息监听器
     */
    private void setupMessageListeners() {
        // 使用静态标志确保监听器只设置一次
        if (listenersSetup) {
            System.out.println("[DEBUG][TeacherController] 监听器已设置，跳过");
            return;
        }
        
        synchronized (TeacherController.class) {
            if (listenersSetup) {
                return;
            }
            
            System.out.println("[DEBUG][TeacherController] 设置教师信息监听器");
        
        // 获取教师信息响应监听器
        serverConnection.setMessageListener(MessageType.GET_TEACHER_INFO_SUCCESS, message -> {
            System.out.println("[DEBUG][TeacherController] ========== 收到GET_TEACHER_INFO_SUCCESS响应 ==========");
            System.out.println("[DEBUG][TeacherController] 响应接收线程：" + Thread.currentThread().getName());
            System.out.println("[DEBUG][TeacherController] 响应接收时间：" + java.time.LocalDateTime.now());
            System.out.println("[DEBUG][TeacherController] 消息类型：" + message.getType());
            System.out.println("[DEBUG][TeacherController] 消息数据：" + message.getData());
            System.out.println("[DEBUG][TeacherController] 消息时间戳：" + message.getTimestamp());
            System.out.println("[DEBUG][TeacherController] currentGetTeacherInfoCallback=" + (currentGetTeacherInfoCallback != null ? "非null" : "null"));
            
            GetTeacherInfoCallback callback = null;
            synchronized (this) {
                callback = currentGetTeacherInfoCallback;
                currentGetTeacherInfoCallback = null; // 立即清空，避免重复调用
            }
            
            if (callback != null) {
                System.out.println("[DEBUG][TeacherController] 开始解析教师数据");
                Object data = message.getData();
                System.out.println("[DEBUG][TeacherController] 原始数据类型：" + (data != null ? data.getClass().getSimpleName() : "null"));
                System.out.println("[DEBUG][TeacherController] 原始数据值：" + data);
                
                TeacherVO teacher = (TeacherVO) data;
                System.out.println("[DEBUG][TeacherController] 类型转换结果：" + (teacher != null ? "成功" : "失败"));
                System.out.println("[DEBUG][TeacherController] 解析教师数据：" + (teacher != null ? 
                    ("姓名=" + teacher.getName() + ", 学院=" + teacher.getDepartment() + ", 职称=" + teacher.getTitle()) : "null"));
                
                // 详细打印教师信息
                if (teacher != null) {
                    System.out.println("[DEBUG][TeacherController] 教师详细信息：");
                    System.out.println("  - ID=" + teacher.getId());
                    System.out.println("  - 用户ID=" + teacher.getUserId());
                    System.out.println("  - 姓名=" + teacher.getName());
                    System.out.println("  - 工号=" + teacher.getTeacherNo());
                    System.out.println("  - 电话=" + teacher.getPhone());
                    System.out.println("  - 邮箱=" + teacher.getEmail());
                    System.out.println("  - 学院=" + teacher.getDepartment());
                    System.out.println("  - 职称=" + teacher.getTitle());
                    System.out.println("  - 办公室=" + teacher.getOffice());
                    System.out.println("  - 研究方向=" + teacher.getResearchArea());
                    System.out.println("  - 余额=" + teacher.getBalance());
                    System.out.println("  - 创建时间=" + teacher.getCreatedTime());
                    System.out.println("  - 更新时间=" + teacher.getUpdatedTime());
                    
                    if (teacher.getUser() != null) {
                        System.out.println("[DEBUG][TeacherController] 关联用户信息：");
                        System.out.println("    - 用户ID=" + teacher.getUser().getUserId());
                        System.out.println("    - 登录ID=" + teacher.getUser().getLoginId());
                        System.out.println("    - 角色=" + teacher.getUser().getRole());
                        System.out.println("    - 头像路径=" + teacher.getUser().getAvatarPath());
                    } else {
                        System.err.println("[DEBUG][TeacherController] 警告：teacher.getUser()为null");
                    }
                } else {
                    System.err.println("[DEBUG][TeacherController] 错误：解析得到的teacher对象为null");
                }
                
                System.out.println("[DEBUG][TeacherController] 准备调用回调函数onSuccess");
                System.out.println("[DEBUG][TeacherController] 回调函数类型：" + callback.getClass().getSimpleName());
                
                try {
                    callback.onSuccess(teacher);
                    System.out.println("[DEBUG][TeacherController] 回调执行完成，无异常");
                } catch (Exception e) {
                    System.err.println("[DEBUG][TeacherController] 回调执行时发生异常：" + e.getMessage());
                    System.err.println("[DEBUG][TeacherController] 异常类型：" + e.getClass().getSimpleName());
                    e.printStackTrace();
                }
            } else {
                System.err.println("[DEBUG][TeacherController] 错误：回调函数为null");
                System.err.println("[DEBUG][TeacherController] 可能原因：");
                System.err.println("  1. 重复响应，回调已被清空");
                System.err.println("  2. 监听器被覆盖");
                System.err.println("  3. 线程竞争条件");
                System.err.println("[DEBUG][TeacherController] 当前回调状态：" + (currentGetTeacherInfoCallback != null ? "非null" : "null"));
            }
            System.out.println("[DEBUG][TeacherController] ========== GET_TEACHER_INFO_SUCCESS处理完成 ==========");
        });
        
        serverConnection.setMessageListener(MessageType.GET_TEACHER_INFO_FAIL, message -> {
            System.err.println("[DEBUG][TeacherController] 收到失败响应");
            GetTeacherInfoCallback callback = null;
            synchronized (this) {
                callback = currentGetTeacherInfoCallback;
                currentGetTeacherInfoCallback = null;
            }
            
            if (callback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "获取教师信息失败";
                System.err.println("[DEBUG][TeacherController] 失败原因：" + errorMsg);
                callback.onFailure(errorMsg);
            } else {
                System.err.println("[DEBUG][TeacherController] 回调函数为null，可能是重复响应");
            }
        });
        
        // 更新教师信息响应监听器
        serverConnection.setMessageListener(MessageType.UPDATE_TEACHER_SUCCESS, message -> {
            System.out.println("[DEBUG][TeacherController] 收到更新成功响应");
            if (currentUpdateTeacherCallback != null) {
                String successMsg = message.getMessage() != null ? message.getMessage() : "更新成功";
                currentUpdateTeacherCallback.onSuccess(successMsg);
                currentUpdateTeacherCallback = null;
            }
        });
        
        serverConnection.setMessageListener(MessageType.UPDATE_TEACHER_FAIL, message -> {
            System.err.println("[DEBUG][TeacherController] 收到更新失败响应");
            if (currentUpdateTeacherCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "更新教师信息失败";
                currentUpdateTeacherCallback.onFailure(errorMsg);
                currentUpdateTeacherCallback = null;
            }
        });
        
        listenersSetup = true;
        System.out.println("[DEBUG][TeacherController] 监听器设置完成");
        }
    }
    
    /**
     * 获取教师信息
     * @param userId 用户ID，可以为null（服务器根据会话判断）
     * @param callback 获取教师信息回调
     */
    public void getTeacherInfo(Integer userId, GetTeacherInfoCallback callback) {
        System.out.println("[DEBUG][TeacherController] ========== 开始获取教师信息 ==========");
        System.out.println("[DEBUG][TeacherController] 调用线程：" + Thread.currentThread().getName());
        System.out.println("[DEBUG][TeacherController] 调用时间：" + java.time.LocalDateTime.now());
        System.out.println("[DEBUG][TeacherController] userId=" + userId);
        System.out.println("[DEBUG][TeacherController] userId类型：" + (userId != null ? userId.getClass().getSimpleName() : "null"));
        System.out.println("[DEBUG][TeacherController] callback=" + (callback != null ? "非null" : "null"));
        System.out.println("[DEBUG][TeacherController] callback类型：" + (callback != null ? callback.getClass().getSimpleName() : "null"));
        
        if (serverConnection == null) {
            System.err.println("[DEBUG][TeacherController] 错误：serverConnection为null！");
            System.err.println("[DEBUG][TeacherController] 可能原因：ServerConnection.getInstance()返回null");
            callback.onFailure("服务器连接对象为空");
            return;
        }
        
        System.out.println("[DEBUG][TeacherController] serverConnection状态检查：");
        System.out.println("  - serverConnection=" + (serverConnection != null ? "非null" : "null"));
        System.out.println("  - isConnected=" + serverConnection.isConnected());
        System.out.println("  - serverHost=" + serverConnection.getServerHost());
        System.out.println("  - serverPort=" + serverConnection.getServerPort());
        
        if (!serverConnection.isConnected()) {
            System.err.println("[DEBUG][TeacherController] 错误：服务器未连接");
            System.err.println("[DEBUG][TeacherController] 连接详情：");
            System.err.println("  - host=" + serverConnection.getServerHost());
            System.err.println("  - port=" + serverConnection.getServerPort());
            callback.onFailure("未连接到服务器");
            return;
        }
        
        System.out.println("[DEBUG][TeacherController] 服务器连接正常，准备发送请求");
        
        // 创建请求消息，数据可以为userId或null
        System.out.println("[DEBUG][TeacherController] 开始创建请求消息");
        Message request = new Message(MessageType.GET_TEACHER_INFO_REQUEST, userId);
        System.out.println("[DEBUG][TeacherController] 请求消息创建完成：");
        System.out.println("  - 消息类型：" + request.getType());
        System.out.println("  - 消息数据：" + request.getData());
        System.out.println("  - 消息时间戳：" + request.getTimestamp());
        
        // 检查监听器状态
        System.out.println("[DEBUG][TeacherController] 检查监听器状态：");
        System.out.println("  - GET_TEACHER_INFO_SUCCESS监听器：" + (serverConnection.getMessageListener(MessageType.GET_TEACHER_INFO_SUCCESS) != null ? "已设置" : "未设置"));
        System.out.println("  - GET_TEACHER_INFO_FAIL监听器：" + (serverConnection.getMessageListener(MessageType.GET_TEACHER_INFO_FAIL) != null ? "已设置" : "未设置"));
        
        // 设置回调（使用同步确保线程安全）
        System.out.println("[DEBUG][TeacherController] 开始设置回调函数");
        synchronized (this) {
            GetTeacherInfoCallback oldCallback = this.currentGetTeacherInfoCallback;
            this.currentGetTeacherInfoCallback = callback;
            System.out.println("[DEBUG][TeacherController] 回调函数设置完成");
            System.out.println("  - 旧回调：" + (oldCallback != null ? "非null" : "null"));
            System.out.println("  - 新回调：" + (callback != null ? "非null" : "null"));
            System.out.println("  - 当前回调：" + (this.currentGetTeacherInfoCallback != null ? "非null" : "null"));
        }
        
        // 发送请求
        System.out.println("[DEBUG][TeacherController] 准备发送消息到服务器");
        System.out.println("[DEBUG][TeacherController] 发送前连接状态：" + serverConnection.isConnected());
        boolean sent = serverConnection.sendMessage(request);
        System.out.println("[DEBUG][TeacherController] 发送消息结果：" + sent);
        System.out.println("[DEBUG][TeacherController] 发送后连接状态：" + serverConnection.isConnected());
        
        if (!sent) {
            synchronized (this) {
                this.currentGetTeacherInfoCallback = null;
            }
            System.err.println("[DEBUG][TeacherController] 发送请求失败");
            callback.onFailure("发送请求失败");
        } else {
            System.out.println("[DEBUG][TeacherController] 请求发送成功，等待服务器响应");
        }
        System.out.println("[DEBUG][TeacherController] ========== getTeacherInfo调用完成 ==========");
    }
    
    /**
     * 更新教师信息
     * @param teacher 要更新的教师信息
     * @param callback 更新教师信息回调
     */
    public void updateTeacher(TeacherVO teacher, UpdateTeacherCallback callback) {
        System.out.println("[DEBUG][TeacherController] ========== 开始更新教师信息 ==========");
        System.out.println("[DEBUG][TeacherController] 教师ID=" + teacher.getId() + ", 用户ID=" + teacher.getUserId());
        System.out.println("[DEBUG][TeacherController] 更新字段：name=" + teacher.getName() + ", phone=" + teacher.getPhone() + ", email=" + teacher.getEmail());
        System.out.println("[DEBUG][TeacherController] 专业字段：teacherNo=" + teacher.getTeacherNo() + ", title=" + teacher.getTitle() + ", office=" + teacher.getOffice() + ", researchArea=" + teacher.getResearchArea());
        
        if (!serverConnection.isConnected()) {
            System.err.println("[DEBUG][TeacherController] 服务器未连接");
            callback.onFailure("未连接到服务器");
            return;
        }
        
        // 创建请求消息
        Message request = new Message(MessageType.UPDATE_TEACHER_REQUEST, teacher);
        System.out.println("[DEBUG][TeacherController] 创建更新请求消息：" + request.getType());
        
        // 设置回调
        this.currentUpdateTeacherCallback = callback;
        System.out.println("[DEBUG][TeacherController] 设置更新回调函数");
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        System.out.println("[DEBUG][TeacherController] 发送更新消息结果：" + sent);
        
        if (!sent) {
            this.currentUpdateTeacherCallback = null;
            System.err.println("[DEBUG][TeacherController] 发送更新请求失败");
            callback.onFailure("发送请求失败");
        } else {
            System.out.println("[DEBUG][TeacherController] 更新请求发送成功，等待服务器响应");
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
     * 获取教师信息回调接口
     */
    public interface GetTeacherInfoCallback {
        void onSuccess(TeacherVO teacher);
        void onFailure(String error);
    }
    
    /**
     * 更新教师信息回调接口
     */
    public interface UpdateTeacherCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
