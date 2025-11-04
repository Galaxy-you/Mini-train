package com.mini12306.service;

import com.mini12306.model.Result;
import com.mini12306.dto.TicketDetailDTO;
import com.mini12306.dto.ChangeTicketRequest;
import com.mini12306.dto.RefundRuleDTO;
import com.mini12306.dto.ChangeRuleDTO;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

/**
 * 票务服务接口
 */
public interface TicketService {
    /**
     * 查询用户购买的所有票（包括为他人购买的），返回详细信息
     */
    Result<List<TicketDetailDTO>> listUserBoughtTickets(Long userId);
    
    /**
     * 查询用户作为乘车人的所有票，返回详细信息
     */
    Result<List<TicketDetailDTO>> listUserTickets(Long passengerId);
    
    /**
     * 取消票
     */
    Result<TicketDetailDTO> cancelTicket(Long userId, String ticketNo);
    
    /**
     * 查询票详情
     */
    Result<TicketDetailDTO> getTicketDetail(String ticketNo);

    /**
     * 分页查询车票列表（管理后台）
     */
    Result<?> listTickets(String trainCode, String startStation, String endStation, String passengerName, PageRequest pageRequest);

    /**
     * 更新车票（管理后台）
     */
    Result<?> updateTicket(Long id, Map<String, Object> ticketInfo);

    /**
     * 取消车票（管理后台）
     */
    Result<?> cancelTicket(Long id);
    
    /**
     * 获取退票规则及计算退款金额
     */
    Result<RefundRuleDTO> getRefundRule(String ticketNo);
    
    /**
     * 检查票是否可以改签
     */
    Result<ChangeRuleDTO> checkChangeTicket(String ticketNo);
    
    /**
     * 改签车票
     */
    Result<TicketDetailDTO> changeTicket(Long userId, ChangeTicketRequest request);
}
