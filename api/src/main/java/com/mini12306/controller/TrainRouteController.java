package com.mini12306.controller;

import com.mini12306.model.Result;
import com.mini12306.service.TrainRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 列车路线控制器
 */
@RestController
@RequestMapping("/api/train-route")
public class TrainRouteController {
    
    @Autowired
    private TrainRouteService trainRouteService;
    
    /**
     * 根据列车ID获取完整路线
     */
    @GetMapping("/train/{trainId}")
    public Result<?> getTrainRoutes(@PathVariable Long trainId) {
        return trainRouteService.getTrainRoutesByTrainId(trainId);
    }
}
