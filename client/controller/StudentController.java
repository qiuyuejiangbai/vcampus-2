package client.controller;

import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;
import common.protocol.StatusCode;
import common.vo.StudentVO;

/**
 * 学生控制器
 * 处理学生相关的客户端请求
 */
public class StudentController {
    private final ServerConnection serverConnection;
    
    public StudentController() {
        this.serverConnection = ServerConnection.getInstance();
    }
    
    /**
     * 获取学生详细信息的回调接口
     */
    public interface GetStudentInfoCallback {
        void onSuccess(StudentVO student);
        void onFailure(String error);
    }
    
    /**
     * 根据用户ID获取学生详细信息
     * @param userId 用户ID
     * @param callback 回调接口
     */
    public void getStudentInfo(Integer userId, GetStudentInfoCallback callback) {
        if (!serverConnection.isConnected()) {
            callback.onFailure("未连接到服务器");
            return;
        }
        
        // 设置响应监听器
        serverConnection.setMessageListener(MessageType.GET_STUDENT_INFO_SUCCESS, message -> {
            StudentVO student = (StudentVO) message.getData();
            callback.onSuccess(student);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.GET_STUDENT_INFO_SUCCESS);
        });
        
        // 创建请求消息
        Message request = new Message(MessageType.GET_STUDENT_INFO_REQUEST);
        request.setData(userId);
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        if (!sent) {
            callback.onFailure("发送请求失败");
        }
    }
}
