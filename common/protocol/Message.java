package common.protocol;

import java.io.Serializable;

/**
 * 网络通信消息类
 * 用于客户端和服务器端之间的数据传输
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;      // 消息类型
    private int statusCode;        // 状态码
    private Object data;           // 数据载荷
    private String message;        // 消息描述
    private long timestamp;        // 时间戳
    
    public Message() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Message(MessageType type) {
        this();
        this.type = type;
    }
    
    public Message(MessageType type, Object data) {
        this(type);
        this.data = data;
    }
    
    public Message(MessageType type, int statusCode, Object data) {
        this(type, data);
        this.statusCode = statusCode;
    }
    
    public Message(MessageType type, int statusCode, Object data, String message) {
        this(type, statusCode, data);
        this.message = message;
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", statusCode=" + statusCode +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
