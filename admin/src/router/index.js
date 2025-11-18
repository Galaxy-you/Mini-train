import { createRouter, createWebHistory } from 'vue-router';
import { isLoggedIn, getToken } from '@/utils/tokenHelper';

// 路由懒加载
const Login = () => import('@/views/Login.vue');
const Layout = () => import('@/views/Layout.vue');
const Dashboard = () => import('@/views/Dashboard.vue');
const StationManage = () => import('@/views/station/StationManage.vue');
const TrainManage = () => import('@/views/train/TrainManage.vue');
const RouteManage = () => import('@/views/route/RouteManage.vue');
const ScheduleManage = () => import('@/views/schedule/ScheduleManage.vue');
const TicketManage = () => import('@/views/ticket/TicketManage.vue');
const UserManage = () => import('@/views/user/UserManage.vue');

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { title: '登录', noAuth: true }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: Dashboard,
          meta: { title: '仪表盘' }
        },
        {
          path: 'station',
          name: 'StationManage',
          component: StationManage,
          meta: { title: '车站管理' }
        },
        {
          path: 'train',
          name: 'TrainManage',
          component: TrainManage,
          meta: { title: '车次管理' }
        },
        {
          path: 'route',
          name: 'RouteManage',
          component: RouteManage,
          meta: { title: '线路管理' }
        },
        {
          path: 'schedule',
          name: 'ScheduleManage',
          component: ScheduleManage,
          meta: { title: '时刻表管理' }
        },
        {
          path: 'ticket',
          name: 'TicketManage',
          component: TicketManage,
          meta: { title: '车票管理' }
        },
        {
          path: 'user',
          name: 'UserManage',
          component: UserManage,
          meta: { title: '用户管理' }
        }
      ]
    }
  ]
});

// 使用tokenHelper检查登录状态
import { adminAPI } from '@/api';

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Mini12306管理后台` : 'Mini12306管理后台';
  
  // 详细的调试日志
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');
  
  console.log('路由导航 - 目标路径:', to.fullPath);
  console.log('路由导航 - 来源路径:', from.fullPath);
  console.log('路由导航 - 页面meta:', to.meta);
  console.log('路由导航 - 是否需要认证:', !to.meta.noAuth);
  console.log('路由导航 - Token值:', token);
  console.log('路由导航 - UserId值:', userId);
  
  // 如果是不需要认证的页面，或者已有token，则允许访问
  if (to.path === '/login' || to.meta.noAuth || token) {
    console.log('路由导航 - 允许访问:', to.fullPath);
    next();
  } else {
    // 否则跳转到登录页
    console.log('路由导航 - 未授权，重定向到登录页');
    next({ path: '/login', query: { redirect: to.fullPath } });
  }
});

export default router;
