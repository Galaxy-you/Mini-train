import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 15000 // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token并添加到请求头中
    const token = localStorage.getItem('token');
    if (token) {
      // 详细调试令牌使用情况
      console.log('添加认证令牌到请求头:', token);
      console.log('请求URL:', config.url);
      console.log('请求方法:', config.method);

      // 直接使用token，不添加Bearer前缀
      config.headers['Authorization'] = token;
      
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
    console.log('收到API响应:', response);
    const res = response.data;
    console.log('解析响应数据:', res);
    
    // 检查顶级结构，后端返回 {success: boolean, message: string, data: any}
    if (res && res.success === true) {
      console.log('API请求成功，返回数据:', res.data);
      
      // 为了数据一致性，检查数据是否为null或undefined
      if (res.data === null || res.data === undefined) {
        // 根据API类型推断默认数据类型
        console.warn('API返回空数据，尝试设置适当的默认值');
        if (response.config && response.config.url) {
          const url = response.config.url;
          if (url.includes('/ticket') || url.includes('/train') || 
              url.includes('/passenger') || url.includes('/order')) {
            // 列表类API默认返回空数组
            console.log('列表API返回空数据，设为空数组');
            res.data = [];
          } else {
            // 其他API默认返回空对象
            console.log('对象API返回空数据，设为空对象');
            res.data = {};
          }
        }
      }
      
      // 直接返回整个响应对象，让调用方决定如何处理数据
      return res;
    } else {
      const errorMsg = res?.message || '请求失败';
      console.error('API请求失败:', errorMsg);
      
      // 显示错误消息
      ElMessage({
        message: errorMsg,
        type: 'error',
        duration: 5 * 1000
      });
      return Promise.reject(new Error(errorMsg));
    }
  },
  error => {
    console.log('API请求错误:', error);
    console.log('错误响应对象:', error.response);
    
    let message = error.message || '请求错误';
    const url = error.config?.url || '未知API';
    console.log(`API ${url} 请求失败:`, message);
    
    // 检查具体的HTTP状态码
    if (error.response) {
      const status = error.response.status;
      console.log(`服务器返回状态码: ${status}`);
      
      // 处理常见的HTTP错误
      if (status === 401) {
        console.error('收到401未授权响应 - 可能是令牌无效或已过期');
        message = '未授权，请重新登录';
        
        // 清除本地存储中的过期令牌
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        
        // 检查当前页面，避免在登录页面重复跳转
        if (!window.location.pathname.includes('/login')) {
          console.log('重定向到登录页面...');
          setTimeout(() => {
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
          }, 1000);
        }
      } else if (status === 403) {
        message = '拒绝访问，您没有权限执行此操作';
      } else if (status === 404) {
        message = `请求的资源不存在: ${url}`;
      } else if (status === 500) {
        // 尝试解析服务器错误详情
        try {
          const serverError = error.response.data;
          if (serverError && serverError.message) {
            message = `服务器错误: ${serverError.message}`;
          } else {
            message = '服务器内部错误，请稍后再试';
          }
        } catch (e) {
          message = '服务器内部错误，请稍后再试';
        }
        
        console.error('服务器返回500错误:', error.response.data);
      } else {
        message = `请求错误 (${status}): ${message}`;
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('没有收到服务器响应:', error.request);
      message = '服务器没有响应，请检查网络连接';
    }
    
    // 显示错误消息
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    });
    
    // 包装错误对象以提供更多上下文
    const enhancedError = new Error(message);
    enhancedError.originalError = error;
    enhancedError.url = url;
    enhancedError.status = error.response?.status;
    
    return Promise.reject(enhancedError);
  }
);

export default service;
