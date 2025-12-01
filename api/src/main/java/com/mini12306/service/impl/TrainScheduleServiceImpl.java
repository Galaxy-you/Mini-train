package com.mini12306.service.impl;

import com.mini12306.model.Result;
import com.mini12306.model.TrainSchedule;
import com.mini12306.model.Train;
import com.mini12306.repository.TrainScheduleRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.service.TrainScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 车次日程服务实现类
 */
@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Override
    public Result<?> listTrainSchedules(Long trainId, String travelDate, Integer status, PageRequest pageRequest) {
        Specification<TrainSchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (trainId != null) {
                predicates.add(cb.equal(root.get("trainId"), trainId));
            }

            if (StringUtils.hasText(travelDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = sdf.parse(travelDate);
                    predicates.add(cb.equal(root.get("travelDate"), new Date(date.getTime())));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<TrainSchedule> page = trainScheduleRepository.findAll(spec, pageRequest);

            // 转换为包含列车信息的DTO
            Page<Map<String, Object>> dtoPage = page.map(this::convertToDTO);

            return Result.success("查询成功", dtoPage);
        } catch (Exception e) {
            return Result.fail("查询车次日程列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> getTrainSchedulesByTrainId(Long trainId) {
        try {
            List<TrainSchedule> schedules = trainScheduleRepository.findByTrainId(trainId);

            List<Map<String, Object>> dtoList = new ArrayList<>();
            for (TrainSchedule schedule : schedules) {
                dtoList.add(convertToDTO(schedule));
            }

            return Result.success("查询成功", dtoList);
        } catch (Exception e) {
            return Result.fail("查询车次日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> getTrainScheduleDetail(Long id) {
        Optional<TrainSchedule> scheduleOpt = trainScheduleRepository.findById(id);
        if (!scheduleOpt.isPresent()) {
            return Result.fail("车次日程不存在");
        }

        return Result.success("查询成功", convertToDTO(scheduleOpt.get()));
    }

    @Override
    @Transactional
    public Result<?> addTrainSchedule(Map<String, Object> scheduleData) {
        try {
            TrainSchedule schedule = new TrainSchedule();

            schedule.setTrainId(getLongValue(scheduleData.get("trainId")));
            schedule.setTravelDate(parseDate(scheduleData.get("travelDate")));
            schedule.setStatus(getIntegerValue(scheduleData.get("status"), 1)); // 默认状态为1(正常)
            schedule.setCreateTime(new java.util.Date());
            schedule.setUpdateTime(new java.util.Date());

            TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);

            return Result.success("添加车次日程成功", convertToDTO(savedSchedule));
        } catch (Exception e) {
            return Result.fail("添加车次日程失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> batchAddTrainSchedules(Long trainId, List<String> travelDates) {
        try {
            List<TrainSchedule> schedules = new ArrayList<>();

            for (String dateStr : travelDates) {
                TrainSchedule schedule = new TrainSchedule();
                schedule.setTrainId(trainId);
                schedule.setTravelDate(parseDate(dateStr));
                schedule.setStatus(1); // 正常状态
                schedule.setCreateTime(new java.util.Date());
                schedule.setUpdateTime(new java.util.Date());

                schedules.add(schedule);
            }

            List<TrainSchedule> savedSchedules = trainScheduleRepository.saveAll(schedules);

            return Result.success("批量添加车次日程成功", savedSchedules.size());
        } catch (Exception e) {
            return Result.fail("批量添加车次日程失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> updateTrainSchedule(Long id, Map<String, Object> scheduleData) {
        Optional<TrainSchedule> scheduleOpt = trainScheduleRepository.findById(id);
        if (!scheduleOpt.isPresent()) {
            return Result.fail("车次日程不存在");
        }

        try {
            TrainSchedule schedule = scheduleOpt.get();

            if (scheduleData.containsKey("travelDate")) {
                schedule.setTravelDate(parseDate(scheduleData.get("travelDate")));
            }
            if (scheduleData.containsKey("status")) {
                schedule.setStatus(getIntegerValue(scheduleData.get("status"), 1));
            }

            schedule.setUpdateTime(new java.util.Date());

            TrainSchedule updatedSchedule = trainScheduleRepository.save(schedule);

            return Result.success("更新车次日程成功", convertToDTO(updatedSchedule));
        } catch (Exception e) {
            return Result.fail("更新车次日程失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> deleteTrainSchedule(Long id) {
        Optional<TrainSchedule> scheduleOpt = trainScheduleRepository.findById(id);
        if (!scheduleOpt.isPresent()) {
            return Result.fail("车次日程不存在");
        }

        try {
            trainScheduleRepository.deleteById(id);
            return Result.success("删除车次日程成功");
        } catch (Exception e) {
            return Result.fail("删除车次日程失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> deleteTrainSchedulesByTrainId(Long trainId) {
        try {
            trainScheduleRepository.deleteByTrainId(trainId);
            return Result.success("删除列车所有日程成功");
        } catch (Exception e) {
            return Result.fail("删除列车所有日程失败：" + e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 将TrainSchedule实体转换为DTO
     */
    private Map<String, Object> convertToDTO(TrainSchedule schedule) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", schedule.getId());
        dto.put("trainId", schedule.getTrainId());
        dto.put("travelDate", schedule.getTravelDate() != null ? schedule.getTravelDate().toString() : null);
        dto.put("status", schedule.getStatus());
        dto.put("statusText", schedule.getStatus() == 1 ? "正常" : "停运");
        dto.put("createTime", schedule.getCreateTime());
        dto.put("updateTime", schedule.getUpdateTime());

        // 查询列车信息
        if (schedule.getTrainId() != null) {
            Optional<Train> trainOpt = trainRepository.findById(schedule.getTrainId());
            if (trainOpt.isPresent()) {
                Train train = trainOpt.get();
                dto.put("trainCode", train.getCode());
                dto.put("trainType", train.getType());
                dto.put("startStation", train.getStartStation());
                dto.put("endStation", train.getEndStation());
                dto.put("startTime", train.getStartTime());
                dto.put("endTime", train.getEndTime());
            }
        }

        return dto;
    }

    /**
     * 解析日期字符串为Date对象
     */
    private Date parseDate(Object dateValue) {
        if (dateValue == null) {
            return null;
        }

        if (dateValue instanceof Date) {
            return (Date) dateValue;
        }

        try {
            String dateStr = dateValue.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = sdf.parse(dateStr);
            return new Date(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地获取Long值
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }

    /**
     * 安全地获取Integer值
     */
    private Integer getIntegerValue(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return defaultValue;
    }
}

