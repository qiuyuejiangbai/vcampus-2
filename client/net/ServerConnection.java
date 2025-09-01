package client.net;

import common.protocol.Message;
import common.protocol.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器连接类
 * 负责客户端与服务器的网络通信
 */
public class ServerConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private boolean isConnected = false;
    private ExecutorService executor;
    
    // 消息监听器管理
    private final ConcurrentHashMap<MessageType, MessageListener> messageListeners = new ConcurrentHashMap<>();
    private MessageListener defaultMessageListener;
    
    // 单例实例
    private static ServerConnection instance;
    
    private ServerConnection() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    private ServerConnection(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 获取单例实例
     * @return ServerConnection实例
     */
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }
    
    /**
     * 连接到服务器
     * @return 连接成功返回true，失败返回false
     */
    public boolean connect() {
        return connect(serverHost, serverPort);
    }
    
    /**
     * 连接到指定服务器
     * @param host 服务器地址
     * @param port 服务器端口
     * @return 连接成功返回true，失败返回false
     */
    public boolean connect(String host, int port) {
        if (isConnected) {
            System.out.println("已经连接到服务器");
            return true;
        }
        
        try {
            this.serverHost = host;
            this.serverPort = port;
            
            System.out.println("正在连接服务器: " + host + ":" + port);
            socket = new Socket(host, port);
            
            // 创建输入输出流
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());
            
            isConnected = true;
            
            // 启动消息接收线程
            executor.execute(this::receiveMessages);
            
            System.out.println("连接服务器成功");
            return true;
            
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * 断开与服务器的连接
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        isConnected = false;
        
        try {
            if (objectIn != null) {
                objectIn.close();
            }
            if (objectOut != null) {
                objectOut.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭连接失败: " + e.getMessage());
        }
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = Executors.newSingleThreadExecutor();
        }
        
        System.out.println("已断开服务器连接");
    }
    
    /**
     * 发送消息到服务器
     * @param message 消息对象
     * @return 发送成功返回true，失败返回false
     */
    public boolean sendMessage(Message message) {
        if (!isConnected || objectOut == null) {
            System.err.println("未连接到服务器");
            return false;
        }
        
        try {
            objectOut.writeObject(message);
            objectOut.flush();
            return true;
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * 消息接收线程
     */
    private void receiveMessages() {
        while (isConnected && !Thread.currentThread().isInterrupted()) {
            try {
                Object receivedObject = objectIn.readObject();
                
                if (receivedObject instanceof Message) {
                    Message message = (Message) receivedObject;
                    handleReceivedMessage(message);
                } else {
                    System.err.println("收到无效消息类型: " + receivedObject.getClass().getName());
                }
                
            } catch (SocketException | EOFException e) {
                // 连接断开
                System.out.println("服务器连接断开");
                break;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("接收消息失败: " + e.getMessage());
                break;
            }
        }
        
        disconnect();
    }
    
    /**
     * 处理接收到的消息
     * @param message 接收到的消息
     */
    private void handleReceivedMessage(Message message) {
        if (message == null || message.getType() == null) {
            return;
        }
        
        // 查找特定类型的监听器
        MessageListener listener = messageListeners.get(message.getType());
        if (listener != null) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("处理消息监听器异常: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (defaultMessageListener != null) {
            // 使用默认监听器
            try {
                defaultMessageListener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("处理默认消息监听器异常: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("收到未处理的消息: " + message.getType());
        }
    }
    
    /**
     * 设置消息监听器
     * @param messageType 消息类型
     * @param listener 监听器
     */
    public void setMessageListener(MessageType messageType, MessageListener listener) {
        if (messageType != null && listener != null) {
            messageListeners.put(messageType, listener);
        }
    }
    
    /**
     * 移除消息监听器
     * @param messageType 消息类型
     */
    public void removeMessageListener(MessageType messageType) {
        if (messageType != null) {
            messageListeners.remove(messageType);
        }
    }
    
    /**
     * 设置默认消息监听器
     * @param listener 默认监听器
     */
    public void setDefaultMessageListener(MessageListener listener) {
        this.defaultMessageListener = listener;
    }
    
    /**
     * 发送心跳消息
     */
    public void sendHeartbeat() {
        Message heartbeat = new Message(MessageType.HEARTBEAT);
        sendMessage(heartbeat);
    }
    
    /**
     * 检查连接状态
     * @return 连接正常返回true，断开返回false
     */
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
    
    /**
     * 获取服务器地址
     * @return 服务器地址
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * 获取服务器端口
     * @return 服务器端口
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * 设置服务器地址和端口
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public void setServerAddress(String host, int port) {
        if (!isConnected) {
            this.serverHost = host;
            this.serverPort = port;
        }
    }
    
    /**
     * 消息监听器接口
     */
    public interface MessageListener {
        /**
         * 消息接收回调
         * @param message 接收到的消息
         */
        void onMessageReceived(Message message);
    }
}
