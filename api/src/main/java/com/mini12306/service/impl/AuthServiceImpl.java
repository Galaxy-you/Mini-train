package com.mini12306.service.impl;

import com.mini12306.config.SecurityConfig;
import com.mini12306.dto.AuthRequest;
import com.mini12306.dto.LoginRequest;
import com.mini12306.dto.LoginResponse;
import com.mini12306.dto.RegisterRequest;
import com.mini12306.model.Account;
import com.mini12306.model.Result;
import com.mini12306.repository.AccountRepository;
import com.mini12306.repository.OrderRepository;
import com.mini12306.repository.TicketRepository;
import com.mini12306.repository.PassengerRepository;
import com.mini12306.service.AuthService;
import com.mini12306.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public Result<Account> register(RegisterRequest request) {
        // 验证用户名是否已存在
        if (accountRepository.existsByUsername(request.getUsername())) {
            return Result.fail("用户名已存在");
        }

        // 验证密码强度
        if (!PasswordUtils.validatePasswordStrength(request.getPassword())) {
            return Result.fail("密码不能少于6位");
        }

        // 创建用户
        Account account = new Account();
        account.setUsername(request.getUsername());

        // 使用BCrypt加密密码
        String encryptedPassword = PasswordUtils.encrypt(request.getPassword());
        account.setPassword(encryptedPassword);

        account.setPhone(request.getPhone());
        account.setCreateTime(new Date());
        account.setUpdateTime(new Date());

        // 如果提供了真实姓名和身份证号，自动完成身份认证
        if (request.getRealName() != null && !request.getRealName().trim().isEmpty()
                && request.getCardId() != null && !request.getCardId().trim().isEmpty()) {
            // 验证身份证格式
            String cardId = request.getCardId().trim();
            if (!validateIdCard(cardId)) {
                return Result.fail("身份证号格式不正确");
            }

            account.setRealName(request.getRealName().trim());
            account.setCardId(cardId);
            account.setAuthStatus(1); // 已认证
        } else {
            account.setAuthStatus(0); // 未认证
        }

        accountRepository.save(account);

        return Result.success("注册成功", account);
    }

    /**
     * 验证身份证号格式
     */
    private boolean validateIdCard(String idCard) {
        // 简单校验：15位或18位，18位最后一位可以是X
        return idCard.matches("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");
    }

    @Override
    public Result<LoginResponse> login(LoginRequest request) {
        // 参数校验
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        // 查询用户
        Optional<Account> accountOpt = accountRepository.findByUsername(request.getUsername());
        if (!accountOpt.isPresent()) {
            return Result.fail("用户名或密码错误");
        }

        Account account = accountOpt.get();

        // 使用BCrypt验证密码
        if (!PasswordUtils.matches(request.getPassword(), account.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        // 生成令牌
        String token = SecurityConfig.generateToken(account.getId());

        // 构造响应
        LoginResponse response = new LoginResponse();
        response.setUserId(account.getId());
        response.setUsername(account.getUsername());
        response.setToken(token);
        response.setAuthStatus(account.getAuthStatus());

        return Result.success("登录成功", response);
    }

    @Override
    public Result<Account> authenticate(Long userId, AuthRequest request) {
        // 查询用户
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if (!accountOpt.isPresent()) {
            return Result.fail("用户不存在");
        }

        Account account = accountOpt.get();

        // 更新身份信息
        account.setRealName(request.getRealName());
        account.setCardId(request.getCardId());
        account.setAuthStatus(1); // 已认证
        account.setUpdateTime(new Date());

        accountRepository.save(account);

        return Result.success("身份认证成功", account);
    }

    @Override
    public Result<Account> getUserInfo(Long userId) {
        // 查询用户
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if (!accountOpt.isPresent()) {
            return Result.fail("用户不存在");
        }

        return Result.success(accountOpt.get());
    }

    @Override
    public Result<?> getUserStats(Long userId) {
        // 查询用户是否存在
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if (!accountOpt.isPresent()) {
            return Result.fail("用户不存在");
        }

        Map<String, Object> stats = new HashMap<>();

        // 统计该用户的订单数量
        long orderCount = orderRepository.countByUserId(userId);
        stats.put("orderCount", orderCount);

        // 统计该用户的车票数量
        long ticketCount = ticketRepository.countByUserId(userId);
        stats.put("ticketCount", ticketCount);

        // 统计该用户的常用乘车人数量
        long passengerCount = passengerRepository.countByUserId(userId);
        stats.put("passengerCount", passengerCount);

        return Result.success(stats);
    }
}

