package com.mini12306.controller;

import com.mini12306.model.Result;
import com.mini12306.model.Station;
import com.mini12306.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站点控制器
 */
@RestController
@RequestMapping("/api/station")
public class StationController {
    
    @Autowired
    private StationService stationService;
    
    /**
     * 获取所有站点
     */
    @GetMapping
    public Result<List<Station>> getAllStations() {
        return stationService.getAllStations();
    }
    
    /**
     * 根据ID获取站点
     */
    @GetMapping("/{id}")
    public Result<Station> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id);
    }
    
    /**
     * 根据名称获取站点
     */
    @GetMapping("/name")
    public Result<Station> getStationByName(@RequestParam String name) {
        return stationService.getStationByName(name);
    }
    
    /**
     * 根据城市获取站点列表
     */
    @GetMapping("/city")
    public Result<List<Station>> getStationsByCity(@RequestParam String city) {
        return stationService.getStationsByCity(city);
    }
    
    /**
     * 根据省份获取站点列表
     */
    @GetMapping("/province")
    public Result<List<Station>> getStationsByProvince(@RequestParam String province) {
        return stationService.getStationsByProvince(province);
    }
    
    /**
     * 搜索站点
     */
    @GetMapping("/search")
    public Result<List<Station>> searchStations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city) {
        
        if (name != null && !name.isEmpty()) {
            return stationService.searchStationsByName(name);
        } else if (city != null && !city.isEmpty()) {
            return stationService.searchStationsByCity(city);
        } else {
            return Result.fail("请提供搜索条件");
        }
    }
}
