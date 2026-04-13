import request from '@/api/request';

/**
 * 获取用户列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 分页用户列表
 */
export function listUsers(params) {
  return request({ url: '/users', method: 'get', params });
}

/**
 * 更新用户资料
 * @param {string} id 用户ID
 * @param {Object} data 更新字段
 * @returns {Promise<Object>} 更新后的用户对象
 */
export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data });
}

/**
 * 余额充值
 * @param {string} id 用户ID
 * @param {number} amount 充值金额
 * @returns {Promise<Object>} { id, balance }
 */
export function rechargeUser(id, amount) {
  return request({ url: `/users/${id}/recharge`, method: 'post', data: { amount } });
}

/**
 * 增加用户积分
 * @param {string} id 用户ID
 * @param {number} points 发放积分值
 * @returns {Promise<Object>} { id, points }
 */
export function addUserPoints(id, points) {
  return request({ url: `/users/${id}/points`, method: 'post', data: { points } });
}

/**
 * 切换用户状态
 * @param {string} id 用户ID
 * @param {'active'|'disabled'} status 用户状态
 * @returns {Promise<Object>} { id, status }
 */
export function toggleUserStatus(id, status) {
  return request({ url: `/users/${id}/status`, method: 'patch', data: { status } });
}
