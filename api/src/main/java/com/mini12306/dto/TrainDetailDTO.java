package com.mini12306.dto;

import com.mini12306.model.Money;
import com.mini12306.model.Train;
import lombok.Data;

import java.util.Date;
import java.util.List;
/**
 * 列车详情DTO，包含列车、站点、座位等完整信息
 * 用于前端显示列车查询结果
 */
@Data
public class TrainDetailDTO {
    // 列车ID
    private Long id;
    
    // 车次编号
    private String trainNumber;
    
    // 列车类型
    private String type;
    
    // 站点信息
    private String startStation;
    private String endStation;
    
    // 时间信息
    private String departureTime;
    private String arrivalTime;
    private String duration;
    
    // 座位信息
    private List<SeatInfoDTO> seatInfo;
    
    // 列车状态
    private String status;
    
    // 创建时间
    private Date createTime;
    
    // 简单座位信息内部类
    @Data
    public static class SeatInfoDTO {
        private String seatType;
        private Money price;
        private Integer remaining;
        
        public SeatInfoDTO(String seatType, Money price, Integer remaining) {
            this.seatType = seatType;
            this.price = price;
            this.remaining = remaining;
        }
    }
    
    // 从Train对象构造DTO
    public static TrainDetailDTO fromTrain(Train train) {
        TrainDetailDTO dto = new TrainDetailDTO();
        dto.setId(train.getId());
        dto.setTrainNumber(train.getCode()); // 使用code作为trainNumber
        dto.setType(train.getType());
        dto.setStartStation(train.getStartStation());
        dto.setEndStation(train.getEndStation());
        dto.setDepartureTime(train.getStartTime());
        dto.setArrivalTime(train.getEndTime());
        
        // 计算并设置行程时长
        if (train.getDuration() != null) {
            int hours = train.getDuration() / 60;
            int minutes = train.getDuration() % 60;
            String duration = hours > 0 ? 
                String.format("%d小时%d分钟", hours, minutes) : 
                String.format("%d分钟", minutes);
            dto.setDuration(duration);
        }
        
        // 设置状态为"正常"，因为Train实体中可能没有这个字段
        dto.setStatus("正常");
        
        dto.setCreateTime(train.getCreateTime());
        
        return dto;
    }
}
