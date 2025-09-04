## 动态加载页面（模块）注册与集成指南

本指南说明如何在客户端 Dashboard 中新增一个“动态加载”的页面（模块），并使其：
- 通过左侧导航可见并可点击切换；
- 挂载到中心内容区 `ContentHost` 并按 key 切页；
- 获取当前用户与连接上下文。

### 关键概念与参与角色

- `IModuleView`（接口）：定义一个模块页面的最小契约。你要新增的页面需要实现它。
- `ModuleRegistry`（注册中心）：集中登记所有模块实例，避免重复并负责查询。
- `ModuleKeys`（常量）：统一的 key 常量，确保侧边导航与页面切换的 key 一致。
- `ContentHost`（容器）：基于 `CardLayout` 的页面宿主，按 key 切换显示。
- `SideNav`（侧边导航）：显示模块菜单项，点击后通知按 key 切换。
- `StudentDashboardUI`/`TeacherDashboardUI`：主界面骨架，负责初始化与装配上述部件。

相关代码位置（节选）：

```121:140:client/ui/dashboard/StudentDashboardUI.java
            m.initContext(currentUser, connection);
            contentHost.addPage(m.getKey(), m.getComponent());
            Icon icon = loadIcon(m.getIconPath());
            sideNav.addItem(m.getKey(), m.getDisplayName(), icon);
        }
        contentHost.showPage(client.ui.integration.ModuleKeys.STUDENT_HOME);
        sideNav.selectKey(client.ui.integration.ModuleKeys.STUDENT_HOME);
```

### 一、创建模块类并实现 IModuleView

1) 在合适的包（建议 `client.ui.modules`）中新建一个类，例如 `MyFeatureModule`，实现 `IModuleView`。

接口要求位于：

```1:27:client/ui/api/IModuleView.java
public interface IModuleView {
    String getKey();           // 全局唯一 key，必须与侧栏导航一致
    String getDisplayName();   // 侧栏显示名称
    String getIconPath();      // 16~20px PNG 图标相对路径或类路径
    JComponent getComponent(); // 可显示的根组件（构造 UI，禁止做耗时）
    void initContext(common.vo.UserVO currentUser, client.net.ServerConnection connection);
}
```

实现建议：
- 在构造函数中仅做 UI 组件的构建（如 `JPanel`、布局、占位 Label 等）。
- 在 `initContext` 中仅保存 `currentUser` 和 `connection` 的引用，避免耗时 IO；真正的业务数据可在用户交互后再触发异步加载。
- `getKey()` 应使用 `ModuleKeys` 中的常量，避免拼写不一致。

示例（精简版）：

```java
package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import javax.swing.*;
import java.awt.*;

public class MyFeatureModule implements IModuleView {
    private JPanel root;
    private common.vo.UserVO currentUser;
    private client.net.ServerConnection connection;

    public MyFeatureModule() {
        buildUI();
    }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        root.add(new JLabel("我的功能页面（开发中）", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    @Override public String getKey() { return ModuleKeys.STUDENT_RESOURCE_CENTER; /* 示例：替换为你的 key */ }
    @Override public String getDisplayName() { return "我的功能"; }
    @Override public String getIconPath() { return "icons/资源中心.png"; /* 建议提供 16~20px PNG */ }
    @Override public JComponent getComponent() { return root; }

    @Override public void initContext(common.vo.UserVO user, client.net.ServerConnection conn) {
        this.currentUser = user;
        this.connection = conn;
        // 仅保存引用，不做耗时操作
    }
}
```

图标加载策略（无需修改）：`StudentDashboardUI#loadIcon` 会在类路径与 `resources/` 目录等多处尝试寻找图片。

### 二、为你的模块选择或新增唯一的 key

优先复用 `client.ui.integration.ModuleKeys` 中的常量：

```1:32:client/ui/integration/ModuleKeys.java
public final class ModuleKeys {
    public static final String STUDENT_HOME = "student_home";
    public static final String STUDENT_FORUM = "student_forum";
    public static final String STUDENT_RESOURCE_CENTER = "student_resource_center";
    // ... 其他键
}
```

如需新增，请在 `ModuleKeys` 中补充常量并在命名上体现角色或功能域，如：`STUDENT_SCHEDULE`、`TEACHER_GRADE`。

### 三、注册模块到注册中心

有两种常见方式：

- 直接注册：
```java
ModuleRegistry.register(new MyFeatureModule());
```

- 提供一个便捷的静态方法（参考现有模块，如 `StudentHomeModule.registerTo(...)`）：
```java
public static void registerTo(Class<?> ignored) {
    ModuleRegistry.register(new MyFeatureModule());
}
```

学生端示例：

```121:129:client/ui/dashboard/StudentDashboardUI.java
// 学生专属首页
client.ui.modules.StudentHomeModule.registerTo(ModuleRegistry.class);
// 注册学生版论坛与资源中心
ModuleRegistry.register(new client.ui.modules.StudentForumModule());
ModuleRegistry.register(new client.ui.modules.StudentResourceCenterModule());
```

将你的模块以相同方式注册即可。

### 四、装配到 ContentHost 与 SideNav（由 Dashboard 自动完成）

`StudentDashboardUI#initModules` 会遍历 `ModuleRegistry.getAll()`：
- 调用 `initContext(currentUser, connection)` 注入上下文；
- 调用 `contentHost.addPage(m.getKey(), m.getComponent())` 挂载页面；
- 调用 `sideNav.addItem(m.getKey(), m.getDisplayName(), icon)` 在侧栏加入菜单项；
- 默认显示并选中首页键（如 `ModuleKeys.STUDENT_HOME`）。

你无需手动调用 `ContentHost.addPage` 或 `SideNav.addItem`，只需保证模块已注册即可。

### 五、图标与资源放置规范

- 推荐放置在 `resources/icons/` 或类路径下的 `icons/` 目录。
- 文件格式：PNG，建议 16~20px 方形图标。
- `getIconPath()` 支持相对类路径（如 `icons/论坛.png`）或资源目录前缀（如 `resources/icons/论坛.png`）。

### 六、最佳实践

- UI 构造与数据加载解耦：构造期不进行网络 IO；用户操作后按需异步加载。
- 保持 key 全局唯一，统一使用 `ModuleKeys` 常量。
- 避免在 `initContext` 中做耗时；仅存引用。
- 图标不可用时也应保证模块可正常显示（`loadIcon` 会容错）。
- 若模块与角色强相关，按角色区分 key 与模块类（如 `StudentForumModule`、`TeacherForumModule`）。

### 七、故障排查

- 侧栏无菜单：确认已调用 `ModuleRegistry.register(...)` 且 `getDisplayName()` 返回非空。
- 点击无反应：确认 `getKey()` 与 `SideNav`/`ModuleKeys` 一致；`ContentHost` 只有在 `addPage` 成功后才能 `showPage`。
- 图标不显示：检查 `getIconPath()` 路径是否存在于类路径或 `resources/` 目录。
- 页面空白：确认 `getComponent()` 返回的组件已构造并加入了可见子组件。

### 八、参考实现（现有模块）

- `client.ui.modules.StudentHomeModule`
- `client.ui.modules.StudentForumModule`
- `client.ui.modules.StudentResourceCenterModule`

以上模块已完整展示了：实现接口 → 注册 → 自动装配 的流程，建议先参考其最小实现后再扩展业务逻辑。


