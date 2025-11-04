/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="user-center-container page-container">
    <h2 class="page-title">个人中心</h2>
    
    <el-card class="user-info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
        </div>
      </template>
      
      <div class="user-info-content" v-if="userInfo">
        <div class="user-avatar">
          <el-avatar :size="100" :src="avatarUrl">{{ userInfo.username?.charAt(0) || 'U' }}</el-avatar>
        </div>
        
        <el-descriptions class="user-details" :column="1" border>
          <el-descriptions-item label="用户名">{{ userInfo.username || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ userInfo.realName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ formatIdNumber(userInfo.cardId) || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(userInfo.createTime) || '未知' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="auth-status">
          <el-tag 
            :type="userInfo.authenticated ? 'success' : 'warning'" 
            class="auth-tag"
          >
            {{ userInfo.authenticated ? '已实名认证' : '未实名认证' }}
          </el-tag>
          
          <el-button 
            v-if="!userInfo.authenticated" 
            type="primary" 
            size="small"
            @click="openAuthDialog"
          >
            实名认证
          </el-button>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="获取用户信息失败，请重试"></el-empty>
    </el-card>
    
    <!-- <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>使用统计</span>
        </div>
      </template>
      
      <div class="stats-content">
        <div class="stat-item">
          <div class="stat-icon">
            <el-icon><location /></el-icon>
          </div>
          <div class="stat-value">{{ stats.orderCount || 0 }}</div>
          <div class="stat-label">总订单</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">
            <el-icon><ticket /></el-icon>
          </div>
          <div class="stat-value">{{ stats.ticketCount || 0 }}</div>
          <div class="stat-label">总车票</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">
            <el-icon><user /></el-icon>
          </div>
          <div class="stat-value">{{ stats.passengerCount || 0 }}</div>
          <div class="stat-label">乘车人</div>
        </div>
      </div>
    </el-card> -->
    
    <!-- 实名认证对话框 -->
    <el-dialog
      v-model="authDialogVisible"
      title="实名认证"
      width="500px"
    >
      <el-form
        ref="authFormRef"
        :model="authForm"
        :rules="authRules"
        label-width="100px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="authForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="authForm.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="authDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAuth" :loading="authSubmitting">提交认证</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { authAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default {
  name: 'UserCenter',
  setup() {
    const loading = ref(false);
    const userInfo = ref(null);
    const stats = ref({
      orderCount: 0,
      ticketCount: 0,
      passengerCount: 0
    });
    const avatarUrl = ref('');
    
    // 实名认证相关
    const authDialogVisible = ref(false);
    const authSubmitting = ref(false);
    const authFormRef = ref(null);
    
    const authForm = reactive({
      realName: '',
      idNumber: ''
    });
    
    // 验证身份证号
    const validateIdNumber = (rule, value, callback) => {
      const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
      if (!reg.test(value)) {
        callback(new Error('请输入正确的身份证号'));
      } else {
        callback();
      }
    };
    
    const authRules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      idNumber: [
        { required: true, message: '请输入身份证号', trigger: 'blur' },
        { validator: validateIdNumber, trigger: 'blur' }
      ]
    };
    
    // 获取用户信息
    const getUserInfo = async () => {
      loading.value = true;
      try {
        // 获取API响应
        const response = await authAPI.getUserInfo();
        console.log('获取到的用户信息响应:', response);
        
        // 使用dataHelper安全地提取数据
        const extractedData = dataHelper.extractApiData(response);
        const userData = dataHelper.ensureObject(extractedData);
        
        console.log('处理后的用户数据:', userData);
        userInfo.value = userData;
        
        // 检查用户数据是否正确获取
        if (!userData || !userData.username) {
          console.error('用户数据格式错误或为空:', userData);
          ElMessage.warning('用户信息格式不正确，请联系管理员');
        }
        
        // 处理认证状态，从authStatus转为更友好的布尔值
        if (userInfo.value) {
          userInfo.value.authenticated = userInfo.value.authStatus === 1;
        }
        
        // 模拟使用统计数据
        stats.value = {
          orderCount: Math.floor(Math.random() * 10),
          ticketCount: Math.floor(Math.random() * 20),
          passengerCount: Math.floor(Math.random() * 5)
        };
        
        // 使用用户名首字母和颜色生成头像
        const firstChar = userData.username?.charAt(0)?.toUpperCase() || 'U';
        avatarUrl.value = `https://ui-avatars.com/api/?name=${firstChar}&background=random&color=fff&size=100`;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        ElMessage.error('获取用户信息失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 打开实名认证对话框
    const openAuthDialog = () => {
      authForm.realName = userInfo.value?.realName || '';
      authForm.idNumber = userInfo.value?.idNumber || '';
      authDialogVisible.value = true;
    };
    
    // 提交实名认证
    const submitAuth = () => {
      authFormRef.value.validate(async (valid) => {
        if (valid) {
          authSubmitting.value = true;
          try {
            // 转换字段名以匹配后端API期望的格式
            const authData = {
              realName: authForm.realName,
              cardId: authForm.idNumber // 后端API使用cardId字段
            };
            
            const response = await authAPI.authenticate(authData);
            console.log('实名认证响应:', response);
            
            if (response && response.success) {
              ElMessage.success('实名认证成功');
              authDialogVisible.value = false;
              getUserInfo(); // 刷新用户信息
            } else {
              throw new Error(response?.message || '认证失败');
            }
          } catch (error) {
            console.error('实名认证失败:', error);
            ElMessage.error('实名认证失败: ' + (error.message || '未知错误'));
          } finally {
            authSubmitting.value = false;
          }
        }
      });
    };
    
    // 格式化身份证号，只显示前4位和后4位
    const formatIdNumber = (idNumber) => {
      if (!idNumber) return '';
      if (idNumber.length <= 8) return idNumber;
      return idNumber.substring(0, 4) + '**********' + idNumber.substring(idNumber.length - 4);
    };
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN');
    };
    
    onMounted(() => {
      getUserInfo();
    });
    
    return {
      loading,
      userInfo,
      stats,
      avatarUrl,
      authDialogVisible,
      authSubmitting,
      authForm,
      authFormRef,
      authRules,
      openAuthDialog,
      submitAuth,
      formatIdNumber,
      formatTime
    };
  }
}
</script>

<style scoped>
.user-center-container {
  padding: 20px;
}

.user-info-card, .stats-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}

.user-info-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  margin-bottom: 20px;
}

.user-details {
  width: 100%;
  max-width: 500px;
  margin-bottom: 20px;
}

.auth-status {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.auth-tag {
  margin-right: 10px;
}

.stats-content {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 20px 0;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-icon {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 10px;
  background-color: #ecf5ff;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
}
</style>
