package com.mini12306.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 退票规则和金额DTO
 */
@Data
public class RefundRuleDTO {
    // 原票价
    private BigDecimal originalPrice;
    
    // 可退金额
    private BigDecimal refundableAmount;
    
    // 手续费
    private BigDecimal fee;
    
    // 退票规则说明
    private List<String> rules;
    
    // 是否可以退票
    private boolean canRefund;
    
    // 不可退票原因
    private String reason;
}
