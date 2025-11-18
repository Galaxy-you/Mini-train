package com.mini12306.repository;

import com.mini12306.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * 列车数据访问接口
 */
public interface TrainRepository extends JpaRepository<Train, Long>, JpaSpecificationExecutor<Train> {
    /**
     * 基于站点名称查询列车
     * 保留原有方法以兼容现有代码
     */
    @Query("SELECT t FROM Train t WHERE t.startStation = ?1 AND t.endStation = ?2")
    List<Train> findByStations(String startStation, String endStation);
    
    /**
     * 基于站点ID查询列车
     */
    @Query("SELECT t FROM Train t WHERE t.startStationId = :startStationId AND t.endStationId = :endStationId")
    List<Train> findByStationIds(@Param("startStationId") Long startStationId, @Param("endStationId") Long endStationId);
    
    /**
     * 查询经过某个站点（作为起点）的所有列车
     */
    @Query("SELECT t FROM Train t WHERE t.startStationId = :stationId")
    List<Train> findByStartStationId(@Param("stationId") Long stationId);
    
    /**
     * 查询经过某个站点（作为终点）的所有列车
     */
    @Query("SELECT t FROM Train t WHERE t.endStationId = :stationId")
    List<Train> findByEndStationId(@Param("stationId") Long stationId);
    
    /**
     * 根据列车类型查询
     */
    List<Train> findByType(String type);

    /**
     * 根据车次编号查询
     */
    List<Train> findByCode(String code);

    /**
     * 查询热门车次Top5
     * 通过关联车票表计算售票量
     */
    @Query(nativeQuery = true, value = 
        "SELECT t.id, t.code, t.type, t.start_station, t.end_station, COUNT(tk.id) as ticket_count " +
        "FROM train t LEFT JOIN ticket tk ON t.id = tk.train_id " +
        "GROUP BY t.id " +
        "ORDER BY ticket_count DESC " +
        "LIMIT 5")
    List<Map<String, Object>> findPopularTrains();
}
