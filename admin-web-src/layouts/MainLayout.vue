<template>
  <div class="layout-wrap">
    <div v-if="isMobile && mobileMenuOpen" class="mobile-mask" @click="mobileMenuOpen = false" />
    <el-container class="layout-root">
    <el-aside class="layout-aside" :width="isMobile ? '220px' : '220px'" v-show="!isMobile || mobileMenuOpen">
      <div class="brand">社区食堂营运系统</div>
      <el-menu :default-active="$route.path" background-color="#0f172a" text-color="#cbd5e1" active-text-color="#34d399" @select="onSelect">
        <el-menu-item v-if="$hasPermission('dashboard:view')" index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item v-if="$hasPermission('users:view')" index="/users">用户管理</el-menu-item>
        <el-menu-item v-if="$hasPermission('orders:view')" index="/orders">订单管理</el-menu-item>
        <el-menu-item v-if="$hasPermission('menu:view')" index="/menu-management">菜品管理</el-menu-item>
        <el-menu-item v-if="$hasPermission('supply:view')" index="/supply">供应链配置</el-menu-item>
        <el-menu-item v-if="$hasPermission('production:view')" index="/production">生产调度</el-menu-item>
        <el-menu-item v-if="$hasPermission('reports:view')" index="/reports">报表中心</el-menu-item>
        <el-menu-item v-if="$isAdmin() && $hasPermission('vip-coupon:view')" index="/vip-coupon-config">VIP与优惠券</el-menu-item>
        <el-menu-item v-if="$isAdmin() && $hasPermission('permission-config:view')" index="/permission-config">权限配置</el-menu-item>
        <el-menu-item v-if="$isAdmin() && $hasPermission('permission-config:view')" index="/employee-management">员工管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <el-button v-if="isMobile" type="text" @click="mobileMenuOpen = !mobileMenuOpen">菜单</el-button>
        <h3 class="page-title">{{ pageTitle }}</h3>
        <el-dropdown @command="handleHeaderCommand" trigger="click">
          <span class="header-user-btn">{{ headerUserName }} <i class="el-icon-arrow-down el-icon--right"></i></span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="logout" icon="el-icon-switch-button">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-header>
      <el-main class="layout-main" @click.native="closeMobileMenu">
        <router-view />
      </el-main>
    </el-container>
    </el-container>
  </div>
</template>

<script>
import { clearToken, getCurrentUser } from '@/utils/token';

export default {
  name: 'MainLayout',
  data() {
    return {
      mobileMenuOpen: false,
      isMobile: window.innerWidth < 960
    };
  },
  computed: {
    pageTitle() {
      return this.$route.meta.title || '社区食堂营运管理系统';
    },
    currentUser() {
      return getCurrentUser() || {};
    },
    headerUserName() {
      const name = this.currentUser.name || this.currentUser.username || '未知用户';
      const roleCount = Array.isArray(this.currentUser.roleCodes) ? this.currentUser.roleCodes.length : 0;
      const permissionCount = Array.isArray(this.currentUser.permissions) ? this.currentUser.permissions.length : 0;
      return this.$isAdmin() ? `${name} (管理员 | 角色${roleCount} | 权限${permissionCount})` : `${name} (员工 | 角色${roleCount} | 权限${permissionCount})`;
    }
  },
  mounted() {
    window.addEventListener('resize', this.onResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);
  },
  methods: {
    onResize() {
      this.isMobile = window.innerWidth < 960;
      if (!this.isMobile) {
        this.mobileMenuOpen = false;
      }
    },
    closeMobileMenu() {
      if (this.isMobile) {
        this.mobileMenuOpen = false;
      }
    },
    onSelect(path) {
      this.$router.push(path);
      this.mobileMenuOpen = false;
    },
    handleHeaderCommand(command) {
      if (command === 'logout') {
        this.confirmLogout();
      }
    },
    confirmLogout() {
      this.$confirm('您确定要退出当前账号吗？未保存的更改可能会丢失。', '确认退出', {
        type: 'warning',
        confirmButtonText: '确认退出',
        cancelButtonText: '取消'
      }).then(() => {
        clearToken();
        this.$message.info('已安全退出系统');
        this.$router.push('/login');
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.layout-wrap { min-height: 100vh; position: relative; }
.layout-root { min-height: 100vh; }
.layout-aside { background: #0f172a; color: #cbd5e1; position: relative; }
.brand { height: 60px; line-height: 60px; color: #fff; font-weight: 700; text-align: center; border-bottom: 1px solid #1e293b; }
.layout-header { background: #fff; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; }
.page-title { margin: 0; color: #1e293b; }
.header-user-btn { color: #475569; font-size: 13px; cursor: pointer; display: flex; align-items: center; }
.header-user-btn:hover { color: #10b981; }
.layout-main { background: #f8fafc; }
.mobile-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 9;
}
@media (max-width: 959px) {
  .layout-aside {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 10;
    height: 100vh;
  }
}
</style>
