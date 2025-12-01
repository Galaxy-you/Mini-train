/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="train-search-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" class="search-form">
        <div class="search-row">
          <div class="search-item">
            <label class="search-label">出发地</label>
            <StationSearch
              v-model="searchForm.startStation"
              placeholder="请输入出发站"
              size="large"
              @select="handleStartStationSelect"
            />
          </div>

          <div class="exchange-wrapper">
            <el-button
              circle
              @click="exchangeStations"
              class="exchange-btn"
            >
              <el-icon><sort /></el-icon>
            </el-button>
          </div>

          <div class="search-item">
            <label class="search-label">目的地</label>
            <StationSearch
              v-model="searchForm.endStation"
              placeholder="请输入到达站"
              size="large"
              @select="handleEndStationSelect"
            />
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="search-button"
            @click="handleSearch"
          >
            <el-icon><search /></el-icon>
            <span>查询</span>
          </el-button>
        </div>
      </el-form>
    </el-card>
    
    <!-- 查询结果 -->
    <div class="result-section" v-if="trains.length > 0">
      <div class="result-header">
        <span class="result-count">找到 <strong>{{ trains.length }}</strong> 个车次</span>
      </div>

      <div class="train-list">
        <div
          class="train-card"
          v-for="train in trains"
          :key="train.id"
          @click="selectTrain(train)"
        >
          <div class="train-main">
            <div class="train-number">
              <span class="number">{{ train.trainNumber }}</span>
              <el-tag :type="getTrainTypeTag(train.type)" size="small">{{ train.type }}</el-tag>
            </div>

            <div class="train-route">
              <div class="station-time">
                <div class="time">{{ train.departureTime }}</div>
                <div class="station">{{ formatStation(train.startStation) }}</div>
              </div>

              <div class="duration-line">
                <div class="duration">{{ formatDuration(train.duration) }}</div>
                <div class="line">
                  <div class="line-bar"></div>
                  <el-icon class="arrow"><right /></el-icon>
                </div>
              </div>

              <div class="station-time">
                <div class="time">{{ train.arrivalTime }}</div>
                <div class="station">{{ formatStation(train.endStation) }}</div>
              </div>
            </div>
          </div>

          <div class="train-seats">
            <div class="seat-item" v-for="(seat, index) in train.seatInfo" :key="index">
              <div class="seat-type">{{ seat.seatType }}</div>
              <div class="seat-price">{{ formatPrice(seat.price) }}</div>
              <div class="seat-count" :class="{ 'low-stock': seat.remaining < 10 }">
                {{ seat.remaining > 0 ? `余${seat.remaining}张` : '无票' }}
              </div>
            </div>
            <div class="no-seat" v-if="!train.seatInfo || train.seatInfo.length === 0">
              暂无座位信息
            </div>
          </div>

          <div class="train-action">
            <el-button type="primary" size="large" @click.stop="selectTrain(train)">
              预订
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="searched && !loading"
      description="未找到符合条件的车次"
      :image-size="200"
    >
      <el-button type="primary" @click="resetForm">重新搜索</el-button>
    </el-empty>

    <!-- 初始提示 -->
    <div class="initial-prompt" v-else-if="!searched && !loading">
      <el-icon class="prompt-icon"><search /></el-icon>
      <p>请输入出发地和目的地查询车次</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Sort, Right } from '@element-plus/icons-vue';
import { trainAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';
import StationSearch from '@/components/StationSearch.vue';
import { formatStation, formatPrice, formatDuration } from '@/utils/formatters';

export default {
  name: 'TrainSearch',
  components: {
    StationSearch,
    Search,
    Sort,
    Right
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

    const getTrainTypeTag = (type) => {
      const typeMap = {
        '高铁': 'danger',
        '动车': 'warning',
        '特快': 'success',
        '快速': 'info',
        '普通': ''
      };
      return typeMap[type] || 'info';
    };

    const handleSearch = async () => {
      if (!searchForm.startStation || !searchForm.endStation) {
        ElMessage.warning('请输入出发地和目的地');
        return;
      }
      
      loading.value = true;
      searched.value = true;
      
      try {
        const response = await trainAPI.searchTrains(searchForm.startStation, searchForm.endStation);
        const extractedData = dataHelper.extractApiData(response);
        const trainData = dataHelper.ensureArray(extractedData);
        
        trains.value = trainData.map(train => dataHelper.adaptTrainData(train));
      } catch (error) {
        console.error('查询车次失败:', error);
        ElMessage.error('查询车次失败');
      } finally {
        loading.value = false;
      }
    };
    
    const resetForm = () => {
      searchForm.startStation = '';
      searchForm.endStation = '';
      trains.value = [];
      searched.value = false;
    };
    
    const exchangeStations = () => {
      const temp = searchForm.startStation;
      searchForm.startStation = searchForm.endStation;
      searchForm.endStation = temp;
    };

    const selectTrain = (train) => {
      const queryParams = {
        from: 'search',
        startStation: searchForm.startStation,
        endStation: searchForm.endStation
      };
      
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
    
    const handleStartStationSelect = (station) => {
      if (station && station.id) {
        searchForm.startStationId = station.id;
      }
    };
    
    const handleEndStationSelect = (station) => {
      if (station && station.id) {
        searchForm.endStationId = station.id;
      }
    };

    onMounted(() => {
      if (searchForm.startStation && searchForm.endStation) {
        handleSearch();
      }
    });

    return {
      trains,
      loading,
      searched,
      searchForm,
      getTrainTypeTag,
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
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.search-form {
  margin: 0;
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.search-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-label {
  font-size: 14px;
  font-weight: 500;
  color: #595959;
}

.exchange-wrapper {
  padding-bottom: 2px;
}

.exchange-btn {
  width: 40px;
  height: 40px;
  background: #f5f5f5;
  border: none;
  color: #595959;
  transition: all 0.3s;
}

.exchange-btn:hover {
  background: #1890ff;
  color: #fff;
}

.search-button {
  height: 40px;
  padding: 0 32px;
}

/* 结果区域 */
.result-section {
  margin-top: 24px;
}

.result-header {
  margin-bottom: 16px;
  font-size: 14px;
  color: #595959;
}

.result-count strong {
  color: #1890ff;
  font-size: 18px;
}

/* 列车卡片 */
.train-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.train-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.train-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.train-main {
  flex: 0 0 auto;
  display: flex;
  gap: 32px;
}

.train-number {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 80px;
}

.train-number .number {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.train-route {
  display: flex;
  align-items: center;
  gap: 24px;
}

.station-time {
  text-align: center;
}

.station-time .time {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.station-time .station {
  font-size: 14px;
  color: #595959;
}

.duration-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 120px;
}

.duration {
  font-size: 12px;
  color: #8c8c8c;
}

.line {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
}

.line-bar {
  flex: 1;
  height: 2px;
  background: #e8e8e8;
}

.arrow {
  color: #1890ff;
  font-size: 16px;
}

/* 座位信息 */
.train-seats {
  flex: 1;
  display: flex;
  gap: 16px;
  padding-left: 24px;
  border-left: 1px solid #e8e8e8;
}

.seat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 90px;
}

.seat-type {
  font-size: 12px;
  color: #8c8c8c;
}

.seat-price {
  font-size: 20px;
  font-weight: 600;
  color: #ff4d4f;
}

.seat-count {
  font-size: 12px;
  color: #52c41a;
}

.seat-count.low-stock {
  color: #faad14;
}

.no-seat {
  font-size: 14px;
  color: #8c8c8c;
}

/* 操作按钮 */
.train-action {
  flex: 0 0 auto;
}

/* 初始提示 */
.initial-prompt {
  text-align: center;
  padding: 80px 0;
  color: #8c8c8c;
}

.prompt-icon {
  font-size: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.initial-prompt p {
  font-size: 16px;
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-row {
    flex-direction: column;
  }

  .exchange-wrapper {
    order: 2;
  }

  .train-card {
    flex-direction: column;
    align-items: stretch;
  }

  .train-seats {
    border-left: none;
    border-top: 1px solid #e8e8e8;
    padding-left: 0;
    padding-top: 16px;
    flex-wrap: wrap;
  }
}
</style>
