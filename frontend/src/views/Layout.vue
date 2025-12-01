/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header height="64px" class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><train /></el-icon>
          <span class="logo-text">Mini12306</span>
        </div>
      </div>

      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
          <div class="user-info-wrapper">
            <el-avatar :size="36" class="user-avatar">
              {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <span class="username">{{ userInfo.username || '用户' }}</span>
            <el-icon class="dropdown-icon"><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="userCenter">
                <el-icon><user /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><switch-button /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主体内容区域 -->
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
        >
          <el-menu-item index="/home">
            <el-icon><house /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item index="/train/search">
            <el-icon><search /></el-icon>
            <span>车票查询</span>
          </el-menu-item>

          <el-menu-item index="/order">
            <el-icon><tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>

          <el-menu-item index="/ticket">
            <el-icon><ticket /></el-icon>
            <span>车票管理</span>
          </el-menu-item>

          <el-menu-item index="/passenger">
            <el-icon><user /></el-icon>
            <span>乘车人管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区 -->
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { House, Search, User, Tickets, Ticket, ArrowDown, Train, SwitchButton } from '@element-plus/icons-vue';
import { authAPI } from '@/api';

export default {
  name: 'Layout',
  components: {
    House,
    Search,
    User,
    Tickets,
    Ticket,
    ArrowDown,
    Train,
    SwitchButton
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    
    const userInfo = ref({
      username: '',
      userId: null
    });

    const activeMenu = computed(() => route.path);

    const getUserInfo = async () => {
      try {
        const data = await authAPI.getUserInfo();
        userInfo.value = data;
      } catch (error) {
        console.error('获取用户信息失败:', error);
      }
    };

    // 处理下拉菜单命令
    const handleCommand = (command) => {
      if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 清除token和用户信息
          localStorage.removeItem('token');
          localStorage.removeItem('userId');
          
          ElMessage({
            type: 'success',
            message: '退出成功'
          });
          
          // 跳转到登录页
          router.push('/login');
        }).catch(() => {});
      } else if (command === 'userCenter') {
        router.push('/user');
      }
    };

    onMounted(() => {
      getUserInfo();
    });

    return {
      userInfo,
      activeMenu,
      handleCommand
    };
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

/* 顶部导航栏 */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  cursor: pointer;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
}

.user-info-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
}

.user-info-wrapper:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 14px;
  color: #8c8c8c;
  transition: transform 0.3s;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 主体容器 */
.main-container {
  flex: 1;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  background: #fff;
  border-right: 1px solid #e8e8e8;
  height: 100%;
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 6px;
  color: #595959;
  font-size: 14px;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 8px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #f5f5f5;
  color: #1890ff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #e6f7ff;
  color: #1890ff;
  font-weight: 500;
}

/* 内容区 */
.content {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
  padding: 20px;
  overflow: auto;
}

.el-menu {
  border-right: none;
}
</style>
