/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="train-search-container page-container">
    <h2 class="page-title">车票查询</h2>
    
    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="出发地" required>
          <StationSearch
            v-model="searchForm.startStation"
            placeholder="请输入出发站"
            @select="handleStartStationSelect"
          />
        </el-form-item>
        
        <!-- 添加交换按钮 -->
        <el-form-item class="exchange-button-item">
          <el-button 
            type="primary"
            circle
            size="small"
            @click="exchangeStations"
            title="交换出发地和目的地"
            class="exchange-btn"
          >
            <i class="el-icon-refresh"></i>
            ↔
          </el-button>
        </el-form-item>
        
        <el-form-item label="目的地" required>
          <StationSearch
            v-model="searchForm.endStation"
            placeholder="请输入到达站"
            @select="handleEndStationSelect"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 查询结果 -->
    <el-card class="result-card" v-if="trains.length > 0">
      <el-table :data="trains" stripe style="width: 100%" v-loading="loading">
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
        <el-table-column label="历时">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="票价" width="150">
          <template #default="scope">
            <div v-if="scope.row.seatInfo">
              <div v-for="(seat, index) in scope.row.seatInfo" :key="index">
                {{ seat.seatType }}: {{ formatPrice(seat.price) }}
              </div>
            </div>
            <div v-else>暂无价格信息</div>
          </template>
        </el-table-column>
        <el-table-column label="余票" width="150">
          <template #default="scope">
            <div v-if="scope.row.seatInfo">
              <div v-for="(seat, index) in scope.row.seatInfo" :key="index">
                {{ seat.seatType }}: {{ seat.remaining }}
              </div>
            </div>
            <div v-else>暂无余票信息</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button @click="selectTrain(scope.row)" type="primary" size="small">预订</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-empty v-else-if="searched && !loading" description="暂无符合条件的车次"></el-empty>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { trainAPI } from '@/api'; // 移除 stationAPI 引用
import dataHelper from '@/utils/dataHelper';
import StationSearch from '@/components/StationSearch.vue';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'TrainSearch',
  components: {
    StationSearch
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const trains = ref([]);
    const loading = ref(false);
    const searched = ref(false);
    
    const searchForm = reactive({
      startStation: route.query.startStation || '',
      endStation: route.query.endStation || '',
      startStationId: null,
      endStationId: null
    });
    
    // 查询车次
    const handleSearch = async () => {
      if (!searchForm.startStation || !searchForm.endStation) {
        ElMessage.warning('请输入出发地和目的地');
        return;
      }
      
      loading.value = true;
      searched.value = true;
      
      try {
        const response = await trainAPI.searchTrains(searchForm.startStation, searchForm.endStation);
        console.log('搜索列车响应:', response);
        const extractedData = dataHelper.extractApiData(response);
        const trainData = dataHelper.ensureArray(extractedData);
        
        // 检查数据结构，确保拥有所需属性，使用adaptTrainData处理每个列车数据
        trains.value = trainData.map(train => {
          return dataHelper.adaptTrainData(train);
        });
        
        console.log('处理后的搜索列车数据:', trains.value);
      } catch (error) {
        console.error('查询车次失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 重置表单
    const resetForm = () => {
      searchForm.startStation = '';
      searchForm.endStation = '';
      trains.value = [];
      searched.value = false;
    };
    
    // 选择车次进行预订
    const selectTrain = (train) => {
      const queryParams = {
        from: 'search',
        startStation: searchForm.startStation,
        endStation: searchForm.endStation
      };
      
      // 如果有站点ID，也传递给详情页
      if (searchForm.startStationId) {
        queryParams.startStationId = searchForm.startStationId;
      }
      
      if (searchForm.endStationId) {
        queryParams.endStationId = searchForm.endStationId;
      }
      
      router.push({
        path: `/train/${train.id}`,
        query: queryParams
      });
    };
    
    onMounted(() => {
      // 如果URL中有查询参数，自动执行查询
      if (searchForm.startStation && searchForm.endStation) {
        handleSearch();
      }
    });
    
    // 处理选择起始站
    const handleStartStationSelect = (station) => {
      if (station && station.id) {
        searchForm.startStationId = station.id;
        console.log('选择起始站:', station);
      }
    };
    
    // 处理选择终点站
    const handleEndStationSelect = (station) => {
      if (station && station.id) {
        searchForm.endStationId = station.id;
        console.log('选择终点站:', station);
      }
    };

    // 交换出发地和目的地
    const exchangeStations = () => {
      const tempStation = searchForm.startStation;
      searchForm.startStation = searchForm.endStation;
      searchForm.endStation = tempStation;

      const tempStationId = searchForm.startStationId;
      searchForm.startStationId = searchForm.endStationId;
      searchForm.endStationId = tempStationId;
    };
    
    return {
      trains,
      loading,
      searched,
      searchForm,
      handleSearch,
      resetForm,
      selectTrain,
      handleStartStationSelect,
      handleEndStationSelect,
      exchangeStations,
      formatStation,
      formatPrice,
      formatDuration
    };
  }
}
</script>

<style scoped>
.train-search-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.result-card {
  margin-top: 20px;
}

.exchange-button-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.exchange-btn {
  margin: 0 10px;
}
</style>
