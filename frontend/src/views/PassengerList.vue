<template>
  <div class="passenger-list-container page-container">
    <h2 class="page-title">乘车人管理</h2>
    
    <div class="action-bar">
      <el-button type="primary" @click="addPassenger">添加乘车人</el-button>
    </div>
    
    <el-card v-loading="loading">
      <el-table :data="passengers" style="width: 100%">
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="idType" label="证件类型">
          <template #default="scope">
            {{ getIdTypeName(scope.row.idType) }}
          </template>
        </el-table-column>
        <el-table-column prop="idNumber" label="证件号码" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button @click="editPassenger(scope.row)" size="small" type="primary">编辑</el-button>
            <el-button @click="confirmDeletePassenger(scope.row)" size="small" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="passengers.length === 0 && !loading" class="empty-tip">
        暂无乘车人，请点击添加乘车人按钮添加
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { passengerAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default {
  name: 'PassengerList',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const passengers = ref([]);
    const loading = ref(false);
    
    // 获取乘车人列表
    const fetchPassengers = async () => {
      loading.value = true;
      try {
        const response = await passengerAPI.listPassengers();
        console.log('获取到的乘车人列表响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        // 使用适配器处理乘车人数据，确保idType和idNumber字段正确
        const passengerArray = dataHelper.ensureArray(extractedData);
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p));
        console.log('处理后的乘车人数据:', passengers.value);
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
        ElMessage.error('获取乘车人列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 添加乘车人
    const addPassenger = () => {
      router.push({
        path: '/passenger/edit',
        query: route.query // 保留重定向参数
      });
    };
    
    // 编辑乘车人
    const editPassenger = (passenger) => {
      router.push({
        path: `/passenger/edit/${passenger.id}`,
        query: route.query // 保留重定向参数
      });
    };
    
    // 确认删除乘车人
    const confirmDeletePassenger = (passenger) => {
      ElMessageBox.confirm(`确定要删除乘车人"${passenger.realName}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePassenger(passenger.id);
      }).catch(() => {});
    };
    
    // 删除乘车人
    const deletePassenger = async (id) => {
      try {
        await passengerAPI.deletePassenger(id);
        ElMessage.success('删除成功');
        fetchPassengers(); // 刷新列表
      } catch (error) {
        console.error('删除乘车人失败:', error);
        ElMessage.error('删除乘车人失败');
      }
    };
    
    // 获取证件类型名称
    const getIdTypeName = (idType) => {
      const idTypeMap = {
        '1': '身份证',
        '2': '护照',
        '3': '军官证',
        '4': '港澳通行证'
      };
      return idTypeMap[idType] || '未知';
    };
    
    onMounted(() => {
      fetchPassengers();
    });
    
    return {
      passengers,
      loading,
      addPassenger,
      editPassenger,
      confirmDeletePassenger,
      getIdTypeName
    };
  }
}
</script>

<style scoped>
.passenger-list-container {
  padding: 20px;
}

.action-bar {
  margin-bottom: 20px;
}

.empty-tip {
  padding: 30px 0;
  text-align: center;
  color: #909399;
}
</style>
