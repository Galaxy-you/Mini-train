/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <h1 class="brand-title">Mini12306</h1>
        <p class="brand-subtitle">智能便捷的铁路购票平台</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>快速查询车次</span>
          </div>
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>在线支付订票</span>
          </div>
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>便捷退改签</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-section">
      <div class="login-box">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号信息</p>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><user /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              type="password"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-button"
              size="large"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <router-link to="/register" class="register-link">
            还没有账号？立即注册
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Check } from '@element-plus/icons-vue';
import { authAPI } from '@/api';

export default {
  name: 'Login',
  components: {
    User,
    Lock,
    Check
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const loginFormRef = ref(null);
    const loading = ref(false);
    const rememberMe = ref(false);

    const loginForm = reactive({
      username: '',
      password: ''
    });

    // 加载记住的用户名
    onMounted(() => {
      const rememberedUsername = localStorage.getItem('rememberedUsername');
      if (rememberedUsername) {
        loginForm.username = rememberedUsername;
        rememberMe.value = true;
      }
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
            const response = await authAPI.login(loginForm);

            if (!response || !response.data || !response.data.token) {
              throw new Error('登录返回数据无效');
            }
            
            const data = response.data;
            localStorage.setItem('token', data.token);
            localStorage.setItem('userId', data.userId);

            // 处理记住我
            if (rememberMe.value) {
              localStorage.setItem('rememberedUsername', loginForm.username);
            } else {
              localStorage.removeItem('rememberedUsername');
            }

            ElMessage.success('登录成功');

            const redirect = route.query.redirect || '/home';
            setTimeout(() => {
              router.push(redirect);
            }, 500);
          } catch (error) {
            ElMessage.error('登录失败: ' + (error.message || '未知错误'));
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
      rememberMe,
      handleLogin
    };
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #fff;
}

.brand-content {
  max-width: 500px;
}

.brand-title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 16px 0;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 20px;
  margin: 0 0 48px 0;
  opacity: 0.9;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.feature-item .el-icon {
  font-size: 20px;
  color: #52c41a;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px;
  border-radius: 50%;
}

/* 右侧表单区 */
.form-section {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 400px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 32px 0;
  text-align: center;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 15px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
  margin-top: 8px;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.register-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    flex: 0 0 auto;
    padding: 40px 20px;
  }

  .brand-title {
    font-size: 36px;
  }

  .form-section {
    flex: 1;
    padding: 20px;
  }
}
</style>
