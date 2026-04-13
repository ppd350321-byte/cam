import Vue from 'vue';
import Router from 'vue-router';
import { Message } from 'element-ui';
import { clearToken, getCurrentUser, getToken } from '@/utils/token';
import { hasPermission, isAdmin } from '@/utils/permission';
import { PERMISSIONS } from '@/constants/permissions';

Vue.use(Router);

const Login = () => import('@/views/Login.vue');
const MainLayout = () => import('@/layouts/MainLayout.vue');
const Dashboard = () => import('@/views/Dashboard.vue');
const UserManagement = () => import('@/views/UserManagement.vue');
const OrderManagement = () => import('@/views/OrderManagement.vue');
const SupplyChain = () => import('@/views/SupplyChain.vue');
const Production = () => import('@/views/Production.vue');
const Reports = () => import('@/views/Reports.vue');
const VipCouponConfig = () => import('@/views/VipCouponConfig.vue');
const MenuManagement = () => import('@/views/MenuManagement.vue');
const PermissionConfig = () => import('@/views/PermissionConfig.vue');
const EmployeeManagement = () => import('@/views/EmployeeManagement.vue');

const router = new Router({
  mode: 'hash',
  routes: [
    { path: '/login', name: 'login', component: Login },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: Dashboard, meta: { title: '仪表盘' } },
        { path: 'users', name: 'users', component: UserManagement, meta: { title: '用户管理', permission: PERMISSIONS.USERS_VIEW } },
        { path: 'orders', name: 'orders', component: OrderManagement, meta: { title: '订单管理', permission: PERMISSIONS.ORDERS_VIEW } },
        { path: 'menu-management', name: 'menu-management', component: MenuManagement, meta: { title: '菜品管理', permission: PERMISSIONS.MENU_VIEW } },
        { path: 'supply', name: 'supply', component: SupplyChain, meta: { title: '供应链配置', permission: PERMISSIONS.SUPPLY_VIEW } },
        { path: 'production', name: 'production', component: Production, meta: { title: '生产调度', permission: PERMISSIONS.PRODUCTION_VIEW } },
        { path: 'reports', name: 'reports', component: Reports, meta: { title: '报表中心', permission: PERMISSIONS.REPORTS_VIEW } },
        { path: 'vip-coupon-config', name: 'vip-coupon-config', component: VipCouponConfig, meta: { title: 'VIP与优惠券', permission: PERMISSIONS.VIP_COUPON_VIEW, adminOnly: true } },
        {
          path: 'permission-config',
          name: 'permission-config',
          component: PermissionConfig,
          meta: { title: '权限配置', permission: PERMISSIONS.PERMISSION_CONFIG_VIEW, adminOnly: true }
        },
        {
          path: 'employee-management',
          name: 'employee-management',
          component: EmployeeManagement,
          meta: { title: '员工管理', permission: PERMISSIONS.PERMISSION_CONFIG_VIEW, adminOnly: true }
        }
      ]
    }
  ]
});

router.beforeEach((to, from, next) => {
  const token = getToken();
  const user = getCurrentUser();
  if (token && !user) {
    clearToken();
    next('/login');
    return;
  }
  if (to.path !== '/login' && !token) {
    next('/login');
    return;
  }
  if (to.path === '/login' && token) {
    next('/dashboard');
    return;
  }
  if (to.meta && to.meta.adminOnly && !isAdmin(user)) {
    Message.warning('您没有权限访问该页面');
    next('/dashboard');
    return;
  }
  if (to.meta && to.meta.permission && !hasPermission(user, to.meta.permission)) {
    Message.warning('您没有权限访问该页面');
    next('/dashboard');
    return;
  }
  next();
});

export default router;
