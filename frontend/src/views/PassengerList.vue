<template>
  <div class="passenger-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>乘车人管理</h2>
      <el-button type="primary" @click="addPassenger">
        <el-icon><plus /></el-icon>
        添加乘车人
      </el-button>
    </div>

    <!-- 乘车人列表 -->
    <div class="passenger-list" v-loading="loading">
      <div
        class="passenger-card"
        v-for="passenger in passengers"
        :key="passenger.id"
      >
        <div class="passenger-avatar">
          <el-avatar :size="56" class="avatar">
            {{ passenger.realName ? passenger.realName.charAt(0) : 'U' }}
          </el-avatar>
        </div>

        <div class="passenger-info">
          <div class="info-row name-row">
            <span class="passenger-name">{{ passenger.realName }}</span>
            <el-tag v-if="passenger.isDefault" type="success" size="small">常用</el-tag>
          </div>

          <div class="info-row">
            <div class="info-item">
              <el-icon class="info-icon"><postcard /></el-icon>
              <span class="info-label">{{ getIdTypeName(passenger.idType) }}</span>
              <span class="info-value">{{ passenger.idNumber }}</span>
            </div>
          </div>

          <div class="info-row">
            <div class="info-item">
              <el-icon class="info-icon"><phone /></el-icon>
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ passenger.phone || '未填写' }}</span>
            </div>
          </div>
        </div>

        <div class="passenger-actions">
          <el-button type="primary" size="small" @click="editPassenger(passenger)">
            <el-icon><edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="confirmDeletePassenger(passenger)">
            <el-icon><delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="passengers.length === 0 && !loading"
        description="暂无乘车人信息"
        :image-size="200"
      >
        <el-button type="primary" @click="addPassenger">添加乘车人</el-button>
      </el-empty>
    </div>

    <!-- 浮动添加按钮（移动端） -->
    <el-button
      type="primary"
      circle
      class="fab-button"
      @click="addPassenger"
    >
      <el-icon><plus /></el-icon>
    </el-button>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Postcard, Phone, Edit, Delete } from '@element-plus/icons-vue';
import { passengerAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default {
  name: 'PassengerList',
  components: {
    Plus,
    Postcard,
    Phone,
    Edit,
    Delete
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const passengers = ref([]);
    const loading = ref(false);
    
    const fetchPassengers = async () => {
      loading.value = true;
      try {
        const response = await passengerAPI.listPassengers();
        const extractedData = dataHelper.extractApiData(response);
        const passengerArray = dataHelper.ensureArray(extractedData);
        passengers.value = passengerArray.map(p => dataHelper.adaptPassengerData(p));
      } catch (error) {
        console.error('获取乘车人列表失败:', error);
        ElMessage.error('获取乘车人列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    const addPassenger = () => {
      router.push({
        path: '/passenger/edit',
        query: route.query
      });
    };
    
    const editPassenger = (passenger) => {
      router.push({
        path: `/passenger/edit/${passenger.id}`,
        query: route.query
      });
    };

    const confirmDeletePassenger = (passenger) => {
      ElMessageBox.confirm(`确定要删除乘车人"${passenger.realName}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePassenger(passenger.id);
      }).catch(() => {});
    };
    
    const deletePassenger = async (id) => {
      try {
        await passengerAPI.deletePassenger(id);
        ElMessage.success('删除成功');
        fetchPassengers();
      } catch (error) {
        console.error('删除乘车人失败:', error);
        ElMessage.error('删除乘车人失败');
      }
    };
    
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
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

/* 乘车人列表 */
.passenger-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

/* 乘车人卡片 */
.passenger-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.passenger-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.passenger-avatar {
  flex: 0 0 auto;
}

.avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 24px;
  font-weight: 600;
}

.passenger-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-row {
  margin-bottom: 4px;
}

.passenger-name {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #595959;
}

.info-icon {
  color: #1890ff;
  font-size: 16px;
}

.info-label {
  color: #8c8c8c;
}

.info-value {
  color: #262626;
}

.passenger-actions {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

/* 浮动按钮 */
.fab-button {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .passenger-list {
    grid-template-columns: 1fr;
  }

  .passenger-card {
    flex-direction: column;
  }

  .passenger-actions {
    flex-direction: row;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .fab-button {
    display: flex;
  }

  .page-header .el-button {
    display: none;
  }
}
</style>
