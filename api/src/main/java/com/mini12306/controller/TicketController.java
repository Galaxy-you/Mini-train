package com.mini12306.controller;

import com.mini12306.dto.ChangeRuleDTO;
import com.mini12306.dto.ChangeTicketRequest;
import com.mini12306.dto.RefundRuleDTO;
import com.mini12306.dto.TicketDetailDTO;
// import com.mini12306.model.Account;
import com.mini12306.model.Result;
import com.mini12306.service.TicketService;
import com.mini12306.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    
    /**
     * 获取用户购买的所有票
     */
    @GetMapping("/bought")
    public Result<List<TicketDetailDTO>> listUserBoughtTickets(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = SecurityConfig.validateToken(token);
        if (userId == null) {
            return Result.fail("未登录");
        }
        return ticketService.listUserBoughtTickets(userId);
    }
    
    /**
     * 获取用户作为乘车人的所有票
     */
    @GetMapping("/passenger/{passengerId}")
    public Result<List<TicketDetailDTO>> listUserTickets(@PathVariable Long passengerId) {
        return ticketService.listUserTickets(passengerId);
    }
    
    /**
     * 取消票
     */
    @PostMapping("/cancel")
    public Result<TicketDetailDTO> cancelTicket(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String ticketNo) {
        Long userId = SecurityConfig.validateToken(token);
        if (userId == null) {
            return Result.fail("未登录");
        }
        return ticketService.cancelTicket(userId, ticketNo);
    }
    
    /**
     * 获取票详情
     */
    @GetMapping("/{ticketNo}")
    public Result<TicketDetailDTO> getTicketDetail(@PathVariable String ticketNo) {
        return ticketService.getTicketDetail(ticketNo);
    }
    
    /**
     * 获取退票规则及计算退款金额
     */
    @GetMapping("/refund-rule")
    public Result<RefundRuleDTO> getRefundRule(@RequestParam String ticketNo) {
        return ticketService.getRefundRule(ticketNo);
    }
    
    /**
     * 检查票是否可以改签
     */
    @GetMapping("/change-rule")
    public Result<ChangeRuleDTO> checkChangeTicket(@RequestParam String ticketNo) {
        return ticketService.checkChangeTicket(ticketNo);
    }
    
    /**
     * 改签车票
     */
    @PostMapping("/change")
    public Result<TicketDetailDTO> changeTicket(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody ChangeTicketRequest request) {
        Long userId = SecurityConfig.validateToken(token);
        if (userId == null) {
            return Result.fail("未登录");
        }
        return ticketService.changeTicket(userId, request);
    }
    
    /**
     * 查询车票列表（管理员接口）
     */
    @GetMapping("/admin/list")
    public Result<?> listTickets(
            @RequestParam(required = false) String trainCode,
            @RequestParam(required = false) String startStation,
            @RequestParam(required = false) String endStation,
            @RequestParam(required = false) String passengerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success("查询成功");
    }
    
    /**
     * 更新车票（管理员接口）
     */
    @PutMapping("/admin/{id}")
    public Result<?> updateTicket(
            @PathVariable Long id,
            @RequestBody Map<String, Object> ticketInfo) {
        return Result.success("更新成功");
    }
}