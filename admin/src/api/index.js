import axios from 'axios';
import { getToken, clearAuth } from '@/utils/tokenHelper';

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 30000 // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 与用户系统完全保持一致，直接从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      console.log('添加认证令牌到请求头:', token);
      console.log('请求URL:', config.url);
      console.log('请求方法:', config.method);

      // 添加Bearer前缀，符合JWT标准
      config.headers['Authorization'] = `Bearer ${token}`;
      
      // 打印完整请求头，供调试
      console.log('完整请求头:', config.headers);
    } else {
      console.warn('没有找到认证令牌，请求可能未授权');
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 调试信息，打印详细的响应数据
    console.log('收到API响应:', response);
    const res = response.data;
    console.log('解析响应数据:', res);
    
    // 简化响应处理逻辑，直接返回数据
    if (res) {
      // 检查特定的响应格式并进行处理
      if (res.content !== undefined && !Array.isArray(res.content)) {
        console.warn('API响应中content字段不是数组，正在修正:', res.content);
        res.content = Array.isArray(res.content) ? res.content : [];
      }
      return res; // 返回处理后的响应数据
    } else {
      // 无效响应
      console.error('无效的API响应');
      return Promise.reject(new Error('服务器返回了无效的响应'));
    }
  },
  error => {
    // 处理HTTP错误
    let errorMsg = '网络请求失败';
    if (error.response) {
      console.error('HTTP错误:', error.response);
      // 服务端返回的错误状态码
      switch (error.response.status) {
        case 401:
          errorMsg = '未授权，请重新登录';
          let message = 'hhh'; // 声明变量用于调试
          console.log('调试信息:', message);
          // 只有在非登录页面时才清除token并跳转
          if (window.location.pathname !== '/login') {
            // 使用tokenHelper清除认证信息
            console.log('清除认证信息');
            clearAuth();
            console.log('清除认证信息完成');
            window.location.href = '/login';
          }
          break;
        case 403:
          errorMsg = '拒绝访问';
          break;
        case 404:
          errorMsg = '请求的资源不存在';
          break;
        case 500:
          errorMsg = '服务器内部错误';
          break;
        default:
          errorMsg = `请求失败: ${error.response.status}`;
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      errorMsg = '服务器无响应';
    } else {
      // 请求配置错误
      errorMsg = '请求配置错误';
    }
    
    console.error('API请求错误:', errorMsg);
    return Promise.reject(new Error(errorMsg));
  }
);

// API请求函数
export const adminAPI = {
  // 管理员登录
  login(data) {
    return service({
      url: '/admin/login',
      method: 'post',
      data
    }).then(response => {
      // 确保返回的数据格式一致
      return response;
    });
  },
  
  // 获取统计数据
  getStats() {
    return service({
      url: '/admin/stats',
      method: 'get'
    });
  },
  
  // 获取最新订单数据
  getLatestOrders() {
    return service({
      url: '/admin/orders/latest',
      method: 'get'
    });
  },
  
  // 获取订单统计数据（近7天）
  getOrderStats() {
    return service({
      url: '/admin/stats/orders',
      method: 'get'
    });
  },
  
  // 获取热门车次统计
  getTrainStats() {
    return service({
      url: '/admin/stats/trains',
      method: 'get'
    });
  },
  
  // 站点管理
  station: {
    // 获取所有站点
    list(params) {
      return service({
        url: '/admin/station',
        method: 'get',
        params
      });
    },
    // 添加站点
    add(data) {
      return service({
        url: '/admin/station',
        method: 'post',
        data
      });
    },
    // 更新站点
    update(id, data) {
      return service({
        url: `/admin/station/${id}`,
        method: 'put',
        data
      });
    },
    // 删除站点
    delete(id) {
      return service({
        url: `/admin/station/${id}`,
        method: 'delete'
      });
    }
  },
  
  // 车次管理
  train: {
    // 获取所有车次
    list(params) {
      return service({
        url: '/admin/train',
        method: 'get',
        params
      });
    },
    // 获取车次详情
    get(id) {
      return service({
        url: `/admin/train/${id}`,
        method: 'get'
      });
    },
    // 添加车次
    add(data) {
      return service({
        url: '/admin/train',
        method: 'post',
        data
      });
    },
    // 更新车次
    update(id, data) {
      return service({
        url: `/admin/train/${id}`,
        method: 'put',
        data
      });
    },
    // 删除车次
    delete(id) {
      return service({
        url: `/admin/train/${id}`,
        method: 'delete'
      });
    }
  },
  
  // 线路管理
  route: {
    // 获取所有线路
    list(params) {
      return service({
        url: '/admin/route',
        method: 'get',
        params
      });
    },
    // 获取线路详情
    get(id) {
      return service({
        url: `/admin/route/${id}`,
        method: 'get'
      });
    },
    // 添加线路
    add(data) {
      return service({
        url: '/admin/route',
        method: 'post',
        data
      });
    },
    // 更新线路
    update(id, data) {
      return service({
        url: `/admin/route/${id}`,
        method: 'put',
        data
      });
    },
    // 删除线路
    delete(id) {
      return service({
        url: `/admin/route/${id}`,
        method: 'delete'
      });
    }
  },
  
  // 时刻表管理
  schedule: {
    // 获取所有时刻表
    list(params) {
      return service({
        url: '/admin/schedule',
        method: 'get',
        params
      });
    },
    // 获取时刻表详情
    get(id) {
      return service({
        url: `/admin/schedule/${id}`,
        method: 'get'
      });
    },
    // 添加时刻表
    add(data) {
      return service({
        url: '/admin/schedule',
        method: 'post',
        data
      });
    },
    // 更新时刻表
    update(id, data) {
      return service({
        url: `/admin/schedule/${id}`,
        method: 'put',
        data
      });
    },
    // 删除时刻表
    delete(id) {
      return service({
        url: `/admin/schedule/${id}`,
        method: 'delete'
      });
    }
  },
  
  // 车票管理
  ticket: {
    // 获取车票列表
    list(params) {
      return service({
        url: '/admin/ticket',
        method: 'get',
        params
      });
    },
    // 获取车票详情
    get(id) {
      return service({
        url: `/admin/ticket/${id}`,
        method: 'get'
      });
    },
    // 更新车票信息
    update(id, data) {
      return service({
        url: `/admin/ticket/${id}`,
        method: 'put',
        data
      });
    },
    // 取消车票
    cancel(id) {
      return service({
        url: `/admin/ticket/${id}/cancel`,
        method: 'post'
      });
    }
  },
  
  // 用户管理
  user: {
    // 获取用户列表
    list(params) {
      return service({
        url: '/admin/user',
        method: 'get',
        params
      });
    },
    // 获取用户详情
    get(id) {
      return service({
        url: `/admin/user/${id}`,
        method: 'get'
      });
    },
    // 添加用户
    add(data) {
      return service({
        url: '/admin/user',
        method: 'post',
        data
      });
    },
    // 更新用户
    update(id, data) {
      return service({
        url: `/admin/user/${id}`,
        method: 'put',
        data
      });
    },
    // 删除用户
    delete(id) {
      return service({
        url: `/admin/user/${id}`,
        method: 'delete'
      });
    },
    // 重置用户密码
    resetPassword(id) {
      return service({
        url: `/admin/user/${id}/reset-password`,
        method: 'post'
      });
    }
  }
};

export default service;
