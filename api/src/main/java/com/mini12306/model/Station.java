package com.mini12306.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 站点信息
 */
@Data
@Entity
@Table(name = "station")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    // 站点代码，如"BJP"代表北京
    private String code;
    
    // 所属城市
    @Column(name = "city")
    private String city;
    
    // 所属省份
    @Column(name = "province")
    private String province;
    
    // 站点类型：高铁站、普通站等
    private String type;
    
    // 站点地址
    private String address;
    
    // 经度
    private Double longitude;
    
    // 纬度
    private Double latitude;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
