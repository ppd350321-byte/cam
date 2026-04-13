import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import router from './router';
import './index.css';
import { getCurrentUser } from '@/utils/token';
import { getUserPermissions, hasPermission, hasAnyPermission, isAdmin } from '@/utils/permission';

Vue.config.productionTip = false;
Vue.use(ElementUI);

Vue.mixin({
  methods: {
    $hasPermission(code) {
      return hasPermission(getCurrentUser(), code);
    },
    $hasAnyPermission(codes) {
      return hasAnyPermission(getCurrentUser(), codes);
    },
    $getCurrentPermissions() {
      return getUserPermissions(getCurrentUser());
    },
    $isAdmin() {
      return isAdmin(getCurrentUser());
    }
  }
});

new Vue({
  router,
  render: (h) => h(App)
}).$mount('#root');
