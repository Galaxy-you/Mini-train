package com.mini12306.repository;

import com.mini12306.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 按用户ID查询
     */
    List<Order> findByUserId(Long userId);

    /**
     * 统计用户订单数量
     */
    long countByUserId(Long userId);

    /**
     * 按订单号查询
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 查询今日订单数
     */
    @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(create_time) = CURDATE()", nativeQuery = true)
    long countTodayOrders();

    /**
     * 查询昨日订单数
     */
    @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)", nativeQuery = true)
    long countYesterdayOrders();

    /**
     * 查询本月订单数
     */
    @Query(value = "SELECT COUNT(*) FROM orders WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE())", nativeQuery = true)
    long countMonthOrders();

    /**
     * 查询最新订单列表
     */
    @Query("SELECT o FROM Order o ORDER BY o.createTime DESC")
    List<Order> findLatestOrders();
}
