/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="ticket-detail-container page-container">
    <h2 class="page-title">车票详情</h2>
    
    <div v-loading="loading">
      <el-card class="ticket-card" v-if="ticket">
        <div class="status-banner" :class="getStatusClass(ticket.status)">
          {{ getTicketStatusText(ticket.status) }}
        </div>
        
        <div class="ticket-content">
          <div class="train-info">
            <div class="train-number">{{ ticket.trainNumber }}</div>
            <div class="train-date">{{ extractDate(ticket.departureTime) }}</div>
          </div>
          
          <div class="station-info">
            <div class="departure-info">
              <div class="time">{{ extractTime(ticket.departureTime) }}</div>
              <div class="station">{{ formatStation(ticket.startStation) }}</div>
            </div>
            <div class="journey-line">
              <div class="line"></div>
            </div>
            <div class="arrival-info">
              <div class="time">{{ extractTime(ticket.arrivalTime) }}</div>
              <div class="station">{{ formatStation(ticket.endStation) }}</div>
            </div>
          </div>
          
          <div class="passenger-seat">
            <div class="section">
              <div class="label">乘车人</div>
              <div class="value">{{ ticket.passengerName }}</div>
            </div>
            <div class="section">
              <div class="label">证件号</div>
              <div class="value id-number">{{ formatIdNumber(ticket.passengerIdNumber) }}</div>
            </div>
            <div class="section">
              <div class="label">座位</div>
              <div class="value">{{ ticket.carNumber }}车{{ ticket.seatNumber }}号 ({{ ticket.seatType }})</div>
            </div>
          </div>
          
          <div class="qr-code">
            <div class="code-image">
              <el-image
                style="width: 150px; height: 150px"
                :src="getQrCodeUrl()"
                fit="contain"
              />
            </div>
            <div class="ticket-no">{{ ticket.ticketNo }}</div>
          </div>
          
          <div class="price-section">
            <div class="price-label">票价</div>
            <div class="price-value">{{ formatPrice(ticket.price) }}</div>
          </div>
          
          <div class="order-section">
            <div class="order-item">
              <span class="label">订单号：</span>
              <span class="value">{{ ticket.orderNo }}</span>
            </div>
            <div class="order-item">
              <span class="label">购票时间：</span>
              <span class="value">{{ formatTime(ticket.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="ticket-actions" v-if="ticket.status === 'VALID'">
          <el-button type="danger" @click="showRefundDialog">退票</el-button>
          <el-button type="primary" @click="showChangeDialog">改签</el-button>
        </div>
      </el-card>
      
      <el-empty v-else-if="!loading" description="车票不存在或已被删除"></el-empty>
    </div>
    
    <div class="action-row">
      <el-button @click="goBack">返回</el-button>
    </div>
    
    <!-- 退票对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="退票"
      width="500px"
    >
      <div v-loading="refundRuleLoading">
        <div v-if="refundRule" class="refund-info">
          <div class="refund-rule">
            <h3>退票规则</h3>
            <ul>
              <li v-for="(rule, index) in refundRule.rules" :key="index">{{ rule }}</li>
            </ul>
          </div>
          
          <div class="refund-amount">
            <div class="amount-item">
              <span class="label">原票价：</span>
              <span class="value">{{ formatPrice(refundRule.originalPrice) }}</span>
            </div>
            <div class="amount-item">
              <span class="label">手续费：</span>
              <span class="value">{{ formatPrice(refundRule.fee) }}</span>
            </div>
            <div class="amount-item highlight">
              <span class="label">可退金额：</span>
              <span class="value">{{ formatPrice(refundRule.refundableAmount) }}</span>
            </div>
          </div>
          
          <div class="confirm-tip">
            <el-alert
              title="请确认退款信息无误，退票后无法撤销"
              type="warning"
              :closable="false"
            />
          </div>
        </div>
        
        <div v-else-if="!refundRuleLoading" class="refund-error">
          <el-alert
            :title="refundRule?.reason || '无法计算退款金额'"
            type="error"
            :closable="false"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmRefund"
            :disabled="!refundRule?.canRefund || refundLoading"
            :loading="refundLoading"
          >
            确认退票
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 改签对话框 -->
    <el-dialog
      v-model="changeDialogVisible"
      title="车票改签"
      width="650px"
      class="change-ticket-dialog"
    >
      <div v-loading="changeRuleLoading">
        <div v-if="!changeRuleLoading && changeRule && !changeRule.canChange" class="change-error">
          <el-alert
            :title="changeRule?.reason || '无法改签'"
            type="error"
            :closable="false"
          />
        </div>
        
        <div v-else-if="!changeRuleLoading && changeRule">
          <!-- 改签规则 -->
          <div class="change-rule">
            <h4>改签规则</h4>
            <ul>
              <li v-for="(rule, index) in changeRule.rules" :key="index">{{ rule }}</li>
            </ul>
          </div>
          
          <!-- 改签表单 -->
          <el-form 
            :model="changeForm" 
            ref="changeFormRef"
            label-width="100px"
            :rules="changeRules"
          >
            <el-form-item label="原车次">
              <span>{{ ticket.trainNumber }}</span>
            </el-form-item>
            
            <el-form-item label="原乘车日期">
              <span>{{ extractDate(ticket.departureTime) }}</span>
            </el-form-item>
            
            <el-form-item label="原席别">
              <span>{{ ticket.seatType }}</span>
            </el-form-item>
            
            <el-form-item label="新车次" prop="trainId">
              <el-select 
                v-model="changeForm.trainId" 
                placeholder="请选择车次"
                filterable
                @change="handleTrainChange"
                :loading="changeDialog.trainsLoading"
              >
                <el-option
                  v-for="train in availableTrains"
                  :key="train.id"
                  :label="`${train.code} (${train.startStation}-${train.endStation})`"
                  :value="train.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="出发站" prop="startStation">
              <el-select 
                v-model="changeForm.startStation" 
                placeholder="请选择出发站"
                :disabled="!selectedTrain"
              >
                <el-option
                  v-for="station in stationOptions"
                  :key="station.id"
                  :label="station.name"
                  :value="station.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="到达站" prop="endStation">
              <el-select 
                v-model="changeForm.endStation" 
                placeholder="请选择到达站"
                :disabled="!selectedTrain"
              >
                <el-option
                  v-for="station in stationOptions"
                  :key="station.id"
                  :label="station.name"
                  :value="station.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="乘车日期" prop="travelDate">
              <el-date-picker
                v-model="changeForm.travelDate"
                type="date"
                placeholder="请选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disablePastDates"
              />
            </el-form-item>
            
            <el-form-item label="席别" prop="seatType">
              <el-select 
                v-model="changeForm.seatType" 
                placeholder="请选择席别"
                :disabled="!selectedTrain"
              >
                <el-option label="一等座" value="一等座" />
                <el-option label="二等座" value="二等座" />
                <el-option label="商务座" value="商务座" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="支付方式" prop="payMethod">
              <el-radio-group v-model="changeForm.payMethod">
                <el-radio :label="1">微信支付</el-radio>
                <el-radio :label="2">支付宝</el-radio>
                <el-radio :label="3">银行卡</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 价格计算 -->
            <el-form-item v-if="changeDialog.priceDifference !== null">
              <div class="price-calculation">
                <div class="price-item">
                  <span>原票价：</span>
                  <span>{{ formatPrice(changeDialog.originalPrice) }}</span>
                </div>
                <div class="price-item">
                  <span>新票价：</span>
                  <span>{{ formatPrice(changeDialog.newPrice) }}</span>
                </div>
                <div class="price-item" v-if="changeDialog.priceDifference > 0">
                  <span>需补差价：</span>
                  <span class="highlight">{{ formatPrice(changeDialog.priceDifference) }}</span>
                </div>
                <div class="price-item" v-else-if="changeDialog.priceDifference < 0">
                  <span>可退差价：</span>
                  <span class="highlight">{{ formatPrice(Math.abs(changeDialog.priceDifference)) }}</span>
                </div>
                <div class="price-item" v-if="changeDialog.changeFee > 0">
                  <span>改签手续费：</span>
                  <span>{{ formatPrice(changeDialog.changeFee) }}</span>
                </div>
                <div class="price-item total" v-if="changeDialog.totalPayment > 0">
                  <span>应付金额：</span>
                  <span>{{ formatPrice(changeDialog.totalPayment) }}</span>
                </div>
                <div class="price-item total" v-else-if="changeDialog.totalPayment < 0">
                  <span>应退金额：</span>
                  <span>{{ formatPrice(Math.abs(changeDialog.totalPayment)) }}</span>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changeDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmChange"
            :disabled="!changeRule?.canChange || changeLoading"
            :loading="changeLoading"
          >
            确认改签
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialog.visible"
      title="支付"
      width="400px"
    >
      <div class="payment-container">
        <div class="payment-amount">
          <p>支付金额</p>
          <h2>¥{{ paymentDialog.amount }}</h2>
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
              v-else-if="paymentDialog.method === 3" 
              class="bank-form"
            >
              <el-form>
                <el-form-item label="银行卡号">
                  <el-input placeholder="请输入银行卡号" />
                </el-form-item>
                <el-form-item label="持卡人">
                  <el-input placeholder="请输入持卡人姓名" />
                </el-form-item>
                <el-form-item label="有效期">
                  <el-input placeholder="MM/YY" />
                </el-form-item>
                <el-form-item label="CVV">
                  <el-input placeholder="请输入安全码" />
                </el-form-item>
              </el-form>
              
              <el-button type="primary" @click="simulatePayment">确认支付</el-button>
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
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// import { ElMessage, ElMessageBox } from 'element-plus';
import { ElMessage } from 'element-plus';
import { ticketAPI, trainAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'TicketDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const ticketNo = route.params.ticketNo;
    
    const loading = ref(false);
    const ticket = ref(null);
    
    // 退票相关响应式变量
    const refundDialogVisible = ref(false);
    const refundRuleLoading = ref(false);
    const refundRule = ref(null);
    const refundLoading = ref(false);
    
    // 改签相关响应式变量
    const changeDialogVisible = ref(false);
    const changeRuleLoading = ref(false);
    const changeRule = ref(null);
    const changeLoading = ref(false);
    const changeFormRef = ref(null);
    const changeForm = reactive({
      trainId: null,
      startStation: null,
      endStation: null,
      travelDate: null,
      seatType: null,
      payMethod: 1 // 默认微信支付
    });
    const changeRules = {
      trainId: [{ required: true, message: '请选择车次', trigger: 'change' }],
      startStation: [{ required: true, message: '请选择出发站', trigger: 'change' }],
      endStation: [{ required: true, message: '请选择到达站', trigger: 'change' }],
      travelDate: [{ required: true, message: '请选择乘车日期', trigger: 'change' }],
      seatType: [{ required: true, message: '请选择席别', trigger: 'change' }],
      payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
    };
    const selectedTrain = ref(null);
    const availableTrains = ref([]);
    const stationOptions = ref([]);
    const changeDialog = reactive({
      trainsLoading: false,
      originalPrice: 0,
      newPrice: 0,
      priceDifference: null,
      changeFee: 0,
      totalPayment: 0
    });
    
    // 支付对话框
    const paymentDialog = reactive({
      visible: false,
      amount: 0,
      method: 1, // 1-微信，2-支付宝，3-银行卡
      processing: false,
      progress: 0,
      progressText: '正在处理支付...',
      ticketChangeRequestData: null // 存储改签请求数据
    });
    
    // 获取车票详情
    const fetchTicketDetail = async () => {
      loading.value = true;
      try {
        const response = await ticketAPI.getTicketDetail(ticketNo);
        console.log('获取到的车票详情响应:', response);
        
        const extractedData = dataHelper.extractApiData(response);
        if (extractedData) {
          // 使用适配器处理TicketDetailDTO
          ticket.value = dataHelper.adaptTicketData(extractedData);
          console.log('处理后的车票详情数据:', ticket.value);
        } else {
          ticket.value = null;
          ElMessage.warning('未找到车票信息');
        }
      } catch (error) {
        console.error('获取车票详情失败:', error);
        ElMessage.error('获取车票详情失败');
        ticket.value = null;
      } finally {
        loading.value = false;
      }
    };
    
    // 显示退票对话框
    const showRefundDialog = async () => {
      refundDialogVisible.value = true;
      refundRuleLoading.value = true;
      refundRule.value = null;
      
      try {
        const response = await ticketAPI.getRefundRule(ticketNo);
        console.log('退票规则响应:', response);
        
        const extractedData = dataHelper.extractApiData(response);
        if (extractedData) {
          refundRule.value = extractedData;
        } else {
          refundRule.value = {
            canRefund: false,
            reason: '无法获取退票规则'
          };
        }
      } catch (error) {
        console.error('获取退票规则失败:', error);
        refundRule.value = {
          canRefund: false,
          reason: '获取退票规则失败'
        };
      } finally {
        refundRuleLoading.value = false;
      }
    };
    
    // 确认退票
    const confirmRefund = async () => {
      if (!refundRule.value?.canRefund) {
        ElMessage.warning('当前车票不可退');
        return;
      }
      
      refundLoading.value = true;
      try {
        const response = await ticketAPI.cancelTicket(ticketNo);
        console.log('退票响应:', response);
        
        ElMessage.success('退票成功');
        refundDialogVisible.value = false;
        fetchTicketDetail(); // 刷新车票详情
      } catch (error) {
        console.error('退票失败:', error);
        ElMessage.error(error.message || '退票失败');
      } finally {
        refundLoading.value = false;
      }
    };
    
    // 显示改签对话框
    const showChangeDialog = async () => {
      changeDialogVisible.value = true;
      changeRuleLoading.value = true;
      changeRule.value = null;
      resetChangeForm();
      
      try {
        const response = await ticketAPI.checkChangeTicket(ticketNo);
        console.log('改签规则响应:', response);
        
        const extractedData = dataHelper.extractApiData(response);
        if (extractedData) {
          changeRule.value = extractedData;
          
          if (changeRule.value.canChange) {
            // 设置默认表单值
            await loadTrains();
            
            if (ticket.value) {
              changeDialog.originalPrice = ticket.value.price || 0;
            }
          }
        } else {
          changeRule.value = {
            canChange: false,
            reason: '无法获取改签规则'
          };
        }
      } catch (error) {
        console.error('获取改签规则失败:', error);
        changeRule.value = {
          canChange: false,
          reason: '获取改签规则失败'
        };
      } finally {
        changeRuleLoading.value = false;
      }
    };
    
    // 重置改签表单
    const resetChangeForm = () => {
      if (changeFormRef.value) {
        changeFormRef.value.resetFields();
      }
      
      changeForm.trainId = null;
      changeForm.startStation = null;
      changeForm.endStation = null;
      changeForm.travelDate = new Date().toISOString().split('T')[0]; // 默认今天
      changeForm.seatType = ticket.value?.seatType || null;
      changeForm.payMethod = 1;
      
      selectedTrain.value = null;
      changeDialog.originalPrice = 0;
      changeDialog.newPrice = 0;
      changeDialog.priceDifference = null;
      changeDialog.changeFee = 0;
      changeDialog.totalPayment = 0;
    };
    
    // 加载可选列车
    const loadTrains = async () => {
      changeDialog.trainsLoading = true;
      try {
        const response = await trainAPI.listAllTrains();
        const extractedData = dataHelper.extractApiData(response);
        
        if (Array.isArray(extractedData)) {
          availableTrains.value = extractedData.map(train => ({
            id: train.id,
            code: train.code || train.trainNumber,
            startStation: train.startStation,
            endStation: train.endStation
          }));
        } else {
          availableTrains.value = [];
        }
      } catch (error) {
        console.error('加载列车数据失败:', error);
        ElMessage.error('加载列车数据失败');
        availableTrains.value = [];
      } finally {
        changeDialog.trainsLoading = false;
      }
    };
    
    // 处理选择车次
    const handleTrainChange = async () => {
      if (!changeForm.trainId) {
        selectedTrain.value = null;
        stationOptions.value = [];
        return;
      }
      
      try {
        const response = await trainAPI.getTrainDetail(changeForm.trainId);
        const extractedData = dataHelper.extractApiData(response);
        
        if (extractedData) {
          selectedTrain.value = extractedData;
          
          // 更新车站选项
          if (Array.isArray(extractedData.stations)) {
            stationOptions.value = extractedData.stations;
            
            // 默认选择始发站和终点站
            if (stationOptions.value.length > 0) {
              changeForm.startStation = stationOptions.value[0].id;
              changeForm.endStation = stationOptions.value[stationOptions.value.length - 1].id;
            }
            
            // 计算票价
            calculatePrice();
          } else {
            stationOptions.value = [];
          }
        } else {
          selectedTrain.value = null;
          stationOptions.value = [];
        }
      } catch (error) {
        console.error('获取列车详情失败:', error);
        ElMessage.error('获取列车详情失败');
        selectedTrain.value = null;
        stationOptions.value = [];
      }
    };
    
    // 计算票价差额
    const calculatePrice = () => {
      // 模拟计算票价，实际项目中应该调用后端接口计算
      // 这里仅作为示例
      const originalPrice = ticket.value?.price || 0;
      
      // 简单模拟新票价计算逻辑，实际应该根据车次、席别等信息从后端获取
      let newPrice = 0;
      if (selectedTrain.value && changeForm.seatType) {
        // 简单计算逻辑，仅作为示例
        const basePrice = Math.round(Math.random() * 500) + 100;
        
        if (changeForm.seatType === '商务座') {
          newPrice = basePrice * 1.5;
        } else if (changeForm.seatType === '一等座') {
          newPrice = basePrice * 1.2;
        } else {
          newPrice = basePrice;
        }
      }
      
      // 计算价格差额
      const priceDifference = newPrice - originalPrice;
      
      // 计算改签手续费，通常为原票价的某个比例
      const changeFee = Math.round(originalPrice * 0.05); // 5%手续费，仅作为示例
      
      // 计算总支付金额
      let totalPayment = priceDifference + changeFee;
      if (priceDifference < 0) {
        // 如果新票价低于原票价，则只收取手续费
        totalPayment = Math.max(0, changeFee + priceDifference);
      }
      
      // 更新改签对话框中的价格信息
      changeDialog.originalPrice = originalPrice;
      changeDialog.newPrice = newPrice;
      changeDialog.priceDifference = priceDifference;
      changeDialog.changeFee = changeFee;
      changeDialog.totalPayment = totalPayment;
    };
    
    // 确认改签
    const confirmChange = async () => {
      if (!changeForm.trainId || !changeForm.startStation || !changeForm.endStation || 
          !changeForm.travelDate || !changeForm.seatType || !changeForm.payMethod) {
        ElMessage.warning('请完成所有必填项');
        return;
      }
      
      // 表单验证
      try {
        if (changeFormRef.value) {
          await changeFormRef.value.validate();
        }
        
        // 如果需要支付，显示支付对话框
        if (changeDialog.totalPayment > 0) {
          // 构建改签请求数据
          const changeTicketRequestData = {
            ticketNo: ticketNo,
            trainId: changeForm.trainId,
            departDate: changeForm.travelDate,
            startStationId: changeForm.startStation,
            endStationId: changeForm.endStation,
            seatType: changeForm.seatType,
            paymentMethod: changeForm.payMethod
          };
          
          // 保存改签请求数据并显示支付对话框
          paymentDialog.ticketChangeRequestData = changeTicketRequestData;
          paymentDialog.amount = changeDialog.totalPayment;
          paymentDialog.method = changeForm.payMethod;
          paymentDialog.visible = true;
        } else {
          // 如果不需要额外支付，直接提交改签请求
          submitChangeTicket();
        }
      } catch (error) {
        console.error('表单验证失败:', error);
      }
    };
    
    // 提交改签请求
    const submitChangeTicket = async () => {
      changeLoading.value = true;
      
      try {
        const requestData = paymentDialog.ticketChangeRequestData || {
          ticketNo: ticketNo,
          trainId: changeForm.trainId,
          departDate: changeForm.travelDate,
          startStationId: changeForm.startStation,
          endStationId: changeForm.endStation,
          seatType: changeForm.seatType,
          paymentMethod: changeForm.payMethod
        };
        
        const response = await ticketAPI.changeTicket(requestData);
        console.log('改签响应:', response);
        
        ElMessage.success('改签成功');
        changeDialogVisible.value = false;
        paymentDialog.visible = false;
        fetchTicketDetail(); // 刷新车票详情
      } catch (error) {
        console.error('改签失败:', error);
        ElMessage.error(error.message || '改签失败');
      } finally {
        changeLoading.value = false;
        paymentDialog.processing = false;
      }
    };
    
    // 模拟支付流程
    const simulatePayment = async () => {
      paymentDialog.processing = true;
      paymentDialog.progress = 0;
      paymentDialog.progressText = '正在处理支付...';
      
      const interval = setInterval(async () => {
        if (paymentDialog.progress >= 100) {
          clearInterval(interval);
          paymentDialog.progressText = '支付成功';
          
          try {
            // 改签场景：调用后端接口确认支付并处理改签
            if (paymentDialog.ticketChangeRequestData) {
              // 模拟改签的支付确认，这里可以使用票据相关的订单号
              // 由于改签流程比较复杂，这里直接调用改签API
              await submitChangeTicket();
            } else {
              ElMessage.warning('支付流程配置错误');
              paymentDialog.processing = false;
              paymentDialog.visible = false;
            }
          } catch (error) {
            paymentDialog.processing = false;
            console.error('支付确认失败:', error);
            ElMessage.error('支付确认失败，请联系客服');
          }
        } else {
          paymentDialog.progress += 10;
          
          if (paymentDialog.progress < 50) {
            paymentDialog.progressText = '正在处理支付...';
          } else if (paymentDialog.progress < 80) {
            paymentDialog.progressText = '支付验证中...';
          } else {
            paymentDialog.progressText = '支付完成，处理改签...';
          }
        }
      }, 300);
    };
    
    // 禁用今天以前的日期
    const disablePastDates = (date) => {
      return date < new Date(new Date().setHours(0, 0, 0, 0));
    };
    
    // 返回上一页
    const goBack = () => {
      router.push('/ticket');
    };
    
    // 获取票状态文本
    const getTicketStatusText = (status) => {
      const statusMap = {
        'VALID': '有效',
        'USED': '已使用',
        'CANCELED': '已退票',
        'EXPIRED': '已过期',
        'CHANGED': '已改签'
      };
      return statusMap[status] || status;
    };
    
    // 获取状态对应的CSS类名
    const getStatusClass = (status) => {
      const classMap = {
        'VALID': 'valid',
        'USED': 'used',
        'CANCELED': 'canceled',
        'EXPIRED': 'expired',
        'CHANGED': 'changed'
      };
      return classMap[status] || '';
    };
    
    // 从日期时间字符串中提取日期
    const extractDate = (dateTimeStr) => {
      if (!dateTimeStr) return '';
      const parts = dateTimeStr.split(' ');
      return parts[0] || '';
    };
    
    // 从日期时间字符串中提取时间
    const extractTime = (dateTimeStr) => {
      if (!dateTimeStr) return '';
      const parts = dateTimeStr.split(' ');
      return parts[1] || '';
    };
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN');
    };
    
    // 格式化身份证号，只显示前4位和后4位
    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };
    
    // 获取二维码URL（模拟，实际应从后端获取）
    const getQrCodeUrl = () => {
      // 使用占位图像，实际项目中应当从后端获取真实的二维码
      return `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(ticketNo)}`;
    };
    
    onMounted(() => {
      fetchTicketDetail();
    });
    
    return {
      loading,
      ticket,
      goBack,
      getTicketStatusText,
      getStatusClass,
      extractDate,
      extractTime,
      formatTime,
      formatIdNumber,
      getQrCodeUrl,
      formatStation,
      formatPrice,
      
      // 退票相关
      refundDialogVisible,
      refundRuleLoading,
      refundRule,
      refundLoading,
      showRefundDialog,
      confirmRefund,
      
      // 改签相关
      changeDialogVisible,
      changeRuleLoading,
      changeRule,
      changeLoading,
      changeForm,
      changeFormRef,
      changeRules,
      selectedTrain,
      availableTrains,
      stationOptions,
      changeDialog,
      showChangeDialog,
      handleTrainChange,
      confirmChange,
      disablePastDates,
      
      // 支付相关
      paymentDialog,
      simulatePayment
    };
  }
}
</script>

<style scoped>
.ticket-detail-container {
  padding: 20px;
}

.ticket-card {
  position: relative;
  margin-bottom: 20px;
  overflow: hidden;
}

.status-banner {
  position: absolute;
  top: 20px;
  right: -30px;
  transform: rotate(45deg);
  background-color: #409EFF;
  color: #fff;
  padding: 5px 40px;
  font-weight: bold;
  font-size: 14px;
  z-index: 1;
}

.status-banner.valid {
  background-color: #67C23A;
}

.status-banner.used {
  background-color: #909399;
}

.status-banner.canceled {
  background-color: #F56C6C;
}

.status-banner.expired {
  background-color: #E6A23C;
}

.ticket-content {
  padding: 20px;
}

.train-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.train-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.train-date {
  font-size: 18px;
  color: #606266;
}

.station-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
}

.departure-info, .arrival-info {
  flex: 0 0 40%;
}

.journey-line {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 15px;
}

.line {
  height: 2px;
  background-color: #dcdfe6;
  width: 100%;
  position: relative;
}

.line:after {
  content: '';
  position: absolute;
  right: 0;
  top: -3px;
  width: 8px;
  height: 8px;
  border-top: 2px solid #dcdfe6;
  border-right: 2px solid #dcdfe6;
  transform: rotate(45deg);
}

.time {
  font-size: 24px;
  font-weight: bold;
}

.station {
  margin-top: 5px;
  font-size: 16px;
}

.passenger-seat {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
}

.section {
  margin-bottom: 10px;
}

.section:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
  margin-bottom: 5px;
}

.value {
  font-weight: bold;
}

.id-number {
  font-family: monospace;
}

.qr-code {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 30px 0;
}

.ticket-no {
  margin-top: 10px;
  font-family: monospace;
  color: #606266;
}

.price-section {
  text-align: center;
  margin-bottom: 20px;
}

.price-label {
  color: #909399;
  margin-bottom: 5px;
}

.price-value {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.order-section {
  border-top: 1px dashed #dcdfe6;
  padding-top: 15px;
}

.order-item {
  margin-bottom: 5px;
}

.ticket-actions {
  margin-top: 20px;
  text-align: center;
}

.action-row {
  margin-top: 20px;
  text-align: center;
}

/* 退票对话框样式 */
.refund-info {
  padding: 10px 0;
}

.refund-rule {
  margin-bottom: 20px;
}

.refund-rule h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.refund-rule ul {
  padding-left: 20px;
  color: #606266;
}

.refund-rule li {
  margin-bottom: 5px;
  line-height: 1.5;
}

.refund-amount {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.amount-item:last-child {
  margin-bottom: 0;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
}

.amount-item.highlight .value {
  color: #F56C6C;
  font-weight: bold;
  font-size: 16px;
}

.confirm-tip {
  margin-top: 20px;
}

.refund-error {
  padding: 10px 0;
}

/* 改签对话框样式 */
.change-ticket-dialog {
  min-height: 300px;
}

.change-error {
  margin-bottom: 20px;
}

.change-rule {
  margin-bottom: 20px;
}

.change-rule h4 {
  font-size: 14px;
  margin-bottom: 10px;
  color: #303133;
}

.change-rule ul {
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
}

.change-rule li {
  margin-bottom: 4px;
  line-height: 1.5;
}

.price-calculation {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.price-item:last-child {
  margin-bottom: 0;
}

.price-item.total {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
  font-weight: bold;
}

.price-item .highlight {
  color: #F56C6C;
}

/* 支付对话框样式 */
.payment-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.payment-amount {
  text-align: center;
  margin-bottom: 20px;
}

.payment-amount p {
  color: #909399;
  margin-bottom: 5px;
}

.payment-amount h2 {
  color: #F56C6C;
  font-size: 28px;
  margin: 0;
}

.payment-method {
  width: 100%;
}

.payment-method h4 {
  margin-bottom: 15px;
  color: #303133;
}

.payment-options {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.payment-qrcode {
  text-align: center;
}

.qrcode-container {
  border: 1px solid #dcdfe6;
  padding: 10px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 10px;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  object-fit: contain;
}

.payment-qrcode p {
  color: #606266;
  margin-top: 10px;
}

.payment-qrcode.wechat {
  color: #09BB07;
}

.payment-qrcode.alipay {
  color: #1677FF;
}

.bank-form {
  width: 100%;
}

.payment-progress {
  margin-top: 20px;
  text-align: center;
}

.payment-progress p {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
}

.ticket-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.status-banner.changed {
  background-color: #409EFF;
}
</style>
