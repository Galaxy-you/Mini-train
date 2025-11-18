-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS Mini12306;

-- 使用数据库
USE Mini12306;

-- 删除现有表（按照外键约束的相反顺序）
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

-- 创建管理员用户表
CREATE TABLE IF NOT EXISTS admin_user (
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
CREATE TABLE IF NOT EXISTS user_account (
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

-- 创建用户令牌表（用于登录认证）
CREATE TABLE IF NOT EXISTS user_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expire_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户令牌表';

-- 创建车站信息表
CREATE TABLE IF NOT EXISTS station (
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
CREATE TABLE IF NOT EXISTS train (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '车次编号',
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
CREATE TABLE IF NOT EXISTS train_route (
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
    UNIQUE KEY `unique_train_station` (train_id, station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='列车路线表';

-- 创建车次日程表
CREATE TABLE IF NOT EXISTS train_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    travel_date DATE NOT NULL,
    status INT DEFAULT 1 COMMENT '状态：0-停运，1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES train(id),
    UNIQUE KEY `unique_train_date` (train_id, travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车次日程表';

-- 创建座位类型表
CREATE TABLE IF NOT EXISTS seat_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE COMMENT '座位类型名称',
    code VARCHAR(10) COMMENT '座位类型代码',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位类型表';

-- 创建车票价格表
CREATE TABLE IF NOT EXISTS ticket_price (
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
    UNIQUE KEY `unique_route_seat` (train_id, start_station_id, end_station_id, seat_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车票价格表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT '待支付' COMMENT '订单状态：待支付、已支付、已取消、已完成',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pay_time TIMESTAMP NULL,
    cancel_time TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建车票表
CREATE TABLE IF NOT EXISTS ticket (
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
    status INT DEFAULT 1 COMMENT '车票状态：0-未支付，1-正常，2-已检票，3-已取消',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车票表';

-- 创建乘客信息表
CREATE TABLE IF NOT EXISTS passenger (
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
    UNIQUE KEY `unique_user_passenger` (user_id, card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乘客信息表';

-- 初始化座位类型
INSERT INTO seat_type (name, code) VALUES 
('商务座', 'SWZ'),
('一等座', 'YDZ'),
('二等座', 'EDZ'),
('硬座', 'YZ'),
('硬卧', 'YW'),
('软卧', 'RW');

-- 初始化管理员账号（密码：admin123的BCrypt加密）
-- BCrypt加密的密码，原密码为admin123
INSERT INTO admin_user (username, password, role, real_name, status)
VALUES ('admin', '$2a$10$XALW8EtiSGMNI3mTqUqgbOouakeEC18UUFH62P/7.5/RZCAUdxSMe', 'admin', '系统管理员', 1);