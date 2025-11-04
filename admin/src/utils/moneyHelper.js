/**
 * 金额工具类
 */

/**
 * 格式化金额为人民币显示
 * @param {number} amount 金额（元）
 * @returns {string} 格式化后的金额，如 ￥99.00
 */
export function formatMoney(amount) {
  if (amount === undefined || amount === null) return '￥0.00'
  return `￥${amount.toFixed(2)}`
}

/**
 * 将分转换为元
 * @param {number} fen 金额（分）
 * @returns {number} 金额（元）
 */
export function fenToYuan(fen) {
  return fen / 100
}

/**
 * 将元转换为分
 * @param {number} yuan 金额（元）
 * @returns {number} 金额（分）
 */
export function yuanToFen(yuan) {
  return Math.round(yuan * 100)
}
