/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="order-create-container page-container">
    <h2 class="page-title">提交订单</h2>
    
    <el-card class="order-form-card" v-loading="loading">
      <template v-if="trainInfo">
        <div class="train-info">
          <div class="train-header">
            <div class="train-number">{{ trainInfo.trainNumber }}</div>
            <div class="train-route">{{ formatStation(trainInfo.startStation) }} → {{ formatStation(trainInfo.endStation) }}</div>
          </div>
          
          <div class="train-time">
            <div class="departure">
              <div class="time">{{ trainInfo.departureTime }}</div>
              <div class="station">{{ formatStation(trainInfo.startStation) }}</div>
            </div>
            <div class="journey">
              <div class="line"></div>
              <div class="duration">{{ formatDuration(trainInfo.duration) }}</div>
            </div>
            <div class="arrival">
              <div class="time">{{ trainInfo.arrivalTime }}</div>
              <div class="station">{{ formatStation(trainInfo.endStation) }}</div>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="passenger-selection">
          <h3>选择乘车人</h3>
          <div v-if="passengers.length > 0">
            <el-checkbox-group v-model="selectedPassengers">
              <el-checkbox 
                v-for="passenger in passengers" 
                :key="passenger.id" 
                :label="passenger.id"
                border
              >
                {{ passenger.realName }} ({{ formatIdNumber(passenger.idNumber) }})
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <el-empty v-else description="您还没有添加乘车人">
            <el-button type="primary" @click="goToAddPassenger">添加乘车人</el-button>
          </el-empty>
        </div>
        
        <div class="seat-selection" v-if="selectedPassengers.length > 0">
          <h3>选择座位类型</h3>
          <el-radio-group v-model="selectedSeatType">
            <el-radio 
              v-for="seat in trainInfo.seatInfo" 
              :key="seat.seatType" 
              :label="seat.seatType"
              :disabled="seat.remaining < selectedPassengers.length"
            >
              {{ seat.seatType }} - {{ formatPrice(seat.price) }} (剩余: {{ seat.remaining }}张)
            </el-radio>
          </el-radio-group>
        </div>
        
        <div class="order-summary" v-if="selectedPassengers.length > 0 && selectedSeatType">
          <el-divider />
          <h3>订单摘要</h3>
          <div class="summary-info">
            <div class="summary-item">
              <span class="label">乘车人:</span>
              <span class="value">{{ getSelectedPassengerNames() }}</span>
            </div>
            <div class="summary-item">
              <span class="label">座位类型:</span>
              <span class="value">{{ selectedSeatType }}</span>
            </div>
            <div class="summary-item">
              <span class="label">车票单价:</span>
              <span class="value">{{ formatPrice(getSelectedSeatPrice()) }}</span>
            </div>
            <div class="summary-item total">
              <span class="label">总金额:</span>
              <span class="value price">{{ formatPrice(getSelectedSeatPrice() * selectedPassengers.length) }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button 
            type="primary" 
            @click="createOrder" 
            :disabled="!canSubmit"
            :loading="submitting"
          >提交订单</el-button>
        </div>
      </template>
      
      <el-empty v-else-if="!loading" description="未找到列车信息"></el-empty>
    </el-card>
    
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
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { trainAPI, passengerAPI, orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

// 从站点名称中提取城市名
function extractCityFromStation(stationName) {
  if (!stationName) return '';
  
  // 移除站、东站、南站、西站、北站等后缀
  const suffixes = ['站', '东站', '南站', '西站', '北站'];
  for (const suffix of suffixes) {
    if (stationName.endsWith(suffix)) {
      return stationName.substring(0, stationName.length - suffix.length);
    }
  }
  
  return stationName;
}

export default {
  name: 'OrderCreate',
  setup() {
    const router = useRouter();
    const route = useRoute();
    
    const trainId = route.query.trainId;
    const loading = ref(false);
    const submitting = ref(false);
    const trainInfo = ref(null);
    const passengers = ref([]);
    const selectedPassengers = ref([]);
    const selectedSeatType = ref('');
    
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
    
    // 是否可以提交订单
    const canSubmit = computed(() => {
      return selectedPassengers.value.length > 0 && selectedSeatType.value;
    });
    
    // 获取列车信息
    const getTrainInfo = async () => {
      if (!trainId) {
        ElMessage.error('缺少列车信息，请重新选择');
        router.push('/train/search');
        return;
      }
      
      loading.value = true;
      try {
        const data = await trainAPI.getTrainDetail(trainId);
        trainInfo.value = data;
      } catch (error) {
        console.error('获取列车信息失败:', error);
        ElMessage.error('获取列车信息失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 获取乘车人列表
    const getPassengers = async () => {
      try {
        const response = await passengerAPI.listPassengers();
        const extractedData = dataHelper.extractApiData(response);
        const passengerArray = dataHelper.ensureArray(extractedData);
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p)) || [];
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
        ElMessage.error('获取乘车人列表失败');
      }
    };
    
    // 格式化身份证号，只显示前4位和后4位
    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };
    
    // 获取选中的乘车人姓名列表
    const getSelectedPassengerNames = () => {
      if (!selectedPassengers.value.length) return '';
      
      return selectedPassengers.value.map(id => {
        const passenger = passengers.value.find(p => p.id === id);
        return passenger ? passenger.realName : '';
      }).filter(Boolean).join(', ');
    };
    
    // 获取选中座位类型的价格
    const getSelectedSeatPrice = () => {
      if (!selectedSeatType.value || !trainInfo.value || !trainInfo.value.seatInfo) return 0;
      
      const seatInfo = trainInfo.value.seatInfo.find(s => s.seatType === selectedSeatType.value);
      return seatInfo ? seatInfo.price : 0;
    };
    
    // 前往添加乘车人页面
    const goToAddPassenger = () => {
      router.push({
        path: '/passenger/edit',
        query: { redirect: route.fullPath }
      });
    };
    
    // 创建订单
    const createOrder = async () => {
      if (!canSubmit.value) return;
      
      submitting.value = true;
      
      try {
        // 为每个乘客创建订单
        // 改为单一订单多乘客的模式
        const orderData = {
          trainId: trainInfo.value.id,
          startStation: trainInfo.value.startStation,
          endStation: trainInfo.value.endStation,
          startStationId: trainInfo.value.startStationId || null,
          endStationId: trainInfo.value.endStationId || null,
          // 提取城市信息
          startCity: extractCityFromStation(trainInfo.value.startStation),
          endCity: extractCityFromStation(trainInfo.value.endStation),
          passengerIds: selectedPassengers.value,
          seatType: selectedSeatType.value
        };
        
        console.log('创建订单数据:', orderData);
        
        const response = await orderAPI.createOrder(orderData);
        const result = dataHelper.extractApiData(response);
        
        console.log('创建订单响应:', response);
        console.log('提取的订单数据:', result);
        console.log('响应原始数据:', JSON.stringify(response, null, 2));
        console.log('提取数据详情:', JSON.stringify(result, null, 2));
      
        if (result) {
          // 从后端响应中提取订单号
          // 根据后端返回的数据结构：Result.success("购票成功", (Order)orderDTO)
          // 数据应该在 result 对象中直接包含 orderNo 字段
          let orderNo = result.orderNo;
          
          console.log('从 result.orderNo 获取订单号:', orderNo);
          
          // 如果直接获取失败，尝试其他路径
          if (!orderNo && response.data && response.data.data) {
            orderNo = response.data.data.orderNo;
            console.log('从 response.data.data.orderNo 获取订单号:', orderNo);
          }
          
          console.log('最终获取的订单号:', orderNo);
          
          if (orderNo) {
            // 显示支付对话框
            paymentDialog.orderNo = orderNo;
            paymentDialog.orderData = result;
            paymentDialog.amount = getSelectedSeatPrice() * selectedPassengers.value.length;
            paymentDialog.visible = true;
            
            console.log('订单创建成功，订单号:', orderNo);
          } else {
            console.error('订单号未找到，完整数据结构:', {
              result: result,
              response: response
            });
            ElMessage.error('订单创建成功，但获取订单号失败');
          }
        } else {
          ElMessage.error('创建订单失败，响应数据为空');
        }
      } catch (error) {
        console.error('创建订单失败:', error);
        ElMessage.error('创建订单失败');
      } finally {
        submitting.value = false;
      }
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
            
            console.log('开始支付确认，订单号:', paymentDialog.orderNo, '支付方式:', paymentDialog.method);
            await orderAPI.confirmPayment(paymentDialog.orderNo, paymentDialog.method);
            
            // 延迟关闭对话框并显示成功消息
            setTimeout(() => {
              paymentDialog.processing = false;
              paymentDialog.visible = false;
              
              // 支付成功后显示消息并跳转到订单详情页面
              const ticketCount = selectedPassengers.value.length;
              ElMessageBox.alert(`支付成功！已为${ticketCount}位乘车人购买车票，订单号: ${paymentDialog.orderNo}`, '订单支付成功', {
                confirmButtonText: '查看订单',
                callback: () => {
                  router.push(`/order/${paymentDialog.orderNo}`);
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
      
      // 跳转到订单详情页面
      ElMessageBox.confirm('是否放弃支付并跳转到订单详情页面?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        router.push(`/order/${paymentDialog.orderNo}`);
      }).catch(() => {});
    };
    
    // 删除重复代码
    
    // 返回上一页
    const goBack = () => {
      router.back();
    };
    
    onMounted(() => {
      getTrainInfo();
      getPassengers();
    });
    
    return {
      loading,
      submitting,
      trainInfo,
      passengers,
      selectedPassengers,
      selectedSeatType,
      canSubmit,
      formatIdNumber,
      getSelectedPassengerNames,
      getSelectedSeatPrice,
      goToAddPassenger,
      formatStation,
      formatPrice,
      formatDuration,
      createOrder,
      simulatePayment,
      cancelPayment,
      paymentDialog,
      goBack
    };
  }
}
</script>

<style scoped>
.order-create-container {
  padding: 20px;
}

.order-form-card {
  margin-bottom: 20px;
}

.train-info {
  margin-bottom: 20px;
}

.train-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.train-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.train-route {
  font-size: 18px;
}

.train-time {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.departure, .arrival {
  text-align: center;
}

.time {
  font-size: 20px;
  font-weight: bold;
}

.station {
  margin-top: 5px;
  color: #606266;
}

.journey {
  flex: 1;
  position: relative;
  margin: 0 20px;
  height: 30px;
}

.line {
  height: 2px;
  background-color: #DCDFE6;
  position: absolute;
  width: 100%;
  top: 50%;
  transform: translateY(-50%);
}

.duration {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 0 10px;
  color: #909399;
}

.passenger-selection, .seat-selection, .order-summary {
  margin-top: 20px;
}

.summary-info {
  background-color: #f8f8f8;
  border-radius: 4px;
  padding: 15px;
}

.summary-item {
  margin-bottom: 10px;
  display: flex;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #606266;
  width: 100px;
}

.value {
  font-weight: bold;
}

.total {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #DCDFE6;
}

.price {
  color: #F56C6C;
  font-size: 18px;
}

.order-actions {
  margin-top: 30px;
  text-align: center;
}

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
