package com.mini12306.dto;

import lombok.Data;
import java.util.List;

/**
 * 购票请求DTO
 */
@Data
public class BuyTicketRequest {
    private Long trainId;
    private String startStation;
    private String endStation;
    private Long startStationId;
    private Long endStationId;
    private String startCity;
    private String endCity;
    private List<Long> passengerIds;
    private String seatType;
}
