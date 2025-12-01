/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="user-center-container">
    <div class="user-header">
      <div class="user-profile">
        <el-avatar :size="80" class="profile-avatar">
          {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div class="profile-info">
          <h2 class="username">{{ userInfo.username || '用户' }}</h2>
          <el-tag :type="userInfo.authenticated ? 'success' : 'warning'" size="large">
            <el-icon>
              <component :is="userInfo.authenticated ? SuccessFilled : WarningFilled" />
            </el-icon>
            {{ userInfo.authenticated ? '已实名认证' : '未实名认证' }}
          </el-tag>
        </div>
      </div>
      <el-button
        v-if="!userInfo.authenticated"
        type="primary"
        size="large"
        @click="openAuthDialog"
      >
        立即认证
      </el-button>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：基本信息 -->
      <el-col :xs="24" :lg="16">
        <el-card class="info-card" shadow="hover" v-loading="loading">
          <template #header>
            <div class="card-header">
              <el-icon><user /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <div class="info-grid" v-if="userInfo">
            <div class="info-item">
              <div class="info-label">
                <el-icon><user /></el-icon>
                用户名
              </div>
              <div class="info-value">{{ userInfo.username || '未设置' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <el-icon><user /></el-icon>
                真实姓名
              </div>
              <div class="info-value">{{ userInfo.realName || '未设置' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <el-icon><postcard /></el-icon>
                身份证号
              </div>
              <div class="info-value">{{ formatIdNumber(userInfo.cardId) || '未设置' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <el-icon><phone /></el-icon>
                手机号码
              </div>
              <div class="info-value">{{ userInfo.phone || '未设置' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <el-icon><calendar /></el-icon>
                注册时间
              </div>
              <div class="info-value">{{ formatTime(userInfo.createTime) || '未知' }}</div>
            </div>
          </div>

          <el-empty v-else-if="!loading" description="暂无信息" :image-size="100" />
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="action-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><setting /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>

          <div class="action-grid">
            <div class="action-item" @click="$router.push('/order')">
              <el-icon class="action-icon" color="#1890ff"><tickets /></el-icon>
              <span class="action-label">我的订单</span>
            </div>

            <div class="action-item" @click="$router.push('/ticket')">
              <el-icon class="action-icon" color="#52c41a"><ticket /></el-icon>
              <span class="action-label">我的车票</span>
            </div>

            <div class="action-item" @click="$router.push('/passenger')">
              <el-icon class="action-icon" color="#faad14"><user-filled /></el-icon>
              <span class="action-label">乘车人</span>
            </div>

            <div class="action-item" @click="handleLogout">
              <el-icon class="action-icon" color="#ff4d4f"><switch-button /></el-icon>
              <span class="action-label">退出登录</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：统计信息 -->
      <el-col :xs="24" :lg="8">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><data-analysis /></el-icon>
              <span>使用统计</span>
            </div>
          </template>

          <div class="stats-list">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon><tickets /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.orderCount || 0 }}</div>
                <div class="stat-label">总订单数</div>
              </div>
            </div>

            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon><ticket /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.ticketCount || 0 }}</div>
                <div class="stat-label">购票总数</div>
              </div>
            </div>

            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon><user /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.passengerCount || 0 }}</div>
                <div class="stat-label">常用乘车人</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  User, Postcard, Phone, Calendar, Setting,
  Tickets, Ticket, UserFilled, SwitchButton,
  DataAnalysis, SuccessFilled, WarningFilled
} from '@element-plus/icons-vue';
import { authAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default {
  name: 'UserCenter',
  components: {
    User, Postcard, Phone, Calendar, Setting,
    Tickets, Ticket, UserFilled, SwitchButton,
    DataAnalysis, SuccessFilled, WarningFilled
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const userInfo = ref({
      username: '',
      realName: '',
      cardId: '',
      phone: '',
      authenticated: false,
      createTime: null
    });
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
        
        // 获取用户统计数据
        await getUserStats();

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
    
    // 获取用户统计数据
    const getUserStats = async () => {
      try {
        const response = await authAPI.getUserStats();
        console.log('获取到的统计数据响应:', response);

        const extractedData = dataHelper.extractApiData(response);
        const statsData = dataHelper.ensureObject(extractedData);

        console.log('处理后的统计数据:', statsData);

        // 更新统计数据
        stats.value = {
          orderCount: statsData.orderCount || 0,
          ticketCount: statsData.ticketCount || 0,
          passengerCount: statsData.passengerCount || 0
        };
      } catch (error) {
        console.error('获取统计数据失败:', error);
        // 失败时使用默认值
        stats.value = {
          orderCount: 0,
          ticketCount: 0,
          passengerCount: 0
        };
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

    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        ElMessage.success('退出成功');
        router.push('/login');
      }).catch(() => {});
    };

    onMounted(() => {
      getUserInfo();
    });
    
    return {
      loading,
      userInfo,
      stats,
      authDialogVisible,
      authSubmitting,
      authForm,
      authFormRef,
      authRules,
      openAuthDialog,
      submitAuth,
      formatIdNumber,
      formatTime,
      handleLogout
    };
  }
}
</script>

<style scoped>
.user-center-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 用户头部 */
.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar {
  background: rgba(255, 255, 255, 0.2);
  font-size: 32px;
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.username {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

/* 信息卡片 */
.info-card, .action-card, .stats-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 16px;
}

.card-header .el-icon {
  color: #1890ff;
  font-size: 18px;
}

.info-grid {
  display: grid;
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-item:hover {
  background: #f0f0f0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c8c8c;
  font-size: 14px;
}

.info-label .el-icon {
  color: #1890ff;
}

.info-value {
  color: #262626;
  font-weight: 500;
}

/* 快捷操作 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #e6f7ff;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
}

.action-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

/* 统计卡片 */
.stats-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    gap: 16px;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
