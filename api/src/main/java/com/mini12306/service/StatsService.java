package com.mini12306.service;

import java.util.Map;

/**
 * 统计数据服务接口
 */
public interface StatsService {

    /**
     * 获取系统统计数据
     * 包含:订单总数、车票总数、列车总数、车站总数等
     */
    Map<String, Object> getSystemStats();

    /**
     * 获取订单统计数据
     * 包含:今日订单数、昨日订单数、本月订单数、订单总数等
     */
    Map<String, Object> getOrderStats();

    /**
     * 获取热门车次统计数据
     * 包含:最热门列车Top5、最热门车站Top5等
     */
    Map<String, Object> getPopularTrainStats();
}
