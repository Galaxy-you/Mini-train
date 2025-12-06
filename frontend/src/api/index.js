import request from '@/utils/request';

export const authAPI = {
  // 用户登录
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    });
  },
  // 用户注册
  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    });
  },
  // 身份认证
  authenticate(data) {
    return request({
      url: '/auth/authenticate',
      method: 'post',
      data
    });
  },
  // 获取用户信息
  getUserInfo() {
    return request({
      url: '/auth/userinfo',
      method: 'get'
    });
  },
  // 获取用户统计数据
  getUserStats() {
    return request({
      url: '/auth/stats',
      method: 'get'
    });
  }
};

export const trainAPI = {
  // 查询所有列车
  listAllTrains() {
    return request({
      url: '/train',
      method: 'get'
    });
  },
  // 根据起始站和终点站查询列车
  searchTrains(startStation, endStation) {
    return request({
      url: '/train/search',
      method: 'get',
      params: { startStation, endStation }
    });
  },
  // 获取列车详情
  getTrainDetail(id) {
    return request({
      url: `/train/${id}`,
      method: 'get'
    });
  },
  // 获取列车路线
  getTrainRoute(trainId) {
    return request({
      url: `/train-route/train/${trainId}`,
      method: 'get'
    });
  }
};

export const passengerAPI = {
  // 添加乘车人
  addPassenger(data) {
    return request({
      url: '/passenger',
      method: 'post',
      data
    });
  },
  // 修改乘车人
  updatePassenger(id, data) {
    return request({
      url: `/passenger/${id}`,
      method: 'put',
      data
    });
  },
  // 删除乘车人
  deletePassenger(id) {
    return request({
      url: `/passenger/${id}`,
      method: 'delete'
    });
  },
  // 查询乘车人列表
  listPassengers() {
    return request({
      url: '/passenger',
      method: 'get'
    });
  },
  // 查询乘车人详情
  getPassenger(id) {
    return request({
      url: `/passenger/${id}`,
      method: 'get'
    });
  }
};

export const orderAPI = {
  // 创建订单并购票
  createOrder(data) {
    return request({
      url: '/order',
      method: 'post',
      data
    });
  },
  // 查询用户的所有订单
  listUserOrders() {
    return request({
      url: '/order',
      method: 'get'
    });
  },
  // 取消订单
  cancelOrder(orderNo) {
    return request({
      url: '/order/cancel',
      method: 'post',
      params: { orderNo }
    });
  },
  // 查询订单详情
  getOrderDetail(orderNo) {
    return request({
      url: `/order/${orderNo}`,
      method: 'get'
    });
  },
  // 查询订单的所有票
  getOrderTickets(orderNo) {
    return request({
      url: `/order/${orderNo}/tickets`,
      method: 'get'
    });
  },
  // 支付确认
  confirmPayment(orderNo, paymentMethod) {
    return request({
      url: `/order/${orderNo}/pay`,
      method: 'post',
      params: { paymentMethod }
    });
  },
  // 按起始站搜索订单
  searchOrdersByStartStation(startStation) {
    return request({
      url: '/order/search',
      method: 'get',
      params: { startStation }
    });
  }
};

export const ticketAPI = {
  // 查询用户购买的所有票
  listUserBoughtTickets() {
    return request({
      url: '/ticket/bought',
      method: 'get'
    });
  },
  // 查询用户作为乘车人的所有票
  listUserTickets(passengerId) {
    return request({
      url: `/ticket/passenger/${passengerId}`,
      method: 'get'
    });
  },
  // 取消票
  cancelTicket(ticketNo) {
    return request({
      url: '/ticket/cancel',
      method: 'post',
      params: { ticketNo }
    });
  },
  // 查询票详情
  getTicketDetail(ticketNo) {
    return request({
      url: `/ticket/${ticketNo}`,
      method: 'get'
    });
  },
  // 获取退票规则及计算退款金额
  getRefundRule(ticketNo) {
    return request({
      url: '/ticket/refund-rule',
      method: 'get',
      params: { ticketNo }
    });
  },
  // 检查票是否可以改签
  checkChangeTicket(ticketNo) {
    return request({
      url: '/ticket/change-rule',
      method: 'get',
      params: { ticketNo }
    });
  },
  // 改签车票
  changeTicket(data) {
    return request({
      url: '/ticket/change',
      method: 'post',
      data
    });
  }
};

// 导入站点API
import { stationAPI } from './station';
export { stationAPI };
