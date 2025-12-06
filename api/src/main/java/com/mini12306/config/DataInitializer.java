package com.mini12306.config;

import com.mini12306.model.Money;
import com.mini12306.model.Train;
import com.mini12306.model.Station;
import com.mini12306.model.Account;
import com.mini12306.repository.TrainRepository;
import com.mini12306.repository.StationRepository;
import com.mini12306.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 数据初始化
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        // 初始化列车数据
        if (trainRepository.count() == 0) {
            Train train1 = new Train();
            train1.setCode("G1234");
            train1.setType("高铁");
            train1.setStartStation("北京站");
            train1.setEndStation("上海站");
            train1.setStartTime("08:00");
            train1.setEndTime("13:00");
            train1.setSeatCount(300);
            train1.setPrice(new Money(553.5));
            train1.setCreateTime(new Date());
            train1.setUpdateTime(new Date());
            trainRepository.save(train1);
            
            Train train2 = new Train();
            train2.setCode("D5678");
            train2.setType("动车");
            train2.setStartStation("北京站");
            train2.setEndStation("广州站");
            train2.setStartTime("10:30");
            train2.setEndTime("18:30");
            train2.setSeatCount(200);
            train2.setPrice(new Money(219.5));
            train2.setCreateTime(new Date());
            train2.setUpdateTime(new Date());
            trainRepository.save(train2);
            
            Train train3 = new Train();
            train3.setCode("K9876");
            train3.setType("快车");
            train3.setStartStation("上海站");
            train3.setEndStation("广州站");
            train3.setStartTime("14:20");
            train3.setEndTime("次日06:20");
            train3.setSeatCount(100);
            train3.setPrice(new Money(112.0));
            train3.setCreateTime(new Date());
            train3.setUpdateTime(new Date());
            trainRepository.save(train3);
        }
        
        // 初始化站点数据
        if (stationRepository.count() == 0) {
            // 初始化北京站
            Station station1 = new Station();
            station1.setName("北京站");
            station1.setCode("100000");
            station1.setCity("北京");
            station1.setCreateTime(parseDate("2025-06-05 13:07:20"));
            station1.setUpdateTime(parseDate("2025-06-05 13:07:20"));
            stationRepository.save(station1);
            
            // 初始化广州站
            Station station2 = new Station();
            station2.setName("广州站");
            station2.setCode("510000");
            station2.setCity("广州");
            station2.setCreateTime(parseDate("2025-06-05 13:07:53"));
            station2.setUpdateTime(parseDate("2025-06-05 13:07:53"));
            stationRepository.save(station2);
            
            // 初始化上海站
            Station station3 = new Station();
            station3.setName("上海站");
            station3.setCode("200000");
            station3.setCity("上海");
            station3.setCreateTime(parseDate("2025-06-05 13:08:17"));
            station3.setUpdateTime(parseDate("2025-06-05 13:08:17"));
            stationRepository.save(station3);
            
            // 初始化武昌站
            Station station4 = new Station();
            station4.setName("武昌站");
            station4.setCode("430061");
            station4.setCity("武汉");
            station4.setCreateTime(parseDate("2025-06-05 13:09:04"));
            station4.setUpdateTime(parseDate("2025-06-05 13:09:04"));
            stationRepository.save(station4);
            
            // 初始化沈阳北站
            Station station5 = new Station();
            station5.setName("沈阳北站");
            station5.setCode("110000");
            station5.setCity("沈阳");
            station5.setCreateTime(parseDate("2025-06-05 13:10:08"));
            station5.setUpdateTime(parseDate("2025-06-05 13:10:08"));
            stationRepository.save(station5);
            
            // 初始化广州白云站
            Station station6 = new Station();
            station6.setName("广州白云站");
            station6.setCode("510400");
            station6.setCity("广州");
            station6.setCreateTime(parseDate("2025-06-05 13:11:04"));
            station6.setUpdateTime(parseDate("2025-06-05 13:11:04"));
            stationRepository.save(station6);
            
            // 更新列车表中的站点ID
            updateTrainStationIds();
        }
        
        // 初始化用户账户数据
        if (accountRepository.count() == 0) {
            Account account = new Account();
            account.setUsername("test_user");
            account.setPassword("$2a$10$Tr9Ddw.25NLt.5FNqpn1nOcwab6hEWULsflw2KuQ3iA8AzV7JXWVC");
            account.setRealName("张三");
            account.setCardId("341003200512341111");
            account.setPhone("19512345678");
            account.setAuthStatus(1);
            account.setCreateTime(parseDate("2025-12-01 8:00:00"));
            account.setUpdateTime(parseDate("2025-12-01 8:00:00"));
            accountRepository.save(account);
        }
    }
    
    /**
     * 解析日期字符串为Date对象
     */
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
    
    /**
     * 更新列车表中的站点ID
     */
    private void updateTrainStationIds() {
        for (Train train : trainRepository.findAll()) {
            // 查找起始站和终点站
            stationRepository.findByName(train.getStartStation()).ifPresent(station -> {
                train.setStartStationId(station.getId());
            });
            
            stationRepository.findByName(train.getEndStation()).ifPresent(station -> {
                train.setEndStationId(station.getId());
            });
            
            // 保存更新后的列车信息
            trainRepository.save(train);
        }
    }
}
