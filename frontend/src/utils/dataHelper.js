/**
 * 数据处理帮助函数
 */

/**
 * 确保数据是数组类型
 * 如果传入的值不是数组，则返回空数组
 * @param {any} data 要检查的数据
 * @param {Array} defaultValue 默认值，如果data不是数组时返回
 * @returns {Array} 确保是数组类型的结果
 */
export function ensureArray(data, defaultValue = []) {
  if (Array.isArray(data)) {
    return data;
  } else {
    console.warn('期望数组类型，但收到:', typeof data, data);
    return defaultValue;
  }
}

/**
 * 确保数据是对象类型
 * 如果传入的值不是对象或为null，则返回空对象
 * @param {any} data 要检查的数据
 * @param {Object} defaultValue 默认值，如果data不是对象时返回
 * @returns {Object} 确保是对象类型的结果
 */
export function ensureObject(data, defaultValue = {}) {
  if (data && typeof data === 'object' && !Array.isArray(data)) {
    return data;
  } else {
    console.warn('期望对象类型，但收到:', typeof data, data);
    return defaultValue;
  }
}

/**
 * 安全地从API响应中提取数据
 * 处理标准的后端响应格式 {success, message, data}
 * @param {Object} response API响应对象
 * @param {any} defaultValue 如果无法提取数据时返回的默认值
 * @returns {any} 提取的数据或默认值
 */
export function extractApiData(response, defaultValue = null) {
  try {
    // 检查响应格式
    if (!response) {
      console.error('API响应为空');
      return defaultValue;
    }
    
    // 如果响应有标准结构，提取data字段
    if (response.success === true && response.data !== undefined) {
      return response.data;
    }
    
    // 其他情况尝试直接返回响应
    return response || defaultValue;
  } catch (error) {
    console.error('提取API数据时出错:', error);
    return defaultValue;
  }
}

/**
 * 适配车票DTO数据为前端所需的格式
 * 处理后端返回的TicketDetailDTO，确保其具有前端组件所需的字段和格式
 * @param {Object} ticketDTO 后端返回的TicketDetailDTO对象
 * @returns {Object} 适配后的票务对象
 */
export function adaptTicketData(ticketDTO) {
  if (!ticketDTO) return null;
  
  const adaptedTicket = {...ticketDTO};
  
  // 确保车次编号的一致性
  if (!adaptedTicket.trainNumber && adaptedTicket.trainCode) {
    adaptedTicket.trainNumber = adaptedTicket.trainCode;
  }
  
  // 确保时间字段的一致性
  if (!adaptedTicket.departureTime && adaptedTicket.startTime) {
    adaptedTicket.departureTime = adaptedTicket.startTime;
  }
  
  if (!adaptedTicket.arrivalTime && adaptedTicket.endTime) {
    adaptedTicket.arrivalTime = adaptedTicket.endTime;
  }
  
  // 处理座位信息
  if (adaptedTicket.seatInfo && !adaptedTicket.carNumber && !adaptedTicket.seatNumber) {
    // 尝试解析完整的座位信息，如"12车14A号"
    const match = /(\d+)车(\w+)号/.exec(adaptedTicket.seatInfo);
    if (match) {
      adaptedTicket.carNumber = match[1];
      adaptedTicket.seatNumber = match[2];
    }
  }
  
  // 确保证件号码的一致性
  if (!adaptedTicket.passengerIdNumber && adaptedTicket.passengerCard) {
    adaptedTicket.passengerIdNumber = adaptedTicket.passengerCard;
  }
  
  // 确保票状态的一致性
  if (typeof adaptedTicket.status === 'number') {
    // 数值状态转换为字符串状态
    switch (adaptedTicket.status) {
      case 0:
        adaptedTicket.statusText = '已取消';
        adaptedTicket.status = 'CANCELLED';
        break;
      case 1:
        adaptedTicket.statusText = '正常';
        adaptedTicket.status = 'VALID';
        break;
      case 2:
        adaptedTicket.statusText = '已检票';
        adaptedTicket.status = 'CHECKED';
        break;
      case 3:
        adaptedTicket.statusText = '已改签';
        adaptedTicket.status = 'CHANGED';
        break;
      case 4:
        adaptedTicket.statusText = '已过期';
        adaptedTicket.status = 'EXPIRED';
        break;
      default:
        adaptedTicket.statusText = '未知状态';
        adaptedTicket.status = 'UNKNOWN';
    }
  }
  
  return adaptedTicket;
}

/**
 * 适配列车DTO数据为前端所需的格式
 * 处理后端返回的TrainDetailDTO，确保其具有前端组件所需的字段和格式
 * @param {Object} trainDTO 后端返回的TrainDetailDTO对象
 * @returns {Object} 适配后的列车对象
 */
export function adaptTrainData(trainDTO) {
  if (!trainDTO) return null;
  
  const adaptedTrain = {...trainDTO};
  
  // 确保车次编号的一致性
  if (!adaptedTrain.trainNumber && adaptedTrain.code) {
    adaptedTrain.trainNumber = adaptedTrain.code;
  }
  
  // 确保时间字段的一致性
  if (!adaptedTrain.departureTime && adaptedTrain.startTime) {
    adaptedTrain.departureTime = adaptedTrain.startTime;
  }
  
  if (!adaptedTrain.arrivalTime && adaptedTrain.endTime) {
    adaptedTrain.arrivalTime = adaptedTrain.endTime;
  }
  
  return adaptedTrain;
}

/**
 * 适配乘车人数据为前端所需的格式
 * 处理后端返回的Passenger对象，确保其具有前端组件所需的字段和格式
 * @param {Object} passenger 后端返回的Passenger对象
 * @returns {Object} 适配后的乘车人对象
 */
export function adaptPassengerData(passenger) {
  if (!passenger) return null;
  
  const adaptedPassenger = {...passenger};
  
  // 确保idNumber和cardId的一致性
  if (!adaptedPassenger.idNumber && adaptedPassenger.cardId) {
    adaptedPassenger.idNumber = adaptedPassenger.cardId;
  }
  
  // 确保idType和cardType的一致性
  if (!adaptedPassenger.idType && adaptedPassenger.cardType) {
    // 将文字类型转换为数值类型，与前端保持一致
    switch (adaptedPassenger.cardType) {
      case '身份证':
        adaptedPassenger.idType = '1';
        break;
      case '护照':
        adaptedPassenger.idType = '2';
        break;
      case '军官证':
        adaptedPassenger.idType = '3';
        break;
      case '港澳通行证':
        adaptedPassenger.idType = '4';
        break;
      default:
        adaptedPassenger.idType = '1'; // 默认为身份证
    }
  }
  
  return adaptedPassenger;
}

export default {
  ensureArray,
  ensureObject,
  extractApiData,
  adaptTicketData,
  adaptTrainData,
  adaptPassengerData
};
