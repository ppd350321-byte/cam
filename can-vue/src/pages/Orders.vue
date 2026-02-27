<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-2xl font-bold text-slate-800">Order Management</h2>
        <p class="text-slate-500 text-sm">Monitor and process incoming orders.</p>
      </div>
      <div class="flex gap-2">
        <button class="bg-emerald-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-emerald-700 shadow-sm transition-colors">
          Create Manual Order
        </button>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
      <!-- Toolbar -->
      <div class="p-4 border-b border-slate-100 flex flex-col md:flex-row gap-4 justify-between items-center bg-slate-50/50">
        <div class="relative w-full md:w-64">
          <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search ID or Customer..."
            v-model="searchTerm"
            class="w-full pl-9 pr-4 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <div class="flex items-center gap-2 overflow-x-auto w-full md:w-auto pb-2 md:pb-0">
          <Filter class="w-4 h-4 text-slate-400 mr-1" />
          <button
            v-for="status in ['All', OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.COMPLETED]"
            :key="status"
            @click="filterStatus = status"
            :class="`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${
              filterStatus === status
                ? 'bg-emerald-600 text-white'
                : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
            }`"
          >
            {{ status }}
          </button>
        </div>
      </div>

      <!-- Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-500">
            <tr>
              <th class="px-6 py-4 font-medium">Order ID</th>
              <th class="px-6 py-4 font-medium">Time</th>
              <th class="px-6 py-4 font-medium">Customer</th>
              <th class="px-6 py-4 font-medium">Total</th>
              <th class="px-6 py-4 font-medium">Payment</th>
              <th class="px-6 py-4 font-medium">Status</th>
              <th class="px-6 py-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="order in filteredOrders"
              :key="order.id"
              class="hover:bg-slate-50 transition-colors"
            >
              <td class="px-6 py-4 font-medium text-emerald-700">{{ order.id }}</td>
              <td class="px-6 py-4 text-slate-500">
                {{ formatTime(order.createdAt) }}
              </td>
              <td class="px-6 py-4">
                <div class="text-slate-900 font-medium">{{ order.userName }}</div>
                <div class="text-slate-400 text-xs">{{ order.items.length }} items</div>
              </td>
              <td class="px-6 py-4 font-medium text-slate-900">${order.totalAmount.toFixed(2)}</td>
              <td class="px-6 py-4 text-slate-600">{{ order.paymentMethod }}</td>
              <td class="px-6 py-4">
                <span
                  :class="`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${
                    order.status === OrderStatus.COMPLETED
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                      : order.status === OrderStatus.PREPARING
                      ? 'bg-amber-50 text-amber-700 border-amber-100'
                      : order.status === OrderStatus.PENDING
                      ? 'bg-blue-50 text-blue-700 border-blue-100'
                      : 'bg-slate-50 text-slate-600 border-slate-200'
                  }`"
                >
                  <CheckCircle v-if="order.status === OrderStatus.COMPLETED" class="w-3 h-3" />
                  <Clock v-if="order.status === OrderStatus.PREPARING" class="w-3 h-3" />
                  {{ order.status }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <button class="text-slate-400 hover:text-emerald-600 transition-colors">
                  <MoreHorizontal class="w-5 h-5 ml-auto" />
                </button>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-slate-500">
                No orders found matching your criteria.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { Search, Filter, CheckCircle, Clock, MoreHorizontal } from 'lucide-vue-next';
import { MOCK_ORDERS, OrderStatus } from '@/constants';

const searchTerm = ref('');
const filterStatus = ref('All');

const filteredOrders = computed(() => {
  return MOCK_ORDERS.filter((order) => {
    const matchesStatus = filterStatus.value === 'All' || order.status === filterStatus.value;
    const matchesSearch =
      order.id.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      order.userName.toLowerCase().includes(searchTerm.value.toLowerCase());
    return matchesStatus && matchesSearch;
  });
});

const formatTime = (dateString: string) => {
  return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};
</script>