import java.sql.*;

public class test_forum_posts {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/vcampus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "123456"; // 请根据实际情况修改密码
        
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功");
            
            // 测试查询forum_posts表
            String sql = "SELECT COUNT(*) FROM forum_posts WHERE status = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("forum_posts表中status=1的记录数: " + count);
            }
            
            // 测试查询特定主题的回复
            sql = "SELECT p.* FROM forum_posts p WHERE p.thread_id = ? AND p.status = 1";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, 1); // 查询主题ID为1的回复
            rs = ps.executeQuery();
            
            int replyCount = 0;
            while (rs.next()) {
                replyCount++;
                System.out.println("回复ID: " + rs.getInt("post_id") + 
                                 ", 内容: " + rs.getString("content").substring(0, Math.min(50, rs.getString("content").length())) + 
                                 ", 作者ID: " + rs.getInt("author_id"));
            }
            System.out.println("主题ID=1的回复数量: " + replyCount);
            
            rs.close();
            ps.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
