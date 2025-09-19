package client.ui.api;

import javax.swing.JComponent;

/**
 * 模块统一接口：他人仅需实现本接口即可将页面接入主界面。
 * 严禁在此发起耗时的业务调用，构造 UI 即可。
 */
public interface IModuleView {
    /** 全局唯一 key，必须与侧栏导航一致，用于 ContentHost 切换 */
    String getKey();

    /** 侧栏显示名（如"课程"、"成绩"） */
    String getDisplayName();

    /** 16~20px 图标路径（仅 PNG） */
    String getIconPath();

    /** 实际可显示的根组件（不要在此发起业务调用，UI 装好即可） */
    JComponent getComponent();

    /**
     * UI 初始化后注入上下文（如当前登录用户、连接等），仅存引用，不做耗时。
     */
    void initContext(common.vo.UserVO currentUser,
                     client.net.ServerConnection connection);
    
    /**
     * 清理模块资源，在用户登出时调用
     * 子类应该重写此方法来清理控制器、连接、缓存等资源
     */
    default void dispose() {
        // 默认实现为空，子类可以重写
    }
}


