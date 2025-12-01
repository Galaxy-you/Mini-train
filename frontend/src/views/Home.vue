/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="welcome-title">欢迎使用 Mini12306</h1>
        <p class="welcome-subtitle">智能便捷的铁路购票服务平台</p>
      </div>
    </div>

    <!-- 快速搜索区 -->
    <el-card class="search-card" shadow="hover">
      <div class="search-header">
        <el-icon class="search-icon"><search /></el-icon>
        <span>快速查询车票</span>
      </div>

      <el-form :model="searchForm" class="search-form">
        <div class="search-inputs">
          <el-form-item label="出发地" class="search-item">
            <station-search
              v-model="searchForm.startStation"
              placeholder="请输入出发站"
            />
          </el-form-item>

          <div class="exchange-icon" @click="exchangeStations">
            <el-icon><sort /></el-icon>
          </div>

          <el-form-item label="目的地" class="search-item">
            <station-search
              v-model="searchForm.endStation"
              placeholder="请输入到达站"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="search-button"
            @click="handleSearch"
          >
            查询车票
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 快捷功能 -->
    <div class="shortcuts-section">
      <h2 class="section-title">快捷功能</h2>
      <div class="shortcuts-grid">
        <div class="shortcut-card" @click="navigateTo('/train/search')">
          <div class="shortcut-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><search /></el-icon>
          </div>
          <div class="shortcut-text">
            <div class="shortcut-title">车票查询</div>
            <div class="shortcut-desc">查询列车时刻</div>
          </div>
        </div>

        <div class="shortcut-card" @click="navigateTo('/order')">
          <div class="shortcut-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><tickets /></el-icon>
          </div>
          <div class="shortcut-text">
            <div class="shortcut-title">订单管理</div>
            <div class="shortcut-desc">查看订单记录</div>
          </div>
        </div>

        <div class="shortcut-card" @click="navigateTo('/passenger')">
          <div class="shortcut-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><user /></el-icon>
          </div>
          <div class="shortcut-text">
            <div class="shortcut-title">乘车人</div>
            <div class="shortcut-desc">管理常用联系人</div>
          </div>
        </div>

        <div class="shortcut-card" @click="navigateTo('/ticket')">
          <div class="shortcut-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon><ticket /></el-icon>
          </div>
          <div class="shortcut-text">
            <div class="shortcut-title">车票管理</div>
            <div class="shortcut-desc">查看已购车票</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 信息展示区 -->
    <div class="info-section">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><trip-filled /></el-icon>
                <span>热门线路</span>
              </div>
            </template>
            <div class="popular-routes">
              <div class="route-item" v-for="(route, index) in popularRoutes" :key="index">
                <div class="route-info">
                  <span class="route-name">{{ route.start }} - {{ route.end }}</span>
                  <el-tag size="small" type="info">{{ route.type }}</el-tag>
                </div>
                <span class="route-price">￥{{ route.price }}</span>
              </div>
              <el-empty v-if="popularRoutes.length === 0" description="暂无数据" :image-size="80" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><bell /></el-icon>
                <span>系统公告</span>
              </div>
            </template>
            <div class="notices">
              <div class="notice-item" v-for="(notice, index) in notices" :key="index">
                <el-icon class="notice-icon"><info-filled /></el-icon>
                <span class="notice-text">{{ notice }}</span>
              </div>
              <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="80" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Sort, User, Tickets, Ticket, TripFilled, Bell, InfoFilled } from '@element-plus/icons-vue';
import StationSearch from '@/components/StationSearch.vue';

export default {
  name: 'Home',
  components: {
    Search,
    Sort,
    User,
    Tickets,
    Ticket,
    TripFilled,
    Bell,
    InfoFilled,
    StationSearch
  },
  setup() {
    const router = useRouter();

    const searchForm = reactive({
      startStation: '',
      endStation: ''
    });

    const popularRoutes = ref([
      { start: '北京', end: '上海', type: '高铁', price: '553' },
      { start: '北京', end: '广州', type: '高铁', price: '862' },
      { start: '上海', end: '广州', type: '高铁', price: '794' },
      { start: '北京', end: '哈尔滨', type: '高铁', price: '635' }
    ]);

    const notices = ref([
      '欢迎使用Mini12306购票系统',
      '请提前30分钟到达车站候车',
      '购票成功后请及时查看车票信息'
    ]);

    const exchangeStations = () => {
      const temp = searchForm.startStation;
      searchForm.startStation = searchForm.endStation;
      searchForm.endStation = temp;
    };

    const handleSearch = () => {
      if (searchForm.startStation && searchForm.endStation) {
        router.push({
          path: '/train/search',
          query: {
            startStation: searchForm.startStation,
            endStation: searchForm.endStation
          }
        });
      } else {
        router.push('/train/search');
      }
    };

    const navigateTo = (path) => {
      router.push(path);
    };

    return {
      searchForm,
      popularRoutes,
      notices,
      exchangeStations,
      handleSearch,
      navigateTo
    };
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 48px;
  margin-bottom: 24px;
  color: #fff;
  text-align: center;
}

.welcome-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 20px;
}

.search-icon {
  font-size: 20px;
  color: #1890ff;
}

.search-form {
  margin: 0;
}

.search-inputs {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.search-item {
  flex: 1;
  margin-bottom: 0;
}

.search-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #595959;
}

.search-item :deep(.el-autocomplete) {
  width: 100%;
}

.search-item :deep(.el-input__wrapper) {
  height: 40px;
}

.exchange-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 2px;
}

.exchange-icon:hover {
  background: #1890ff;
  color: #fff;
}

.exchange-icon .el-icon {
  font-size: 20px;
}

.search-button {
  height: 40px;
  padding: 0 32px;
  margin-bottom: 2px;
}

/* 快捷功能 */
.shortcuts-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.shortcut-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.shortcut-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.shortcut-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.shortcut-icon .el-icon {
  font-size: 28px;
}

.shortcut-text {
  text-align: center;
}

.shortcut-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.shortcut-desc {
  font-size: 12px;
  color: #8c8c8c;
}

/* 信息展示区 */
.info-section {
  margin-bottom: 24px;
}

.info-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.card-header .el-icon {
  color: #1890ff;
}

.popular-routes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  transition: all 0.3s;
}

.route-item:hover {
  background: #f0f0f0;
}

.route-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-name {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.route-price {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
}

.notices {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.notice-icon {
  color: #1890ff;
  margin-top: 2px;
}

.notice-text {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .shortcuts-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .search-inputs {
    flex-direction: column;
  }

  .exchange-icon {
    transform: rotate(90deg);
  }
}
</style>
