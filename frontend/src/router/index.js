import { createRouter, createWebHistory } from 'vue-router';

// 路由懒加载
const Login = () => import('@/views/Login.vue');
const Register = () => import('@/views/Register.vue');
const Layout = () => import('@/views/Layout.vue');
const Home = () => import('@/views/Home.vue');
const TrainSearch = () => import('@/views/TrainSearch.vue');
const TrainDetail = () => import('@/views/TrainDetail.vue');
const PassengerList = () => import('@/views/PassengerList.vue');
const PassengerEdit = () => import('@/views/PassengerEdit.vue');
const OrderCreate = () => import('@/views/OrderCreate.vue');
const OrderList = () => import('@/views/OrderList.vue');
const OrderDetail = () => import('@/views/OrderDetail.vue');
const TicketList = () => import('@/views/TicketList.vue');
const TicketDetail = () => import('@/views/TicketDetail.vue');
const UserCenter = () => import('@/views/UserCenter.vue');

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: Home,
        meta: { title: '首页' }
      },
      {
        path: 'train/search',
        name: 'TrainSearch',
        component: TrainSearch,
        meta: { title: '车票查询' }
      },
      {
        path: 'train/:id',
        name: 'TrainDetail',
        component: TrainDetail,
        meta: { title: '车次详情' }
      },
      {
        path: 'passenger',
        name: 'PassengerList',
        component: PassengerList,
        meta: { title: '乘车人管理' }
      },
      {
        path: 'passenger/edit/:id?',
        name: 'PassengerEdit',
        component: PassengerEdit,
        meta: { title: '编辑乘车人' }
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: OrderCreate,
        meta: { title: '提交订单' }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: OrderList,
        meta: { title: '订单列表' }
      },
      {
        path: 'order/:orderNo',
        name: 'OrderDetail',
        component: OrderDetail,
        meta: { title: '订单详情' }
      },
      {
        path: 'ticket',
        name: 'TicketList',
        component: TicketList,
        meta: { title: '车票列表' }
      },
      {
        path: 'ticket/:ticketNo',
        name: 'TicketDetail',
        component: TicketDetail,
        meta: { title: '车票详情' }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: UserCenter,
        meta: { title: '个人中心' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Mini12306` : 'Mini12306';
  
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
  if (to.meta.noAuth || token) {
    console.log('路由导航 - 允许访问:', to.fullPath);
    next();
  } else {
    // 否则跳转到登录页
    console.log('路由导航 - 未授权，重定向到登录页');
    next({ path: '/login', query: { redirect: to.fullPath } });
  }
});

export default router;
