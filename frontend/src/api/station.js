import request from '@/utils/request';

/**
 * 站点相关API接口
 */
export const stationAPI = {
  // 获取所有站点
  getAllStations() {
    return request({
      url: '/station',
      method: 'get'
    });
  },
  
  // 根据ID获取站点
  getStationById(id) {
    return request({
      url: `/station/${id}`,
      method: 'get'
    });
  },
  
  // 根据名称获取站点
  getStationByName(name) {
    return request({
      url: '/station/name',
      method: 'get',
      params: { name }
    });
  },
  
  // 根据城市获取站点列表
  getStationsByCity(city) {
    return request({
      url: '/station/city',
      method: 'get',
      params: { city }
    });
  },
  
  // 根据省份获取站点列表
  getStationsByProvince(province) {
    return request({
      url: '/station/province',
      method: 'get',
      params: { province }
    });
  },
  
  // 搜索站点
  searchStations(params) {
    return request({
      url: '/station/search',
      method: 'get',
      params
    });
  }
};
