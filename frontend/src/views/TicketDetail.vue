/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="ticket-detail-container">
    <div v-loading="loading">
      <!-- 车票卡片 - 仿真火车票设计 -->
      <div class="ticket-wrapper" v-if="ticket">
        <div class="ticket-main">
          <!-- 状态横幅 -->
          <div class="ticket-status" :class="`status-${ticket.status?.toLowerCase()}`">
            <el-icon class="status-icon">
              <component :is="getStatusIcon(ticket.status)" />
            </el-icon>
            <span>{{ getTicketStatusText(ticket.status) }}</span>
          </div>

          <!-- 车票内容 -->
          <div class="ticket-content">
            <!-- 顶部：车次和日期 -->
            <div class="ticket-top">
              <div class="train-code">{{ ticket.trainNumber }}</div>
              <div class="ticket-no">票号：{{ ticket.ticketNo }}</div>
            </div>

            <!-- 中部：行程信息 -->
            <div class="journey-section">
              <div class="station departure">
                <div class="time">{{ extractTime(ticket.departureTime) }}</div>
                <div class="name">{{ formatStation(ticket.startStation) }}</div>
                <div class="date">{{ extractDate(ticket.departureTime) }}</div>
              </div>

              <div class="journey-arrow">
                <div class="arrow-line"></div>
                <el-icon class="arrow-icon"><Right /></el-icon>
              </div>

              <div class="station arrival">
                <div class="time">{{ extractTime(ticket.arrivalTime) }}</div>
                <div class="name">{{ formatStation(ticket.endStation) }}</div>
                <div class="date">{{ extractDate(ticket.arrivalTime) }}</div>
              </div>
            </div>

            <!-- 乘客信息 -->
            <div class="passenger-section">
              <div class="info-row">
                <span class="label">乘车人</span>
                <span class="value">{{ ticket.passengerName }}</span>
              </div>
              <div class="info-row">
                <span class="label">证件号</span>
                <span class="value">{{ formatIdNumber(ticket.passengerIdNumber) }}</span>
              </div>
              <div class="info-row">
                <span class="label">席位</span>
                <span class="value">{{ ticket.carNumber }}车{{ ticket.seatNumber }}号</span>
              </div>
              <div class="info-row">
                <span class="label">席别</span>
                <span class="value">{{ ticket.seatType }}</span>
              </div>
            </div>

            <!-- 价格 -->
            <div class="price-section">
              <span class="price-label">票价</span>
              <span class="price-value">{{ formatPrice(ticket.price) }}</span>
            </div>
          </div>
        </div>

        <!-- 票根线 -->
        <div class="ticket-perforation"></div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="ticket">
        <el-button size="large" @click="goBack">返回</el-button>
        <el-button
            size="large"
            type="info"
            @click="showRouteDialog"
        >
          查看路线
        </el-button>
        <el-button
            size="large"
            type="warning"
            v-if="ticket.status === 'VALID'"
            @click="showChangeDialog"
        >
          改签
        </el-button>
        <el-button
            type="danger"
            size="large"
            v-if="ticket.status === 'VALID'"
            @click="cancelTicket"
        >
          退票
        </el-button>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-else-if="!loading"
          description="车票不存在"
          :image-size="200"
      >
        <el-button type="primary" @click="goBack">返回</el-button>
      </el-empty>
    </div>
    
    <!-- 改签对话框 -->
    <el-dialog
        v-model="changeDialogVisible"
        title="车票改签"
        width="800px"
        :close-on-click-modal="false"
    >
      <div v-loading="changingTrainsLoading">
        <!-- 行程信息 -->
        <div class="route-info" v-if="ticket">
          <div class="route-stations">
            <span class="station-label">起点：</span>
            <span class="station-name">{{ formatStation(ticket.startStation) }}</span>
            <el-icon class="route-arrow"><Right /></el-icon>
            <span class="station-label">终点：</span>
            <span class="station-name">{{ formatStation(ticket.endStation) }}</span>
          </div>
        </div>
        
        <el-alert
            title="改签规则"
            type="info"
            :closable="false"
            style="margin-bottom: 20px"
        >
          <p>只能改签到相同起点和终点的其他车次，座位类型可以改变。</p>
        </el-alert>
        
        <div class="train-list" v-if="availableTrains.length > 0">
          <div
              v-for="train in availableTrains"
              :key="train.id"
              class="train-item"
              :class="{ selected: selectedTrain?.id === train.id }"
              @click="selectTrain(train)"
          >
            <div class="train-header">
              <span class="train-code">{{ train.trainNumber }}</span>
              <span class="train-type">{{ train.type }}</span>
            </div>
            <div class="train-time">
              <span>{{ train.departureTime }}</span>
              <span class="arrow">→</span>
              <span>{{ train.arrivalTime }}</span>
            </div>
            <div class="seat-types">
              <el-tag
                  v-for="seat in train.seatInfo"
                  :key="seat.seatType"
                  :type="selectedSeatType === seat.seatType && selectedTrain?.id === train.id ? 'success' : ''"
                  @click.stop="selectSeatType(train, seat.seatType)"
                  class="seat-tag"
              >
                <span class="seat-type-name">{{ seat.seatType }}</span>
                <span class="seat-price">{{ formatPrice(seat.price) }}</span>
                <span class="seat-count">余{{ seat.remaining }}张</span>
              </el-tag>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无可改签的车次" />
        
        <!-- 当前选择提示 -->
        <div v-if="selectedTrain && selectedSeatType" class="selection-info">
          <el-alert type="success" :closable="false">
            <template #title>
              已选择：{{ selectedTrain.trainNumber }} - {{ selectedSeatType }}
            </template>
          </el-alert>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="changeDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="confirmChange"
            :disabled="!selectedTrain || !selectedSeatType"
        >
          确认改签
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 路线对话框 -->
    <el-dialog
        v-model="routeDialogVisible"
        title="列车路线"
        width="700px"
        :close-on-click-modal="false"
    >
      <div v-loading="routeLoading">
        <div class="route-header" v-if="ticket">
          <div class="route-train-info">
            <span class="route-train-number">{{ ticket.trainNumber }}</span>
            <el-tag :type="getTrainTypeTag(ticket.trainType)" class="route-train-type">
              {{ ticket.trainType }}
            </el-tag>
          </div>
          <div class="route-summary">
            <span class="route-station">{{ formatStation(ticket.startStation) }}</span>
            <el-icon class="route-arrow-icon"><Right /></el-icon>
            <span class="route-station">{{ formatStation(ticket.endStation) }}</span>
          </div>
        </div>
        
        <el-divider />
        
        <div v-if="trainRoutes.length > 0" class="route-list">
          <div 
              v-for="(station, index) in trainRoutes" 
              :key="station.id"
              class="route-station-item"
          >
            <div class="route-timeline">
              <div class="route-dot" :class="{ 'route-dot-start': index === 0, 'route-dot-end': index === trainRoutes.length - 1 }"></div>
              <div v-if="index < trainRoutes.length - 1" class="route-line"></div>
            </div>
            
            <div class="route-station-content">
              <div class="route-station-name">
                <span class="station-order">{{ station.stationOrder }}</span>
                <span class="station-name">{{ station.stationName }}</span>
              </div>
              
              <div class="route-station-time">
                <div v-if="station.arriveTime" class="time-info">
                  <span class="time-label">到达：</span>
                  <span class="time-value">{{ formatTime(station.arriveTime) }}</span>
                </div>
                <div v-if="station.departTime" class="time-info">
                  <span class="time-label">出发：</span>
                  <span class="time-value">{{ formatTime(station.departTime) }}</span>
                </div>
                <div v-if="station.stopTime" class="time-info stop-time">
                  <span class="time-label">停留：</span>
                  <span class="time-value">{{ station.stopTime }}分钟</span>
                </div>
              </div>
              
              <div v-if="station.distance" class="route-distance">
                <el-icon><Location /></el-icon>
                <span>{{ station.distance }}公里</span>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无路线信息" />
      </div>
      
      <template #footer>
        <el-button @click="routeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { SuccessFilled, WarningFilled, CircleClose, Right, Location } from '@element-plus/icons-vue';
import { ticketAPI, trainAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'TicketDetail',
  components: {
    SuccessFilled, WarningFilled, CircleClose, Right, Location
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const ticketNo = route.params.ticketNo;

    const loading = ref(false);
    const ticket = ref(null);
    const changeDialogVisible = ref(false);
    const changingTrainsLoading = ref(false);
    const availableTrains = ref([]);
    const selectedTrain = ref(null);
    const selectedSeatType = ref(null);
    const routeDialogVisible = ref(false);
    const routeLoading = ref(false);
    const trainRoutes = ref([]);

    const fetchTicketDetail = async () => {
      loading.value = true;
      try {
        const response = await ticketAPI.getTicketDetail(ticketNo);
        const extractedData = dataHelper.extractApiData(response);
        ticket.value = dataHelper.adaptTicketData(extractedData);
      } catch (error) {
        console.error('获取车票详情失败:', error);
        ElMessage.error('获取车票详情失败');
      } finally {
        loading.value = false;
      }
    };

    const cancelTicket = () => {
      ElMessageBox.confirm('确定要退票吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await ticketAPI.cancelTicket(ticketNo);
          ElMessage.success('退票成功');
          fetchTicketDetail();
        } catch (error) {
          console.error('退票失败:', error);
          ElMessage.error('退票失败');
        }
      }).catch(() => {});
    };

    const getTicketStatusText = (status) => {
      const statusMap = {
        'VALID': '有效',
        'USED': '已使用',
        'CANCELED': '已取消',
        'CANCELLED': '已取消',
        'EXPIRED': '已过期',
        'CHECKED': '已検票',
        'CHANGED': '已改签'
      };
      return statusMap[status] || status;
    };

    const getStatusIcon = (status) => {
      const iconMap = {
        'VALID': SuccessFilled,
        'USED': CircleClose,
        'CANCELED': WarningFilled,
        'CANCELLED': WarningFilled,
        'EXPIRED': WarningFilled,
        'CHECKED': CircleClose,
        'CHANGED': WarningFilled
      };
      return iconMap[status] || WarningFilled;
    };

    const extractTime = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts[1]?.substring(0, 5) || timeStr;
    };

    const extractDate = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts[0] || '';
    };

    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };

    const goBack = () => {
      router.push('/ticket');
    };
    
    const showRouteDialog = async () => {
      if (!ticket.value || !ticket.value.trainId) {
        ElMessage.warning('车次信息不存在');
        return;
      }
      
      routeDialogVisible.value = true;
      routeLoading.value = true;
      trainRoutes.value = [];
      
      try {
        const response = await trainAPI.getTrainRoute(ticket.value.trainId);
        const extractedData = dataHelper.extractApiData(response);
        trainRoutes.value = dataHelper.ensureArray(extractedData);
        
        if (trainRoutes.value.length === 0) {
          ElMessage.info({
            message: '该车次暂无详细的路线信息。\n\n可能的原因：\n1. 该车次为直达列车，无中途停靠站\n2. 系统尚未录入该车次的详细路线数据\n3. 数据库初始化时未包含此车次的路线信息\n\n建议：\n• 尝试查看其他车次的路线信息\n• 联系系统管理员确认数据完整性',
            dangerouslyUseHTMLString: true,
            duration: 10000
          });
        }
      } catch (error) {
        console.error('获取车次路线失败:', error);
        ElMessage.error('获取车次路线失败: ' + (error.response?.data?.message || error.message));
      } finally {
        routeLoading.value = false;
      }
    };
    
    const formatTime = (timeStr) => {
      if (!timeStr) return '';
      // 处理 HH:mm:ss 格式
      if (typeof timeStr === 'string' && timeStr.includes(':')) {
        return timeStr.substring(0, 5); // 只显示 HH:mm
      }
      return timeStr;
    };
    
    const getTrainTypeTag = (type) => {
      const typeMap = {
        '高铁': 'danger',
        '动车': 'warning',
        '特快': 'success',
        '快车': 'info',
        '快速': 'info'
      };
      return typeMap[type] || 'info';
    };
    
    const showChangeDialog = async () => {
      changeDialogVisible.value = true;
      changingTrainsLoading.value = true;
      selectedTrain.value = null;
      selectedSeatType.value = null;
      
      try {
        // 搜索相同起点和终点的其他列车
        const response = await trainAPI.searchTrains(
          ticket.value.startStation,
          ticket.value.endStation
        );
        const extractedData = dataHelper.extractApiData(response);
        const trains = dataHelper.ensureArray(extractedData);
        
        // 过滤掉当前车次
        availableTrains.value = trains.filter(t => t.id !== ticket.value.trainId);
        
        if (availableTrains.value.length === 0) {
          ElMessage.info('暂无可改签的车次');
        }
      } catch (error) {
        console.error('获取可改签车次失败:', error);
        ElMessage.error('获取可改签车次失败');
      } finally {
        changingTrainsLoading.value = false;
      }
    };
    
    const selectTrain = (train) => {
      console.log('Selected train:', train);
      selectedTrain.value = train;
      selectedSeatType.value = null;
    };
    
    const selectSeatType = (train, seatType) => {
      console.log('Selected seat type:', seatType, 'for train:', train);
      selectedTrain.value = train;
      selectedSeatType.value = seatType;
    };
    
    const confirmChange = async () => {
      if (!selectedTrain.value || !selectedSeatType.value) {
        ElMessage.warning('请选择车次和座位类型');
        return;
      }
      
      ElMessageBox.confirm(
        `确认改签到 ${selectedTrain.value.trainNumber} 的 ${selectedSeatType.value}？`,
        '确认改签',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          // 获取当前日期（简化处理，实际应该由用户选择）
          const today = new Date();
          const travelDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
          
          const changeRequest = {
            originalTicketNo: ticketNo,
            newTrainId: selectedTrain.value.id,
            newSeatType: selectedSeatType.value,
            travelDate: travelDate
          };
          
          await ticketAPI.changeTicket(changeRequest);
          ElMessage.success('改签成功');
          changeDialogVisible.value = false;
          
          // 重新加载车票详情
          fetchTicketDetail();
        } catch (error) {
          console.error('改签失败:', error);
          ElMessage.error(error.response?.data?.message || '改签失败');
        }
      }).catch(() => {});
    };

    onMounted(() => {
      if (ticketNo) {
        fetchTicketDetail();
      } else {
        ElMessage.error('车票号不存在');
        goBack();
      }
    });

    return {
      loading,
      ticket,
      changeDialogVisible,
      changingTrainsLoading,
      availableTrains,
      selectedTrain,
      selectedSeatType,
      routeDialogVisible,
      routeLoading,
      trainRoutes,
      cancelTicket,
      getTicketStatusText,
      getStatusIcon,
      extractTime,
      extractDate,
      formatIdNumber,
      formatStation,
      formatPrice,
      goBack,
      showRouteDialog,
      showChangeDialog,
      selectTrain,
      selectSeatType,
      confirmChange,
      formatTime,
      getTrainTypeTag
    };
  }
}
</script>

<style scoped>
.ticket-detail-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0;
}

/* 车票包装器 */
.ticket-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 24px;
}

/* 车票主体 */
.ticket-main {
  padding: 0;
}

/* 状态横幅 */
.ticket-status {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.ticket-status.status-valid {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.ticket-status.status-used {
  background: linear-gradient(135deg, #8c8c8c 0%, #bfbfbf 100%);
}

.ticket-status.status-canceled {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.ticket-status.status-cancelled {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.ticket-status.status-expired {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.ticket-status.status-checked {
  background: linear-gradient(135deg, #8c8c8c 0%, #bfbfbf 100%);
}

.ticket-status.status-changed {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.status-icon {
  font-size: 20px;
}

/* 车票内容 */
.ticket-content {
  padding: 24px;
}

.ticket-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.train-code {
  font-size: 28px;
  font-weight: 600;
  color: #1890ff;
}

.ticket-no {
  font-size: 12px;
  color: #8c8c8c;
}

/* 行程区域 */
.journey-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.station {
  flex: 1;
  text-align: center;
}

.station .time {
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.station .name {
  font-size: 16px;
  color: #595959;
  margin-bottom: 4px;
}

.station .date {
  font-size: 12px;
  color: #8c8c8c;
}

.journey-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.arrow-line {
  width: 60px;
  height: 2px;
  background: #d9d9d9;
}

.arrow-icon {
  font-size: 20px;
  color: #1890ff;
}

/* 乘客信息 */
.passenger-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f0f0f0;
  border-radius: 8px;
}

.info-row {
  display: flex;
  gap: 8px;
}

.info-row .label {
  color: #8c8c8c;
  font-size: 14px;
  min-width: 60px;
}

.info-row .value {
  color: #262626;
  font-weight: 500;
  font-size: 14px;
}

/* 价格区域 */
.price-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px dashed #d9d9d9;
}

.price-label {
  font-size: 14px;
  color: #8c8c8c;
}

.price-value {
  font-size: 32px;
  font-weight: 600;
  color: #ff4d4f;
}
  /* 票根线 */
  .ticket-perforation {
    height: 24px;
    background: linear-gradient(to right, #fff 50%, transparent 0);
    background-size: 12px 100%;
    background-repeat: repeat-x;
    position: relative;
  }

  .ticket-perforation::before,
  .ticket-perforation::after {
    content: '';
    position: absolute;
    top: 0;
    width: 12px;
    height: 100%;
    background: #f0f0f0;
  }

  .ticket-perforation::before {
    left: 0;
  }

  .ticket-perforation::after {
    right: 0;
  }

  /* 操作按钮 */
  .action-bar {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 24px 0;
  }

  /* 响应式 */
  @media (max-width: 768px) {
    .journey-section {
      flex-direction: column;
      gap: 16px;
    }

    .journey-arrow {
      transform: rotate(90deg);
    }

    .passenger-section {
      grid-template-columns: 1fr;
    }

    .action-bar {
      flex-direction: column;
    }

    .action-bar .el-button {
      width: 100%;
    }
  }
  
  /* 路线对话框样式 */
  .route-header {
    text-align: center;
    margin-bottom: 20px;
  }
  
  .route-train-info {
    margin-bottom: 15px;
  }
  
  .route-train-number {
    font-size: 24px;
    font-weight: 600;
    color: #1890ff;
    margin-right: 10px;
  }
  
  .route-train-type {
    font-size: 14px;
  }
  
  .route-summary {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }
  
  .route-station {
    font-size: 18px;
    font-weight: 500;
  }
  
  .route-arrow-icon {
    font-size: 20px;
    color: #1890ff;
  }
  
  .route-list {
    max-height: 400px;
    overflow-y: auto;
  }
  
  .route-station-item {
    display: flex;
    padding: 15px 0;
  }
  
  .route-timeline {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 15px;
  }
  
  .route-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #1890ff;
    border: 2px solid #fff;
    box-shadow: 0 0 0 2px #1890ff;
  }
  
  .route-dot-start {
    background: #52c41a;
    box-shadow: 0 0 0 2px #52c41a;
  }
  
  .route-dot-end {
    background: #ff4d4f;
    box-shadow: 0 0 0 2px #ff4d4f;
  }
  
  .route-line {
    width: 2px;
    height: 100%;
    background: #1890ff;
    flex-grow: 1;
  }
  
  .route-station-content {
    flex: 1;
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 15px;
  }
  
  .route-station-name {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .station-order {
    display: inline-block;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    background: #1890ff;
    color: white;
    border-radius: 50%;
    font-size: 12px;
    font-weight: 600;
    margin-right: 10px;
  }
  
  .station-name {
    font-size: 18px;
    font-weight: 500;
  }
  
  .route-station-time {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 10px;
  }
  
  .time-info {
    display: flex;
    align-items: center;
  }
  
  .time-label {
    color: #8c8c8c;
    font-size: 14px;
    margin-right: 5px;
  }
  
  .time-value {
    font-weight: 500;
  }
  
  .stop-time {
    color: #faad14;
  }
  
  .route-distance {
    display: flex;
    align-items: center;
    color: #8c8c8c;
    font-size: 14px;
  }
  
  .route-distance .el-icon {
    margin-right: 5px;
  }
  
  /* 改签对话框样式 */
  .route-info {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }
  
  .route-stations {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: #fff;
  }
  
  .station-label {
    font-size: 14px;
    font-weight: 500;
    opacity: 0.9;
  }
  
  .station-name {
    font-size: 18px;
    font-weight: 600;
  }
  
  .route-arrow {
    font-size: 20px;
    margin: 0 8px;
  }
  
  .train-list {
    max-height: 400px;
    overflow-y: auto;
  }
  
  .train-item {
    padding: 16px;
    margin-bottom: 12px;
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
  }
  
  .train-item:hover {
    border-color: #1890ff;
    background: #f0f9ff;
  }
  
  .train-item.selected {
    border-color: #1890ff;
    background: #e6f7ff;
  }
  
  .train-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .train-code {
    font-size: 20px;
    font-weight: 600;
    color: #1890ff;
  }
  
  .train-type {
    font-size: 14px;
    color: #8c8c8c;
  }
  
  .train-time {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 16px;
    color: #262626;
  }
  
  .train-time .arrow {
    color: #1890ff;
  }
  
  .seat-types {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .seat-tag {
    margin: 4px;
    padding: 8px 12px;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    min-width: 120px;
  }
  
  .seat-tag:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .seat-type-name {
    font-weight: 600;
    font-size: 14px;
  }
  
  .seat-price {
    font-size: 16px;
    color: #ff4d4f;
    font-weight: 600;
  }
  
  .seat-count {
    font-size: 12px;
    color: #8c8c8c;
  }
  
  .selection-info {
    margin-top: 20px;
  }
</style>

