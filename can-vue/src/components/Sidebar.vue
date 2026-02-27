<template>
  <div class="w-64 bg-indigo-900 text-slate-100 h-screen flex flex-col fixed left-0 top-0 shadow-xl z-20">
    <div class="p-6 border-b border-indigo-800">
      <h1 class="text-xl font-bold bg-gradient-to-r from-purple-400 to-indigo-300 text-transparent bg-clip-text">
        Community Canteen
      </h1>
      <p class="text-xs text-slate-400 mt-1">Admin Operations</p>
    </div>

    <nav class="flex-1 py-4">
      <button
        v-for="item in menuItems"
        :key="item.id"
        @click="navigate(item.id)"
        class="w-full flex items-center px-6 py-3 transition-colors duration-200"
        :class="[
          currentPath === item.id 
            ? 'bg-purple-600/20 text-purple-400 border-r-2 border-purple-400' 
            : 'text-slate-400 hover:bg-indigo-800 hover:text-slate-100'
        ]"
      >
        <component :is="item.icon" class="w-5 h-5 mr-3" />
        <span class="font-medium">{{ item.label }}</span>
      </button>
    </nav>

    <div class="p-4 border-t border-indigo-800 text-xs text-slate-500">
      v1.0.3 &copy; 2024
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
// 导入 Vue 版图标
import {
  LayoutDashboard,
  Users,
  ShoppingBag,
  Truck,
  ChefHat,
  BarChart3,
  Settings,
  PieChart
} from 'lucide-vue-next';

// 定义 Props（替代 React 的 Props 类型）
const props = defineProps({
  currentPath: {
    type: String,
    required: true
  }
});

// 定义事件（替代 React 的回调函数）
const emit = defineEmits(['navigate']);

// 导航方法
const navigate = (path) => {
  emit('navigate', path);
};

// 菜单数据（新增 Analytics 项）
const menuItems = [
  { id: '/', label: 'Dashboard', icon: LayoutDashboard },
  { id: '/orders', label: 'Order Management', icon: ShoppingBag },
  { id: '/users', label: 'User Management', icon: Users },
  { id: '/supply-chain', label: 'Supply Chain', icon: Truck },
  { id: '/production', label: 'Production', icon: ChefHat },
  { id: '/analytics', label: 'Analytics', icon: PieChart }, // 新增菜单项
  { id: '/reports', label: 'Reports', icon: BarChart3 },
  { id: '/settings', label: 'Settings', icon: Settings },
];
</script>