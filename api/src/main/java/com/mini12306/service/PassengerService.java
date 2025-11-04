package com.mini12306.service;

import com.mini12306.model.Passenger;
import com.mini12306.model.Result;

import java.util.List;

/**
 * 乘车人服务接口
 */
public interface PassengerService {
    /**
     * 添加乘车人
     */
    Result<Passenger> addPassenger(Long userId, Passenger passenger);
    
    /**
     * 修改乘车人
     */
    Result<Passenger> updatePassenger(Long userId, Passenger passenger);
    
    /**
     * 删除乘车人
     */
    Result<Void> deletePassenger(Long userId, Long passengerId);
    
    /**
     * 查询用户的所有乘车人
     */
    Result<List<Passenger>> listPassengers(Long userId);
    
    /**
     * 获取乘车人详情
     */
    Result<Passenger> getPassenger(Long userId, Long passengerId);
}
