import request from '@/api/request';

/**
 * 获取订单列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 分页订单结果
 */
export function listOrders(params) {
  return request({ url: '/orders', method: 'get', params });
}

/**
 * 更新订单状态
 * @param {string} id 订单号
 * @param {string} orderStatus 新状态代码
 * @param {Object} extra 额外参数（如 chefId）
 * @returns {Promise<Object>} { id, orderStatus, orderStatusLabel }
 */
export function updateOrderStatus(id, orderStatus, extra = {}) {
  return request({ url: `/orders/${id}/status`, method: 'patch', data: { orderStatus, ...extra } });
}

export function approveCancelOrder(id) {
  return request({ url: `/orders/${id}/approve-cancel`, method: 'post' });
}

export function rejectCancelOrder(id) {
  return request({ url: `/orders/${id}/reject-cancel`, method: 'post' });
}

/**
 * 获取厨师列表（拥有 chef 角色的员工）
 * @returns {Promise<Array>} [{ id, name }]
 */
export function listChefs() {
  return request({ url: '/orders/chefs', method: 'get' });
}
