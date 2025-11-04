
/**
 * 数据处理工具类
 */
export const dataHelper = {
  /**
   * 确保数据为数组类型
   * 用于处理 API 返回的数据，防止 Element Plus 表格组件报错
   * @param {*} data 需要确认为数组的数据
   * @param {Array} defaultValue 默认数组值，当 data 不是数组时返回此值
   * @returns {Array} 确保返回数组类型数据
   */
  ensureArray(data, defaultValue = []) {
    // 如果数据为空，返回空数组
    if (data === null || data === undefined) {
      return defaultValue;
    }

    // 如果已经是数组，直接返回
    if (Array.isArray(data)) {
      return data;
    }

    console.warn('预期数组类型但收到非数组数据:', data);
    
    // 如果数据是对象，尝试转换为数组
    if (typeof data === 'object') {
      // 如果对象有 content 属性且为数组，返回该数组（分页数据情况）
      if (data.content && Array.isArray(data.content)) {
        return data.content;
      }
      
      // 如果是对象但没有可用的数组属性，返回包含该对象的数组
      return [data];
    }
    
    // 其他情况返回默认空数组
    return defaultValue;
  },
  
  /**
   * 处理分页数据
   * @param {*} data API返回的分页数据
   * @returns {Object} 处理后的分页数据对象 {content: Array, totalElements: Number}
   */
  handlePaginationData(data) {
    // 处理空数据情况
    if (!data) {
      return {
        content: [],
        totalElements: 0
      };
    }

    // 处理包裹在 Result 中的数据
    if (data.success && data.data) {
      data = data.data;
    }
    
    // 如果数据已经是标准分页格式
    if (data.content && typeof data.totalElements === 'number') {
      // 确保 content 是数组
      const content = this.ensureArray(data.content);
      return {
        content,
        totalElements: data.totalElements
      };
    }
    
    // 如果数据直接是数组
    if (Array.isArray(data)) {
      return {
        content: data,
        totalElements: data.length
      };
    }
    
    // 其他情况按空数据处理
    console.warn('无法识别的分页数据格式:', data);
    return {
      content: [],
      totalElements: 0
    };
  }
};
