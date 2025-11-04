<template>
  <div class="ticket-manage">
    <div class="page-header">
      <h2>车票管理</h2>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="车票编号">
          <el-input v-model="searchForm.ticketNo" placeholder="请输入车票编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="乘客姓名">
          <el-input v-model="searchForm.passengerName" placeholder="请输入乘客姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="车次编号">
          <el-input v-model="searchForm.trainCode" placeholder="请输入车次编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="车票状态">
          <el-select v-model="searchForm.status" placeholder="请选择车票状态" clearable>
            <el-option label="已出票" value="已出票"></el-option>
            <el-option label="已取消" value="已取消"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchTickets">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="ticketList" 
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="ticketNo" label="车票编号" width="110" />
      <el-table-column prop="orderNo" label="订单编号" width="110" />
      <el-table-column prop="trainCode" label="车次编号" width="90" />
      <el-table-column label="乘车区间" min-width="160">
        <template #default="scope">
          {{ scope.row.fromStation }} <el-icon><ArrowRight /></el-icon> {{ scope.row.toStation }}
        </template>
      </el-table-column>
      <el-table-column prop="departureTime" label="发车时间" width="150" />
      <el-table-column prop="arrivalTime" label="到达时间" width="150" />
      <el-table-column prop="passengerName" label="乘客姓名" width="100" />
      <el-table-column prop="seatType" label="座位类型" width="100" />
      <el-table-column prop="seatNumber" label="座位号" width="100" />
      <el-table-column prop="price" label="票价(元)" width="100">
        <template #default="scope">
          {{ formatPrice(scope.row.price) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getTicketStatusType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleViewTicket(scope.row)">查看</el-button>
          <el-button 
            size="small" 
            type="danger" 
            v-if="scope.row.status === '已出票'"
            @click="handleCancelTicket(scope.row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 车票详情对话框 -->
    <el-dialog 
      v-model="ticketDialog.visible" 
      title="车票详情"
      width="680px"
    >
      <div v-loading="ticketDialog.loading">
        <div class="ticket-detail" v-if="ticketDetail">
          <div class="ticket-header">
            <div class="train-info">
              <h3>{{ ticketDetail.trainCode }} 次列车</h3>
              <div class="train-date">{{ formatDate(ticketDetail.departureTime) }}</div>
            </div>
            <div class="status-tag">
              <el-tag :type="getTicketStatusType(ticketDetail.status)">{{ ticketDetail.status }}</el-tag>
            </div>
          </div>
          
          <div class="station-info">
            <div class="from-station">
              <div class="time">{{ formatTime(ticketDetail.departureTime) }}</div>
              <div class="station">{{ ticketDetail.fromStation }}</div>
            </div>
            <div class="duration">
              <div class="arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
              <div class="duration-text">{{ calculateDuration(ticketDetail.departureTime, ticketDetail.arrivalTime) }}</div>
            </div>
            <div class="to-station">
              <div class="time">{{ formatTime(ticketDetail.arrivalTime) }}</div>
              <div class="station">{{ ticketDetail.toStation }}</div>
            </div>
          </div>
          
          <el-divider></el-divider>
          
          <div class="passenger-info">
            <div class="info-item">
              <span class="label">乘客姓名：</span>
              <span class="value">{{ ticketDetail.passengerName }}</span>
            </div>
            <div class="info-item">
              <span class="label">证件号码：</span>
              <span class="value">{{ ticketDetail.passengerCard }}</span>
            </div>
            <div class="info-item">
              <span class="label">座位类型：</span>
              <span class="value">{{ ticketDetail.seatType }}</span>
            </div>
            <div class="info-item">
              <span class="label">座位号：</span>
              <span class="value">{{ ticketDetail.seatNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">票价：</span>
              <span class="value price">¥{{ formatPrice(ticketDetail.price) }}</span>
            </div>
            <div class="info-item">
              <span class="label">车票编号：</span>
              <span class="value">{{ ticketDetail.ticketNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单编号：</span>
              <span class="value">{{ ticketDetail.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">购票时间：</span>
              <span class="value">{{ formatFullDateTime(ticketDetail.createTime) }}</span>
            </div>
            <div class="info-item" v-if="ticketDetail.status === '已取消'">
              <span class="label">取消时间：</span>
              <span class="value">{{ formatFullDateTime(ticketDetail.updateTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ticketDialog.visible = false">关闭</el-button>
          <el-button 
            type="danger" 
            v-if="ticketDetail && ticketDetail.status === '已出票'"
            @click="handleCancelDialogTicket"
          >
            取消票
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { ArrowRight } from '@element-plus/icons-vue'
import { dataHelper } from '@/utils/dataHelper'

export default {
  name: 'TicketManage',
  components: {
    ArrowRight
  },
  setup() {
    // 车票列表数据
    const ticketList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      ticketNo: '',
      passengerName: '',
      trainCode: '',
      status: ''
    })
    const searchFormRef = ref(null)
    
    // 车票详情对话框
    const ticketDialog = reactive({
      visible: false,
      loading: false
    })
    const ticketDetail = ref(null)
    
    // 初始化数据
    onMounted(() => {
      fetchTicketList()
    })
    
    // 获取车票列表
    const fetchTicketList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          ticketNo: searchForm.ticketNo || null,
          passengerName: searchForm.passengerName || null,
          trainCode: searchForm.trainCode || null,
          status: searchForm.status || null
        }
        
        const result = await adminAPI.ticket.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        ticketList.value = processedData.content
        pagination.total = processedData.totalElements
      } catch (error) {
        ElMessage.error(error.message || '获取车票列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索车票
    const searchTickets = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchTicketList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchTickets()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchTicketList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchTicketList()
    }
    
    // 获取车票状态对应的tag类型
    const getTicketStatusType = (status) => {
      const types = {
        '已出票': 'success',
        '已取消': 'danger'
      }
      return types[status] || 'info'
    }
    
    // 查看车票详情
    const handleViewTicket = async (row) => {
      ticketDialog.visible = true
      ticketDialog.loading = true
      ticketDetail.value = null
      
      try {
        const result = await adminAPI.ticket.get(row.id)
        ticketDetail.value = result
      } catch (error) {
        ElMessage.error(error.message || '获取车票详情失败')
      } finally {
        ticketDialog.loading = false
      }
    }
    
    // 取消车票
    const handleCancelTicket = (row) => {
      ElMessageBox.confirm(
        `确定要取消车票 ${row.ticketNo} 吗？此操作不可恢复！`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.ticket.cancel(row.id)
          ElMessage.success('车票取消成功')
          fetchTicketList() // 刷新列表
        } catch (error) {
          ElMessage.error(error.message || '取消车票失败')
        }
      }).catch(() => {
        // 取消操作
      })
    }
    
    // 从详情对话框中取消车票
    const handleCancelDialogTicket = () => {
      if (ticketDetail.value) {
        ElMessageBox.confirm(
          `确定要取消车票 ${ticketDetail.value.ticketNo} 吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(async () => {
          try {
            await adminAPI.ticket.cancel(ticketDetail.value.id)
            ElMessage.success('车票取消成功')
            // 刷新详情
            const result = await adminAPI.ticket.get(ticketDetail.value.id)
            ticketDetail.value = result
            fetchTicketList() // 刷新列表
          } catch (error) {
            ElMessage.error(error.message || '取消车票失败')
          }
        }).catch(() => {
          // 取消操作
        })
      }
    }
    
    // 格式化金额
    const formatPrice = (price) => {
      if (price === undefined || price === null) return '0.00'
      return Number(price).toFixed(2)
    }
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    
    // 格式化时间
    const formatTime = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    }
    
    // 格式化完整的日期时间
    const formatFullDateTime = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }).replace(/\//g, '-')
    }
    
    // 计算行程时间
    const calculateDuration = (departureTimeStr, arrivalTimeStr) => {
      if (!departureTimeStr || !arrivalTimeStr) return ''
      
      const departureTime = new Date(departureTimeStr)
      const arrivalTime = new Date(arrivalTimeStr)
      
      // 计算时间差（毫秒）
      let diffMs = arrivalTime - departureTime
      
      // 处理可能的跨天情况
      if (diffMs < 0) {
        diffMs += 24 * 60 * 60 * 1000 // 加24小时
      }
      
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
      const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
      
      return `${diffHours}小时${diffMinutes}分钟`
    }
    
    return {
      ticketList,
      loading,
      pagination,
      searchForm,
      searchFormRef,
      ticketDialog,
      ticketDetail,
      fetchTicketList,
      searchTickets,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      getTicketStatusType,
      handleViewTicket,
      handleCancelTicket,
      handleCancelDialogTicket,
      formatPrice,
      formatDate,
      formatTime,
      formatFullDateTime,
      calculateDuration
    }
  }
}
</script>

<style scoped>
.ticket-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.ticket-detail {
  padding: 10px;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.train-info {
  display: flex;
  flex-direction: column;
}

.train-info h3 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}

.train-date {
  color: #606266;
  font-size: 14px;
  margin-top: 5px;
}

.station-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.from-station, .to-station {
  flex: 1;
  text-align: center;
}

.time {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.station {
  margin-top: 10px;
  font-size: 16px;
  color: #606266;
}

.duration {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.arrow {
  font-size: 24px;
  color: #409EFF;
}

.duration-text {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.passenger-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-top: 20px;
}

.info-item {
  display: flex;
}

.label {
  font-weight: bold;
  color: #606266;
  width: 100px;
}

.value {
  flex: 1;
  color: #303133;
}

.value.price {
  color: #F56C6C;
  font-weight: bold;
}
</style>
