package com.mini12306.service.impl;

import com.mini12306.model.Result;
import com.mini12306.model.Station;
import com.mini12306.repository.StationRepository;
import com.mini12306.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 站点服务实现
 */
@Service
public class StationServiceImpl implements StationService {
    
    @Autowired
    private StationRepository stationRepository;
    
    @Override
    public Result<List<Station>> getAllStations() {
        List<Station> stations = stationRepository.findAll();
        return Result.success(stations);
    }
    
    @Override
    public Result<Station> getStationById(Long id) {
        Optional<Station> stationOpt = stationRepository.findById(id);
        if (!stationOpt.isPresent()) {
            return Result.fail("站点不存在");
        }
        return Result.success(stationOpt.get());
    }
    
    @Override
    public Result<Station> getStationByName(String name) {
        Optional<Station> stationOpt = stationRepository.findByName(name);
        if (!stationOpt.isPresent()) {
            return Result.fail("站点不存在");
        }
        return Result.success(stationOpt.get());
    }
    
    @Override
    public Result<List<Station>> getStationsByCity(String city) {
        List<Station> stations = stationRepository.findByCity(city);
        return Result.success(stations);
    }
    
    @Override
    public Result<List<Station>> getStationsByProvince(String province) {
        List<Station> stations = stationRepository.findByProvince(province);
        return Result.success(stations);
    }
    
    @Override
    public Result<List<Station>> searchStationsByName(String keyword) {
        List<Station> stations = stationRepository.findByNameContaining(keyword);
        return Result.success(stations);
    }
    
    @Override
    public Result<List<Station>> searchStationsByCity(String keyword) {
        List<Station> stations = stationRepository.findByCityContaining(keyword);
        return Result.success(stations);
    }
}
