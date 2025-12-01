/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="register-container">
    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <h1 class="brand-title">加入我们</h1>
        <p class="brand-subtitle">注册Mini12306账号，开启便捷出行</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>安全可靠的购票保障</span>
          </div>
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>便捷的订单管理</span>
          </div>
          <div class="feature-item">
            <el-icon><check /></el-icon>
            <span>快速的退改签服务</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="form-section">
      <div class="register-box">
        <h2 class="form-title">创建账号</h2>
        <p class="form-subtitle">请填写以下信息完成注册</p>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><user /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  placeholder="请输入密码"
                  type="password"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <el-icon><lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  placeholder="请再次输入密码"
                  type="password"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <el-icon><lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  size="large"
                  clearable
                >
                  <template #prefix>
                    <el-icon><user /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idNumber">
                <el-input
                  v-model="registerForm.idNumber"
                  placeholder="请输入身份证号"
                  size="large"
                  clearable
                >
                  <template #prefix>
                    <el-icon><postcard /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              size="large"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <router-link to="/login" class="login-link">
            已有账号？立即登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Phone, Postcard, Check } from '@element-plus/icons-vue';
import { authAPI } from '@/api';

export default {
  name: 'Register',
  components: {
    User,
    Lock,
    Phone,
    Postcard,
    Check
  },
  setup() {
    const router = useRouter();
    const registerFormRef = ref(null);
    const loading = ref(false);

    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      realName: '',
      idNumber: ''
    });

    const validatePass = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    const validateIdNumber = (rule, value, callback) => {
      const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
      if (!reg.test(value)) {
        callback(new Error('请输入正确的身份证号'));
      } else {
        callback();
      }
    };

    const validatePhone = (rule, value, callback) => {
      const reg = /^1[3-9]\d{9}$/;
      if (!reg.test(value)) {
        callback(new Error('请输入正确的手机号'));
      } else {
        callback();
      }
    };

    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, message: '用户名不能少于3个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码不能少于6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { validator: validatePhone, trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      idNumber: [
        { required: true, message: '请输入身份证号', trigger: 'blur' },
        { validator: validateIdNumber, trigger: 'blur' }
      ]
    };

    const handleRegister = () => {
      registerFormRef.value.validate(async valid => {
        if (valid) {
          loading.value = true;
          try {
            // 构造注册请求数据
            const registerData = {
              username: registerForm.username,
              password: registerForm.password,
              phone: registerForm.phone,
              realName: registerForm.realName,
              cardId: registerForm.idNumber // 调整字段名以匹配后端API
            };
            
            console.log('发送注册请求:', registerData);
            await authAPI.register(registerData);
            
            ElMessage({
              message: '注册成功，请登录',
              type: 'success'
            });
            
            router.push('/login').catch(err => {
              console.error('路由跳转失败:', err);
            });
          } catch (error) {
            console.error('注册失败:', error);
            ElMessage({
              message: '注册失败: ' + (error.message || '未知错误'),
              type: 'error'
            });
          } finally {
            loading.value = false;
          }
        }
      });
    };

    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      handleRegister
    };
  }
}
</script>

<style scoped>
.register-container {
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
  position: relative;
  overflow: hidden;
  /* 添加高铁背景图片 - 渐变遮罩 + 图片 */
  background-image:
      url('@/assets/train-bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
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
  flex: 0 0 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
  overflow-y: auto;
}

.register-box {
  width: 100%;
  max-width: 480px;
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

.register-form {
  margin-top: 24px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #595959;
}

.register-form :deep(.el-input__wrapper) {
  padding: 12px 15px;
}

.register-button {
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

.login-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.login-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
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

