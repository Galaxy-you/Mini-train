/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="order-detail-container page-container">
    <h2 class="page-title">订单详情</h2>
    
    <div v-loading="loading">
      <el-card class="order-info-card" v-if="orderInfo">
        <div class="order-header">
          <div class="order-status">
            <div class="status-text">{{ getOrderStatusText(orderInfo.status) }}</div>
            <div class="status-desc" v-if="orderInfo.status === 'UNPAID'">
              请尽快完成支付，订单将在30分钟内自动取消
            </div>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="train-info">
          <div class="train-number">{{ orderInfo.trainCode || orderInfo.trainNumber }}</div>
          <div class="train-route">
            <div class="station-time">
              <div class="time">{{ extractTime(orderInfo.departureTime || orderInfo.startTime) }}</div>
              <div class="date">{{ extractDate(orderInfo.departureTime || orderInfo.startTime) }}</div>
            </div>
            <div class="station-name">{{ formatStation(orderInfo.startStation) }}</div>
          </div>
          <div class="route-divider">
            <div class="arrow"></div>
          </div>
          <div class="train-route">
            <div class="station-time">
              <div class="time">{{ extractTime(orderInfo.arrivalTime || orderInfo.endTime) }}</div>
              <div class="date">{{ extractDate(orderInfo.arrivalTime || orderInfo.endTime) }}</div>
            </div>
            <div class="station-name">{{ formatStation(orderInfo.endStation) }}</div>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="ticket-list">
          <h3>车票信息</h3>
          <div class="ticket-item" v-for="ticket in tickets" :key="ticket.ticketNo">
            <div class="passenger-info">
              <div class="passenger-name">{{ ticket.passengerName }}</div>
              <div class="passenger-id">{{ ticket.passengerIdNumber }}</div>
            </div>
            <div class="seat-info">
              <div>{{ ticket.carNumber }}车</div>
              <div>{{ ticket.seatNumber }}号</div>
              <div>{{ ticket.seatType }}</div>
            </div>
            <div class="ticket-price">
              {{ formatPrice(ticket.price) }}
            </div>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号：</span>
            <span class="value">{{ orderInfo.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ formatTime(orderInfo.createTime) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">票数：</span>
            <span class="value">{{ tickets.length }} 张</span>
          </div>
          <div class="summary-item">
            <span class="label">总金额：</span>
            <span class="value price">{{ formatPrice(orderInfo.totalAmount) }}</span>
          </div>
        </div>
        
        <div class="order-actions">
          <el-button type="primary" v-if="orderInfo.status === 'UNPAID'" @click="showPaymentDialog">去支付</el-button>
          <el-button type="danger" v-if="orderInfo.status === 'UNPAID'" @click="cancelOrder">取消订单</el-button>
          <el-button @click="goBack">返回</el-button>
        </div>
      </el-card>
      
      <el-empty v-else-if="!loading" description="订单不存在或已被删除"></el-empty>
    </div>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialog.visible"
      title="订单支付"
      width="400px"
    >
      <div class="payment-container">
        <div class="payment-amount">
          <p>支付金额</p>
          <h2>{{ formatPrice(paymentDialog.amount) }}</h2>
        </div>
        
        <div class="payment-method">
          <h4>支付方式</h4>
          <div class="payment-options">
            <div 
              v-if="paymentDialog.method === 1" 
              class="payment-qrcode wechat"
            >
              <div class="qrcode-container">
                <img src="/img/wechat-pay-mock.png" alt="微信支付" class="qrcode-placeholder" />
              </div>
              <p>请使用微信扫一扫</p>
            </div>
            
            <div 
              v-else-if="paymentDialog.method === 2" 
              class="payment-qrcode alipay"
            >
              <div class="qrcode-container">
                <img src="/img/alipay-mock.png" alt="支付宝支付" class="qrcode-placeholder" />
              </div>
              <p>请使用支付宝扫一扫</p>
            </div>
            
            <div 
              v-else class="payment-method-selector"
            >
              <el-radio-group v-model="paymentDialog.method">
                <el-radio :label="1">微信支付</el-radio>
                <el-radio :label="2">支付宝</el-radio>
              </el-radio-group>
              
              <div style="margin-top: 20px; text-align: center;">
                <el-button type="primary" @click="paymentDialog.method">选择支付方式</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="payment-progress" v-if="paymentDialog.processing">
        <el-progress 
          :percentage="paymentDialog.progress" 
          :status="paymentDialog.progress >= 100 ? 'success' : ''"
        />
        <p>{{ paymentDialog.progressText }}</p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPayment">取消支付</el-button>
          <el-button type="primary" @click="simulatePayment" :loading="paymentDialog.processing">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'OrderDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const orderNo = route.params.orderNo;
    
    // 调试订单号获取
    console.log('=== OrderDetail 调试信息 ===');
    console.log('当前路由:', route.path);
    console.log('路由参数:', route.params);
    console.log('获取到的订单号:', orderNo);
    console.log('订单号类型:', typeof orderNo);
    
    const loading = ref(false);
    const orderInfo = ref(null);
    const tickets = ref([]);
    
    // 支付对话框数据
    const paymentDialog = reactive({
      visible: false,
      amount: 0,
      method: 0, // 0-未选择, 1-微信，2-支付宝
      processing: false,
      progress: 0,
      progressText: '正在处理支付...'
    });
    
    // 获取订单详情
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
    
    // 从时间中提取日期
    const extractDate = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts[0] || '';
    };
    
    // 从时间中提取时间部分
    const extractTime = (timeStr) => {
      if (!timeStr) return '';
      const parts = timeStr.split(' ');
      return parts.length > 1 ? parts[1] : timeStr;
    };
    
    onMounted(() => {
      fetchOrderDetail();
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
      formatTime,
      extractDate,
      extractTime,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
}

.order-info-card {
  margin-bottom: 20px;
}

.order-header {
  text-align: center;
}

.status-text {
  font-size: 24px;
  color: #409EFF;
  font-weight: bold;
  margin-bottom: 10px;
}

.status-desc {
  color: #909399;
  font-size: 14px;
}

.train-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 30px 0;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-right: 30px;
}

.train-route {
  text-align: center;
}

.station-time {
  margin-bottom: 5px;
}

.time {
  font-size: 24px;
  font-weight: bold;
}

.date {
  font-size: 14px;
  color: #909399;
}

.station-name {
  font-size: 16px;
}

.route-divider {
  width: 100px;
  height: 2px;
  background-color: #dcdfe6;
  margin: 0 20px;
  position: relative;
}

.arrow {
  position: absolute;
  right: -5px;
  top: -4px;
  width: 10px;
  height: 10px;
  border-top: 2px solid #dcdfe6;
  border-right: 2px solid #dcdfe6;
  transform: rotate(45deg);
}

.ticket-list {
  margin-top: 20px;
}

.ticket-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px dashed #dcdfe6;
}

.ticket-item:last-child {
  border-bottom: none;
}

.passenger-info {
  flex: 1;
}

.passenger-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.passenger-id {
  color: #909399;
  font-size: 14px;
}

.seat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 20px;
}

.ticket-price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}

.order-summary {
  margin-top: 20px;
}

.summary-item {
  margin-bottom: 10px;
}

.label {
  color: #909399;
}

.value {
  margin-left: 10px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}

.order-actions {
  margin-top: 30px;
  text-align: center;
}

/* 支付对话框样式 */
.payment-container {
  text-align: center;
}

.payment-amount h2 {
  color: #F56C6C;
  font-size: 24px;
}

.payment-method {
  margin-top: 20px;
}

.payment-options {
  margin-top: 10px;
}

.payment-qrcode {
  text-align: center;
}

.qrcode-container {
  margin: 20px auto;
  width: 200px;
  height: 200px;
  background-color: #f2f2f2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-placeholder {
  max-width: 100%;
  max-height: 100%;
}

.payment-progress {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
