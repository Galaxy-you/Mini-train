/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h1>欢迎使用 Mini12306 系统</h1>
        <p>快速便捷的铁路购票平台</p>

        <div class="quick-search">
          <div class="search-title">快速查询</div>
          <el-form :model="searchForm" label-width="100px" class="search-form">
            <el-form-item label="出发地">
              <el-input v-model="searchForm.startStation" placeholder="请输入出发地" />
            </el-form-item>
            <el-form-item label="目的地">
              <el-input v-model="searchForm.endStation" placeholder="请输入目的地" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="shortcuts">
          <h3>快捷入口</h3>
          <div class="shortcut-cards">
            <div class="shortcut-card" @click="navigateTo('/train/search')">
              <el-icon><search /></el-icon>
              <span>车票查询</span>
            </div>
            <div class="shortcut-card" @click="navigateTo('/order')">
              <el-icon><tickets /></el-icon>
              <span>订单管理</span>
            </div>
            <div class="shortcut-card" @click="navigateTo('/passenger')">
              <el-icon><user /></el-icon>
              <span>乘车人管理</span>
            </div>
            <div class="shortcut-card" @click="navigateTo('/ticket')">
              <el-icon><ticket /></el-icon>
              <span>车票管理</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="recent-trains-card">
      <template #header>
        <div class="card-header">
          <span>热门车次</span>
        </div>
      </template>
      <div v-loading="loading">
        <el-table :data="trains" stripe style="width: 100%">
          <el-table-column prop="trainNumber" label="车次" />
          <el-table-column label="出发站">
            <template #default="scope">
              {{ formatStation(scope.row.startStation) }}
            </template>
          </el-table-column>
          <el-table-column label="终点站">
            <template #default="scope">
              {{ formatStation(scope.row.endStation) }}
            </template>
          </el-table-column>
          <el-table-column prop="departureTime" label="发车时间" />
          <el-table-column prop="arrivalTime" label="到达时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button @click="viewTrain(scope.row)" type="primary" size="small">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { trainAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'Home',
  setup() {
    const router = useRouter();
    const trains = ref([]);
    const loading = ref(false);
    
    const searchForm = reactive({
      startStation: '',
      endStation: ''
    });

    // 获取所有列车
    const fetchTrains = async () => {
      loading.value = true;
      try {
        const response = await trainAPI.listAllTrains();
        console.log('获取到的列车数据:', response);
        // 使用dataHelper确保返回的是数组类型
        const extractedData = dataHelper.extractApiData(response);
        trains.value = dataHelper.ensureArray(extractedData);
        console.log('处理后的列车数据:', trains.value);
      } catch (error) {
        console.error('获取列车数据失败:', error);
      } finally {
        loading.value = false;
      }
    };

    // 查询按钮处理
    const handleSearch = () => {
      if (searchForm.startStation && searchForm.endStation) {
        router.push({
          path: '/train/search',
          query: {
            startStation: searchForm.startStation,
            endStation: searchForm.endStation
          }
        });
      }
    };

    // 查看车次详情
    const viewTrain = (train) => {
      router.push(`/train/${train.id}`);
    };

    // 导航到指定路径
    const navigateTo = (path) => {
      router.push(path);
    };

    onMounted(() => {
      fetchTrains();
    });

    return {
      trains,
      loading,
      searchForm,
      handleSearch,
      viewTrain,
      navigateTo,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  text-align: center;
}

.welcome-content h1 {
  color: #409EFF;
  margin-bottom: 10px;
}

.welcome-content p {
  color: #666;
  margin-bottom: 30px;
}

.quick-search {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 30px;
}

.search-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: left;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.search-form .el-form-item {
  flex: 1;
  min-width: 200px;
  margin-right: 10px;
}

.shortcuts {
  margin-top: 30px;
}

.shortcut-cards {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
}

.shortcut-card {
  width: 150px;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.shortcut-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-5px);
}

.shortcut-card .el-icon {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 10px;
}

.card-header {
  font-weight: bold;
}
</style>
