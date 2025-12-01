/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="train-detail-container">
    <div v-loading="loading">
      <!-- 列车信息卡片 -->
      <el-card class="train-card" v-if="train" shadow="hover">
        <div class="train-header">
          <div class="train-number-section">
            <span class="train-number">{{ train.trainNumber }}</span>
            <el-tag :type="getTrainTypeTag(train.type)" size="large">{{ train.type }}</el-tag>
          </div>
          <el-button @click="goBack" circle>
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <div class="train-journey">
          <div class="journey-point">
            <div class="point-time">{{ train.departureTime }}</div>
            <div class="point-station">{{ formatStation(train.startStation) }}</div>
          </div>

          <div class="journey-line">
            <div class="line-start"></div>
            <div class="line-bar"></div>
            <div class="duration-badge">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDuration(train.duration) }}</span>
            </div>
            <div class="line-bar"></div>
            <div class="line-end"></div>
          </div>

          <div class="journey-point">
            <div class="point-time">{{ train.arrivalTime }}</div>
            <div class="point-station">{{ formatStation(train.endStation) }}</div>
          </div>
        </div>
      </el-card>

      <!-- 座位选择卡片 -->
      <el-card class="seats-card" v-if="train" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Tickets /></el-icon>
            <span>座位选择</span>
          </div>
        </template>

        <div class="seats-grid">
          <div
              class="seat-option"
              v-for="seat in seatInfoArray"
              :key="seat.seatType"
              @click="selectSeat(seat)"
              :class="{
              'selected': selectedSeat?.seatType === seat.seatType,
              'disabled': seat.remaining <= 0
            }"
          >
            <div class="seat-type">{{ seat.seatType }}</div>
            <div class="seat-price">{{ formatPrice(seat.price) }}</div>
            <div class="seat-remaining" :class="{ 'low': seat.remaining < 10 }">
              {{ seat.remaining > 0 ? `余${seat.remaining}张` : '无票' }}
            </div>
            <div class="seat-action">
              <el-icon v-if="selectedSeat?.seatType === seat.seatType"><Check /></el-icon>
              <span v-else>{{ seat.remaining > 0 ? '选择' : '售罄' }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 预订表单 -->
      <el-card class="booking-card" v-if="selectedSeat" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>确认预订</span>
          </div>
        </template>

        <div class="booking-summary">
          <div class="summary-item">
            <span class="label">车次</span>
            <span class="value">{{ train.trainNumber }}</span>
          </div>
          <div class="summary-item">
            <span class="label">座位类型</span>
            <span class="value">{{ selectedSeat.seatType }}</span>
          </div>
          <div class="summary-item">
            <span class="label">票价</span>
            <span class="value price">{{ formatPrice(selectedSeat.price) }}</span>
          </div>
        </div>

        <div class="booking-actions">
          <el-button size="large" @click="selectedSeat = null">取消</el-button>
          <el-button type="primary" size="large" @click="proceedToBooking">
            继续预订
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty
          v-else-if="!loading && !train"
          description="车次信息不存在"
          :image-size="200"
      >
        <el-button type="primary" @click="goBack">返回</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Close, Clock, Tickets, User, Check } from '@element-plus/icons-vue';
import { trainAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'TrainDetail',
  components: {
    Close, Clock, Tickets, User, Check
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const trainId = route.params.id;

    const loading = ref(false);
    const train = ref(null);
    const selectedSeat = ref(null);

    const seatInfoArray = computed(() => {
      if (!train.value?.seatInfo) return [];
      return dataHelper.ensureArray(train.value.seatInfo);
    });

    const getTrainTypeTag = (type) => {
      const typeMap = {
        '高铁': 'danger',
        '动车': 'warning',
        '特快': 'success',
        '快速': 'info'
      };
      return typeMap[type] || 'info';
    };

    const fetchTrainDetail = async () => {
      loading.value = true;
      try {
        const response = await trainAPI.getTrainDetail(trainId);
        const extractedData = dataHelper.extractApiData(response);
        train.value = dataHelper.adaptTrainData(extractedData);
      } catch (error) {
        console.error('获取车次详情失败:', error);
        ElMessage.error('获取车次详情失败');
      } finally {
        loading.value = false;
      }
    };

    const selectSeat = (seat) => {
      if (seat.remaining <= 0) {
        ElMessage.warning('该座位类型已售罄');
        return;
      }
      selectedSeat.value = seat;
    };

    const proceedToBooking = () => {
      const queryParams = {
        trainId: trainId,
        seatType: selectedSeat.value.seatType,
        startStation: train.value.startStation,
        endStation: train.value.endStation
      };
      router.push({
        path: '/order/create',
        query: queryParams
      });
    };

    const goBack = () => {
      router.back();
    };

    onMounted(() => {
      if (trainId) {
        fetchTrainDetail();
      } else {
        ElMessage.error('车次ID不存在');
        goBack();
      }
    });

    return {
      loading,
      train,
      selectedSeat,
      seatInfoArray,
      getTrainTypeTag,
      selectSeat,
      proceedToBooking,
      goBack,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.train-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

/* 列车卡片 */
.train-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.train-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.train-number-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.train-number {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
}

.train-journey {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 32px 0;
}

.journey-point {
  flex: 1;
  text-align: center;
}

.point-time {
  font-size: 36px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.point-station {
  font-size: 18px;
  color: #595959;
}

.journey-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 280px;
}

.line-start,
.line-end {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1890ff;
}

.line-bar {
  flex: 1;
  height: 2px;
  background: #d9d9d9;
}

.duration-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #e6f7ff;
  border-radius: 16px;
  color: #1890ff;
  font-size: 14px;
  white-space: nowrap;
}

/* 座位选择 */
.seats-card,
.booking-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 16px;
}

.card-header .el-icon {
  color: #1890ff;
  font-size: 18px;
}

.seats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.seat-option {
  padding: 20px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.seat-option:hover:not(.disabled) {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.seat-option.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.seat-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.seat-type {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.seat-price {
  font-size: 24px;
  font-weight: 600;
  color: #ff4d4f;
}

.seat-remaining {
  font-size: 12px;
  color: #52c41a;
}

.seat-remaining.low {
  color: #faad14;
}

.seat-action {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  color: #1890ff;
}

/* 预订摘要 */
.booking-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.summary-item .label {
  color: #8c8c8c;
}

.summary-item .value {
  font-weight: 500;
  color: #262626;
}

.summary-item .price {
  font-size: 24px;
  font-weight: 600;
  color: #ff4d4f;
}

.booking-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .train-journey {
    flex-direction: column;
    gap: 16px;
  }

  .journey-line {
    transform: rotate(90deg);
    min-width: 100px;
  }

  .seats-grid {
    grid-template-columns: 1fr;
  }

  .booking-actions {
    flex-direction: column-reverse;
  }
}
</style>

