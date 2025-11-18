package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 列车
 */
@Data
@Entity
@Table(name = "train")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    // 列车类型：高铁、动车、普快等
    private String type;
    
    // 起始站ID
    @Column(name = "start_station_id")
    private Long startStationId;
    
    // 终点站ID
    @Column(name = "end_station_id")
    private Long endStationId;
    
    // 冗余存储站名，提高查询效率
    @Column(name = "start_station")
    private String startStation;
    
    @Column(name = "end_station")
    private String endStation;
    
    @Column(name = "start_time")
    private String startTime;
    
    @Column(name = "end_time")
    private String endTime;
    
    // 运行时间(分钟)
    @Column(name = "duration")
    private Integer duration;
    
    @Column(name = "seat_count")
    private Integer seatCount;
    
    @Column(name = "price")
    private Money price;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
