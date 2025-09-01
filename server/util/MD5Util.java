package server.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * 提供密码加密功能
 */
public class MD5Util {
    
    /**
     * 对字符串进行MD5加密
     * @param input 待加密的字符串
     * @return 加密后的32位十六进制字符串
     */
    public static String encrypt(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5算法不可用: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证密码是否匹配
     * @param input 明文密码
     * @param encrypted 加密后的密码
     * @return 匹配返回true，不匹配返回false
     */
    public static boolean verify(String input, String encrypted) {
        if (input == null || encrypted == null) {
            return false;
        }
        
        String inputEncrypted = encrypt(input);
        return encrypted.equals(inputEncrypted);
    }
    
    /**
     * 生成随机盐值
     * @return 8位随机字符串
     */
    public static String generateSalt() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder salt = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            salt.append(chars.charAt(index));
        }
        
        return salt.toString();
    }
    
    /**
     * 使用盐值加密密码
     * @param password 明文密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String encryptWithSalt(String password, String salt) {
        if (password == null || salt == null) {
            return null;
        }
        
        return encrypt(password + salt);
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        String password = "123456";
        String encrypted = encrypt(password);
        System.out.println("原密码: " + password);
        System.out.println("加密后: " + encrypted);
        System.out.println("验证结果: " + verify(password, encrypted));
        
        // 测试盐值加密
        String salt = generateSalt();
        String saltedPassword = encryptWithSalt(password, salt);
        System.out.println("盐值: " + salt);
        System.out.println("加盐加密后: " + saltedPassword);
    }
}
