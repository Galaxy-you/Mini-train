package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 乘车人
 */
@Data
@Entity
@Table(name = "passenger")
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 接收前端传过来的idNumber
    @Transient
    private String idNumber;
    
    // 接收前端传过来的证件类型
    @Transient
    private String idType;
    
    @Column(name = "real_name", nullable = false)
    private String realName;
    
    @Column(name = "card_id", nullable = false)
    private String cardId;
    
    @Column(name = "card_type")
    private String cardType = "身份证";
    
    private String phone;
    
    // 乘客类型: 成人、儿童、学生等
    private String type = "成人";
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
