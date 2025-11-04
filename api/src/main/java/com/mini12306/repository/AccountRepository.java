package com.mini12306.repository;

import com.mini12306.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    
    // 分页查询相关方法
    Page<Account> findByUsernameContaining(String username, Pageable pageable);
    Page<Account> findByRealNameContaining(String realName, Pageable pageable);
    Page<Account> findByPhoneContaining(String phone, Pageable pageable);
    Page<Account> findByAuthStatus(Integer authStatus, Pageable pageable);
}
