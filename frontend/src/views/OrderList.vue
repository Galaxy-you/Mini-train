<template>
  <div class="order-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <el-input
            v-model="searchKeyword"
            placeholder="输入起始站名称查询（如：上海、北京等）"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
        >
          <template #suffix>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-button type="info" @click="clearSearch" v-if="searchKeyword">清除搜索</el-button>
      </div>
      <div class="search-tips">
        <span v-if="!searchKeyword && isSearching">下面显示的是搜索的结果</span>
        <span v-else-if="!searchKeyword">帮助提示：输入起始站名称创会筛选订单</span>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-card class="tab-card" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部订单" name="all">
          <template #label>
            <span class="tab-label">
              <el-icon><tickets /></el-icon>
              全部订单
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="待支付" name="unpaid">
          <template #label>
            <span class="tab-label">
              <el-icon><wallet /></el-icon>
              待支付
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed">
          <template #label>
            <span class="tab-label">
              <el-icon><success-filled /></el-icon>
              已完成
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已取消" name="canceled">
          <template #label>
            <span class="tab-label">
              <el-icon><circle-close /></el-icon>
              已取消
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div class="order-card" v-for="order in orders" :key="order.orderNo">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)" size="large">
            {{ getOrderStatusText(order.status) }}
          </el-tag>
        </div>

        <div class="order-body">
          <div class="train-info">
            <div class="train-number">{{ order.trainNumber }}</div>
            <div class="train-route">
              <div class="route-station">
                <div class="station-name">{{ formatStation(order.startStation) }}</div>
                <div class="station-time">{{ order.departureTime }}</div>
              </div>
              <div class="route-arrow">
                <el-icon><right /></el-icon>
                <span class="duration" v-if="calculateDuration(order.departureTime, order.arrivalTime)">
                  {{ calculateDuration(order.departureTime, order.arrivalTime) }}
                </span>
              </div>
              <div class="route-station">
                <div class="station-name">{{ formatStation(order.endStation) }}</div>
                <div class="station-time">{{ order.arrivalTime }}</div>
              </div>
            </div>
          </div>

          <div class="order-details">
            <div class="detail-item">
              <span class="label">车票数量</span>
              <span class="value">{{ order.ticketCount }} 张</span>
            </div>
            <div class="detail-item total-amount">
              <span class="label">订单金额</span>
              <span class="value price">{{ formatPrice(order.totalAmount) }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
          <el-button
              size="small"
              type="danger"
              v-if="order.status === 'UNPAID'"
              @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
          <el-button
              size="small"
              type="primary"
              v-if="order.status === 'UNPAID'"
              @click="payOrder(order)"
          >
            去支付
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-if="orders.length === 0 && !loading"
          description="暂无订单"
          :image-size="200"
      />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Tickets, Wallet, SuccessFilled, CircleClose, Right } from '@element-plus/icons-vue';
import { Search } from '@element-plus/icons-vue';
import { orderAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice } from '@/utils/formatters';

export default {
  name: 'OrderList',
  components: {
    Tickets,
    Wallet,
    SuccessFilled,
    CircleClose,
    Right
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const allOrders = ref([]); // 存储所有订单
    const orders = ref([]); // 存储过滤后的订单
    const activeTab = ref('all');
    const searchKeyword = ref('');
    const isSearching = ref(false); // 是否正在搜索状态

    const fetchOrders = async () => {
      loading.value = true;
      try {
        const response = await orderAPI.listUserOrders();
        const extractedData = dataHelper.extractApiData(response);
        const orderData = dataHelper.ensureArray(extractedData);
        allOrders.value = orderData; // 保存所有订单
        orders.value = filterOrders(orderData); // 应用过滤
      } catch (error) {
        console.error('获取订单列表失败:', error);
        ElMessage.error('获取订单列表失败');
      } finally {
        loading.value = false;
      }
    };

    const filterOrders = (orderList) => {
      if (activeTab.value === 'all') {
        return orderList;
      }

      const statusMap = {
        unpaid: ['UNPAID'],
        completed: ['PAID', 'COMPLETED'],
        canceled: ['CANCELED']
      };

      const targetStatuses = statusMap[activeTab.value] || [];
      return orderList.filter(order => targetStatuses.includes(order.status));
    };

    // 监听 activeTab 变化，自动重新过滤订单
    watch(activeTab, () => {
      orders.value = filterOrders(allOrders.value);
    });

    const viewOrderDetail = (order) => {
      router.push(`/order/${order.orderNo}`);
    };

    const getStatusType = (status) => {
      const typeMap = {
        'UNPAID': 'warning',
        'PAID': 'success',
        'COMPLETED': 'success',
        'CANCELED': 'info'
      };
      return typeMap[status] || 'info';
    };

    const calculateDuration = (startTime, endTime) => {
      if (!startTime || !endTime) return '';

      try {
        // 处理时间格式，可能是 HH:mm 或完整的日期时间
        if (startTime.includes(':') && startTime.length <= 5) {
          // 纯时间格式 HH:mm
          const startParts = startTime.split(':');
          const endParts = endTime.split(':');

          if (startParts.length < 2 || endParts.length < 2) return '';

          const startMinutes = parseInt(startParts[0]) * 60 + parseInt(startParts[1]);
          const endMinutes = parseInt(endParts[0]) * 60 + parseInt(endParts[1]);

          if (isNaN(startMinutes) || isNaN(endMinutes)) return '';

          let diff = endMinutes - startMinutes;
          if (diff < 0) diff += 24 * 60;

          const hours = Math.floor(diff / 60);
          const minutes = diff % 60;
          return `${hours}小时${minutes > 0 ? minutes + '分' : ''}`;
        } else {
          // 完整日期时间格式
          const start = new Date(startTime);
          const end = new Date(endTime);

          if (isNaN(start.getTime()) || isNaN(end.getTime())) return '';

          const diffMs = end - start;
          const diffMinutes = Math.floor(diffMs / (1000 * 60));
          const hours = Math.floor(diffMinutes / 60);
          const minutes = diffMinutes % 60;
          return `${hours}小时${minutes > 0 ? minutes + '分' : ''}`;
        }
      } catch (error) {
        console.error('时间计算错误:', error);
        return '';
      }
    };

    const formatTime = (dateTime) => {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    const cancelOrder = (order) => {
      ElMessageBox.confirm('确定要取消该订单吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await orderAPI.cancelOrder(order.orderNo);
          ElMessage.success('订单取消成功');
          fetchOrders();
        } catch (error) {
          console.error('取消订单失败:', error);
          ElMessage.error('取消订单失败');
        }
      }).catch(() => {});
    };

    const payOrder = (order) => {
      router.push(`/order/${order.orderNo}`);
    };

    const getOrderStatusText = (status) => {
      const statusMap = {
        'UNPAID': '待支付',
        'PAID': '已支付',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
      };
      return statusMap[status] || status;
    };

    const handleSearch = async () => {
      if (!searchKeyword.value.trim()) {
        ElMessage.warning('请输入起始站名称');
        return;
      }

      loading.value = true;
      isSearching.value = true;
      try {
        const response = await orderAPI.searchOrdersByStartStation(searchKeyword.value);
        const extractedData = dataHelper.extractApiData(response);
        const orderData = dataHelper.ensureArray(extractedData);
        allOrders.value = orderData; // 更新为搜索结果
        orders.value = filterOrders(orderData); // 应用过滤
        
        if (orderData.length === 0) {
          ElMessage.info('未找到匹配的订单');
        } else {
          ElMessage.success(`找到 ${orderData.length} 个订单`);
        }
      } catch (error) {
        console.error('搜索订单失败:', error);
        ElMessage.error('搜索订单失败');
      } finally {
        loading.value = false;
      }
    };

    const clearSearch = async () => {
      searchKeyword.value = '';
      isSearching.value = false;
      // 重新加载所有订单
      loading.value = true;
      try {
        const response = await orderAPI.listUserOrders();
        const extractedData = dataHelper.extractApiData(response);
        const orderData = dataHelper.ensureArray(extractedData);
        allOrders.value = orderData;
        orders.value = filterOrders(orderData);
        ElMessage.success('已清除搜索条件');
      } catch (error) {
        console.error('重新加载订单失败:', error);
        ElMessage.error('重新加载失败');
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchOrders();
    });

    return {
      loading,
      orders,
      activeTab,
      searchKeyword,
      Search,
      viewOrderDetail,
      cancelOrder,
      payOrder,
      getOrderStatusText,
      getStatusType,
      calculateDuration,
      formatTime,
      formatStation,
      formatPrice,
      handleSearch,
      clearSearch
    };
  }
}
</script>

<style scoped>
.order-list-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* 搜索一样式 */
.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.search-input {
  flex: 1;
}

.search-tips {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
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

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 16px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  color: #8c8c8c;
}

/* 订单主体 */
.order-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.train-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.train-number {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.train-route {
  display: flex;
  align-items: center;
  gap: 24px;
}

.route-station {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.station-name {
  font-size: 18px;
  font-weight: 500;
  color: #262626;
}

.station-time {
  font-size: 14px;
  color: #595959;
}

.route-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #1890ff;
}

.route-arrow .el-icon {
  font-size: 20px;
}

.duration {
  font-size: 12px;
  color: #8c8c8c;
}

/* 订单详情 */
.order-details {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 12px;
  color: #8c8c8c;
}

.detail-item .value {
  font-size: 16px;
  color: #262626;
  font-weight: 500;
}

.total-amount .value.price {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: 600;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
  margin-top: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .train-route {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-details {
    flex-direction: column;
    gap: 12px;
  }

  .order-footer {
    flex-direction: column;
  }
}
</style>
