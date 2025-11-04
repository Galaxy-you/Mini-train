/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="train-detail-container page-container">
    <h2 class="page-title">车次详情</h2>
    
    <div v-loading="loading">
      <el-card class="train-info-card" v-if="train">
        <div class="train-header">
          <div class="train-number">{{ train.trainNumber }}</div>
          <div class="train-route">{{ formatStation(train.startStation) }} → {{ formatStation(train.endStation) }}</div>
        </div>
        
        <el-divider />
        
        <div class="train-time-info">
          <div class="departure">
            <div class="time">{{ train.departureTime }}</div>
            <div class="station">{{ formatStation(train.startStation) }}</div>
          </div>
          <div class="duration">
            <div class="arrow-line"></div>
            <div class="duration-time">{{ formatDuration(train.duration) }}</div>
          </div>
          <div class="arrival">
            <div class="time">{{ train.arrivalTime }}</div>
            <div class="station">{{ formatStation(train.endStation) }}</div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="ticket-info">
          <h3>票价信息</h3>
          <el-table :data="seatInfoArray" stripe>
            <el-table-column prop="seatType" label="座位类型" />
            <el-table-column label="票价(元)">
              <template #default="scope">
                {{ formatPrice(scope.row.price) }}
              </template>
            </el-table-column>
            <el-table-column prop="remaining" label="剩余票数" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button @click="selectSeat(scope.row)" type="primary" size="small" 
                  :disabled="scope.row.remaining <= 0">预订</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="booking-form" v-if="selectedSeat">
          <h3>填写订票信息</h3>
          <el-form :model="bookingForm" ref="bookingFormRef" :rules="bookingRules" label-width="100px">
            <el-form-item label="座位类型">
              <span>{{ selectedSeat.seatType }}</span>
            </el-form-item>
            <el-form-item label="票价">
              <span>{{ formatPrice(selectedSeat.price) }}（单张票价）</span>
            </el-form-item>
            <el-form-item label="总票价" v-if="bookingForm.passengerIds.length > 0">
              <span class="total-price">￥{{ calculateTotalPrice(bookingForm.passengerIds.length, selectedSeat.price) }}</span>
            </el-form-item>
            <el-form-item label="乘车人" prop="passengerIds">
              <el-checkbox-group v-model="bookingForm.passengerIds">
                <el-checkbox 
                  v-for="item in passengers" 
                  :key="item.id" 
                  :label="item.id"
                  border
                >
                  {{ item.realName }} ({{ formatIdNumber(item.idNumber || item.cardId) }})
                </el-checkbox>
              </el-checkbox-group>
              <div style="margin-top: 10px;">
                <el-button type="primary" plain size="small" @click="goToPassengerManage">添加乘车人</el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitBooking" :loading="submitting">提交订单</el-button>
              <el-button @click="cancelSeatSelection">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
      
      <el-empty v-else-if="!loading" description="未找到车次信息"></el-empty>
    </div>
    
    <div class="action-row">
      <el-button @click="goBack">返回</el-button>
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
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { trainAPI, passengerAPI, orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'TrainDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const trainId = route.params.id;
    
    // 从路由中获取查询参数
    const startStation = ref(route.query.startStation || '');
    const endStation = ref(route.query.endStation || '');
    const startStationId = ref(route.query.startStationId ? Number(route.query.startStationId) : null);
    const endStationId = ref(route.query.endStationId ? Number(route.query.endStationId) : null);
    
    const train = ref(null);
    const loading = ref(false);
    const submitting = ref(false);
    const passengers = ref([]);
    const selectedSeat = ref(null);
    const bookingFormRef = ref(null);
    
    const bookingForm = reactive({
      passengerIds: []
    });
    
    const bookingRules = {
      passengerIds: [
        { type: 'array', required: true, message: '请至少选择一位乘车人', trigger: 'change' }
      ]
    };
    
    // 支付对话框数据
    const paymentDialog = reactive({
      visible: false,
      amount: 0,
      method: 0, // 0-未选择, 1-微信，2-支付宝
      processing: false,
      progress: 0,
      progressText: '正在处理支付...',
      orderNo: '',
      orderData: null
    });
    
    const seatInfoArray = computed(() => {
      if (!train.value || !train.value.seatInfo) return [];
      return train.value.seatInfo;
    });

    // 获取车次详情
    const fetchTrainDetail = async () => {
      loading.value = true;
      try {
        const response = await trainAPI.getTrainDetail(trainId);
        console.log('获取车次详情响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        
        // 使用adaptTrainData处理train数据，确保有正确的属性
        if (extractedData) {
          train.value = dataHelper.adaptTrainData(extractedData);
          
          // 如果没有seatInfo字段，创建一个默认的空数组
          if (!train.value.seatInfo) {
            train.value.seatInfo = [];
          }
          
          console.log('处理后的车次详情:', train.value);
        } else {
          train.value = null;
          ElMessage.warning('未找到车次信息');
        }
      } catch (error) {
        console.error('获取车次详情失败:', error);
        ElMessage.error('获取车次详情失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 获取乘车人列表
    const fetchPassengers = async () => {
      try {
        const response = await passengerAPI.listPassengers();
        console.log('获取乘车人列表响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        const passengerArray = dataHelper.ensureArray(extractedData);
        // 使用适配器处理乘车人数据，确保idType和idNumber字段正确
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p));
        console.log('处理后的乘车人列表:', passengers.value);
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
        ElMessage.error('获取乘车人列表失败');
      }
    };
    
    // 选择座位类型
    const selectSeat = (seat) => {
      selectedSeat.value = seat;
      bookingForm.passengerIds = [];
    };
    
    // 取消座位选择
    const cancelSeatSelection = () => {
      selectedSeat.value = null;
      bookingForm.passengerIds = [];
    };
    
    // 提交订单
    const submitBooking = async () => {
      if (!bookingFormRef.value) return;
      
      bookingFormRef.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true;            try {
              const buyTicketRequest = {
                trainId: train.value.id,
                seatType: selectedSeat.value.seatType,
                // 传入站点ID
                startStationId: startStationId.value || null,
                endStationId: endStationId.value || null,
                // 传入站点名称
                startStation: startStation.value || train.value.startStation,
                endStation: endStation.value || train.value.endStation,
                // 传入乘客ID列表
                passengerIds: bookingForm.passengerIds
              };
              
              console.log('TrainDetail - 创建订单请求数据:', buyTicketRequest);
              
              const response = await orderAPI.createOrder(buyTicketRequest);
              const result = dataHelper.extractApiData(response);
              
              console.log('TrainDetail - 创建订单响应:', response);
              console.log('TrainDetail - 提取的订单数据:', result);
              
              // 从后端响应中提取订单号
              let orderNo = result?.orderNo;
              
              console.log('TrainDetail - 从 result.orderNo 获取订单号:', orderNo);
              
              // 如果直接获取失败，尝试其他路径
              if (!orderNo && response?.data?.data) {
                orderNo = response.data.data.orderNo;
                console.log('TrainDetail - 从 response.data.data.orderNo 获取订单号:', orderNo);
              }
              
              console.log('TrainDetail - 最终获取的订单号:', orderNo);
              
              if (orderNo) {
                // 显示支付对话框
                paymentDialog.orderNo = orderNo;
                paymentDialog.orderData = result;
                paymentDialog.amount = calculateTotalPrice(bookingForm.passengerIds.length, selectedSeat.value.price);
                paymentDialog.visible = true;
                
                console.log('TrainDetail - 订单创建成功，订单号:', orderNo);
                ElMessage.success('订单创建成功，请完成支付');
              } else {
                console.error('TrainDetail - 订单号未找到，完整数据结构:', {
                  result: result,
                  response: response
                });
                ElMessage.error('订单创建成功，但获取订单号失败');
              }
            
          } catch (error) {
            console.error('提交订单失败:', error);
            ElMessage.error('提交订单失败');
          } finally {
            submitting.value = false;
          }
        }
      });
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
            if (!paymentDialog.orderNo) {
              throw new Error('订单号未找到，无法进行支付确认');
            }
            
            console.log('TrainDetail - 开始支付确认，订单号:', paymentDialog.orderNo, '支付方式:', paymentDialog.method);
            await orderAPI.confirmPayment(paymentDialog.orderNo, paymentDialog.method);
            
            // 延迟关闭对话框并显示成功消息
            setTimeout(() => {
              paymentDialog.processing = false;
              paymentDialog.visible = false;
              
              // 支付成功后显示消息并跳转到订单详情页面
              const ticketCount = bookingForm.passengerIds.length;
              ElMessageBox.alert(`支付成功！已为${ticketCount}位乘车人购买车票，订单号: ${paymentDialog.orderNo}`, '订单支付成功', {
                confirmButtonText: '查看订单',
                callback: () => {
                  router.push(`/order/${paymentDialog.orderNo}`);
                }
              });
            }, 1000);
          } catch (error) {
            paymentDialog.processing = false;
            console.error('TrainDetail - 支付确认失败:', error);
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
      
      // 跳转到订单详情页面
      ElMessageBox.confirm('是否放弃支付并跳转到订单详情页面?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        router.push(`/order/${paymentDialog.orderNo}`);
      }).catch(() => {});
    };
    
    // 返回上一页
    const goBack = () => {
      router.back();
    };
    
    // 跳转到乘车人管理
    const goToPassengerManage = () => {
      router.push({
        path: '/passenger',
        query: { redirect: route.fullPath }
      });
    };

    onMounted(() => {
      fetchTrainDetail();
      fetchPassengers();
    });
    
    // 格式化身份证号，保留前4位和后4位，中间用*代替
    const formatIdNumber = (idNumber) => {
      if (!idNumber || idNumber.length < 8) return idNumber;
      const prefix = idNumber.substring(0, 4);
      const suffix = idNumber.substring(idNumber.length - 4);
      return `${prefix}****${suffix}`;
    };
    
    // 计算总票价
    const calculateTotalPrice = (passengerCount, unitPrice) => {
      if (!passengerCount || !unitPrice) return 0;
      
      // 处理价格为对象的情况，如 {amount: 553.50}
      let price = unitPrice;
      if (typeof unitPrice === 'object' && unitPrice.amount !== undefined) {
        price = unitPrice.amount;
      }
      
      return (passengerCount * Number(price)).toFixed(2);
    };
    
    return {
      train,
      loading,
      submitting,
      passengers,
      selectedSeat,
      bookingForm,
      bookingFormRef,
      bookingRules,
      seatInfoArray,
      paymentDialog,
      selectSeat,
      cancelSeatSelection,
      submitBooking,
      goBack,
      goToPassengerManage,
      cancelPayment,
      simulatePayment,
      formatStation,
      formatPrice,
      formatDuration,
      formatIdNumber,
      calculateTotalPrice
    };
  }
}
</script>

<style scoped>
.train-detail-container {
  padding: 20px;
}

.train-info-card {
  margin-bottom: 20px;
}

.train-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.train-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.train-route {
  font-size: 18px;
  color: #666;
}

.train-time-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}

.departure, .arrival {
  text-align: center;
}

.time {
  font-size: 22px;
  font-weight: bold;
}

.station {
  font-size: 16px;
  color: #666;
  margin-top: 5px;
}

.duration {
  flex: 1;
  text-align: center;
  position: relative;
}

.arrow-line {
  height: 2px;
  background-color: #dcdfe6;
  position: relative;
}

.arrow-line:before,
.arrow-line:after {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  border-top: 2px solid #dcdfe6;
  border-right: 2px solid #dcdfe6;
}

.arrow-line:before {
  left: 0;
  top: -3px;
  transform: rotate(-135deg);
}

.arrow-line:after {
  right: 0;
  top: -3px;
  transform: rotate(45deg);
}

.duration-time {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.ticket-info {
  margin-top: 20px;
}

.booking-form {
  margin-top: 30px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 20px;
}

.action-row {
  margin-top: 20px;
  text-align: center;
}

/* 支付对话框样式 */
.payment-container {
  padding: 20px;
}

.payment-amount {
  text-align: center;
  margin-bottom: 20px;
}

.payment-amount h2 {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.payment-method {
  margin-top: 20px;
}

.payment-method h4 {
  text-align: center;
  margin-bottom: 15px;
}

.payment-options {
  text-align: center;
}

.payment-qrcode {
  margin-top: 20px;
}

.qrcode-container {
  display: inline-block;
  width: 150px;
  height: 150px;
  background-color: #f2f2f2;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.qrcode-placeholder {
  max-width: 90%;
  max-height: 90%;
}

.payment-method-selector {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.payment-progress {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.payment-progress p {
  text-align: center;
  margin-top: 10px;
  color: #409eff;
}
</style>
