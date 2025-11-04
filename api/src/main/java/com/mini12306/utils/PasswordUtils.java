package com.mini12306.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类，用于处理密码的加密和验证
 */
public class PasswordUtils {
    
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    /**
     * 对密码进行BCrypt加密
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }
    
    /**
     * 验证密码是否匹配
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 验证密码强度
     * 密码至少包含6位字符
     * 
     * @param password 密码
     * @return 是否合规
     */
    public static boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
}
