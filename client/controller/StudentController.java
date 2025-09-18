package client.controller;

import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;
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
     * 更新学生信息的回调接口
     */
    public interface UpdateStudentCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
    
    /**
     * 根据用户ID获取学生详细信息
     * @param userId 用户ID
     * @param callback 回调接口
     */
    public void getStudentInfo(Integer userId, GetStudentInfoCallback callback) {
        System.out.println("[DEBUG][StudentController] 开始获取学生信息，userId=" + userId);
        
        if (!serverConnection.isConnected()) {
            System.err.println("[DEBUG][StudentController] 服务器未连接");
            callback.onFailure("未连接到服务器");
            return;
        }
        
        System.out.println("[DEBUG][StudentController] 服务器连接正常，准备发送请求");
        
        // 设置响应监听器
        serverConnection.setMessageListener(MessageType.GET_STUDENT_INFO_SUCCESS, message -> {
            System.out.println("[DEBUG][StudentController] 收到成功响应");
            StudentVO student = (StudentVO) message.getData();
            if (student != null) {
                System.out.println("[DEBUG][StudentController] 解析学生数据：" + 
                    "姓名=" + student.getName() + 
                    ", 学号=" + student.getStudentNo() + 
                    ", 专业=" + student.getMajor() + 
                    ", 班级=" + student.getClassName());
            } else {
                System.err.println("[DEBUG][StudentController] 学生数据为null");
            }
            callback.onSuccess(student);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.GET_STUDENT_INFO_SUCCESS);
        });
        
        // 添加失败响应监听器
        serverConnection.setMessageListener(MessageType.GET_STUDENT_INFO_FAIL, message -> {
            System.err.println("[DEBUG][StudentController] 收到失败响应");
            String errorMsg = message.getMessage() != null ? message.getMessage() : "获取学生信息失败";
            System.err.println("[DEBUG][StudentController] 失败原因：" + errorMsg);
            callback.onFailure(errorMsg);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.GET_STUDENT_INFO_FAIL);
        });
        
        // 创建请求消息
        Message request = new Message(MessageType.GET_STUDENT_INFO_REQUEST);
        request.setData(userId);
        System.out.println("[DEBUG][StudentController] 创建请求消息：" + request.getType() + ", 数据=" + userId);
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        System.out.println("[DEBUG][StudentController] 发送消息结果：" + sent);
        
        if (!sent) {
            System.err.println("[DEBUG][StudentController] 发送请求失败");
            callback.onFailure("发送请求失败");
        } else {
            System.out.println("[DEBUG][StudentController] 请求发送成功，等待服务器响应");
        }
    }
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @param callback 回调接口
     */
    public void updateStudent(StudentVO student, UpdateStudentCallback callback) {
        System.out.println("[DEBUG][StudentController] 开始更新学生信息");
        
        if (!serverConnection.isConnected()) {
            System.err.println("[DEBUG][StudentController] 服务器未连接");
            callback.onFailure("未连接到服务器");
            return;
        }
        
        if (student == null) {
            System.err.println("[DEBUG][StudentController] 学生信息为null");
            callback.onFailure("学生信息不能为空");
            return;
        }
        
        System.out.println("[DEBUG][StudentController] 准备更新学生信息：" + 
            "学生ID=" + student.getStudentId() + 
            ", 姓名=" + student.getName() + 
            ", 专业=" + student.getMajor());
        
        // 设置响应监听器
        serverConnection.setMessageListener(MessageType.UPDATE_STUDENT_SUCCESS, message -> {
            System.out.println("[DEBUG][StudentController] 收到更新成功响应");
            callback.onSuccess("更新成功");
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.UPDATE_STUDENT_SUCCESS);
            serverConnection.removeMessageListener(MessageType.UPDATE_STUDENT_FAIL);
        });
        
        serverConnection.setMessageListener(MessageType.UPDATE_STUDENT_FAIL, message -> {
            System.err.println("[DEBUG][StudentController] 收到更新失败响应");
            String errorMsg = message.getMessage() != null ? message.getMessage() : "更新失败";
            System.err.println("[DEBUG][StudentController] 更新失败原因：" + errorMsg);
            callback.onFailure(errorMsg);
            // 移除监听器
            serverConnection.removeMessageListener(MessageType.UPDATE_STUDENT_SUCCESS);
            serverConnection.removeMessageListener(MessageType.UPDATE_STUDENT_FAIL);
        });
        
        // 创建请求消息
        Message request = new Message(MessageType.UPDATE_STUDENT_REQUEST, student);
        System.out.println("[DEBUG][StudentController] 创建更新请求消息：" + request.getType());
        
        // 发送请求
        boolean sent = serverConnection.sendMessage(request);
        System.out.println("[DEBUG][StudentController] 发送更新消息结果：" + sent);
        
        if (!sent) {
            System.err.println("[DEBUG][StudentController] 发送更新请求失败");
            callback.onFailure("发送请求失败");
        } else {
            System.out.println("[DEBUG][StudentController] 更新请求发送成功，等待服务器响应");
        }
    }
}
