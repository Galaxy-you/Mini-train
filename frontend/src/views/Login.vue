/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="login-container">
    <div class="login-box">
      <div class="title">Mini12306</div>
      <div class="login-form">
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名">
              <template #prefix>
                <el-icon><user /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" placeholder="密码" type="password" show-password>
              <template #prefix>
                <el-icon><lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-links">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authAPI } from '@/api';

export default {
  name: 'Login',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const loginFormRef = ref(null);
    const loading = ref(false);

    const loginForm = reactive({
      username: '',
      password: ''
    });

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码不能少于6个字符', trigger: 'blur' }
      ]
    };

    const handleLogin = () => {
      loginFormRef.value.validate(async valid => {
        if (valid) {
          loading.value = true;
          try {
            console.log('准备发送登录请求:', loginForm);
            const response = await authAPI.login(loginForm);
            console.log('登录成功，完整返回数据:', response);
            
            // 确保响应包含必要的数据
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
            console.log('准备跳转到:', redirect || '/home');
            console.log('当前路由对象:', router);
            console.log('当前路由位置:', router.currentRoute.value);
            
            // 显示调试信息，查看token是否正确存储
            console.log('再次确认localStorage中的token:', localStorage.getItem('token'));
            
            // 使用Vue Router导航，不重载页面
            setTimeout(() => {
              router.push(redirect || '/home')
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
            loading.value = false;
          }
        }
      });
    };

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin
    };
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
  width: 400px;
  padding: 30px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 26px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  margin-bottom: 30px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}

.login-links {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  font-size: 14px;
}

.login-links a {
  color: #409EFF;
  text-decoration: none;
}
</style>
