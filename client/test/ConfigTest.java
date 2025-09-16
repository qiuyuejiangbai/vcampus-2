package client.test;

import client.util.ConfigUtil;

/**
 * 配置工具类测试
 * 用于验证配置文件读取功能是否正常工作
 */
public class ConfigTest {
    
    public static void main(String[] args) {
        System.out.println("=== vCampus 配置测试 ===");
        
        // 测试配置读取
        System.out.println("1. 测试配置读取:");
        String host = ConfigUtil.getServerHost();
        int port = ConfigUtil.getServerPort();
        System.out.println("   服务器地址: " + host);
        System.out.println("   服务器端口: " + port);
        
        // 测试配置重新加载
        System.out.println("\n2. 测试配置重新加载:");
        ConfigUtil.reloadServerConfig();
        System.out.println("   重新加载后的服务器地址: " + ConfigUtil.getServerHost());
        System.out.println("   重新加载后的服务器端口: " + ConfigUtil.getServerPort());
        
        // 测试配置状态
        System.out.println("\n3. 配置状态:");
        System.out.println("   配置已加载: " + ConfigUtil.isConfigLoaded());
        
        System.out.println("\n=== 测试完成 ===");
        
        // 显示连接信息
        System.out.println("\n客户端将尝试连接到: " + host + ":" + port);
        System.out.println("请确保服务器在该地址上运行。");
    }
}
