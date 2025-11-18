/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="register-container">
    <div class="register-box">
      <div class="title">Mini12306 注册</div>
      <div class="register-form">
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" placeholder="请输入密码" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" placeholder="请再次输入密码" type="password" show-password />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idNumber">
            <el-input v-model="registerForm.idNumber" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="register-button" @click="handleRegister">注册</el-button>
          </el-form-item>
        </el-form>
        <div class="register-links">
          <router-link to="/login">已有账号？立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authAPI } from '@/api';

export default {
  name: 'Register',
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

    // 验证密码是否一致
    const validatePass = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    // 验证身份证号
    const validateIdNumber = (rule, value, callback) => {
      const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
      if (!reg.test(value)) {
        callback(new Error('请输入正确的身份证号'));
      } else {
        callback();
      }
    };

    // 验证手机号
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
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px 0;
}

.register-box {
  width: 500px;
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

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
}

.register-links {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  font-size: 14px;
}

.register-links a {
  color: #409EFF;
  text-decoration: none;
}
</style>
