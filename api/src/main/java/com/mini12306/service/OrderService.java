package com.mini12306.service;

import com.mini12306.dto.BuyTicketRequest;
import com.mini12306.model.Order;
import com.mini12306.model.Result;
import com.mini12306.model.Ticket;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    /**
     * 创建订单并购票
     */
    Result<Order> createOrder(Long userId, BuyTicketRequest request);
    
    /**
     * 查询用户的所有订单
     */
    Result<List<Order>> listUserOrders(Long userId);
    
    /**
     * 取消订单
     */
    Result<Order> cancelOrder(Long userId, String orderNo);
    
    /**
     * 查询订单详情
     */
    Result<Order> getOrderDetail(Long userId, String orderNo);
    
    /**
     * 查询订单的所有票
     */
    Result<List<Ticket>> getOrderTickets(Long userId, String orderNo);
    
    /**
     * 支付确认 - 更新订单状态为已支付
     */
    Result<Order> confirmPayment(Long userId, String orderNo, Integer paymentMethod);
}
