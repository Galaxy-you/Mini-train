package com.mini12306.repository;

import com.mini12306.model.Ticket;
import com.mini12306.dto.TicketDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 车票数据访问接口
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    /**
     * 查询用户的所有车票
     */
    List<Ticket> findByUserId(Long userId);
    
    /**
     * 统计用户车票数量
     */
    long countByUserId(Long userId);

    /**
     * 查询乘车人的所有车票
     */
    List<Ticket> findByPassengerId(Long passengerId);
    
    /**
     * 查询订单的所有车票
     */
    List<Ticket> findByOrderId(Long orderId);
    
    /**
     * 根据车票编号查询车票
     */
    Optional<Ticket> findByTicketNo(String ticketNo);
    
    /**
     * 查询用户的有效车票
     */
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.status = 1")
    List<Ticket> findValidTicketsByUserId(@Param("userId") Long userId);
    
    /**
     * 高效查询用户的所有车票详情（包含关联信息）
     */
    @Query("SELECT NEW com.mini12306.dto.TicketDetailDTO(t) " +
           "FROM Ticket t " +
           "WHERE t.userId = :userId " +
           "ORDER BY t.createTime DESC")
    List<TicketDetailDTO> findTicketDetailsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询订单的所有有效车票
     */
    List<Ticket> findByOrderIdAndStatus(Long orderId, Integer status);
    
    /**
     * 根据出发日期查询用户的车票
     */
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.travelDate = :travelDate")
    List<Ticket> findByUserIdAndTravelDate(@Param("userId") Long userId, @Param("travelDate") Date travelDate);
    
    /**
     * 根据列车ID和旅行日期查询车票数量，用于座位余量计算
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.trainId = :trainId AND t.travelDate = :travelDate AND t.status = 1")
    Long countByTrainIdAndTravelDate(@Param("trainId") Long trainId, @Param("travelDate") Date travelDate);
    
    /**
     * 根据列车ID、旅行日期和座位类型查询车票数量，用于特定座位类型的余量计算
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.trainId = :trainId AND t.travelDate = :travelDate " +
           "AND t.seatType = :seatType AND t.status = 1")
    Long countByTrainIdAndTravelDateAndSeatType(
        @Param("trainId") Long trainId, 
        @Param("travelDate") Date travelDate, 
        @Param("seatType") String seatType);
}
