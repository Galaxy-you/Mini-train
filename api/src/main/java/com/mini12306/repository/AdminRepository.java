package com.mini12306.repository;

import com.mini12306.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员数据访问层
 */
@Repository
public interface AdminRepository extends JpaRepository<AdminUser, Long> {
    
    /**
     * 根据用户名查找管理员
     * 
     * @param username 用户名
     * @return 管理员用户
     */
    Optional<AdminUser> findByUsername(String username);
}
