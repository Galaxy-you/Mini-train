package com.mini12306.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 改签规则和金额DTO
 */
@Data
public class ChangeRuleDTO {
    // 原票价
    private BigDecimal originalPrice;
    
    // 是否可改签
    private boolean canChange;
    
    // 不可改签原因
    private String reason;
    
    // 改签手续费规则
    private List<String> rules;
    
    // 最低手续费
    private BigDecimal minFee;
}
