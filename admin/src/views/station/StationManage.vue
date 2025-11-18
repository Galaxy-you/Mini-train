<template>
  <div class="station-manage">
    <div class="page-header">
      <h2>车站管理</h2>
      <el-button type="primary" @click="handleAddStation">添加车站</el-button>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="车站名称">
          <el-input v-model="searchForm.name" placeholder="请输入车站名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="请输入城市" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchStations">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="stationList" 
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="车站名称" min-width="120" />
      <el-table-column prop="code" label="车站代码" min-width="100" />
      <el-table-column prop="city" label="所在城市" min-width="120" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEditStation(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteStation(scope.row)">删除</el-button>
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
    
    <!-- 车站表单对话框 -->
    <el-dialog 
      v-model="stationDialog.visible" 
      :title="stationDialog.isEdit ? '编辑车站' : '添加车站'"
      width="500px"
    >
      <el-form 
        :model="stationForm" 
        :rules="stationRules"
        ref="stationFormRef"
        label-width="100px"
      >
        <el-form-item label="车站名称" prop="name">
          <el-input v-model="stationForm.name" placeholder="请输入车站名称" />
        </el-form-item>
        <el-form-item label="车站代码" prop="code">
          <el-input v-model="stationForm.code" placeholder="请输入车站代码" />
        </el-form-item>
        <el-form-item label="所在城市" prop="city">
          <el-input v-model="stationForm.city" placeholder="请输入所在城市" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stationDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitStationForm" :loading="stationDialog.loading">
            确定
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
import { dataHelper } from '@/utils/dataHelper'

export default {
  name: 'StationManage',
  setup() {
    // 车站列表数据
    const stationList = ref([])
    const loading = ref(false)
    
    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      city: ''
    })
    const searchFormRef = ref(null)
    
    // 车站表单对话框
    const stationDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false
    })
    
    // 车站表单
    const stationForm = reactive({
      id: null,
      name: '',
      code: '',
      city: ''
    })
    
    // 表单验证规则
    const stationRules = {
      name: [
        { required: true, message: '请输入车站名称', trigger: 'blur' },
        { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
      ],
      code: [
        { required: true, message: '请输入车站代码', trigger: 'blur' },
        { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
      ],
      city: [
        { required: true, message: '请输入所在城市', trigger: 'blur' },
        { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
      ]
    }
    const stationFormRef = ref(null)
    
    // 初始化数据
    onMounted(() => {
      fetchStationList()
    })
    
    // 获取车站列表
    const fetchStationList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.currentPage - 1, // 后端从0开始计数
          size: pagination.pageSize,
          name: searchForm.name || null,
          city: searchForm.city || null
        }
        
        const result = await adminAPI.station.list(params)
        const processedData = dataHelper.handlePaginationData(result)
        stationList.value = processedData.content
        pagination.total = processedData.totalElements
      } catch (error) {
        ElMessage.error(error.message || '获取车站列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索车站
    const searchStations = () => {
      pagination.currentPage = 1 // 重置到第一页
      fetchStationList()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchFormRef.value.resetFields()
      searchStations()
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchStationList()
    }
    
    // 处理当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      fetchStationList()
    }
    
    // 打开添加车站对话框
    const handleAddStation = () => {
      stationDialog.isEdit = false
      // 重置表单
      stationForm.id = null
      stationForm.name = ''
      stationForm.code = ''
      stationForm.city = ''
      
      stationDialog.visible = true
    }
    
    // 打开编辑车站对话框
    const handleEditStation = (row) => {
      stationDialog.isEdit = true
      // 填充表单
      stationForm.id = row.id
      stationForm.name = row.name
      stationForm.code = row.code
      stationForm.city = row.city
      
      stationDialog.visible = true
    }
    
    // 提交车站表单
    const submitStationForm = () => {
      stationFormRef.value.validate(async (valid) => {
        if (valid) {
          stationDialog.loading = true
          try {
            if (stationDialog.isEdit) {
              // 编辑车站
              await adminAPI.station.update(stationForm.id, stationForm)
              ElMessage.success('车站更新成功')
            } else {
              // 添加车站
              await adminAPI.station.add(stationForm)
              ElMessage.success('车站添加成功')
            }
            stationDialog.visible = false
            fetchStationList() // 刷新列表
          } catch (error) {
            ElMessage.error(error.message || '操作失败')
          } finally {
            stationDialog.loading = false
          }
        }
      })
    }
    
    // 删除车站
    const handleDeleteStation = (row) => {
      ElMessageBox.confirm(
        `确定要删除车站"${row.name}"吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await adminAPI.station.delete(row.id)
          ElMessage.success('车站删除成功')
          fetchStationList() // 刷新列表
        } catch (error) {
          ElMessage.error(error.message || '删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    return {
      stationList,
      loading,
      pagination,
      searchForm,
      searchFormRef,
      stationDialog,
      stationForm,
      stationRules,
      stationFormRef,
      fetchStationList,
      searchStations,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleAddStation,
      handleEditStation,
      submitStationForm,
      handleDeleteStation
    }
  }
}
</script>

<style scoped>
.station-manage {
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
