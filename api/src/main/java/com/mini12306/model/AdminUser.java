package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 管理员账户
 */
@Data
@Entity
@Table(name = "admin_user")
public class AdminUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    @Column(name = "last_login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginTime;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    
    // 状态：0-禁用，1-启用
    private Integer status = 1;
}
