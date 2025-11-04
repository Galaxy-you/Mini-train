/**
 * 站点名称格式化工具
 */

/**
 * 格式化站点名称，去除"站"字（如果有）
 * @param {string} stationName 原始站点名称
 * @returns {string} 格式化后的站点名称
 */
export function formatStationName(stationName) {
  if (!stationName) return ''
  return stationName.endsWith('站') ? stationName.slice(0, -1) : stationName
}

/**
 * 恢复站点名称，如果不以"站"结尾则添加
 * @param {string} stationName 格式化后的站点名称
 * @returns {string} 原始站点名称
 */
export function restoreStationName(stationName) {
  if (!stationName) return ''
  return stationName.endsWith('站') ? stationName : `${stationName}站`
}
