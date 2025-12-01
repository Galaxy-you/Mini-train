import axios from 'axios';
import { clearAuth } from '@/utils/tokenHelper';

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

// 导出axios实例和adminAPI
export default service;

// 从admin.js导入并重新导出adminAPI
export { adminAPI } from './admin';


