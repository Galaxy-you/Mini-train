package com.mini12306.service.impl;

import com.mini12306.dto.BuyTicketRequest;
import com.mini12306.model.Money;
import com.mini12306.model.Order;
import com.mini12306.model.Passenger;
import com.mini12306.model.Result;
import com.mini12306.model.Ticket;
import com.mini12306.model.Train;
import com.mini12306.repository.OrderRepository;
import com.mini12306.repository.PassengerRepository;
import com.mini12306.repository.TicketRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.service.OrderService;
import com.mini12306.util.CodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private PassengerRepository passengerRepository;
    
    /**
     * 扩展的Order DTO类，包含车次和站点信息
     */
    public static class OrderDTO extends Order {
        private String trainNumber; // 车次
        private String trainType;   // 车型
        private String startStation; // 出发站
        private String endStation;   // 到达站
        private String departureTime; // 出发时间
        private String arrivalTime;   // 到达时间
        
        // 从Order构造一个OrderDTO
        public OrderDTO(Order order) {
            // 复制Order的所有属性
            this.setId(order.getId());
            this.setOrderNo(order.getOrderNo());
            this.setUserId(order.getUserId());
            this.setTotalAmount(order.getTotalAmount());
            this.setStatus(order.getStatus());
            this.setCreateTime(order.getCreateTime());
            this.setPayTime(order.getPayTime());
            this.setCancelTime(order.getCancelTime());
            this.setTicketCount(order.getTicketCount());
        }

        // Getter和Setter
        public String getTrainNumber() {
            return trainNumber;
        }

        public void setTrainNumber(String trainNumber) {
            this.trainNumber = trainNumber;
        }

        public String getTrainType() {
            return trainType;
        }

        public void setTrainType(String trainType) {
            this.trainType = trainType;
        }

        public String getStartStation() {
            return startStation;
        }

        public void setStartStation(String startStation) {
            this.startStation = startStation;
        }

        public String getEndStation() {
            return endStation;
        }

        public void setEndStation(String endStation) {
            this.endStation = endStation;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }
    }
    
    @Override
    @Transactional
    public Result<Order> createOrder(Long userId, BuyTicketRequest request) {
        // 验证请求参数
        if (request.getTrainId() == null || request.getPassengerIds() == null || request.getPassengerIds().isEmpty()) {
            return Result.fail("参数错误");
        }
        
        // 查询列车
        Optional<Train> trainOpt = trainRepository.findById(request.getTrainId());
        if (!trainOpt.isPresent()) {
            return Result.fail("列车不存在");
        }      

        Train train = trainOpt.get();
        
        // 验证站点
        if (!train.getStartStation().equals(request.getStartStation()) || 
            !train.getEndStation().equals(request.getEndStation())) {
            return Result.fail("站点信息不匹配");
        }
        
        // 验证乘车人
        List<Passenger> passengers = new ArrayList<>();
        for (Long passengerId : request.getPassengerIds()) {
            Optional<Passenger> passengerOpt = passengerRepository.findById(passengerId);
            if (!passengerOpt.isPresent()) {
                return Result.fail("乘车人不存在: " + passengerId);
            }
            passengers.add(passengerOpt.get());
        }
        
        // 获取座位类型和计算实际价格
        String seatType = request.getSeatType() != null ? request.getSeatType() : "二等座";
        Money actualPrice = calculateSeatPrice(train, seatType);
        if (actualPrice == null) {
            return Result.fail("无法计算该座位类型的价格");
        }
        
        // 验证余票（根据座位类型检查）
        int requestedSeats = request.getPassengerIds().size();
        
        // 根据座位类型和列车类型检查余票
        boolean hasSeat = false;
        if ("高铁".equals(train.getType())) {
            if ("商务座".equals(seatType)) {
                hasSeat = train.getHighSeatCount() != null && train.getHighSeatCount() >= requestedSeats;
            } else if ("一等座".equals(seatType)) {
                hasSeat = train.getMidSeatCount() != null && train.getMidSeatCount() >= requestedSeats;
            } else if ("二等座".equals(seatType)) {
                hasSeat = train.getLowSeatCount() != null && train.getLowSeatCount() >= requestedSeats;
            }
        } else if ("动车".equals(train.getType())) {
            if ("一等座".equals(seatType)) {
                hasSeat = train.getMidSeatCount() != null && train.getMidSeatCount() >= requestedSeats;
            } else if ("二等座".equals(seatType)) {
                hasSeat = train.getLowSeatCount() != null && train.getLowSeatCount() >= requestedSeats;
            }
        } else {
            // 普通列车
            if ("软卧".equals(seatType)) {
                hasSeat = train.getHighSeatCount() != null && train.getHighSeatCount() >= requestedSeats;
            } else if ("硬卧".equals(seatType)) {
                hasSeat = train.getMidSeatCount() != null && train.getMidSeatCount() >= requestedSeats;
            } else if ("硬座".equals(seatType)) {
                hasSeat = train.getLowSeatCount() != null && train.getLowSeatCount() >= requestedSeats;
            }
        }
        
        if (!hasSeat) {
            return Result.fail("该座位类型余票不足");
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(CodeGenerator.generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(actualPrice.multiply(request.getPassengerIds().size()));
        order.setStatus("UNPAID");  // 使用英文状态标识
        order.setCreateTime(new Date());
        order.setPayTime(null);  // 未支付时 payTime 应该为 null

        orderRepository.save(order);
        
        // 获取当前座位类型已售出的数量（用于分配座位号）
        int soldSeatsOfType = getSoldSeatsCount(train, seatType);
        
        // 创建车票
        for (int i = 0; i < passengers.size(); i++) {
            Passenger passenger = passengers.get(i);
            
            Ticket ticket = new Ticket();
            ticket.setTicketNo(CodeGenerator.generateTicketNo());
            ticket.setOrderId(order.getId());
            ticket.setUserId(userId);
            ticket.setPassengerId(passenger.getId());
            ticket.setPassengerName(passenger.getRealName());
            ticket.setPassengerCard(passenger.getCardId());
            ticket.setTrainId(train.getId());
            ticket.setTrainCode(train.getCode());
            ticket.setTrainType(train.getType());
            // 设置起始站和终点站ID
            ticket.setStartStationId(train.getStartStationId());
            ticket.setEndStationId(train.getEndStationId());
            // 设置起始站和终点站名称
            ticket.setStartStation(request.getStartStation());
            ticket.setEndStation(request.getEndStation());
            // 设置城市信息
            ticket.setStartCity(request.getStartCity() != null ? request.getStartCity() : extractCityFromStation(request.getStartStation()));
            ticket.setEndCity(request.getEndCity() != null ? request.getEndCity() : extractCityFromStation(request.getEndStation()));
            
            // 根据座位类型分配车厢和座位号
            int coachNumber = getCoachNumberBySeatType(train.getType(), seatType);
            int seatNumber = soldSeatsOfType + i + 1; // 当前座位类型已售 + 当前索引 + 1
            
            ticket.setSeat(String.valueOf(seatNumber));
            ticket.setCoach(String.valueOf(coachNumber));
            ticket.setSeatInfo(coachNumber + "车" + seatNumber + "号座");
            ticket.setSeatType(seatType);
            ticket.setPrice(actualPrice); // 使用实际价格
            ticket.setStartTime(train.getStartTime());
            ticket.setEndTime(train.getEndTime());
            // 设置旅行日期为当天
            ticket.setTravelDate(new Date());
            // 设置行程时间（分钟）
            if (train.getDuration() != null) {
                ticket.setDuration(train.getDuration());
            } else {
                // 如果列车没有提供时长，则设置一个默认值
                ticket.setDuration(120); // 默认2小时
            }
            ticket.setStatus(1); // 1表示正常状态，0表示已取消
            ticket.setCreateTime(new Date());
            ticket.setUpdateTime(new Date());
            
            ticketRepository.save(ticket);
        }
        
        // 注意：创建订单时不扣减余票，等支付时再扣减
        // 这样可以避免恶意用户批量下单占用余票

        // 设置订单包含的票数
        order.setTicketCount(request.getPassengerIds().size());
        
        // 创建扩展的OrderDTO对象并填充信息
        OrderDTO orderDTO = new OrderDTO(order);
        orderDTO.setTrainNumber(train.getCode());
        orderDTO.setTrainType(train.getType());
        orderDTO.setStartStation(request.getStartStation());
        orderDTO.setEndStation(request.getEndStation());
        orderDTO.setDepartureTime(train.getStartTime());
        orderDTO.setArrivalTime(train.getEndTime());
        
        // 调试输出订单创建信息
        System.out.println("=================创建订单=======================");
        System.out.println("订单号: " + order.getOrderNo());
        System.out.println("用户ID: " + userId);
        System.out.println("列车ID: " + request.getTrainId());
        System.out.println("起始站: " + request.getStartStation());
        System.out.println("终点站: " + request.getEndStation());
        System.out.println("乘车人IDs: " + request.getPassengerIds());

        return Result.success("购票成功", (Order)orderDTO);
    }
    
    /**
     * 从站点名称中提取城市名
     * 例如：上海站 -> 上海，北京西站 -> 北京，广州南站 -> 广州
     */
    private String extractCityFromStation(String stationName) {
        if (stationName == null || stationName.isEmpty()) {
            return "";
        }
        
        // 移除站、东站、南站、西站、北站等后缀
        String[] suffixes = {"站", "东站", "南站", "西站", "北站"};
        for (String suffix : suffixes) {
            if (stationName.endsWith(suffix)) {
                return stationName.substring(0, stationName.length() - suffix.length());
            }
        }
        
        return stationName;
    }
    
    @Override
    public Result<List<Order>> listUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<OrderDTO> orderDTOs = new ArrayList<>();
        
        // 为每个订单加载票数信息并转换为OrderDTO
        for (Order order : orders) {
            List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
            order.setTicketCount(tickets.size());
            
            // 创建扩展的OrderDTO对象
            OrderDTO orderDTO = new OrderDTO(order);
            
            // 如果有票，从第一张票获取车次和站点信息
            if (!tickets.isEmpty()) {
                Ticket firstTicket = tickets.get(0);
                orderDTO.setTrainNumber(firstTicket.getTrainCode());
                orderDTO.setTrainType(firstTicket.getTrainType());
                orderDTO.setStartStation(firstTicket.getStartStation());
                orderDTO.setEndStation(firstTicket.getEndStation());
                orderDTO.setDepartureTime(firstTicket.getStartTime());
                orderDTO.setArrivalTime(firstTicket.getEndTime());
            }
            
            orderDTOs.add(orderDTO);
        }
        
        // 创建一个新的Order列表以避免类型转换警告
        List<Order> resultList = new ArrayList<>(orderDTOs);
        return Result.success(resultList);
    }
    
    @Override
    @Transactional
    public Result<Order> cancelOrder(Long userId, String orderNo) {
        // 查询订单
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            return Result.fail("订单不存在");
        }
        
        Order order = orderOpt.get();
        
        // 验证订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权取消该订单");
        }
        
        // 验证订单状态
        if ("CANCELED".equals(order.getStatus())) {
            return Result.fail("订单已取消");
        }
        
        // 查询订单的所有票
        // 记录订单原状态，用于判断是否需要恢复余票
        String originalStatus = order.getStatus();

        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        
        // 验证票状态
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != 1) { // 1表示正常状态
                return Result.fail("订单中有票已改签或退票，无法取消");
            }
        }
        
        // 取消订单
        order.setStatus("CANCELED");  // 使用英文状态标识
        order.setCancelTime(new Date());
        orderRepository.save(order);
        
        // 获取第一张票的信息（用于后续填充 OrderDTO）
        Ticket firstTicket = tickets.isEmpty() ? null : tickets.get(0);
        
        // 取消票并恢复余票
        for (Ticket ticket : tickets) {
            // 只取消状态为正常的票
            if (ticket.getStatus() == 1) {  
                ticket.setStatus(0); // 使用0表示已取消状态
                ticket.setUpdateTime(new Date());
                ticketRepository.save(ticket);

                // 只有已支付的订单才恢复列车余票（未支付的订单没有扣票）
                if ("PAID".equals(originalStatus)) {
                    Optional<Train> trainOpt = trainRepository.findById(ticket.getTrainId());
                    if (trainOpt.isPresent()) {
                        Train train = trainOpt.get();
                        String seatType = ticket.getSeatType();
                        
                        // 根据座位类型恢复库存
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
                            // 普通列车
                            if ("软卧".equals(seatType) && train.getHighSeatCount() != null) {
                                train.setHighSeatCount(train.getHighSeatCount() + 1);
                            } else if ("硬卧".equals(seatType) && train.getMidSeatCount() != null) {
                                train.setMidSeatCount(train.getMidSeatCount() + 1);
                            } else if ("硬座".equals(seatType) && train.getLowSeatCount() != null) {
                                train.setLowSeatCount(train.getLowSeatCount() + 1);
                            }
                        }
                        
                        train.setSeatCount(train.getSeatCount() + 1);
                        trainRepository.save(train);

                        System.out.println("=== 取消已支付订单，恢复余票 ===");
                        System.out.println("列车ID: " + ticket.getTrainId());
                        System.out.println("座位类型: " + seatType);
                        System.out.println("恢复后总余票: " + train.getSeatCount());
                    }
                }
            }
        }
        
        // 创建扩展的OrderDTO对象
        OrderDTO orderDTO = new OrderDTO(order);
        orderDTO.setTicketCount(tickets.size());
        
        // 如果有票，从第一张票获取车次和站点信息
        if (firstTicket != null) {
            orderDTO.setTrainNumber(firstTicket.getTrainCode());
            orderDTO.setTrainType(firstTicket.getTrainType());
            orderDTO.setStartStation(firstTicket.getStartStation());
            orderDTO.setEndStation(firstTicket.getEndStation());
            orderDTO.setDepartureTime(firstTicket.getStartTime());
            orderDTO.setArrivalTime(firstTicket.getEndTime());
        }
        
        return Result.success("取消订单成功", (Order)orderDTO);
    }
    
    @Override
    public Result<Order> getOrderDetail(Long userId, String orderNo) {
        // 查询订单
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            return Result.fail("订单不存在");
        }
        
        Order order = orderOpt.get();
        
        // 验证订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权查看该订单");
        }
        
        // 查询订单的票
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        order.setTicketCount(tickets.size());
        
        // 创建扩展的OrderDTO对象
        OrderDTO orderDTO = new OrderDTO(order);
        
        // 如果有票，从第一张票获取车次和站点信息
        if (!tickets.isEmpty()) {
            Ticket firstTicket = tickets.get(0);
            orderDTO.setTrainNumber(firstTicket.getTrainCode());
            orderDTO.setTrainType(firstTicket.getTrainType());
            orderDTO.setStartStation(firstTicket.getStartStation());
            orderDTO.setEndStation(firstTicket.getEndStation());
            orderDTO.setDepartureTime(firstTicket.getStartTime());
            orderDTO.setArrivalTime(firstTicket.getEndTime());
        }
        
        return Result.success((Order)orderDTO);
    }
    
    @Override
    public Result<List<Ticket>> getOrderTickets(Long userId, String orderNo) {
        // 查询订单
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            return Result.fail("订单不存在");
        }
        
        Order order = orderOpt.get();
        
        // 验证订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权查看该订单");
        }
        
        // 查询订单的所有票
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        
        return Result.success(tickets);
    }
    
    @Override
    @Transactional
    public Result<Order> confirmPayment(Long userId, String orderNo, Integer paymentMethod) {
        // 添加调试日志
        System.out.println("=== 支付确认调试信息 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("订单号: " + orderNo);
        System.out.println("支付方式: " + paymentMethod);
        
        // 查询订单
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            System.out.println("错误: 订单不存在 - " + orderNo);
            return Result.fail("订单不存在");
        }
        
        Order order = orderOpt.get();
        
        // 验证订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作该订单");
        }
        
        // 验证订单状态
        if (!"UNPAID".equals(order.getStatus())) {
            return Result.fail("订单状态错误，无法支付");
        }
        
        // 查询订单的票信息
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());

        if (tickets.isEmpty()) {
            return Result.fail("订单中没有车票");
        }

        // 获取列车信息用于扣减余票
        Ticket firstTicket = tickets.get(0);
        Long trainId = firstTicket.getTrainId();
        int ticketCount = tickets.size();
        String seatType = firstTicket.getSeatType();

        // 查询列车信息
        Optional<Train> trainOpt = trainRepository.findById(trainId);
        if (!trainOpt.isPresent()) {
            return Result.fail("列车不存在");
        }

        Train train = trainOpt.get();

        // 根据座位类型检查余票是否足够并扣减
        boolean deducted = false;
        if ("高铁".equals(train.getType())) {
            if ("商务座".equals(seatType)) {
                if (train.getHighSeatCount() != null && train.getHighSeatCount() >= ticketCount) {
                    train.setHighSeatCount(train.getHighSeatCount() - ticketCount);
                    deducted = true;
                }
            } else if ("一等座".equals(seatType)) {
                if (train.getMidSeatCount() != null && train.getMidSeatCount() >= ticketCount) {
                    train.setMidSeatCount(train.getMidSeatCount() - ticketCount);
                    deducted = true;
                }
            } else if ("二等座".equals(seatType)) {
                if (train.getLowSeatCount() != null && train.getLowSeatCount() >= ticketCount) {
                    train.setLowSeatCount(train.getLowSeatCount() - ticketCount);
                    deducted = true;
                }
            }
        } else if ("动车".equals(train.getType())) {
            if ("一等座".equals(seatType)) {
                if (train.getMidSeatCount() != null && train.getMidSeatCount() >= ticketCount) {
                    train.setMidSeatCount(train.getMidSeatCount() - ticketCount);
                    deducted = true;
                }
            } else if ("二等座".equals(seatType)) {
                if (train.getLowSeatCount() != null && train.getLowSeatCount() >= ticketCount) {
                    train.setLowSeatCount(train.getLowSeatCount() - ticketCount);
                    deducted = true;
                }
            }
        } else {
            // 普通列车
            if ("软卧".equals(seatType)) {
                if (train.getHighSeatCount() != null && train.getHighSeatCount() >= ticketCount) {
                    train.setHighSeatCount(train.getHighSeatCount() - ticketCount);
                    deducted = true;
                }
            } else if ("硬卧".equals(seatType)) {
                if (train.getMidSeatCount() != null && train.getMidSeatCount() >= ticketCount) {
                    train.setMidSeatCount(train.getMidSeatCount() - ticketCount);
                    deducted = true;
                }
            } else if ("硬座".equals(seatType)) {
                if (train.getLowSeatCount() != null && train.getLowSeatCount() >= ticketCount) {
                    train.setLowSeatCount(train.getLowSeatCount() - ticketCount);
                    deducted = true;
                }
            }
        }
        
        if (!deducted) {
            return Result.fail("该座位类型余票不足");
        }

        // 扣减列车余票
        train.setSeatCount(train.getSeatCount() - ticketCount);
        trainRepository.save(train);

        // 余票扣减成功后，才更新订单状态为已支付
        order.setStatus("PAID");
        order.setPayTime(new Date());
        orderRepository.save(order);

        // 更新所有车票状态为正常（1）
        for (Ticket ticket : tickets) {
            ticket.setStatus(1); // 1表示正常状态
            ticket.setUpdateTime(new Date());
            ticketRepository.save(ticket);
        }

        System.out.println("=== 支付成功，扣减余票 ===");
        System.out.println("列车ID: " + trainId);
        System.out.println("扣减票数: " + ticketCount);
        System.out.println("剩余票数: " + train.getSeatCount());

        order.setTicketCount(tickets.size());
        
        // 创建扩展的OrderDTO对象
        OrderDTO orderDTO = new OrderDTO(order);
        
        // 如果有票，从第一张票获取车次和站点信息
        if (!tickets.isEmpty()) {
            Ticket ticketInfo = tickets.get(0);
            orderDTO.setTrainNumber(ticketInfo.getTrainCode());
            orderDTO.setTrainType(ticketInfo.getTrainType());
            orderDTO.setStartStation(ticketInfo.getStartStation());
            orderDTO.setEndStation(ticketInfo.getEndStation());
            orderDTO.setDepartureTime(ticketInfo.getStartTime());
            orderDTO.setArrivalTime(ticketInfo.getEndTime());
        }
        
        return Result.success("支付成功", (Order)orderDTO);
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
     * 根据座位类型分配车厢号
     */
    private int getCoachNumberBySeatType(String trainType, String seatType) {
        if ("高铁".equals(trainType)) {
            if ("商务座".equals(seatType)) {
                return 1; // 商务座1车
            } else if ("一等座".equals(seatType)) {
                return 2; // 一等座2车
            } else if ("二等座".equals(seatType)) {
                return 3; // 二等座3车
            }
        } else if ("动车".equals(trainType)) {
            if ("一等座".equals(seatType)) {
                return 1; // 一等座1车
            } else if ("二等座".equals(seatType)) {
                return 2; // 二等座2车
            }
        } else {
            // 普通列车
            if ("软卧".equals(seatType)) {
                return 1; // 软卧1车
            } else if ("硬卧".equals(seatType)) {
                return 2; // 硬卧2车
            } else if ("硬座".equals(seatType)) {
                return 3; // 硬座3车
            }
        }
        return 1; // 默认1车
    }
    
    /**
     * 获取该座位类型已売出的数量（用于计算下一个座位号）
     * 通过查询数据库中该列车该座位类型的已売票数
     */
    private int getSoldSeatsCount(Train train, String seatType) {
        // 查询该列车该座位类型的所有有效车票（状态为1）
        List<Ticket> soldTickets = ticketRepository.findByTrainIdAndSeatTypeAndStatus(
            train.getId(), seatType, 1);
        return soldTickets != null ? soldTickets.size() : 0;
    }
    
    @Override
    public Result<List<Order>> searchOrdersByStartStation(Long userId, String startStation) {
        // 获取用户的所有订单（按创建时间倒序）
        List<Order> orders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<OrderDTO> filteredOrders = new ArrayList<>();
        
        // 根据起始站筛选订单
        for (Order order : orders) {
            List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
            
            // 如果有票，检查起始站是否匹配
            if (!tickets.isEmpty()) {
                Ticket firstTicket = tickets.get(0);
                // 验证起始站是否匹配
                if (firstTicket.getStartStation() != null && 
                    (firstTicket.getStartStation().contains(startStation) || 
                     startStation.equals(firstTicket.getStartStation()))) {
                    
                    order.setTicketCount(tickets.size());
                    
                    // 创建扩展的OrderDTO对象
                    OrderDTO orderDTO = new OrderDTO(order);
                    
                    // 从第一张票获取车次和站点信息
                    orderDTO.setTrainNumber(firstTicket.getTrainCode());
                    orderDTO.setTrainType(firstTicket.getTrainType());
                    orderDTO.setStartStation(firstTicket.getStartStation());
                    orderDTO.setEndStation(firstTicket.getEndStation());
                    orderDTO.setDepartureTime(firstTicket.getStartTime());
                    orderDTO.setArrivalTime(firstTicket.getEndTime());
                    
                    filteredOrders.add(orderDTO);
                }
            }
        }
        
        // 创建一个新的Order列表以避免类型转换警告
        List<Order> resultList = new ArrayList<>(filteredOrders);
        return Result.success(resultList);
    }
}
