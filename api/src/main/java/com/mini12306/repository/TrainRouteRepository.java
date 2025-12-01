package com.mini12306.repository;

import com.mini12306.model.TrainRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 列车路线数据访问接口
 */
@Repository
public interface TrainRouteRepository extends JpaRepository<TrainRoute, Long>, JpaSpecificationExecutor<TrainRoute> {

    /**
     * 根据列车ID查询路线，按站点顺序排序
     */
    List<TrainRoute> findByTrainIdOrderByStationOrderAsc(Long trainId);

    /**
     * 根据列车ID和站点ID查询
     */
    TrainRoute findByTrainIdAndStationId(Long trainId, Long stationId);

    /**
     * 根据站点ID查询所有经过该站点的路线
     */
    List<TrainRoute> findByStationId(Long stationId);

    /**
     * 根据列车ID删除所有路线
     */
    void deleteByTrainId(Long trainId);
}

