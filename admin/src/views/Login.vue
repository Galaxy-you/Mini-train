<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>Mini12306 管理后台</h2>
      </div>
      <el-form 
        ref="loginFormRef"
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            :loading="loading" 
            type="primary" 
            class="login-button" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'
import { saveToken, clearAuth } from '@/utils/tokenHelper'

export default {
  name: 'LoginView',
  components: {
    User,
    Lock
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loginFormRef = ref(null)
    const loading = ref(false)
    
    // 登录表单
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    // 表单验证规则
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    }
    
    // 处理登录 - 参考用户前端的登录处理方式实现
    const handleLogin = () => {
      loginFormRef.value.validate(async valid => {
        if (valid) {
          loading.value = true;
          try {
            console.log('准备发送登录请求:', loginForm);
            const response = await adminAPI.login(loginForm);
            console.log('登录成功，完整返回数据:', response);
            
            // 处理响应，提取token和userId
            if (!response || !response.data || !response.data.token) {
              throw new Error('登录返回数据无效：' + JSON.stringify(response));
            }
                        
            const data = response.data;
            console.log('提取响应数据:', data);
            console.log('保存token到localStorage:', data.token);
            localStorage.setItem('token', data.token);
            localStorage.setItem('userId', data.userId);
            
            ElMessage({
              message: '登录成功',
              type: 'success'
            });
            
            // 如果有重定向路径，则跳转到该路径，否则跳转到首页
            const redirect = route.query.redirect;
            console.log('准备跳转到:', redirect || '/dashboard');
            console.log('当前路由对象:', router);
            console.log('当前路由位置:', router.currentRoute.value);
            
            // 显示调试信息，查看token是否正确存储
            console.log('再次确认localStorage中的token:', localStorage.getItem('token'));

            // 延迟跳转，确保token已存储
            setTimeout(() => {
              router.push(redirect || '/dashboard')
                .then(() => console.log('路由跳转成功!'))
                .catch(err => console.error('路由跳转失败:', err));
            }, 500);
          } catch (error) {
            console.error('登录失败:', error);
            ElMessage({
              message: '登录失败: ' + (error.message || '未知错误'),
              type: 'error'
            });
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    return {
      loginForm,
      loginRules,
      loginFormRef,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-box {
  width: 350px;
  padding: 30px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-weight: 600;
  color: #303133;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}
</style>
