package com.mini12306.service;

import com.mini12306.dto.AuthRequest;
import com.mini12306.dto.LoginRequest;
import com.mini12306.dto.LoginResponse;
import com.mini12306.dto.RegisterRequest;
import com.mini12306.model.Account;
import com.mini12306.model.Result;

/**
 * 用户认证服务接口
 */
public interface AuthService {
    /**
     * 用户注册
     */
    Result<Account> register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    Result<LoginResponse> login(LoginRequest request);
    
    /**
     * 用户身份认证
     */
    Result<Account> authenticate(Long userId, AuthRequest request);
    
    /**
     * 查询用户信息
     */
    Result<Account> getUserInfo(Long userId);
}
