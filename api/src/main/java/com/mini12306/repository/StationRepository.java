package com.mini12306.repository;

import com.mini12306.model.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 站点数据访问接口
 */
public interface StationRepository extends JpaRepository<Station, Long>, JpaSpecificationExecutor<Station> {
    
    /**
     * 根据站点名称查询站点
     */
    Optional<Station> findByName(String name);
    
    /**
     * 根据站点代码查询站点
     */
    Optional<Station> findByCode(String code);
    
    /**
     * 根据城市查询所有站点
     */
    List<Station> findByCity(String city);
    
    /**
     * 根据省份查询所有站点
     */
    List<Station> findByProvince(String province);
    
    /**
     * 根据站点名称模糊查询站点(分页)
     */
    @Query("SELECT s FROM Station s WHERE s.name LIKE %?1%")
    Page<Station> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 根据城市模糊查询站点(分页)
     */
    @Query("SELECT s FROM Station s WHERE s.city LIKE %?1%")
    Page<Station> findByCityContaining(String city, Pageable pageable);

    /**
     * 根据站点名称模糊查询站点(不分页)
     */
    @Query("SELECT s FROM Station s WHERE s.name LIKE %?1%")
    List<Station> findByNameContaining(String name);
    
    /**
     * 根据城市模糊查询站点(不分页)
     */
    @Query("SELECT s FROM Station s WHERE s.city LIKE %?1%")
    List<Station> findByCityContaining(String city);

    /**
     * 查询热门车站Top5
     * 通过关联车票表统计发站到达站的累计量
     */
    @Query(nativeQuery = true, value = 
        "SELECT s.id, s.name, s.city, COUNT(tk.id) as usage_count " +
        "FROM station s " +
        "LEFT JOIN ticket tk ON s.id = tk.start_station_id OR s.id = tk.end_station_id " +
        "GROUP BY s.id " +
        "ORDER BY usage_count DESC " +
        "LIMIT 5")
    List<Map<String, Object>> findPopularStations();
}
