package client.ui.integration;

/** 统一模块 Key 常量，避免拼写不一致。 */
public final class ModuleKeys {
    private ModuleKeys() {}

    // 兼容保留：HOME 旧键（尽量不再使用）
    public static final String HOME = "home";
    // 新增区分的首页键
    public static final String STUDENT_HOME = "student_home";
    public static final String TEACHER_HOME = "teacher_home";
    public static final String ADMIN_HOME = "admin_home";
    public static final String COURSE = "course";
    public static final String TIMETABLE = "timetable";
    public static final String GRADE = "grade";
    public static final String NOTICE = "notice";
    public static final String ACTIVITY = "activity";
    public static final String PROFILE = "profile";
    public static final String SETTINGS = "settings";
    public static final String LOGOUT = "logout";

    // 论坛/资源中心按角色区分
    public static final String FORUM = "forum"; // 旧键，兼容保留
    public static final String STUDENT_FORUM = "student_forum";
    public static final String TEACHER_FORUM = "teacher_forum";
    public static final String ADMIN_FORUM = "admin_forum";

    public static final String RESOURCE_CENTER = "resource_center"; // 旧键，兼容保留
    public static final String STUDENT_RESOURCE_CENTER = "student_resource_center";
    public static final String TEACHER_RESOURCE_CENTER = "teacher_resource_center";
    public static final String ADMIN_RESOURCE_CENTER = "admin_resource_center";
}


