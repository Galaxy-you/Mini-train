package com.mini12306.controller;

import com.mini12306.dto.AuthRequest;
import com.mini12306.dto.LoginRequest;
import com.mini12306.dto.RegisterRequest;
import com.mini12306.model.Result;
import com.mini12306.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    /**
     * 身份认证
     */
    @PostMapping("/authenticate")
    public Result<?> authenticate(HttpServletRequest request, @RequestBody AuthRequest authRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.authenticate(userId, authRequest);
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getUserInfo(userId);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Result<?> getUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getUserStats(userId);
    }
}
