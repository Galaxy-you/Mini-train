package com.mini12306.controller;

import com.mini12306.model.Result;
import com.mini12306.service.*;
import com.mini12306.dto.*;
import com.mini12306.model.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * 管理员控制器
 * 提供管理后台所需的各种接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private TrainService trainService;
    
    // @Autowired
    // private OrderService orderService;
    
    @Autowired
    private TicketService ticketService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private StatsService statsService;
    
    @Autowired
    private TrainRouteService trainRouteService;

    @Autowired
    private TrainScheduleService trainScheduleService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody AdminLoginRequest request) {
        return adminService.login(request);
    }
    
    /**
     * 验证管理员Token
     */
    @GetMapping("/validate-token")
    public Result<?> validateToken(HttpServletRequest request) {
        AdminUser admin = (AdminUser) request.getAttribute("admin");
        return Result.success(admin);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<?> getAdminInfo(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return adminService.validateToken(token.substring(7));
        }
        return Result.fail("未授权，请重新登录");
    }
    
    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats/system")
    public Result<?> getSystemStats() {
        return Result.success(statsService.getSystemStats());
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/stats/order") 
    public Result<?> getOrderStats() {
        return Result.success(statsService.getOrderStats());
    }

    /**
     * 获取热门车次统计数据
     */
    @GetMapping("/stats/popular")
    public Result<?> getPopularTrainStats() {
        return Result.success(statsService.getPopularTrainStats());
    }
    
    /**
     * 站点管理 - 获取站点列表
     */
    @GetMapping("/station")
    public Result<?> listStations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city) {
        return trainService.listStations(name, city, 
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }
    
    /**
     * 站点管理 - 添加站点
     */
    @PostMapping("/station")
    public Result<?> addStation(@RequestBody Map<String, Object> station) {
        return trainService.addStation(station);
    }
    
    /**
     * 站点管理 - 更新站点
     */
    @PutMapping("/station/{id}")
    public Result<?> updateStation(@PathVariable Long id, @RequestBody Map<String, Object> station) {
        return trainService.updateStation(id, station);
    }
    
    /**
     * 站点管理 - 删除站点
     */
    @DeleteMapping("/station/{id}")
    public Result<?> deleteStation(@PathVariable Long id) {
        return trainService.deleteStation(id);
    }
    
    /**
     * 车次管理 - 获取车次列表
     */
    @GetMapping("/train")
    public Result<?> listTrains(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trainNo,
            @RequestParam(required = false) String type) {
        return trainService.listTrains(trainNo, type,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }
    
    /**
     * 车次管理 - 获取车次详情
     */
    @GetMapping("/train/{id}")
    public Result<?> getTrainDetail(@PathVariable Long id) {
        return trainService.getTrainDetail(id);
    }
    
    /**
     * 车次管理 - 添加车次
     */
    @PostMapping("/train")
    public Result<?> addTrain(@RequestBody Map<String, Object> train) {
        return trainService.addTrain(train);
    }
    
    /**
     * 车次管理 - 更新车次
     */
    @PutMapping("/train/{id}")
    public Result<?> updateTrain(@PathVariable Long id, @RequestBody Map<String, Object> train) {
        return trainService.updateTrain(id, train);
    }
    
    /**
     * 车次管理 - 删除车次
     */
    @DeleteMapping("/train/{id}")
    public Result<?> deleteTrain(@PathVariable Long id) {
        return trainService.deleteTrain(id);
    }
    
    /**
     * 列车路线管理 - 获取列车路线列表（对应train_route表）
     */
    @GetMapping("/train-route")
    public Result<?> listTrainRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long trainId,
            @RequestParam(required = false) Long stationId) {
        return trainRouteService.listTrainRoutes(trainId, stationId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "stationOrder")));
    }

    /**
     * 列车路线管理 - 根据列车ID获取完整路线
     */
    @GetMapping("/train-route/train/{trainId}")
    public Result<?> getTrainRoutesByTrainId(@PathVariable Long trainId) {
        return trainRouteService.getTrainRoutesByTrainId(trainId);
    }
    
    /**
     * 列车路线管理 - 获取路线详情
     */
    @GetMapping("/train-route/{id}")
    public Result<?> getTrainRouteDetail(@PathVariable Long id) {
        return trainRouteService.getTrainRouteDetail(id);
    }
    
    /**
     * 列车路线管理 - 添加列车路线
     */
    @PostMapping("/train-route")
    public Result<?> addTrainRoute(@RequestBody Map<String, Object> route) {
        return trainRouteService.addTrainRoute(route);
    }
    
    /**
     * 列车路线管理 - 批量添加列车路线
     */
    @PostMapping("/train-route/batch")
    public Result<?> batchAddTrainRoutes(@RequestBody Map<String, Object> data) {
        Long trainId = Long.valueOf(data.get("trainId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> routes = (List<Map<String, Object>>) data.get("routes");
        return trainRouteService.batchAddTrainRoutes(trainId, routes);
    }
    
    /**
     * 列车路线管理 - 更新列车路线
     */
    @PutMapping("/train-route/{id}")
    public Result<?> updateTrainRoute(@PathVariable Long id, @RequestBody Map<String, Object> route) {
        return trainRouteService.updateTrainRoute(id, route);
    }
    
    /**
     * 列车路线管理 - 删除列车路线
     */
    @DeleteMapping("/train-route/{id}")
    public Result<?> deleteTrainRoute(@PathVariable Long id) {
        return trainRouteService.deleteTrainRoute(id);
    }
    
    /**
     * 车次日程管理 - 获取车次日程列表（对应train_schedule表）
     */
    @GetMapping("/train-schedule")
    public Result<?> listTrainSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long trainId,
            @RequestParam(required = false) String travelDate,
            @RequestParam(required = false) Integer status) {
        return trainScheduleService.listTrainSchedules(trainId, travelDate, status,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "travelDate")));
    }

    /**
     * 车次日程管理 - 根据列车ID获取所有日程
     */
    @GetMapping("/train-schedule/train/{trainId}")
    public Result<?> getTrainSchedulesByTrainId(@PathVariable Long trainId) {
        return trainScheduleService.getTrainSchedulesByTrainId(trainId);
    }
    
    /**
     * 车次日程管理 - 获取日程详情
     */
    @GetMapping("/train-schedule/{id}")
    public Result<?> getTrainScheduleDetail(@PathVariable Long id) {
        return trainScheduleService.getTrainScheduleDetail(id);
    }
    
    /**
     * 车次日程管理 - 添加车次日程
     */
    @PostMapping("/train-schedule")
    public Result<?> addTrainSchedule(@RequestBody Map<String, Object> schedule) {
        return trainScheduleService.addTrainSchedule(schedule);
    }
    
    /**
     * 车次日程管理 - 批量添加车次日程
     */
    @PostMapping("/train-schedule/batch")
    public Result<?> batchAddTrainSchedules(@RequestBody Map<String, Object> data) {
        Long trainId = Long.valueOf(data.get("trainId").toString());
        @SuppressWarnings("unchecked")
        List<String> travelDates = (List<String>) data.get("travelDates");
        return trainScheduleService.batchAddTrainSchedules(trainId, travelDates);
    }
    
    /**
     * 车次日程管理 - 更新车次日程
     */
    @PutMapping("/train-schedule/{id}")
    public Result<?> updateTrainSchedule(@PathVariable Long id, @RequestBody Map<String, Object> schedule) {
        return trainScheduleService.updateTrainSchedule(id, schedule);
    }
    
    /**
     * 车次日程管理 - 删除车次日程
     */
    @DeleteMapping("/train-schedule/{id}")
    public Result<?> deleteTrainSchedule(@PathVariable Long id) {
        return trainScheduleService.deleteTrainSchedule(id);
    }
    
    /**
     * 车票管理 - 获取车票列表
     */
    @GetMapping("/ticket")
    public Result<?> listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String ticketNo,
            @RequestParam(required = false) String passengerName,
            @RequestParam(required = false) String trainNo,
            @RequestParam(required = false) String status) {
        return ticketService.listTickets(ticketNo, passengerName, trainNo, status,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }
    
    /**
     * 车票管理 - 获取车票详情
     */
    @GetMapping("/ticket/{id}")
    public Result<?> getTicketDetail(@PathVariable Long id) {
        return ticketService.getTicketDetail(String.valueOf(id));
    }
    
    /**
     * 车票管理 - 更新车票信息
     */
    @PutMapping("/ticket/{id}")
    public Result<?> updateTicket(@PathVariable Long id, @RequestBody Map<String, Object> ticket) {
        return ticketService.updateTicket(id, ticket);
    }
    
    /**
     * 车票管理 - 取消车票
     */
    @PostMapping("/ticket/{id}/cancel")
    public Result<?> cancelTicket(@PathVariable Long id) {
        return ticketService.cancelTicket(id);
    }
    
    /**
     * 用户管理 - 获取用户列表
     */
    @GetMapping("/user")
    public Result<?> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer authStatus) {
        return accountService.findAll(keyword, authStatus, 
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }

    /**
     * 用户管理 - 获取用户详情
     */
    @GetMapping("/user/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        return accountService.findById(id);
    }

    /**
     * 用户管理 - 更新用户认证状态
     */
    @PutMapping("/user/{id}/auth-status")
    public Result<?> updateAuthStatus(
            @PathVariable Long id,
            @RequestParam Integer authStatus) {
        return accountService.updateAuthStatus(id, authStatus);
    }

    /**
     * 用户管理 - 重置用户密码
     */
    @PostMapping("/user/{id}/reset-password")
    public Result<?> resetUserPassword(@PathVariable Long id) {
        return accountService.resetPassword(id);
    }
}
