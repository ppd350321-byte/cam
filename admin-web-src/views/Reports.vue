<template>
  <div>
    <div class="head">
      <div>
        <h2>营运报表中心</h2>
        <p>数据更新时间: {{ updateTime }}</p>
      </div>
      <div>
        <el-button @click="toggleView">{{ viewMode === 'week' ? '本周' : '本月' }}</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="8"><el-card><div class="metric">本{{ viewMode === 'week' ? '周' : '月' }}总营收 <b>¥ {{ totalRevenue.toLocaleString() }}</b></div></el-card></el-col>
      <el-col :xs="24" :sm="8"><el-card><div class="metric">本{{ viewMode === 'week' ? '周' : '月' }}净利润 <b>¥ {{ totalProfit.toLocaleString() }}</b></div></el-card></el-col>
      <el-col :xs="24" :sm="8"><el-card><div class="metric">最佳员工 (绩效) <b>{{ bestEmployee }}</b></div></el-card></el-col>
    </el-row>

    <el-row v-loading="loadingSummary" :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :lg="12">
        <el-card>
          <div slot="header">盈亏分析趋势</div>
          <v-chart class="chart" :options="trendChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <div slot="header">产品销量占比</div>
          <v-chart class="chart" :options="salesPieOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-loading="loadingEmployees" style="margin-top: 16px;">
      <div slot="header">雇员绩效评价</div>
      <el-form :inline="true" :model="employeeFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="employeeFilters.keyword" clearable placeholder="姓名/岗位" @keyup.enter.native="searchEmployees" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="employeeFilters.role" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option v-for="role in roleOptions" :key="role" :label="role" :value="role" />
          </el-select>
        </el-form-item>
        <el-form-item label="最低评分">
          <el-input-number v-model="employeeFilters.minScore" :min="0" :max="100" :step="1" controls-position="right" style="width: 120px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchEmployees">查询</el-button>
          <el-button @click="resetEmployeeFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loadingEmployees" :data="employees" stripe>
        <el-table-column prop="name" label="员工姓名" width="110" />
        <el-table-column prop="role" label="岗位" width="90" />
        <el-table-column prop="tasks" label="完成任务数" width="110" />
        <el-table-column prop="rating" label="客户评价" width="90">
          <template slot-scope="scope">★ {{ scope.row.rating }}</template>
        </el-table-column>
        <el-table-column prop="score" label="综合评分" width="120">
          <template slot-scope="scope">
            <el-tag :type="Number(scope.row.score) >= 95 ? 'success' : (Number(scope.row.score) >= 90 ? 'primary' : 'info')">{{ scope.row.score }} 分</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          :current-page="employeePage"
          :page-size="employeePageSize"
          :total="employeeTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onEmployeePageChange"
          @size-change="onEmployeeSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getReportSummary, getReportEmployees } from '@/api/modules/reports';
import ECharts from 'vue-echarts/dist/vue-echarts.js';
import 'echarts/lib/chart/line';
import 'echarts/lib/chart/bar';
import 'echarts/lib/chart/pie';
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/grid';

export default {
  name: 'Reports',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      viewMode: 'week',
      summaryData: null,
      employees: [],
      employeeFilters: {
        keyword: '',
        role: 'all',
        minScore: 0
      },
      roleOptions: ['主厨', '炒锅', '炖汤', '主食', '配菜'],
      employeePage: 1,
      employeePageSize: 10,
      employeeTotal: 0,
      trendData: [],
      productSales: [],
      bestEmployee: '暂无',
      loadingSummary: false,
      loadingEmployees: false
    };
  },
  computed: {
    updateTime() { return new Date().toLocaleString(); },
    currentData() { return this.trendData; },
    totalRevenue() {
      if (this.summaryData && this.summaryData.totalRevenue) return this.summaryData.totalRevenue;
      return this.currentData.reduce((sum, item) => sum + item.营收, 0);
    },
    totalProfit() {
      if (this.summaryData && this.summaryData.totalProfit) return this.summaryData.totalProfit;
      return this.currentData.reduce((sum, item) => sum + item.利润, 0);
    },
    trendChartOption() {
      return {
        tooltip: { trigger: 'axis' },
        legend: { top: 0 },
        grid: { left: 40, right: 16, top: 40, bottom: 20 },
        xAxis: {
          type: 'category',
          data: this.currentData.map((item) => item.name),
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
            type: 'bar',
            barWidth: 22,
            itemStyle: { color: '#38bdf8' },
            data: this.currentData.map((item) => item.营收)
          },
          {
            name: '成本',
            type: 'bar',
            barWidth: 22,
            itemStyle: { color: '#f59e0b' },
            data: this.currentData.map((item) => item.成本)
          },
          {
            name: '利润',
            type: 'line',
            smooth: true,
            itemStyle: { color: '#22c55e' },
            lineStyle: { color: '#22c55e', width: 3 },
            data: this.currentData.map((item) => item.利润)
          }
        ]
      };
    },
    salesPieOption() {
      return {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          left: 8,
          top: 'middle'
        },
        series: [
          {
            name: '销量占比',
            type: 'pie',
            radius: ['38%', '68%'],
            center: ['64%', '50%'],
            label: { formatter: '{b}\n{d}%' },
            data: this.productSales.map((item) => ({ name: item.name, value: item.value })),
            itemStyle: {
              borderWidth: 2,
              borderColor: '#ffffff'
            }
          }
        ]
      };
    }
  },
  async created() {
    await this.loadSummary();
    await this.loadEmployees();
  },
  methods: {
    async loadSummary() {
      this.loadingSummary = true;
      try {
        const summary = await getReportSummary({ viewMode: this.viewMode });
        if (summary) {
          this.summaryData = summary;
          if (summary.bestEmployee) this.bestEmployee = summary.bestEmployee;
          if (Array.isArray(summary.trendData)) this.trendData = summary.trendData;
          if (Array.isArray(summary.productSales)) this.productSales = summary.productSales;
        }
      } catch (error) {} finally {
        this.loadingSummary = false;
      }
    },
    async loadEmployees() {
      this.loadingEmployees = true;
      try {
        const res = await getReportEmployees({
          viewMode: this.viewMode,
          page: this.employeePage,
          pageSize: this.employeePageSize,
          keyword: this.employeeFilters.keyword,
          role: this.employeeFilters.role,
          minScore: this.employeeFilters.minScore
        });
        if (res && Array.isArray(res.list)) {
          this.employees = res.list;
          this.employeeTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingEmployees = false;
      }
    },
    onEmployeePageChange(page) {
      this.employeePage = page;
      this.loadEmployees();
    },
    onEmployeeSizeChange(size) {
      this.employeePageSize = size;
      this.employeePage = 1;
      this.loadEmployees();
    },
    searchEmployees() {
      this.employeePage = 1;
      this.loadEmployees();
    },
    resetEmployeeFilters() {
      this.employeeFilters = { keyword: '', role: 'all', minScore: 0 };
      this.employeePage = 1;
      this.loadEmployees();
    },
    async toggleView() {
      this.viewMode = this.viewMode === 'week' ? 'month' : 'week';
      this.employeePage = 1;
      await this.loadSummary();
      await this.loadEmployees();
      this.$message.info(`已切换至本${this.viewMode === 'week' ? '周' : '月'}数据视图`);
    }
  }
};
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.metric { font-size: 14px; color: #475569; }
.metric b { display: block; margin-top: 6px; color: #0f172a; font-size: 24px; }
.chart { width: 100%; height: 320px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.filter-form { margin-bottom: 8px; }
</style>
