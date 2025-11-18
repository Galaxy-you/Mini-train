package com.mini12306.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini12306.model.Result;
import com.mini12306.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

/**
 * 管理员权限拦截器
 */
public class AdminInterceptor implements HandlerInterceptor {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 详细的日志记录
        System.out.println("=========== AdminInterceptor 调试信息 ===========");
        System.out.println("请求URI: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("Authorization: " + request.getHeader("Authorization"));

        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            System.out.println("OPTIONS请求，放行");
            return true;
        }
        
        // 排除登录接口
        String uri = request.getRequestURI();
        System.out.println("当前URI: " + uri);
        if (uri.equals("/api/admin/login")) {
            System.out.println("登录接口，放行");
            return true;
        }
        
        // 获取Authorization头
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
            System.out.println("Authorization头无效: " + token);
            handleUnauthorized(response, "未授权，请重新登录");
            return false;
        }
        
        // 验证Token
        token = token.substring(7);
        try {
            System.out.println("验证Token: " + token);
            // 检查Token有效性
            if (JwtUtil.isTokenExpired(token)) {
                System.out.println("Token已过期");
                handleUnauthorized(response, "登录凭证已过期，请重新登录");
                return false;
            }
            
            // 检查角色
            String role = JwtUtil.getRoleFromToken(token);
            System.out.println("Token中的角色: " + role);
            if (!"admin".equals(role)) {
                System.out.println("非管理员角色，拒绝访问");
                handleUnauthorized(response, "没有管理员权限");
                return false;
            }
            
            // 将用户名、用户ID和角色添加到请求属性中，方便后续使用
            request.setAttribute("username", JwtUtil.getUsernameFromToken(token));
            // 添加用户ID到请求属性
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                request.setAttribute("userId", userId);
                System.out.println("从JWT提取的用户ID: " + userId);
            }
            request.setAttribute("role", role);
            
            System.out.println("Token验证通过，用户名: " + request.getAttribute("username") + 
                         ", 用户ID: " + request.getAttribute("userId") + 
                         ", 角色: " + role);
            return true;
        } catch (Exception e) {
            handleUnauthorized(response, "无效的登录凭证");
            System.out.println("Token验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 处理未授权的响应
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<?> result = Result.fail(message);
        String json = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(json);
    }
}
