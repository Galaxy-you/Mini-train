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
     * 线路管理 - 获取线路列表
     */
    @GetMapping("/route")
    public Result<?> listRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startStation,
            @RequestParam(required = false) String endStation) {
        // 应该调用服务层方法获取数据，这里使用模拟数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 模拟分页数据
        int start = page * size;
        int total = 50;
        int count = Math.min(size, total - start);
        
        if (start < total) {
            for (int i = 0; i < count; i++) {
                Map<String, Object> route = new HashMap<>();
                route.put("id", start + i + 1);
                route.put("name", "线路" + (start + i + 1));
                route.put("startStation", "北京");
                route.put("endStation", "上海");
                route.put("distance", 1300);
                route.put("stationCount", 4);
                route.put("createTime", "2025-01-01 00:00:00");
                route.put("updateTime", "2025-01-01 00:00:00");
                content.add(route);
            }
        }
        
        result.put("content", content);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("size", size);
        result.put("number", page);
        
        return Result.success("获取线路列表成功", result);
    }
    
    /**
     * 线路管理 - 获取线路详情
     */
    @GetMapping("/route/{id}")
    public Result<?> getRouteDetail(@PathVariable Long id) {
        // 这里应该调用服务层方法获取线路详情
        Map<String, Object> route = new HashMap<>();
        route.put("id", id);
        route.put("name", "北京-上海");
        route.put("startStation", "北京");
        route.put("endStation", "上海");
        route.put("startStationId", 1);
        route.put("endStationId", 4);
        route.put("distance", 1300);
        route.put("stationCount", 4);
        
        // 经过的站点
        List<Map<String, Object>> stations = new ArrayList<>();
        stations.add(createRouteStation(1, 1, "北京", 0));
        stations.add(createRouteStation(2, 2, "济南", 400));
        stations.add(createRouteStation(3, 3, "南京", 900));
        stations.add(createRouteStation(4, 4, "上海", 1300));
        
        route.put("stations", stations);
        
        return Result.success("获取线路详情成功", route);
    }
    
    private Map<String, Object> createRouteStation(int order, long stationId, String stationName, int distance) {
        Map<String, Object> station = new HashMap<>();
        station.put("order", order);
        station.put("stationId", stationId);
        station.put("stationName", stationName);
        station.put("distance", distance);
        return station;
    }
    
    /**
     * 线路管理 - 添加线路
     */
    @PostMapping("/route")
    public Result<?> addRoute(@RequestBody Map<String, Object> route) {
        // 这里应该调用服务层方法保存数据
        route.put("id", System.currentTimeMillis());
        route.put("createTime", new Date());
        route.put("updateTime", new Date());
        return Result.success("添加线路成功", route);
    }
    
    /**
     * 线路管理 - 更新线路
     */
    @PutMapping("/route/{id}")
    public Result<?> updateRoute(@PathVariable Long id, @RequestBody Map<String, Object> route) {
        // 更新线路信息
        route.put("id", id);
        route.put("updateTime", new Date());
        return Result.success("更新线路成功", route);
    }
    
    /**
     * 线路管理 - 删除线路
     */
    @DeleteMapping("/route/{id}")
    public Result<?> deleteRoute(@PathVariable Long id) {
        // 删除线路
        return Result.success("删除线路成功");
    }
    
    /**
     * 时刻表管理 - 获取时刻表列表
     */
    @GetMapping("/schedule")
    public Result<?> listSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trainNo,
            @RequestParam(required = false) String date) {
        // 应该调用服务层方法获取数据，这里使用模拟数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 模拟分页数据
        int start = page * size;
        int total = 200;
        int count = Math.min(size, total - start);
        
        if (start < total) {
            for (int i = 0; i < count; i++) {
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("id", start + i + 1);
                schedule.put("trainNo", "G" + (1000 + (start + i) % 20));
                schedule.put("date", "2025-06-" + String.format("%02d", ((start + i) % 30) + 1));
                schedule.put("startStation", "北京");
                schedule.put("endStation", "上海");
                schedule.put("startTime", "08:00");
                schedule.put("endTime", "14:00");
                schedule.put("duration", "6小时");
                schedule.put("status", (start + i) % 5 == 0 ? "已取消" : "正常");
                schedule.put("createTime", "2025-01-01 00:00:00");
                content.add(schedule);
            }
        }
        
        result.put("content", content);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("size", size);
        result.put("number", page);
        
        return Result.success("获取时刻表列表成功", result);
    }
    
    /**
     * 时刻表管理 - 获取时刻表详情
     */
    @GetMapping("/schedule/{id}")
    public Result<?> getScheduleDetail(@PathVariable Long id) {
        // 这里应该调用服务层方法获取时刻表详情
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("id", id);
        schedule.put("trainId", 1);
        schedule.put("trainNo", "G1234");
        schedule.put("date", "2025-06-01");
        schedule.put("startStation", "北京");
        schedule.put("endStation", "上海");
        schedule.put("startTime", "08:00");
        schedule.put("endTime", "14:00");
        schedule.put("duration", "6小时");
        schedule.put("status", "正常");
        
        // 席位信息
        List<Map<String, Object>> seats = new ArrayList<>();
        seats.add(createSeatInfo("商务座", 3000, 100));
        seats.add(createSeatInfo("一等座", 1500, 200));
        seats.add(createSeatInfo("二等座", 900, 500));
        
        schedule.put("seats", seats);
        
        return Result.success("获取时刻表详情成功", schedule);
    }
    
    private Map<String, Object> createSeatInfo(String type, double price, int count) {
        Map<String, Object> seat = new HashMap<>();
        seat.put("type", type);
        seat.put("price", price);
        seat.put("count", count);
        return seat;
    }
    
    /**
     * 时刻表管理 - 添加时刻表
     */
    @PostMapping("/schedule")
    public Result<?> addSchedule(@RequestBody Map<String, Object> schedule) {
        // 这里应该调用服务层方法保存数据
        schedule.put("id", System.currentTimeMillis());
        schedule.put("createTime", new Date());
        return Result.success("添加时刻表成功", schedule);
    }
    
    /**
     * 时刻表管理 - 更新时刻表
     */
    @PutMapping("/schedule/{id}")
    public Result<?> updateSchedule(@PathVariable Long id, @RequestBody Map<String, Object> schedule) {
        // 更新时刻表信息
        schedule.put("id", id);
        schedule.put("updateTime", new Date());
        return Result.success("更新时刻表成功", schedule);
    }
    
    /**
     * 时刻表管理 - 删除时刻表
     */
    @DeleteMapping("/schedule/{id}")
    public Result<?> deleteSchedule(@PathVariable Long id) {
        // 删除时刻表
        return Result.success("删除时刻表成功");
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
