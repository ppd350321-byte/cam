<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>社区食堂营运管理系统</h2>
      <p>Community Canteen Operation System</p>
      <el-form :model="form" @submit.native.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入管理员账号" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password />
        </el-form-item>
        <el-form-item style="margin-top: 16px;">
          <el-button type="success" style="width: 100%;" @click="submit">登录系统</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { login } from '@/api/modules/auth';
import { setToken, setCurrentUser } from '@/utils/token';

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      error: ''
    };
  },
  methods: {
    async submit() {
      this.error = '';
      try {
        const data = await login(this.form);
        setToken(data && data.token ? data.token : 'server-token');
        setCurrentUser(data && data.user ? data.user : null);
        this.$message.success('登录成功，欢迎回来！');
        this.$router.push('/dashboard');
      } catch (error) {
        this.error = '用户名或密码错误';
      }
    }
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfeff 50%, #f8fafc 100%);
}
.login-card {
  width: 420px;
}
h2 { margin: 0 0 6px 0; text-align: center; color: #0f172a; }
p { margin: 0 0 20px 0; text-align: center; color: #64748b; }
</style>
