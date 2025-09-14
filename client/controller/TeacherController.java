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
    
    public TeacherController() {
        this.serverConnection = ServerConnection.getInstance();
        setupMessageListeners();
    }
    
    /**
     * 设置消息监听器
     */
    private void setupMessageListeners() {
        // 获取教师信息响应监听器
        serverConnection.setMessageListener(MessageType.GET_TEACHER_INFO_SUCCESS, message -> {
            System.out.println("[DEBUG][TeacherController] 收到成功响应");
            if (currentGetTeacherInfoCallback != null) {
                TeacherVO teacher = (TeacherVO) message.getData();
                System.out.println("[DEBUG][TeacherController] 解析教师数据：" + (teacher != null ? 
                    ("姓名=" + teacher.getName() + ", 学院=" + teacher.getDepartment() + ", 职称=" + teacher.getTitle()) : "null"));
                currentGetTeacherInfoCallback.onSuccess(teacher);
                currentGetTeacherInfoCallback = null;
            } else {
                System.err.println("[DEBUG][TeacherController] 回调函数为null，可能是重复响应");
            }
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
}
