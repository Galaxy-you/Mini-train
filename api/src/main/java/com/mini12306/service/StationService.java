package com.mini12306.service;

import com.mini12306.model.Result;
import com.mini12306.model.Station;

import java.util.List;

/**
 * 站点服务接口
 */
public interface StationService {
    
    /**
     * 获取所有站点
     */
    Result<List<Station>> getAllStations();
    
    /**
     * 根据ID获取站点
     */
    Result<Station> getStationById(Long id);
    
    /**
     * 根据名称获取站点
     */
    Result<Station> getStationByName(String name);
    
    /**
     * 根据城市获取站点列表
     */
    Result<List<Station>> getStationsByCity(String city);
    
    /**
     * 根据省份获取站点列表
     */
    Result<List<Station>> getStationsByProvince(String province);
    
    /**
     * 根据名称模糊搜索站点
     */
    Result<List<Station>> searchStationsByName(String keyword);
    
    /**
     * 根据城市模糊搜索站点
     */
    Result<List<Station>> searchStationsByCity(String keyword);
}
