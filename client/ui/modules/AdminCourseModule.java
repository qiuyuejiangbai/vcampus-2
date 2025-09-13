package client.ui.modules;

import client.net.ServerConnection;
import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.ui.integration.ModuleRegistry;
import common.vo.UserVO;
import client.ui.modules.course.UITheme;

import javax.swing.*;
import java.awt.*;

/** 管理员课程管理模块 */
public class AdminCourseModule implements IModuleView {
    private JPanel root;  //创建根面板
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;

    public AdminCourseModule() { 
        buildUI(); 
    }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setOpaque(true);  //设置根面板为不透明
        root.setBackground(UIManager.getColor("Panel.background"));
        
        // 主容器区域
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 内容区域
        JPanel contentPanel = getContentPanel();
        mainContent.add(contentPanel, BorderLayout.CENTER);
        
        root.add(mainContent, BorderLayout.CENTER);
    }

    private static JPanel getContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 占位
        JLabel placeholderLabel = new JLabel("课程管理功能开发中...", JLabel.CENTER);
        placeholderLabel.setFont(UITheme.DEFAULT_FONT);
        placeholderLabel.setForeground(UITheme.LIGHT_GRAY);
        contentPanel.add(placeholderLabel, BorderLayout.CENTER);
        return contentPanel;
    }


    /**
     * 注册到模块注册表
     */
    @Override 
    public String getKey() { 
        return ModuleKeys.ADMIN_COURSE; 
    }
    
    @Override 
    public String getDisplayName() { 
        return "课程管理"; 
    }
    
    @Override 
    public String getIconPath() { 
        return null;
    }
    
    @Override 
    public JComponent getComponent() { 
        return root; 
    }
    
    @Override 
    public void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection) {
        this.currentUser = currentUser;
        this.connection = connection;
    }

    public static void registerTo(Class<?> ignored) { 
        ModuleRegistry.register(new AdminCourseModule()); 
    }

    public UserVO getCurrentUser() {
        return currentUser;
    }

    public ServerConnection getConnection() {
        return connection;
    }
}
