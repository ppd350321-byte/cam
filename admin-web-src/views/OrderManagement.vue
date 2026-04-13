<template>
  <div>
    <el-card>
      <el-form :inline="true" size="small" class="toolbar">
        <el-form-item>
          <el-input v-model="searchTerm" placeholder="搜索订单号/客户" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterPayment" placeholder="支付方式">
            <el-option label="全部方式" value="all" />
            <el-option label="预存支付" value="balance" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="未支付" value="unpaid" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待接单" name="pending_accept" />
        <el-tab-pane label="制作中" name="preparing" />
        <el-tab-pane label="待取餐" name="pending_pickup" />
        <el-tab-pane label="派送中" name="delivering" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>

      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="id" label="订单号" width="200" />
        <el-table-column prop="customerName" label="客户" width="130" />
        <el-table-column prop="itemsSummary" label="菜品明细" min-width="220" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template slot-scope="scope">¥ {{ Number(scope.row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paymentMethodLabel" label="支付方式" width="110" />
        <el-table-column prop="orderStatusLabel" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.orderStatus)">{{ scope.row.orderStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170">
          <template slot-scope="scope">{{ formatCreatedAt(scope.row.createdAt, scope.row.displayTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.orderStatus === 'pending_accept' && $hasPermission('orders:accept')" size="mini" type="success" @click="openAcceptDialog(scope.row)">接单</el-button>
            <el-button v-if="scope.row.orderStatus === 'pending_pickup' && $hasPermission('orders:pickup')" size="mini" type="warning" @click="changeStatus(scope.row, 'delivering', '开始派送')">开始派送</el-button>
            <el-button v-if="scope.row.orderStatus === 'delivering' && $hasPermission('orders:complete')" size="mini" type="success" @click="changeStatus(scope.row, 'completed', '标记完成')">标记完成</el-button>
            <el-button v-if="scope.row.orderStatus === 'pending_cancel' && $hasPermission('orders:accept')" size="mini" type="danger" @click="handleCancelApproval(scope.row, true)">同意取消</el-button>
            <el-button v-if="scope.row.orderStatus === 'pending_cancel' && $hasPermission('orders:accept')" size="mini" @click="handleCancelApproval(scope.row, false)">驳回取消</el-button>
            <el-button v-if="$hasPermission('orders:detail')" size="mini" @click="showDetails(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20, 50]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog :visible.sync="detailDialog" title="订单详情" width="640px">
      <el-descriptions v-if="selectedOrder.id" :column="2" border>
        <el-descriptions-item label="订单号">{{ selectedOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedOrder.orderStatusLabel }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selectedOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatCreatedAt(selectedOrder.createdAt, selectedOrder.displayTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ selectedOrder.paymentMethodLabel }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥ {{ Number(selectedOrder.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 12px;">
        <div style="margin-bottom: 6px; color: #64748b;">菜品明细</div>
        <el-tag v-for="item in detailItems" :key="item" style="margin-right: 8px; margin-bottom: 8px;">{{ item }}</el-tag>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="acceptDialog" title="接单 — 指定厨师" width="480px">
      <el-form label-width="80px">
        <el-form-item label="订单号">
          <span>{{ acceptOrder.id }}</span>
        </el-form-item>
        <el-form-item label="菜品">
          <span>{{ acceptOrder.itemsSummary }}</span>
        </el-form-item>
        <el-form-item label="指定厨师" required>
          <el-select v-model="acceptChefId" placeholder="请选择厨师" style="width: 100%;" filterable>
            <el-option v-for="c in chefList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="acceptDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!acceptChefId" @click="confirmAccept">确认接单</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listOrders, updateOrderStatus, approveCancelOrder, rejectCancelOrder, listChefs } from '@/api/modules/orders';

export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      activeTab: 'all',
      searchTerm: '',
      filterPayment: 'all',
      detailDialog: false,
      selectedOrder: {},
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      acceptDialog: false,
      acceptOrder: {},
      acceptChefId: null,
      chefList: []
    };
  },
  computed: {
    detailItems() {
      return Array.isArray(this.selectedOrder.items)
        ? this.selectedOrder.items.map((item) => `${item.name} x${item.quantity}`)
        : [];
    }
  },
  watch: {
    activeTab() {
      this.page = 1;
      this.loadOrders();
    },
    searchTerm() {
      this.page = 1;
      this.loadOrders();
    },
    filterPayment() {
      this.page = 1;
      this.loadOrders();
    }
  },
  async created() {
    await this.loadOrders();
  },
  methods: {
    async loadOrders() {
      this.loading = true;
      try {
        const res = await listOrders({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchTerm || undefined,
          orderStatus: this.activeTab === 'all' ? undefined : this.activeTab,
          paymentMethod: this.filterPayment === 'all' ? undefined : this.filterPayment
        });
        if (res && Array.isArray(res.list)) {
          this.orders = res.list;
          this.total = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loading = false;
      }
    },
    onPageChange(value) {
      this.page = value;
      this.loadOrders();
    },
    onSizeChange(value) {
      this.pageSize = value;
      this.page = 1;
      this.loadOrders();
    },
    statusType(orderStatus) {
      if (orderStatus === 'completed') return 'success';
      if (orderStatus === 'preparing') return 'primary';
      if (orderStatus === 'pending_pickup') return 'warning';
      if (orderStatus === 'delivering') return '';
      if (orderStatus === 'pending_accept') return 'info';
      if (orderStatus === 'pending_cancel') return 'danger';
      if (orderStatus === 'cancelled') return 'info';
      return '';
    },
    formatCreatedAt(createdAt, displayTime) {
      if (displayTime && !createdAt) return displayTime;
      if (!createdAt) return '-';
      const date = new Date(createdAt);
      return Number.isNaN(date.getTime()) ? createdAt : date.toLocaleString();
    },
    showDetails(order) {
      this.selectedOrder = order;
      this.detailDialog = true;
    },
    async openAcceptDialog(order) {
      this.acceptOrder = order;
      this.acceptChefId = null;
      try {
        this.chefList = await listChefs() || [];
      } catch (error) {
        this.chefList = [];
      }
      this.acceptDialog = true;
    },
    async confirmAccept() {
      try {
        await updateOrderStatus(this.acceptOrder.id, 'preparing', { chefId: this.acceptChefId });
      } catch (error) {
        this.$message.error('接单失败');
        return;
      }
      this.acceptDialog = false;
      await this.loadOrders();
      this.$message.success(`订单 ${this.acceptOrder.id} 已接单，任务已同步到生产调度`);
    },
    changeStatus(order, status, title) {
      this.$confirm(`您确定要对订单 ${order.id} 执行${title}操作吗？`, '确认操作', { type: 'warning' }).then(async () => {
        try {
          await updateOrderStatus(order.id, status);
        } catch (error) {
          await this.loadOrders();
          return;
        }
        await this.loadOrders();
        this.$message.success(`订单 ${order.id} 状态已更新`);
      }).catch(() => {});
    },
    handleCancelApproval(order, approve) {
      const action = approve ? '同意取消' : '驳回取消';
      this.$confirm(`您确定要对订单 ${order.id} 执行「${action}」操作吗？`, '确认操作', { type: 'warning' }).then(async () => {
        try {
          if (approve) {
            await approveCancelOrder(order.id);
          } else {
            await rejectCancelOrder(order.id);
          }
        } catch (error) {}
        await this.loadOrders();
        this.$message.success(`订单 ${order.id} 已${action}`);
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.toolbar { margin-bottom: 10px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
