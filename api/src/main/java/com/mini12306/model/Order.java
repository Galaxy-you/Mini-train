package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 订单
 */
@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "total_amount")
    private Money totalAmount;
    
    @Transient // 非持久化字段，不存储到数据库
    private Integer ticketCount; // 订单包含的票数
    
    // 订单状态：待支付、已支付、已取消、已完成
    private String status;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "pay_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date payTime;
    
    @Column(name = "cancel_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelTime;
}
