import service from './index';

// Admin API接口
const adminAPI = {
  // 管理员登录
  login(data) {
    return service({
      url: '/admin/login',
      method: 'post',
      data
    });
  },
  
  // 验证管理员token
  validateToken: () => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject(new Error('未找到token'));
    }
    return service.get('/admin/validate-token');
  },
  
  // 获取管理员信息
  getInfo: () => service.get('/admin/info'),
  
  // 退出登录
  logout: () => service.post('/admin/logout'),
  
  // 统计数据
  stats: {
    system: () => service.get('/admin/stats/system'),
    order: () => service.get('/admin/stats/order'),
    popular: () => service.get('/admin/stats/popular')
  },

  // 车站管理
  station: {
    list: (params) => service.get('/admin/station', { params }),
    add: (data) => service.post('/admin/station', data),
    update: (id, data) => service.put(`/admin/station/${id}`, data),
    delete: (id) => service.delete(`/admin/station/${id}`)
  },
  
  // 列车管理
  train: {
    list: (params) => service.get('/admin/train', { params }),
    get: (id) => service.get(`/admin/train/${id}`),
    add: (data) => service.post('/admin/train', data),
    update: (id, data) => service.put(`/admin/train/${id}`, data),
    delete: (id) => service.delete(`/admin/train/${id}`)
  },
  
  // 列车路线管理（对应train_route表 - 列车经过哪些站点）
  trainRoute: {
    list: (params) => service.get('/admin/train-route', { params }),
    getByTrainId: (trainId) => service.get(`/admin/train-route/train/${trainId}`),
    get: (id) => service.get(`/admin/train-route/${id}`),
    add: (data) => service.post('/admin/train-route', data),
    batchAdd: (data) => service.post('/admin/train-route/batch', data),
    update: (id, data) => service.put(`/admin/train-route/${id}`, data),
    delete: (id) => service.delete(`/admin/train-route/${id}`)
  },

  // 车次日程管理（对应train_schedule表 - 车次在哪些日期运行）
  trainSchedule: {
    list: (params) => service.get('/admin/train-schedule', { params }),
    getByTrainId: (trainId) => service.get(`/admin/train-schedule/train/${trainId}`),
    get: (id) => service.get(`/admin/train-schedule/${id}`),
    add: (data) => service.post('/admin/train-schedule', data),
    batchAdd: (data) => service.post('/admin/train-schedule/batch', data),
    update: (id, data) => service.put(`/admin/train-schedule/${id}`, data),
    delete: (id) => service.delete(`/admin/train-schedule/${id}`)
  },

  // 用户管理
  user: {
    list: (params) => service.get('/admin/user', { params }),
    get: (id) => service.get(`/admin/user/${id}`),
    updateAuthStatus: (id, authStatus) => service.put(`/admin/user/${id}/auth`, { authStatus }),
    resetPassword: (id) => service.post(`/admin/user/${id}/reset-password`)
  },

  // 车票管理
  ticket: {
    list: (params) => service.get('/admin/ticket', { params }),
    get: (id) => service.get(`/admin/ticket/${id}`),
    update: (id, data) => service.put(`/admin/ticket/${id}`, data),
    cancel: (id) => service.post(`/admin/ticket/${id}/cancel`)
  }
};

export { service as request, adminAPI };


