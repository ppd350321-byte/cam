import request from '@/api/request';

/**
 * 获取生产任务列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 分页任务数组
 */
export function listProductionTasks(params = {}) {
  return request({ url: '/production/tasks', method: 'get', params });
}

/**
 * 更新任务状态
 * @param {string} id 任务ID
 * @param {'start'|'complete'} action 操作类型
 * @returns {Promise<Object>} { id, status, progress }
 */
export function updateTaskStatus(id, action) {
  return request({ url: `/production/tasks/${id}/action`, method: 'post', data: { action } });
}


