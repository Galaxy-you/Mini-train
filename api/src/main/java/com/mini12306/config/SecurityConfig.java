package com.mini12306.config;

// import com.mini12306.utils.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
// import java.util.UUID;

/**
 * 简单的安全配置，模拟JWT令牌
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    
    // 存储用户令牌 - 在实际项目中应该使用Redis等
    public static final Map<String, Long> TOKEN_USER_MAP = new HashMap<>();
    
    // 存储管理员令牌 - 在实际项目中应该使用Redis等
    public static final Map<String, Long> TOKEN_ADMIN_MAP = new HashMap<>();
    
    // 生成用户令牌 - 使用JWT
    public static String generateToken(Long userId) {
        // 调用JwtUtil生成JWT token，用户角色设为"user"
        return com.mini12306.utils.JwtUtil.generateToken("user_" + userId, userId, "user");
    }
    
    // 生成管理员令牌 
    // public static String generateAdminToken(Long adminId) {
    //     // 不再使用UUID，改用JWT
    //     return com.mini12306.utils.JwtUtil.generateToken("admin_" + adminId, adminId, "admin");
    // }
    
    // 验证用户令牌 - 现在使用JWT验证
    public static Long validateToken(String token) {
        // 如果token以Bearer开头，需要去掉前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return com.mini12306.utils.JwtUtil.getUserIdFromToken(token);
    }
    
    // // 验证管理员令牌
    // public static Long validateAdminToken(String token) {
    //     return TOKEN_ADMIN_MAP.get(token);
    // }
    
    // 用户令牌拦截器
    public static class TokenInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
            // 排除登录和注册接口
            String path = request.getRequestURI();
            if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
                return true;
            }
            
            // 验证令牌
            String token = request.getHeader("Authorization");
            if (token == null) {
                response.setStatus(401);
                return false;
            }
            
            // 使用JWT验证
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除Bearer前缀
            }
            
            // 检查Token是否过期
            if (com.mini12306.utils.JwtUtil.isTokenExpired(token)) {
                response.setStatus(401);
                System.out.println("Token已过期");
                return false;
            }
            
            Long userId = com.mini12306.utils.JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(401);
                return false;
            }
            
            // 将用户ID放入请求属性中
            request.setAttribute("userId", userId);
            return true;
        }
    }
    
    // 管理员令牌拦截器
    public static class AdminTokenInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
            // 排除登录接口
            String path = request.getRequestURI();
            if (path.contains("/api/admin/login")) {
                return true;
            }
            
            // 验证令牌 - 完全与用户拦截器保持一致的处理方式
            String token = request.getHeader("Authorization");
            if (token == null) {
                response.setStatus(401);
                System.out.println("未提供Authorization头");
                return false;
            }
            
            // 从请求头中获取令牌并验证 - 使用正确的JwtUtil类和方法
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除Bearer前缀
            }
            
            // 尝试提取userId
            Long adminId = com.mini12306.utils.JwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                // 如果无法直接获取userId，尝试通过username查找
                String username = com.mini12306.utils.JwtUtil.getUsernameFromToken(token);
                if (username == null) {
                    response.setStatus(401);
                    System.out.println("无效的管理员令牌: 无法提取用户名或ID");
                    return false;
                }
                
                // 验证是否是admin角色
                String role = com.mini12306.utils.JwtUtil.getRoleFromToken(token);
                if (!"admin".equals(role)) {
                    response.setStatus(401);
                    System.out.println("无效的管理员令牌: 非管理员角色");
                    return false;
                }
                
                System.out.println("无法从令牌直接获取userId，但验证了用户名和角色: " + username);
            }
            
            // 将管理员ID添加到请求属性中
            request.setAttribute("adminId", adminId);
            System.out.println("管理员ID: " + adminId);
            return true;
        }
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 添加用户令牌拦截器
        registry.addInterceptor(new TokenInterceptor())
                .addPathPatterns("/api/auth/**", "/api/user/**", "/api/train/**", "/api/ticket/**", "/api/order/**", "/api/passenger/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
                
        // 添加管理员令牌拦截器
        registry.addInterceptor(new AdminTokenInterceptor())
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }
}
