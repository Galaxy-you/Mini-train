package com.mini12306.service;

import com.mini12306.dto.AdminLoginRequest;
import com.mini12306.model.AdminUser;
import com.mini12306.model.Result;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求
     * @return 登录结果
     */
    Result<?> login(AdminLoginRequest request);
    
    /**
     * 根据用户名查找管理员
     * 
     * @param username 用户名
     * @return 管理员
     */
    AdminUser findByUsername(String username);
    
    /**
     * 根据ID查找管理员
     * 
     * @param id 管理员ID
     * @return 管理员
     */
    AdminUser findById(Long id);
    
    /**
     * 验证Token
     * 
     * @param token JWT Token
     * @return 验证结果
     */
    Result<?> validateToken(String token);
}
