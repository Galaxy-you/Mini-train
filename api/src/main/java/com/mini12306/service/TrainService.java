package com.mini12306.service;

import com.mini12306.model.Result;
// import com.mini12306.model.Train;
import com.mini12306.dto.TrainDetailDTO;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

/**
 * 列车服务接口
 */
public interface TrainService {
    /**
     * 查询所有列车
     */
    Result<List<TrainDetailDTO>> listAllTrains();
    
    /**
     * 根据起始站和终点站查询列车
     */
    Result<List<TrainDetailDTO>> searchTrains(String startStation, String endStation);
    
    /**
     * 获取列车详情
     */
    Result<TrainDetailDTO> getTrainDetail(Long trainId);

    /**
     * 分页查询车站列表
     */
    Result<?> listStations(String province, String city, PageRequest pageRequest);

    /**
     * 添加车站
     */
    Result<?> addStation(Map<String, Object> stationInfo);

    /**
     * 更新车站
     */
    Result<?> updateStation(Long id, Map<String, Object> stationInfo);

    /**
     * 删除车站
     */
    Result<?> deleteStation(Long id);

    /**
     * 分页查询列车列表
     */
    Result<?> listTrains(String type, String code, PageRequest pageRequest);

    /**
     * 添加列车
     */
    Result<?> addTrain(Map<String, Object> trainInfo);

    /**
     * 更新列车
     */
    Result<?> updateTrain(Long id, Map<String, Object> trainInfo);

    /**
     * 删除列车
     */
    Result<?> deleteTrain(Long id);
}
