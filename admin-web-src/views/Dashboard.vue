<template>
  <div>
    <el-alert
      v-if="!$hasPermission('dashboard:view')"
      type="warning"
      :closable="false"
      title="您暂无任何页面权限，请联系管理员为您分配角色和权限。"
      show-icon
      style="margin-bottom: 16px;"
    />
    <template v-if="$hasPermission('dashboard:view')">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in stats" :key="card.title">
        <el-card class="metric-card">
          <div class="metric-title">{{ card.title }}</div>
          <div class="metric-value">{{ card.value }}</div>
          <div :class="['metric-trend', card.positive ? 'up' : 'down']">{{ card.trend }} 较昨日</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="24">
        <el-card v-loading="chartLoading">
          <div slot="header">实时营收趋势</div>
          <v-chart class="chart" :options="revenueChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px;">
      <div slot="header" class="table-head">
        <span>最新订单</span>
        <el-button v-if="$hasPermission('orders:view')" type="text" @click="$router.push('/orders')">查看全部</el-button>
      </div>
      <el-form :inline="true" :model="orderFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="orderFilters.keyword" clearable placeholder="订单号/客户" @keyup.enter.native="onRecentFilterSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="orderFilters.status" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="待接单" value="pending_accept" />
            <el-option label="制作中" value="preparing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onRecentFilterSearch">查询</el-button>
          <el-button @click="resetRecentFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="recentOrders" stripe>
        <el-table-column prop="id" label="订单号" width="200" />
        <el-table-column prop="customerName" label="客户" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template slot-scope="scope">
            ¥ {{ Number(scope.row.totalAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatusLabel" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.orderStatus === 'completed' ? 'success' : (scope.row.orderStatus === 'preparing' ? 'primary' : 'warning')">{{ scope.row.orderStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="displayTime" label="时间" width="120" />
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          :current-page="recentOrderPage"
          :page-size="recentOrderPageSize"
          :total="recentOrderTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onRecentOrderPageChange"
          @size-change="onRecentOrderSizeChange"
        />
      </div>
    </el-card>
    </template>
  </div>
</template>

<script>
import { getDashboardOverview, getRecentOrders } from '@/api/modules/dashboard';
import ECharts from 'vue-echarts/dist/vue-echarts.js';
import 'echarts/lib/chart/line';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/grid';

export default {
  name: 'Dashboard',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      stats: [],
      revenueData: [],
      recentOrders: [],
      chartLoading: false,
      orderFilters: {
        keyword: '',
        status: 'all'
      },
      recentOrderPage: 1,
      recentOrderPageSize: 5,
      recentOrderTotal: 0,
      loading: false
    };
  },
  computed: {
    revenueChartOption() {
      return {
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 26, bottom: 28 },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.revenueData.map((item) => item.name),
          axisLine: { lineStyle: { color: '#94a3b8' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { formatter: '¥ {value}' },
          splitLine: { lineStyle: { color: '#e2e8f0' } }
        },
        series: [
          {
            name: '营收',
            type: 'line',
            smooth: true,
            data: this.revenueData.map((item) => item.value),
            lineStyle: { color: '#0ea5e9', width: 3 },
            itemStyle: { color: '#0ea5e9' },
            areaStyle: { color: 'rgba(14, 165, 233, 0.15)' }
          }
        ]
      };
    },
  },
  async created() {
    this.loading = true;
    this.chartLoading = true;
    try {
      const overview = await getDashboardOverview();
      if (overview && Array.isArray(overview.stats)) {
        this.stats = overview.stats;
      }
      if (overview && Array.isArray(overview.revenueData)) {
        this.revenueData = overview.revenueData;
      }
    } catch (error) {} finally {
      this.chartLoading = false;
    }
    await this.loadRecentOrders();
    this.loading = false;
  },
  methods: {
    async loadRecentOrders() {
      this.loading = true;
      try {
        const res = await getRecentOrders({
          page: this.recentOrderPage,
          pageSize: this.recentOrderPageSize,
          keyword: this.orderFilters.keyword,
          orderStatus: this.orderFilters.status
        });
        if (res && Array.isArray(res.list)) {
          this.recentOrders = res.list;
          this.recentOrderTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loading = false;
      }
    },
    onRecentOrderPageChange(page) {
      this.recentOrderPage = page;
      this.loadRecentOrders();
    },
    onRecentOrderSizeChange(size) {
      this.recentOrderPageSize = size;
      this.recentOrderPage = 1;
      this.loadRecentOrders();
    },
    onRecentFilterSearch() {
      this.recentOrderPage = 1;
      this.loadRecentOrders();
    },
    resetRecentFilters() {
      this.orderFilters = { keyword: '', status: 'all' };
      this.recentOrderPage = 1;
      this.loadRecentOrders();
    }
  }
};
</script>

<style scoped>
.metric-card { margin-bottom: 12px; }
.metric-title { color: #64748b; font-size: 13px; }
.metric-value { font-size: 26px; font-weight: 700; margin: 8px 0; color: #0f172a; }
.metric-trend { font-size: 13px; }
.up { color: #059669; }
.down { color: #dc2626; }
.table-head { display: flex; justify-content: space-between; align-items: center; }
.progress-label { font-size: 13px; color: #334155; margin-bottom: 4px; }
.chart { width: 100%; height: 280px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.filter-form { margin-bottom: 8px; }
</style>
