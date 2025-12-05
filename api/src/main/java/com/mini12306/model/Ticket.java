package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 车票
 */
@Data
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_no", nullable = false, unique = true)
    private String ticketNo;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;
    
    @Column(name = "passenger_name")
    private String passengerName;
    
    @Column(name = "passenger_card")
    private String passengerCard;
    
    @Column(name = "train_id", nullable = false)
    private Long trainId;
    
    @Column(name = "train_code")
    private String trainCode;
    
    @Column(name = "train_type")
    private String trainType;
    
    // 发站ID，关联station表
    @Column(name = "start_station_id")
    private Long startStationId;
    
    // 到站ID，关联station表
    @Column(name = "end_station_id")
    private Long endStationId;
    
    // 冗余存储站名，提高查询效率
    @Column(name = "start_station")
    private String startStation;
    
    @Column(name = "end_station")
    private String endStation;
    
    // 冗余存储站点所在城市
    @Column(name = "start_city")
    private String startCity;
    
    @Column(name = "end_city")
    private String endCity;
    
    // 座位类型
    @Column(name = "seat_type")
    private String seatType;
    
    // 车厢号
    @Column(name = "coach")
    private String coach;
    
    // 座位号
    @Column(name = "seat")
    private String seat;
    
    // 完整座位信息：如"12车14A号"
    @Column(name = "seat_info")
    private String seatInfo;
    
    // 票价
    @Column(name = "price")
    @Convert(converter = Money.MoneyConverter.class)
    private Money price;
    
    // 发车日期
    @Column(name = "travel_date")
    @Temporal(TemporalType.DATE)
    private Date travelDate;
    
    // 发车时间
    @Column(name = "start_time")
    private String startTime;
    
    // 到达时间
    @Column(name = "end_time")
    private String endTime;
    
    // 历时(分钟)
    @Column(name = "duration")
    private Integer duration;
    
    // 票状态：0-已取消，1-正常，2-已检票
    private Integer status = 1;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
