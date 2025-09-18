package client.ui.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 * 提供各种数据格式验证功能
 */
public class ValidationUtil {
    
    // 手机号正则表达式 - 支持中国手机号格式
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$|^\\+?86\\s?1[3-9]\\d{9}$|^\\+?\\d{1,4}[-\\s]?\\d{1,14}$"
    );
    
    // 邮箱正则表达式 - 更严格的邮箱格式验证
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // 学号正则表达式 - 通常为数字或字母数字组合
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]{6,20}$"
    );
    
    // 工号正则表达式 - 通常为数字或字母数字组合
    private static final Pattern TEACHER_NO_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]{4,20}$"
    );
    
    // 姓名正则表达式 - 支持中文、英文、少数民族姓名
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[\\u4e00-\\u9fa5a-zA-Z\\s·]{2,20}$"
    );
    
    // 日期格式正则表达式 - yyyy-MM-dd
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "^(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
    );
    
    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // 允许为空
        }
        
        String cleanPhone = phone.trim();
        
        // 移除所有空格、连字符
        String digitsOnly = cleanPhone.replaceAll("[\\s\\-]", "");
        
        // 检查是否只包含数字、+号
        if (!digitsOnly.matches("^\\+?\\d+$")) {
            return false;
        }
        
        // 检查长度（7-15位数字）
        String phoneDigits = digitsOnly.replaceAll("^\\+", "");
        if (phoneDigits.length() < 7 || phoneDigits.length() > 15) {
            return false;
        }
        
        // 使用正则表达式进行最终验证
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // 允许为空
        }
        
        String cleanEmail = email.trim().toLowerCase();
        
        // 基本格式检查
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return false;
        }
        
        // 检查@符号数量
        if (cleanEmail.indexOf("@") != cleanEmail.lastIndexOf("@")) {
            return false;
        }
        
        // 检查域名部分
        String[] parts = cleanEmail.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // 本地部分不能为空或过长
        if (localPart.isEmpty() || localPart.length() > 64) {
            return false;
        }
        
        // 域名部分不能为空
        if (domainPart.isEmpty()) {
            return false;
        }
        
        // 使用正则表达式进行最终验证
        return EMAIL_PATTERN.matcher(cleanEmail).matches();
    }
    
    /**
     * 验证学号格式
     * @param studentNo 学号
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidStudentNo(String studentNo) {
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return false; // 学号不能为空
        }
        
        return STUDENT_NO_PATTERN.matcher(studentNo.trim()).matches();
    }
    
    /**
     * 验证工号格式
     * @param teacherNo 工号
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidTeacherNo(String teacherNo) {
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            return false; // 工号不能为空
        }
        
        return TEACHER_NO_PATTERN.matcher(teacherNo.trim()).matches();
    }
    
    /**
     * 验证姓名格式
     * @param name 姓名
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false; // 姓名不能为空
        }
        
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * 验证日期格式
     * @param dateStr 日期字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return true; // 允许为空
        }
        
        return DATE_PATTERN.matcher(dateStr.trim()).matches();
    }
    
    /**
     * 验证年份格式
     * @param yearStr 年份字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidYear(String yearStr) {
        if (yearStr == null || yearStr.trim().isEmpty()) {
            return true; // 允许为空
        }
        
        try {
            int year = Integer.parseInt(yearStr.trim());
            return year >= 1900 && year <= 2100; // 合理的年份范围
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 获取手机号验证错误信息
     * @param phone 手机号
     * @return 错误信息，null表示验证通过
     */
    public static String getPhoneValidationError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // 允许为空
        }
        
        if (!isValidPhone(phone)) {
            return "手机号格式不正确，请输入有效的手机号码（支持中国手机号和国际格式）";
        }
        
        return null;
    }
    
    /**
     * 获取邮箱验证错误信息
     * @param email 邮箱
     * @return 错误信息，null表示验证通过
     */
    public static String getEmailValidationError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null; // 允许为空
        }
        
        if (!isValidEmail(email)) {
            return "邮箱格式不正确，请输入有效的邮箱地址（格式：user@domain.com）";
        }
        
        return null;
    }
    
    /**
     * 获取学号验证错误信息
     * @param studentNo 学号
     * @return 错误信息，null表示验证通过
     */
    public static String getStudentNoValidationError(String studentNo) {
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return "学号不能为空";
        }
        
        if (!isValidStudentNo(studentNo)) {
            return "学号格式不正确，请输入6-20位字母数字组合";
        }
        
        return null;
    }
    
    /**
     * 获取工号验证错误信息
     * @param teacherNo 工号
     * @return 错误信息，null表示验证通过
     */
    public static String getTeacherNoValidationError(String teacherNo) {
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            return "工号不能为空";
        }
        
        if (!isValidTeacherNo(teacherNo)) {
            return "工号格式不正确，请输入4-20位字母数字组合";
        }
        
        return null;
    }
    
    /**
     * 获取姓名验证错误信息
     * @param name 姓名
     * @return 错误信息，null表示验证通过
     */
    public static String getNameValidationError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "姓名不能为空";
        }
        
        if (!isValidName(name)) {
            return "姓名格式不正确，请输入2-20位中英文字符";
        }
        
        return null;
    }
    
    /**
     * 获取日期验证错误信息
     * @param dateStr 日期字符串
     * @return 错误信息，null表示验证通过
     */
    public static String getDateValidationError(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null; // 允许为空
        }
        
        if (!isValidDateFormat(dateStr)) {
            return "日期格式不正确，请输入正确的日期格式（yyyy-MM-dd）";
        }
        
        return null;
    }
    
    /**
     * 获取年份验证错误信息
     * @param yearStr 年份字符串
     * @return 错误信息，null表示验证通过
     */
    public static String getYearValidationError(String yearStr) {
        if (yearStr == null || yearStr.trim().isEmpty()) {
            return null; // 允许为空
        }
        
        if (!isValidYear(yearStr)) {
            return "年份格式不正确，请输入1900-2100之间的有效年份";
        }
        
        return null;
    }
    
    /**
     * 清理手机号格式
     * @param phone 原始手机号
     * @return 清理后的手机号
     */
    public static String cleanPhoneNumber(String phone) {
        if (phone == null) return "";
        
        // 移除所有空格和连字符
        return phone.replaceAll("[\\s\\-]", "");
    }
    
    /**
     * 清理邮箱格式
     * @param email 原始邮箱
     * @return 清理后的邮箱
     */
    public static String cleanEmail(String email) {
        if (email == null) return "";
        
        return email.trim().toLowerCase();
    }
}
