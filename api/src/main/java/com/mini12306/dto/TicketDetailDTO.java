package com.mini12306.dto;

import com.mini12306.model.Station;
import com.mini12306.model.Ticket;
import com.mini12306.model.Train;
import com.mini12306.model.Money;
import lombok.Data;

import java.util.Date;

/**
 * 车票详情DTO，包含车票、车次、站点等完整信息
 * 用于前端显示车票信息
 */
@Data
public class TicketDetailDTO {
    // 车票ID
    private Long id;
    
    // 车票编号
    private String ticketNo;
    
    // 订单号
    private String orderNo;
    
    // 乘车人信息
    private String passengerName;
    private String passengerCard;
    
    // 车次信息
    private Long trainId;
    private String trainCode;
    private String trainType;
    
    // 站点信息
    private String startStation;
    private String endStation;
    private String startCity;
    private String endCity;
    
    // 座位信息
    private String seatType;
    private String seatInfo;
    
    // 时间信息
    private Date travelDate;
    private String startTime;
    private String endTime;
    private String duration;
    
    // 票价
    private Money price;
    
    // 车票状态
    private Integer status;
    
    // 创建时间
    private Date createTime;
    
    // 构造方法，从基本实体对象构建DTO
    public TicketDetailDTO(Ticket ticket, Train train, Station startStation, Station endStation) {
        this.id = ticket.getId();
        this.ticketNo = ticket.getTicketNo();
        this.passengerName = ticket.getPassengerName();
        this.passengerCard = ticket.getPassengerCard();
        
        this.trainId = train.getId();
        this.trainCode = train.getCode();
        this.trainType = train.getType();
        
        this.startStation = startStation.getName();
        this.endStation = endStation.getName();
        this.startCity = startStation.getCity();
        this.endCity = endStation.getCity();
        
        this.seatType = ticket.getSeatType();
        this.seatInfo = ticket.getSeatInfo();
        
        this.travelDate = ticket.getTravelDate();
        this.startTime = ticket.getStartTime();
        this.endTime = ticket.getEndTime();
        this.duration = formatDuration(ticket.getDuration());
        
        this.price = ticket.getPrice();
        this.status = ticket.getStatus();
        this.createTime = ticket.getCreateTime();
    }
    
    // 简化构造方法，直接使用车票对象
    public TicketDetailDTO(Ticket ticket) {
        this.id = ticket.getId();
        this.ticketNo = ticket.getTicketNo();
        this.passengerName = ticket.getPassengerName();
        this.passengerCard = ticket.getPassengerCard();
        
        this.trainId = ticket.getTrainId();
        this.trainCode = ticket.getTrainCode();
        this.trainType = ticket.getTrainType();
        
        this.startStation = ticket.getStartStation();
        this.endStation = ticket.getEndStation();
        this.startCity = ticket.getStartCity();
        this.endCity = ticket.getEndCity();
        
        this.seatType = ticket.getSeatType();
        this.seatInfo = ticket.getSeatInfo();
        
        this.travelDate = ticket.getTravelDate();
        this.startTime = ticket.getStartTime();
        this.endTime = ticket.getEndTime();
        this.duration = formatDuration(ticket.getDuration());
        
        this.price = ticket.getPrice();
        this.status = ticket.getStatus();
        this.createTime = ticket.getCreateTime();
    }
    
    // 格式化持续时间
    private String formatDuration(Integer minutes) {
        if (minutes == null) {
            return "未知";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours > 0 ? String.format("%d小时%d分钟", hours, mins) : String.format("%d分钟", mins);
    }
}
