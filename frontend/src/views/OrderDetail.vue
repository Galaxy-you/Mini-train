/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="order-detail-container">
    <div v-loading="loading">
      <!-- 订单状态卡片 -->
      <el-card class="status-card" v-if="orderInfo" shadow="never">
        <div class="status-banner" :class="`status-${orderInfo.status?.toLowerCase()}`">
          <el-icon class="status-icon">
            <component :is="getStatusIcon(orderInfo.status)" />
          </el-icon>
          <div class="status-info">
            <h2 class="status-title">{{ getOrderStatusText(orderInfo.status) }}</h2>
            <p class="status-desc" v-if="orderInfo.status === 'UNPAID'">
              请尽快完成支付，订单将在30分钟内自动取消
            </p>
          </div>
        </div>
      </el-card>

      <!-- 行程信息卡片 -->
      <el-card class="journey-card" v-if="orderInfo" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><train /></el-icon>
            <span>行程信息</span>
          </div>
        </template>

        <div class="journey-content">
          <div class="train-header">
            <span class="train-number">{{ orderInfo.trainCode || orderInfo.trainNumber }}</span>
            <span class="travel-date">{{ formatDate(orderInfo.departureTime || orderInfo.startTime) }}</span>
          </div>

          <div class="journey-route">
            <div class="journey-point">
              <div class="point-time">{{ extractTime(orderInfo.departureTime || orderInfo.startTime) }}</div>
              <div class="point-station">{{ formatStation(orderInfo.startStation) }}</div>
            </div>

            <div class="journey-line">
              <div class="line-circle"></div>
              <div class="line-bar"></div>
              <div class="line-arrow">
                <el-icon><right /></el-icon>
              </div>
              <div class="line-bar"></div>
              <div class="line-circle"></div>
            </div>

            <div class="journey-point">
              <div class="point-time">{{ extractTime(orderInfo.arrivalTime || orderInfo.endTime) }}</div>
              <div class="point-station">{{ formatStation(orderInfo.endStation) }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 乘客车票信息 -->
      <el-card class="tickets-card" v-if="tickets.length > 0" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><user /></el-icon>
            <span>乘客信息（共{{ tickets.length }}张）</span>
          </div>
        </template>

        <div class="tickets-list">
          <div class="ticket-row" v-for="(ticket, index) in tickets" :key="ticket.ticketNo">
            <div class="ticket-index">{{ index + 1 }}</div>
            <div class="ticket-passenger">
              <div class="passenger-name">{{ ticket.passengerName }}</div>
              <div class="passenger-id">{{ formatIdNumber(ticket.passengerIdNumber) }}</div>
            </div>
            <div class="ticket-seat">
              <el-tag size="small">{{ ticket.seatType }}</el-tag>
              <span class="seat-number">{{ ticket.carNumber }}车{{ ticket.seatNumber }}号</span>
            </div>
            <div class="ticket-price">{{ formatPrice(ticket.price) }}</div>
          </div>
        </div>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="summary-card" v-if="orderInfo" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><document /></el-icon>
            <span>订单信息</span>
          </div>
        </template>

        <div class="summary-list">
          <div class="summary-row">
            <span class="summary-label">订单号</span>
            <span class="summary-value">{{ orderInfo.orderNo }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">下单时间</span>
            <span class="summary-value">{{ formatTime(orderInfo.createTime) }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">票数</span>
            <span class="summary-value">{{ tickets.length }} 张</span>
          </div>
          <div class="summary-row total-row">
            <span class="summary-label">订单总额</span>
            <span class="summary-value total-price">{{ formatPrice(orderInfo.totalAmount) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="orderInfo">
        <el-button size="large" @click="goBack">返回</el-button>
        <el-button
          type="danger"
          size="large"
          v-if="orderInfo.status === 'UNPAID'"
          @click="cancelOrder"
        >
          取消订单
        </el-button>
        <el-button
          type="primary"
          size="large"
          v-if="orderInfo.status === 'UNPAID'"
          @click="showPaymentDialog"
        >
          立即支付
        </el-button>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="!loading"
        description="订单不存在或已被删除"
        :image-size="200"
      >
        <el-button type="primary" @click="goBack">返回订单列表</el-button>
      </el-empty>
    </div>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialog.visible"
      title="订单支付"
      width="450px"
      :close-on-click-modal="false"
    >
      <div class="payment-container">
        <div class="payment-amount">
          <div class="amount-label">支付金额</div>
          <div class="amount-value">{{ formatPrice(paymentDialog.amount) }}</div>
        </div>
        
        <el-divider />

        <div class="payment-method">
          <div class="method-label">选择支付方式</div>
          <el-radio-group v-model="paymentDialog.method" class="payment-options">
            <el-radio :label="1" border size="large">
              <el-icon><WalletFilled /></el-icon>
              微信支付
            </el-radio>
            <el-radio :label="2" border size="large">
              <el-icon><CreditCard /></el-icon>
              支付宝
            </el-radio>
          </el-radio-group>
        </div>

        <div class="payment-tip" v-if="!paymentDialog.processing">
          <el-icon color="#faad14"><InfoFilled /></el-icon>
          <span>这是模拟支付，点击确认后将自动完成支付</span>
        </div>

        <div class="payment-progress" v-if="paymentDialog.processing">
          <el-progress
            :percentage="paymentDialog.progress"
            :status="paymentDialog.progress >= 100 ? 'success' : ''"
          />
          <p class="progress-text">{{ paymentDialog.progressText }}</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialog.visible = false" :disabled="paymentDialog.processing">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="simulatePayment"
            :loading="paymentDialog.processing"
            :disabled="paymentDialog.method === 0"
          >
            {{ paymentDialog.processing ? '支付中...' : '确认支付' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  SuccessFilled, WarningFilled, CircleClose,
  Train, User, Document, Right,
  WalletFilled, CreditCard, InfoFilled
} from '@element-plus/icons-vue';
import { orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'OrderDetail',
  components: {
    SuccessFilled, WarningFilled, CircleClose,
    Train, User, Document, Right,
    WalletFilled, CreditCard, InfoFilled
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const orderNo = route.params.orderNo;
    
    const loading = ref(false);
    const orderInfo = ref(null);
    const tickets = ref([]);
    
    const paymentDialog = reactive({
      visible: false,
      amount: 0,
      method: 0,
      processing: false,
      progress: 0,
      progressText: '正在处理支付...'
    });
    
    const fetchOrderDetail = async () => {
      loading.value = true;
      try {
        const response = await orderAPI.getOrderDetail(orderNo);
        const extractedData = dataHelper.extractApiData(response);
        orderInfo.value = extractedData;
        console.log('处理后的订单详情数据:', orderInfo.value);
        fetchOrderTickets();
      } catch (error) {
        console.error('获取订单详情失败:', error);
        ElMessage.error('获取订单详情失败');
        loading.value = false;
      }
    };
    
    // 获取订单车票列表
    const fetchOrderTickets = async () => {
      try {
        const response = await orderAPI.getOrderTickets(orderNo);
        const extractedData = dataHelper.extractApiData(response);
        const ticketArray = dataHelper.ensureArray(extractedData);
        // 使用适配器处理每个车票数据，确保证件号码正确显示
        tickets.value = ticketArray.map(ticket => dataHelper.adaptTicketData(ticket)) || [];
        console.log('处理后的订单车票数据:', tickets.value);
      } catch (error) {
        console.error('获取订单车票列表失败:', error);
        ElMessage.error('获取订单车票列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 取消订单
    const cancelOrder = () => {
      ElMessageBox.confirm('确定要取消该订单吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await orderAPI.cancelOrder(orderNo);
          ElMessage.success('订单取消成功');
          fetchOrderDetail(); // 刷新订单详情
        } catch (error) {
          console.error('取消订单失败:', error);
          ElMessage.error('取消订单失败');
        }
      }).catch(() => {});
    };
    
    // 返回订单列表
    const goBack = () => {
      router.push('/order');
    };
    
    // 显示支付对话框
    const showPaymentDialog = () => {
      if (!orderInfo.value || orderInfo.value.status !== 'UNPAID') {
        ElMessage.warning('订单状态错误，无法支付');
        return;
      }
      
      paymentDialog.amount = orderInfo.value.totalAmount || 0;
      paymentDialog.method = 0;
      paymentDialog.processing = false;
      paymentDialog.progress = 0;
      paymentDialog.progressText = '正在处理支付...';
      paymentDialog.visible = true;
    };
    
    // 模拟支付过程
    const simulatePayment = async () => {
      if (paymentDialog.method === 0) {
        ElMessage.warning('请先选择支付方式');
        return;
      }
      
      paymentDialog.processing = true;
      paymentDialog.progress = 0;
      paymentDialog.progressText = '正在处理支付...';
      
      const interval = setInterval(async () => {
        if (paymentDialog.progress >= 100) {
          clearInterval(interval);
          paymentDialog.progressText = '支付成功';
          
          try {
            // 调用后端接口确认支付
            if (!orderNo) {
              throw new Error('订单号未找到');
            }
            
            console.log('开始支付确认，订单号:', orderNo, '支付方式:', paymentDialog.method);
            await orderAPI.confirmPayment(orderNo, paymentDialog.method);
            
            // 延迟关闭对话框并显示成功消息
            setTimeout(() => {
              paymentDialog.processing = false;
              paymentDialog.visible = false;
              
              // 支付成功后显示消息并刷新订单详情
              ElMessageBox.alert(`支付成功！订单号: ${orderNo}`, '订单支付成功', {
                confirmButtonText: '确定',
                callback: () => {
                  fetchOrderDetail(); // 刷新订单详情
                }
              });
            }, 1000);
          } catch (error) {
            paymentDialog.processing = false;
            console.error('支付确认失败:', error);
            ElMessage.error('支付确认失败，请联系客服');
          }
        } else {
          paymentDialog.progress += 10;
          if (paymentDialog.progress >= 80) {
            paymentDialog.progressText = '即将完成...';
          } else if (paymentDialog.progress >= 60) {
            paymentDialog.progressText = '校验支付信息...';
          } else if (paymentDialog.progress >= 30) {
            paymentDialog.progressText = '请稍候...';
          }
        }
      }, 300);
    };
    
    // 取消支付
    const cancelPayment = () => {
      // 如果正在处理支付，不允许取消
      if (paymentDialog.processing) return;
      
      paymentDialog.visible = false;
    };
    
    // 获取订单状态文本
    const getOrderStatusText = (status) => {
      const statusMap = {
        'UNPAID': '待支付',
        'PAID': '已支付',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
      };
      return statusMap[status] || status;
    };
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN');
    };
    
    const extractDate = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts[0] || '';
    };

    const extractTime = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts[1]?.substring(0, 5) || timeStr;
    };

    const formatDate = (dateTime) => {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    };

    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };

    const getStatusIcon = (status) => {
      const iconMap = {
        'COMPLETED': SuccessFilled,
        'UNPAID': WarningFilled,
        'CANCELED': CircleClose
      };
      return iconMap[status] || WarningFilled;
    };

    onMounted(() => {
      if (orderNo) {
        fetchOrderDetail();
      } else {
        ElMessage.error('订单号不存在');
        goBack();
      }
    });

    return {
      loading,
      orderInfo,
      tickets,
      paymentDialog,
      cancelOrder,
      goBack,
      showPaymentDialog,
      simulatePayment,
      cancelPayment,
      getOrderStatusText,
      getStatusIcon,
      formatTime,
      formatDate,
      extractDate,
      extractTime,
      formatStation,
      formatPrice,
      formatIdNumber
    };
  }
}
</script>

<style scoped>
.order-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

/* 状态卡片 */
.status-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.status-banner {
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.status-banner.status-completed {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.status-banner.status-canceled {
  background: linear-gradient(135deg, #8c8c8c 0%, #bfbfbf 100%);
}

.status-icon {
  font-size: 56px;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.status-desc {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

/* 卡片通用样式 */
.journey-card,
.tickets-card,
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

/* 行程信息 */
.journey-content {
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

.journey-route {
  display: flex;
  align-items: center;
  gap: 32px;
}

.journey-point {
  flex: 1;
  text-align: center;
}

.point-time {
  font-size: 32px;
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
  min-width: 200px;
}

.line-circle {
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

.line-arrow {
  color: #1890ff;
  font-size: 24px;
}

/* 乘客车票列表 */
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.ticket-row:hover {
  background: #f0f0f0;
}

.ticket-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1890ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.ticket-passenger {
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

.ticket-seat {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seat-number {
  font-size: 14px;
  color: #595959;
}

.ticket-price {
  font-size: 20px;
  font-weight: 600;
  color: #ff4d4f;
  min-width: 100px;
  text-align: right;
}

/* 订单汇总 */
.summary-list {
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

.summary-row.total-row {
  border-bottom: none;
  padding-top: 16px;
  border-top: 2px solid #e8e8e8;
}

.summary-label {
  color: #8c8c8c;
  font-size: 14px;
}

.summary-value {
  color: #262626;
  font-weight: 500;
}

.total-price {
  font-size: 28px;
  font-weight: 600;
  color: #ff4d4f;
}

/* 操作按钮 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 0;
}

/* 支付对话框样式 */
.payment-container {
  padding: 12px 0;
}

.payment-amount {
  text-align: center;
  padding: 24px 0;
}

.amount-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 12px;
}

.amount-value {
  font-size: 36px;
  font-weight: 600;
  color: #ff4d4f;
}

.payment-method {
  margin: 24px 0;
}

.method-label {
  font-size: 14px;
  color: #595959;
  margin-bottom: 16px;
  font-weight: 500;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.payment-options :deep(.el-radio) {
  margin-right: 0;
  width: 100%;
}

.payment-options :deep(.el-radio.is-bordered) {
  padding: 16px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.payment-options :deep(.el-radio.is-bordered:hover) {
  border-color: #1890ff;
  background-color: #f0f5ff;
}

.payment-options :deep(.el-radio.is-bordered.is-checked) {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.payment-options :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.payment-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  margin-top: 16px;
  font-size: 13px;
  color: #595959;
}

.payment-progress {
  margin-top: 24px;
  text-align: center;
}

.progress-text {
  margin-top: 12px;
  font-size: 14px;
  color: #1890ff;
}

/* 响应式 */
@media (max-width: 768px) {
  .journey-route {
    flex-direction: column;
    gap: 16px;
  }

  .journey-line {
    transform: rotate(90deg);
    min-width: 100px;
  }

  .ticket-row {
    flex-wrap: wrap;
  }

  .action-bar {
    flex-direction: column;
  }

  .amount-value {
    font-size: 28px;
  }
}
</style>
