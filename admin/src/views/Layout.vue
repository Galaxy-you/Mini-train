<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="logo">
        <h2>Mini12306 管理后台</h2>
      </div>
      <el-menu 
        :default-active="activeMenu" 
        class="sidebar-menu" 
        background-color="#304156"
        text-color="#fff"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>系统仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/station">
          <el-icon><Location /></el-icon>
          <span>车站管理</span>
        </el-menu-item>
        <el-menu-item index="/train">
          <el-icon><Ship /></el-icon>
          <span>车次管理</span>
        </el-menu-item>
        <el-menu-item index="/route">
          <el-icon><Map /></el-icon>
          <span>线路管理</span>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <span>时刻表管理</span>
        </el-menu-item>
        <el-menu-item index="/ticket">
          <el-icon><Ticket /></el-icon>
          <span>车票管理</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="40" icon="el-icon-user"></el-avatar>
              <span class="username">管理员</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 页面内容 -->
      <div class="page-container">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Odometer, Location, Ship, Map, Calendar, Ticket, User, CaretBottom } from '@element-plus/icons-vue'

export default {
  name: 'AdminLayout',
  components: {
    Odometer, 
    Location, 
    Ship, 
    Map, 
    Calendar, 
    Ticket, 
    User, 
    CaretBottom
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // 根据当前路由计算激活菜单
    const activeMenu = computed(() => {
      return route.path
    })
    
    // 退出登录
    const logout = () => {
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
      }).catch(() => {})
    }
    
    return {
      activeMenu,
      logout
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background-color: #304156;
  color: white;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo h2 {
  font-size: 18px;
  margin: 0;
  text-align: center;
}

.sidebar-menu {
  border-right: none;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.navbar {
  height: 50px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  padding: 0 15px;
  justify-content: flex-end;
}

.right-menu {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 10px;
}

.page-container {
  flex: 1;
  padding: 20px;
  background-color: #f0f2f5;
  overflow: auto;
}
</style>
