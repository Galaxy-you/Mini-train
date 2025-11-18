package com.mini12306.dto;

import lombok.Data;

/**
 * 车票改签请求DTO
 */
@Data
public class ChangeTicketRequest {
    // 原车票编号
    private String originalTicketNo;
    
    // 新列车ID
    private Long newTrainId;
    
    // 新座位类型
    private String newSeatType;
    
    // 新的旅行日期 (yyyy-MM-dd)
    private String travelDate;
    
    // 新的发站ID
    private Long startStationId;
    
    // 新的到站ID
    private Long endStationId;
    
    // 支付方式: 1-微信, 2-支付宝, 3-银行卡
    private Integer payMethod;
}
