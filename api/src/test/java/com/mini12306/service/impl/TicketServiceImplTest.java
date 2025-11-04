package com.mini12306.service.impl;

// import com.mini12306.dto.ChangeRuleDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TicketServiceImpl 单元测试类
 */
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 创建模拟Ticket对象
     */
    private Ticket createMockTicket(Long id, String ticketNo, Long userId, Long passengerId, Long orderId, Integer status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTicketNo(ticketNo);
        ticket.setUserId(userId);
        ticket.setPassengerId(passengerId);
        ticket.setOrderId(orderId);
        ticket.setStatus(status); // 1表示有效票
        ticket.setTrainId(1L);
        ticket.setTrainCode("G101");
        ticket.setTrainType("高铁");
        ticket.setStartStationId(1L);
        ticket.setEndStationId(2L);
        ticket.setStartStation("北京南");
        ticket.setEndStation("上海虹桥");
        ticket.setStartCity("北京");
        ticket.setEndCity("上海");
        ticket.setSeatType("一等座");
        ticket.setSeatInfo("12车14A号");
        
        Money price = new Money("553.50");
        ticket.setPrice(price);
        
        ticket.setTravelDate(new Date());
        ticket.setStartTime("08:00");
        ticket.setEndTime("13:00");
        ticket.setDuration(300); // 5小时
        ticket.setCreateTime(new Date());
        ticket.setUpdateTime(new Date());
        
        return ticket;
    }

    /**
     * 创建模拟Train对象
     */
    private Train createMockTrain(Long id, String code, String type, Long startStationId, Long endStationId) {
        Train train = new Train();
        train.setId(id);
        train.setCode(code);
        train.setType(type);
        train.setStartStationId(startStationId);
        train.setEndStationId(endStationId);
        train.setStartStation("北京南");
        train.setEndStation("上海虹桥");
        train.setStartTime("08:00");
        train.setEndTime("13:00");
        train.setDuration(300);
        train.setSeatCount(50);
        
        Money price = new Money("553.50");
        train.setPrice(price);
        
        train.setCreateTime(new Date());
        train.setUpdateTime(new Date());
        
        return train;
    }

    /**
     * 创建模拟Station对象
     */
    private Station createMockStation(Long id, String name, String code, String city) {
        Station station = new Station();
        station.setId(id);
        station.setName(name);
        station.setCode(code);
        station.setCity(city);
        station.setType("高铁站");
        return station;
    }

    /**
     * 创建模拟Order对象
     */
    private Order createMockOrder(Long id, String orderNo, Long userId) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        
        Money total = new Money("553.50");
        order.setTotalAmount(total);
        
        order.setStatus("已支付");
        order.setCreateTime(new Date());
        order.setPayTime(new Date());
        
        return order;
    }

    @Test
    @DisplayName("测试查询用户购买的所有票")
    public void testListUserBoughtTickets() {
        // 准备测试数据
        Long userId = 1L;
        List<Ticket> mockTickets = Arrays.asList(
            createMockTicket(1L, "T2023060800001", userId, 10L, 100L, 1),
            createMockTicket(2L, "T2023060800002", userId, 11L, 100L, 1)
        );
        
        // 模拟优化查询方法返回空，触发传统查询方式
        when(ticketRepository.findTicketDetailsByUserId(userId)).thenReturn(Collections.emptyList());
        when(ticketRepository.findByUserId(userId)).thenReturn(mockTickets);
        
        // 执行被测试的方法
        Result<List<TicketDetailDTO>> result = ticketService.listUserBoughtTickets(userId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findTicketDetailsByUserId(userId);
        verify(ticketRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("测试查询用户购买的所有票 - 优化查询方式")
    public void testListUserBoughtTickets_OptimizedQuery() {
        // 准备测试数据
        Long userId = 1L;
        
        // 创建车票
        Ticket ticket1 = createMockTicket(1L, "T2023060800001", userId, 10L, 100L, 1);
        Ticket ticket2 = createMockTicket(2L, "T2023060800002", userId, 11L, 101L, 1);
        
        // 创建TicketDetailDTO列表，通过现有的构造函数
        List<TicketDetailDTO> mockDetails = Arrays.asList(
            new TicketDetailDTO(ticket1),
            new TicketDetailDTO(ticket2)
        );
        
        // 模拟优化查询方法直接返回结果
        when(ticketRepository.findTicketDetailsByUserId(userId)).thenReturn(mockDetails);
        
        // 执行被测试的方法
        Result<List<TicketDetailDTO>> result = ticketService.listUserBoughtTickets(userId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findTicketDetailsByUserId(userId);
        // 不应该调用传统查询方法
        verify(ticketRepository, never()).findByUserId(userId);
    }

    @Test
    @DisplayName("测试查询乘车人的所有票")
    public void testListUserTickets() {
        // 准备测试数据
        Long passengerId = 10L;
        List<Ticket> mockTickets = Arrays.asList(
            createMockTicket(1L, "T2023060800001", 1L, passengerId, 100L, 1),
            createMockTicket(2L, "T2023060800002", 1L, passengerId, 101L, 1)
        );
        
        when(ticketRepository.findByPassengerId(passengerId)).thenReturn(mockTickets);
        
        // 执行被测试的方法
        Result<List<TicketDetailDTO>> result = ticketService.listUserTickets(passengerId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByPassengerId(passengerId);
    }

    @Test
    @DisplayName("测试取消车票成功")
    public void testCancelTicket_Success() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, userId, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        Order mockOrder = createMockOrder(orderId, "O2023060800001", userId);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByOrderIdAndStatus(orderId, 1)).thenReturn(Collections.emptyList());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("取消车票成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getStatus()); // 票状态已更新为取消(0)
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, times(1)).findById(mockTicket.getTrainId());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).save(any(Train.class));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(ticketRepository, times(1)).findByOrderIdAndStatus(orderId, 1);
    }

    @Test
    @DisplayName("测试取消车票失败 - 车票不存在")
    public void testCancelTicket_Failed_TicketNotExist() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票不存在", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    @DisplayName("测试取消车票失败 - 无权限")
    public void testCancelTicket_Failed_NoPermission() {
        // 准备测试数据
        Long userId = 1L;
        Long wrongUserId = 2L;
        String ticketNo = "T2023060800001";
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, wrongUserId, 10L, 100L, 1); // 属于用户2的票
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo); // 用户1尝试取消
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("无权取消该车票", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    @DisplayName("测试取消车票失败 - 票状态不正确")
    public void testCancelTicket_Failed_InvalidStatus() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, userId, 10L, 100L, 0); // 已取消的票
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票已改签或退票", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    @DisplayName("测试获取车票详情")
    public void testGetTicketDetail() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(ticketNo, result.getData().getTicketNo());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
    }

    @Test
    @DisplayName("测试获取车票详情 - 票不存在")
    public void testGetTicketDetail_NotExist() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票不存在", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
    }

    @Test
    @DisplayName("测试获取车票详情 - 站点名称从ticket获取")
    public void testGetTicketDetail_StationNameFromTicket() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartStation(null); // 设置为null，触发查询相关实体
        mockTicket.setEndStation(null);
        
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        
        // 创建站点，但不设置名称
        Station startStation = createMockStation(1L, null, "BJP", "北京");
        Station endStation = createMockStation(2L, null, "SHH", "上海");
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(stationRepository.findById(mockTicket.getStartStationId())).thenReturn(Optional.of(startStation));
        when(stationRepository.findById(mockTicket.getEndStationId())).thenReturn(Optional.of(endStation));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, times(1)).findById(mockTicket.getTrainId());
        verify(stationRepository, times(1)).findById(mockTicket.getStartStationId());
        verify(stationRepository, times(1)).findById(mockTicket.getEndStationId());
    }
    
    @Test
    @DisplayName("测试获取车票详情 - 站点ID为空但车票有完整信息")
    public void testGetTicketDetail_NullStationIds() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartStationId(null); // 设置站点ID为null
        mockTicket.setEndStationId(null);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("北京南", result.getData().getStartStation());
        assertEquals("上海虹桥", result.getData().getEndStation());
        assertEquals("G101", result.getData().getTrainCode());
        
        // 验证方法调用 - 当车票有完整信息时，不应该查询其他实体
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, never()).findById(anyLong());
        verify(stationRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("测试获取车票详情 - 车票信息不完整需要查询实体")
    public void testGetTicketDetail_IncompleteTicketInfo() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        // 清空车票的某些信息，强制查询相关实体
        mockTicket.setStartStation(null);
        mockTicket.setEndStation(null);
        
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        Station startStation = createMockStation(1L, "北京南", "BJP", "北京");
        Station endStation = createMockStation(2L, "上海虹桥", "SHH", "上海");
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(stationRepository.findById(mockTicket.getStartStationId())).thenReturn(Optional.of(startStation));
        when(stationRepository.findById(mockTicket.getEndStationId())).thenReturn(Optional.of(endStation));
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("北京南", result.getData().getStartStation());
        assertEquals("上海虹桥", result.getData().getEndStation());
        
        // 验证方法调用 - 当车票信息不完整时，需要查询相关实体
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, times(1)).findById(mockTicket.getTrainId());
        verify(stationRepository, times(1)).findById(mockTicket.getStartStationId());
        verify(stationRepository, times(1)).findById(mockTicket.getEndStationId());
    }

    @Test
    @DisplayName("测试获取车票详情 - 站点查询返回为空")
    public void testGetTicketDetail_StationQueryEmpty() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartStation(null); // 设置为null，触发查询相关实体
        mockTicket.setEndStation(null);
        
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(stationRepository.findById(mockTicket.getStartStationId())).thenReturn(Optional.empty());
        when(stationRepository.findById(mockTicket.getEndStationId())).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, times(1)).findById(mockTicket.getTrainId());
        verify(stationRepository, times(1)).findById(mockTicket.getStartStationId());
        verify(stationRepository, times(1)).findById(mockTicket.getEndStationId());
    }
    
    @Test
    @DisplayName("测试获取车票详情 - 所有字段均使用备选值")
    public void testGetTicketDetail_AllFieldsFallback() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartStation(null);
        mockTicket.setEndStation(null);
        mockTicket.setTrainCode(null);
        mockTicket.setSeatInfo(null);
        
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(stationRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.getTicketDetail(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(trainRepository, times(1)).findById(mockTicket.getTrainId());
    }

    @Test
    @DisplayName("测试分页查询车票列表")
    @SuppressWarnings("unchecked")
    public void testListTickets() {
        // 准备测试数据
        String trainCode = "G101";
        String startStation = "北京南";
        String endStation = "上海虹桥";
        String passengerName = "张三";
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        List<Ticket> mockTickets = Collections.singletonList(
            createMockTicket(1L, "T2023060800001", 1L, 10L, 100L, 1)
        );
        Page<Ticket> page = new PageImpl<>(mockTickets, pageRequest, 1);
        
        when(ticketRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(page);
        
        // 执行被测试的方法
        Result<?> result = ticketService.listTickets(trainCode, startStation, endStation, passengerName, pageRequest);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("测试分页查询车票列表 - 异常情况")
    @SuppressWarnings("unchecked")
    public void testListTickets_Exception() {
        // 准备测试数据
        String trainCode = "G101";
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        // 模拟异常情况
        when(ticketRepository.findAll(any(Specification.class), eq(pageRequest)))
            .thenThrow(new RuntimeException("数据库查询错误"));
        
        // 执行被测试的方法
        Result<?> result = ticketService.listTickets(trainCode, null, null, null, pageRequest);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("查询车票列表失败"));
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("测试更新车票信息")
    public void testUpdateTicket() {
        // 准备测试数据
        Long ticketId = 1L;
        Map<String, Object> ticketInfo = new HashMap<>();
        ticketInfo.put("seatType", "二等座");
        ticketInfo.put("seatInfo", "11车22B号");
        
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, 100L, 1);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 执行被测试的方法
        Result<?> result = ticketService.updateTicket(ticketId, ticketInfo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("更新车票成功", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        
        // 验证更新后的值
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals("二等座", savedTicket.getSeatType());
        assertEquals("11车22B号", savedTicket.getSeatInfo());
    }
    
    @Test
    @DisplayName("测试更新车票信息失败 - 票不存在")
    public void testUpdateTicket_NotExist() {
        // 准备测试数据
        Long ticketId = 999L;
        Map<String, Object> ticketInfo = new HashMap<>();
        ticketInfo.put("seatType", "二等座");
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<?> result = ticketService.updateTicket(ticketId, ticketInfo);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票不存在", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    @DisplayName("测试管理后台取消车票")
    public void testCancelTicketById() {
        // 准备测试数据
        Long ticketId = 1L;
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        Order mockOrder = createMockOrder(orderId, "O2023060800001", 1L);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());
        
        // 执行被测试的方法
        Result<?> result = ticketService.cancelTicket(ticketId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("取消车票成功", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).findById(anyLong());
        verify(trainRepository, times(1)).save(any(Train.class));
        
        // 验证车票状态已更改
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket cancelledTicket = ticketCaptor.getValue();
        assertEquals(0, cancelledTicket.getStatus()); // 0表示已取消
    }
    
    @Test
    @DisplayName("测试管理后台取消车票 - 车票不存在")
    public void testCancelTicketById_TicketNotExist() {
        // 准备测试数据
        Long ticketId = 999L;
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<?> result = ticketService.cancelTicket(ticketId);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票不存在", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    @DisplayName("测试管理后台取消车票 - 车票状态无效")
    public void testCancelTicketById_InvalidStatus() {
        // 准备测试数据
        Long ticketId = 1L;
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, 100L, 0); // 已取消状态
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<?> result = ticketService.cancelTicket(ticketId);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("车票已改签或退票", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    @DisplayName("测试管理后台取消车票 - 订单不存在")
    public void testCancelTicketById_OrderNotExist() {
        // 准备测试数据
        Long ticketId = 1L;
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty()); // 订单不存在
        when(ticketRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());
        
        // 执行被测试的方法
        Result<?> result = ticketService.cancelTicket(ticketId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("取消车票成功", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class)); // 由于订单不存在，不应调用保存
    }
    
    @Test
    @DisplayName("测试管理后台取消车票 - 存在其他有效票")
    public void testCancelTicketById_WithOtherValidTickets() {
        // 准备测试数据
        Long ticketId = 1L;
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        Order mockOrder = createMockOrder(orderId, "O2023060800001", 1L);
        List<Ticket> otherTickets = Collections.singletonList(
            createMockTicket(2L, "T2023060800002", 1L, 11L, orderId, 1)  // 同一订单下的另一张有效票
        );
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByOrderIdAndStatus(orderId, 1)).thenReturn(otherTickets);
        
        // 执行被测试的方法
        Result<?> result = ticketService.cancelTicket(ticketId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).findById(anyLong());
        verify(trainRepository, times(1)).save(any(Train.class));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(ticketRepository, times(1)).findByOrderIdAndStatus(orderId, 1);
        
        // 验证订单状态和金额更新
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertNotEquals("已取消", savedOrder.getStatus()); // 订单不应被取消，因为还有有效票
    }
    
    @Test
    @DisplayName("测试更新车票信息 - 更新状态字段")
    public void testUpdateTicket_UpdateStatus() {
        // 准备测试数据
        Long ticketId = 1L;
        Map<String, Object> ticketInfo = new HashMap<>();
        ticketInfo.put("status", 2); // 设置状态为2（例如：已检票）
        
        Ticket mockTicket = createMockTicket(ticketId, "T2023060800001", 1L, 10L, 100L, 1);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 执行被测试的方法
        Result<?> result = ticketService.updateTicket(ticketId, ticketInfo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        
        // 验证更新的值
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals(2, savedTicket.getStatus());
    }
    
    @Test
    @DisplayName("测试 updateOrderAfterCancelTicket 方法 - 存在无效票时的总金额计算")
    public void testUpdateOrderAfterCancelTicket_WithInvalidTickets() {
        // 准备测试数据
        Long orderId = 100L;
        Order mockOrder = createMockOrder(orderId, "O2023060800001", 1L);
        
        List<Ticket> validTickets = Arrays.asList(
            createMockTicket(1L, "T2023060800001", 1L, 10L, orderId, 1)  // 有效票
        );
        
        Ticket invalidTicket = createMockTicket(2L, "T2023060800002", 1L, 11L, orderId, 0);
        invalidTicket.setPrice(null);  // 设置价格为null测试边界情况
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(ticketRepository.findByOrderIdAndStatus(orderId, 1)).thenReturn(validTickets);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 创建一个新的测试实例以便我们可以直接调用private方法
        TicketServiceImpl testInstance = new TicketServiceImpl();
        // 使用反射注入依赖
        try {
            java.lang.reflect.Field orderRepoField = TicketServiceImpl.class.getDeclaredField("orderRepository");
            orderRepoField.setAccessible(true);
            orderRepoField.set(testInstance, orderRepository);
            
            java.lang.reflect.Field ticketRepoField = TicketServiceImpl.class.getDeclaredField("ticketRepository");
            ticketRepoField.setAccessible(true);
            ticketRepoField.set(testInstance, ticketRepository);
            
            // 使用反射调用私有方法
            java.lang.reflect.Method method = TicketServiceImpl.class.getDeclaredMethod("updateOrderAfterCancelTicket", Long.class);
            method.setAccessible(true);
            method.invoke(testInstance, orderId);
            
            // 验证方法调用
            verify(orderRepository, times(1)).findById(orderId);
            verify(ticketRepository, times(1)).findByOrderIdAndStatus(orderId, 1);
            verify(orderRepository, times(1)).save(any(Order.class));
            
            // 验证更新的订单金额
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(new BigDecimal("553.50"), savedOrder.getTotalAmount().getAmount());
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试取消车票 - 有多个票且金额计算")
    public void testCancelTicket_MultipleTicketsWithAmount() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, userId, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        Order mockOrder = createMockOrder(orderId, "O2023060800001", userId);
        
        // 另一张同订单下的有效票
        Ticket otherTicket = createMockTicket(2L, "T2023060800002", userId, 11L, orderId, 1);
        Money price = new Money("300.00"); // 不同的票价
        otherTicket.setPrice(price);
        
        List<Ticket> validTickets = Collections.singletonList(otherTicket);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByOrderIdAndStatus(orderId, 1)).thenReturn(validTickets);
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        
        // 验证订单金额被正确重新计算
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order updatedOrder = orderCaptor.getValue();
        assertEquals(new BigDecimal("300.00"), updatedOrder.getTotalAmount().getAmount());
    }
    
    @Test
    @DisplayName("测试取消车票 - 订单不存在的情况")
    public void testCancelTicket_OrderNotExist() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, userId, 10L, orderId, 1);
        Train mockTrain = createMockTrain(1L, "G101", "高铁", 1L, 2L);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.of(mockTrain));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainRepository.save(any(Train.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty()); // 订单不存在
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess()); // 取消车票应成功
        assertEquals("取消车票成功", result.getMessage());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).findById(any());
        verify(trainRepository, times(1)).save(any(Train.class));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class)); // 订单不存在，不应保存
    }
    
    @Test
    @DisplayName("测试取消车票 - 列车不存在的情况")
    public void testCancelTicket_TrainNotExist() {
        // 准备测试数据
        Long userId = 1L;
        String ticketNo = "T2023060800001";
        Long orderId = 100L;
        
        Ticket mockTicket = createMockTicket(1L, ticketNo, userId, 10L, orderId, 1);
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        when(trainRepository.findById(mockTicket.getTrainId())).thenReturn(Optional.empty()); // 列车不存在
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.cancelTicket(userId, ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess()); // 虽然列车不存在，但取消票务仍应成功
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(ticketNo);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(trainRepository, times(1)).findById(any());
        verify(trainRepository, never()).save(any(Train.class)); // 列车不存在，不应保存
    }

    @Test
    @DisplayName("测试获取退票规则 - 车票价格对象为null")
    public void testGetRefundRule_NullPriceObject() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setPrice(null); // 设置价格为null
        
        // 设置出发时间为未来48小时
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 48); 
        Date travelDate = cal.getTime();
        mockTicket.setTravelDate(travelDate);
        mockTicket.setStartTime("12:00");
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<RefundRuleDTO> result = ticketService.getRefundRule(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        // 即使车票价格为null，方法也应该正常执行
        assertTrue(result.getData().isCanRefund());
        assertEquals(BigDecimal.ZERO, result.getData().getOriginalPrice());
    }
    
    @Test
    @DisplayName("测试获取退票规则 - 票价为null的情况")
    public void testGetRefundRule_NullPrice() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        // 创建一个Money实例但金额为null
        Money emptyMoney = new Money();
        emptyMoney.setAmount(null);
        mockTicket.setPrice(emptyMoney);
        
        // 设置出发时间为未来48小时
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 48); 
        Date travelDate = cal.getTime();
        mockTicket.setTravelDate(travelDate);
        mockTicket.setStartTime("12:00");
        mockTicket.setDuration(300); // 设置行程时长为5小时
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法
        Result<RefundRuleDTO> result = ticketService.getRefundRule(ticketNo);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        // 即使车票价格为null，方法也应该正常执行
        assertTrue(result.getData().isCanRefund());
        assertEquals(BigDecimal.ZERO, result.getData().getOriginalPrice());
        assertEquals(new BigDecimal("0.00"), result.getData().getFee());
        assertEquals(new BigDecimal("0.00"), result.getData().getRefundableAmount());
        assertNotNull(result.getData());
        // 即使车票价格为null，方法也应该正常执行
        assertTrue(result.getData().isCanRefund());
        assertEquals(BigDecimal.ZERO, result.getData().getOriginalPrice()); // 修改为期望BigDecimal.ZERO
    }
    
    @Test
    @DisplayName("测试获取改签规则 - 时间解析异常")
    public void testCheckChangeTicket_TimeParseException() {
        // 准备测试数据
        String ticketNo = "TICKET202506061005550534";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartTime("invalid:time"); // 设置无效时间格式
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法 - 应该抛出IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.checkChangeTicket(ticketNo);
        });
    }

    @Test
    @DisplayName("测试updateOrderAfterCancelTicket方法 - 订单为空")
    public void testUpdateOrderAfterCancelTicket_NullOrder() {
        // 准备模拟对象
        Long orderId = 999L;
        
        // 模拟订单不存在
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法 - 直接通过反射调用private方法
        try {
            java.lang.reflect.Method method = TicketServiceImpl.class.getDeclaredMethod("updateOrderAfterCancelTicket", Long.class);
            method.setAccessible(true);
            method.invoke(ticketService, orderId);
            
            // 验证方法调用
            verify(orderRepository, times(1)).findById(orderId);
            verify(orderRepository, never()).save(any(Order.class));
        } catch (Exception e) {
            fail("不应抛出异常: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试获取退票规则 - 时间解析异常")
    public void testGetRefundRule_TimeParseException() {
        // 准备测试数据
        String ticketNo = "T2023060800001";
        Ticket mockTicket = createMockTicket(1L, ticketNo, 1L, 10L, 100L, 1);
        mockTicket.setStartTime("invalid:time"); // 设置无效时间格式
        
        when(ticketRepository.findByTicketNo(ticketNo)).thenReturn(Optional.of(mockTicket));
        
        // 执行被测试的方法 - 应该抛出IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.getRefundRule(ticketNo);
        });
    }
    
    @Test
    @DisplayName("测试改签车票 - 站点信息获取异常处理")
    public void testChangeTicket_StationError() {
        // 准备测试数据
        Long userId = 1L;
        String originalTicketNo = "T2023060800001";
        Long targetTrainId = 2L;
        Long newStartStationId = 3L;
        Long newEndStationId = 4L;
        
        ChangeTicketRequest request = new ChangeTicketRequest();
        request.setOriginalTicketNo(originalTicketNo);
        request.setNewTrainId(targetTrainId);
        request.setNewSeatType("二等座");
        request.setStartStationId(newStartStationId);
        request.setEndStationId(newEndStationId);
        request.setTravelDate("2025-06-09");
        
        // 原车票
        Ticket originalTicket = createMockTicket(1L, originalTicketNo, userId, 10L, 100L, 1);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 24);
        Date travelDate = cal.getTime();
        originalTicket.setTravelDate(travelDate);
        originalTicket.setStartTime("12:00");
        
        // 目标车次
        Train targetTrain = createMockTrain(targetTrainId, "G102", "高铁", 1L, 2L);
        
        when(ticketRepository.findByTicketNo(originalTicketNo)).thenReturn(Optional.of(originalTicket));
        when(trainRepository.findById(targetTrainId)).thenReturn(Optional.of(targetTrain));
        // 模拟站点查询失败
        when(stationRepository.findById(newStartStationId)).thenReturn(Optional.empty());
        when(stationRepository.findById(newEndStationId)).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket savedTicket = invocation.getArgument(0);
            if (savedTicket.getId() == null) {
                savedTicket.setId(3L);
                savedTicket.setTicketNo("T2023060800003");
            }
            return savedTicket;
        });
        
        // 执行被测试的方法
        Result<TicketDetailDTO> result = ticketService.changeTicket(userId, request);
        
        // 验证结果 - 即使站点不存在，应该使用默认值而不是失败
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        // 验证方法调用
        verify(ticketRepository, times(1)).findByTicketNo(originalTicketNo);
        verify(trainRepository, times(1)).findById(targetTrainId);
        verify(stationRepository, times(1)).findById(newStartStationId);
        verify(stationRepository, times(1)).findById(newEndStationId);
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }
}
