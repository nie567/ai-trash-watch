package com.example.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * BCrypt 密码加密与强度校验工具类
 */
public class BCryptUtil {

    private static final int LOG_ROUNDS = 10;

    /** 最小长度：防止 "123456" 这类短口令 */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** 弱口令黑名单（小写比较） */
    private static final Set<String> WEAK_PASSWORDS = new HashSet<>(Arrays.asList(
            "12345678", "123456789", "1234567890",
            "password", "passw0rd", "password1",
            "qwerty", "qwerty123", "qwertyuiop",
            "abc12345", "abcd1234", "admin123",
            "11111111", "00000000", "88888888",
            "iloveyou", "welcome1", "letmein1"
    ));

    /** 加密密码 */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /** 验证密码 */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /** 验证密码（别名，供 Service 层调用） */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return verifyPassword(plainPassword, hashedPassword);
    }

    /**
     * 校验密码是否足够强：
     *   1) 长度 ≥ 8；
     *   2) 至少包含字母、数字、符号中的两类；
     *   3) 不在弱口令黑名单中；
     *   4) 不为纯重复字符（如 "aaaaaaaa"）。
     */
    public static boolean isPasswordStrongEnough(String password) {
        return checkStrength(password) == null;
    }

    /**
     * 返回具体的校验失败原因；通过返回 null 表示通过。
     */
    public static String checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "密码长度至少 " + MIN_PASSWORD_LENGTH + " 位";
        }
        if (password.length() > 64) {
            return "密码长度不得超过 64 位";
        }
        if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
            return "密码过于常见，请更换";
        }
        if (isSingleRepeatedChar(password)) {
            return "密码不能全部为同一字符";
        }
        int categories = 0;
        if (password.chars().anyMatch(Character::isLetter)) categories++;
        if (password.chars().anyMatch(Character::isDigit)) categories++;
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) categories++;
        if (categories < 2) {
            return "密码需至少包含字母、数字、符号中的两类";
        }
        return null;
    }

    private static boolean isSingleRepeatedChar(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) return false;
        }
        return true;
    }
}
