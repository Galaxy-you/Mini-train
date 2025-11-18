import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建应用实例
const app = createApp(App)

// 注册ElementPlus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 导入token辅助函数
import { getToken } from './utils/tokenHelper'

// 使用路由和ElementPlus
app.use(router)
app.use(ElementPlus)

// 应用启动时检查token
console.log('应用启动 - 检查localStorage中的token')
const token = getToken()
console.log('应用启动时的token状态:', token ? '已存在' : '不存在')

// 检查localStorage的完整内容
console.log('应用启动时localStorage完整内容:', 
  Object.keys(localStorage).map(key => ({ key, value: localStorage.getItem(key) }))
)

// 挂载应用
app.mount('#app')
