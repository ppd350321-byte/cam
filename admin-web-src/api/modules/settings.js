import request from '@/api/request';

/**
 * 获取系统设置
 * @returns {Promise<Object>} 系统设置对象
 */
export function getSettings() {
  return request({ url: '/settings', method: 'get' });
}

/**
 * 保存全部系统设置
 * @param {Object} data 设置对象
 * @returns {Promise<Object>} 保存结果
 */
export function saveSettings(data) {
  return request({ url: '/settings', method: 'put', data });
}

/**
 * 获取角色分页列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 角色分页结果
 */
export function listRoles(params = {}) {
  return request({ url: '/settings/roles', method: 'get', params });
}

/**
 * 获取全部权限树
 * @returns {Promise<Array>} 权限树
 */
export function listPermissionTree() {
  return request({ url: '/settings/permissions/tree', method: 'get' });
}

/**
 * 获取充值规则分页列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 充值规则分页结果
 */
export function listRechargeRules(params = {}) {
  return request({ url: '/settings/recharge-rules', method: 'get', params });
}

/**
 * 新增角色
 * @param {Object} data 角色信息
 * @returns {Promise<Object>} 新增后的角色对象
 */
export function addRole(data) {
  return request({ url: '/settings/roles', method: 'post', data });
}

/**
 * 更新角色
 * @param {number|string} id 角色ID
 * @param {Object} data 更新字段
 * @returns {Promise<Object>} 更新后的角色对象
 */
export function updateRole(id, data) {
  return request({ url: `/settings/roles/${id}`, method: 'put', data });
}

/**
 * 保存角色授权
 * @param {number|string} roleId 角色ID
 * @param {string[]} permissionCodes 授权后的权限编码数组
 * @returns {Promise<Object>} 授权保存结果
 */
export function saveRolePermissions(roleId, permissionCodes) {
  return request({ url: `/settings/roles/${roleId}/permissions`, method: 'put', data: { permissionCodes } });
}

/**
 * 删除角色
 * @param {number|string} id 角色ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteRole(id) {
  return request({ url: `/settings/roles/${id}`, method: 'delete' });
}

/**
 * 获取管理员/员工分页列表（t_admin）
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 分页管理员列表
 */
export function listAdminEmployees(params = {}) {
  return request({ url: '/admin/employees', method: 'get', params });
}

/**
 * 为管理员配置角色
 * @param {number|string} id 管理员ID
 * @param {Array} roleIds 角色ID数组
 * @returns {Promise<Object>} 配置结果
 */
export function configureAdminRoles(id, roleIds) {
  return request({ url: `/admin/employees/${id}/roles`, method: 'put', data: { roleIds } });
}

/**
 * 新增管理员/员工
 * @param {Object} data 员工信息
 * @returns {Promise<Object>} 新增后的员工对象
 */
export function createAdminEmployee(data) {
  return request({ url: '/admin/employees', method: 'post', data });
}

/**
 * 更新管理员/员工
 * @param {number|string} id 员工ID
 * @param {Object} data 更新字段
 * @returns {Promise<Object>} 更新后的员工对象
 */
export function updateAdminEmployee(id, data) {
  return request({ url: `/admin/employees/${id}`, method: 'put', data });
}

/**
 * 删除管理员/员工
 * @param {number|string} id 员工ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteAdminEmployee(id) {
  return request({ url: `/admin/employees/${id}`, method: 'delete' });
}

// ── VIP 等级配置 ──
export function listVipLevels() {
  return request({ url: '/admin/vip-coupon/vip-levels', method: 'get' });
}

export function addVipLevel(data) {
  return request({ url: '/admin/vip-coupon/vip-levels', method: 'post', data });
}

export function updateVipLevel(id, data) {
  return request({ url: `/admin/vip-coupon/vip-levels/${id}`, method: 'put', data });
}

export function deleteVipLevel(id) {
  return request({ url: `/admin/vip-coupon/vip-levels/${id}`, method: 'delete' });
}

// ── 优惠券模板配置 ──
export function listCouponTemplates(params = {}) {
  return request({ url: '/admin/vip-coupon/coupons', method: 'get', params });
}

export function addCouponTemplate(data) {
  return request({ url: '/admin/vip-coupon/coupons', method: 'post', data });
}

export function updateCouponTemplate(id, data) {
  return request({ url: `/admin/vip-coupon/coupons/${id}`, method: 'put', data });
}

export function deleteCouponTemplate(id) {
  return request({ url: `/admin/vip-coupon/coupons/${id}`, method: 'delete' });
}
