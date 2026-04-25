package com.example.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt 密码加密工具类
 */
public class BCryptUtil {
    
    private static final int LOG_ROUNDS = 10;
    
    /**
     * 加密密码
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }
    
    /**
     * 验证密码
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            // 兼容旧密码（MD5）
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证密码（别名，供 Service 层调用）
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return verifyPassword(plainPassword, hashedPassword);
    }
    
    /**
     * 检查密码强度（至少6位）
     */
    public static boolean isPasswordStrongEnough(String password) {
        return password != null && password.length() >= 6;
    }
}