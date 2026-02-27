<template>
  <div class="flex min-h-screen bg-slate-50 font-sans">
    <!-- 侧边栏组件 -->
    <Sidebar :current-path="$route.path" />
    
    <div class="flex-1 ml-64 flex flex-col">
      <!-- 头部导航 -->
      <header class="h-16 bg-white border-b border-slate-100 flex items-center justify-between px-8 sticky top-0 z-10 shadow-sm">
        <div class="flex items-center w-96">
          <Search class="w-4 h-4 text-slate-400 mr-2" />
          <input 
            type="text" 
            placeholder="Global search..." 
            class="bg-transparent text-sm w-full focus:outline-none text-slate-600 placeholder-slate-400"
          />
        </div>
        
        <div class="flex items-center gap-6">
          <button 
            @click="isAIModalOpen = true"
            class="flex items-center gap-2 bg-gradient-to-r from-emerald-500 to-teal-500 text-white px-4 py-2 rounded-full text-sm font-medium shadow hover:shadow-lg transition-all hover:-translate-y-0.5"
          >
            <Sparkles class="w-4 h-4" />
            AI Assistant
          </button>
          <div class="relative">
            <Bell class="w-5 h-5 text-slate-500 hover:text-slate-700 cursor-pointer transition-colors" />
            <span class="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </div>
          <div class="flex items-center gap-2 cursor-pointer">
            <UserCircle class="w-8 h-8 text-slate-400" />
            <div class="hidden md:block">
              <div class="text-sm font-medium text-slate-800">Admin User</div>
              <div class="text-xs text-slate-500">Manager</div>
            </div>
          </div>
        </div>
      </header>

      <!-- 路由出口 -->
      <main class="flex-1 p-8 overflow-y-auto">
        <div class="max-w-7xl mx-auto">
          <router-view></router-view>
        </div>
      </main>
    </div>

    <!-- AI 助手弹窗 -->
    <AIAssistant 
      v-model="isAIModalOpen" 
      :context-data="contextData"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
// 导入组件
import Sidebar from './components/Sidebar.vue';
import AIAssistant from './components/AIAssistant.vue';
// 导入图标（Vue 版）
import { Bell, Search, UserCircle, Sparkles } from 'lucide-vue-next';
// 导入模拟数据
import { MOCK_INVENTORY, MOCK_ORDERS, MOCK_TASKS, SALES_DATA } from './constants';

// 响应式状态（替代 React 的 useState）
const isAIModalOpen = ref(false);
const route = useRoute();

// 上下文数据（供 AI 助手使用）
const contextData = computed(() => ({
  sales: SALES_DATA,
  inventory_alerts: MOCK_INVENTORY.filter(i => i.currentStock < i.reorderLevel),
  pending_orders: MOCK_ORDERS.filter(o => o.status !== 'Completed').length,
  active_tasks: MOCK_TASKS.filter(t => t.status === 'In Progress'),
  system_time: new Date().toLocaleString()
}));
</script>