<template>
  <div class="order-list-container page-container">
    <h2 class="page-title">订单管理</h2>
    
    <el-card v-loading="loading">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部订单" name="all"></el-tab-pane>
        <el-tab-pane label="待支付" name="unpaid"></el-tab-pane>
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
        <el-tab-pane label="已取消" name="canceled"></el-tab-pane>
      </el-tabs>
      
      <div class="order-list" v-if="orders.length > 0">
        <div class="order-item" v-for="order in orders" :key="order.orderNo">
          <div class="order-header">
            <div class="order-no">订单号：{{ order.orderNo }}</div>
            <div class="order-status">{{ getOrderStatusText(order.status) }}</div>
          </div>
          
          <div class="order-content">
            <div class="train-info">
              <div class="train-route">
                <div class="train-number">{{ order.trainNumber }}</div>
                <div class="train-stations">{{ formatStation(order.startStation) }} → {{ formatStation(order.endStation) }}</div>
              </div>
              <div class="train-time">
                <div>出发：{{ order.departureTime }}</div>
                <div>到达：{{ order.arrivalTime }}</div>
              </div>
            </div>
            
            <div class="ticket-count">
              车票数量：{{ order.ticketCount }}
            </div>
            
            <div class="order-amount">
              总金额：<span class="price">{{ formatPrice(order.totalAmount) }}</span>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-time">下单时间：{{ formatTime(order.createTime) }}</div>
            <div class="order-actions">
              <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="cancelOrder(order)"
                v-if="order.status === 'UNPAID'"
              >取消订单</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty description="暂无订单" v-else-if="!loading"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'OrderList',
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const orders = ref([]);
    const activeTab = ref('all');
    
    // 获取订单列表
    const fetchOrders = async () => {
      loading.value = true;
      try {
        const response = await orderAPI.listUserOrders();
        console.log('获取到的订单列表响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        const orderData = dataHelper.ensureArray(extractedData);
        orders.value = filterOrders(orderData);
      } catch (error) {
        console.error('获取订单列表失败:', error);
        ElMessage.error('获取订单列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 根据当前选中的标签过滤订单
    const filterOrders = (allOrders) => {
      if (activeTab.value === 'all') {
        return allOrders;
      }
      
      const statusMap = {
        unpaid: 'UNPAID',
        completed: 'COMPLETED',
        canceled: 'CANCELED'
      };
      
      return allOrders.filter(order => order.status === statusMap[activeTab.value]);
    };
    
    // 处理标签切换
    const handleTabClick = () => {
      fetchOrders();
    };
    
    // 查看订单详情
    const viewOrderDetail = (order) => {
      router.push(`/order/${order.orderNo}`);
    };
    
    // 取消订单
    const cancelOrder = (order) => {
      ElMessageBox.confirm('确定要取消该订单吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await orderAPI.cancelOrder(order.orderNo);
          ElMessage.success('订单取消成功');
          fetchOrders(); // 刷新订单列表
        } catch (error) {
          console.error('取消订单失败:', error);
          ElMessage.error('取消订单失败');
        }
      }).catch(() => {});
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
    
    onMounted(() => {
      fetchOrders();
    });
    
    return {
      loading,
      orders,
      activeTab,
      handleTabClick,
      viewOrderDetail,
      cancelOrder,
      getOrderStatusText,
      formatTime,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.order-list-container {
  padding: 20px;
}

.order-list {
  margin-top: 20px;
}

.order-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 15px;
  overflow: hidden;
}

.order-header {
  background-color: #f5f7fa;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #dcdfe6;
}

.order-no {
  font-weight: bold;
}

.order-status {
  color: #409EFF;
  font-weight: bold;
}

.order-content {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.train-info {
  flex: 1;
}

.train-route {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  margin-right: 15px;
  color: #409EFF;
}

.train-time {
  color: #606266;
  font-size: 14px;
}

.ticket-count {
  margin: 0 20px;
}

.order-amount {
  text-align: right;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}

.order-footer {
  padding: 12px 15px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #dcdfe6;
}

.order-time {
  color: #909399;
  font-size: 14px;
}
</style>
