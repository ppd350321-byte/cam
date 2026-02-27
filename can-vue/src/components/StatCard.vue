<template>
  <div 
    class="bg-white rounded-xl shadow-sm border border-slate-100 p-6 flex items-start justify-between hover:shadow-md transition-shadow cursor-pointer"
    :class="onClick ? 'hover:bg-slate-50' : ''"
    @click="handleClick"
  >
    <div class="flex-1 mr-4">
      <p class="text-sm font-medium text-slate-500">{{ label }}</p>
      <h3 class="text-2xl font-bold text-slate-900 mt-2">{{ value }}</h3>
      <p 
        v-if="trend"
        class="text-xs font-medium mt-1 flex items-center"
        :class="trendUp ? 'text-emerald-600' : 'text-red-500'"
      >
        {{ trendUp ? '↑' : '↓' }} {{ trend }}
        <span class="text-slate-400 ml-1 font-normal">{{ trendLabel }}</span>
      </p>
    </div>
    <div :class="colorMap[color]">
      <component :is="icon" class="w-6 h-6 p-3 rounded-lg" />
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

// 定义 Props
const props = defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  trend: {
    type: String,
    default: ''
  },
  trendUp: {
    type: Boolean,
    default: false
  },
  icon: {
    type: Object,
    required: true
  },
  color: {
    type: String,
    validator: (value) => ['emerald', 'blue', 'orange', 'purple', 'red'].includes(value),
    default: 'emerald'
  },
  trendLabel: {
    type: String,
    default: 'vs last week'
  }
});

// 定义事件
const emit = defineEmits(['click']);

// 点击事件处理
const handleClick = () => {
  emit('click');
};

// 颜色映射
const colorMap = computed(() => ({
  emerald: 'bg-emerald-100 text-emerald-600',
  blue: 'bg-blue-100 text-blue-600',
  orange: 'bg-orange-100 text-orange-600',
  purple: 'bg-purple-100 text-purple-600',
  red: 'bg-red-100 text-red-600'
}));
</script>