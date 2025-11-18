package com.mini12306.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成和解析Token
 */
public class JwtUtil {
    
    // 密钥 - 使用至少32字节(256位)的字符串，满足HMAC-SHA256的安全要求
    private static final String SECRET_KEY = "Mini12306_JWT_SECRET_KEY_2025_SECURE_KEY_FOR_JWT_TOKEN_GENERATION_AND_VALIDATION";
    
    // 获取用于签名的密钥对象
    private static Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Token有效期（毫秒）
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 1天
    
    /**
     * 生成JWT Token，包含用户ID
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param role 角色
     * @return JWT Token
     */
    public static String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("role", role);
        return generateToken(claims);
    }
    
    /**
     * 生成JWT Token
     * 
     * @param claims 数据声明
     * @return JWT Token
     */
    public static String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析JWT Token
     * 
     * @param token JWT Token
     * @return 数据声明
     */
    public static Map<String, Object> parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return new HashMap<>(claims);
    }
    
    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Map<String, Object> claims = parseToken(token);
        return (String) claims.get("username");
    }
    
    /**
     * 从Token中获取角色
     * 
     * @param token JWT Token
     * @return 角色
     */
    public static String getRoleFromToken(String token) {
        Map<String, Object> claims = parseToken(token);
        return (String) claims.get("role");
    }
    
    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        try {
            Map<String, Object> claims = parseToken(token);
            // 尝试获取userId，处理可能的类型转换
            Object userIdObj = claims.get("userId");
            
            if (userIdObj == null) {
                System.out.println("JWT令牌中不存在userId字段");
                return null;
            }
            
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            } else if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            
            System.out.println("无法解析的userId类型: " + userIdObj.getClass().getName());
            return null;
        } catch (Exception e) {
            System.out.println("从令牌获取用户ID失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Token是否过期
     * 
     * @param token JWT Token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
