/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="ticket-list-container page-container">
    <h2 class="page-title">车票管理</h2>
    
    <el-card v-loading="loading">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我购买的车票" name="bought"></el-tab-pane>
        <el-tab-pane label="我的乘车记录" name="passenger" v-if="hasPassengers"></el-tab-pane>
      </el-tabs>
      
      <div v-if="activeTab === 'passenger' && !selectedPassengerId">
        <div class="passenger-select">
          <p>请选择乘车人查看票务：</p>
          <el-select v-model="selectedPassengerId" placeholder="选择乘车人" @change="fetchPassengerTickets">
            <el-option
              v-for="passenger in passengers"
              :key="passenger.id"
              :label="passenger.realName"
              :value="passenger.id"
            />
          </el-select>
        </div>
      </div>
      
      <div class="ticket-list" v-if="tickets.length > 0">
        <div class="ticket-item" v-for="ticket in tickets" :key="ticket.ticketNo">
          <div class="ticket-header">
            <div class="ticket-no">车票号：{{ ticket.ticketNo }}</div>
            <div class="ticket-status">{{ getTicketStatusText(ticket.status) }}</div>
          </div>
          
          <div class="ticket-content">
            <div class="train-info">
              <div class="train-number">{{ ticket.trainNumber }}</div>
              <div class="train-route">
                <div class="departure">
                  <div class="time">{{ ticket.departureTime }}</div>
                  <div class="station">{{ formatStation(ticket.startStation) }}</div>
                </div>
                <div class="route-arrow">
                  <div class="arrow-line"></div>
                </div>
                <div class="arrival">
                  <div class="time">{{ ticket.arrivalTime }}</div>
                  <div class="station">{{ formatStation(ticket.endStation) }}</div>
                </div>
              </div>
            </div>
            
            <div class="passenger-info">
              <div class="passenger-name">{{ ticket.passengerName }}</div>
              <div class="seat-info">{{ ticket.carNumber }}车 {{ ticket.seatNumber }}号 ({{ ticket.seatType }})</div>
            </div>
            
            <div class="price-info">
              <div class="price">{{ formatPrice(ticket.price) }}</div>
            </div>
          </div>
          
          <div class="ticket-footer">
            <div class="order-info">订单号：{{ ticket.orderNo }}</div>
            <div class="ticket-actions">
              <el-button 
                size="small" 
                @click="viewTicket(ticket)"
              >查看详情</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="cancelTicket(ticket)"
                v-if="ticket.status === 'VALID'"
              >退票</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty description="暂无车票" v-else-if="!loading"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ticketAPI, passengerAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'TicketList',
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const tickets = ref([]);
    const activeTab = ref('bought');
    const passengers = ref([]);
    const selectedPassengerId = ref(null);
    
    // 是否有乘车人
    const hasPassengers = computed(() => {
      return passengers.value.length > 0;
    });
    
    // 获取我购买的车票
    const fetchBoughtTickets = async () => {
      loading.value = true;
      try {
        // 添加超时处理
        const timeoutPromise = new Promise((_, reject) => 
          setTimeout(() => reject(new Error('请求超时')), 10000)
        );
        
        const responsePromise = ticketAPI.listUserBoughtTickets();
        const response = await Promise.race([responsePromise, timeoutPromise]);
        
        console.log('获取到的车票列表响应:', response);
        
        // 确保response是一个有效的响应对象
        if (!response) {
          throw new Error('服务器未返回有效数据');
        }
        
        // 使用try-catch细化处理数据提取过程，便于调试
        try {
          const extractedData = dataHelper.extractApiData(response);
          console.log('提取的数据:', extractedData);
          
          if (extractedData === null || extractedData === undefined) {
            console.warn('从API响应中提取的数据为空');
            tickets.value = [];  // 设置为空数组而不是抛出错误
          } else {
            // 使用适配器处理票务数据
            const ticketArray = dataHelper.ensureArray(extractedData);
            tickets.value = ticketArray.map(ticket => dataHelper.adaptTicketData(ticket));
            console.log('处理后的车票数据数组:', tickets.value);
          }
        } catch (dataError) {
          console.error('处理API返回数据时出错:', dataError);
          tickets.value = [];  // 出错时设为空数组
          ElMessage.warning('数据格式异常，请联系管理员');
        }
      } catch (error) {
        console.error('获取车票列表失败:', error);
        tickets.value = [];  // 确保tickets至少是空数组
        
        // 提供更具体的错误信息
        if (error.response && error.response.status) {
          const status = error.response.status;
          if (status === 401) {
            ElMessage.error('未授权，请重新登录');
          } else if (status === 500) {
            ElMessage.error('服务器内部错误，请稍后再试');
          } else {
            ElMessage.error(`获取车票失败 (${status}): ${error.message || '未知错误'}`);
          }
        } else {
          ElMessage.error('获取车票失败: ' + (error.message || '网络错误'));
        }
      } finally {
        loading.value = false;
      }
    };
    
    // 获取乘车人列表
    const fetchPassengers = async () => {
      try {
        const response = await passengerAPI.listPassengers();
        console.log('获取到的乘车人列表响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        const passengerArray = dataHelper.ensureArray(extractedData);
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p));
        console.log('处理后的乘车人数据:', passengers.value);
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
      }
    };
    
    // 获取乘车人的票
    const fetchPassengerTickets = async (passengerId) => {
      if (!passengerId) return;
      
      loading.value = true;
      try {
        // 添加详细日志
        console.log('开始获取乘车人车票，乘车人ID:', passengerId);
        
        const response = await ticketAPI.listUserTickets(passengerId);
        console.log('获取到的乘车人车票列表响应:', response);
        
        // 确保response是一个有效的响应对象
        if (!response) {
          throw new Error('服务器未返回有效数据');
        }
        
        try {
          const extractedData = dataHelper.extractApiData(response);
          console.log('提取的乘车人票据数据:', extractedData);
          
          if (extractedData === null || extractedData === undefined) {
            console.warn('从API响应中提取的乘车人票据数据为空');
            tickets.value = [];
          } else {
            // 使用适配器处理票务数据
            const ticketArray = dataHelper.ensureArray(extractedData);
            tickets.value = ticketArray.map(ticket => dataHelper.adaptTicketData(ticket));
            console.log('处理后的乘车人车票数组:', tickets.value);
          }
        } catch (dataError) {
          console.error('处理乘车人车票数据时出错:', dataError);
          tickets.value = [];
          ElMessage.warning('数据格式异常，请联系管理员');
        }
      } catch (error) {
        console.error('获取乘车人车票列表失败:', error);
        tickets.value = [];
        
        // 优化错误信息
        if (error.response && error.response.status) {
          const status = error.response.status;
          ElMessage.error(`获取乘车人车票失败 (${status}): ${error.message || '请稍后重试'}`);
        } else {
          ElMessage.error('获取乘车人车票失败: ' + (error.message || '网络错误'));
        }
      } finally {
        loading.value = false;
      }
    };
    
    // 处理标签切换
    const handleTabClick = () => {
      if (activeTab.value === 'bought') {
        selectedPassengerId.value = null;
        fetchBoughtTickets();
      } else if (activeTab.value === 'passenger') {
        selectedPassengerId.value = null;
        tickets.value = [];
      }
    };
    
    // 查看车票详情
    const viewTicket = (ticket) => {
      router.push(`/ticket/${ticket.ticketNo}`);
    };
    
    // 退票
    const cancelTicket = (ticket) => {
      ElMessageBox.confirm('确定要退票吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await ticketAPI.cancelTicket(ticket.ticketNo);
          ElMessage.success('退票成功');
          
          // 根据当前标签页刷新数据
          if (activeTab.value === 'bought') {
            fetchBoughtTickets();
          } else if (activeTab.value === 'passenger' && selectedPassengerId.value) {
            fetchPassengerTickets(selectedPassengerId.value);
          }
        } catch (error) {
          console.error('退票失败:', error);
          ElMessage.error('退票失败');
        }
      }).catch(() => {});
    };
    
    // 获取票状态文本
    const getTicketStatusText = (status) => {
      const statusMap = {
        'VALID': '有效',
        'USED': '已使用',
        'CANCELED': '已退票',
        'EXPIRED': '已过期'
      };
      return statusMap[status] || status;
    };
    
    onMounted(() => {
      fetchBoughtTickets();
      fetchPassengers();
    });
    
    return {
      loading,
      tickets,
      activeTab,
      passengers,
      selectedPassengerId,
      hasPassengers,
      handleTabClick,
      fetchPassengerTickets,
      viewTicket,
      cancelTicket,
      getTicketStatusText,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.ticket-list-container {
  padding: 20px;
}

.passenger-select {
  margin: 20px 0;
  text-align: center;
}

.ticket-list {
  margin-top: 20px;
}

.ticket-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 15px;
  overflow: hidden;
}

.ticket-header {
  background-color: #f5f7fa;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #dcdfe6;
}

.ticket-no {
  font-weight: bold;
}

.ticket-status {
  color: #409EFF;
  font-weight: bold;
}

.ticket-content {
  padding: 15px;
}

.train-info {
  margin-bottom: 15px;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #409EFF;
}

.train-route {
  display: flex;
  align-items: center;
}

.departure, .arrival {
  text-align: center;
}

.time {
  font-size: 18px;
  font-weight: bold;
}

.station {
  margin-top: 5px;
}

.route-arrow {
  flex: 1;
  margin: 0 15px;
  position: relative;
}

.arrow-line {
  height: 2px;
  background-color: #dcdfe6;
  position: relative;
}

.arrow-line:after {
  content: '';
  position: absolute;
  right: -5px;
  top: -4px;
  width: 10px;
  height: 10px;
  border-top: 2px solid #dcdfe6;
  border-right: 2px solid #dcdfe6;
  transform: rotate(45deg);
}

.passenger-info {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
}

.passenger-name {
  font-weight: bold;
}

.price-info {
  margin-top: 10px;
  text-align: right;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}

.ticket-footer {
  padding: 12px 15px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #dcdfe6;
}

.order-info {
  color: #909399;
  font-size: 14px;
}
</style>
