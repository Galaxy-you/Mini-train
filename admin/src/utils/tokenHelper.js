/**
 * Token管理工具
 * 完全与用户系统保持一致的token存储和管理方式
 */

const TOKEN_KEY = 'token';
const USER_ID_KEY = 'userId';

/**
 * 存储令牌到本地存储
 * @param {string} token 令牌字符串
 */
export function saveToken(token) {
  if (token) {
    console.log('保存令牌到localStorage:', token);
    localStorage.setItem(TOKEN_KEY, token);
  }
}

/**
 * 从本地存储获取令牌
 * @returns {string|null} 令牌字符串，如果不存在则返回null
 */
export function getToken() {
  const token = localStorage.getItem(TOKEN_KEY);
  console.log('从localStorage获取令牌:', token);
  return token;
}

/**
 * 从本地存储中移除令牌
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
  console.log('已移除管理员令牌');
}

/**
 * 存储用户ID到本地存储
 * @param {string} userId 用户ID
 */
export function saveUserId(userId) {
  if (userId) {
    localStorage.setItem(USER_ID_KEY, userId);
  }
}

/**
 * 从本地存储获取用户ID
 * @returns {string|null} 用户ID，如果不存在则返回null
 */
export function getUserId() {
  return localStorage.getItem(USER_ID_KEY);
}

/**
 * 从本地存储中移除用户ID
 */
export function removeUserId() {
  localStorage.removeItem(USER_ID_KEY);
}

/**
 * 清除所有认证相关数据
 */
export function clearAuth() {
  removeToken();
  removeUserId();
}

/**
 * 检查是否已登录
 * @returns {boolean} 如果存在有效令牌则返回true，否则返回false
 */
export function isLoggedIn() {
  // 直接从localStorage获取，绕过getToken函数，用于调试
  const rawToken = localStorage.getItem(TOKEN_KEY);
  console.log('直接从localStorage获取的token:', rawToken);
  
  // 常规方式获取token
  const token = getToken();
  const isLogged = !!token;
  
  // 显示详细日志
  console.log('isLoggedIn 检查结果:', isLogged);
  console.log('令牌值:', token);
  console.log('localStorage内容:', Object.keys(localStorage).map(key => ({ key, value: localStorage.getItem(key) })));
  
  return isLogged;
}

export default {
  saveToken,
  getToken,
  removeToken,
  saveUserId,
  getUserId,
  removeUserId,
  clearAuth,
  isLoggedIn
};