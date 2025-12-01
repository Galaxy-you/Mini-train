<template>
  <div class="passenger-edit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button circle @click="goBack" class="back-button">
        <el-icon><arrow-left /></el-icon>
      </el-button>
      <h2>{{ isEdit ? '编辑乘车人' : '添加乘车人' }}</h2>
    </div>

    <!-- 表单卡片 -->
    <el-card class="form-card" shadow="hover" v-loading="loading">
      <el-form
        ref="passengerFormRef"
        :model="passengerForm"
        :rules="passengerRules"
        label-position="top"
        class="passenger-form"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="realName">
              <el-input
                v-model="passengerForm.realName"
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

          <el-col :xs="24" :sm="12">
            <el-form-item label="证件类型" prop="idType">
              <el-select
                v-model="passengerForm.idType"
                placeholder="请选择证件类型"
                size="large"
                style="width: 100%"
              >
                <el-option label="身份证" value="1" />
                <el-option label="护照" value="2" />
                <el-option label="军官证" value="3" />
                <el-option label="港澳通行证" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="证件号码" prop="idNumber">
          <el-input
            v-model="passengerForm.idNumber"
            placeholder="请输入证件号码"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><postcard /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="passengerForm.phone"
                placeholder="请输入联系电话"
                size="large"
                clearable
              >
                <template #prefix>
                  <el-icon><phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="passengerForm.gender" size="large">
                <el-radio-button label="1">男</el-radio-button>
                <el-radio-button label="2">女</el-radio-button>
              </el-radio-group>
              <div v-if="passengerForm.idType === '1' && passengerForm.idNumber" class="auto-detect-tip">
                <el-icon><info-filled /></el-icon>
                <span>已根据身份证号自动识别</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider />

        <el-form-item>
          <div class="form-actions">
            <el-button size="large" @click="goBack">取消</el-button>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="submitForm"
            >
              保存
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, User, Postcard, Phone, InfoFilled } from '@element-plus/icons-vue';
import { passengerAPI } from '@/api';
import dataHelper from '@/utils/dataHelper';

export default {
  name: 'PassengerEdit',
  components: {
    ArrowLeft, User, Postcard, Phone, InfoFilled
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const passengerId = route.params.id;
    const passengerFormRef = ref(null);
    const loading = ref(false);
    const submitting = ref(false);
    
    const isEdit = computed(() => !!passengerId);
    
    const passengerForm = reactive({
      realName: '',
      idType: '1',
      idNumber: '',
      cardId: '',
      phone: '',
      gender: '1' // 默认男
    });
    
    // 监听证件号码变化，自动确定性别
    const autoDetectGender = (idNumber) => {
      // 只针对身份证类型且有效长度的证件号码进行处理
      if (passengerForm.idType === '1' && (idNumber.length === 18 || idNumber.length === 15)) {
        // 18位身份证号，倒数第二位即第17位（索引16）判断性别
        // 15位身份证号，倒数第一位即第15位（索引14）判断性别
        const sexIndex = idNumber.length === 18 ? 16 : 14;
        const sexDigit = parseInt(idNumber.charAt(sexIndex));
        
        // 奇数为男性，偶数为女性
        passengerForm.gender = sexDigit % 2 === 1 ? '1' : '2';
        
        // 同时设置cardId与idNumber保持一致，确保后端能正确接收数据
        passengerForm.cardId = idNumber;
        
        console.log(`根据身份证号${idNumber}自动判断性别: ${passengerForm.gender === '1' ? '男' : '女'}`);
      }
    };
    
    // 验证身份证号
    const validateIdNumber = (rule, value, callback) => {
      if (passengerForm.idType === '1') {
        const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
        if (!reg.test(value)) {
          callback(new Error('请输入正确的身份证号'));
        } else {
          callback();
        }
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
    
    // 表单验证规则
    const passengerRules = {
      realName: [
        { required: true, message: '请输入乘车人姓名', trigger: 'blur' }
      ],
      idType: [
        { required: true, message: '请选择证件类型', trigger: 'change' }
      ],
      idNumber: [
        { required: true, message: '请输入证件号码', trigger: 'blur' },
        { validator: validateIdNumber, trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { validator: validatePhone, trigger: 'blur' }
      ],
      gender: [
        { required: true, message: '请选择性别', trigger: 'change' }
      ]
    };
    
    // 获取乘车人详情
    const fetchPassenger = async () => {
      if (!passengerId) return;
      
      loading.value = true;
      try {
        const response = await passengerAPI.getPassenger(passengerId);
        const extractedData = dataHelper.extractApiData(response);
        if (extractedData) {
          // 使用适配器处理乘车人数据
          const adaptedPassenger = dataHelper.adaptPassengerData(extractedData);
          console.log('处理后的乘车人详情:', adaptedPassenger);
          
          // 将适配后的数据填充到表单
          Object.keys(passengerForm).forEach(key => {
            if (adaptedPassenger[key] !== undefined) {
              passengerForm[key] = adaptedPassenger[key];
            }
          });
        }
      } catch (error) {
        console.error('获取乘车人详情失败:', error);
        ElMessage.error('获取乘车人详情失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 提交表单
    const submitForm = () => {
      passengerFormRef.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true;
          
          // 准备提交数据
          const submitData = {
            ...passengerForm,
            // 确保cardId存在（使用idNumber的值）
            cardId: passengerForm.idNumber,
          };
          
          // 处理证件类型
          if (submitData.idType === '1') {
            submitData.cardType = '身份证';
          } else if (submitData.idType === '2') {
            submitData.cardType = '护照';
          } else if (submitData.idType === '3') {
            submitData.cardType = '军官证';
          } else if (submitData.idType === '4') {
            submitData.cardType = '港澳通行证';
          }
          
          console.log("准备提交的乘车人数据:", JSON.stringify(submitData));
          
          try {
            if (isEdit.value) {
              // 编辑乘车人
              await passengerAPI.updatePassenger(passengerId, submitData);
              ElMessage.success('编辑乘车人成功');
            } else {
              // 添加乘车人
              await passengerAPI.addPassenger(submitData);
              ElMessage.success('添加乘车人成功');
            }
            
            // 如果有重定向路径，则跳转到该路径，否则返回乘车人列表
            if (route.query.redirect) {
              router.push(route.query.redirect);
            } else {
              router.push('/passenger');
            }
          } catch (error) {
            console.error('保存乘车人失败:', error);
            ElMessage.error('保存乘车人失败');
          } finally {
            submitting.value = false;
          }
        }
      });
    };
    
    // 返回上一页
    const goBack = () => {
      router.back();
    };
    
    // 监听身份证号码变化，自动确定性别
    watch(() => passengerForm.idNumber, (newValue) => {
      if (passengerForm.idType === '1' && newValue) {
        autoDetectGender(newValue);
      }
    });
    
    // 监听证件类型变化，如果切换到身份证类型并且已有号码，自动确定性别
    watch(() => passengerForm.idType, (newValue) => {
      if (newValue === '1' && passengerForm.idNumber) {
        autoDetectGender(passengerForm.idNumber);
      }
    });
    
    onMounted(() => {
      if (isEdit.value) {
        fetchPassenger();
      }
    });
    
    return {
      isEdit,
      passengerFormRef,
      passengerForm,
      passengerRules,
      loading,
      submitting,
      submitForm,
      goBack
    };
  }
}
</script>

<style scoped>
.passenger-edit-container {
  max-width: 800px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-button {
  background: #fff;
  border: 1px solid #d9d9d9;
  transition: all 0.3s;
}

.back-button:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

/* 表单卡片 */
.form-card {
  border-radius: 12px;
}

.passenger-form {
  padding: 20px 0;
}

.passenger-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #595959;
}

.passenger-form :deep(.el-input__wrapper) {
  padding: 12px 15px;
}

.auto-detect-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #52c41a;
}

.auto-detect-tip .el-icon {
  font-size: 14px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

.form-actions .el-button {
  min-width: 120px;
}

/* 响应式 */
@media (max-width: 768px) {
  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>
