package common.protocol;

/**
 * 状态码常量定义
 * 定义系统中使用的各种状态码
 */
public class StatusCode {
    
    // 成功状态码
    public static final int SUCCESS = 200;
    public static final int CREATED = 201;
    
    // 客户端错误状态码
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    
    // 服务器错误状态码
    public static final int INTERNAL_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;
    
    // 业务逻辑状态码
    public static final int USER_NOT_FOUND = 1001;
    public static final int INVALID_PASSWORD = 1002;
    public static final int USER_EXISTS = 1003;
    public static final int ACCOUNT_NOT_ACTIVATED = 1004;
    public static final int PERMISSION_DENIED = 1005;
    
    public static final int COURSE_FULL = 2001;
    public static final int SCHEDULE_CONFLICT = 2002;
    public static final int ALREADY_ENROLLED = 2003;
    public static final int NOT_ENROLLED = 2004;
    public static final int GRADE_UPDATE_FAILED = 2005;
    
    public static final int BOOK_NOT_FOUND = 3001;
    public static final int OUT_OF_STOCK = 3002;
    public static final int BORROW_LIMIT_EXCEEDED = 3003;
    public static final int BOOK_NOT_BORROWED = 3004;
    public static final int OVERDUE_BOOK = 3005;
    
    public static final int PRODUCT_NOT_FOUND = 4001;
    public static final int INSUFFICIENT_STOCK = 4002;
    public static final int INSUFFICIENT_FUNDS = 4003;
    public static final int ORDER_CREATION_FAILED = 4004;
    
    public static final int THREAD_NOT_FOUND = 5001;
    public static final int POST_NOT_FOUND = 5002;
    public static final int CONTENT_DELETED = 5003;
    
    public static final int FILE_NOT_FOUND = 6001;
    public static final int FILE_TOO_LARGE = 6002;
    public static final int UPLOAD_FAILED = 6003;
    public static final int DOWNLOAD_FAILED = 6004;
    public static final int FILE_ACCESS_DENIED = 6005;
    
    private StatusCode() {
        // 工具类，不允许实例化
    }
    
    /**
     * 判断状态码是否表示成功
     * @param statusCode 状态码
     * @return true表示成功，false表示失败
     */
    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * 判断状态码是否表示客户端错误
     * @param statusCode 状态码
     * @return true表示客户端错误，false表示其他
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * 判断状态码是否表示服务器错误
     * @param statusCode 状态码
     * @return true表示服务器错误，false表示其他
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * 根据状态码获取描述信息
     * @param statusCode 状态码
     * @return 状态码描述
     */
    public static String getDescription(int statusCode) {
        switch (statusCode) {
            case SUCCESS: return "成功";
            case CREATED: return "创建成功";
            case BAD_REQUEST: return "请求错误";
            case UNAUTHORIZED: return "未授权";
            case FORBIDDEN: return "禁止访问";
            case NOT_FOUND: return "资源不存在";
            case CONFLICT: return "资源冲突";
            case INTERNAL_ERROR: return "服务器内部错误";
            case SERVICE_UNAVAILABLE: return "服务不可用";
            
            case USER_NOT_FOUND: return "用户不存在";
            case INVALID_PASSWORD: return "密码错误";
            case USER_EXISTS: return "用户已存在";
            case ACCOUNT_NOT_ACTIVATED: return "账户未激活";
            case PERMISSION_DENIED: return "权限不足";
            
            case COURSE_FULL: return "课程已满";
            case SCHEDULE_CONFLICT: return "时间冲突";
            case ALREADY_ENROLLED: return "已选此课程";
            case NOT_ENROLLED: return "未选此课程";
            case GRADE_UPDATE_FAILED: return "成绩更新失败";
            
            case BOOK_NOT_FOUND: return "图书不存在";
            case OUT_OF_STOCK: return "库存不足";
            case BORROW_LIMIT_EXCEEDED: return "超出借阅限制";
            case BOOK_NOT_BORROWED: return "未借阅此书";
            case OVERDUE_BOOK: return "图书逾期";
            
            case PRODUCT_NOT_FOUND: return "商品不存在";
            case INSUFFICIENT_STOCK: return "库存不足";
            case INSUFFICIENT_FUNDS: return "余额不足";
            case ORDER_CREATION_FAILED: return "订单创建失败";
            
            case THREAD_NOT_FOUND: return "主题不存在";
            case POST_NOT_FOUND: return "回复不存在";
            case CONTENT_DELETED: return "内容已删除";
            
            case FILE_NOT_FOUND: return "文件不存在";
            case FILE_TOO_LARGE: return "文件过大";
            case UPLOAD_FAILED: return "上传失败";
            case DOWNLOAD_FAILED: return "下载失败";
            case FILE_ACCESS_DENIED: return "文件访问被拒绝";
            
            default: return "未知状态码: " + statusCode;
        }
    }
}
