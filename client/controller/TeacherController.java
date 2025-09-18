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
    
    public TeacherController() {
        this.serverConnection = ServerConnection.getInstance();
        setupMessageListeners();
    }
    
    /**
     * 设置消息监听器
     */
    private void setupMessageListeners() {
        // 检查是否已经存在监听器，避免覆盖
        if (serverConnection.getMessageListener(MessageType.GET_TEACHER_INFO_SUCCESS) != null) {
            System.out.println("[DEBUG][TeacherController] 监听器已存在，跳过设置");
            return;
        }
        
        // 获取教师信息响应监听器
        serverConnection.setMessageListener(MessageType.GET_TEACHER_INFO_SUCCESS, message -> {
            System.out.println("[DEBUG][TeacherController] ========== 收到GET_TEACHER_INFO_SUCCESS响应 ==========");
            System.out.println("[DEBUG][TeacherController] 消息类型：" + message.getType());
            System.out.println("[DEBUG][TeacherController] 消息数据：" + message.getData());
            System.out.println("[DEBUG][TeacherController] currentGetTeacherInfoCallback=" + (currentGetTeacherInfoCallback != null ? "非null" : "null"));
            
            if (currentGetTeacherInfoCallback != null) {
                TeacherVO teacher = (TeacherVO) message.getData();
                System.out.println("[DEBUG][TeacherController] 解析教师数据：" + (teacher != null ? 
                    ("姓名=" + teacher.getName() + ", 学院=" + teacher.getDepartment() + ", 职称=" + teacher.getTitle()) : "null"));
                currentGetTeacherInfoCallback.onSuccess(teacher);
                currentGetTeacherInfoCallback = null;
                System.out.println("[DEBUG][TeacherController] 回调执行完成，回调函数已清空");
            } else {
                System.err.println("[DEBUG][TeacherController] 回调函数为null，可能是重复响应或监听器被覆盖");
            }
            System.out.println("[DEBUG][TeacherController] ========== GET_TEACHER_INFO_SUCCESS处理完成 ==========");
        });
        
        serverConnection.setMessageListener(MessageType.GET_TEACHER_INFO_FAIL, message -> {
            System.err.println("[DEBUG][TeacherController] 收到失败响应");
            if (currentGetTeacherInfoCallback != null) {
                String errorMsg = message.getMessage() != null ? message.getMessage() : "获取教师信息失败";
                System.err.println("[DEBUG][TeacherController] 失败原因：" + errorMsg);
                currentGetTeacherInfoCallback.onFailure(errorMsg);
                currentGetTeacherInfoCallback = null;
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
    }
    
    /**
     * 获取教师信息
     * @param userId 用户ID，可以为null（服务器根据会话判断）
     * @param callback 获取教师信息回调
     */
    public void getTeacherInfo(Integer userId, GetTeacherInfoCallback callback) {
        System.out.println("[DEBUG][TeacherController] 开始获取教师信息，userId=" + userId);
        
        if (!serverConnection.isConnected()) {
            System.err.println("[DEBUG][TeacherController] 服务器未连接");
            callback.onFailure("未连接到服务器");
            return;
        }
        
        System.out.println("[DEBUG][TeacherController] 服务器连接正常，准备发送请求");
        
        // 创建请求消息，数据可以为userId或null
        Message request = new Message(MessageType.GET_TEACHER_INFO_REQUEST, userId);
        System.out.println("[DEBUG][TeacherController] 创建请求消息：" + request.getType());
        
        // 设置回调
        this.currentGetTeacherInfoCallback = callback;
        System.out.println("[DEBUG][TeacherController] 设置回调函数");
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        System.out.println("[DEBUG][TeacherController] 发送消息结果：" + sent);
        
        if (!sent) {
            this.currentGetTeacherInfoCallback = null;
            System.err.println("[DEBUG][TeacherController] 发送请求失败");
            callback.onFailure("发送请求失败");
        } else {
            System.out.println("[DEBUG][TeacherController] 请求发送成功，等待服务器响应");
        }
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
