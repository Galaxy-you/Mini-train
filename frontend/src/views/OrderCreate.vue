/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="order-create-container">
    <!-- 步骤条 -->
    <el-card class="steps-card" shadow="never">
      <el-steps :active="1" align-center>
        <el-step title="选择车次" />
        <el-step title="填写订单" />
        <el-step title="完成支付" />
      </el-steps>
    </el-card>

    <div v-loading="loading">
      <!-- 车次信息 -->
      <el-card class="train-card" v-if="trainInfo" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Train /></el-icon>
            <span>车次信息</span>
          </div>
        </template>

        <div class="train-info-content">
          <div class="train-header">
            <span class="train-number">{{ trainInfo.trainNumber }}</span>
            <span class="travel-date">{{ formatDate(trainInfo.travelDate) }}</span>
          </div>

          <div class="train-journey">
            <div class="journey-point">
              <div class="time">{{ trainInfo.departureTime }}</div>
              <div class="station">{{ formatStation(trainInfo.startStation) }}</div>
            </div>

            <div class="journey-line">
              <div class="line-bar"></div>
              <div class="duration">{{ formatDuration(trainInfo.duration) }}</div>
              <div class="line-bar"></div>
            </div>

            <div class="journey-point">
              <div class="time">{{ trainInfo.arrivalTime }}</div>
              <div class="station">{{ formatStation(trainInfo.endStation) }}</div>
            </div>
          </div>

          <div class="seat-info">
            <el-tag size="large">{{ selectedSeatType }}</el-tag>
            <span class="price">单价：{{ formatPrice(seatPrice) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 选择乘车人 -->
      <el-card class="passenger-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>选择乘车人</span>
            <el-button type="primary" text @click="goToAddPassenger">
              <el-icon><Plus /></el-icon>
              添加乘车人
            </el-button>
          </div>
        </template>

        <div class="passenger-list" v-if="passengers.length > 0">
          <div
            class="passenger-item"
            v-for="passenger in passengers"
            :key="passenger.id"
            @click="togglePassenger(passenger.id)"
            :class="{ 'selected': selectedPassengers.includes(passenger.id) }"
          >
            <el-checkbox
              :model-value="selectedPassengers.includes(passenger.id)"
              @change="togglePassenger(passenger.id)"
            />
            <div class="passenger-info">
              <div class="passenger-name">{{ passenger.realName }}</div>
              <div class="passenger-id">{{ formatIdNumber(passenger.idNumber) }}</div>
            </div>
            <el-icon class="check-icon" v-if="selectedPassengers.includes(passenger.id)">
              <Check />
            </el-icon>
          </div>
        </div>

        <el-empty
          v-else
          description="暂无乘车人"
          :image-size="120"
        >
          <el-button type="primary" @click="goToAddPassenger">添加乘车人</el-button>
        </el-empty>
      </el-card>

      <!-- 订单汇总 -->
      <el-card class="summary-card" v-if="selectedPassengers.length > 0" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>订单汇总</span>
          </div>
        </template>

        <div class="summary-content">
          <div class="summary-row">
            <span class="label">票数</span>
            <span class="value">{{ selectedPassengers.length }} 张</span>
          </div>
          <div class="summary-row">
            <span class="label">单价</span>
            <span class="value">{{ formatPrice(seatPrice) }}</span>
          </div>
          <div class="summary-row total">
            <span class="label">总金额</span>
            <span class="value">{{ formatPrice(totalAmount) }}</span>
          </div>
        </div>

        <div class="submit-actions">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="submitOrder"
          >
            提交订单
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Train, User, Document, Plus, Check } from '@element-plus/icons-vue';
import { trainAPI, passengerAPI, orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'OrderCreate',
  components: {
    Train, User, Document, Plus, Check
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const loading = ref(false);
    const submitting = ref(false);
    const trainInfo = ref(null);
    const passengers = ref([]);
    const selectedPassengers = ref([]);
    const selectedSeatType = ref('');
    const seatPrice = ref(0);

    const totalAmount = computed(() => {
      return seatPrice.value * selectedPassengers.value.length;
    });

    const fetchTrainInfo = async () => {
      const trainId = route.query.trainId;
      if (!trainId) {
        ElMessage.error('缺少车次信息');
        goBack();
        return;
      }

      loading.value = true;
      try {
        const response = await trainAPI.getTrainDetail(trainId);
        const extractedData = dataHelper.extractApiData(response);
        trainInfo.value = dataHelper.adaptTrainData(extractedData);

        selectedSeatType.value = route.query.seatType || '';

        if (selectedSeatType.value && trainInfo.value.seatInfo) {
          const seat = trainInfo.value.seatInfo.find(s => s.seatType === selectedSeatType.value);
          if (seat) {
            // 处理 Money 对象类型的价格
            if (typeof seat.price === 'object' && seat.price !== null) {
              seatPrice.value = seat.price.amount || seat.price;
            } else {
              seatPrice.value = seat.price || 0;
            }
          }
        }
      } catch (error) {
        console.error('获取车次信息失败:', error);
        ElMessage.error('获取车次信息失败');
      } finally {
        loading.value = false;
      }
    };

    const fetchPassengers = async () => {
      try {
        const response = await passengerAPI.listPassengers();
        const extractedData = dataHelper.extractApiData(response);
        const passengerArray = dataHelper.ensureArray(extractedData);
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p));
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
      }
    };

    const togglePassenger = (passengerId) => {
      const index = selectedPassengers.value.indexOf(passengerId);
      if (index > -1) {
        selectedPassengers.value.splice(index, 1);
      } else {
        selectedPassengers.value.push(passengerId);
      }
    };

    const submitOrder = async () => {
      if (selectedPassengers.value.length === 0) {
        ElMessage.warning('请至少选择一位乘车人');
        return;
      }

      submitting.value = true;
      try {
        const orderData = {
          trainId: route.query.trainId,
          passengerIds: selectedPassengers.value,
          seatType: selectedSeatType.value,
          startStation: trainInfo.value.startStation,
          endStation: trainInfo.value.endStation,
          startStationId: trainInfo.value.startStationId,
          endStationId: trainInfo.value.endStationId
        };

        const response = await orderAPI.createOrder(orderData);
        const extractedData = dataHelper.extractApiData(response);

        ElMessage.success('订单创建成功');
        router.push(`/order/${extractedData.orderNo}`);
      } catch (error) {
        console.error('创建订单失败:', error);
        ElMessage.error('创建订单失败');
      } finally {
        submitting.value = false;
      }
    };

    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };

    const formatDate = (dateTime) => {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleDateString('zh-CN');
    };

    const goToAddPassenger = () => {
      router.push({
        path: '/passenger/edit',
        query: { redirect: route.fullPath }
      });
    };

    const goBack = () => {
      router.back();
    };

    onMounted(() => {
      fetchTrainInfo();
      fetchPassengers();
    });

    return {
      loading,
      submitting,
      trainInfo,
      passengers,
      selectedPassengers,
      selectedSeatType,
      seatPrice,
      totalAmount,
      togglePassenger,
      submitOrder,
      formatIdNumber,
      formatDate,
      formatStation,
      formatPrice,
      formatDuration,
      goToAddPassenger,
      goBack
    };
  }
}
</script>

<style scoped>
.order-create-container {
  max-width: 900px;
  margin: 0 auto;
}

/* 步骤卡片 */
.steps-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.steps-card :deep(.el-step__title) {
  font-size: 14px;
}

/* 通用卡片样式 */
.train-card,
.passenger-card,
.summary-card {
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

.card-header .el-button {
  margin-left: auto;
}

/* 车次信息 */
.train-info-content {
  padding: 8px 0;
}

.train-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.train-number {
  font-size: 28px;
  font-weight: 600;
  color: #1890ff;
}

.travel-date {
  font-size: 14px;
  color: #8c8c8c;
}

.train-journey {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.journey-point {
  flex: 1;
  text-align: center;
}

.journey-point .time {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.journey-point .station {
  font-size: 16px;
  color: #595959;
}

.journey-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
}

.line-bar {
  flex: 1;
  height: 2px;
  background: #d9d9d9;
}

.duration {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
}

.seat-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.seat-info .price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

/* 乘车人列表 */
.passenger-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.passenger-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.passenger-item:hover {
  border-color: #1890ff;
}

.passenger-item.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.passenger-info {
  flex: 1;
}

.passenger-name {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.passenger-id {
  font-size: 12px;
  color: #8c8c8c;
}

.check-icon {
  font-size: 20px;
  color: #1890ff;
}

/* 订单汇总 */
.summary-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.summary-row.total {
  border-bottom: none;
  padding-top: 16px;
  border-top: 2px solid #e8e8e8;
}

.summary-row .label {
  color: #8c8c8c;
  font-size: 14px;
}

.summary-row .value {
  font-weight: 500;
  color: #262626;
}

.summary-row.total .value {
  font-size: 28px;
  font-weight: 600;
  color: #ff4d4f;
}

.submit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
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

  .passenger-list {
    grid-template-columns: 1fr;
  }

  .submit-actions {
    flex-direction: column-reverse;
  }
}
</style>

