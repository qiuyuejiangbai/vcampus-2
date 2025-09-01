package server.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 提供数据库连接的创建、关闭等功能
 */
public class DatabaseUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    
    static {
        loadConfig();
    }
    
    /**
     * 加载数据库配置
     * 优先加载本地配置文件 config.local.properties，如果不存在则使用 config.properties
     */
    private static void loadConfig() {
        Properties props = new Properties();
        InputStream is = null;
        String configFile = null;
        
        try {
            // 优先尝试加载本地配置文件
            is = DatabaseUtil.class.getClassLoader().getResourceAsStream("config.local.properties");
            if (is != null) {
                configFile = "config.local.properties";
                System.out.println("使用本地开发配置文件: " + configFile);
            } else {
                // 如果本地配置不存在，使用默认配置
                is = DatabaseUtil.class.getClassLoader().getResourceAsStream("config.properties");
                if (is != null) {
                    configFile = "config.properties";
                    System.out.println("使用默认配置文件: " + configFile);
                }
            }
            
            if (is != null) {
                props.load(is);
                url = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");
                driver = props.getProperty("db.driver");
                
                // 加载数据库驱动
                Class.forName(driver);
                System.out.println("数据库配置加载成功");
                
                // 如果是本地配置，显示开发者信息
                String developerName = props.getProperty("developer.name");
                if (developerName != null) {
                    System.out.println("开发者: " + developerName);
                }
            } else {
                System.err.println("无法找到配置文件 config.local.properties 或 config.properties");
                // 使用默认配置
                setDefaultConfig();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载数据库配置失败: " + e.getMessage());
            // 使用默认配置
            setDefaultConfig();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("关闭配置文件流失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 设置默认配置
     */
    private static void setDefaultConfig() {
        url = "jdbc:mysql://localhost:3306/vcampus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        username = "root";
        password = "123456";
        driver = "com.mysql.cj.jdbc.Driver";
        
        try {
            Class.forName(driver);
            System.out.println("使用默认数据库配置");
        } catch (ClassNotFoundException e) {
            System.err.println("加载数据库驱动失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接对象
     * @throws SQLException SQL异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * 关闭数据库连接
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("关闭数据库连接失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 关闭PreparedStatement
     * @param pstmt PreparedStatement对象
     */
    public static void closePreparedStatement(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.err.println("关闭PreparedStatement失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 关闭ResultSet
     * @param rs ResultSet对象
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("关闭ResultSet失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 关闭所有数据库资源
     * @param conn 数据库连接
     * @param pstmt PreparedStatement对象
     * @param rs ResultSet对象
     */
    public static void closeAll(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        closeResultSet(rs);
        closePreparedStatement(pstmt);
        closeConnection(conn);
    }
    
    /**
     * 测试数据库连接
     * @return true表示连接成功，false表示连接失败
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("数据库连接测试成功");
            return true;
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取数据库配置信息（用于调试）
     * @return 配置信息字符串
     */
    public static String getConfigInfo() {
        return "Database Config: " +
               "URL=" + url + ", " +
               "Username=" + username + ", " +
               "Driver=" + driver;
    }
    
    /**
     * 主方法：用于测试数据库连接
     */
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("数据库连接测试工具");
        System.out.println("=================================");
        
        try {
            // 显示配置信息
            System.out.println("当前配置信息:");
            System.out.println(getConfigInfo());
            System.out.println();
            
            // 测试数据库连接
            System.out.println("正在测试数据库连接...");
            if (testConnection()) {
                System.out.println("✓ 数据库连接成功！");
            } else {
                System.out.println("✗ 数据库连接失败！");
                System.out.println();
                System.out.println("请检查以下项目:");
                System.out.println("1. MySQL服务是否启动");
                System.out.println("2. 数据库vcampus是否存在");
                System.out.println("3. 用户名密码是否正确");
                System.out.println("4. 端口3306是否可访问");
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=================================");
    }
}
