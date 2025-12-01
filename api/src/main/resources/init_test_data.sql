-- ========================================
-- Mini12306 测试数据初始化脚本
-- ========================================

USE Mini12306;

-- ========================================
-- 1. 添加更多车站数据（中国主要城市车站）
-- ========================================

INSERT INTO station (name, code, city, province, type, create_time, update_time) VALUES
-- 华北地区
('天津站', '120000', '天津', '天津市', '高铁站', NOW(), NOW()),
('石家庄站', '050000', '石家庄', '河北省', '高铁站', NOW(), NOW()),
('太原站', '030000', '太原', '山西省', '高铁站', NOW(), NOW()),

-- 华东地区
('南京南站', '210000', '南京', '江苏省', '高铁站', NOW(), NOW()),
('杭州东站', '310000', '杭州', '浙江省', '高铁站', NOW(), NOW()),
('合肥南站', '230000', '合肥', '安徽省', '高铁站', NOW(), NOW()),
('济南西站', '250000', '济南', '山东省', '高铁站', NOW(), NOW()),

-- 华南地区
('深圳北站', '518000', '深圳', '广东省', '高铁站', NOW(), NOW()),
('长沙南站', '410000', '长沙', '湖南省', '高铁站', NOW(), NOW()),
('福州站', '350000', '福州', '福建省', '高铁站', NOW(), NOW()),
('南昌西站', '330000', '南昌', '江西省', '高铁站', NOW(), NOW()),

-- 西南地区
('成都东站', '610000', '成都', '四川省', '高铁站', NOW(), NOW()),
('重庆北站', '400000', '重庆', '重庆市', '高铁站', NOW(), NOW()),
('昆明站', '650000', '昆明', '云南省', '高铁站', NOW(), NOW()),
('贵阳北站', '550000', '贵阳', '贵州省', '高铁站', NOW(), NOW()),

-- 西北地区
('西安北站', '710000', '西安', '陕西省', '高铁站', NOW(), NOW()),
('兰州站', '730000', '兰州', '甘肃省', '高铁站', NOW(), NOW()),

-- 东北地区
('哈尔滨西站', '150000', '哈尔滨', '黑龙江省', '高铁站', NOW(), NOW()),
('长春站', '130000', '长春', '吉林省', '高铁站', NOW(), NOW()),
('大连北站', '116000', '大连', '辽宁省', '高铁站', NOW(), NOW());

-- ========================================
-- 2. 添加更多列车数据
-- ========================================

-- 京沪线高铁
INSERT INTO train (code, type, start_station_id, end_station_id, start_station, end_station, start_time, end_time, duration, seat_count, price, create_time, update_time) VALUES
('G1', '高铁', 1, 3, '北京站', '上海站', '07:00', '11:28', 268, 1200, 553.00, NOW(), NOW()),
('G2', '高铁', 3, 1, '上海站', '北京站', '08:05', '12:33', 268, 1200, 553.00, NOW(), NOW()),
('G3', '高铁', 1, 3, '北京站', '上海站', '09:00', '13:35', 275, 1200, 553.00, NOW(), NOW()),

-- 京广线高铁
('G71', '高铁', 1, 2, '北京站', '广州站', '08:00', '16:22', 502, 1200, 862.00, NOW(), NOW()),
('G72', '高铁', 2, 1, '广州站', '北京站', '08:30', '16:52', 502, 1200, 862.00, NOW(), NOW()),

-- 沪广线高铁
('G101', '高铁', 3, 2, '上海站', '广州站', '09:17', '17:06', 469, 1000, 793.50, NOW(), NOW()),
('G102', '高铁', 2, 3, '广州站', '上海站', '10:15', '18:04', 469, 1000, 793.50, NOW(), NOW()),

-- 京哈线高铁
('G381', '高铁', 1, 24, '北京站', '哈尔滨西站', '06:48', '14:26', 458, 800, 635.00, NOW(), NOW()),
('G382', '高铁', 24, 1, '哈尔滨西站', '北京站', '07:15', '14:53', 458, 800, 635.00, NOW(), NOW()),

-- 京沪动车
('D321', '动车', 1, 3, '北京站', '上海站', '13:20', '19:45', 385, 600, 384.00, NOW(), NOW()),
('D322', '动车', 3, 1, '上海站', '北京站', '14:10', '20:35', 385, 600, 384.00, NOW(), NOW()),

-- 成渝高铁
('G8501', '高铁', 18, 19, '成都东站', '重庆北站', '07:46', '09:11', 85, 600, 154.00, NOW(), NOW()),
('G8502', '高铁', 19, 18, '重庆北站', '成都东站', '08:35', '10:00', 85, 600, 154.00, NOW(), NOW()),

-- 武广高铁
('G1001', '高铁', 4, 2, '武昌站', '广州站', '08:02', '11:27', 205, 1000, 463.50, NOW(), NOW()),
('G1002', '高铁', 2, 4, '广州站', '武昌站', '09:03', '12:28', 205, 1000, 463.50, NOW(), NOW());

-- ========================================
-- 3. 添加列车路线数据（train_route）
-- ========================================

-- G1次列车路线（北京-上海）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(4, 1, 1, NULL, '07:00:00', 0, 0, NOW(), NOW()),              -- 北京站（始发）
(4, 7, 2, '08:33:00', '08:35:00', 2, 406, NOW(), NOW()),      -- 天津站
(4, 13, 3, '09:42:00', '09:44:00', 2, 650, NOW(), NOW()),     -- 济南西站
(4, 10, 4, '10:58:00', '11:00:00', 2, 1023, NOW(), NOW()),    -- 南京南站
(4, 3, 5, '11:28:00', NULL, 0, 1318, NOW(), NOW());           -- 上海站（终到）

-- G2次列车路线（上海-北京）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(5, 3, 1, NULL, '08:05:00', 0, 0, NOW(), NOW()),              -- 上海站（始发）
(5, 10, 2, '08:33:00', '08:35:00', 2, 295, NOW(), NOW()),     -- 南京南站
(5, 13, 3, '09:49:00', '09:51:00', 2, 668, NOW(), NOW()),     -- 济南西站
(5, 7, 4, '10:58:00', '11:00:00', 2, 912, NOW(), NOW()),      -- 天津站
(5, 1, 5, '12:33:00', NULL, 0, 1318, NOW(), NOW());           -- 北京站（终到）

-- G71次列车路线（北京-广州）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(7, 1, 1, NULL, '08:00:00', 0, 0, NOW(), NOW()),              -- 北京站（始发）
(7, 8, 2, '10:26:00', '10:28:00', 2, 600, NOW(), NOW()),      -- 石家庄站
(7, 4, 3, '12:44:00', '12:46:00', 2, 1200, NOW(), NOW()),     -- 武昌站
(7, 15, 4, '14:15:00', '14:17:00', 2, 1580, NOW(), NOW()),    -- 长沙南站
(7, 2, 5, '16:22:00', NULL, 0, 2298, NOW(), NOW());           -- 广州站（终到）

-- G101次列车路线（上海-广州）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(9, 3, 1, NULL, '09:17:00', 0, 0, NOW(), NOW()),              -- 上海站（始发）
(9, 11, 2, '11:05:00', '11:07:00', 2, 320, NOW(), NOW()),     -- 杭州东站
(9, 15, 3, '13:36:00', '13:38:00', 2, 880, NOW(), NOW()),     -- 长沙南站
(9, 2, 4, '17:06:00', NULL, 0, 1450, NOW(), NOW());           -- 广州站（终到）

-- G381次列车路线（北京-哈尔滨）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(11, 1, 1, NULL, '06:48:00', 0, 0, NOW(), NOW()),             -- 北京站（始发）
(11, 5, 2, '09:15:00', '09:17:00', 2, 680, NOW(), NOW()),     -- 沈阳北站
(11, 25, 3, '11:38:00', '11:40:00', 2, 950, NOW(), NOW()),    -- 长春站
(11, 24, 4, '14:26:00', NULL, 0, 1240, NOW(), NOW());         -- 哈尔滨西站（终到）

-- G8501次列车路线（成都-重庆）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time) VALUES
(15, 18, 1, NULL, '07:46:00', 0, 0, NOW(), NOW()),            -- 成都东站（始发）
(15, 19, 2, '09:11:00', NULL, 0, 308, NOW(), NOW());          -- 重庆北站（终到）

-- ========================================
-- 4. 添加车次日程数据（train_schedule）
-- 未来30天的运行日程
-- ========================================

-- 为每个列车添加未来30天的运行日程
INSERT INTO train_schedule (train_id, travel_date, status, create_time, update_time)
SELECT
    t.id as train_id,
    DATE_ADD(CURDATE(), INTERVAL n.n DAY) as travel_date,
    1 as status,
    NOW() as create_time,
    NOW() as update_time
FROM
    train t
CROSS JOIN (
    SELECT 0 as n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL
    SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL
    SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL
    SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
) n;

-- ========================================
-- 完成！
-- ========================================

-- 查看添加结果
SELECT '车站数据统计：' as info;
SELECT COUNT(*) as total_stations,
       COUNT(DISTINCT city) as cities,
       COUNT(DISTINCT province) as provinces
FROM station;

SELECT '列车数据统计：' as info;
SELECT COUNT(*) as total_trains,
       COUNT(DISTINCT type) as train_types
FROM train;

SELECT '列车路线数据统计：' as info;
SELECT COUNT(*) as total_routes,
       COUNT(DISTINCT train_id) as trains_with_routes
FROM train_route;

SELECT '车次日程数据统计：' as info;
SELECT COUNT(*) as total_schedules,
       COUNT(DISTINCT train_id) as trains_with_schedules,
       COUNT(DISTINCT travel_date) as schedule_days
FROM train_schedule;

COMMIT;

