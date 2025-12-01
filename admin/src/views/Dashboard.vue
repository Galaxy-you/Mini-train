<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <span>总用户数</span>
            <el-icon class="icon"><User /></el-icon>
          </div>
          <div class="data-content">
            <span class="count">{{ stats.userCount || '0' }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <span>总车次数</span>
            <el-icon class="icon"><Ship /></el-icon>
          </div>
          <div class="data-content">
            <span class="count">{{ stats.trainCount || '0' }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <span>总订单数</span>
            <el-icon class="icon"><List /></el-icon>
          </div>
          <div class="data-content">
            <span class="count">{{ stats.orderCount || '0' }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <div class="data-header">
            <span>总车票数</span>
            <el-icon class="icon"><Ticket /></el-icon>
          </div>
          <div class="data-content">
            <span class="count">{{ stats.ticketCount || '0' }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>订单统计（近7天）</span>
            </div>
          </template>
          <div class="chart" ref="orderChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门车次排行</span>
            </div>
          </template>
          <div class="chart" ref="trainChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
            </div>
          </template>
          <el-table :data="latestOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单编号" width="180" />
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="trainCode" label="车次" width="100" />
            <el-table-column prop="startStation" label="出发站" width="120" />
            <el-table-column prop="endStation" label="目的站" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="amount" label="金额" width="100" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Ship, List, Ticket } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { adminAPI } from '@/api'

// 注册ECharts组件
echarts.use([
  TitleComponent, 
  TooltipComponent, 
  GridComponent, 
  LegendComponent,
  BarChart, 
  LineChart, 
  PieChart, 
  CanvasRenderer
])

export default {
  name: 'DashboardView',
  components: {
    User,
    Ship,
    List,
    Ticket
  },
  setup() {
    // 数据统计
    const stats = reactive({
      userCount: 0,
      trainCount: 0,
      orderCount: 0,
      ticketCount: 0,
    })
    
    // 图表DOM引用
    const orderChartRef = ref(null)
    const trainChartRef = ref(null)
    
    // 最新订单数据
    const latestOrders = ref([])
    
    // 初始化数据
    onMounted(async () => {
      try {
        // 获取系统统计数据
        const statsData = await adminAPI.stats.system()
        if (statsData && statsData.data) {
          Object.assign(stats, statsData.data)
        }

        // 获取订单统计数据
        const orderStatsData = await adminAPI.stats.order()
        if (orderStatsData && orderStatsData.data) {
          initOrderChart(orderStatsData.data)
        }

        // 获取热门车次数据
        const trainStatsData = await adminAPI.stats.popular()
        if (trainStatsData && trainStatsData.data) {
          initTrainChart(trainStatsData.data)
        }
      } catch (error) {
        console.error('加载仪表盘数据出错:', error)
        ElMessage.error('加载仪表盘数据失败')
      }
    })
    
    // 初始化订单图表
    const initOrderChart = (data) => {
      const chart = echarts.init(orderChartRef.value)
      const option = {
        title: {
          text: '订单统计'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '订单数',
            data: data.orders,
            type: 'line',
            smooth: true
          }
        ]
      }
      chart.setOption(option)
      // 响应窗口大小变化
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
    
    // 初始化热门车次图表
    const initTrainChart = (data) => {
      const chart = echarts.init(trainChartRef.value)
      const option = {
        title: {
          text: '热门车次'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: data.trains,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '订票数',
            data: data.counts,
            type: 'bar'
          }
        ]
      }
      chart.setOption(option)
      // 响应窗口大小变化
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
    
    // 获取状态标签类型
    const getStatusType = (status) => {
      const types = {
        '待支付': 'warning',
        '已支付': 'success',
        '已取消': 'info',
        '已完成': 'primary'
      }
      return types[status] || ''
    }
    
    return {
      stats,
      orderChartRef,
      trainChartRef,
      latestOrders,
      getStatusType
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.data-card {
  height: 120px;
  margin-bottom: 20px;
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.data-header .icon {
  font-size: 24px;
  color: #409EFF;
}

.data-content {
  text-align: center;
}

.data-content .count {
  font-size: 28px;
  font-weight: bold;
}

.chart-card {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
