<template>
  <div class="employee-page">
    <div class="head">
      <div>
        <h2>员工管理</h2>
        <p>管理可登录后台管理系统的员工账号，包括新增、编辑、删除和密码重置。</p>
      </div>
      <el-button type="success" @click="openDialog()">新增员工</el-button>
    </div>

    <el-card>
      <el-form :inline="true" :model="filters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" clearable placeholder="用户名/姓名/手机号" @keyup.enter.native="search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" style="width: 100px;">
            <el-option label="全部" value="all" />
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="employees" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />

        <el-table-column label="角色" min-width="200">
          <template slot-scope="scope">
            <el-tag
              v-for="rn in (scope.row.roleNames || [])"
              :key="`${scope.row.id}-${rn}`"
              size="mini"
              style="margin-right: 4px;"
            >{{ rn }}</el-tag>
            <span v-if="!scope.row.roleNames || scope.row.roleNames.length === 0" style="color:#999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-button
              type="text"
              style="color: #e11d48;"
              :disabled="scope.row.username === 'admin'"
              @click="removeEmployee(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :visible.sync="dialogVisible" :title="editingEmployee.id ? '编辑员工' : '新增员工'" width="560px">
      <el-form :model="editingEmployee" :rules="formRules" ref="empForm" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editingEmployee.username" :disabled="Boolean(editingEmployee.id)" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" :prop="editingEmployee.id ? '' : 'password'">
          <el-input v-model="editingEmployee.password" type="password" show-password :placeholder="editingEmployee.id ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editingEmployee.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editingEmployee.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editingEmployee.email" placeholder="邮箱" />
        </el-form-item>

        <el-form-item v-if="editingEmployee.id" label="状态">
          <el-select v-model="editingEmployee.status" style="width: 100%;">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEmployee">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listAdminEmployees, createAdminEmployee, updateAdminEmployee, deleteAdminEmployee } from '@/api/modules/settings';

export default {
  name: 'EmployeeManagement',
  data() {
    return {
      employees: [],
      page: 1,
      pageSize: 10,
      total: 0,
      filters: { keyword: '', status: 'all' },
      dialogVisible: false,
      editingEmployee: this.emptyForm(),
      formRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.loadEmployees();
  },
  methods: {
    emptyForm() {
      return { id: null, username: '', password: '', realName: '', phone: '', email: '', department: '', title: '', status: 'active' };
    },
    async loadEmployees() {
      try {
        const res = await listAdminEmployees({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.filters.keyword,
          status: this.filters.status
        });
        if (res && Array.isArray(res.list)) {
          this.employees = res.list;
          this.total = Number(res.total || 0);
        }
      } catch (error) { /* handled by interceptor */ }
    },
    onPageChange(p) { this.page = p; this.loadEmployees(); },
    onSizeChange(s) { this.pageSize = s; this.page = 1; this.loadEmployees(); },
    search() { this.page = 1; this.loadEmployees(); },
    resetFilters() { this.filters = { keyword: '', status: 'all' }; this.page = 1; this.loadEmployees(); },
    openDialog(row) {
      if (row) {
        this.editingEmployee = {
          id: row.id,
          username: row.username,
          password: '',
          realName: row.name || '',
          phone: row.phone || '',
          email: row.email || '',
          department: row.department || '',
          title: row.title || '',
          status: row.status || 'active'
        };
      } else {
        this.editingEmployee = this.emptyForm();
      }
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.empForm) this.$refs.empForm.clearValidate(); });
    },
    async saveEmployee() {
      try {
        await this.$refs.empForm.validate();
      } catch { return; }

      try {
        if (this.editingEmployee.id) {
          const data = { ...this.editingEmployee };
          delete data.id;
          delete data.username;
          if (!data.password) delete data.password;
          await updateAdminEmployee(this.editingEmployee.id, data);
        } else {
          await createAdminEmployee(this.editingEmployee);
        }
        this.dialogVisible = false;
        await this.loadEmployees();
        this.$message.success(this.editingEmployee.id ? '员工已更新' : '员工已创建');
      } catch (error) { /* handled by interceptor */ }
    },
    removeEmployee(row) {
      this.$confirm(`确认删除员工 "${row.name || row.username}" 吗？`, '确认删除', { type: 'warning' })
        .then(async () => {
          try {
            await deleteAdminEmployee(row.id);
            await this.loadEmployees();
            this.$message.success('员工已删除');
          } catch (error) { /* handled by interceptor */ }
        })
        .catch(() => {});
    }
  }
};
</script>

<style scoped>
.employee-page { min-height: 100%; }
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.filter-form { margin-bottom: 8px; }
</style>
