<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold text-slate-800">User Management</h2>
        <p class="text-slate-500 text-sm">VIP accounts, loyalty points, and prepaid balances.</p>
      </div>
    </div>

    <!-- VIP Tiers Summary -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div
        v-for="tier in [UserTier.PLATINUM, UserTier.GOLD, UserTier.SILVER, UserTier.REGULAR]"
        :key="tier"
        class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm text-center"
      >
        <div
          :class="`mx-auto w-10 h-10 rounded-full flex items-center justify-center mb-2 ${
            tier === UserTier.PLATINUM
              ? 'bg-slate-800 text-white'
              : tier === UserTier.GOLD
              ? 'bg-yellow-100 text-yellow-600'
              : tier === UserTier.SILVER
              ? 'bg-slate-100 text-slate-500'
              : 'bg-emerald-50 text-emerald-600'
          }`"
        >
          <Star class="w-5 h-5 fill-current" />
        </div>
        <div class="font-bold text-slate-800">{{ tier }}</div>
        <div class="text-xs text-slate-400">24 Active Users</div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
        <div class="relative w-64">
          <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Find user..."
            class="w-full pl-9 pr-4 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>
        <button class="text-emerald-600 text-sm font-bold">Export List</button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-500">
            <tr>
              <th class="px-6 py-4 font-medium">User Info</th>
              <th class="px-6 py-4 font-medium">Tier</th>
              <th class="px-6 py-4 font-medium">Balance (Prepaid)</th>
              <th class="px-6 py-4 font-medium">Points</th>
              <th class="px-6 py-4 font-medium">Last Visit</th>
              <th class="px-6 py-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="user in MOCK_USERS" :key="user.id" class="hover:bg-slate-50">
              <td class="px-6 py-4">
                <div class="font-medium text-slate-900">{{ user.name }}</div>
                <div class="text-xs text-slate-500">{{ user.phone }}</div>
              </td>
              <td class="px-6 py-4">
                <span
                  :class="`px-2 py-1 rounded text-xs font-bold ${
                    user.tier === UserTier.PLATINUM
                      ? 'bg-slate-800 text-white'
                      : user.tier === UserTier.GOLD
                      ? 'bg-yellow-100 text-yellow-700'
                      : user.tier === UserTier.SILVER
                      ? 'bg-slate-100 text-slate-700'
                      : 'bg-emerald-50 text-emerald-700'
                  }`"
                >
                  {{ user.tier }}
                </span>
              </td>
              <td class="px-6 py-4 font-mono font-medium text-emerald-700">
                ¥{{ user.balance.toFixed(2) }}
              </td>
              <td class="px-6 py-4 text-slate-600">
                <div class="flex items-center gap-1">
                  <Gift class="w-3 h-3 text-purple-500" />
                  {{ user.points }}
                </div>
              </td>
              <td class="px-6 py-4 text-slate-500">
                {{ user.lastVisit }}
              </td>
              <td class="px-6 py-4 text-right">
                <button class="text-emerald-600 hover:text-emerald-700 font-medium text-xs mr-3">Top Up</button>
                <button class="text-slate-400 hover:text-slate-600 font-medium text-xs">Edit</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Star, CreditCard, Gift, Search } from 'lucide-vue-next';
import { MOCK_USERS, UserTier } from '@/constants';
</script>