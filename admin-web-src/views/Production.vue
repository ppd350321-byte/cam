<template>
  <div>
    <div class="head">
      <div>
        <h2>今日生产调度看板</h2>
      </div>
    </div>

    <el-card v-loading="loadingTasks">
      <div slot="header">任务编排</div>
      <el-form :inline="true" :model="taskFilters" size="small" class="filter-form">
        <el-form-item label="">
          <el-input v-model="taskFilters.keyword" clearable placeholder="任务/编号/负责人" @keyup.enter.native="searchTasks" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="taskFilters.status" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="taskFilters.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchTasks">查询</el-button>
          <el-button @click="resetTaskFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <div v-for="task in tasks" :key="task.id" class="task-card">
        <div class="task-row">
          <div>
            <div><b>{{ task.name }}</b> </div>
            <div class="sub">负责人: {{ task.chef }} | {{ task.startTime }} - {{ task.endTime }}</div>
          </div>
          <div>
            <el-tag :type="task.status === '已完成' ? 'success' : (task.status === '进行中' ? 'primary' : 'info')">{{ task.status }}</el-tag>
            <el-button v-if="task.status === '待开始' && $hasPermission('production:task:start')" size="mini" type="primary" plain @click="taskAction(task, 'start')">开始</el-button>
            <el-button v-if="task.status === '进行中' && $hasPermission('production:task:complete')" size="mini" type="success" plain @click="taskAction(task, 'complete')">完成</el-button>
          </div>
        </div>
      </div>
      <div class="pager-wrap">
        <el-pagination
          :current-page="taskPage"
          :page-size="taskPageSize"
          :total="taskTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onTaskPageChange"
          @size-change="onTaskSizeChange"
        />
      </div>
    </el-card>

  </div>
</template>

<script>
import { listProductionTasks, updateTaskStatus } from '@/api/modules/production';

export default {
  name: 'Production',
  data() {
    return {
      tasks: [],
      taskPage: 1,
      taskPageSize: 10,
      taskTotal: 0,
      taskFilters: {
        keyword: '',
        status: 'all',
        date: ''
      },
      loadingTasks: false
    };
  },
  async created() {
    await this.loadTasks();
  },
  methods: {
    async loadTasks() {
      this.loadingTasks = true;
      try {
        const res = await listProductionTasks({
          page: this.taskPage,
          pageSize: this.taskPageSize,
          keyword: this.taskFilters.keyword,
          status: this.taskFilters.status,
          date: this.taskFilters.date || undefined
        });
        if (res && Array.isArray(res.list)) {
          this.tasks = res.list;
          this.taskTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingTasks = false;
      }
    },
    onTaskPageChange(page) {
      this.taskPage = page;
      this.loadTasks();
    },
    onTaskSizeChange(size) {
      this.taskPageSize = size;
      this.taskPage = 1;
      this.loadTasks();
    },
    searchTasks() {
      this.taskPage = 1;
      this.loadTasks();
    },
    resetTaskFilters() {
      this.taskFilters = { keyword: '', status: 'all', date: '' };
      this.taskPage = 1;
      this.loadTasks();
    },
    taskAction(task, action) {
      this.$confirm(`是否确定要将此任务标记为${action === 'start' ? '开始' : '完成'}？`, '确认操作', { type: 'warning' }).then(async () => {
        try {
          await updateTaskStatus(task.id, action);
        } catch (error) {}
        this.tasks = this.tasks.map((t) => {
          if (t.id !== task.id) return t;
          if (action === 'start') return { ...t, status: '进行中', progress: 10 };
          return { ...t, status: '已完成', progress: 100 };
        });
        await this.loadTasks();
        this.$message.success(`任务 ${task.id} 已${action === 'start' ? '开始' : '完成'}`);
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.task-card { border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.task-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.sub { color: #64748b; font-size: 12px; margin-top: 4px; }
.mono { font-family: monospace; color: #94a3b8; margin-left: 6px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.filter-form { margin-bottom: 8px; }
</style>
