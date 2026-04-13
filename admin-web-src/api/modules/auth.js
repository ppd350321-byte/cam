import request from '@/api/request';

/**
 * 登录接口
 * @param {Object} data 登录参数
 * @param {string} data.username 管理员账号
 * @param {string} data.password 管理员密码
 * @returns {Promise<Object>} { token, user: { id, username, name, isAdmin, roleCodes, roleIds, permissions }, expiresAt }
 */
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data });
}

/**
 * 退出登录接口
 * @returns {Promise<Object>} { success, message }
 */
export function logout() {
  return request({ url: '/auth/logout', method: 'post' });
}
