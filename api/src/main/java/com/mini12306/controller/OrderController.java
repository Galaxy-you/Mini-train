package com.mini12306.controller;

import com.mini12306.dto.BuyTicketRequest;
import com.mini12306.model.Result;
import com.mini12306.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单并购票
     */
    @PostMapping
    public Result<?> createOrder(HttpServletRequest request, @RequestBody BuyTicketRequest buyTicketRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.createOrder(userId, buyTicketRequest);
    }
    
    /**
     * 查询用户的所有订单
     */
    @GetMapping
    public Result<?> listUserOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.listUserOrders(userId);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Result<?> cancelOrder(HttpServletRequest request, @RequestParam String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.cancelOrder(userId, orderNo);
    }
    
    /**
     * 查询订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<?> getOrderDetail(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrderDetail(userId, orderNo);
    }
    
    /**
     * 查询订单的所有票
     */
    @GetMapping("/{orderNo}/tickets")
    public Result<?> getOrderTickets(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrderTickets(userId, orderNo);
    }
    
    /**
     * 支付确认
     */
    @PostMapping("/{orderNo}/pay")
    public Result<?> confirmPayment(HttpServletRequest request, 
                                  @PathVariable String orderNo,
                                  @RequestParam Integer paymentMethod) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.confirmPayment(userId, orderNo, paymentMethod);
    }
    
    /**
     * 按起始站查询订单
     */
    @GetMapping("/search")
    public Result<?> searchOrdersByStartStation(HttpServletRequest request, @RequestParam String startStation) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.searchOrdersByStartStation(userId, startStation);
    }
}
