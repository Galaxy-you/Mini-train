<template>
  <div class="station-search-container">
    <el-autocomplete
      v-model="searchText"
      :fetch-suggestions="queryStations"
      :trigger-on-focus="false"
      clearable
      :placeholder="placeholder"
      @select="handleSelect"
      class="station-autocomplete"
    >
      <template #default="{ item }">
        <div class="station-suggestion">
          <div class="station-name">{{ item.name }}</div>
          <div class="station-info">
            <span>{{ item.city }}</span>
            <span v-if="item.province && item.province !== item.city"> · {{ item.province }}</span>
            <span v-if="item.type"> · {{ item.type }}</span>
          </div>
        </div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script>
import { ref, watch, defineComponent } from 'vue';
import { ElMessage } from 'element-plus';
import { stationAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default defineComponent({
  name: 'StationSearch',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请输入站点名称'
    }
  },
  emits: ['update:modelValue', 'select'],
  setup(props, { emit }) {
    const searchText = ref(props.modelValue);
    const stations = ref([]);
    const loading = ref(false);
    
    // 监听外部modelValue变化
    watch(() => props.modelValue, (newValue) => {
      searchText.value = newValue;
    });
    
    // 监听内部searchText变化
    watch(searchText, (newValue) => {
      emit('update:modelValue', newValue);
    });
    
    // 查询站点
    const queryStations = async (queryString, callback) => {
      if (!queryString || queryString.length < 1) {
        callback([]);
        return;
      }
      
      loading.value = true;
      try {
        // 先按名称搜索
        const response = await stationAPI.searchStations({ name: queryString });
        const extractedData = dataHelper.extractApiData(response);
        const stationList = dataHelper.ensureArray(extractedData);
        
        // 如果名称搜索结果少于5个，再按城市搜索补充
        if (stationList.length < 5) {
          const cityResponse = await stationAPI.searchStations({ city: queryString });
          const cityData = dataHelper.extractApiData(cityResponse);
          const cityStations = dataHelper.ensureArray(cityData);
          
          // 合并并去重
          const allStations = [...stationList];
          cityStations.forEach(station => {
            if (!allStations.some(s => s.id === station.id)) {
              allStations.push(station);
            }
          });
          
          stations.value = allStations.slice(0, 10);
        } else {
          stations.value = stationList.slice(0, 10);
        }
        
        callback(stations.value);
      } catch (error) {
        console.error('查询站点失败:', error);
        ElMessage.error('查询站点失败');
        callback([]);
      } finally {
        loading.value = false;
      }
    };
    
    // 选择站点
    const handleSelect = (item) => {
      searchText.value = item.name;
      emit('select', item);
    };
    
    return {
      searchText,
      loading,
      queryStations,
      handleSelect
    };
  }
});
</script>

<style scoped>
.station-search-container {
  display: flex;
  align-items: center;
}

.station-autocomplete {
  width: 100%;
}

.station-suggestion {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
}

.station-name {
  font-weight: bold;
}

.station-info {
  font-size: 12px;
  color: #909399;
}
</style>
