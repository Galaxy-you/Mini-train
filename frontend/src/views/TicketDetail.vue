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
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { SuccessFilled, WarningFilled, CircleClose, Right } from '@element-plus/icons-vue';
import { ticketAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'TicketDetail',
  components: {
    SuccessFilled, WarningFilled, CircleClose, Right
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const ticketNo = route.params.ticketNo;

    const loading = ref(false);
    const ticket = ref(null);

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
        'CANCELED': '已退票',
        'EXPIRED': '已过期'
      };
      return statusMap[status] || status;
    };

    const getStatusIcon = (status) => {
      const iconMap = {
        'VALID': SuccessFilled,
        'USED': CircleClose,
        'CANCELED': WarningFilled,
        'EXPIRED': WarningFilled
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
      cancelTicket,
      getTicketStatusText,
      getStatusIcon,
      extractTime,
      extractDate,
      formatIdNumber,
      formatStation,
      formatPrice,
      goBack
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

.ticket-status.status-expired {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
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
</style>

