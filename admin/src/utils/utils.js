import { formatMoney } from '@/utils/moneyHelper'

/**
 * 格式化站点名称，移除"站"字（如果有）
 * @param {string} name 站点名称
 * @returns {string} 格式化后的站点名称
 */
export function formatStationName(name) {
  if (!name) return ''
  return name.endsWith('站') ? name.slice(0, -1) : name
}

/**
 * 恢复站点名称，添加"站"字（如果没有）
 * @param {string} name 站点名称
 * @returns {string} 完整的站点名称
 */
export function restoreStationName(name) {
  if (!name) return ''
  return name.endsWith('站') ? name : `${name}站`
}

/**
 * 格式化金额
 * @param {number} amount 金额
 * @returns {string} 格式化后的金额
 */
export function formatPrice(amount) {
  return formatMoney(amount)
}

/**
 * 格式化持续时间
 * @param {number} minutes 分钟数
 * @returns {string} 格式化后的时间
 */
export function formatDuration(minutes) {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
  return `${mins}分钟`
}
