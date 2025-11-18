package com.mini12306.controller;

import com.mini12306.model.Passenger;
import com.mini12306.model.Result;
import com.mini12306.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 乘车人控制器
 */
@RestController
@RequestMapping("/api/passenger")
public class PassengerController {
    
    @Autowired
    private PassengerService passengerService;
    
    /**
     * 添加乘车人
     */
    @PostMapping
    public Result<?> addPassenger(HttpServletRequest request, @RequestBody Passenger passenger) {
        Long userId = (Long) request.getAttribute("userId");
        return passengerService.addPassenger(userId, passenger);
    }
    
    /**
     * 修改乘车人
     */
    @PutMapping("/{id}")
    public Result<?> updatePassenger(HttpServletRequest request, @PathVariable Long id, @RequestBody Passenger passenger) {
        Long userId = (Long) request.getAttribute("userId");
        passenger.setId(id);
        return passengerService.updatePassenger(userId, passenger);
    }
    
    /**
     * 删除乘车人
     */
    @DeleteMapping("/{id}")
    public Result<?> deletePassenger(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return passengerService.deletePassenger(userId, id);
    }
    
    /**
     * 查询乘车人列表
     */
    @GetMapping
    public Result<?> listPassengers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return passengerService.listPassengers(userId);
    }
    
    /**
     * 查询乘车人详情
     */
    @GetMapping("/{id}")
    public Result<?> getPassenger(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return passengerService.getPassenger(userId, id);
    }
}
