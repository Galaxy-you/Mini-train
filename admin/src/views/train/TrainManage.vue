<template>
  <div class="train-manage">
    <div class="page-header">
      <h2>车次管理</h2>
      <el-button type="primary" @click="handleAddTrain">添加车次</el-button>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="车次编号">
          <el-input v-model="searchForm.code" placeholder="请输入车次编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="车次类型">
          <el-select v-model="searchForm.type" placeholder="请选择车次类型" clearable>
            <el-option label="高铁" value="高铁"></el-option>
            <el-option label="动车" value="动车"></el-option>
            <el-option label="特快" value="特快"></el-option>
            <el-option label="快速" value="快速"></el-option>
            <el-option label="普通" value="普通"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchTrains">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="trainList" 
      style="width: 100%"
      border
    >
      <el-table-column label="车次编号" min-width="100">
        <template #default="scope">
          {{ scope.row.trainNumber }}
        </template>
      </el-table-column>
      <el-table-column label="车型" width="100">
        <template #default="scope">
          {{ scope.row.type }}
        </template>
      </el-table-column>
      <el-table-column label="始发站" min-width="120">
        <template #default="scope">
          {{ formatStationName(scope.row.startStation) }}
        </template>
      </el-table-column>
      <el-table-column label="终点站" min-width="120">
        <template #default="scope">
          {{ formatStationName(scope.row.endStation) }}
        </template>
      </el-table-column>
      <el-table-column label="发车时间" width="100">
        <template #default="scope">
          {{ scope.row.departureTime }}
        </template>
      </el-table-column>
      <el-table-column label="到达时间" width="100">
        <template #default="scope">
          {{ scope.row.arrivalTime }}
        </template>
      </el-table-column>
      <el-table-column label="运行时长" width="100">
        <template #default="scope">
          {{ formatDuration(scope.row.duration) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEditTrain(scope.row)">编辑</el-button>
          <el-button size="small" type="success" @click="viewTrainDetail(scope.row)">查看详情</el-button>
          <el-button size="small" type="danger" @click="handleDeleteTrain(scope.row)">删除</el-button>
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
    
    <!-- 车次表单对话框 -->
    <el-dialog 
      v-model="trainDialog.visible" 
      :title="trainDialog.isEdit ? '编辑车次' : '添加车次'"
      width="650px"
    >
      <el-form 
        :model="trainForm" 
        :rules="trainRules"
        ref="trainFormRef"
        label-width="100px"
      >
        <el-form-item label="车次编号" prop="code">
          <el-input v-model="trainForm.code" placeholder="请输入车次编号" />
        </el-form-item>
        <el-form-item label="车次名称" prop="name">
          <el-input v-model="trainForm.name" placeholder="请输入车次名称" />
        </el-form-item>
        <el-form-item label="车次类型" prop="type">
          <el-select v-model="trainForm.type" placeholder="请选择车次类型" style="width: 100%;">
            <el-option label="高铁" value="高铁"></el-option>
            <el-option label="动车" value="动车"></el-option>
            <el-option label="特快" value="特快"></el-option>
            <el-option label="快速" value="快速"></el-option>
            <el-option label="普通" value="普通"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="始发站" prop="startStationId">
          <el-select 
            v-model="trainForm.startStationId" 
            placeholder="请选择始发站"
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
        <el-form-item label="终点站" prop="endStationId">
          <el-select 
            v-model="trainForm.endStationId" 
            placeholder="请选择终点站"
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
        <el-form-item label="发车时间" prop="startTime">
          <el-time-picker 
            v-model="trainForm.startTime" 
            placeholder="选择发车时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="到达时间" prop="endTime">
          <el-time-picker 
            v-model="trainForm.endTime" 
            placeholder="选择到达时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="trainForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="正常" value="正常"></el-option>
            <el-option label="停运" value="停运"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="trainDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitTrainForm" :loading="trainDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 车次详情对话框 -->
    <el-dialog 
      v-model="detailDialog.visible" 
      title="车次详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车次编号">{{ detailTrain.code }}</el-descriptions-item>
        <el-descriptions-item label="车次名称">{{ detailTrain.name }}</el-descriptions-item>
        <el-descriptions-item label="车次类型">{{ detailTrain.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailTrain.status === '正常' ? 'success' : 'danger'">
            {{ detailTrain.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="始发站">{{ formatStationName(detailTrain.startStation) }}</el-descriptions-item>
        <el-descriptions-item label="终点站">{{ formatStationName(detailTrain.endStation) }}</el-descriptions-item>
        <el-descriptions-item label="发车时间">{{ detailTrain.startTime }}</el-descriptions-item>
        <el-descriptions-item label="到达时间">{{ detailTrain.endTime }}</el-descriptions-item>
        <el-descriptions-item label="运行时长">{{ formatDuration(detailTrain.duration) }}</el-descriptions-item>
        <el-descriptions-item label="总座位数">{{ detailTrain.totalSeats }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h3>座位类型及价格</h3>
        <el-table :data="detailTrain.seatTypes || []" border style="width: 100%">
          <el-table-column prop="type" label="座位类型" />
          <el-table-column prop="count" label="座位数量" />
          <el-table-column label="价格">
            <template #default="scope">
              {{ formatPrice(scope.row.price) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div style="margin-top: 20px;">
        <h3>停靠站信息</h3>
        <el-table :data="detailTrain.stations || []" border style="width: 100%">
          <el-table-column label="站点名称">
            <template #default="scope">
              {{ formatStationName(scope.row.stationName) }}
            </template>
          </el-table-column>
          <el-table-column prop="arriveTime" label="到达时间" />
          <el-table-column prop="departureTime" label="出发时间" />
          <el-table-column prop="stopMinutes" label="停留时间(分钟)" />
          <el-table-column prop="distance" label="里程(公里)" />
          <el-table-column prop="sequence" label="站序" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { dataHelper } from '@/utils/dataHelper'
// import { formatStationName } from '@/utils/stationHelper'
import { formatStationName, formatPrice, formatDuration } from '@/utils/utils'

  


export default {
  name: 'TrainManage',
  setup() {
    // 车次列表数据
    const trainList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      code: '',
      type: ''
    })
    const searchFormRef = ref(null)
    
    // 车站选项
    const stationOptions = ref([])
    
    // 车次表单对话框
    const trainDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false
    })
    
    // 车次详情对话框
    const detailDialog = reactive({
      visible: false
    })
    
    // 详情中的车次对象
    const detailTrain = reactive({
      code: '',
      name: '',
      type: '',
      status: '',
      startStation: '',
      endStation: '',
      startTime: '',
      endTime: '',
      duration: '',
      totalSeats: 0,
      seatTypes: [],
      stations: []
    })
    
    // 车次表单
    const trainForm = reactive({
      id: null,
      code: '',
      name: '',
      type: '高铁',
      startStationId: null,
      endStationId: null,
      startTime: null,
      endTime: null,
      status: '正常'
    })
    
    // 表单验证规则
    const trainRules = {
      code: [
        { required: true, message: '请输入车次编号', trigger: 'blur' },
        { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入车次名称', trigger: 'blur' },
        { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择车次类型', trigger: 'change' }
      ],
      startStationId: [
        { required: true, message: '请选择始发站', trigger: 'change' }
      ],
      endStationId: [
        { required: true, message: '请选择终点站', trigger: 'change' }
      ],
      startTime: [
        { required: true, message: '请选择发车时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择到达时间', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }
    const trainFormRef = ref(null)
    
    // 初始化数据
    onMounted(() => {
      fetchTrainList()
      fetchStationOptions()
    })
    
    // 获取车次列表
    const fetchTrainList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          code: searchForm.code || null,
          type: searchForm.type || null
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
    
    // 获取车站选项
    const fetchStationOptions = async () => {
      try {
        // 获取所有车站作为选项
        const result = await adminAPI.station.list({ page: 0, size: 1000 })
        const processedData = dataHelper.handlePaginationData(result)
        stationOptions.value = processedData.content
      } catch (error) {
        ElMessage.error(error.message || '获取车站选项失败')
      }
    }
    
    // 搜索车次
    const searchTrains = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchTrainList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchTrains()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchTrainList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchTrainList()
    }
    
    // 打开添加车次对话框
    const handleAddTrain = () => {
      trainDialog.isEdit = false
      // 重置表单
      trainForm.id = null
      trainForm.code = ''
      trainForm.name = ''
      trainForm.type = '高铁'
      trainForm.startStationId = null
      trainForm.endStationId = null
      trainForm.startTime = null
      trainForm.endTime = null
      trainForm.status = '正常'
      
      trainDialog.visible = true
    }
    
    // 打开编辑车次对话框
    const handleEditTrain = (row) => {
      trainDialog.isEdit = true
      // 填充表单
      trainForm.id = row.id
      trainForm.code = row.code
      trainForm.name = row.name
      trainForm.type = row.type
      
      // 需要从名称映射到ID
      const startStation = stationOptions.value.find(s => s.name === row.startStation)
      const endStation = stationOptions.value.find(s => s.name === row.endStation)
      
      trainForm.startStationId = startStation ? startStation.id : null
      trainForm.endStationId = endStation ? endStation.id : null
      
      // 时间处理
      trainForm.startTime = row.startTime ? new Date('2000-01-01 ' + row.startTime) : null
      trainForm.endTime = row.endTime ? new Date('2000-01-01 ' + row.endTime) : null
      
      trainForm.status = row.status
      
      trainDialog.visible = true
    }
    
    // 提交车次表单
    const submitTrainForm = () => {
      trainFormRef.value.validate(async (valid) => {
        if (valid) {
          trainDialog.loading = true
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
              code: trainForm.code,
              name: trainForm.name,
              type: trainForm.type,
              startStationId: trainForm.startStationId,
              endStationId: trainForm.endStationId,
              startTime: formatTime(trainForm.startTime),
              endTime: formatTime(trainForm.endTime),
              status: trainForm.status
            }
            
            if (trainDialog.isEdit) {
              // 编辑车次
              await adminAPI.train.update(trainForm.id, submitData)
              ElMessage.success('车次更新成功')
            } else {
              // 添加车次
              await adminAPI.train.add(submitData)
              ElMessage.success('车次添加成功')
            }
            trainDialog.visible = false
            fetchTrainList() // 刷新列表
          } catch (error) {
            ElMessage.error(error.message || '操作失败')
          } finally {
            trainDialog.loading = false
          }
        }
      })
    }
    
    // 查看车次详情
    const viewTrainDetail = async (row) => {
      try {
        const trainDetail = await adminAPI.train.get(row.id)
        // 复制详情到响应式对象
        Object.assign(detailTrain, trainDetail)
        detailDialog.visible = true
      } catch (error) {
        ElMessage.error(error.message || '获取车次详情失败')
      }
    }
    
    // 删除车次
    const handleDeleteTrain = (row) => {
      ElMessageBox.confirm(
        `确定要删除车次"${row.code}"吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.train.delete(row.id)
          ElMessage.success('车次删除成功')
          fetchTrainList() // 刷新列表
        } catch (error) {
          ElMessage.error(error.message || '删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    return {
      trainList,
      loading,
      pagination,
      searchForm,
      searchFormRef,
      stationOptions,
      trainDialog,
      trainForm,
      trainRules,
      trainFormRef,
      detailDialog,
      detailTrain,
      fetchTrainList,
      searchTrains,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleAddTrain,
      handleEditTrain,
      submitTrainForm,
      viewTrainDetail,
      handleDeleteTrain,
      formatStationName,
      formatPrice,
      formatDuration
    }
  }
}

</script>

<style scoped>
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
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
