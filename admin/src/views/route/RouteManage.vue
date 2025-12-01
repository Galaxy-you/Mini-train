<template>
  <div class="route-manage">
    <div class="page-header">
      <h2>线路管理</h2>
      <el-button type="primary" @click="handleAddRoute">添加线路</el-button>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="始发站">
          <el-input v-model="searchForm.startStation" placeholder="请输入始发站" clearable></el-input>
        </el-form-item>
        <el-form-item label="终点站">
          <el-input v-model="searchForm.endStation" placeholder="请输入终点站" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRoutes">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="routeList" 
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="线路名称" min-width="120" />
      <el-table-column prop="startStation" label="始发站" min-width="120" />
      <el-table-column prop="endStation" label="终点站" min-width="120" />
      <el-table-column prop="distance" label="距离(公里)" min-width="100" />
      <el-table-column prop="stationCount" label="站点数" min-width="100" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleViewRoute(scope.row)">查看</el-button>
          <el-button size="small" @click="handleEditRoute(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteRoute(scope.row)">删除</el-button>
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
    
    <!-- 线路表单对话框 -->
    <el-dialog 
      v-model="routeDialog.visible" 
      :title="routeDialog.isEdit ? '编辑线路' : '添加线路'"
      width="600px"
    >
      <el-form 
        :model="routeForm" 
        :rules="routeRules"
        ref="routeFormRef"
        label-width="100px"
      >
        <el-form-item label="线路名称" prop="name">
          <el-input v-model="routeForm.name" placeholder="请输入线路名称" />
        </el-form-item>
        
        <el-form-item label="始发站" prop="startStationId">
          <el-select v-model="routeForm.startStationId" placeholder="请选择始发站" filterable>
            <el-option
              v-for="station in stationOptions"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="终点站" prop="endStationId">
          <el-select v-model="routeForm.endStationId" placeholder="请选择终点站" filterable>
            <el-option
              v-for="station in stationOptions"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="距离(公里)" prop="distance">
          <el-input-number v-model="routeForm.distance" :min="1" :max="10000" :precision="0" />
        </el-form-item>
        
        <el-divider>途经站点信息</el-divider>
        
        <el-form-item label="途经站点" prop="stations">
          <div class="station-list">
            <div 
              v-for="(station, index) in routeForm.stations" 
              :key="index" 
              class="station-item"
            >
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-select v-model="station.stationId" placeholder="选择站点" filterable>
                    <el-option
                      v-for="opt in stationOptions"
                      :key="opt.id"
                      :label="opt.name"
                      :value="opt.id"
                    />
                  </el-select>
                </el-col>
                <el-col :span="5">
                  <el-input-number v-model="station.order" :min="1" placeholder="序号" />
                </el-col>
                <el-col :span="5">
                  <el-input-number v-model="station.distance" :min="0" placeholder="距离" />
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" @click="removeStation(index)" icon="Delete" circle />
                </el-col>
              </el-row>
            </div>
          </div>
          <div class="add-station-btn">
            <el-button type="primary" @click="addStation" icon="Plus">添加站点</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="routeDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitRouteForm" :loading="routeDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 线路详情对话框 -->
    <el-dialog 
      v-model="routeDetailDialog.visible" 
      title="线路详情"
      width="700px"
    >
      <div v-if="routeDetail" class="route-detail">
        <h3>{{ routeDetail.name }}</h3>
        <div class="route-info">
          <p><strong>始发站:</strong> {{ routeDetail.startStation }}</p>
          <p><strong>终点站:</strong> {{ routeDetail.endStation }}</p>
          <p><strong>总距离:</strong> {{ routeDetail.distance }}公里</p>
          <p><strong>站点数量:</strong> {{ routeDetail.stationCount }}</p>
        </div>
        
        <el-divider>途经站点</el-divider>
        
        <el-table :data="routeDetail.stations" border stripe>
          <el-table-column prop="order" label="序号" width="80" />
          <el-table-column prop="stationName" label="站点名称" min-width="120" />
          <el-table-column prop="distance" label="距始发站距离(公里)" min-width="160" />
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

export default {
  name: 'RouteManage',
  setup() {
    // 线路列表数据
    const routeList = ref([])
    const loading = ref(false)
    
    // 车站选项
    const stationOptions = ref([])
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      startStation: '',
      endStation: ''
    })
    const searchFormRef = ref(null)
    
    // 线路表单对话框
    const routeDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false
    })
    
    // 线路表单
    const routeForm = reactive({
      id: null,
      name: '',
      startStationId: null,
      endStationId: null,
      distance: 0,
      stations: []
    })
    
    // 表单验证规则
    const routeRules = {
      name: [
        { required: true, message: '请输入线路名称', trigger: 'blur' },
        { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
      ],
      startStationId: [
        { required: true, message: '请选择始发站', trigger: 'change' }
      ],
      endStationId: [
        { required: true, message: '请选择终点站', trigger: 'change' }
      ],
      distance: [
        { required: true, message: '请输入距离', trigger: 'blur' }
      ]
    }
    const routeFormRef = ref(null)
    
    // 线路详情对话框
    const routeDetailDialog = reactive({
      visible: false
    })
    const routeDetail = ref(null)
    
    // 初始化数据
    onMounted(() => {
      fetchRouteList()
      fetchStationOptions()
    })
    
    // 获取所有车站选项
    const fetchStationOptions = async () => {
      try {
        const params = { page: 0, size: 1000 } // 获取所有车站
        const result = await adminAPI.station.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        stationOptions.value = processedData.content || []
      } catch (error) {
        console.error('获取车站数据失败:', error)
        ElMessage.error('获取车站数据失败')
      }
    }
    
    // 获取列车路线列表（train_route表）
    const fetchRouteList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          trainId: searchForm.trainId || null,
          stationId: searchForm.stationId || null
        }
        
        const result = await adminAPI.trainRoute.list(params)
        routeList.value = result.data?.content || []
        pagination.total = result.data?.totalElements || 0
      } catch (error) {
        ElMessage.error(error.message || '获取列车路线列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索线路
    const searchRoutes = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchRouteList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchRoutes()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchRouteList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchRouteList()
    }
    
    // 添加途经站点
    const addStation = () => {
      routeForm.stations.push({
        stationId: null,
        order: routeForm.stations.length + 1,
        distance: 0
      })
    }
    
    // 删除途经站点
    const removeStation = (index) => {
      routeForm.stations.splice(index, 1)
      // 重新排序
      routeForm.stations.forEach((station, idx) => {
        station.order = idx + 1
      })
    }
    
    // 打开添加线路对话框
    const handleAddRoute = () => {
      routeDialog.isEdit = false
      // 重置表单
      routeForm.id = null
      routeForm.name = ''
      routeForm.startStationId = null
      routeForm.endStationId = null
      routeForm.distance = 0
      routeForm.stations = []
      
      routeDialog.visible = true
    }
    
    // 打开编辑列车路线对话框
    const handleEditRoute = async (row) => {
      routeDialog.isEdit = true
      try {
        const routeData = await adminAPI.trainRoute.get(row.id)

        // 填充表单
        routeForm.id = routeData.data.id
        routeForm.trainId = routeData.data.trainId
        routeForm.stationId = routeData.data.stationId
        routeForm.stationOrder = routeData.data.stationOrder
        routeForm.arriveTime = routeData.data.arriveTime
        routeForm.departTime = routeData.data.departTime
        routeForm.stopTime = routeData.data.stopTime
        routeForm.distance = routeData.data.distance

        routeDialog.visible = true
      } catch (error) {
        ElMessage.error('获取路线详情失败')
      }
    }
    
    // 查看列车路线详情
    const handleViewRoute = async (row) => {
      try {
        const detail = await adminAPI.trainRoute.get(row.id)
        routeDetail.value = detail.data
        routeDetailDialog.visible = true
      } catch (error) {
        ElMessage.error('获取路线详情失败')
      }
    }
    
    // 提交列车路线表单
    const submitRouteForm = () => {
      routeFormRef.value.validate(async (valid) => {
        if (valid) {
          routeDialog.loading = true
          try {
            if (routeDialog.isEdit) {
              // 编辑路线
              await adminAPI.trainRoute.update(routeForm.id, routeForm)
              ElMessage.success('路线更新成功')
            } else {
              // 添加路线
              await adminAPI.trainRoute.add(routeForm)
              ElMessage.success('路线添加成功')
            }
            routeDialog.visible = false
            fetchRouteList() // 刷新列表
          } catch (error) {
            ElMessage.error(error.message || '操作失败')
          } finally {
            routeDialog.loading = false
          }
        }
      })
    }
    
    // 删除列车路线
    const handleDeleteRoute = (row) => {
      ElMessageBox.confirm(
        `确定要删除该路线吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.trainRoute.delete(row.id)
          ElMessage.success('路线删除成功')
          fetchRouteList() // 刷新列表
        } catch (error) {
          ElMessage.error(error.message || '删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    return {
      routeList,
      loading,
      stationOptions,
      pagination,
      searchForm,
      searchFormRef,
      routeDialog,
      routeForm,
      routeRules,
      routeFormRef,
      routeDetailDialog,
      routeDetail,
      fetchRouteList,
      searchRoutes,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      addStation,
      removeStation,
      handleAddRoute,
      handleEditRoute,
      handleViewRoute,
      submitRouteForm,
      handleDeleteRoute
    }
  }
}
</script>

<style scoped>
.route-manage {
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

.station-list {
  margin-bottom: 15px;
}

.station-item {
  margin-bottom: 10px;
}

.add-station-btn {
  margin-top: 10px;
}

.route-detail h3 {
  margin-bottom: 20px;
  text-align: center;
}

.route-info {
  margin-bottom: 20px;
}

.route-info p {
  margin-bottom: 10px;
}
</style>
