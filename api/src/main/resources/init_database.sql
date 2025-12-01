-- ========================================
-- Mini12306 完整数据库重置和初始化脚本
-- ========================================
-- 警告：此脚本将删除所有现有数据！
-- 执行前请确保已备份重要数据
-- ========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS Mini12306;

-- 使用数据库
USE Mini12306;

-- ========================================
-- 第一步：删除所有现有表
-- ========================================

DROP TABLE IF EXISTS passenger;
DROP TABLE IF EXISTS ticket;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS ticket_price;
DROP TABLE IF EXISTS seat_type;
DROP TABLE IF EXISTS train_schedule;
DROP TABLE IF EXISTS train_route;
DROP TABLE IF EXISTS train;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS user_token;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS admin_user;

-- ========================================
-- 第二步：创建所有表结构
-- ========================================

-- 创建管理员用户表
CREATE TABLE admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色',
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    last_login_time TIMESTAMP NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- 创建用户账户表
CREATE TABLE user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    card_id VARCHAR(18),
    phone VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    auth_status INT DEFAULT 0 COMMENT '身份验证状态：0-未验证，1-已验证'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表';

-- 创建用户令牌表
CREATE TABLE user_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expire_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户令牌表';

-- 创建车站信息表
CREATE TABLE station (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '站点名称',
    code VARCHAR(10) COMMENT '站点代码',
    city VARCHAR(50) COMMENT '所属城市',
    province VARCHAR(50) COMMENT '所属省份',
    type VARCHAR(20) COMMENT '站点类型：高铁站、普通站等',
    address VARCHAR(200) COMMENT '站点地址',
    longitude DOUBLE COMMENT '经度',
    latitude DOUBLE COMMENT '纬度',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车站信息表';

-- 创建列车信息表
CREATE TABLE train (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '车次编号',
    type VARCHAR(10) COMMENT '列车类型',
    start_station_id BIGINT COMMENT '起始站ID',
    end_station_id BIGINT COMMENT '终点站ID',
    start_station VARCHAR(50) COMMENT '起始站名称',
    end_station VARCHAR(50) COMMENT '终点站名称',
    start_time VARCHAR(10) COMMENT '出发时间',
    end_time VARCHAR(10) COMMENT '到达时间',
    duration INT COMMENT '运行时间(分钟)',
    seat_count INT COMMENT '座位数',
    price DECIMAL(10,2) COMMENT '票价',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (start_station_id) REFERENCES station(id),
    FOREIGN KEY (end_station_id) REFERENCES station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='列车信息表';

-- 创建列车路线表
CREATE TABLE train_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    station_order INT NOT NULL COMMENT '站点顺序',
    arrive_time TIME COMMENT '到站时间',
    depart_time TIME COMMENT '出站时间',
    stop_time INT COMMENT '停留时间(分钟)',
    distance INT COMMENT '与起始站距离(公里)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES train(id),
    FOREIGN KEY (station_id) REFERENCES station(id),
    UNIQUE KEY unique_train_station (train_id, station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='列车路线表';

-- 创建车次日程表
CREATE TABLE train_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    travel_date DATE NOT NULL,
    status INT DEFAULT 1 COMMENT '状态：0-停运，1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES train(id),
    UNIQUE KEY unique_train_date (train_id, travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车次日程表';

-- 创建座位类型表
CREATE TABLE seat_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE COMMENT '座位类型名称',
    code VARCHAR(10) COMMENT '座位类型代码',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位类型表';

-- 创建车票价格表
CREATE TABLE ticket_price (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    start_station_id BIGINT NOT NULL,
    end_station_id BIGINT NOT NULL,
    seat_type_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES train(id),
    FOREIGN KEY (start_station_id) REFERENCES station(id),
    FOREIGN KEY (end_station_id) REFERENCES station(id),
    FOREIGN KEY (seat_type_id) REFERENCES seat_type(id),
    UNIQUE KEY unique_route_seat (train_id, start_station_id, end_station_id, seat_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车票价格表';

-- 创建订单表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '订单状态：UNPAID-待支付、PAID-已支付、CANCELED-已取消、COMPLETED-已完成',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pay_time TIMESTAMP NULL,
    cancel_time TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建车票表
CREATE TABLE ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(50) NOT NULL UNIQUE COMMENT '车票编号',
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '购票用户ID',
    passenger_id BIGINT NOT NULL COMMENT '乘车人ID',
    passenger_name VARCHAR(50) COMMENT '乘车人姓名',
    passenger_card VARCHAR(18) COMMENT '乘车人证件号',
    train_id BIGINT NOT NULL COMMENT '列车ID',
    train_code VARCHAR(20) COMMENT '列车车次',
    train_type VARCHAR(10) COMMENT '列车类型',
    start_station_id BIGINT NOT NULL COMMENT '发站ID',
    end_station_id BIGINT NOT NULL COMMENT '到站ID',
    start_station VARCHAR(50) COMMENT '发站名称',
    end_station VARCHAR(50) COMMENT '到站名称',
    start_city VARCHAR(50) COMMENT '发站所在城市',
    end_city VARCHAR(50) COMMENT '到站所在城市',
    seat_type VARCHAR(20) DEFAULT '二等座' COMMENT '座位类型',
    coach VARCHAR(10) COMMENT '车厢号',
    seat VARCHAR(10) COMMENT '座位号',
    seat_info VARCHAR(30) COMMENT '完整座位信息',
    travel_date DATE COMMENT '发车日期',
    start_time VARCHAR(10) COMMENT '发车时间',
    end_time VARCHAR(10) COMMENT '到达时间',
    duration INT COMMENT '历时(分钟)',
    price DECIMAL(10,2) NOT NULL COMMENT '票价',
    status INT DEFAULT 1 COMMENT '车票状态：0-已取消，1-正常，2-已检票，3-已改签，4-已过期',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车票表';

-- 创建乘客信息表
CREATE TABLE passenger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    card_id VARCHAR(18) NOT NULL,
    card_type VARCHAR(10) DEFAULT '身份证' COMMENT '证件类型',
    phone VARCHAR(20),
    type VARCHAR(10) DEFAULT '成人' COMMENT '乘客类型: 成人、儿童、学生等',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认乘车人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_account(id),
    UNIQUE KEY unique_user_passenger (user_id, card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乘客信息表';

-- ========================================
-- 第三步：初始化基础数据
-- ========================================

-- 初始化座位类型
INSERT INTO seat_type (name, code) VALUES
('商务座', 'SWZ'),
('一等座', 'YDZ'),
('二等座', 'EDZ'),
('硬座', 'YZ'),
('硬卧', 'YW'),
('软卧', 'RW');

-- 初始化管理员账号（用户名：admin，密码：admin123）
INSERT INTO admin_user (username, password, role, real_name, status)
VALUES ('admin', '$2a$10$XALW8EtiSGMNI3mTqUqgbOouakeEC18UUFH62P/7.5/RZCAUdxSMe', 'admin', '系统管理员', 1);

-- ========================================
-- 第四步：初始化车站数据
-- ========================================

INSERT INTO station (name, code, city, province, type, create_time, update_time) VALUES
-- 原有车站
('北京站', '100000', '北京', '北京市', '高铁站', NOW(), NOW()),
('广州站', '510000', '广州', '广东省', '高铁站', NOW(), NOW()),
('上海站', '200000', '上海', '上海市', '高铁站', NOW(), NOW()),
('武昌站', '430061', '武汉', '湖北省', '高铁站', NOW(), NOW()),
('沈阳北站', '110000', '沈阳', '辽宁省', '高铁站', NOW(), NOW()),
('广州白云站', '510400', '广州', '广东省', '高铁站', NOW(), NOW()),

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
-- 第五步：初始化列车数据
-- ========================================

INSERT INTO train (code, type, start_station_id, end_station_id, start_station, end_station, start_time, end_time, duration, seat_count, price, create_time, update_time)
SELECT 'G1234', '高铁', bj.id, sh.id, '北京站', '上海站', '08:00', '13:00', 300, 298, 553.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '上海站') sh
UNION ALL
SELECT 'D5678', '动车', bj.id, gz.id, '北京站', '广州站', '10:30', '18:30', 480, 200, 219.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '广州站') gz
UNION ALL
SELECT 'K9876', '快车', sh.id, gz.id, '上海站', '广州站', '14:20', '次日06:20', 960, 100, 112.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '上海站') sh, (SELECT id FROM station WHERE name = '广州站') gz
UNION ALL
-- 京沪线高铁
SELECT 'G1', '高铁', bj.id, sh.id, '北京站', '上海站', '07:00', '11:28', 268, 1200, 553.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '上海站') sh
UNION ALL
SELECT 'G2', '高铁', sh.id, bj.id, '上海站', '北京站', '08:05', '12:33', 268, 1200, 553.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '上海站') sh, (SELECT id FROM station WHERE name = '北京站') bj
UNION ALL
SELECT 'G3', '高铁', bj.id, sh.id, '北京站', '上海站', '09:00', '13:35', 275, 1200, 553.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '上海站') sh
UNION ALL
-- 京广线高铁
SELECT 'G71', '高铁', bj.id, gz.id, '北京站', '广州站', '08:00', '16:22', 502, 1200, 862.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '广州站') gz
UNION ALL
SELECT 'G72', '高铁', gz.id, bj.id, '广州站', '北京站', '08:30', '16:52', 502, 1200, 862.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '广州站') gz, (SELECT id FROM station WHERE name = '北京站') bj
UNION ALL
-- 沪广线高铁
SELECT 'G101', '高铁', sh.id, gz.id, '上海站', '广州站', '09:17', '17:06', 469, 1000, 793.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '上海站') sh, (SELECT id FROM station WHERE name = '广州站') gz
UNION ALL
SELECT 'G102', '高铁', gz.id, sh.id, '广州站', '上海站', '10:15', '18:04', 469, 1000, 793.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '广州站') gz, (SELECT id FROM station WHERE name = '上海站') sh
UNION ALL
-- 京哈线高铁
SELECT 'G381', '高铁', bj.id, heb.id, '北京站', '哈尔滨西站', '06:48', '14:26', 458, 800, 635.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '哈尔滨西站') heb
UNION ALL
SELECT 'G382', '高铁', heb.id, bj.id, '哈尔滨西站', '北京站', '07:15', '14:53', 458, 800, 635.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '哈尔滨西站') heb, (SELECT id FROM station WHERE name = '北京站') bj
UNION ALL
-- 京沪动车
SELECT 'D321', '动车', bj.id, sh.id, '北京站', '上海站', '13:20', '19:45', 385, 600, 460.80, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '北京站') bj, (SELECT id FROM station WHERE name = '上海站') sh
UNION ALL
SELECT 'D322', '动车', sh.id, bj.id, '上海站', '北京站', '14:10', '20:35', 385, 600, 460.80, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '上海站') sh, (SELECT id FROM station WHERE name = '北京站') bj
UNION ALL
-- 成渝高铁
SELECT 'G8501', '高铁', cd.id, cq.id, '成都东站', '重庆北站', '07:46', '09:11', 85, 600, 154.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '成都东站') cd, (SELECT id FROM station WHERE name = '重庆北站') cq
UNION ALL
SELECT 'G8502', '高铁', cq.id, cd.id, '重庆北站', '成都东站', '08:35', '10:00', 85, 600, 154.00, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '重庆北站') cq, (SELECT id FROM station WHERE name = '成都东站') cd
UNION ALL
-- 武广高铁
SELECT 'G1001', '高铁', wh.id, gz.id, '武昌站', '广州站', '08:02', '11:27', 205, 1000, 463.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '武昌站') wh, (SELECT id FROM station WHERE name = '广州站') gz
UNION ALL
SELECT 'G1002', '高铁', gz.id, wh.id, '广州站', '武昌站', '09:03', '12:28', 205, 1000, 463.50, NOW(), NOW()
FROM (SELECT id FROM station WHERE name = '广州站') gz, (SELECT id FROM station WHERE name = '武昌站') wh;

-- ========================================
-- 第六步：初始化列车路线数据
-- ========================================

-- G1次列车路线（北京-上海）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '07:00:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G1' AND s.name = '北京站'
UNION ALL
SELECT t.id, s.id, 2, '08:33:00', '08:35:00', 2, 406, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G1' AND s.name = '天津站'
UNION ALL
SELECT t.id, s.id, 3, '09:42:00', '09:44:00', 2, 650, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G1' AND s.name = '济南西站'
UNION ALL
SELECT t.id, s.id, 4, '10:58:00', '11:00:00', 2, 1023, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G1' AND s.name = '南京南站'
UNION ALL
SELECT t.id, s.id, 5, '11:28:00', NULL, 0, 1318, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G1' AND s.name = '上海站';

-- G2次列车路线（上海-北京）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '08:05:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G2' AND s.name = '上海站'
UNION ALL
SELECT t.id, s.id, 2, '08:33:00', '08:35:00', 2, 295, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G2' AND s.name = '南京南站'
UNION ALL
SELECT t.id, s.id, 3, '09:49:00', '09:51:00', 2, 668, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G2' AND s.name = '济南西站'
UNION ALL
SELECT t.id, s.id, 4, '10:58:00', '11:00:00', 2, 912, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G2' AND s.name = '天津站'
UNION ALL
SELECT t.id, s.id, 5, '12:33:00', NULL, 0, 1318, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G2' AND s.name = '北京站';

-- G71次列车路线（北京-广州）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '08:00:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G71' AND s.name = '北京站'
UNION ALL
SELECT t.id, s.id, 2, '10:26:00', '10:28:00', 2, 600, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G71' AND s.name = '石家庄站'
UNION ALL
SELECT t.id, s.id, 3, '12:44:00', '12:46:00', 2, 1200, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G71' AND s.name = '武昌站'
UNION ALL
SELECT t.id, s.id, 4, '14:15:00', '14:17:00', 2, 1580, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G71' AND s.name = '长沙南站'
UNION ALL
SELECT t.id, s.id, 5, '16:22:00', NULL, 0, 2298, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G71' AND s.name = '广州站';

-- G101次列车路线（上海-广州）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '09:17:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G101' AND s.name = '上海站'
UNION ALL
SELECT t.id, s.id, 2, '11:05:00', '11:07:00', 2, 320, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G101' AND s.name = '杭州东站'
UNION ALL
SELECT t.id, s.id, 3, '13:36:00', '13:38:00', 2, 880, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G101' AND s.name = '长沙南站'
UNION ALL
SELECT t.id, s.id, 4, '17:06:00', NULL, 0, 1450, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G101' AND s.name = '广州站';

-- G381次列车路线（北京-哈尔滨）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '06:48:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G381' AND s.name = '北京站'
UNION ALL
SELECT t.id, s.id, 2, '09:15:00', '09:17:00', 2, 680, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G381' AND s.name = '沈阳北站'
UNION ALL
SELECT t.id, s.id, 3, '11:38:00', '11:40:00', 2, 950, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G381' AND s.name = '长春站'
UNION ALL
SELECT t.id, s.id, 4, '14:26:00', NULL, 0, 1240, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G381' AND s.name = '哈尔滨西站';

-- G8501次列车路线（成都-重庆）
INSERT INTO train_route (train_id, station_id, station_order, arrive_time, depart_time, stop_time, distance, create_time, update_time)
SELECT t.id, s.id, 1, NULL, '07:46:00', 0, 0, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G8501' AND s.name = '成都东站'
UNION ALL
SELECT t.id, s.id, 2, '09:11:00', NULL, 0, 308, NOW(), NOW()
FROM train t, station s WHERE t.code = 'G8501' AND s.name = '重庆北站';

-- ========================================
-- 第七步：初始化车次日程数据（未来30天）
-- ========================================

INSERT INTO train_schedule (train_id, travel_date, status, create_time, update_time)
SELECT
    t.id as train_id,
    DATE_ADD(CURDATE(), INTERVAL n.n DAY) as travel_date,
    1 as status,
    NOW() as create_time,
    NOW() as update_time
FROM train t
CROSS JOIN (
    SELECT 0 as n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL
    SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL
    SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL
    SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
) n;

-- ========================================
-- 完成！显示统计信息
-- ========================================

SELECT '初始化完成！' as status;

SELECT 'Step 1: 车站统计' as step;
SELECT COUNT(*) as total_stations, COUNT(DISTINCT city) as cities, COUNT(DISTINCT province) as provinces FROM station;

SELECT 'Step 2: 列车统计' as step;
SELECT COUNT(*) as total_trains, COUNT(DISTINCT type) as train_types FROM train;

SELECT 'Step 3: 列车路线统计' as step;
SELECT COUNT(*) as total_routes, COUNT(DISTINCT train_id) as trains_with_routes FROM train_route;

SELECT 'Step 4: 车次日程统计' as step;
SELECT COUNT(*) as total_schedules, COUNT(DISTINCT train_id) as trains_with_schedules FROM train_schedule;

SELECT 'Step 5: 座位类型统计' as step;
SELECT COUNT(*) as total_seat_types FROM seat_type;

SELECT 'Step 6: 管理员账号' as step;
SELECT username, real_name, role, status FROM admin_user;

COMMIT;

