package com.mini12306.service.impl;

// import com.mini12306.config.SecurityConfig;
import com.mini12306.dto.AdminLoginRequest;
import com.mini12306.model.AdminUser;
import com.mini12306.model.Result;
import com.mini12306.repository.AdminRepository;
import com.mini12306.service.AdminService;
import com.mini12306.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public Result<?> login(AdminLoginRequest request) {
        // 参数校验
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        
        // 查找管理员
        Optional<AdminUser> adminOpt = adminRepository.findByUsername(username);
        if (!adminOpt.isPresent()) {
            return Result.fail("用户名或密码错误");
        }
        
        AdminUser admin = adminOpt.get();
        
        // 检查账号是否被禁用
        if (admin.getStatus() != 1) {
            return Result.fail("账号已被禁用，请联系系统管理员");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return Result.fail("用户名或密码错误");
        }
        
        // 更新最后登录时间
        admin.setLastLoginTime(new Date());
        adminRepository.save(admin);
        
        // 生成包含用户ID的JWT令牌
        String token = JwtUtil.generateToken(admin.getUsername(), admin.getId(), "admin");
        System.out.println("已生成JWT令牌，包含userId: " + admin.getId());
        
        // 构建标准响应结果，与用户登录格式保持一致
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", admin.getId());  // 使用相同的键名
        data.put("username", admin.getUsername());
        data.put("role", admin.getRole());
        
        return Result.success("登录成功", data);
    }
    
    @Override
    public AdminUser findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public AdminUser findById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }
    
    @Override
    public Result<?> validateToken(String token) {
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String username = (String) claims.get("username");
            
            Optional<AdminUser> adminOpt = adminRepository.findByUsername(username);
            if (!adminOpt.isPresent()) {
                return Result.fail("无效的令牌");
            }
            
            AdminUser admin = adminOpt.get();
            if (admin.getStatus() != 1) {
                return Result.fail("账号已被禁用");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getId());
            data.put("username", admin.getUsername());
            data.put("role", admin.getRole());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("令牌验证失败");
        }
    }
}
