import request from '@/api/request';

/**
 * 获取仪表盘概览数据
 * @returns {Promise<Object>} { stats[], revenueData[], productionData[] }
 */
export function getDashboardOverview() {
  return request({ url: '/dashboard/overview', method: 'get' });
}

/**
 * 获取最新订单列表
 * @param {Object} params 查询参数
 * @param {number} [params.page=1] 页码
 * @param {number} [params.pageSize=5] 每页条数
 * @returns {Promise<Object>} 分页订单结果
 */
export function getRecentOrders(params = {}) {
  return request({ url: '/dashboard/recent-orders', method: 'get', params });
}
