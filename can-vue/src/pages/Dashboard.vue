<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-slate-800">Operational Overview</h2>
      <div class="text-sm text-slate-500">Last updated: Just now</div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard
        label="Total Revenue (Today)"
        value="$2,450.00"
        trend="12%"
        trendUp
        :icon="DollarSign"
        color="emerald"
      />
      <StatCard
        label="Active Orders"
        :value="MOCK_ORDERS.filter(o => o.status !== 'Completed').length"
        trend="5%"
        trendUp
        :icon="ShoppingBag"
        color="blue"
      />
      <StatCard
        label="VIP Visits"
        value="128"
        trend="2%"
        :trendUp="false"
        :icon="Users"
        color="purple"
      />
      <StatCard
        label="Avg Prep Time"
        value="8.5 min"
        trend="0.5 min"
        trendUp
        :icon="Clock"
        color="orange"
      />
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Revenue Trend (Weekly)</h3>
        <div class="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart :data="SALES_DATA">
              <CartesianGrid strokeDasharray="3 3" :vertical="false" stroke="#e2e8f0" />
              <XAxis dataKey="date" :axisLine="false" :tickLine="false" tick={{ fill: '#64748b' }} />
              <YAxis :axisLine="false" :tickLine="false" tick={{ fill: '#64748b' }} />
              <Tooltip
                contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
              />
              <Line
                type="monotone"
                dataKey="revenue"
                stroke="#10b981"
                strokeWidth={3}
                dot={{ r: 4, fill: '#10b981', strokeWidth: 2, stroke: '#fff' }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Orders Volume</h3>
        <div class="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart :data="SALES_DATA">
              <CartesianGrid strokeDasharray="3 3" :vertical="false" stroke="#e2e8f0" />
              <XAxis dataKey="date" :axisLine="false" :tickLine="false" tick={{ fill: '#64748b' }} />
              <YAxis :axisLine="false" :tickLine="false" tick={{ fill: '#64748b' }} />
              <Tooltip cursor={{ fill: '#f1f5f9' }} contentStyle={{ borderRadius: '8px', border: 'none' }} />
              <Bar dataKey="orders" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={32} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-bold text-slate-800">Recent Orders</h3>
        <button class="text-emerald-600 text-sm font-medium hover:text-emerald-700 flex items-center">
          View All <ArrowRight class="w-4 h-4 ml-1" />
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-slate-100 text-slate-500 text-sm">
              <th class="pb-3 font-medium">Order ID</th>
              <th class="pb-3 font-medium">Customer</th>
              <th class="pb-3 font-medium">Items</th>
              <th class="pb-3 font-medium">Amount</th>
              <th class="pb-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody class="text-sm">
            <tr
              v-for="order in MOCK_ORDERS"
              :key="order.id"
              class="border-b border-slate-50 last:border-0 hover:bg-slate-50"
            >
              <td class="py-4 font-medium text-slate-900">{{ order.id }}</td>
              <td class="py-4 text-slate-600">{{ order.userName }}</td>
              <td class="py-4 text-slate-600">
                {{ order.items.map(i => `${i.quantity}x ${i.name}`).join(', ') }}
              </td>
              <td class="py-4 text-slate-900 font-medium">${order.totalAmount}</td>
              <td class="py-4">
                <span
                  :class="`px-2 py-1 rounded-full text-xs font-medium ${
                    order.status === 'Completed'
                      ? 'bg-emerald-100 text-emerald-700'
                      : order.status === 'Preparing'
                      ? 'bg-orange-100 text-orange-700'
                      : order.status === 'Pending'
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-slate-100 text-slate-700'
                  }`"
                >
                  {{ order.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  DollarSign,
  ShoppingBag,
  Users,
  Clock,
  ArrowRight,
} from 'lucide-vue-next';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
} from 'recharts';
import StatCard from '@/components/StatCard.vue';
import { SALES_DATA, MOCK_ORDERS } from '@/constants';
</script>