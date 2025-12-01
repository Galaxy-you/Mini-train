/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="ticket-list-container">
    <!-- 标签切换 -->
    <el-card class="tab-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我的车票" name="bought">
          <template #label>
            <span class="tab-label">
              <el-icon><ticket /></el-icon>
              我的车票
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 车票列表 -->
    <div class="ticket-list" v-loading="loading">
      <div
        class="ticket-card"
        v-for="ticket in tickets"
        :key="ticket.ticketNo"
        @click="viewTicket(ticket)"
      >
        <!-- 车票左侧：行程信息 -->
        <div class="ticket-journey">
          <div class="train-number">
            <span class="number">{{ ticket.trainNumber }}</span>
            <el-tag :type="getStatusType(ticket.status)" size="small">
              {{ getTicketStatusText(ticket.status) }}
            </el-tag>
          </div>
          
          <div class="journey-info">
            <div class="station-box">
              <div class="time">{{ ticket.departureTime }}</div>
              <div class="station">{{ formatStation(ticket.startStation) }}</div>
            </div>

            <div class="journey-line">
              <div class="line-dot"></div>
              <div class="line-bar"></div>
              <div class="line-dot"></div>
            </div>

            <div class="station-box">
              <div class="time">{{ ticket.arrivalTime }}</div>
              <div class="station">{{ formatStation(ticket.endStation) }}</div>
            </div>
          </div>

          <div class="ticket-date">
            {{ formatDate(ticket.travelDate) }}
          </div>
        </div>

        <!-- 车票中间：乘客和座位信息 -->
        <div class="ticket-details">
          <div class="detail-row">
            <el-icon class="detail-icon"><user /></el-icon>
            <span class="detail-label">乘车人</span>
            <span class="detail-value">{{ ticket.passengerName }}</span>
          </div>

          <div class="detail-row">
            <el-icon class="detail-icon"><location /></el-icon>
            <span class="detail-label">座位</span>
            <span class="detail-value">{{ ticket.carNumber }}车{{ ticket.seatNumber }}号</span>
          </div>

          <div class="detail-row">
            <el-icon class="detail-icon"><service /></el-icon>
            <span class="detail-label">席别</span>
            <span class="detail-value">{{ ticket.seatType }}</span>
          </div>
        </div>

        <!-- 车票右侧：价格和操作 -->
        <div class="ticket-actions-section">
          <div class="ticket-price">
            <span class="price-label">票价</span>
            <span class="price-value">{{ formatPrice(ticket.price) }}</span>
          </div>

          <div class="action-buttons">
            <el-button
              size="small"
              type="primary"
              @click.stop="viewTicket(ticket)"
            >
              查看详情
            </el-button>
            <el-button
              size="small"
              type="danger"
              v-if="ticket.status === 'VALID'"
              @click.stop="cancelTicket(ticket)"
            >
              退票
            </el-button>
          </div>

          <div class="ticket-no">
            票号：{{ ticket.ticketNo }}
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <el-empty
        v-if="tickets.length === 0 && !loading"
        description="暂无车票"
        :image-size="200"
      >
        <el-button type="primary" @click="$router.push('/train/search')">
          去购票
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Ticket, User, Location, Service } from '@element-plus/icons-vue';
import { ticketAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'TicketList',
  components: {
    Ticket, User, Location, Service
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const tickets = ref([]);
    const activeTab = ref('bought');

    const fetchBoughtTickets = async () => {
      loading.value = true;
      try {
        const response = await ticketAPI.listUserBoughtTickets();
        const extractedData = dataHelper.extractApiData(response);

        if (extractedData) {
          const ticketArray = dataHelper.ensureArray(extractedData);
          tickets.value = ticketArray.map(ticket => dataHelper.adaptTicketData(ticket));
        } else {
          tickets.value = [];
        }
      } catch (error) {
        console.error('获取车票列表失败:', error);
        tickets.value = [];
        ElMessage.error('获取车票失败');
      } finally {
        loading.value = false;
      }
    };

    const handleTabClick = () => {
      fetchBoughtTickets();
    };
    
    const viewTicket = (ticket) => {
      router.push(`/ticket/${ticket.ticketNo}`);
    };
    
    const cancelTicket = (ticket) => {
      ElMessageBox.confirm('确定要退票吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await ticketAPI.cancelTicket(ticket.ticketNo);
          ElMessage.success('退票成功');
          fetchBoughtTickets();
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
        'CANCELED': '已退票',
        'EXPIRED': '已过期'
      };
      return statusMap[status] || status;
    };

    const getStatusType = (status) => {
      const typeMap = {
        'VALID': 'success',
        'USED': 'info',
        'CANCELED': 'warning',
        'EXPIRED': 'danger'
      };
      return typeMap[status] || 'info';
    };

    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    };

    onMounted(() => {
      fetchBoughtTickets();
    });
    
    return {
      loading,
      tickets,
      activeTab,
      handleTabClick,
      viewTicket,
      cancelTicket,
      getTicketStatusText,
      getStatusType,
      formatStation,
      formatPrice,
      formatDate
    };
  }
}
</script>

<style scoped>
.ticket-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 标签卡片 */
.tab-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 车票列表 */
.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 车票卡片 - 类似真实火车票 */
.ticket-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
  border-left: 4px solid #1890ff;
}

.ticket-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 行程信息 */
.ticket-journey {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.train-number {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.train-number .number {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.journey-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.station-box {
  flex: 1;
  text-align: center;
}

.station-box .time {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.station-box .station {
  font-size: 14px;
  color: #595959;
}

.journey-line {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
}

.line-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1890ff;
}

.line-bar {
  flex: 1;
  height: 2px;
  background: #d9d9d9;
  min-width: 40px;
}

.ticket-date {
  font-size: 14px;
  color: #8c8c8c;
  text-align: center;
}

/* 详细信息 */
.ticket-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 24px;
  border-left: 1px dashed #e8e8e8;
  border-right: 1px dashed #e8e8e8;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-icon {
  color: #1890ff;
  font-size: 16px;
}

.detail-label {
  color: #8c8c8c;
  font-size: 14px;
  min-width: 60px;
}

.detail-value {
  color: #262626;
  font-size: 14px;
  font-weight: 500;
}

/* 操作区域 */
.ticket-actions-section {
  flex: 0 0 180px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-end;
}

.ticket-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.price-label {
  font-size: 12px;
  color: #8c8c8c;
}

.price-value {
  font-size: 28px;
  font-weight: 600;
  color: #ff4d4f;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  width: 120px;
  min-height: 68px;
}

.action-buttons .el-button {
  width: 120px !important;
  flex-shrink: 0;
  padding: 8px 15px;
  box-sizing: border-box;
}

.ticket-no {
  font-size: 12px;
  color: #8c8c8c;
  text-align: right;
}

/* 响应式 */
@media (max-width: 768px) {
  .ticket-card {
    flex-direction: column;
  }

  .ticket-journey {
    flex: 1;
  }

  .ticket-details {
    border-left: none;
    border-right: none;
    border-top: 1px dashed #e8e8e8;
    border-bottom: 1px dashed #e8e8e8;
    padding: 16px 0;
  }

  .ticket-actions-section {
    flex: 1;
    align-items: stretch;
  }

  .ticket-price {
    align-items: flex-start;
  }
}
</style>
