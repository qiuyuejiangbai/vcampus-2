package common.protocol;

/**
 * 消息类型枚举
 * 定义客户端和服务器端之间通信的所有消息类型
 */
public enum MessageType {
    // 用户管理模块
    LOGIN_REQUEST,              // 登录请求
    LOGIN_SUCCESS,              // 登录成功
    LOGIN_FAIL,                 // 登录失败
    REGISTER_REQUEST,           // 注册请求
    REGISTER_SUCCESS,           // 注册成功
    REGISTER_FAIL,              // 注册失败
    LOGOUT_REQUEST,             // 登出请求
    LOGOUT_SUCCESS,             // 登出成功
    UPDATE_USER_REQUEST,        // 更新用户信息请求
    UPDATE_USER_SUCCESS,        // 更新用户信息成功
    UPDATE_USER_FAIL,           // 更新用户信息失败
    GET_USER_INFO_REQUEST,      // 获取用户信息请求
    GET_USER_INFO_SUCCESS,      // 获取用户信息成功
    ADD_USER_REQUEST,           // 添加用户请求
    ADD_USER_SUCCESS,           // 添加用户成功
    DELETE_USER_REQUEST,        // 删除用户请求
    DELETE_USER_SUCCESS,        // 删除用户成功
    GET_ALL_USERS_REQUEST,      // 获取所有用户请求
    GET_ALL_USERS_SUCCESS,      // 获取所有用户成功
    
    // 学籍管理模块
    GET_STUDENT_INFO_REQUEST,   // 获取学生信息请求
    GET_STUDENT_INFO_SUCCESS,   // 获取学生信息成功
    UPDATE_STUDENT_REQUEST,     // 更新学生信息请求
    UPDATE_STUDENT_SUCCESS,     // 更新学生信息成功
    CREATE_STUDENT_REQUEST,     // 创建学生请求
    CREATE_STUDENT_SUCCESS,     // 创建学生成功
    DELETE_STUDENT_REQUEST,     // 删除学生请求
    DELETE_STUDENT_SUCCESS,     // 删除学生成功
    GET_TRANSCRIPT_REQUEST,     // 获取成绩单请求
    GET_TRANSCRIPT_SUCCESS,     // 获取成绩单成功
    GET_ALL_STUDENTS_REQUEST,   // 获取所有学生请求
    GET_ALL_STUDENTS_SUCCESS,   // 获取所有学生成功
    
    // 教师管理模块
    GET_TEACHER_INFO_REQUEST,   // 获取教师信息请求
    GET_TEACHER_INFO_SUCCESS,   // 获取教师信息成功
    GET_TEACHER_INFO_FAIL,      // 获取教师信息失败
    
    // 教务管理模块
    GET_ALL_COURSES_REQUEST,    // 获取所有课程请求
    GET_ALL_COURSES_SUCCESS,    // 获取所有课程成功
    ADD_COURSE_REQUEST,         // 添加课程请求
    ADD_COURSE_SUCCESS,         // 添加课程成功
    UPDATE_COURSE_REQUEST,      // 更新课程请求
    UPDATE_COURSE_SUCCESS,      // 更新课程成功
    DELETE_COURSE_REQUEST,      // 删除课程请求
    DELETE_COURSE_SUCCESS,      // 删除课程成功
    ENROLL_COURSE_REQUEST,      // 选课请求
    ENROLL_COURSE_SUCCESS,      // 选课成功
    ENROLL_COURSE_FAIL,         // 选课失败
    DROP_COURSE_REQUEST,        // 退课请求
    DROP_COURSE_SUCCESS,        // 退课成功
    DROP_COURSE_FAIL,           // 退课失败
    GET_STUDENT_LIST_REQUEST,   // 获取学生名单请求
    GET_STUDENT_LIST_SUCCESS,   // 获取学生名单成功
    UPDATE_GRADE_REQUEST,       // 更新成绩请求
    UPDATE_GRADE_SUCCESS,       // 更新成绩成功
    GET_MY_COURSES_REQUEST,     // 获取我的课程请求
    GET_MY_COURSES_SUCCESS,     // 获取我的课程成功
    GET_ALL_ENROLLMENTS_REQUEST, // 获取所有选课记录请求
    GET_ALL_ENROLLMENTS_SUCCESS, // 获取所有选课记录成功
    GET_STUDENT_ENROLLMENTS_REQUEST, // 获取学生选课记录请求
    GET_STUDENT_ENROLLMENTS_SUCCESS, // 获取学生选课记录成功
    GET_ENROLLMENTS_BY_COURSE_REQUEST, // 获取指定课程的选课记录请求
    GET_ENROLLMENTS_BY_COURSE_SUCCESS, // 获取指定课程的选课记录成功

    // ================= 图书馆模块 =================

    // 搜索图书
    SEARCH_BOOK_REQUEST,          // 搜索图书请求
    SEARCH_BOOK_SUCCESS,          // 搜索图书成功
    SEARCH_BOOK_FAIL,             // 搜索图书失败

    // 获取所有图书
    GET_ALL_BOOKS_REQUEST,        // 获取所有图书请求
    GET_ALL_BOOKS_SUCCESS,        // 获取所有图书成功
    GET_ALL_BOOKS_FAIL,           // 获取所有图书失败

    // 按 ID 获取图书
    GET_BOOK_BY_ID_REQUEST,       // 按 ID 获取图书请求
    GET_BOOK_BY_ID_SUCCESS,       // 按 ID 获取图书成功
    GET_BOOK_BY_ID_FAIL,          // 按 ID 获取图书失败

    // 添加图书
    ADD_BOOK_REQUEST,             // 添加图书请求
    ADD_BOOK_SUCCESS,             // 添加图书成功
    ADD_BOOK_FAIL,                // 添加图书失败

    // 更新图书
    UPDATE_BOOK_REQUEST,          // 更新图书请求
    UPDATE_BOOK_SUCCESS,          // 更新图书成功
    UPDATE_BOOK_FAIL,             // 更新图书失败

    // 删除图书
    DELETE_BOOK_REQUEST,          // 删除图书请求
    DELETE_BOOK_SUCCESS,          // 删除图书成功
    DELETE_BOOK_FAIL,             // 删除图书失败

    // 借书
    BORROW_BOOK_REQUEST,          // 借书请求
    BORROW_BOOK_SUCCESS,          // 借书成功
    BORROW_BOOK_FAIL,             // 借书失败

    // 还书
    RETURN_BOOK_REQUEST,          // 还书请求
    RETURN_BOOK_SUCCESS,          // 还书成功
    RETURN_BOOK_FAIL,             // 还书失败

    // 续借
    RENEW_BOOK_REQUEST,           // 续借请求
    RENEW_BOOK_SUCCESS,           // 续借成功
    RENEW_BOOK_FAIL,              // 续借失败

    // 获取借阅记录
    GET_BORROW_RECORDS_REQUEST,   // 获取借阅记录请求
    GET_BORROW_RECORDS_SUCCESS,   // 获取借阅记录成功
    GET_BORROW_RECORDS_FAIL,     // 获取借阅记录失败

    // 借阅历史搜索
    SEARCH_BORROW_HISTORY_REQUEST,   // 搜索借阅历史请求
    SEARCH_BORROW_HISTORY_SUCCESS,   // 搜索借阅历史成功
    SEARCH_BORROW_HISTORY_FAIL,      // 搜索借阅历史失败

    // 文献检索
    SEARCH_DOCUMENTS_REQUEST,
    SEARCH_DOCUMENTS_RESPONSE,

    GET_DOCUMENT_REQUEST,
    GET_DOCUMENT_RESPONSE,

    DOWNLOAD_DOCUMENT_REQUEST,
    DOWNLOAD_DOCUMENT_RESPONSE,

    UPLOAD_DOCUMENT_REQUEST,
    UPLOAD_DOCUMENT_RESPONSE,

    UPDATE_DOCUMENT_REQUEST,
    UPDATE_DOCUMENT_RESPONSE,

    DELETE_DOCUMENT_REQUEST,
    DELETE_DOCUMENT_RESPONSE,




    // 商店模块
    GET_STORE_ITEMS_REQUEST,    // 获取商品列表请求
    GET_STORE_ITEMS_SUCCESS,    // 获取商品列表成功
    ADD_PRODUCT_REQUEST,        // 添加商品请求
    ADD_PRODUCT_SUCCESS,        // 添加商品成功
    UPDATE_PRODUCT_REQUEST,     // 更新商品请求
    UPDATE_PRODUCT_SUCCESS,     // 更新商品成功
    DELETE_PRODUCT_REQUEST,     // 删除商品请求
    DELETE_PRODUCT_SUCCESS,     // 删除商品成功
    PURCHASE_REQUEST,           // 购买请求
    PURCHASE_SUCCESS,           // 购买成功
    PURCHASE_FAIL,              // 购买失败
    GET_ORDER_HISTORY_REQUEST,  // 获取订单历史请求
    GET_ORDER_HISTORY_SUCCESS,  // 获取订单历史成功
    
    // 论坛模块
    GET_ALL_THREADS_REQUEST,    // 获取所有主题请求
    GET_ALL_THREADS_SUCCESS,    // 获取所有主题成功
    GET_ALL_THREADS_FAIL,       // 获取所有主题失败
    GET_FORUM_SECTIONS_REQUEST, // 获取分区列表请求
    GET_FORUM_SECTIONS_SUCCESS, // 获取分区列表成功
    CREATE_THREAD_REQUEST,      // 创建主题请求
    CREATE_THREAD_SUCCESS,      // 创建主题成功
    DELETE_THREAD_REQUEST,      // 删除主题请求
    DELETE_THREAD_SUCCESS,      // 删除主题成功
    SET_THREAD_ESSENCE_REQUEST, // 设置精华帖请求
    SET_THREAD_ESSENCE_SUCCESS, // 设置精华帖成功
    GET_POSTS_REQUEST,          // 获取回复请求
    GET_POSTS_SUCCESS,          // 获取回复成功
    CREATE_POST_REQUEST,        // 创建回复请求
    CREATE_POST_SUCCESS,        // 创建回复成功
    DELETE_POST_REQUEST,        // 删除回复请求
    DELETE_POST_SUCCESS,        // 删除回复成功
    TOGGLE_THREAD_LIKE_REQUEST, // 切换主题点赞请求
    TOGGLE_THREAD_LIKE_SUCCESS, // 切换主题点赞成功
    TOGGLE_POST_LIKE_REQUEST,   // 切换回复点赞请求
    TOGGLE_POST_LIKE_SUCCESS,   // 切换回复点赞成功
    CREATE_SUB_REPLY_REQUEST,   // 创建子回复请求
    CREATE_SUB_REPLY_SUCCESS,   // 创建子回复成功
    CREATE_QUOTE_REPLY_REQUEST, // 创建引用回复请求
    CREATE_QUOTE_REPLY_SUCCESS, // 创建引用回复成功
    SEARCH_THREADS_REQUEST,     // 搜索帖子请求
    SEARCH_THREADS_SUCCESS,     // 搜索帖子成功
    SEARCH_THREADS_FAIL,        // 搜索帖子失败
    
    // 文件资源模块
    GET_COURSE_FILES_REQUEST,   // 获取课程文件请求
    GET_COURSE_FILES_SUCCESS,   // 获取课程文件成功
    FILE_UPLOAD_REQUEST,        // 文件上传请求
    FILE_UPLOAD_READY,          // 文件上传准备就绪
    FILE_UPLOAD_SUCCESS,        // 文件上传成功
    FILE_UPLOAD_FAIL,           // 文件上传失败
    FILE_DOWNLOAD_REQUEST,      // 文件下载请求
    FILE_DOWNLOAD_READY,        // 文件下载准备就绪
    FILE_DOWNLOAD_SUCCESS,      // 文件下载成功
    FILE_DOWNLOAD_FAIL,         // 文件下载失败
    DELETE_FILE_REQUEST,        // 删除文件请求
    DELETE_FILE_SUCCESS,        // 删除文件成功
    
    // 系统消息
    HEARTBEAT,                  // 心跳消息
    ERROR,                      // 错误消息
    SUCCESS,                    // 成功消息
    PERMISSION_DENIED,          // 权限拒绝
    SERVER_BUSY,                // 服务器繁忙
    INVALID_REQUEST             // 无效请求
}
