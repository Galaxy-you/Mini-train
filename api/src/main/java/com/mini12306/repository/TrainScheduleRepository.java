package com.mini12306.repository;

import com.mini12306.model.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

/**
 * 车次日程数据访问接口
 */
@Repository
public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long>, JpaSpecificationExecutor<TrainSchedule> {

    /**
     * 根据列车ID查询所有日程
     */
    List<TrainSchedule> findByTrainId(Long trainId);

    /**
     * 根据列车ID和日期查询
     */
    TrainSchedule findByTrainIdAndTravelDate(Long trainId, Date travelDate);

    /**
     * 根据日期查询所有运行的车次
     */
    List<TrainSchedule> findByTravelDate(Date travelDate);

    /**
     * 根据日期和状态查询
     */
    List<TrainSchedule> findByTravelDateAndStatus(Date travelDate, Integer status);

    /**
     * 根据列车ID删除所有日程
     */
    void deleteByTrainId(Long trainId);
}

