package com.mini12306.service.impl;

import com.mini12306.dto.TrainDetailDTO;
import com.mini12306.model.Result;
import com.mini12306.model.Station;
import com.mini12306.model.Train;
import com.mini12306.model.Money;
import com.mini12306.repository.StationRepository;
import com.mini12306.repository.TrainRepository;
import com.mini12306.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.*;

/**
 * 列车服务实现
 */
@Service
public class TrainServiceImpl implements TrainService {
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Override
    public Result<List<TrainDetailDTO>> listAllTrains() {
        List<Train> trains = trainRepository.findAll();
        List<TrainDetailDTO> trainDetails = convertToTrainDetails(trains);
        return Result.success(trainDetails);
    }
    
    @Override
    public Result<List<TrainDetailDTO>> searchTrains(String startStation, String endStation) {
        // 先尝试通过站点名称查询相应的Station实体
        Optional<Station> startStationOpt = stationRepository.findByName(startStation);
        Optional<Station> endStationOpt = stationRepository.findByName(endStation);
        
        List<Train> trains;
        
        if (startStationOpt.isPresent() && endStationOpt.isPresent()) {
            // 如果找到对应的Station实体，则使用站点ID查询
            Station start = startStationOpt.get();
            Station end = endStationOpt.get();
            trains = trainRepository.findByStationIds(start.getId(), end.getId());
        } else {
            // 兼容旧代码，使用站点名称查询
            trains = trainRepository.findByStations(startStation, endStation);
        }
        
        List<TrainDetailDTO> trainDetails = convertToTrainDetails(trains);
        return Result.success(trainDetails);
    }
    
    @Override
    public Result<TrainDetailDTO> getTrainDetail(Long trainId) {
        Optional<Train> trainOpt = trainRepository.findById(trainId);
        if (!trainOpt.isPresent()) {
            return Result.fail("列车不存在");
        }
        
        Train train = trainOpt.get();
        TrainDetailDTO trainDetail = createTrainDetail(train);
        return Result.success(trainDetail);
    }
    
    @Override
    public Result<?> listStations(String name, String city, PageRequest pageRequest) {
        Specification<Station> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            
            if (StringUtils.hasText(city)) {
                predicates.add(cb.like(root.get("city"), "%" + city + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        try {
            Page<Station> page = stationRepository.findAll(spec, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("查询站点列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<?> addStation(Map<String, Object> stationInfo) {
        Station station = new Station();
        
        // 检查必要参数
        if (!stationInfo.containsKey("name") || !stationInfo.containsKey("code")) {
            return Result.fail("站点名称和编码不能为空");
        }

        // 检查名称和编码是否已存在
        if (stationRepository.findByName((String)stationInfo.get("name")).isPresent()) {
            return Result.fail("站点名称已存在");
        }
        if (stationRepository.findByCode((String)stationInfo.get("code")).isPresent()) {
            return Result.fail("站点编码已存在");
        }

        station.setName((String)stationInfo.get("name"));
        station.setCode((String)stationInfo.get("code"));
        station.setProvince((String)stationInfo.get("province"));
        station.setCity((String)stationInfo.get("city"));
        station.setCreateTime(new Date());
        station.setUpdateTime(new Date());

        stationRepository.save(station);
        return Result.success("添加站点成功");
    }

    @Override
    @Transactional
    public Result<?> updateStation(Long id, Map<String, Object> stationInfo) {
        Optional<Station> stationOpt = stationRepository.findById(id);
        if (!stationOpt.isPresent()) {
            return Result.fail("站点不存在");
        }

        Station station = stationOpt.get();

        // 检查名称和编码是否已存在
        if (stationInfo.containsKey("name")) {
            Optional<Station> existingStation = stationRepository.findByName((String)stationInfo.get("name"));
            if (existingStation.isPresent() && !existingStation.get().getId().equals(id)) {
                return Result.fail("站点名称已存在");
            }
            station.setName((String)stationInfo.get("name"));
        }

        if (stationInfo.containsKey("code")) {
            Optional<Station> existingStation = stationRepository.findByCode((String)stationInfo.get("code"));
            if (existingStation.isPresent() && !existingStation.get().getId().equals(id)) {
                return Result.fail("站点编码已存在");
            }
            station.setCode((String)stationInfo.get("code"));
        }

        if (stationInfo.containsKey("province")) {
            station.setProvince((String)stationInfo.get("province"));
        }

        if (stationInfo.containsKey("city")) {
            station.setCity((String)stationInfo.get("city"));
        }

        station.setUpdateTime(new Date());
        stationRepository.save(station);
        return Result.success("更新站点成功");
    }

    @Override
    @Transactional
    public Result<?> deleteStation(Long id) {
        Optional<Station> stationOpt = stationRepository.findById(id);
        if (!stationOpt.isPresent()) {
            return Result.fail("站点不存在");
        }

        // 检查是否有列车使用此站点
        List<Train> startingTrains = trainRepository.findByStartStationId(id);
        List<Train> endingTrains = trainRepository.findByEndStationId(id);

        if (!startingTrains.isEmpty() || !endingTrains.isEmpty()) {
            return Result.fail("该站点正在被列车使用,无法删除");
        }

        stationRepository.deleteById(id);
        return Result.success("删除站点成功");
    }

    @Override
    public Result<?> listTrains(String code, String type, PageRequest pageRequest) {
        Specification<Train> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(code)) {
                predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            }
            
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Train> page = trainRepository.findAll(spec, pageRequest);
        Page<TrainDetailDTO> dtoPage = page.map(train -> createTrainDetail(train));
        return Result.success(dtoPage);
    }

    /**
     * 处理站点名称，移除"站"字（如果有）
     */
    private String formatStationName(String name) {
        if (name == null) return null;
        return name.endsWith("站") ? name.substring(0, name.length() - 1) : name;
    }

    @Override
    @Transactional
    public Result<?> addTrain(Map<String, Object> trainInfo) {
        Train train = new Train();
        
        // 检查必要参数
        if (!trainInfo.containsKey("code") || !trainInfo.containsKey("type")) {
            return Result.fail("列车编号和类型不能为空");
        }

        // 检查编号是否已存在
        List<Train> existingTrains = trainRepository.findByCode((String)trainInfo.get("code"));
        if (!existingTrains.isEmpty()) {
            return Result.fail("列车编号已存在");
        }

        train.setCode((String)trainInfo.get("code"));
        train.setType((String)trainInfo.get("type"));
        train.setStartStation(formatStationName((String)trainInfo.get("startStation")));
        train.setEndStation(formatStationName((String)trainInfo.get("endStation")));
        train.setStartTime((String)trainInfo.get("startTime"));
        train.setEndTime((String)trainInfo.get("endTime"));
        train.setDuration(Integer.valueOf(trainInfo.get("duration").toString()));
        train.setPrice(new Money(Double.valueOf(trainInfo.get("price").toString())));
        train.setSeatCount(Integer.valueOf(trainInfo.get("seatCount").toString()));
        train.setCreateTime(new Date());
        train.setUpdateTime(new Date());

        trainRepository.save(train);
        return Result.success("添加列车成功");
    }

    @Override
    @Transactional
    public Result<?> updateTrain(Long id, Map<String, Object> trainInfo) {
        Optional<Train> trainOpt = trainRepository.findById(id);
        if (!trainOpt.isPresent()) {
            return Result.fail("列车不存在");
        }

        Train train = trainOpt.get();

        if (trainInfo.containsKey("code")) {
            List<Train> existingTrains = trainRepository.findByCode((String)trainInfo.get("code"));
            if (!existingTrains.isEmpty() && !existingTrains.get(0).getId().equals(id)) {
                return Result.fail("列车编号已存在");
            }
            train.setCode((String)trainInfo.get("code"));
        }

        if (trainInfo.containsKey("type")) {
            train.setType((String)trainInfo.get("type"));
        }
        if (trainInfo.containsKey("startStation")) {
            train.setStartStation((String)trainInfo.get("startStation"));
        }
        if (trainInfo.containsKey("endStation")) {
            train.setEndStation((String)trainInfo.get("endStation"));
        }
        if (trainInfo.containsKey("startTime")) {
            train.setStartTime((String)trainInfo.get("startTime"));
        }
        if (trainInfo.containsKey("endTime")) {
            train.setEndTime((String)trainInfo.get("endTime"));
        }
        if (trainInfo.containsKey("duration")) {
            train.setDuration(Integer.valueOf(trainInfo.get("duration").toString()));
        }
        if (trainInfo.containsKey("price")) {
            train.setPrice(new Money(Double.valueOf(trainInfo.get("price").toString())));
        }
        if (trainInfo.containsKey("seatCount")) {
            train.setSeatCount(Integer.valueOf(trainInfo.get("seatCount").toString()));
        }

        train.setUpdateTime(new Date());
        trainRepository.save(train);
        return Result.success("更新列车成功");
    }

    @Override
    @Transactional
    public Result<?> deleteTrain(Long id) {
        Optional<Train> trainOpt = trainRepository.findById(id);
        if (!trainOpt.isPresent()) {
            return Result.fail("列车不存在"); 
        }

        trainRepository.deleteById(id);
        return Result.success("删除列车成功");
    }

    /**
     * 将Train实体列表转换为TrainDetailDTO列表
     */
    private List<TrainDetailDTO> convertToTrainDetails(List<Train> trains) {
        List<TrainDetailDTO> trainDetails = new ArrayList<>();
        
        for (Train train : trains) {
            TrainDetailDTO trainDetail = createTrainDetail(train);
            trainDetails.add(trainDetail);
        }
        
        return trainDetails;
    }
    
    /**
     * 创建单个TrainDetailDTO
     */
    private TrainDetailDTO createTrainDetail(Train train) {
        TrainDetailDTO trainDetail = TrainDetailDTO.fromTrain(train);
        
        // 获取站点详细信息
        if (train.getStartStationId() != null && train.getEndStationId() != null) {
            Optional<Station> startStationOpt = stationRepository.findById(train.getStartStationId());
            Optional<Station> endStationOpt = stationRepository.findById(train.getEndStationId());
            
            if (startStationOpt.isPresent()) {
                Station startStation = startStationOpt.get();
                trainDetail.setStartStation(startStation.getName());
            }
            
            if (endStationOpt.isPresent()) {
                Station endStation = endStationOpt.get();
                trainDetail.setEndStation(endStation.getName());
            }
        }
        
        // 添加座位信息
        trainDetail.setSeatInfo(generateSeatInfo(train));
        
        return trainDetail;
    }
    
    /**
     * 生成座位信息
     */
    private List<TrainDetailDTO.SeatInfoDTO> generateSeatInfo(Train train) {
        List<TrainDetailDTO.SeatInfoDTO> seatInfo = new ArrayList<>();
        
        // 根据列车类型生成不同座位类型
        if ("高铁".equals(train.getType())) {
            // 商务座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "商务座", train.getPrice().multiply(1.5), train.getSeatCount() / 5));
            
            // 一等座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "一等座", train.getPrice().multiply(1.2), train.getSeatCount() / 3));
            
            // 二等座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "二等座", train.getPrice(), train.getSeatCount() / 2));
        } else if ("动车".equals(train.getType())) {
            // 一等座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "一等座", train.getPrice().multiply(1.2), train.getSeatCount() / 3));
            
            // 二等座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "二等座", train.getPrice(), train.getSeatCount() * 2 / 3));
        } else {
            // 普通列车
            // 硬卧
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "硬卧", train.getPrice().multiply(1.2), train.getSeatCount() / 3));
            
            // 硬座
            seatInfo.add(new TrainDetailDTO.SeatInfoDTO(
                "硬座", train.getPrice(), train.getSeatCount() * 2 / 3));
        }
        
        return seatInfo;
    }
}
