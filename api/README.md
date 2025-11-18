# Mini12306 API 服务

## 项目介绍

Mini12306 API 是一个简化版的12306铁路售票系统后端服务，提供用户管理、身份认证、车票预订和订单管理等功能。

## 功能特性

- 用户注册与登录
- 用户身份认证
- 乘车人管理
- 车次查询
- 车票预订
- 订单管理
- 车票管理

## 技术栈

- Java 21
- Spring Boot 3.1.7
- Spring Data JPA
- H2 数据库 (内存模式)
- Maven

## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.6 或更高版本

### 运行方式

1. 使用脚本快速启动

```bash
cd /path/to/mini12306-api
./run.sh
```

2. 手动编译和运行

```bash
cd /path/to/mini12306-api
mvn clean package -DskipTests
java -jar target/mini12306-api-0.0.1-SNAPSHOT.jar
```

## API 接口说明

### 身份认证相关

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/authenticate` - 身份认证
- `GET /api/auth/userinfo` - 获取用户信息

### 乘车人相关

- `GET /api/passenger` - 获取乘车人列表
- `POST /api/passenger` - 添加乘车人
- `PUT /api/passenger/{id}` - 修改乘车人
- `DELETE /api/passenger/{id}` - 删除乘车人
- `GET /api/passenger/{id}` - 获取乘车人详情

### 列车相关

- `GET /api/train` - 获取所有列车
- `GET /api/train/search` - 按起始站和终点站搜索列车
- `GET /api/train/{id}` - 获取列车详情

### 订单相关

- `POST /api/order` - 创建订单并购票
- `GET /api/order` - 获取用户订单列表
- `POST /api/order/cancel` - 取消订单
- `GET /api/order/{orderNo}` - 获取订单详情
- `GET /api/order/{orderNo}/tickets` - 获取订单的车票列表

### 车票相关

- `GET /api/ticket/bought` - 获取用户购买的所有票
- `GET /api/ticket/passenger/{passengerId}` - 获取用户作为乘车人的所有票
- `POST /api/ticket/cancel` - 取消车票
- `GET /api/ticket/{ticketNo}` - 获取车票详情

## 开发

系统已预置了一些初始数据：
- 列车数据（高铁、动车、普通列车等）
- 支持用户注册和登录功能

## License

MIT
