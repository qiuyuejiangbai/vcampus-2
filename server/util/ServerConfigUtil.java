package server.util;

import java.io.*;
import java.util.Properties;

/**
 * 服务器配置工具类
 * 负责从配置文件读取服务器配置信息
 */
public class ServerConfigUtil {
    private static final int DEFAULT_PORT = 8888;
    private static final String CONFIG_FILE = "config.local.properties";
    
    private static int serverPort;
    private static boolean configLoaded = false;
    
    /**
     * 从配置文件加载服务器配置
     */
    public static void loadServerConfig() {
        Properties props = new Properties();
        try {
            // 首先尝试从resources目录加载
            InputStream is = ServerConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is == null) {
                // 如果resources中没有，尝试从当前目录加载
                File configFile = new File(CONFIG_FILE);
                if (configFile.exists()) {
                    is = new FileInputStream(configFile);
                }
            }
            
            if (is != null) {
                props.load(is);
                is.close();
                
                // 读取服务器端口配置
                String portStr = props.getProperty("server.port", String.valueOf(DEFAULT_PORT));
                try {
                    serverPort = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    System.err.println("无效的端口配置: " + portStr + "，使用默认端口: " + DEFAULT_PORT);
                    serverPort = DEFAULT_PORT;
                }
                
                System.out.println("从配置文件加载服务器端口: " + serverPort);
                configLoaded = true;
            } else {
                System.out.println("配置文件 " + CONFIG_FILE + " 不存在，使用默认端口: " + DEFAULT_PORT);
                serverPort = DEFAULT_PORT;
                configLoaded = true;
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage() + "，使用默认端口");
            serverPort = DEFAULT_PORT;
            configLoaded = true;
        }
    }
    
    /**
     * 获取服务器端口
     * @return 服务器端口
     */
    public static int getServerPort() {
        if (!configLoaded) {
            loadServerConfig();
        }
        return serverPort;
    }
    
    /**
     * 重新加载服务器配置（用于动态更新）
     */
    public static void reloadServerConfig() {
        configLoaded = false;
        loadServerConfig();
        System.out.println("服务器配置已重新加载，端口: " + serverPort);
    }
    
    /**
     * 检查配置是否已加载
     * @return 已加载返回true，未加载返回false
     */
    public static boolean isConfigLoaded() {
        return configLoaded;
    }
}
