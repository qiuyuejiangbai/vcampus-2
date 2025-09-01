package server.net;

import server.util.DatabaseUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * vCampus服务器主类
 * 负责启动服务器，监听客户端连接，管理客户端会话
 */
public class VCampusServer {
    private static final int DEFAULT_PORT = 8888;
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    
    // 在线用户会话管理
    private final ConcurrentHashMap<Integer, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Socket, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    
    public VCampusServer() {
        this(DEFAULT_PORT);
    }
    
    public VCampusServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        try {
            // 测试数据库连接
            if (!DatabaseUtil.testConnection()) {
                System.err.println("数据库连接失败，服务器启动中止");
                return;
            }
            
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            System.out.println("=================================");
            System.out.println("vCampus虚拟校园系统服务器启动成功");
            System.out.println("监听端口: " + port);
            System.out.println("等待客户端连接...");
            System.out.println("=================================");
            
            // 主循环：监听客户端连接
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // 为每个客户端创建处理线程
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clientHandlers.put(clientSocket, handler);
                    threadPool.execute(handler);
                    
                    String clientInfo = clientSocket.getRemoteSocketAddress().toString();
                    System.out.println("新客户端连接: " + clientInfo);
                    System.out.println("当前连接数: " + clientHandlers.size());
                    
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("接受客户端连接失败: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        System.out.println("正在停止服务器...");
        isRunning = false;
        
        // 关闭所有客户端连接
        for (ClientHandler handler : clientHandlers.values()) {
            handler.disconnect();
        }
        clientHandlers.clear();
        onlineUsers.clear();
        
        // 关闭线程池
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        
        // 关闭服务器套接字
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("关闭服务器套接字失败: " + e.getMessage());
            }
        }
        
        System.out.println("服务器已停止");
    }
    
    /**
     * 添加在线用户
     * @param userId 用户ID
     * @param handler 客户端处理器
     */
    public void addOnlineUser(Integer userId, ClientHandler handler) {
        if (userId != null && handler != null) {
            onlineUsers.put(userId, handler);
            System.out.println("用户上线: " + userId + ", 当前在线用户数: " + onlineUsers.size());
        }
    }
    
    /**
     * 移除在线用户
     * @param userId 用户ID
     */
    public void removeOnlineUser(Integer userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
            System.out.println("用户下线: " + userId + ", 当前在线用户数: " + onlineUsers.size());
        }
    }
    
    /**
     * 移除客户端处理器
     * @param socket 客户端套接字
     */
    public void removeClientHandler(Socket socket) {
        if (socket != null) {
            ClientHandler handler = clientHandlers.remove(socket);
            if (handler != null) {
                // 如果该处理器对应的用户在线，也要移除
                Integer userId = handler.getCurrentUserId();
                if (userId != null) {
                    removeOnlineUser(userId);
                }
            }
            System.out.println("客户端断开连接: " + socket.getRemoteSocketAddress() + 
                             ", 当前连接数: " + clientHandlers.size());
        }
    }
    
    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 在线返回true，离线返回false
     */
    public boolean isUserOnline(Integer userId) {
        return userId != null && onlineUsers.containsKey(userId);
    }
    
    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    /**
     * 获取连接客户端数量
     * @return 连接客户端数量
     */
    public int getClientCount() {
        return clientHandlers.size();
    }
    
    /**
     * 检查服务器是否正在运行
     * @return 运行中返回true，已停止返回false
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取服务器端口
     * @return 服务器端口
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 广播消息给所有在线用户（管理员功能）
     * @param message 消息内容
     */
    public void broadcastMessage(Object message) {
        for (ClientHandler handler : onlineUsers.values()) {
            try {
                handler.sendMessage(message);
            } catch (Exception e) {
                System.err.println("广播消息失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 向指定用户发送消息
     * @param userId 用户ID
     * @param message 消息内容
     * @return 发送成功返回true，失败返回false
     */
    public boolean sendMessageToUser(Integer userId, Object message) {
        ClientHandler handler = onlineUsers.get(userId);
        if (handler != null) {
            try {
                handler.sendMessage(message);
                return true;
            } catch (Exception e) {
                System.err.println("发送消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * 主方法：启动服务器
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // 解析命令行参数
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("无效的端口号，使用默认端口: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        
        VCampusServer server = new VCampusServer(port);
        
        // 添加关闭钩子，确保服务器正常关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n收到关闭信号，正在关闭服务器...");
            server.stop();
        }));
        
        // 启动服务器
        server.start();
    }
}
