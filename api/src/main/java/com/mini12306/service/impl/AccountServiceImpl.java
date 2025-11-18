package com.mini12306.service.impl;

import com.mini12306.dto.AccountDTO;
import com.mini12306.model.Account;
import com.mini12306.model.Result;
import com.mini12306.repository.AccountRepository;
import com.mini12306.service.AccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long getUserCount() {
        return accountRepository.count();
    }

    @Override
    public Result<?> findAll(String keyword, Integer authStatus, Pageable pageable) {
        // 构建动态查询条件
        Specification<Account> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键字搜索(用户名、真实姓名、手机号)
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.or(
                    cb.like(root.get("username"), "%" + keyword + "%"),
                    cb.like(root.get("realName"), "%" + keyword + "%"),
                    cb.like(root.get("phone"), "%" + keyword + "%")
                ));
            }
            
            // 认证状态过滤
            if (authStatus != null) {
                predicates.add(cb.equal(root.get("authStatus"), authStatus));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行分页查询
        Page<AccountDTO> page = accountRepository.findAll(spec, pageable).map(account -> {
            AccountDTO dto = new AccountDTO();
            BeanUtils.copyProperties(account, dto);
            return dto;
        });
        
        return Result.success(page);
    }

    @Override
    public Result<AccountDTO> findById(Long id) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return Result.fail("用户不存在");
        }
        
        AccountDTO dto = new AccountDTO();
        BeanUtils.copyProperties(accountOpt.get(), dto);
        return Result.success("获取用户信息成功", dto);
    }

    @Override
    @Transactional
    public Result<?> updateAuthStatus(Long id, Integer authStatus) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return Result.fail("用户不存在");
        }
        
        Account account = accountOpt.get();
        account.setAuthStatus(authStatus);
        accountRepository.save(account);
        return Result.success("更新认证状态成功");
    }

    @Override
    @Transactional
    public Result<?> resetPassword(Long id) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return Result.fail("用户不存在");
        }
        
        Account account = accountOpt.get();
        // 重置为默认密码 123456
        account.setPassword(passwordEncoder.encode("123456"));
        accountRepository.save(account);
        return Result.success("密码重置成功");
    }
}
