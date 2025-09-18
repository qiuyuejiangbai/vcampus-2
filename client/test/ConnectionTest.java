package client.test;

import client.util.ConfigUtil;
import client.net.ServerConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 网络连接测试工具
 * 用于测试客户端与服务器的连接状态
 */
public class ConnectionTest {
    
    /**
     * 测试TCP连接
     * @param host 服务器地址
     * @param port 服务器端口
     * @param timeout 超时时间（毫秒）
     * @return 连接成功返回true，失败返回false
     */
    public static boolean testTcpConnection(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (SocketTimeoutException e) {
            System.err.println("连接超时: " + host + ":" + port + " (超时时间: " + timeout + "ms)");
            return false;
        } catch (IOException e) {
            System.err.println("连接失败: " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试配置文件中的服务器连接
     */
    public static void testConfigConnection() {
        System.out.println("========================================");
        System.out.println("vCampus 连接测试工具");
        System.out.println("========================================");
        System.out.println();
        
        // 加载配置
        ConfigUtil.loadServerConfig();
        String host = ConfigUtil.getServerHost();
        int port = ConfigUtil.getServerPort();
        
        System.out.println("当前配置:");
        System.out.println("  服务器地址: " + host);
        System.out.println("  服务器端口: " + port);
        System.out.println();
        
        // 测试连接
        System.out.println("正在测试连接...");
        boolean connected = testTcpConnection(host, port, 5000);
        
        if (connected) {
            System.out.println("✓ 连接成功！服务器可达");
            
            // 测试应用层连接
            System.out.println("正在测试应用层连接...");
            ServerConnection connection = ServerConnection.getInstance();
            if (connection.connect()) {
                System.out.println("✓ 应用层连接成功！");
                connection.disconnect();
            } else {
                System.out.println("✗ 应用层连接失败！");
            }
        } else {
            System.out.println("✗ 连接失败！请检查:");
            System.out.println("  1. 服务器是否已启动");
            System.out.println("  2. 服务器地址和端口是否正确");
            System.out.println("  3. 网络连接是否正常");
            System.out.println("  4. 防火墙是否阻止了连接");
        }
        
        System.out.println();
        System.out.println("========================================");
    }
    
    /**
     * 测试指定地址的连接
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public static void testSpecificConnection(String host, int port) {
        System.out.println("========================================");
        System.out.println("vCampus 连接测试工具");
        System.out.println("========================================");
        System.out.println();
        
        System.out.println("测试目标:");
        System.out.println("  服务器地址: " + host);
        System.out.println("  服务器端口: " + port);
        System.out.println();
        
        // 测试连接
        System.out.println("正在测试连接...");
        boolean connected = testTcpConnection(host, port, 5000);
        
        if (connected) {
            System.out.println("✓ 连接成功！服务器可达");
            
            // 测试应用层连接
            System.out.println("正在测试应用层连接...");
            ServerConnection connection = ServerConnection.getInstance();
            if (connection.connect(host, port)) {
                System.out.println("✓ 应用层连接成功！");
                connection.disconnect();
            } else {
                System.out.println("✗ 应用层连接失败！");
            }
        } else {
            System.out.println("✗ 连接失败！请检查:");
            System.out.println("  1. 服务器是否已启动");
            System.out.println("  2. 服务器地址和端口是否正确");
            System.out.println("  3. 网络连接是否正常");
            System.out.println("  4. 防火墙是否阻止了连接");
        }
        
        System.out.println();
        System.out.println("========================================");
    }
    
    /**
     * 主方法：运行连接测试
     */
    public static void main(String[] args) {
        if (args.length >= 2) {
            // 测试指定地址
            try {
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                testSpecificConnection(host, port);
            } catch (NumberFormatException e) {
                System.err.println("无效的端口号: " + args[1]);
                System.out.println("用法: java ConnectionTest [host] [port]");
                System.out.println("示例: java ConnectionTest 192.168.1.100 8888");
            }
        } else {
            // 测试配置文件中的地址
            testConfigConnection();
        }
    }
}
