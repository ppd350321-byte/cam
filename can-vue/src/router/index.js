import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import Dashboard from '@/pages/Dashboard.vue'
import Orders from '@/pages/Orders.vue'
import Production from '@/pages/Production.vue'
import SupplyChain from '@/pages/SupplyChain.vue'
import Users from '@/pages/Users.vue'

// 路由配置（对应 React Router）
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: Dashboard },
    { path: '/orders', component: Orders },
    { path: '/production', component: Production },
    { path: '/supply-chain', component: SupplyChain },
    { path: '/users', component: Users },
    { path: '/reports', component: () => import('@/pages/Reports.vue') },
    { path: '/settings', component: () => import('@/pages/Settings.vue') }
  ]
})

export default router
