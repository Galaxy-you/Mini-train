package com.mini12306.service.impl;

import com.mini12306.dto.ChangeRuleDTO;
import com.mini12306.dto.ChangeTicketRequest;
import com.mini12306.dto.RefundRuleDTO;
import com.mini12306.dto.TicketDetailDTO;
import com.mini12306.model.Money;
import com.mini12306.model.Order;
import com.mini12306.model.Result;
import com.mini12306.model.Station;
import com.mini12306.model.Ticket;
import com.mini12306.model.Train;
import com.mini12306.repository.OrderRepository;
import com.mini12306.repository.StationRepository;
import com.mini12306.repository.TicketRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.service.TicketService;
import com.mini12306.util.CodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 票务服务实现
 */
@Service
public class TicketServiceImpl implements TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Result<List<TicketDetailDTO>> listUserBoughtTickets(Long userId) {
        // 使用优化的查询方法直接获取DTO列表
        List<TicketDetailDTO> ticketDetails = ticketRepository.findTicketDetailsByUserId(userId);
        
        // 如果Repository方法没有返回数据，则使用传统方式构建DTO
        if (ticketDetails == null || ticketDetails.isEmpty()) {
            ticketDetails = new ArrayList<>();
            List<Ticket> tickets = ticketRepository.findByUserId(userId);
            
            for (Ticket ticket : tickets) {
                TicketDetailDTO dto = convertToTicketDetail(ticket);
                ticketDetails.add(dto);
            }
        }
        
        return Result.success(ticketDetails);
    }
    
    @Override
    public Result<List<TicketDetailDTO>> listUserTickets(Long passengerId) {
        List<TicketDetailDTO> ticketDetails = new ArrayList<>();
        List<Ticket> tickets = ticketRepository.findByPassengerId(passengerId);
        
        for (Ticket ticket : tickets) {
            TicketDetailDTO dto = convertToTicketDetail(ticket);
            ticketDetails.add(dto);
        }
        
        return Result.success(ticketDetails);
    }
    
    @Override
    @Transactional
    public Result<TicketDetailDTO> cancelTicket(Long userId, String ticketNo) {
        // 查询票
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNo(ticketNo);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }
        
        Ticket ticket = ticketOpt.get();
        
        // 验证票是否属于该用户
        if (!ticket.getUserId().equals(userId)) {
            return Result.fail("无权取消该车票");
        }
        
        // 验证票状态
        if (ticket.getStatus() != 1) {
            return Result.fail("车票已改签或退票");
        }

        // 取消票
        ticket.setStatus(0); // 设置为已取消状态
        ticket.setUpdateTime(new Date());
        ticketRepository.save(ticket);

        // 恢复列车余票
        Optional<Train> trainOpt = trainRepository.findById(ticket.getTrainId());
        if (trainOpt.isPresent()) {
            Train train = trainOpt.get();
            train.setSeatCount(train.getSeatCount() + 1);
            trainRepository.save(train);
        }
        
        // 更新订单总金额和票数
        updateOrderAfterCancelTicket(ticket.getOrderId());
        
        TicketDetailDTO dto = convertToTicketDetail(ticket);
        return Result.success("取消车票成功", dto);
    }
    
    @Override
    public Result<TicketDetailDTO> getTicketDetail(String ticketNo) {
        // 查询票
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNo(ticketNo);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }
        
        Ticket ticket = ticketOpt.get();
        TicketDetailDTO dto = convertToTicketDetail(ticket);
        
        return Result.success(dto);
    }

    @Override
    public Result<?> listTickets(String trainCode, String startStation, 
                                String endStation, String passengerName, PageRequest pageRequest) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(trainCode)) {
                predicates.add(cb.equal(root.get("trainCode"), trainCode));
            }
            
            if (StringUtils.hasText(startStation)) {
                predicates.add(cb.equal(root.get("startStation"), startStation));
            }
            
            if (StringUtils.hasText(endStation)) {
                predicates.add(cb.equal(root.get("endStation"), endStation));
            }
            
            if (StringUtils.hasText(passengerName)) {
                predicates.add(cb.equal(root.get("passengerName"), passengerName));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        try {
            Page<Ticket> page = ticketRepository.findAll(spec, pageRequest);
            Page<TicketDetailDTO> dtoPage = page.map(ticket -> convertToTicketDetail(ticket));
            return Result.success(dtoPage);
        } catch (Exception e) {
            return Result.fail("查询车票列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> updateTicket(Long id, Map<String, Object> ticketInfo) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }

        Ticket ticket = ticketOpt.get();

        // 仅允许修改部分字段
        if (ticketInfo.containsKey("seatType")) {
            ticket.setSeatType((String)ticketInfo.get("seatType"));
        }
        if (ticketInfo.containsKey("seatInfo")) {
            ticket.setSeatInfo((String)ticketInfo.get("seatInfo"));
        }
        if (ticketInfo.containsKey("status")) {
            ticket.setStatus(Integer.valueOf(ticketInfo.get("status").toString()));
        }

        ticket.setUpdateTime(new Date());
        ticketRepository.save(ticket);
        return Result.success("更新车票成功");
    }

    @Override
    @Transactional
    public Result<?> cancelTicket(Long id) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }

        Ticket ticket = ticketOpt.get();

        // 验证票状态
        if (ticket.getStatus() != 1) {
            return Result.fail("车票已改签或退票");
        }

        // 取消票
        ticket.setStatus(0); // 设置为已取消状态
        ticket.setUpdateTime(new Date());
        ticketRepository.save(ticket);

        // 恢复列车余票
        Optional<Train> trainOpt = trainRepository.findById(ticket.getTrainId());
        if (trainOpt.isPresent()) {
            Train train = trainOpt.get();
            train.setSeatCount(train.getSeatCount() + 1);
            trainRepository.save(train);
        }
        
        // 更新订单总金额和票数
        updateOrderAfterCancelTicket(ticket.getOrderId());

        return Result.success("取消车票成功");
    }
    
    /**
     * 更新订单信息，当票被取消时
     */
    protected void updateOrderAfterCancelTicket(Long orderId) {
        // 查询订单
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return;
        }
        
        Order order = orderOpt.get();
        
        // 查询该订单的所有有效票
        List<Ticket> validTickets = ticketRepository.findByOrderIdAndStatus(orderId, 1);
        
        // 如果没有有效票了，将订单状态更改为已取消
        if (validTickets.isEmpty()) {
            order.setStatus("已取消");
            order.setCancelTime(new Date());
            orderRepository.save(order);
            return;
        }
        
        // 重新计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Ticket ticket : validTickets) {
            if (ticket.getPrice() != null && ticket.getPrice().getAmount() != null) {
                totalAmount = totalAmount.add(ticket.getPrice().getAmount());
            }
        }
        
        // 更新订单总金额
        Money money = new Money();
        money.setAmount(totalAmount);
        order.setTotalAmount(money);
        
        // 保存订单
        orderRepository.save(order);
    }
    
    /**
     * 将Ticket实体转换为TicketDetailDTO
     */
    private TicketDetailDTO convertToTicketDetail(Ticket ticket) {
        // 如果Ticket中已经包含了所有必要信息，可以直接构建DTO
        if (ticket.getStartStation() != null && ticket.getEndStation() != null && 
            ticket.getTrainCode() != null && ticket.getSeatInfo() != null) {
            return new TicketDetailDTO(ticket);
        }
        
        // 否则，需要查询相关实体来构建完整的DTO
        Optional<Train> trainOpt = trainRepository.findById(ticket.getTrainId());
        
        TicketDetailDTO dto;
        
        if (trainOpt.isPresent()) {
            Train train = trainOpt.get();
            
            Optional<Station> startStationOpt = null;
            Optional<Station> endStationOpt = null;
            
            // 查询相关站点
            if (ticket.getStartStationId() != null) {
                startStationOpt = stationRepository.findById(ticket.getStartStationId());
            }
            
            if (ticket.getEndStationId() != null) {
                endStationOpt = stationRepository.findById(ticket.getEndStationId());
            }
            
            Station startStation = startStationOpt != null && startStationOpt.isPresent() ? 
                    startStationOpt.get() : new Station();
            Station endStation = endStationOpt != null && endStationOpt.isPresent() ? 
                    endStationOpt.get() : new Station();
            
            // 如果缺少站点名称，从备用字段获取
            if (startStation.getName() == null && ticket.getStartStation() != null) {
                startStation.setName(ticket.getStartStation());
            }
            
            if (endStation.getName() == null && ticket.getEndStation() != null) {
                endStation.setName(ticket.getEndStation());
            }
            
            dto = new TicketDetailDTO(ticket, train, startStation, endStation);
        } else {
            // 如果找不到相关的列车，只使用车票信息创建DTO
            dto = new TicketDetailDTO(ticket);
        }
        
        return dto;
    }
    
    @Override
    public Result<RefundRuleDTO> getRefundRule(String ticketNo) {
        // 查询车票
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNo(ticketNo);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }
        
        Ticket ticket = ticketOpt.get();
        
        // 检查车票状态
        if (ticket.getStatus() != 1) {
            RefundRuleDTO ruleDTO = new RefundRuleDTO();
            ruleDTO.setCanRefund(false);
            ruleDTO.setReason("车票已改签或退票，无法再次操作");
            return Result.success(ruleDTO);
        }
        
        // 获取出发时间
        Date now = new Date();
        Date travelDate = ticket.getTravelDate();
        String startTimeStr = ticket.getStartTime();
        
        // 解析出发时间
        Calendar travelTime = Calendar.getInstance();
        travelTime.setTime(travelDate);
        String[] timeParts = startTimeStr.split(":");
        
        try {
            if (timeParts.length != 2) {
                throw new IllegalArgumentException("无效的时间格式");
            }
            travelTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            travelTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的时间格式: " + startTimeStr);
        }
        
        // 计算距离发车还有多少小时
        long diffMillis = travelTime.getTimeInMillis() - now.getTime();
        long diffHours = diffMillis / (60 * 60 * 1000);
        
        RefundRuleDTO ruleDTO = new RefundRuleDTO();
        
        // 处理票价为null的情况
        BigDecimal originalPrice = BigDecimal.ZERO;
        if (ticket.getPrice() != null && ticket.getPrice().getAmount() != null) {
            originalPrice = ticket.getPrice().getAmount();
            ruleDTO.setOriginalPrice(originalPrice);
        } else {
            ruleDTO.setOriginalPrice(BigDecimal.ZERO);
        }
        
        // 退票规则
        List<String> rules = new ArrayList<>();
        rules.add("1. 开车前48小时（含）以上退票，手续费为票价的5%");
        rules.add("2. 开车前24小时（含）～48小时以内退票，手续费为票价的10%");
        rules.add("3. 开车前24小时以内退票，手续费为票价的20%");
        rules.add("4. 开车后、到站前退票，手续费为票价的50%");
        rules.add("5. 到站后退票，不予退款");
        ruleDTO.setRules(rules);
        
        // 计算手续费和可退金额
        BigDecimal fee;
        
        if (diffHours >= 48) {
            // 开车前48小时（含）以上
            fee = originalPrice.multiply(new BigDecimal("0.05"));
        } else if (diffHours >= 24) {
            // 开车前24小时（含）～48小时以内
            fee = originalPrice.multiply(new BigDecimal("0.10"));
        } else if (diffHours >= 0) {
            // 开车前24小时以内
            fee = originalPrice.multiply(new BigDecimal("0.20"));
        } else if (diffMillis > -ticket.getDuration() * 60 * 1000) {
            // 开车后、到站前
            fee = originalPrice.multiply(new BigDecimal("0.50"));
        } else {
            // 到站后
            ruleDTO.setCanRefund(false);
            ruleDTO.setReason("列车已到站，不可退票");
            return Result.success(ruleDTO);
        }
        
        // 计算可退金额
        BigDecimal refundableAmount = originalPrice.subtract(fee);
        
        ruleDTO.setFee(fee);
        ruleDTO.setRefundableAmount(refundableAmount);
        ruleDTO.setCanRefund(true);
        
        return Result.success(ruleDTO);
    }
    
    @Override
    public Result<ChangeRuleDTO> checkChangeTicket(String ticketNo) {
        // 查询车票
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNo(ticketNo);
        if (!ticketOpt.isPresent()) {
            return Result.fail("车票不存在");
        }
        
        Ticket ticket = ticketOpt.get();
        
        // 检查车票状态
        if (ticket.getStatus() != 1) {
            ChangeRuleDTO ruleDTO = new ChangeRuleDTO();
            ruleDTO.setCanChange(false);
            ruleDTO.setReason("车票已改签或退票，无法再次操作");
            return Result.success(ruleDTO);
        }
        
        // 获取出发时间
        Date now = new Date();
        Date travelDate = ticket.getTravelDate();
        String startTimeStr = ticket.getStartTime();
        
        // 解析出发时间
        Calendar travelTime = Calendar.getInstance();
        travelTime.setTime(travelDate);
        String[] timeParts = startTimeStr.split(":");
        
        try {
            if (timeParts.length != 2) {
                throw new IllegalArgumentException("无效的时间格式");
            }
            travelTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            travelTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的时间格式: " + startTimeStr);
        }
        
        // 计算距离发车还有多少小时
        long diffMillis = travelTime.getTimeInMillis() - now.getTime();
        
        ChangeRuleDTO ruleDTO = new ChangeRuleDTO();
        ruleDTO.setOriginalPrice(ticket.getPrice().getAmount());
        
        // 检查是否已过发车时间
        if (diffMillis <= 0) {
            ruleDTO.setCanChange(false);
            ruleDTO.setReason("列车已发车，不可改签");
            return Result.success(ruleDTO);
        }
        
        // 改签规则
        List<String> rules = new ArrayList<>();
        rules.add("1. 同一车次不同席别或不同日期，收取票价差额");
        rules.add("2. 开车前48小时（含）以上改签，免收手续费");
        rules.add("3. 开车前24小时（含）～48小时以内改签，手续费为票价的2%");
        rules.add("4. 开车前24小时以内改签，手续费为票价的5%");
        rules.add("5. 每张车票限改签2次");
        ruleDTO.setRules(rules);
        
        // 设置最低手续费
        ruleDTO.setMinFee(new BigDecimal("0.00"));
        ruleDTO.setCanChange(true);
        
        return Result.success(ruleDTO);
    }
    
    @Override
    @Transactional
    public Result<TicketDetailDTO> changeTicket(Long userId, ChangeTicketRequest request) {
        // 查询原车票
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNo(request.getOriginalTicketNo());
        if (!ticketOpt.isPresent()) {
            return Result.fail("原车票不存在");
        }
        
        Ticket originalTicket = ticketOpt.get();
        
        // 验证票
        if (!originalTicket.getUserId().equals(userId)) {
            return Result.fail("无权改签该车票");
        }
        if (originalTicket.getStatus() != 1) {
            return Result.fail("车票已改签或退票");
        }
        
        // 检查发车时间
        Date now = new Date();
        Date travelDate = originalTicket.getTravelDate();
        String startTimeStr = originalTicket.getStartTime();
        
        // 解析出发时间 & 检查是否已过发车时间
        Calendar travelTime = Calendar.getInstance();
        travelTime.setTime(travelDate);
        String[] timeParts = startTimeStr.split(":");
        travelTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        travelTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        if (travelTime.getTimeInMillis() <= now.getTime()) {
            return Result.fail("列车已发车，不可改签");
        }
        
        // 查询原列车（用于恢复座位）
        Optional<Train> originalTrainOpt = trainRepository.findById(originalTicket.getTrainId());
        if (!originalTrainOpt.isPresent()) {
            return Result.fail("原车次不存在");
        }
        Train originalTrain = originalTrainOpt.get();
        
        // 查询新车次
        Optional<Train> newTrainOpt = trainRepository.findById(request.getNewTrainId());
        if (!newTrainOpt.isPresent()) {
            return Result.fail("改签目标车次不存在");
        }
        
        Train newTrain = newTrainOpt.get();
        
        // 验证：起点终点必须不变
        if (!originalTicket.getStartStation().equals(newTrain.getStartStation()) ||
            !originalTicket.getEndStation().equals(newTrain.getEndStation())) {
            return Result.fail("只能改签到相同起点和终点的车次");
        }
        
        // 解析旅行日期
        Date newTravelDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            newTravelDate = sdf.parse(request.getTravelDate());
        } catch (Exception e) {
            return Result.fail("日期格式有误");
        }
        
        // 检查新座位类型是否有剩余
        String newSeatType = request.getNewSeatType();
        boolean hasAvailableSeat = false;
        
        if ("高铁".equals(newTrain.getType())) {
            if ("商务座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getHighSeatCount() != null && newTrain.getHighSeatCount() > 0;
            } else if ("一等座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getMidSeatCount() != null && newTrain.getMidSeatCount() > 0;
            } else if ("二等座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getLowSeatCount() != null && newTrain.getLowSeatCount() > 0;
            }
        } else if ("动车".equals(newTrain.getType())) {
            if ("一等座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getMidSeatCount() != null && newTrain.getMidSeatCount() > 0;
            } else if ("二等座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getLowSeatCount() != null && newTrain.getLowSeatCount() > 0;
            }
        } else {
            if ("软卧".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getHighSeatCount() != null && newTrain.getHighSeatCount() > 0;
            } else if ("硬卧".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getMidSeatCount() != null && newTrain.getMidSeatCount() > 0;
            } else if ("硬座".equals(newSeatType)) {
                hasAvailableSeat = newTrain.getLowSeatCount() != null && newTrain.getLowSeatCount() > 0;
            }
        }
        
        if (!hasAvailableSeat) {
            return Result.fail("新车次的该座位类型已售罄");
        }
        
        // 计算新票价（根据座位类型）
        Money newPrice = calculateSeatPrice(newTrain, newSeatType);
        if (newPrice == null) {
            return Result.fail("无法计算该座位类型的价格");
        }
        
        // 恢复原列车座位
        restoreTrainSeat(originalTrain, originalTicket.getSeatType());
        trainRepository.save(originalTrain);
        
        // 减少新列车座位
        reduceTrainSeat(newTrain, newSeatType);
        trainRepository.save(newTrain);
        
        // 获取座位号
        int soldSeatsCount = getSoldSeatsCount(newTrain, newSeatType);
        int coachNumber = getCoachNumberBySeatType(newTrain.getType(), newSeatType);
        int seatNumber = soldSeatsCount + 1;
        
        // 创建新票
        Ticket newTicket = new Ticket();
        newTicket.setTicketNo(CodeGenerator.generateTicketNo());
        newTicket.setOrderId(originalTicket.getOrderId());
        newTicket.setUserId(userId);
        newTicket.setPassengerId(originalTicket.getPassengerId());
        newTicket.setPassengerName(originalTicket.getPassengerName());
        newTicket.setPassengerCard(originalTicket.getPassengerCard());
        newTicket.setTrainId(newTrain.getId());
        newTicket.setTrainCode(newTrain.getCode());
        newTicket.setTrainType(newTrain.getType());
        newTicket.setStartStation(newTrain.getStartStation());
        newTicket.setEndStation(newTrain.getEndStation());
        newTicket.setSeatType(newSeatType);
        newTicket.setCoach(String.valueOf(coachNumber));
        newTicket.setSeat(String.valueOf(seatNumber));
        newTicket.setSeatInfo(coachNumber + "车" + seatNumber + "号座");
        newTicket.setPrice(newPrice);
        newTicket.setTravelDate(newTravelDate);
        newTicket.setStartTime(newTrain.getStartTime());
        newTicket.setEndTime(newTrain.getEndTime());
        newTicket.setDuration(newTrain.getDuration());        
        newTicket.setStatus(1); // 正常状态
        newTicket.setCreateTime(new Date());
        newTicket.setUpdateTime(new Date());
        
        // 保存新票 & 更新原票状态
        ticketRepository.save(newTicket);        
        originalTicket.setStatus(3); // 3表示已改签状态
        originalTicket.setUpdateTime(new Date());
        ticketRepository.save(originalTicket);
        
        // 转换为DTO返回
        TicketDetailDTO dto = convertToTicketDetail(newTicket);
        
        return Result.success("改签成功", dto);
    }
    
    /**
     * 根据座位类型计算实际价格
     */
    private Money calculateSeatPrice(Train train, String seatType) {
        if ("高铁".equals(train.getType())) {
            if ("商务座".equals(seatType)) {
                return train.getPrice().multiply(1.5);
            } else if ("一等座".equals(seatType)) {
                return train.getPrice().multiply(1.2);
            } else if ("二等座".equals(seatType)) {
                return train.getPrice();
            }
        } else if ("动车".equals(train.getType())) {
            if ("一等座".equals(seatType)) {
                return train.getPrice().multiply(1.2);
            } else if ("二等座".equals(seatType)) {
                return train.getPrice();
            }
        } else {
            // 普通列车
            if ("软卧".equals(seatType)) {
                return train.getPrice().multiply(1.5);
            } else if ("硬卧".equals(seatType)) {
                return train.getPrice().multiply(1.2);
            } else if ("硬座".equals(seatType)) {
                return train.getPrice();
            }
        }
        return null;
    }
    
    /**
     * 恢复列车座位（退票/改签时）
     */
    private void restoreTrainSeat(Train train, String seatType) {
        if ("高铁".equals(train.getType())) {
            if ("商务座".equals(seatType) && train.getHighSeatCount() != null) {
                train.setHighSeatCount(train.getHighSeatCount() + 1);
            } else if ("一等座".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() + 1);
            } else if ("二等座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() + 1);
            }
        } else if ("动车".equals(train.getType())) {
            if ("一等座".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() + 1);
            } else if ("二等座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() + 1);
            }
        } else {
            if ("软卧".equals(seatType) && train.getHighSeatCount() != null) {
                train.setHighSeatCount(train.getHighSeatCount() + 1);
            } else if ("硬卧".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() + 1);
            } else if ("硬座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() + 1);
            }
        }
        train.setSeatCount(train.getSeatCount() + 1);
    }
    
    /**
     * 减少列车座位（购票/改签时）
     */
    private void reduceTrainSeat(Train train, String seatType) {
        if ("高铁".equals(train.getType())) {
            if ("商务座".equals(seatType) && train.getHighSeatCount() != null) {
                train.setHighSeatCount(train.getHighSeatCount() - 1);
            } else if ("一等座".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() - 1);
            } else if ("二等座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() - 1);
            }
        } else if ("动车".equals(train.getType())) {
            if ("一等座".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() - 1);
            } else if ("二等座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() - 1);
            }
        } else {
            if ("软卧".equals(seatType) && train.getHighSeatCount() != null) {
                train.setHighSeatCount(train.getHighSeatCount() - 1);
            } else if ("硬卧".equals(seatType) && train.getMidSeatCount() != null) {
                train.setMidSeatCount(train.getMidSeatCount() - 1);
            } else if ("硬座".equals(seatType) && train.getLowSeatCount() != null) {
                train.setLowSeatCount(train.getLowSeatCount() - 1);
            }
        }
        train.setSeatCount(train.getSeatCount() - 1);
    }
    
    /**
     * 根据座位类型分配车厢号
     */
    private int getCoachNumberBySeatType(String trainType, String seatType) {
        if ("高铁".equals(trainType)) {
            if ("商务座".equals(seatType)) {
                return 1;
            } else if ("一等座".equals(seatType)) {
                return 2;
            } else if ("二等座".equals(seatType)) {
                return 3;
            }
        } else if ("动车".equals(trainType)) {
            if ("一等座".equals(seatType)) {
                return 1;
            } else if ("二等座".equals(seatType)) {
                return 2;
            }
        } else {
            if ("软卧".equals(seatType)) {
                return 1;
            } else if ("硬卧".equals(seatType)) {
                return 2;
            } else if ("硬座".equals(seatType)) {
                return 3;
            }
        }
        return 1;
    }
    
    /**
     * 获取该座位类型已售出的数量
     */
    private int getSoldSeatsCount(Train train, String seatType) {
        List<Ticket> soldTickets = ticketRepository.findByTrainIdAndSeatTypeAndStatus(
            train.getId(), seatType, 1);
        return soldTickets != null ? soldTickets.size() : 0;
    }
}
