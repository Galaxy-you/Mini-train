package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 用户账户
 */
@Data
@Entity
@Table(name = "user_account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String realName;
    
    private String cardId;
    
    private String phone;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    
    // 身份验证状态：0-未验证，1-已验证
    private Integer authStatus = 0;
}
