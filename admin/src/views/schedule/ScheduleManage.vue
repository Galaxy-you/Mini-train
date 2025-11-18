<template>
  <div class="schedule-manage">
    <div class="page-header">
      <h2>时刻表管理</h2>
      <el-button type="primary" @click="handleAddSchedule">添加时刻表</el-button>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="车次编号">
          <el-input v-model="searchForm.trainCode" placeholder="请输入车次编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="站点名称">
          <el-input v-model="searchForm.stationName" placeholder="请输入站点名称" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSchedules">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="scheduleList" 
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="trainCode" label="车次编号" width="120" />
      <el-table-column prop="stationName" label="站点名称" min-width="120" />
      <el-table-column prop="arriveTime" label="到达时间" width="120" />
      <el-table-column prop="departureTime" label="出发时间" width="120" />
      <el-table-column prop="stopMinutes" label="停留时间(分钟)" width="120" />
      <el-table-column prop="distance" label="里程(公里)" width="120" />
      <el-table-column prop="sequence" label="站序" width="80" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="getStationType(scope.row.type)">
            {{ scope.row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEditSchedule(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteSchedule(scope.row)">删除</el-button>
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
    
    <!-- 时刻表表单对话框 -->
    <el-dialog 
      v-model="scheduleDialog.visible" 
      :title="scheduleDialog.isEdit ? '编辑时刻表' : '添加时刻表'"
      width="650px"
    >
      <el-form 
        :model="scheduleForm" 
        :rules="scheduleRules"
        ref="scheduleFormRef"
        label-width="120px"
      >
        <el-form-item label="车次" prop="trainId">
          <el-select 
            v-model="scheduleForm.trainId" 
            placeholder="请选择车次"
            filterable
            style="width: 100%;"
            @change="onTrainChange"
          >
            <el-option
              v-for="item in trainOptions"
              :key="item.id"
              :label="`${item.code} - ${item.name}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="站点" prop="stationId">
          <el-select 
            v-model="scheduleForm.stationId" 
            placeholder="请选择站点"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="item in stationOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="到达时间" prop="arriveTime">
          <el-time-picker 
            v-model="scheduleForm.arriveTime" 
            placeholder="选择到达时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="出发时间" prop="departureTime">
          <el-time-picker 
            v-model="scheduleForm.departureTime" 
            placeholder="选择出发时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="停留时间(分钟)" prop="stopMinutes">
          <el-input-number 
            v-model="scheduleForm.stopMinutes" 
            :min="0" 
            :max="1000"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="里程(公里)" prop="distance">
          <el-input-number 
            v-model="scheduleForm.distance" 
            :min="0" 
            :precision="1"
            :step="0.1"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="站序" prop="sequence">
          <el-input-number 
            v-model="scheduleForm.sequence" 
            :min="1" 
            :max="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="scheduleForm.type" placeholder="请选择类型" style="width: 100%;">
            <el-option label="始发站" value="始发站"></el-option>
            <el-option label="终到站" value="终到站"></el-option>
            <el-option label="途经站" value="途经站"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitScheduleForm" :loading="scheduleDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { dataHelper } from '@/utils/dataHelper'

export default {
  name: 'ScheduleManage',
  setup() {
    // 时刻表列表数据
    const scheduleList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      trainCode: '',
      stationName: ''
    })
    const searchFormRef = ref(null)
    
    // 车次选项
    const trainOptions = ref([])
    // 站点选项
    const stationOptions = ref([])
    // 当前选中的车次信息
    const selectedTrain = ref(null)
    
    // 时刻表表单对话框
    const scheduleDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false
    })
    
    // 时刻表表单
    const scheduleForm = reactive({
      id: null,
      trainId: null,
      stationId: null,
      arriveTime: null,
      departureTime: null,
      stopMinutes: 0,
      distance: 0,
      sequence: 1,
      type: '途经站'
    })
    
    // 表单验证规则
    const scheduleRules = {
      trainId: [
        { required: true, message: '请选择车次', trigger: 'change' }
      ],
      stationId: [
        { required: true, message: '请选择站点', trigger: 'change' }
      ],
      arriveTime: [
        { required: true, message: '请选择到达时间', trigger: 'change' }
      ],
      departureTime: [
        { required: true, message: '请选择出发时间', trigger: 'change' }
      ],
      stopMinutes: [
        { required: true, message: '请输入停留时间', trigger: 'blur' }
      ],
      distance: [
        { required: true, message: '请输入里程', trigger: 'blur' }
      ],
      sequence: [
        { required: true, message: '请输入站序', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择类型', trigger: 'change' }
      ]
    }
    const scheduleFormRef = ref(null)
    
    // 初始化数据
    onMounted(() => {
      fetchScheduleList()
      fetchTrainOptions()
      fetchStationOptions()
    })
    
    // 获取时刻表列表
    const fetchScheduleList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          trainCode: searchForm.trainCode || null,
          stationName: searchForm.stationName || null
        }
        
        const result = await adminAPI.schedule.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        scheduleList.value = processedData.content
        pagination.total = processedData.totalElements
      } catch (error) {
        ElMessage.error(error.message || '获取时刻表列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取车次选项
    const fetchTrainOptions = async () => {
      try {
        const result = await adminAPI.train.list({ page: 0, size: 1000 })
        const processedData = dataHelper.handlePaginationData(result)
        trainOptions.value = processedData.content
      } catch (error) {
        ElMessage.error(error.message || '获取车次选项失败')
      }
    }
    
    // 获取站点选项
    const fetchStationOptions = async () => {
      try {
        const result = await adminAPI.station.list({ page: 0, size: 1000 })
        const processedData = dataHelper.handlePaginationData(result)
        stationOptions.value = processedData.content
      } catch (error) {
        ElMessage.error(error.message || '获取站点选项失败')
      }
    }
    
    // 车次变化时的处理
    const onTrainChange = (trainId) => {
      const train = trainOptions.value.find(t => t.id === trainId)
      selectedTrain.value = train
      
      // 如果是始发站，自动填写信息
      if (train) {
        const startStation = stationOptions.value.find(s => s.name === train.startStation)
        if (startStation && !scheduleDialog.isEdit) {
          scheduleForm.sequence = 1
          scheduleForm.type = '始发站'
          scheduleForm.distance = 0
          scheduleForm.stopMinutes = 0
          // 检查是否有相同的车次和站序的记录
          checkDuplicateSequence(trainId, 1)
        }
      }
    }
    
    // 检查是否存在相同车次和站序的记录
    const checkDuplicateSequence = async (trainId, sequence) => {
      try {
        const params = {
          trainId,
          sequence
        }
        const result = await adminAPI.schedule.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        if (processedData.content.length > 0) {
          ElMessage.warning(`该车次已存在站序为 ${sequence} 的记录`)
        }
      } catch (error) {
        console.error('检查站序重复出错:', error)
      }
    }
    
    // 搜索时刻表
    const searchSchedules = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchScheduleList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchSchedules()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchScheduleList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchScheduleList()
    }
    
    // 获取站点类型标签样式
    const getStationType = (type) => {
      const types = {
        '始发站': 'success',
        '终到站': 'danger',
        '途经站': 'primary'
      }
      return types[type] || ''
    }
    
    // 打开添加时刻表对话框
    const handleAddSchedule = () => {
      scheduleDialog.isEdit = false
      // 重置表单
      scheduleForm.id = null
      scheduleForm.trainId = null
      scheduleForm.stationId = null
      scheduleForm.arriveTime = null
      scheduleForm.departureTime = null
      scheduleForm.stopMinutes = 0
      scheduleForm.distance = 0
      scheduleForm.sequence = 1
      scheduleForm.type = '途经站'
      
      scheduleDialog.visible = true
    }
    
    // 打开编辑时刻表对话框
    const handleEditSchedule = (row) => {
      scheduleDialog.isEdit = true
      // 填充表单
      scheduleForm.id = row.id
      
      // 从名称映射到ID
      const train = trainOptions.value.find(t => t.code === row.trainCode)
      const station = stationOptions.value.find(s => s.name === row.stationName)
      
      scheduleForm.trainId = train ? train.id : null
      scheduleForm.stationId = station ? station.id : null
      
      // 时间处理
      scheduleForm.arriveTime = row.arriveTime ? new Date('2000-01-01 ' + row.arriveTime) : null
      scheduleForm.departureTime = row.departureTime ? new Date('2000-01-01 ' + row.departureTime) : null
      
      scheduleForm.stopMinutes = row.stopMinutes
      scheduleForm.distance = row.distance
      scheduleForm.sequence = row.sequence
      scheduleForm.type = row.type
      
      scheduleDialog.visible = true
    }
    
    // 提交时刻表表单
    const submitScheduleForm = () => {
      scheduleFormRef.value.validate(async (valid) => {
        if (valid) {
          scheduleDialog.loading = true
          try {
            // 格式化时间
            const formatTime = (time) => {
              if (!time) return null
              const hours = time.getHours().toString().padStart(2, '0')
              const minutes = time.getMinutes().toString().padStart(2, '0')
              return `${hours}:${minutes}`
            }
            
            // 准备提交的数据
            const submitData = {
              trainId: scheduleForm.trainId,
              stationId: scheduleForm.stationId,
              arriveTime: formatTime(scheduleForm.arriveTime),
              departureTime: formatTime(scheduleForm.departureTime),
              stopMinutes: scheduleForm.stopMinutes,
              distance: scheduleForm.distance,
              sequence: scheduleForm.sequence,
              type: scheduleForm.type
            }
            
            if (scheduleDialog.isEdit) {
              // 编辑时刻表
              await adminAPI.schedule.update(scheduleForm.id, submitData)
              ElMessage.success('时刻表更新成功')
            } else {
              // 添加时刻表
              await adminAPI.schedule.add(submitData)
              ElMessage.success('时刻表添加成功')
            }
            scheduleDialog.visible = false
            fetchScheduleList() // 刷新列表
          } catch (error) {
            ElMessage.error(error.message || '操作失败')
          } finally {
            scheduleDialog.loading = false
          }
        }
      })
    }
    
    // 删除时刻表
    const handleDeleteSchedule = (row) => {
      ElMessageBox.confirm(
        `确定要删除${row.trainCode}的${row.stationName}站时刻表吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.schedule.delete(row.id)
          ElMessage.success('时刻表删除成功')
          fetchScheduleList() // 刷新列表
        } catch (error) {
          ElMessage.error(error.message || '删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    return {
      scheduleList,
      loading,
      pagination,
      searchForm,
      searchFormRef,
      trainOptions,
      stationOptions,
      selectedTrain,
      scheduleDialog,
      scheduleForm,
      scheduleRules,
      scheduleFormRef,
      fetchScheduleList,
      onTrainChange,
      searchSchedules,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      getStationType,
      handleAddSchedule,
      handleEditSchedule,
      submitScheduleForm,
      handleDeleteSchedule
    }
  }
}
</script>

<style scoped>
.schedule-manage {
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
</style>
