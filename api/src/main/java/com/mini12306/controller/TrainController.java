package com.mini12306.controller;

import com.mini12306.dto.TrainDetailDTO;
import com.mini12306.model.Result;
import com.mini12306.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 列车控制器
 */
@RestController
@RequestMapping("/api/train")
public class TrainController {
    
    @Autowired
    private TrainService trainService;
    
    /**
     * 查询所有列车
     */
    @GetMapping
    public Result<List<TrainDetailDTO>> listAllTrains() {
        return trainService.listAllTrains();
    }
    
    /**
     * 根据起始站和终点站查询列车
     */
    @GetMapping("/search")
    public Result<List<TrainDetailDTO>> searchTrains(@RequestParam String startStation, @RequestParam String endStation) {
        return trainService.searchTrains(startStation, endStation);
    }
    
    /**
     * 获取列车详情
     */
    @GetMapping("/{id}")
    public Result<TrainDetailDTO> getTrainDetail(@PathVariable Long id) {
        return trainService.getTrainDetail(id);
    }
}
