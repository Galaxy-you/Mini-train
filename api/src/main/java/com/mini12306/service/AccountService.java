package com.mini12306.service;

import com.mini12306.dto.AccountDTO;
// import com.mini12306.model.Account;
import com.mini12306.model.Result;
// import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    /**
     * 获取用户总数
     */
    long getUserCount();

    /**
     * 分页查询用户列表
     */
    Result<?> findAll(String keyword, Integer authStatus, Pageable pageable);
    
    /**
     * 根据ID查询用户详情
     */
    Result<AccountDTO> findById(Long id);
    
    /**
     * 更新用户状态
     */
    Result<?> updateAuthStatus(Long id, Integer authStatus);
    
    /**
     * 重置用户密码
     */
    Result<?> resetPassword(Long id);
}
