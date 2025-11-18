package com.mini12306.service.impl;

import com.mini12306.repository.OrderRepository;
import com.mini12306.repository.TicketRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.repository.StationRepository;
import com.mini12306.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据服务实现
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private StationRepository stationRepository;

    /**
     * 获取系统统计数据
     */
    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 订单总数
        long orderCount = orderRepository.count();
        stats.put("orderCount", orderCount);

        // 车票总数 
        long ticketCount = ticketRepository.count();
        stats.put("ticketCount", ticketCount);

        // 列车总数
        long trainCount = trainRepository.count();
        stats.put("trainCount", trainCount);

        // 车站总数
        long stationCount = stationRepository.count();
        stats.put("stationCount", stationCount);

        return stats;
    }

    /**
     * 获取订单统计数据
     */
    @Override
    public Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 今日订单数
        long todayOrderCount = orderRepository.countTodayOrders();
        stats.put("todayOrderCount", todayOrderCount);

        // 昨日订单数
        long yesterdayOrderCount = orderRepository.countYesterdayOrders();
        stats.put("yesterdayOrderCount", yesterdayOrderCount);

        // 本月订单数
        long monthOrderCount = orderRepository.countMonthOrders();
        stats.put("monthOrderCount", monthOrderCount);

        // 订单总数
        long totalOrderCount = orderRepository.count();
        stats.put("totalOrderCount", totalOrderCount);

        return stats;
    }

    /**
     * 获取热门车次统计数据
     */
    @Override
    public Map<String, Object> getPopularTrainStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 最热门列车Top5
        stats.put("popularTrains", trainRepository.findPopularTrains());

        // 最热门车站Top5
        stats.put("popularStations", stationRepository.findPopularStations());

        return stats;
    }
}
