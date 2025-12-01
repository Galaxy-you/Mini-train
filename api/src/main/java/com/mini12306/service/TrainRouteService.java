package com.mini12306.service;

import com.mini12306.model.Result;
import org.springframework.data.domain.PageRequest;

import java.util.Map;

/**
 * 列车路线服务接口
 */
public interface TrainRouteService {

    /**
     * 分页查询列车路线列表
     */
    Result<?> listTrainRoutes(Long trainId, Long stationId, PageRequest pageRequest);

    /**
     * 根据列车ID获取完整路线
     */
    Result<?> getTrainRoutesByTrainId(Long trainId);

    /**
     * 根据ID获取路线详情
     */
    Result<?> getTrainRouteDetail(Long id);

    /**
     * 添加列车路线
     */
    Result<?> addTrainRoute(Map<String, Object> routeData);

    /**
     * 批量添加列车路线
     */
    Result<?> batchAddTrainRoutes(Long trainId, java.util.List<Map<String, Object>> routeDataList);

    /**
     * 更新列车路线
     */
    Result<?> updateTrainRoute(Long id, Map<String, Object> routeData);

    /**
     * 删除列车路线
     */
    Result<?> deleteTrainRoute(Long id);

    /**
     * 删除列车的所有路线
     */
    Result<?> deleteTrainRoutesByTrainId(Long trainId);
}

