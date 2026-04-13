import axios from 'axios';
import { Message } from 'element-ui';
import router from '@/router';
import { getToken, clearToken } from '@/utils/token';

const service = axios.create({
  baseURL: '/api',
  timeout: 12000
});

service.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && typeof payload === 'object') {
      if (Object.prototype.hasOwnProperty.call(payload, 'code')) {
        if (payload.code === 0 || payload.code === 200) {
          return payload.data;
        }
        Message.error(payload.message || '请求失败');
        if (payload.code === 401) {
          clearToken();
          router.push('/login');
        }
        return Promise.reject(payload);
      }
      if (Object.prototype.hasOwnProperty.call(payload, 'success')) {
        if (payload.success) {
          return payload.data;
        }
        Message.error(payload.msg || '请求失败');
        return Promise.reject(payload);
      }
    }
    return payload;
  },
  (error) => {
    const status = error.response && error.response.status;
    if (status === 401) {
      clearToken();
      Message.error('登录已失效，请重新登录');
      router.push('/login');
    } else if (status === 403 && !getToken()) {
      clearToken();
      Message.error('登录已失效，请重新登录');
      router.push('/login');
    } else if (status === 403) {
      Message.error('没有操作权限');
    } else if (error.code === 'ECONNABORTED') {
      Message.error('请求超时，请稍后重试');
    } else {
      Message.error('网络异常，请检查服务状态');
    }
    return Promise.reject(error);
  }
);

export default service;
