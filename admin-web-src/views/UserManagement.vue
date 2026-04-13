<template>
  <div>
    <el-card>
      <el-form :inline="true" size="small" class="toolbar">
        <el-form-item>
          <el-input v-model="searchTerm" placeholder="搜索姓名/手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterType" placeholder="用户类型">
            <el-option label="全部类型" value="all" />
            <el-option label="普通用户" :value="false" />
            <el-option label="VIP用户" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="账号状态">
            <el-option label="全部状态" value="all" />
            <el-option label="正常" value="active" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="id" label="用户ID" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userTypeLabel" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isVip ? 'warning' : 'info'">{{ scope.row.isVip ? 'VIP' : '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">{{ scope.row.status === 'active' ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="预存余额" width="120">
          <template slot-scope="scope">¥ {{ Number(scope.row.balance).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column prop="lastVisitAt" label="最近到店" />
        <el-table-column label="操作" width="270" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="$hasPermission('users:recharge')" size="mini" @click="openRecharge(scope.row)">充值</el-button>
            <el-button v-if="$hasPermission('users:points')" size="mini" type="primary" plain @click="openPoints(scope.row)">积分</el-button>
            <el-button v-if="$hasPermission('users:edit')" size="mini" type="warning" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button v-if="$hasPermission('users:status')" size="mini" :type="scope.row.status === 'active' ? 'danger' : 'success'" plain @click="toggleStatus(scope.row)">{{ scope.row.status === 'active' ? '禁用' : '启用' }}</el-button>
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

    <el-dialog :visible.sync="rechargeDialog" :title="`为 ${selectedUser.name || ''} 充值`" width="420px">
      <el-input v-model="amount" placeholder="输入充值金额" type="number" />
      <span slot="footer">
        <el-button @click="rechargeDialog = false">取消</el-button>
        <el-button type="success" @click="doRecharge">确认充值</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="pointsDialog" :title="`发放积分给 ${selectedUser.name || ''}`" width="420px">
      <el-input v-model="points" placeholder="输入积分数量" type="number" />
      <span slot="footer">
        <el-button @click="pointsDialog = false">取消</el-button>
        <el-button type="primary" @click="doPoints">确认发放</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="editDialog" title="编辑用户资料" width="500px">
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="姓名"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.isVip">
            <el-option label="普通" :value="false" />
            <el-option label="VIP" :value="true" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="doEdit">保存更改</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listUsers, updateUser, rechargeUser, addUserPoints, toggleUserStatus } from '@/api/modules/users';

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      searchTerm: '',
      filterType: 'all',
      filterStatus: 'all',
      selectedUser: {},
      rechargeDialog: false,
      pointsDialog: false,
      editDialog: false,
      amount: '',
      points: '',
      editForm: { name: '', phone: '', isVip: false },
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false
    };
  },
  watch: {
    searchTerm() {
      this.page = 1;
      this.loadUsers();
    },
    filterType() {
      this.page = 1;
      this.loadUsers();
    },
    filterStatus() {
      this.page = 1;
      this.loadUsers();
    }
  },
  async created() {
    await this.loadUsers();
  },
  methods: {
    async loadUsers() {
      this.loading = true;
      try {
        const res = await listUsers({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchTerm || undefined,
          isVip: this.filterType === 'all' ? undefined : this.filterType,
          status: this.filterStatus === 'all' ? undefined : this.filterStatus
        });
        if (res && Array.isArray(res.list)) {
          this.users = res.list;
          this.total = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loading = false;
      }
    },
    onPageChange(value) {
      this.page = value;
      this.loadUsers();
    },
    onSizeChange(value) {
      this.pageSize = value;
      this.page = 1;
      this.loadUsers();
    },
    openRecharge(user) {
      this.selectedUser = user;
      this.amount = '';
      this.rechargeDialog = true;
    },
    async doRecharge() {
      const value = Number(this.amount);
      if (!value) return this.$message.error('请输入有效金额');
      try {
        await rechargeUser(this.selectedUser.id, value);
      } catch (error) {}
      this.users = this.users.map((u) => (u.id === this.selectedUser.id ? { ...u, balance: Number(u.balance) + value } : u));
      this.$message.success(`成功为 ${this.selectedUser.name} 充值 ¥${value}`);
      this.rechargeDialog = false;
    },
    openPoints(user) {
      this.selectedUser = user;
      this.points = '';
      this.pointsDialog = true;
    },
    async doPoints() {
      const value = Number(this.points);
      if (!value) return this.$message.error('请输入有效积分');
      try {
        await addUserPoints(this.selectedUser.id, value);
      } catch (error) {}
      this.users = this.users.map((u) => (u.id === this.selectedUser.id ? { ...u, points: Number(u.points) + value } : u));
      this.$message.success(`成功为 ${this.selectedUser.name} 发放 ${value} 积分`);
      this.pointsDialog = false;
    },
    openEdit(user) {
      this.selectedUser = user;
      this.editForm = { name: user.name, phone: user.phone, isVip: !!user.isVip };
      this.editDialog = true;
    },
    async doEdit() {
      if (!this.editForm.name || !this.editForm.phone) return this.$message.error('请填写完整信息');
      if (!/^1[3-9]\d{9}$/.test(this.editForm.phone)) return this.$message.error('请输入正确的手机号');
      try {
        await updateUser(this.selectedUser.id, this.editForm);
      } catch (error) {}
      this.users = this.users.map((u) => (u.id === this.selectedUser.id ? { ...u, ...this.editForm } : u));
      this.$message.success(`用户 ${this.editForm.name} 资料已更新`);
      this.editDialog = false;
    },
    toggleStatus(user) {
      const nextStatus = user.status === 'active' ? 'disabled' : 'active';
      this.$confirm(`您确定要将用户 ${user.id} 状态改为 ${nextStatus === 'active' ? '启用' : '禁用'} 吗？`, '确认操作', {
        type: 'warning'
      }).then(async () => {
        try {
          await toggleUserStatus(user.id, nextStatus);
        } catch (error) {}
        this.users = this.users.map((u) => (u.id === user.id ? { ...u, status: nextStatus } : u));
        this.$message.success(`用户已${nextStatus === 'active' ? '启用' : '禁用'}`);
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.toolbar { margin-bottom: 10px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
