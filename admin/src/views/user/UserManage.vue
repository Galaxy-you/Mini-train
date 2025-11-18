<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable></el-input>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="searchForm.authStatus" placeholder="请选择认证状态" clearable>
            <el-option label="已认证" :value="1"></el-option>
            <el-option label="未认证" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="userList" 
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="cardId" label="身份证号" min-width="160" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="authStatus" label="认证状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.authStatus === 1 ? 'success' : 'info'">
            {{ scope.row.authStatus === 1 ? '已认证' : '未认证' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="150">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后更新时间" min-width="150">
        <template #default="scope">
          {{ formatDateTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleViewUser(scope.row)">查看</el-button>
          <el-button size="small" type="danger" @click="handleResetPassword(scope.row)">重置密码</el-button>
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
    
    <!-- 用户详情对话框 -->
    <el-dialog 
      v-model="userDialog.visible" 
      title="用户详情"
      width="500px"
    >
      <div v-loading="userDialog.loading">
        <el-descriptions 
          class="user-descriptions" 
          :column="1" 
          border 
          v-if="userDetail"
        >
          <el-descriptions-item label="用户ID">{{ userDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ userDetail.realName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ userDetail.cardId || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userDetail.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="userDetail.authStatus === 1 ? 'success' : 'info'">
              {{ userDetail.authStatus === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(userDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后更新时间">{{ formatDateTime(userDetail.updateTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 用户乘车人信息 -->
        <div class="user-passengers" v-if="userDetail && userPassengers.length > 0">
          <h3>关联乘车人</h3>
          <el-table :data="userPassengers" style="width: 100%" border>
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="cardId" label="证件号" min-width="160" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="isDefault" label="默认" width="60">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.isDefault">是</el-tag>
                <span v-else>否</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 用户票务记录摘要 -->
        <div class="user-stats" v-if="userDetail">
          <h3>票务统计</h3>
          <div class="stat-cards">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Tickets /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.totalTickets || 0 }}</div>
                <div class="stat-label">总购票数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.totalOrders || 0 }}</div>
                <div class="stat-label">总订单数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon active">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-content active">
                <div class="stat-value">¥{{ formatPrice(userStats.totalAmount) }}</div>
                <div class="stat-label">消费总额</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialog.visible = false">关闭</el-button>
          <el-button type="danger" @click="handleResetUserPassword">重置密码</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { Tickets, Document, Money } from '@element-plus/icons-vue'
import { dataHelper } from '@/utils/dataHelper'

export default {
  name: 'UserManage',
  components: {
    Tickets, Document, Money
  },
  setup() {
    // 用户列表数据
    const userList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      username: '',
      realName: '',
      phone: '',
      authStatus: ''
    })
    const searchFormRef = ref(null)
    
    // 用户详情对话框
    const userDialog = reactive({
      visible: false,
      loading: false
    })
    const userDetail = ref(null)
    const userPassengers = ref([])
    const userStats = reactive({
      totalTickets: 0,
      totalOrders: 0,
      totalAmount: 0
    })
    
    // 初始化数据
    onMounted(() => {
      fetchUserList()
    })
    
    // 获取用户列表
    const fetchUserList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          keyword: searchForm.username || searchForm.realName || searchForm.phone || null,
          authStatus: searchForm.authStatus !== '' ? searchForm.authStatus : null
        }
        
        const response = await adminAPI.user.list(params)
        console.log('API返回的原始数据:', response)
        
        const processedData = dataHelper.handlePaginationData(response)
        console.log('处理后的分页数据:', processedData)
        
        userList.value = processedData.content
        pagination.total = processedData.totalElements
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error(error.message || '获取用户列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索用户
    const searchUsers = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchUserList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchUsers()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchUserList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchUserList()
    }
    
    // 查看用户详情
    const handleViewUser = async (row) => {
      userDialog.visible = true
      userDialog.loading = true
      userDetail.value = null
      userPassengers.value = []
      
      try {
        // 获取用户详情
        const result = await adminAPI.user.get(row.id)
        userDetail.value = result
        
        // 从API返回的数据中获取乘车人信息
        if (result.passengers) {
          userPassengers.value = dataHelper.ensureArray(result.passengers).map(passenger => ({
            realName: passenger.name,
            cardId: passenger.idCard,
            phone: passenger.phone,
            type: '成人', // 默认成人类型，实际应该从后端获取
            isDefault: passenger.isDefault
          }))
        }
        
        // 从API返回的数据中获取订单统计数据
        if (result.orderStats) {
          userStats.totalTickets = result.orderStats.totalTickets || 0
          userStats.totalOrders = result.orderStats.totalOrders || 0
          userStats.totalAmount = result.orderStats.totalAmount || 0
        }
      } catch (error) {
        ElMessage.error(error.message || '获取用户详情失败')
      } finally {
        userDialog.loading = false
      }
    }
    
    // 重置用户密码
    const handleResetPassword = (row) => {
      ElMessageBox.confirm(
        `确定要重置用户 ${row.username} 的密码吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.user.resetPassword(row.id)
          ElMessage.success('密码重置成功，新密码已发送至用户邮箱')
        } catch (error) {
          ElMessage.error(error.message || '重置密码失败')
        }
      }).catch(() => {
        // 取消操作
      })
    }
    
    // 从详情对话框中重置密码
    const handleResetUserPassword = () => {
      if (userDetail.value) {
        handleResetPassword(userDetail.value)
      }
    }
    
    // 格式化日期时间
    const formatDateTime = (dateStr) => {
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
    
    // 格式化金额
    const formatPrice = (price) => {
      if (price === undefined || price === null) return '0.00'
      return Number(price).toFixed(2)
    }
    
    return {
      userList,
      loading,
      pagination,
      searchForm,
      searchFormRef,
      userDialog,
      userDetail,
      userPassengers,
      userStats,
      fetchUserList,
      searchUsers,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleViewUser,
      handleResetPassword,
      handleResetUserPassword,
      formatDateTime,
      formatPrice
    }
  }
}
</script>

<style scoped>
.user-manage {
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

.user-descriptions {
  margin-bottom: 20px;
}

.user-passengers {
  margin-top: 20px;
}

.user-passengers h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

.user-stats {
  margin-top: 20px;
}

.user-stats h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

.stat-cards {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 4px;
  background-color: #f5f7fa;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.stat-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 15px;
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  background-color: rgba(64, 158, 255, 0.1);
}

.stat-icon.active {
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.stat-content.active .stat-value {
  color: #F56C6C;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
