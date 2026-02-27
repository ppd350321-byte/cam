<template>
  <teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        <!-- 头部（新增清空按钮） -->
        <div class="bg-gradient-to-r from-emerald-600 to-teal-600 p-4 flex justify-between items-center text-white">
          <div class="flex items-center gap-2">
            <Sparkles class="w-5 h-5" />
            <h2 class="font-semibold text-lg">Smart Canteen Assistant</h2>
          </div>
          <div class="flex gap-2">
            <button 
              v-if="response"
              @click="handleClear"
              class="hover:bg-white/20 p-1 rounded-full transition-colors"
            >
              <Trash2 class="w-4 h-4" />
            </button>
            <button @click="closeModal" class="hover:bg-white/20 p-1 rounded-full transition-colors">
              <X class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="p-6 overflow-y-auto flex-1 bg-slate-50">
          <!-- 初始提示 -->
          <div v-if="!response && !loading" class="text-center text-slate-500 py-10">
            <Sparkles class="w-12 h-12 mx-auto text-emerald-200 mb-4" />
            <p>Ask me about production schedules, inventory optimization, or sales analysis.</p>
            <p class="text-xs mt-2 text-slate-400">Powered by Gemini AI</p>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="flex flex-col items-center justify-center py-12">
            <Loader2 class="w-8 h-8 text-emerald-600 animate-spin mb-3" />
            <p class="text-slate-500 text-sm animate-pulse">Analyzing your data, please wait...</p>
          </div>

          <!-- 响应内容 -->
          <div v-if="response" class="bg-white p-5 rounded-xl border border-slate-100 shadow-sm prose prose-emerald max-w-none text-sm">
            <div class="whitespace-pre-wrap">{{ response }}</div>
          </div>
        </div>

        <!-- 输入区域（新增字数限制） -->
        <div class="p-4 bg-white border-t border-slate-100">
          <div class="relative">
            <input
              type="text"
              v-model="prompt"
              @keydown.enter="!loading && handleAnalyze"
              :maxlength="MAX_INPUT_LENGTH"
              placeholder="e.g., 'Generate a procurement plan based on low stock' or 'Analyze today's efficiency'"
              class="w-full pl-4 pr-12 py-3 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
              :disabled="loading"
            />
            <button 
              @click="handleAnalyze"
              :disabled="loading || !prompt.trim()"
              class="absolute right-2 top-2 p-1.5 bg-emerald-600 text-white rounded-md hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <Send class="w-4 h-4" />
            </button>
          </div>
          <p class="text-xs text-slate-400 mt-1 text-right">
            {{ prompt.length }}/{{ MAX_INPUT_LENGTH }} characters
          </p>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
// 导入 Vue 版图标
import { X, Sparkles, Send, Loader2, Trash2 } from 'lucide-vue-next';
// 导入 AI 服务（Vue 版）
import { generateAIAnalysis } from '../services/geminiService';

// 定义 Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  contextData: {
    type: Object,
    required: true
  }
});

// 定义事件
const emit = defineEmits(['update:modelValue']);

// 响应式状态
const prompt = ref('');
const response = ref(null);
const loading = ref(false);
const MAX_INPUT_LENGTH = 500; // 字数限制

// 关闭弹窗
const closeModal = () => {
  emit('update:modelValue', false);
};

// 清空对话
const handleClear = () => {
  prompt.value = '';
  response.value = null;
};

// AI 分析请求
const handleAnalyze = async () => {
  if (!prompt.value.trim()) return;

  loading.value = true;
  response.value = null;

  const contextString = JSON.stringify(props.contextData);
  try {
    const result = await generateAIAnalysis(contextString, prompt.value);
    response.value = result;
  } catch (error) {
    response.value = '⚠️ An error occurred while generating analysis. Please try again.';
  } finally {
    loading.value = false;
  }
};
</script>