/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header height="60px" class="header">
      <div class="logo">
        <router-link to="/">
          <h1>Mini12306</h1>
        </router-link>
      </div>
      <div class="user-info">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown-link">
            {{ userInfo.username || '用户' }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="userCenter">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主体内容区域 -->
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
        >
          <el-menu-item index="/home">
            <el-icon><house /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/train/search">
            <el-icon><search /></el-icon>
            <template #title>车票查询</template>
          </el-menu-item>
          <el-menu-item index="/passenger">
            <el-icon><user /></el-icon>
            <template #title>乘车人管理</template>
          </el-menu-item>
          <el-menu-item index="/order">
            <el-icon><tickets /></el-icon>
            <template #title>订单管理</template>
          </el-menu-item>
          <el-menu-item index="/ticket">
            <el-icon><ticket /></el-icon>
            <template #title>车票管理</template>
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
import { authAPI } from '@/api';

export default {
  name: 'Layout',
  setup() {
    const router = useRouter();
    const route = useRoute();
    
    const userInfo = ref({
      username: '',
      userId: null
    });

    // 当前激活的菜单
    const activeMenu = computed(() => {
      return route.path;
    });

    // 获取用户信息
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
}

.header {
  background-color: #409EFF;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 18px;
}

.logo a {
  text-decoration: none;
  color: #fff;
}

.user-dropdown-link {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #545c64;
  height: 100%;
}

.content {
  padding: 20px;
  overflow: auto;
}

.el-menu {
  border-right: none;
}
</style>
