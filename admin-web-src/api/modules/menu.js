import request from '@/api/request';

// ── 分类管理 ──

export function listCategories() {
  return request({ url: '/admin/menu/categories', method: 'get' });
}

export function createCategory(data) {
  return request({ url: '/admin/menu/categories', method: 'post', data });
}

export function updateCategory(id, data) {
  return request({ url: `/admin/menu/categories/${id}`, method: 'put', data });
}

export function deleteCategory(id) {
  return request({ url: `/admin/menu/categories/${id}`, method: 'delete' });
}

// ── 菜品管理 ──

export function listDishes(params) {
  return request({ url: '/admin/menu/dishes', method: 'get', params });
}

export function createDish(data) {
  return request({ url: '/admin/menu/dishes', method: 'post', data });
}

export function updateDish(id, data) {
  return request({ url: `/admin/menu/dishes/${id}`, method: 'put', data });
}

export function deleteDish(id) {
  return request({ url: `/admin/menu/dishes/${id}`, method: 'delete' });
}
