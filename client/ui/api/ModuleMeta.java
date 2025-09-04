package client.ui.api;

/** 模块元信息：用于描述侧栏与排序等属性。 */
public final class ModuleMeta {
    private final String key;
    private final String displayName;
    private final String iconPath;
    private final int order;
    private final boolean defaultShown;

    public ModuleMeta(String key, String displayName, String iconPath, int order, boolean defaultShown) {
        this.key = key;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.order = order;
        this.defaultShown = defaultShown;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getIconPath() { return iconPath; }
    public int getOrder() { return order; }
    public boolean isDefaultShown() { return defaultShown; }
}


