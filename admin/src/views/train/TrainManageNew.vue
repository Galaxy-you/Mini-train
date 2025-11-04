template&gt;
  div class="train-manage"&gt;
    div class="page-header"&gt;
      h2&gt;车次管理/h2&gt;
      el-button type="primary" @click="handleAddTrain"&gt;添加车次/el-button&gt;
    /div&gt;
    
    !-- 搜索表单 --&gt;
    div class="search-form"&gt;
      el-form :inline="true" :model="searchForm" ref="searchFormRef"&gt;
        el-form-item label="车次编号"&gt;
          el-input v-model="searchForm.code" placeholder="请输入车次编号" clearable&gt;/el-input&gt;
        /el-form-item&gt;
        el-form-item label="车次类型"&gt;
          el-select v-model="searchForm.type" placeholder="请选择车次类型" clearable&gt;
            el-option label="高铁" value="高铁"&gt;/el-option&gt;
            el-option label="动车" value="动车"&gt;/el-option&gt;
            el-option label="特快" value="特快"&gt;/el-option&gt;
            el-option label="快速" value="快速"&gt;/el-option&gt;
            el-option label="普通" value="普通"&gt;/el-option&gt;
          /el-select&gt;
        /el-form-item&gt;
        el-form-item&gt;
          el-button type="primary" @click="searchTrains"&gt;查询/el-button&gt;
          el-button @click="resetSearch"&gt;重置/el-button&gt;
        /el-form-item&gt;
      /el-form&gt;
    /div&gt;
    
    !-- 数据表格 --&gt;
    el-table 
      v-loading="loading" 
      :data="trainList" 
      style="width: 100%"
      border
    &gt;
      el-table-column label="车次编号" min-width="100"&gt;
        template #default="scope"&gt;
          {{ scope.row.trainNumber || scope.row.code }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="车型" width="100"&gt;
        template #default="scope"&gt;
          {{ scope.row.type }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="始发站" min-width="120"&gt;
        template #default="scope"&gt;
          {{ scope.row.startStation }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="终点站" min-width="120"&gt;
        template #default="scope"&gt;
          {{ scope.row.endStation }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="发车时间" width="100"&gt;
        template #default="scope"&gt;
          {{ formatTime(scope.row.departureTime || scope.row.startTime) }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="到达时间" width="100"&gt;
        template #default="scope"&gt;
          {{ formatTime(scope.row.arrivalTime || scope.row.endTime) }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="运行时长" width="100"&gt;
        template #default="scope"&gt;
          {{ calculateDuration(scope.row.departureTime || scope.row.startTime, scope.row.arrivalTime || scope.row.endTime) }}
        /template&gt;
      /el-table-column&gt;
      el-table-column label="状态" width="80"&gt;
        template #default="scope"&gt;
          el-tag :type="getStatusType(scope.row.status)"&gt;
            {{ scope.row.status }}
          /el-tag&gt;
        /template&gt;
      /el-table-column&gt;
      el-table-column label="操作" width="250" fixed="right"&gt;
        template #default="scope"&gt;
          el-button size="small" @click="handleEditTrain(scope.row)"&gt;编辑/el-button&gt;
          el-button size="small" type="success" @click="viewTrainDetail(scope.row)"&gt;查看详情/el-button&gt;
          el-button size="small" type="danger" @click="handleDeleteTrain(scope.row)"&gt;删除/el-button&gt;
        /template&gt;
      /el-table-column&gt;
    /el-table&gt;
    
    !-- 分页 --&gt;
    div class="pagination-container"&gt;
      el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      /&gt;
    /div&gt;
    
    !-- Script部分不变 --&gt;
  /div&gt;
/template&gt;

script&gt;
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { dataHelper } from '@/utils/dataHelper'

export default {
  name: 'TrainManage',
  setup() {
    // Data
    const trainList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // Methods
    const formatTime = (time) => {
      if (!time) return '-'
      return time.split(' ')[1] || time
    }
    
    const calculateDuration = (startTime, endTime) => {
      if (!startTime || !endTime) return '-'
      
      const start = new Date(`2000-01-01 ${startTime}`)
      const end = new Date(`2000-01-01 ${endTime}`)
      
      const diff = end - start
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      
      return `${hours}小时${minutes}分钟`
    }
    
    const getStatusType = (status) => {
      return status === '正常' ? 'success' : 'danger'
    }
    
    // Fetch Data
    const fetchTrainList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1,
          size: pagination.pageSize
        }
        const result = await adminAPI.train.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        trainList.value = processedData.content
        pagination.total = processedData.totalElements
      } catch (error) {
        ElMessage.error(error.message || '获取车次列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // Lifecycle
    onMounted(() => {
      fetchTrainList()
    })
    
    return {
      trainList,
      loading,
      pagination,
      formatTime,
      calculateDuration,
      getStatusType,
      fetchTrainList
      // ...其他方法
    }
  }
}
/script&gt;

style scoped&gt;
.train-manage {
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
/style&gt;
