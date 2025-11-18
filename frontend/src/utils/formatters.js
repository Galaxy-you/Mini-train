import { formatStationName } from './stationHelper'
import { formatMoney } from './moneyHelper'

/**
 * 格式化站点名称，移除"站"字（如果有）
 * @param {string} name 站点名称
 * @returns {string} 格式化后的站点名称
 */
export function formatStation(name) {
  return formatStationName(name)
}

/**
 * 格式化金额
 * @param {number|object} price 金额或Money对象
 * @returns {string} 格式化后的金额
 */
export function formatPrice(price) {
  if (!price) return formatMoney(0)
  
  // 处理Money对象 {amount: xxx}
  if (typeof price === 'object' && price.amount !== undefined) {
    return formatMoney(price.amount)
  }
  
  return formatMoney(price)
}

/**
 * 格式化持续时间
 * @param {number} minutes 分钟数
 * @returns {string} 格式化后的时间
 */
export function formatDuration(minutes) {
  if (!minutes && minutes !== 0) return ''
  
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours === 0) {
    return `${mins}分钟`
  } else {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
}
