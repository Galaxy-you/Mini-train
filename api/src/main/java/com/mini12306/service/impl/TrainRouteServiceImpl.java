package com.mini12306.service.impl;

import com.mini12306.model.Result;
import com.mini12306.model.TrainRoute;
import com.mini12306.model.Station;
import com.mini12306.model.Train;
import com.mini12306.repository.TrainRouteRepository;
import com.mini12306.repository.StationRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.service.TrainRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 列车路线服务实现类
 */
@Service
public class TrainRouteServiceImpl implements TrainRouteService {

    @Autowired
    private TrainRouteRepository trainRouteRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private StationRepository stationRepository;

    @Override
    public Result<?> listTrainRoutes(Long trainId, Long stationId, PageRequest pageRequest) {
        Specification<TrainRoute> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (trainId != null) {
                predicates.add(cb.equal(root.get("trainId"), trainId));
            }

            if (stationId != null) {
                predicates.add(cb.equal(root.get("stationId"), stationId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        try {
            Page<TrainRoute> page = trainRouteRepository.findAll(spec, pageRequest);

            // 转换为包含站点和列车信息的DTO
            Page<Map<String, Object>> dtoPage = page.map(this::convertToDTO);

            return Result.success("查询成功", dtoPage);
        } catch (Exception e) {
            return Result.fail("查询列车路线列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> getTrainRoutesByTrainId(Long trainId) {
        try {
            List<TrainRoute> routes = trainRouteRepository.findByTrainIdOrderByStationOrderAsc(trainId);

            List<Map<String, Object>> dtoList = new ArrayList<>();
            for (TrainRoute route : routes) {
                dtoList.add(convertToDTO(route));
            }

            return Result.success("查询成功", dtoList);
        } catch (Exception e) {
            return Result.fail("查询列车路线失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> getTrainRouteDetail(Long id) {
        Optional<TrainRoute> routeOpt = trainRouteRepository.findById(id);
        if (!routeOpt.isPresent()) {
            return Result.fail("列车路线不存在");
        }

        return Result.success("查询成功", convertToDTO(routeOpt.get()));
    }

    @Override
    @Transactional
    public Result<?> addTrainRoute(Map<String, Object> routeData) {
        try {
            TrainRoute route = new TrainRoute();

            route.setTrainId(getLongValue(routeData.get("trainId")));
            route.setStationId(getLongValue(routeData.get("stationId")));
            route.setStationOrder(getIntegerValue(routeData.get("stationOrder")));
            route.setArriveTime(parseTime(routeData.get("arriveTime")));
            route.setDepartTime(parseTime(routeData.get("departTime")));
            route.setStopTime(getIntegerValue(routeData.get("stopTime")));
            route.setDistance(getIntegerValue(routeData.get("distance")));
            route.setCreateTime(new Date());
            route.setUpdateTime(new Date());

            TrainRoute savedRoute = trainRouteRepository.save(route);

            return Result.success("添加列车路线成功", convertToDTO(savedRoute));
        } catch (Exception e) {
            return Result.fail("添加列车路线失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> batchAddTrainRoutes(Long trainId, List<Map<String, Object>> routeDataList) {
        try {
            List<TrainRoute> routes = new ArrayList<>();

            for (Map<String, Object> routeData : routeDataList) {
                TrainRoute route = new TrainRoute();
                route.setTrainId(trainId);
                route.setStationId(getLongValue(routeData.get("stationId")));
                route.setStationOrder(getIntegerValue(routeData.get("stationOrder")));
                route.setArriveTime(parseTime(routeData.get("arriveTime")));
                route.setDepartTime(parseTime(routeData.get("departTime")));
                route.setStopTime(getIntegerValue(routeData.get("stopTime")));
                route.setDistance(getIntegerValue(routeData.get("distance")));
                route.setCreateTime(new Date());
                route.setUpdateTime(new Date());

                routes.add(route);
            }

            List<TrainRoute> savedRoutes = trainRouteRepository.saveAll(routes);

            return Result.success("批量添加列车路线成功", savedRoutes.size());
        } catch (Exception e) {
            return Result.fail("批量添加列车路线失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> updateTrainRoute(Long id, Map<String, Object> routeData) {
        Optional<TrainRoute> routeOpt = trainRouteRepository.findById(id);
        if (!routeOpt.isPresent()) {
            return Result.fail("列车路线不存在");
        }

        try {
            TrainRoute route = routeOpt.get();

            if (routeData.containsKey("stationId")) {
                route.setStationId(getLongValue(routeData.get("stationId")));
            }
            if (routeData.containsKey("stationOrder")) {
                route.setStationOrder(getIntegerValue(routeData.get("stationOrder")));
            }
            if (routeData.containsKey("arriveTime")) {
                route.setArriveTime(parseTime(routeData.get("arriveTime")));
            }
            if (routeData.containsKey("departTime")) {
                route.setDepartTime(parseTime(routeData.get("departTime")));
            }
            if (routeData.containsKey("stopTime")) {
                route.setStopTime(getIntegerValue(routeData.get("stopTime")));
            }
            if (routeData.containsKey("distance")) {
                route.setDistance(getIntegerValue(routeData.get("distance")));
            }

            route.setUpdateTime(new Date());

            TrainRoute updatedRoute = trainRouteRepository.save(route);

            return Result.success("更新列车路线成功", convertToDTO(updatedRoute));
        } catch (Exception e) {
            return Result.fail("更新列车路线失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> deleteTrainRoute(Long id) {
        Optional<TrainRoute> routeOpt = trainRouteRepository.findById(id);
        if (!routeOpt.isPresent()) {
            return Result.fail("列车路线不存在");
        }

        try {
            trainRouteRepository.deleteById(id);
            return Result.success("删除列车路线成功");
        } catch (Exception e) {
            return Result.fail("删除列车路线失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> deleteTrainRoutesByTrainId(Long trainId) {
        try {
            trainRouteRepository.deleteByTrainId(trainId);
            return Result.success("删除列车所有路线成功");
        } catch (Exception e) {
            return Result.fail("删除列车所有路线失败：" + e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 将TrainRoute实体转换为DTO
     */
    private Map<String, Object> convertToDTO(TrainRoute route) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", route.getId());
        dto.put("trainId", route.getTrainId());
        dto.put("stationId", route.getStationId());
        dto.put("stationOrder", route.getStationOrder());
        dto.put("arriveTime", route.getArriveTime() != null ? route.getArriveTime().toString() : null);
        dto.put("departTime", route.getDepartTime() != null ? route.getDepartTime().toString() : null);
        dto.put("stopTime", route.getStopTime());
        dto.put("distance", route.getDistance());
        dto.put("createTime", route.getCreateTime());
        dto.put("updateTime", route.getUpdateTime());

        // 查询列车信息
        if (route.getTrainId() != null) {
            Optional<Train> trainOpt = trainRepository.findById(route.getTrainId());
            if (trainOpt.isPresent()) {
                Train train = trainOpt.get();
                dto.put("trainCode", train.getCode());
                dto.put("trainType", train.getType());
            }
        }

        // 查询站点信息
        if (route.getStationId() != null) {
            Optional<Station> stationOpt = stationRepository.findById(route.getStationId());
            if (stationOpt.isPresent()) {
                Station station = stationOpt.get();
                dto.put("stationName", station.getName());
                dto.put("stationCity", station.getCity());
            }
        }

        return dto;
    }

    /**
     * 解析时间字符串为Time对象
     */
    private Time parseTime(Object timeValue) {
        if (timeValue == null) {
            return null;
        }

        if (timeValue instanceof Time) {
            return (Time) timeValue;
        }

        try {
            String timeStr = timeValue.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            java.util.Date date = sdf.parse(timeStr);
            return new Time(date.getTime());
        } catch (Exception e) {
            // 尝试只有HH:mm格式
            try {
                String timeStr = timeValue.toString();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                java.util.Date date = sdf.parse(timeStr);
                return new Time(date.getTime());
            } catch (Exception ex) {
                return null;
            }
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
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return null;
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
        return null;
    }
}

