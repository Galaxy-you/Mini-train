package com.mini12306.service.impl;

import com.mini12306.model.Passenger;
import com.mini12306.model.Result;
import com.mini12306.repository.PassengerRepository;
import com.mini12306.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 乘车人服务实现
 */
@Service
public class PassengerServiceImpl implements PassengerService {
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Override
    public Result<Passenger> addPassenger(Long userId, Passenger passenger) {
        // 设置用户ID
        passenger.setUserId(userId);
        passenger.setCreateTime(new Date());
        passenger.setUpdateTime(new Date());
        
        // 将idNumber映射到cardId（前端传入的是idNumber）
        if (passenger.getCardId() == null && passenger.getIdNumber() != null) {
            passenger.setCardId(passenger.getIdNumber());
        }
        
        // 处理证件类型
        if (passenger.getIdType() != null) {
            switch (passenger.getIdType()) {
                case "1":
                    passenger.setCardType("身份证");
                    break;
                case "2":
                    passenger.setCardType("护照");
                    break;
                case "3":
                    passenger.setCardType("军官证");
                    break;
                case "4":
                    passenger.setCardType("港澳通行证");
                    break;
                default:
                    passenger.setCardType("身份证");
            }
        }
        
        // 保存乘车人
        passengerRepository.save(passenger);
        
        return Result.success("添加乘车人成功", passenger);
    }
    
    @Override
    public Result<Passenger> updatePassenger(Long userId, Passenger passenger) {
        // 查询乘车人
        Optional<Passenger> passengerOpt = passengerRepository.findById(passenger.getId());
        if (!passengerOpt.isPresent()) {
            return Result.fail("乘车人不存在");
        }
        
        Passenger existingPassenger = passengerOpt.get();
        
        // 验证乘车人是否属于该用户
        if (!existingPassenger.getUserId().equals(userId)) {
            return Result.fail("无权修改该乘车人");
        }
        
        // 将idNumber映射到cardId（前端传入的是idNumber）
        if (passenger.getIdNumber() != null) {
            passenger.setCardId(passenger.getIdNumber());
        }
        
        // 处理证件类型
        if (passenger.getIdType() != null) {
            switch (passenger.getIdType()) {
                case "1":
                    passenger.setCardType("身份证");
                    break;
                case "2":
                    passenger.setCardType("护照");
                    break;
                case "3":
                    passenger.setCardType("军官证");
                    break;
                case "4":
                    passenger.setCardType("港澳通行证");
                    break;
                default:
                    passenger.setCardType("身份证");
            }
        }
        
        // 更新乘车人信息
        existingPassenger.setRealName(passenger.getRealName());
        existingPassenger.setCardId(passenger.getCardId());
        existingPassenger.setCardType(passenger.getCardType());
        existingPassenger.setPhone(passenger.getPhone());
        existingPassenger.setType(passenger.getType());
        existingPassenger.setIsDefault(passenger.getIsDefault());
        existingPassenger.setUpdateTime(new Date());
        
        passengerRepository.save(existingPassenger);
        
        return Result.success("修改乘车人成功", existingPassenger);
    }
    
    @Override
    @Transactional
    public Result<Void> deletePassenger(Long userId, Long passengerId) {
        // 查询乘车人
        Optional<Passenger> passengerOpt = passengerRepository.findById(passengerId);
        if (!passengerOpt.isPresent()) {
            return Result.fail("乘车人不存在");
        }
        
        Passenger passenger = passengerOpt.get();
        
        // 验证乘车人是否属于该用户
        if (!passenger.getUserId().equals(userId)) {
            return Result.fail("无权删除该乘车人");
        }
        
        // 删除乘车人
        passengerRepository.deleteById(passengerId);
        
        return Result.success("删除乘车人成功");
    }
    
    @Override
    public Result<List<Passenger>> listPassengers(Long userId) {
        // 查询用户的所有乘车人
        List<Passenger> passengers = passengerRepository.findByUserId(userId);
        
        return Result.success(passengers);
    }
    
    @Override
    public Result<Passenger> getPassenger(Long userId, Long passengerId) {
        // 查询乘车人
        Optional<Passenger> passengerOpt = passengerRepository.findById(passengerId);
        if (!passengerOpt.isPresent()) {
            return Result.fail("乘车人不存在");
        }
        
        Passenger passenger = passengerOpt.get();
        
        // 验证乘车人是否属于该用户
        if (!passenger.getUserId().equals(userId)) {
            return Result.fail("无权查看该乘车人");
        }
        
        return Result.success(passenger);
    }
}
