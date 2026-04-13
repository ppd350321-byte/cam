import request from '@/api/request';

// ── 原料管理 ──
export function listMaterials(params = {}) {
  return request({ url: '/supply/materials', method: 'get', params });
}

export function listAllMaterials() {
  return request({ url: '/supply/materials/all', method: 'get' });
}

export function listMaterialCategories() {
  return request({ url: '/supply/materials/categories', method: 'get' });
}

export function addMaterial(data) {
  return request({ url: '/supply/materials', method: 'post', data });
}

export function updateMaterial(id, data) {
  return request({ url: `/supply/materials/${id}`, method: 'put', data });
}

export function deleteMaterial(id) {
  return request({ url: `/supply/materials/${id}`, method: 'delete' });
}

/**
 * 查询供应商列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 供应商分页数组
 */
export function listSuppliers(params = {}) {
  return request({ url: '/supply/suppliers', method: 'get', params });
}

/**
 * 新增供应商
 * @param {Object} data 供应商信息
 * @returns {Promise<Object>} 新增后的供应商对象
 */
export function addSupplier(data) {
  return request({ url: '/supply/suppliers', method: 'post', data });
}

/**
 * 查询采购单列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 采购单分页数组
 */
export function listProcurements(params = {}) {
  return request({ url: '/supply/procurements', method: 'get', params });
}

/**
 * 新增采购单
 * @param {Object} data 采购单信息
 * @returns {Promise<Object>} 新增后的采购单
 */
export function addProcurement(data) {
  return request({ url: '/supply/procurements', method: 'post', data });
}

/**
 * 更新采购单状态
 * @param {string} id 采购单号
 * @param {string} status 新状态
 * @returns {Promise<Object>} { id, status }
 */
export function updateProcurementStatus(id, status) {
  return request({ url: `/supply/procurements/${id}/status`, method: 'patch', data: { status } });
}
