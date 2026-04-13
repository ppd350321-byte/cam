<template>
  <div v-if="$isAdmin()" class="permission-page">
    <div class="head">
      <div>
        <h2>权限配置</h2>
        <p>管理员可维护角色，并按菜单/按钮层级为角色授权。权限项为系统内置，不允许新增。</p>
      </div>
      <el-button v-if="$hasPermission('permission-config:role:create')" type="success" @click="openRoleDialog()">创建角色</el-button>
    </div>

    <el-card>
      <div slot="header">角色列表</div>
      <el-form :inline="true" :model="roleFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="roleFilters.keyword" clearable placeholder="角色名/编码/说明" @keyup.enter.native="searchRoles" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRoles">查询</el-button>
          <el-button @click="resetRoleFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loadingRoles" :data="roles" stripe>
        <el-table-column prop="name" label="角色" min-width="160" />
        <el-table-column prop="code" label="编码" min-width="160" />
        <el-table-column prop="description" label="角色说明" min-width="220" />
        <el-table-column label="权限数" width="90">
          <template slot-scope="scope">{{ scope.row.permissionCodes && scope.row.permissionCodes.length ? scope.row.permissionCodes.length : 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="$hasPermission('permission-config:role:grant') && !scope.row.isSystem"
              type="text"
              @click="openGrantDialog(scope.row)"
            >
              授权
            </el-button>
            <el-button
              v-if="$hasPermission('permission-config:role:update') && !scope.row.isSystem"
              type="text"
              @click="openRoleDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="$hasPermission('permission-config:role:delete') && !scope.row.isSystem"
              type="text"
              style="color: #e11d48;"
              @click="removeRole(scope.row)"
            >
              删除
            </el-button>
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

    <el-card style="margin-top: 16px;">
      <div slot="header">员工配置角色</div>
      <el-form :inline="true" :model="employeeFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="employeeFilters.keyword" clearable placeholder="员工ID/姓名/手机号" @keyup.enter.native="searchEmployees" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="employeeFilters.status" style="width: 100px;">
            <el-option label="全部" value="all" />
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchEmployees">查询</el-button>
          <el-button @click="resetEmployeeFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loadingEmployees" :data="employees" stripe>
        <el-table-column prop="id" label="员工ID" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="当前角色" min-width="220">
          <template slot-scope="scope">
            <el-tag
              v-for="roleName in getRoleNames(scope.row.roleIds)"
              :key="`${scope.row.id}-${roleName}`"
              size="mini"
              style="margin-right: 6px; margin-bottom: 4px;"
            >
              {{ roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">{{ scope.row.status === 'active' ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="$hasPermission('permission-config:role:grant')"
              size="mini"
              type="primary"
              plain
              @click="openEmployeeRoleDialog(scope.row)"
            >
              配置角色
            </el-button>
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

    <el-dialog :visible.sync="roleDialog" :title="editingRole.id ? '编辑角色' : '创建角色'" width="520px">
      <el-form :model="editingRole" label-width="90px">
        <el-form-item label="角色编码">
          <el-input v-model="editingRole.code" :disabled="Boolean(editingRole.id)" placeholder="如 OPERATOR" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="editingRole.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="editingRole.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="success" @click="saveRole">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="grantDialog" :title="`角色授权 - ${grantRole.name || ''}`" width="760px">
      <el-alert
        v-if="grantRole.isSystem"
        type="info"
        :closable="false"
        title="系统内置超级管理员默认拥有全部权限，不可修改授权。"
        style="margin-bottom: 10px;"
      />
      <el-tree
        ref="grantTree"
        node-key="code"
        show-checkbox
        :data="permissionTree"
        :props="treeProps"
        :default-expand-all="true"
        :check-strictly="false"
        :disabled="grantRole.isSystem || !$hasPermission('permission-config:role:grant')"
      />
      <span slot="footer">
        <el-button @click="grantDialog = false">取消</el-button>
        <el-button
          v-if="$hasPermission('permission-config:role:grant')"
          type="primary"
          :disabled="grantRole.isSystem"
          @click="saveGrant"
        >
          保存权限
        </el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="employeeRoleDialog" :title="`配置角色 - ${editingEmployee.name || ''}`" width="560px">
      <el-form label-width="90px">
        <el-form-item label="员工ID">{{ editingEmployee.id || '-' }}</el-form-item>
        <el-form-item label="角色选择">
          <el-select v-model="editingEmployee.roleIds" multiple collapse-tags style="width: 100%;" placeholder="可多选角色">
            <el-option v-for="role in allRoleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="employeeRoleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEmployeeRoles">保存角色</el-button>
      </span>
    </el-dialog>
  </div>
  <el-card v-else>
    <el-empty description="仅管理员可访问权限配置模块" />
  </el-card>
</template>

<script>
import { listRoles, addRole, updateRole, deleteRole, saveRolePermissions, listAdminEmployees, configureAdminRoles } from '@/api/modules/settings';
import { PERMISSION_TREE } from '@/constants/permissions';

export default {
  name: 'PermissionConfig',
  data() {
    return {
      roles: [],
      allRoleOptions: [],
      page: 1,
      pageSize: 10,
      total: 0,
      roleFilters: {
        keyword: ''
      },
      employees: [],
      employeePage: 1,
      employeePageSize: 10,
      employeeTotal: 0,
      employeeFilters: {
        keyword: '',
        status: 'all'
      },
      permissionTree: [],
      treeProps: {
        label: 'label',
        children: 'children'
      },
      roleDialog: false,
      editingRole: { id: null, code: '', name: '', description: '' },
      grantDialog: false,
      grantRole: { id: null, name: '', isSystem: false },
      employeeRoleDialog: false,
      editingEmployee: { id: '', name: '', roleIds: [] },
      loadingRoles: false,
      loadingEmployees: false
    };
  },
  async created() {
    this.loadPermissionTree();
    await this.loadRoles();
    await this.loadAllRoleOptions();
    await this.loadEmployees();
  },
  methods: {
    loadPermissionTree() {
      // 使用前端常量权限树（包含正确的中文标签），过滤掉权限配置节点
      this.permissionTree = PERMISSION_TREE.filter((node) => node.code !== 'permission-config:view');
    },
    async loadRoles() {
      this.loadingRoles = true;
      try {
        const res = await listRoles({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.roleFilters.keyword
        });
        if (res && Array.isArray(res.list)) {
          this.roles = res.list;
          this.total = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingRoles = false;
      }
    },
    async loadAllRoleOptions() {
      try {
        const res = await listRoles({ page: 1, pageSize: 999 });
        if (res && Array.isArray(res.list)) this.allRoleOptions = res.list;
      } catch (error) {}
    },
    async loadEmployees() {
      this.loadingEmployees = true;
      try {
        const res = await listAdminEmployees({
          page: this.employeePage,
          pageSize: this.employeePageSize,
          keyword: this.employeeFilters.keyword,
          status: this.employeeFilters.status
        });
        if (res && Array.isArray(res.list)) {
          this.employees = res.list;
          this.employeeTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingEmployees = false;
      }
    },
    getRoleNames(roleIds = []) {
      const idSet = new Set(roleIds || []);
      return this.allRoleOptions.filter((role) => idSet.has(role.id)).map((role) => role.name);
    },
    onPageChange(page) {
      this.page = page;
      this.loadRoles();
    },
    onSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadRoles();
    },
    searchRoles() {
      this.page = 1;
      this.loadRoles();
    },
    resetRoleFilters() {
      this.roleFilters = { keyword: '' };
      this.page = 1;
      this.loadRoles();
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
      this.employeeFilters = { keyword: '', status: 'all' };
      this.employeePage = 1;
      this.loadEmployees();
    },
    openEmployeeRoleDialog(user) {
      this.editingEmployee = {
        id: user.id,
        name: user.name,
        roleIds: Array.isArray(user.roleIds) ? [...user.roleIds] : []
      };
      this.employeeRoleDialog = true;
    },
    openGrantDialog(role) {
      this.grantRole = role || { id: null, name: '', isSystem: false };
      this.grantDialog = true;
      this.$nextTick(() => {
        if (!this.$refs.grantTree || !role) return;
        // 只设置叶子节点的 key，父节点由 el-tree 的 check-strictly=false 自动推断
        const parentCodes = new Set(
          this.permissionTree.filter((n) => n.children && n.children.length).map((n) => n.code)
        );
        const leafCodes = (role.permissionCodes || []).filter((c) => !parentCodes.has(c));
        this.$refs.grantTree.setCheckedKeys(leafCodes);
      });
    },
    openRoleDialog(role) {
      if (role) {
        this.editingRole = {
          id: role.id,
          code: role.code,
          name: role.name,
          description: role.description
        };
      } else {
        this.editingRole = { id: null, code: '', name: '', description: '' };
      }
      this.roleDialog = true;
    },
    async saveRole() {
      if (!this.editingRole.code || !this.editingRole.name) {
        this.$message.error('请填写角色编码和角色名称');
        return;
      }
      try {
        if (this.editingRole.id) {
          await updateRole(this.editingRole.id, this.editingRole);
        } else {
          await addRole({ ...this.editingRole, isSystem: false, permissionCodes: [] });
        }
        this.roleDialog = false;
        await this.loadRoles();
        await this.loadAllRoleOptions();
        this.$message.success('角色已保存');
      } catch (error) {
        // Error already shown by axios interceptor
      }
    },
    removeRole(role) {
      this.$confirm('确认删除该角色吗？', '确认删除', { type: 'warning' }).then(async () => {
      await this.loadAllRoleOptions();
      await this.loadEmployees();
        try {
          await deleteRole(role.id);
        } catch (error) {}
        await this.loadRoles();
        await this.loadAllRoleOptions();
        await this.loadEmployees();
        this.$message.success('角色已删除');
      }).catch(() => {});
    },
    async saveGrant() {
      if (!this.grantRole || this.grantRole.isSystem || !this.$refs.grantTree) return;
      const checked = this.$refs.grantTree.getCheckedKeys();
      const halfChecked = this.$refs.grantTree.getHalfCheckedKeys();
      const permissionCodes = Array.from(new Set([...(checked || []), ...(halfChecked || [])]));
      try {
        await saveRolePermissions(this.grantRole.id, permissionCodes);
      } catch (error) {}
      await this.loadRoles();
      await this.loadAllRoleOptions();
      this.grantDialog = false;
      this.$message.success('保存权限成功，已向后端提交授权请求');
    },
    async saveEmployeeRoles() {
      if (!this.editingEmployee.id) return;
      try {
        await configureAdminRoles(this.editingEmployee.id, this.editingEmployee.roleIds);
      } catch (error) {}
      this.employeeRoleDialog = false;
      await this.loadEmployees();
      this.$message.success('员工角色配置已保存，已向后端提交请求');
    }
  }
};
</script>

<style scoped>
.permission-page { min-height: 100%; }
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.filter-form { margin-bottom: 8px; }
</style>
