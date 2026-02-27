<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-2xl font-bold text-slate-800">Supply Chain & Inventory</h2>
      <p class="text-slate-500 text-sm">Manage raw materials, procurement plans, and supplier relationships.</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="p-3 bg-blue-100 text-blue-600 rounded-lg">
          <Package class="w-6 h-6" />
        </div>
        <div>
          <p class="text-sm text-slate-500">Total SKUs</p>
          <h3 class="text-2xl font-bold text-slate-900">48</h3>
        </div>
      </div>
      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="p-3 bg-orange-100 text-orange-600 rounded-lg">
          <AlertTriangle class="w-6 h-6" />
        </div>
        <div>
          <p class="text-sm text-slate-500">Low Stock Items</p>
          <h3 class="text-2xl font-bold text-slate-900">3</h3>
        </div>
      </div>
      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="p-3 bg-emerald-100 text-emerald-600 rounded-lg">
          <Truck class="w-6 h-6" />
        </div>
        <div>
          <p class="text-sm text-slate-500">Pending Deliveries</p>
          <h3 class="text-2xl font-bold text-slate-900">2</h3>
        </div>
      </div>
      <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="p-3 bg-purple-100 text-purple-600 rounded-lg">
          <TrendingDown class="w-6 h-6" />
        </div>
        <div>
          <p class="text-sm text-slate-500">Waste Rate</p>
          <h3 class="text-2xl font-bold text-slate-900">1.2%</h3>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Inventory Table -->
      <div class="lg:col-span-2 bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h3 class="font-bold text-slate-800">Current Inventory Levels</h3>
          <button class="text-sm text-emerald-600 font-medium hover:underline">Download Report</button>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead class="bg-slate-50 text-slate-500">
              <tr>
                <th class="px-6 py-4 font-medium">Item Name</th>
                <th class="px-6 py-4 font-medium">Category</th>
                <th class="px-6 py-4 font-medium">Stock</th>
                <th class="px-6 py-4 font-medium">Supplier</th>
                <th class="px-6 py-4 font-medium">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="item in MOCK_INVENTORY" :key="item.id" class="hover:bg-slate-50">
                <td class="px-6 py-4 font-medium text-slate-900">{{ item.name }}</td>
                <td class="px-6 py-4 text-slate-500">{{ item.category }}</td>
                <td class="px-6 py-4 text-slate-700">
                  {{ item.currentStock }} <span class="text-slate-400 text-xs">{{ item.unit }}</span>
                </td>
                <td class="px-6 py-4 text-slate-500">{{ item.supplier }}</td>
                <td class="px-6 py-4">
                  <span
                    v-if="item.currentStock < item.reorderLevel"
                    class="text-red-600 bg-red-50 px-2 py-1 rounded text-xs font-bold border border-red-100"
                  >
                    Restock
                  </span>
                  <span
                    v-else
                    class="text-emerald-600 bg-emerald-50 px-2 py-1 rounded text-xs font-bold border border-emerald-100"
                  >
                    OK
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Stock Levels Chart -->
      <div class="bg-white rounded-xl border border-slate-100 shadow-sm p-6">
        <h3 class="font-bold text-slate-800 mb-6">Stock vs Reorder Level</h3>
        <div class="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart :data="MOCK_INVENTORY" layout="vertical">
              <CartesianGrid strokeDasharray="3 3" :horizontal="true" :vertical="false" stroke="#e2e8f0" />
              <XAxis type="number" hide />
              <YAxis dataKey="name" type="category" width={80} tick={{ fontSize: 12, fill: '#64748b' }} />
              <Tooltip />
              <Bar dataKey="currentStock" name="Current" fill="#10b981" barSize={10} radius={[0, 4, 4, 0]} />
              <Bar dataKey="reorderLevel" name="Min Level" fill="#f43f5e" barSize={10} radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div class="mt-4 p-4 bg-slate-50 rounded-lg border border-slate-100">
          <h4 class="text-sm font-bold text-slate-700 mb-2">Automated Procurement Plan</h4>
          <p class="text-xs text-slate-500 leading-relaxed">
            Based on current consumption rates, the system recommends ordering
            <strong> 10kg of Pork Belly</strong> and <strong>5kg of Bok Choy</strong> by 2:00 PM today to ensure supply for tomorrow's lunch service.
          </p>
          <button class="mt-3 w-full py-2 bg-emerald-600 text-white text-xs font-bold rounded hover:bg-emerald-700 transition-colors">
            Approve & Send to Suppliers
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Package, Truck, AlertTriangle, TrendingDown } from 'lucide-vue-next';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { MOCK_INVENTORY } from '@/constants';
</script>