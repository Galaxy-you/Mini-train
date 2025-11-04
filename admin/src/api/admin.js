import service from './index';
import { saveToken, saveUserId } from '@/utils/tokenHelper';

// Admin API接口
const adminAPI = {
  // 管理员登录 - 简化后的版本，参考用户前端的实现方式
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
  
  // 车站管理
  station: {
    list: () => service.get('/admin/station/list'),
    add: (data) => service.post('/admin/station', data),
    update: (data) => service.put('/admin/station', data),
    delete: (id) => service.delete(`/admin/station/${id}`)
  },
  
  // 列车管理
  train: {
    list: () => service.get('/admin/train/list'),
    add: (data) => service.post('/admin/train', data),
    update: (data) => service.put('/admin/train', data),
    delete: (id) => service.delete(`/admin/train/${id}`)
  },
  
  // 路线管理
  route: {
    list: () => service.get('/admin/route/list'),
    add: (data) => service.post('/admin/route', data),
    update: (data) => service.put('/admin/route', data),
    delete: (id) => service.delete(`/admin/route/${id}`)
  }
};

export { service as request, adminAPI };
