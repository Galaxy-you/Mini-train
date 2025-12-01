package com.mini12306.service;

import com.mini12306.model.Result;
import org.springframework.data.domain.PageRequest;

import java.util.Map;

/**
 * 车次日程服务接口
 */
public interface TrainScheduleService {

    /**
     * 分页查询车次日程列表
     */
    Result<?> listTrainSchedules(Long trainId, String travelDate, Integer status, PageRequest pageRequest);

    /**
     * 根据列车ID获取所有日程
     */
    Result<?> getTrainSchedulesByTrainId(Long trainId);

    /**
     * 根据ID获取日程详情
     */
    Result<?> getTrainScheduleDetail(Long id);

    /**
     * 添加车次日程
     */
    Result<?> addTrainSchedule(Map<String, Object> scheduleData);

    /**
     * 批量添加车次日程（为列车添加多个运行日期）
     */
    Result<?> batchAddTrainSchedules(Long trainId, java.util.List<String> travelDates);

    /**
     * 更新车次日程
     */
    Result<?> updateTrainSchedule(Long id, Map<String, Object> scheduleData);

    /**
     * 删除车次日程
     */
    Result<?> deleteTrainSchedule(Long id);

    /**
     * 删除列车的所有日程
     */
    Result<?> deleteTrainSchedulesByTrainId(Long trainId);
}

